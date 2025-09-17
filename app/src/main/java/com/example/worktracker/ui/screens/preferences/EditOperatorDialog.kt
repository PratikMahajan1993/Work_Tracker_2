package com.example.worktracker.ui.screens.preferences

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.worktracker.data.database.entity.OperatorInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditOperatorDialog(
    showDialog: Boolean,
    editingOperator: OperatorInfo?, 
    uiState: PreferencesUiState,
    onNameChange: (String) -> Unit,
    onHourlySalaryChange: (String) -> Unit,
    onRoleChange: (String) -> Unit,
    onPriorityChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onNotesForAiChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (showDialog && editingOperator != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Edit Operator") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Operator ID (Display only, not editable)
                    Text("Operator ID: ${editingOperator.operatorId}", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Operator Name
                    OutlinedTextField(
                        value = uiState.operatorNameInput,
                        onValueChange = onNameChange,
                        label = { Text("Operator Name*") },
                        singleLine = true,
                        isError = uiState.operatorNameError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    uiState.operatorNameError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Hourly Salary
                    OutlinedTextField(
                        value = uiState.operatorHourlySalaryInput,
                        onValueChange = onHourlySalaryChange,
                        label = { Text("Hourly Salary*") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = uiState.operatorHourlySalaryError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    uiState.operatorHourlySalaryError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Role
                    OutlinedTextField(
                        value = uiState.operatorRoleInput,
                        onValueChange = onRoleChange,
                        label = { Text("Role*") },
                        singleLine = true,
                        isError = uiState.operatorRoleError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    uiState.operatorRoleError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Priority Level
                    OutlinedTextField(
                        value = uiState.operatorPriorityInput,
                        onValueChange = onPriorityChange,
                        label = { Text("Priority (1-5)*") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = uiState.operatorPriorityError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    uiState.operatorPriorityError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Notes
                    OutlinedTextField(
                        value = uiState.operatorNotesInput,
                        onValueChange = onNotesChange,
                        label = { Text("Notes (Optional)") },
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Notes for AI Analysis
                    OutlinedTextField(
                        value = uiState.operatorNotesForAiInput,
                        onValueChange = onNotesForAiChange,
                        label = { Text("Notes for AI (Optional)") },
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = onConfirm) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}
