package com.example.worktracker.ui.screens.history

import com.example.worktracker.data.database.entity.ProductionActivity
import com.example.worktracker.data.database.entity.WorkActivityLog

sealed class HistoryListItem {
    abstract val startTime: Long // Common property for sorting

    data class WorkHistoryItem(
        val workLog: WorkActivityLog,
        override val startTime: Long = workLog.startTime
    ) : HistoryListItem()

    data class ProductionHistoryItem(
        val productionLog: ProductionActivity,
        val boyName: String, // Include The Boy's name
        override val startTime: Long = productionLog.startTime
    ) : HistoryListItem()
}
