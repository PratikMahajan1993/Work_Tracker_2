package com.example.worktracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.worktracker.data.database.entity.TheBoysInfo
import com.example.worktracker.ui.screens.preferences.PreferencesUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTheBoyDialog(
    showDialog: Boolean,
    editingTheBoy: TheBoysInfo?,
    uiState: PreferencesUiState,
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
                    Text("Boy ID: ${editingTheBoy.boyId}", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Boy Name
                    OutlinedTextField(
                        value = uiState.editBoyNameInput,
                        onValueChange = onNameChange,
                        label = { Text("Boy Name*") },
                        singleLine = true,
                        isError = uiState.editBoyNameError != null,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        modifier = Modifier.fillMaxWidth()
                    )
                    uiState.editBoyNameError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Role
                    OutlinedTextField(
                        value = uiState.editBoyRoleInput,
                        onValueChange = onRoleChange,
                        label = { Text("Role*") },
                        singleLine = true,
                        isError = uiState.editBoyRoleError != null,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        modifier = Modifier.fillMaxWidth()
                    )
                    uiState.editBoyRoleError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Notes
                    OutlinedTextField(
                        value = uiState.editBoyNotesInput,
                        onValueChange = onNotesChange,
                        label = { Text("Notes (Optional)") },
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Notes for AI Analysis
                    OutlinedTextField(
                        value = uiState.editBoyNotesForAiInput,
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
