package com.example.worktracker.ui.screens.workdetails

import com.example.worktracker.data.database.entity.ComponentInfo
import com.example.worktracker.data.database.entity.TheBoysInfo // Import TheBoysInfo

// Represents the UI state for the WorkDetailsScreen
data class WorkDetailsState(
    val currentLogId: Long? = null, // ID of the currently *active* (started) log in a session
    val categoryName: String = "Default Category",
    val startTime: Long? = null,
    val endTime: Long? = null, // Stores the endTime if the log is completed
    val duration: Long? = null, // Stores the duration if the log is completed
    val description: String = "",
    val operatorId: String = "",
    val expenses: String = "",
    val isEndButtonEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,

    // Fields for work details
    val logDate: Long = System.currentTimeMillis(),
    val taskSuccessful: Boolean? = null,
    val assignedBy: String? = null,

    // Fields for Component Logging
    val availableComponents: List<ComponentInfo> = emptyList(),
    val selectedComponentIds: Set<Long> = emptySet(),
    val selectedComponentsInSession: List<ComponentInfo> = emptyList(),
    val showComponentSelectionDialog: Boolean = false,

    // Fields for TheBoys Logging
    val availableTheBoys: List<TheBoysInfo> = emptyList(),
    val selectedTheBoyIds: Set<Long> = emptySet(),
    val selectedTheBoysInSession: List<TheBoysInfo> = emptyList(), // Derived in ViewModel
    val showTheBoySelectionDialog: Boolean = false,

    // Fields for Edit Mode
    val isEditMode: Boolean = false,
    val editingWorkLogId: Long? = null, // ID of the log being edited
    val initialEndTimeForEdit: Long? = null, // To store original endTime when editing a completed log
    val initialDurationForEdit: Long? = null // To store original duration when editing a completed log
)
