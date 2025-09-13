package com.example.worktracker.ui.viewlogs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worktracker.data.database.relation.WorkActivityDetails // Import WorkActivityDetails
import com.example.worktracker.data.repository.WorkActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Data class to hold the formatted, display-ready information for a single log row.
 */
data class LogDisplayInfo(
    val srNo: String,
    val date: String,
    val activityCategory: String,
    val duration: String,
    val operatorId: String
)

/**
 * Represents the complete UI state for the ViewLogsScreen.
 */
data class ViewLogsUiState(
    val logs: List<LogDisplayInfo> = emptyList(),
    val operatorIdFilter: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class ViewLogsViewModel @Inject constructor(
    private val workActivityRepository: WorkActivityRepository
) : ViewModel() {

    private val _operatorIdFilter = MutableStateFlow("")

    val uiState: StateFlow<ViewLogsUiState> =
        combine(
            // Corrected: Use getAllWorkActivitiesWithDetails
            workActivityRepository.getAllWorkActivitiesWithDetails(),
            _operatorIdFilter
        ) { allDetails, filterText -> // Renamed to allDetails for clarity
            val filteredDetails = if (filterText.isBlank()) {
                allDetails
            } else {
                allDetails.filter { details ->
                    // Corrected: Access operatorId from details.workActivity
                    details.workActivity.operatorId?.toString()?.contains(filterText, ignoreCase = true) ?: false
                }
            }

            val displayLogs = filteredDetails.map { details ->
                val log = details.workActivity // Extract WorkActivityLog
                LogDisplayInfo(
                    srNo = log.id.toString(),
                    date = formatDate(log.logDate),
                    activityCategory = log.categoryName,
                    duration = formatDuration(log.startTime, log.endTime),
                    // Corrected: Access operatorId from log (which is workActivity)
                    operatorId = log.operatorId?.toString() ?: "N/A"
                )
            }

            ViewLogsUiState(
                logs = displayLogs,
                operatorIdFilter = filterText,
                isLoading = false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ViewLogsUiState(isLoading = true)
        )

    /**
     * Called when the user types in the operator ID filter text field.
     */
    fun onFilterChanged(newText: String) {
        _operatorIdFilter.value = newText
    }

    private fun formatDate(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val netDate = Date(timestamp)
            sdf.format(netDate)
        } catch (e: Exception) {
            "Unknown Date"
        }
    }

    private fun formatDuration(startTime: Long, endTime: Long?): String {
        if (endTime == null) {
            return "Ongoing"
        }
        val diff = endTime - startTime
        if (diff < 0) return "Invalid"

        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
