package com.example.worktracker.ui.screens.workdetails

import android.content.Context
import android.widget.Toast
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class WorkDetailsCoordinator(
    private val viewModel: GenericWorkDetailsViewModel,
    private val navController: NavController,
    private val coroutineScope: CoroutineScope, // Used for observing ViewModel events
    private val applicationContext: Context
) {
    val uiState: StateFlow<WorkDetailsState> = viewModel.uiState

    init {
        // Observe navigation events from the ViewModel
        viewModel.navigationEvent.onEach { event ->
            when (event) {
                is GenericWorkDetailsViewModel.NavigationEvent.NavigateBack -> {
                    // Access the latest state to get categoryName for the Toast
                    val categoryName = uiState.value.categoryName
                    val message = if (uiState.value.isEditMode) {
                        "Activity '${categoryName}' updated!"
                    } else {
                        "Activity for '${categoryName}' saved!"
                    }
                    Toast.makeText(
                        applicationContext,
                        message,
                        Toast.LENGTH_SHORT
                    ).show()
                    navController.popBackStack()
                }
            }
        }.launchIn(coroutineScope)
    }

    fun onDescriptionChanged(newDescription: String) {
        viewModel.onDescriptionChange(newDescription)
    }

    fun onOperatorIdChanged(newId: String) {
        viewModel.onOperatorIdChange(newId)
    }

    fun onExpensesChanged(newExpenses: String) {
        viewModel.onExpensesChange(newExpenses)
    }

    fun onStartPressed() {
        viewModel.onStartPressed()
    }

    fun onSaveOrUpdatePressed() { // Renamed from onEndPressed
        viewModel.onSaveOrUpdatePressed()
    }

    fun onNavigateBack() {
        navController.popBackStack()
    }

    fun onTaskSuccessChanged(isSuccess: Boolean) {
        viewModel.onTaskSuccessChanged(isSuccess)
    }

    fun onAssignedByChanged(assignee: String) {
        viewModel.onAssignedByChanged(assignee)
    }

    // Component selection methods
    fun onToggleComponentSelectionDialog(show: Boolean) {
        viewModel.onToggleComponentSelectionDialog(show)
    }

    fun onComponentSelected(componentId: Long, isSelected: Boolean) {
        viewModel.onComponentSelected(componentId, isSelected)
    }

    // The Boys selection methods
    fun onToggleTheBoySelectionDialog(show: Boolean) {
        viewModel.onToggleTheBoySelectionDialog(show)
    }

    fun onTheBoySelected(boyId: Long, isSelected: Boolean) {
        viewModel.onTheBoySelected(boyId, isSelected)
    }
}
