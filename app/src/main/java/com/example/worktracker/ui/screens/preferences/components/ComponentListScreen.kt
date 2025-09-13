package com.example.worktracker.ui.screens.preferences.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.worktracker.AppRoutes
import com.example.worktracker.data.database.entity.ComponentInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentListScreen(
    navController: NavHostController,
    viewModel: ManageComponentsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Component List") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                ActualComponentList(
                    components = uiState.components,
                    onEditComponent = {
                        // Navigate to ManageComponentsScreen with componentId for editing
                        navController.navigate("${AppRoutes.MANAGE_COMPONENTS}/${it.id}")
                    },
                    onDeleteComponent = { component ->
                        viewModel.handleEvent(ManageComponentsUiEvent.OnDeleteComponentClick(component))
                    }
                )
            }
        }

        if (uiState.showDeleteConfirmationDialog) {
            ActualDeleteConfirmationDialog(
                componentName = uiState.componentToDelete?.componentName ?: "",
                onConfirm = { viewModel.handleEvent(ManageComponentsUiEvent.OnConfirmDeleteComponent) },
                onDismiss = { viewModel.handleEvent(ManageComponentsUiEvent.OnDismissDeleteComponentDialog) }
            )
        }
    }
}

@Composable
private fun ActualComponentList(
    components: List<ComponentInfo>,
    onEditComponent: (ComponentInfo) -> Unit,
    onDeleteComponent: (ComponentInfo) -> Unit
) {
    if (components.isEmpty()) {
        Text("No components added yet. Add components from the 'Manage Components' screen.")
        return
    }
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(components, key = { it.id }) { component ->
            ActualComponentListItem(
                component = component,
                onEdit = { onEditComponent(component) },
                onDelete = { onDeleteComponent(component) }
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun ActualComponentListItem(
    component: ComponentInfo,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { /* Future: Could navigate to a read-only detail view if needed */ },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(component.componentName, style = MaterialTheme.typography.titleMedium)
                Text("Customer: ${component.customer}", style = MaterialTheme.typography.bodySmall)
                Text("Priority: ${component.priorityLevel}", style = MaterialTheme.typography.bodySmall)
                Text("Cycle Time: ${component.cycleTimeMinutes} min", style = MaterialTheme.typography.bodySmall)
                component.notesForAi?.let {
                    if(it.isNotBlank()) Text("AI Notes: $it", style = MaterialTheme.typography.bodySmall, maxLines = 2)
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@Composable
private fun ActualDeleteConfirmationDialog(
    componentName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Deletion") },
        text = { Text("Are you sure you want to delete component '$componentName'? This action cannot be undone.") },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
