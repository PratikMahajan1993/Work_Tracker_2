package com.example.worktracker.ui.screens.workdetails // Corrected package name

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel // Corrected import
import androidx.navigation.NavController
// It's good practice to ensure all necessary imports are here explicitly
// import com.example.worktracker.ui.screens.workdetails.GenericWorkDetailsViewModel // Assuming this is the correct FQN
// import com.example.worktracker.ui.screens.workdetails.WorkDetailsCoordinator // Assuming this is the correct FQN
// import com.example.worktracker.ui.screens.workdetails.WorkDetailsActions // Assuming this is the correct FQN
// import com.example.worktracker.ui.screens.workdetails.WorkDetailsScreen // Assuming this is the correct FQN

const val ROUTE_WORK_DETAILS = "work_details/{categoryName}?workLogId={workLogId}" // Updated to match AppRoutes.kt
const val ROUTE_ARG_CATEGORY_NAME = "categoryName"
// const val ROUTE_ARG_WORK_LOG_ID = "workLogId" // Already defined in ViewModel, not needed here if using NavController extensions correctly

fun NavController.navigateToWorkDetails(categoryName: String, workLogId: Long? = null) {
    val route = "work_details/$categoryName?workLogId=${workLogId ?: 0L}"
    this.navigate(route)
}

@Composable
fun WorkDetailsRoute(
    navController: NavController
) {
    val viewModel: GenericWorkDetailsViewModel = hiltViewModel()
    val applicationContext = LocalContext.current.applicationContext
    val coroutineScope = rememberCoroutineScope()

    val coordinator = remember(viewModel, navController, coroutineScope, applicationContext) {
        WorkDetailsCoordinator(
            viewModel = viewModel,
            navController = navController,
            coroutineScope = coroutineScope,
            applicationContext = applicationContext
        )
    }

    val uiState by viewModel.uiState.collectAsState() 

    val actions = remember(coordinator) {
        WorkDetailsActions(
            onDescriptionChanged = coordinator::onDescriptionChanged,
            onOperatorIdChanged = coordinator::onOperatorIdChanged,
            onExpensesChanged = coordinator::onExpensesChanged,
            onStartPressed = coordinator::onStartPressed,
            onSaveOrUpdatePressed = coordinator::onSaveOrUpdatePressed, // Changed from onEndPressed
            onNavigateBack = coordinator::onNavigateBack,
            onTaskSuccessChanged = coordinator::onTaskSuccessChanged,
            onAssignedByChanged = coordinator::onAssignedByChanged,
            // Component actions
            onToggleComponentSelectionDialog = coordinator::onToggleComponentSelectionDialog,
            onComponentSelected = coordinator::onComponentSelected
            // Removed The Boys actions
        )
    }

    WorkDetailsScreen(
        state = uiState,
        actions = actions
    )
}
