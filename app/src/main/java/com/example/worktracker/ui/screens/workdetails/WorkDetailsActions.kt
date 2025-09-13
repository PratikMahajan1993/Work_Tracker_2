package com.example.worktracker.ui.screens.workdetails

// Defines the user interactions for the WorkDetailsScreen
data class WorkDetailsActions(
    val onDescriptionChanged: (String) -> Unit = {},
    val onOperatorIdChanged: (String) -> Unit = {},
    val onExpensesChanged: (String) -> Unit = {},
    val onStartPressed: () -> Unit = {},
    val onSaveOrUpdatePressed: () -> Unit = {}, // Renamed from onEndPressed
    val onNavigateBack: () -> Unit = {}, // For handling back navigation from UI (e.g., top app bar)
    val onTaskSuccessChanged: (Boolean) -> Unit = {}, 
    val onAssignedByChanged: (String) -> Unit = {},

    // Actions for Component Selection
    val onToggleComponentSelectionDialog: (Boolean) -> Unit = {},
    val onComponentSelected: (componentId: Long, isSelected: Boolean) -> Unit = { _, _ -> },

    // Actions for TheBoys Selection
    val onToggleTheBoySelectionDialog: (Boolean) -> Unit = {},
    val onTheBoySelected: (boyId: Long, isSelected: Boolean) -> Unit = { _, _ -> }
)
