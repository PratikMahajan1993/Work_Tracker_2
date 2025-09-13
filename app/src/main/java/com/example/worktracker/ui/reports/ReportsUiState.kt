package com.example.worktracker.ui.reports

import com.example.worktracker.data.database.entity.WorkActivityLog

/**
 * Represents the display information for a single recent log in the drawer or list.
 */
data class RecentLogDisplayInfo(
    val id: Long,
    val categoryName: String,
    val startTimeFormatted: String,
    val durationFormatted: String
)

/**
 * Represents the overall UI state for the Reports Hub screen.
 */
data class ReportsUiState(
    // Fields for PDF export and general display
    val recentLogs: List<RecentLogDisplayInfo> = emptyList(),
    val isLoadingRecentLogs: Boolean = true,
    val generalError: String? = null,
    val selectedLogForPdfExport: WorkActivityLog? = null,
    val showPdfExportDialog: Boolean = false, // For the confirmation dialog
    val pdfExportMessage: String? = null,
    val showSelectLogForPdfDialog: Boolean = false, // New: For the log selection dialog

    // Fields for SMS functionality
    val showSelectLogForSmsDialog: Boolean = false,
    val logToSmsCandidates: List<WorkActivityLog> = emptyList(),
    val selectedLogForSms: WorkActivityLog? = null,
    val smsContentPreview: String? = null,
    val isGeneratingSmsContent: Boolean = false
)
