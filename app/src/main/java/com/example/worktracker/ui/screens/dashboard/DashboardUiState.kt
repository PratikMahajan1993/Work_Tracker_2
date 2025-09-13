package com.example.worktracker.ui.screens.dashboard

import com.example.worktracker.data.database.entity.WorkActivityLog

data class DashboardUiState(
    val ongoingActivities: List<WorkActivityLog> = emptyList(),
    val recentLogs: List<RecentActivityItem> = emptyList(), // Changed to List<RecentActivityItem>
    val isLoading: Boolean = true
)
