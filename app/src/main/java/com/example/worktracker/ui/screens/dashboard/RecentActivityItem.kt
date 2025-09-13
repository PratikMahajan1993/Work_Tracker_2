package com.example.worktracker.ui.screens.dashboard

import com.example.worktracker.data.database.entity.ProductionActivity
import com.example.worktracker.data.database.entity.WorkActivityLog

sealed interface RecentActivityItem {
    val timestamp: Long // Common property for sorting

    data class RecentWorkLogItem(val workLog: WorkActivityLog) : RecentActivityItem {
        override val timestamp: Long = workLog.startTime
    }

    data class RecentProductionLogItem(
        val productionLog: ProductionActivity,
        val operatorName: String? // Added to display operator name
    ) : RecentActivityItem {
        override val timestamp: Long = productionLog.startTime
    }
}
