package com.example.worktracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.worktracker.data.database.entity.TheBoysInfo
import com.example.worktracker.ui.screens.preferences.PreferencesUiState // Assuming this will be adapted for TheBoys

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTheBoyDialog(
    showDialog: Boolean,
    editingTheBoy: TheBoysInfo?, // Changed from OperatorInfo
    uiState: PreferencesUiState, // This will need to be adapted or a new UiState created for TheBoys
    // onIdChange: (String) -> Unit, // Boy ID is not directly editable in the form after creation
    onNameChange: (String) -> Unit,
    onRoleChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onNotesForAiChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (showDialog && editingTheBoy != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Edit 'Boy' Details") }, 
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Boy ID (Display only, not editable)
                    OutlinedTextField(
                        value = editingTheBoy.boyId.toString(), // Directly from the entity
                        onValueChange = { /* No-op, ID is not changed during edit */ },
                        label = { Text("Boy ID") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        enabled = false, 
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Assuming errors for Boy ID won't occur here as it's not an input
                    Spacer(modifier = Modifier.height(8.dp))

                    // Boy Name
                    OutlinedTextField(
                        value = uiState.editBoyNameInput ?: "", // Assuming field in UiState
                        onValueChange = onNameChange,
                        label = { Text("Boy Name*") },
                        singleLine = true,
                        isError = uiState.editBoyNameError != null, // Assuming error field in UiState
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        modifier = Modifier.fillMaxWidth()
                    )
                    uiState.editBoyNameError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Role
                    OutlinedTextField(
                        value = uiState.editBoyRoleInput ?: "", // Assuming field in UiState
                        onValueChange = onRoleChange,
                        label = { Text("Role*") },
                        singleLine = true,
                        isError = uiState.editBoyRoleError != null, // Assuming error field in UiState
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        modifier = Modifier.fillMaxWidth()
                    )
                    uiState.editBoyRoleError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Notes
                    OutlinedTextField(
                        value = uiState.editBoyNotesInput ?: "", // Assuming field in UiState
                        onValueChange = onNotesChange,
                        label = { Text("Notes (Optional)") },
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Notes for AI Analysis
                    OutlinedTextField(
                        value = uiState.editBoyNotesForAiInput ?: "", // Assuming field in UiState
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
