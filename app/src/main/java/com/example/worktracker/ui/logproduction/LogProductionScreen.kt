package com.example.worktracker.ui.logproduction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel // Corrected import
import androidx.navigation.NavController
import com.example.worktracker.data.database.entity.ComponentInfo
import com.example.worktracker.data.database.entity.TheBoysInfo
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogProductionScreen(
    navController: NavController,
    viewModel: LogProductionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiState.collectLatest { state ->
            state.snackbarMessage?.let {
                snackbarHostState.showSnackbar(it)
                viewModel.clearSnackbarMessage()
            }
        }
    }

    LaunchedEffect(uiState.navigateBack) {
        if (uiState.navigateBack) {
            navController.popBackStack()
            viewModel.onNavigatedBack() // Reset the flag
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) "Edit Production Log" else "Log Production Activity") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Navigate back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            if (uiState.isLoadingBoys) {
                CircularProgressIndicator(modifier = Modifier.padding(vertical = 8.dp))
            } else if (uiState.theBoysList.isEmpty()) {
                Text("No 'Boys' found. Please add them in Preferences first.")
            } else {
                TheBoySelector(uiState, viewModel)
            }

            if (uiState.isLoadingComponents) {
                CircularProgressIndicator(modifier = Modifier.padding(vertical = 8.dp))
            } else if (uiState.componentList.isEmpty()) {
                Text("No Components found. Please add them in Preferences first.")
            } else {
                ComponentSelector(uiState, viewModel)
            }

            OutlinedTextField(
                value = uiState.machineNumber,
                onValueChange = viewModel::onMachineNumberChange,
                label = { Text("Machine Number") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = uiState.machineNumberError != null,
                singleLine = true
            )
            uiState.machineNumberError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            OutlinedTextField(
                value = uiState.productionQuantity,
                onValueChange = viewModel::onProductionQuantityChange,
                label = { Text("Production Quantity") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = uiState.productionQuantityError != null,
                singleLine = true
            )
            uiState.productionQuantityError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            OutlinedTextField(
                value = uiState.rejectionQuantity,
                onValueChange = viewModel::onRejectionQuantityChange,
                label = { Text("Rejection Quantity (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = uiState.rejectionQuantityError != null,
                singleLine = true
            )
            uiState.rejectionQuantityError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            ProductionTimeDetailsSection(uiState, viewModel)
            
            Spacer(modifier = Modifier.height(16.dp))

            if (!uiState.isEditMode) {
                Button(
                    onClick = viewModel::resetFormAndTimer, 
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Reset All Fields")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = viewModel::onSaveOrUpdatePressed,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoadingBoys && !uiState.isLoadingComponents && !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (uiState.isEditMode) "Update Activity" else "Save Activity")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TheBoySelector(uiState: LogProductionUiState, viewModel: LogProductionViewModel) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = uiState.selectedBoy?.name ?: "Select a 'Boy'",
                onValueChange = {},
                readOnly = true,
                label = { Text("Select 'Boy'") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth(),
                isError = uiState.selectedBoyError != null
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth().exposedDropdownSize(true)
            ) {
                uiState.theBoysList.forEach { boy ->
                    DropdownMenuItem(
                        text = { Text("${boy.name} (ID: ${boy.boyId})") },
                        onClick = {
                            viewModel.onBoySelected(boy)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
    uiState.selectedBoyError?.let {
        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start=16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentSelector(uiState: LogProductionUiState, viewModel: LogProductionViewModel) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = uiState.selectedComponent?.componentName ?: "Select a Component",
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Component") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth(),
                isError = uiState.selectedComponentError != null
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth().exposedDropdownSize(true)
            ) {
                uiState.componentList.forEach { component ->
                    DropdownMenuItem(
                        text = { Text(component.componentName) },
                        onClick = {
                            viewModel.onComponentSelected(component)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
    uiState.selectedComponentError?.let {
        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start=16.dp))
    }
}


@Composable
fun ProductionTimeDetailsSection(uiState: LogProductionUiState, viewModel: LogProductionViewModel) {
    val timeDisplayFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateTimeDisplayFormat = remember { SimpleDateFormat("HH:mm:ss dd/MM/yy", Locale.getDefault()) }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = uiState.startTimeInput,
            onValueChange = viewModel::onStartTimeInputChange,
            label = { Text("Start Time (HH:mm)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii), 
            isError = uiState.startTimeError != null,
            singleLine = true
        )
        uiState.startTimeError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.downtimeInput,
            onValueChange = viewModel::onDowntimeInputChange,
            label = { Text("Downtime (minutes)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = uiState.downtimeError != null,
            singleLine = true
        )
        uiState.downtimeError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Parsed Start Time (for save): ${uiState.startTimeMillis?.let { dateTimeDisplayFormat.format(Date(it)) } ?: "--:--"}",
            style = MaterialTheme.typography.bodyMedium
        )

        if (uiState.isEditMode) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Original Start: ${uiState.initialStartTimeForEdit?.let { dateTimeDisplayFormat.format(Date(it)) } ?: "N/A"}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Original End: ${uiState.initialEndTimeForEdit?.let { dateTimeDisplayFormat.format(Date(it)) } ?: "N/A"}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Original Duration: ${uiState.initialDurationForEdit?.let { formatDuration(it) } ?: "N/A"}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

fun formatDuration(millis: Long): String {
    if (millis <= 0) return "00:00:00"
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

