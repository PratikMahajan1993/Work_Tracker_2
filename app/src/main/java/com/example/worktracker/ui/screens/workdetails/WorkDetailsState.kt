package com.example.worktracker.ui.screens.workdetails

import com.example.worktracker.data.database.entity.ComponentInfo

// Represents the UI state for the WorkDetailsScreen
data class WorkDetailsState(
    val currentLogId: Long? = null,
    val categoryName: String = "Default Category",
    val startTime: Long? = null,
    val endTime: Long? = null,
    val duration: Long? = null,
    val description: String = "",
    val operatorId: String = "",
    val expenses: String = "",
    val isEndButtonEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val logDate: Long = System.currentTimeMillis(),
    val taskSuccessful: Boolean? = null,
    val assignedBy: String? = null,
    val availableComponents: List<ComponentInfo> = emptyList(),
    val selectedComponentIds: Set<Long> = emptySet(),
    val selectedComponentsInSession: List<ComponentInfo> = emptyList(),
    val showComponentSelectionDialog: Boolean = false,
    val isEditMode: Boolean = false,
    val editingWorkLogId: Long? = null,
    val initialEndTimeForEdit: Long? = null,
    val initialDurationForEdit: Long? = null
)
