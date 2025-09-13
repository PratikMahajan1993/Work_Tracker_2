package com.example.worktracker.ui.screens.history

import com.example.worktracker.data.database.entity.ProductionActivity
import com.example.worktracker.data.database.entity.WorkActivityLog

data class HistoryUiState(
    val historyItems: List<HistoryListItem> = emptyList(),
    val isLoading: Boolean = true,
    val showDeleteConfirmationDialog: Boolean = false,
    val workLogToDelete: WorkActivityLog? = null,
    val productionLogToDelete: ProductionActivity? = null,
    val deleteDialogItemName: String = ""
)
