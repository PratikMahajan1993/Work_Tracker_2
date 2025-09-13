package com.example.worktracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun SetPasswordDialog(
    showDialog: Boolean,
    newPasswordInput: String,
    onNewPasswordInputChange: (String) -> Unit,
    confirmPasswordInput: String,
    onConfirmPasswordInputChange: (String) -> Unit,
    newPasswordError: String?,
    confirmPasswordError: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Set Master Reset Password") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newPasswordInput,
                        onValueChange = onNewPasswordInputChange,
                        label = { Text("New Password") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = PasswordVisualTransformation(),
                        isError = newPasswordError != null,
                        supportingText = {
                            if (newPasswordError != null) {
                                Text(
                                    text = newPasswordError,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPasswordInput,
                        onValueChange = onConfirmPasswordInputChange,
                        label = { Text("Confirm New Password") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = PasswordVisualTransformation(),
                        isError = confirmPasswordError != null,
                        supportingText = {
                            if (confirmPasswordError != null) {
                                Text(
                                    text = confirmPasswordError,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    // Optionally disable button if inputs are invalid or empty
                    enabled = newPasswordInput.isNotBlank() && confirmPasswordInput.isNotBlank() && newPasswordError == null && confirmPasswordError == null
                ) {
                    Text("Set Password")
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
