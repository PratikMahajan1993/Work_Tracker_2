package com.example.worktracker.ui.reports

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worktracker.core.PdfGenerator
import com.example.worktracker.data.database.entity.WorkActivityLog
import com.example.worktracker.data.database.relation.WorkActivityDetails // Import WorkActivityDetails
import com.example.worktracker.data.repository.WorkActivityRepository
import com.example.worktracker.di.AppModule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val workActivityRepository: WorkActivityRepository,
    private val application: Application,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<PdfExportEvent>()
    val eventFlow: SharedFlow<PdfExportEvent> = _eventFlow.asSharedFlow()

    private val timeFormatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    private val smsDateTimeFormatter = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())

    init {
        fetchRecentLogs()
    }

    private fun fetchRecentLogs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingRecentLogs = true) }
            workActivityRepository.getRecentWorkActivitiesWithDetails()
                .map { detailsList ->
                    detailsList.map { details ->
                        val log = details.workActivity
                        val duration: String = if (log.endTime != null) {
                            val diff = log.endTime - log.startTime
                            val hours = TimeUnit.MILLISECONDS.toHours(diff)
                            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
                            "${hours}h ${minutes}m"
                        } else {
                            "Ongoing"
                        }
                        RecentLogDisplayInfo(
                            id = log.id,
                            categoryName = log.categoryName,
                            startTimeFormatted = timeFormatter.format(Date(log.startTime)),
                            durationFormatted = duration
                        )
                    }
                }
                .catch { e ->
                    _uiState.update { it.copy(generalError = "Failed to load recent logs: ${e.message}", isLoadingRecentLogs = false) }
                }
                .collect { displayLogs ->
                    _uiState.update { it.copy(recentLogs = displayLogs, isLoadingRecentLogs = false) }
                }
        }
    }

    // Renamed to reflect it shows the *confirmation* dialog after a log is identified
    fun onShowPdfConfirmationDialog(logId: Long) { 
        viewModelScope.launch {
            val logDetails = workActivityRepository.getWorkActivityWithDetailsById(logId)
            if (logDetails != null) {
                _uiState.update {
                    it.copy(
                        selectedLogForPdfExport = logDetails.workActivity, 
                        showPdfExportDialog = true, // This shows the confirmation dialog
                        pdfExportMessage = null,
                        showSelectLogForPdfDialog = false // Close selection dialog if it was open
                    )
                }
            } else {
                _uiState.update { it.copy(generalError = "Could not find log details for PDF export.") }
            }
        }
    }
    
    // New function to handle click on the "Export Log as PDF" card/button
    fun onSelectLogForPdfExportClicked() {
        // recentLogs in UI state already contains RecentLogDisplayInfo, suitable for selection dialog
        // No need to fetch again unless data is stale, fetchRecentLogs is called in init.
        if (_uiState.value.recentLogs.isEmpty() && !_uiState.value.isLoadingRecentLogs) {
             _uiState.update { it.copy(generalError = "No recent logs available to export.") }
             return
        }
        _uiState.update { it.copy(showSelectLogForPdfDialog = true, generalError = null) }
    }

    // New function to dismiss the log selection dialog for PDF
    fun onDismissSelectLogForPdfDialog() {
        _uiState.update { it.copy(showSelectLogForPdfDialog = false) }
    }

    // New function called when a log is picked from the selection dialog for PDF
    // This will now directly call onShowPdfConfirmationDialog
    fun onLogSelectedForPdfAndShowConfirmation(logDisplayInfo: RecentLogDisplayInfo) {
        // The dialog will give RecentLogDisplayInfo, use its id to trigger the confirmation
        onShowPdfConfirmationDialog(logDisplayInfo.id)
    }

    fun onDismissPdfDialog() {
        _uiState.update { it.copy(showPdfExportDialog = false, selectedLogForPdfExport = null, pdfExportMessage = null) }
    }

    fun onConfirmPdfExport() {
        val logToExport = _uiState.value.selectedLogForPdfExport
        if (logToExport == null) {
            _uiState.update { it.copy(generalError = "No log selected for PDF export.", showPdfExportDialog = false) }
            return
        }

        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                PdfGenerator.generatePdfFromLog(application, logToExport)
            }
            val message = if (success) "PDF exported successfully to Downloads." else "Failed to export PDF."
            _eventFlow.emit(PdfExportEvent.ShowToast(message))
            _uiState.update {
                it.copy(
                    showPdfExportDialog = false,
                    selectedLogForPdfExport = null
                )
            }
        }
    }

    sealed class PdfExportEvent {
        data class ShowToast(val message: String) : PdfExportEvent()
        data class NavigateToSmsApp(val phoneNumber: String, val messageBody: String) : PdfExportEvent()
    }

    fun onSendActivityDetailsViaSmsClicked() {
        val preferredContact = sharedPreferences.getString(AppModule.KEY_SMS_CONTACT, "") ?: ""
        if (preferredContact.isBlank()) {
            viewModelScope.launch {
                _eventFlow.emit(PdfExportEvent.ShowToast("Please set a preferred SMS contact in Preferences."))
            }
            return
        }

        viewModelScope.launch {
            val recentDetailsList = workActivityRepository.getRecentWorkActivitiesWithDetails().firstOrNull() ?: emptyList()
            val candidates = recentDetailsList.map { it.workActivity }.take(10)

            if (candidates.isEmpty()) {
                _uiState.update { it.copy(generalError = "No recent logs available to send.") }
                return@launch
            }

            _uiState.update {
                it.copy(
                    showSelectLogForSmsDialog = true,
                    logToSmsCandidates = candidates, 
                    selectedLogForSms = null,
                    smsContentPreview = null,
                    isGeneratingSmsContent = false
                )
            }
        }
    }

    fun onDismissSelectLogForSmsDialog() {
        _uiState.update {
            it.copy(
                showSelectLogForSmsDialog = false,
                selectedLogForSms = null,
                logToSmsCandidates = emptyList(),
                smsContentPreview = null,
                isGeneratingSmsContent = false
            )
        }
    }

    fun onLogSelectedForSmsGeneration(log: WorkActivityLog) {
        _uiState.update {
            it.copy(
                selectedLogForSms = log, 
                isGeneratingSmsContent = true,
                smsContentPreview = null
            )
        }
        viewModelScope.launch {
            val previewText = formatSingleLogForSmsPreview(log)
            _uiState.update {
                it.copy(
                    smsContentPreview = previewText,
                    isGeneratingSmsContent = false
                )
            }
        }
    }

    private suspend fun formatSingleLogForSmsPreview(log: WorkActivityLog): String {
        delay(500) 

        val startTimeStr = smsDateTimeFormatter.format(Date(log.startTime))
        val durationStr: String = if (log.endTime != null) {
            val diff = log.endTime!! - log.startTime 
            val hours = TimeUnit.MILLISECONDS.toHours(diff)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
            "${hours}h ${minutes}m"
        } else {
            "Ongoing"
        }
        val descriptionStr = if (log.description.isNotBlank()) " Details: ${log.description}" else ""

        return "Work Log (${log.categoryName}): Started ${startTimeStr}, Duration: ${durationStr}.${descriptionStr}"
    }

    fun onConfirmAndSendSms() {
        val preferredContact = sharedPreferences.getString(AppModule.KEY_SMS_CONTACT, "") ?: ""
        val currentState = _uiState.value
        val selectedLog = currentState.selectedLogForSms 
        val messageBody = currentState.smsContentPreview

        if (preferredContact.isBlank()) {
            viewModelScope.launch {
                _eventFlow.emit(PdfExportEvent.ShowToast("Please set a preferred SMS contact in Preferences to send SMS."))
            }
            onDismissSelectLogForSmsDialog()
            return
        }

        if (selectedLog == null || messageBody.isNullOrBlank()) {
            viewModelScope.launch {
                _eventFlow.emit(PdfExportEvent.ShowToast("Log selection or message generation failed. Please try again."))
            }
            onDismissSelectLogForSmsDialog()
            return
        }

        viewModelScope.launch {
            _eventFlow.emit(PdfExportEvent.NavigateToSmsApp(phoneNumber = preferredContact, messageBody = messageBody))
        }
        onDismissSelectLogForSmsDialog()
    }
}
