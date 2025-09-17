package com.example.worktracker.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worktracker.data.repository.ProductionActivityRepository
import com.example.worktracker.data.repository.TheBoysRepository
import com.example.worktracker.data.repository.WorkActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val workActivityRepository: WorkActivityRepository,
    private val productionActivityRepository: ProductionActivityRepository,
    private val theBoysRepository: TheBoysRepository // Added TheBoysRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        fetchDashboardData()
    }

    private fun fetchDashboardData() {
        viewModelScope.launch {
            combine(
                workActivityRepository.getOngoingActivities(), // Flow<List<WorkActivityLog>>
                workActivityRepository.getRecentWorkActivitiesWithDetails().map { detailsList ->
                    detailsList.map { RecentActivityItem.RecentWorkLogItem(it.workActivity) } // Flow<List<RecentWorkLogItem>>
                },
                productionActivityRepository.getAllProductionActivities() // Flow<List<ProductionActivity>>
            ) { ongoingActivities, recentWorkLogItems, rawProductionLogs ->

                // Fetch TheBoysInfo for each production log to get operator names
                val recentProductionLogItems = rawProductionLogs.map { prodLog ->
                    val boyInfo = prodLog.boyId?.let { nnBoyId -> // Safely handle nullable boyId
                        theBoysRepository.getTheBoyById(nnBoyId)
                    }
                    RecentActivityItem.RecentProductionLogItem(prodLog, boyInfo?.name)
                }

                val combinedRecentLogs = (recentWorkLogItems + recentProductionLogItems)
                    .sortedByDescending { it.timestamp }
                    .take(10) // Optional: limit the number of recent items shown

                DashboardUiState(
                    ongoingActivities = ongoingActivities,
                    recentLogs = combinedRecentLogs,
                    isLoading = false
                )
            }.collect { combinedState ->
                _uiState.value = combinedState
            }
        }
    }
}
