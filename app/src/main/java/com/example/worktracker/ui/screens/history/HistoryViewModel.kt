package com.example.worktracker.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worktracker.data.database.entity.ProductionActivity
import com.example.worktracker.data.database.entity.WorkActivityLog
import com.example.worktracker.data.repository.ProductionActivityRepository
import com.example.worktracker.data.repository.TheBoysRepository
import com.example.worktracker.data.repository.WorkActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map // Import map operator
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val workActivityRepository: WorkActivityRepository,
    private val productionActivityRepository: ProductionActivityRepository,
    private val theBoysRepository: TheBoysRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        fetchHistoryItems()
    }

    private fun fetchHistoryItems() {
        viewModelScope.launch {
            val workLogsFlow = workActivityRepository.getAllWorkActivitiesWithDetails().map { detailsList ->
                detailsList.map { it.workActivity }
            }
            val productionLogsFlow = productionActivityRepository.getAllProductionActivities()
            val theBoysFlow = theBoysRepository.getAllTheBoys()

            combine(
                workLogsFlow,
                productionLogsFlow,
                theBoysFlow
            ) { workLogs, productionLogs, theBoysList ->
                val boysMap = theBoysList.associateBy({ it.boyId }, { it.name })
                val workHistoryItems = workLogs.map { HistoryListItem.WorkHistoryItem(it) }
                val productionHistoryItems = productionLogs.map {
                    HistoryListItem.ProductionHistoryItem(
                        productionLog = it,
                        boyName = boysMap[it.boyId] ?: "Unknown Boy"
                    )
                }
                val combinedList = (workHistoryItems + productionHistoryItems)
                    .sortedByDescending { it.startTime }

                // Preserve dialog state when history items are updated
                _uiState.value.copy(
                    historyItems = combinedList, 
                    isLoading = false
                )
            }.collect { updatedUiStateWithHistory ->
                 // Only update history and loading state, keep dialog state
                _uiState.update {
                    it.copy(
                        historyItems = updatedUiStateWithHistory.historyItems,
                        isLoading = updatedUiStateWithHistory.isLoading
                    )
                }
            }
        }
    }

    fun onDeleteWorkLogClicked(log: WorkActivityLog) {
        _uiState.update {
            it.copy(
                showDeleteConfirmationDialog = true,
                workLogToDelete = log,
                productionLogToDelete = null,
                deleteDialogItemName = log.categoryName ?: "Operator Activity"
            )
        }
    }

    fun onDeleteProductionLogClicked(productionLog: ProductionActivity) {
        _uiState.update {
            val itemName = productionLog.componentName ?: "Production Entry"
            it.copy(
                showDeleteConfirmationDialog = true,
                workLogToDelete = null,
                productionLogToDelete = productionLog,
                deleteDialogItemName = itemName
            )
        }
    }

    fun onConfirmDelete() {
        viewModelScope.launch {
            _uiState.value.workLogToDelete?.let {
                workActivityRepository.deleteLogById(it.id)
            }
            _uiState.value.productionLogToDelete?.let {
                productionActivityRepository.deleteProductionActivity(it)
            }
            // Refreshing the list will happen automatically due to flows
            // Reset dialog state after deletion
            _uiState.update {
                it.copy(
                    showDeleteConfirmationDialog = false,
                    workLogToDelete = null,
                    productionLogToDelete = null,
                    deleteDialogItemName = ""
                )
            }
        }
    }

    fun onDismissDeleteDialog() {
        _uiState.update {
            it.copy(
                showDeleteConfirmationDialog = false,
                workLogToDelete = null,
                productionLogToDelete = null,
                deleteDialogItemName = ""
            )
        }
    }
}
