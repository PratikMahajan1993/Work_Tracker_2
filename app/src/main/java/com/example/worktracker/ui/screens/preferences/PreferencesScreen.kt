package com.example.worktracker.ui.screens.preferences

import android.widget.Toast // Added for Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext // Added for Toast
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.worktracker.AppRoutes
import com.example.worktracker.data.database.entity.OperatorInfo
import com.example.worktracker.data.database.entity.TheBoysInfo
import com.example.worktracker.ui.components.ConfirmActionPasswordDialog
import com.example.worktracker.ui.components.SetPasswordDialog
import com.example.worktracker.ui.components.ManageCategoriesDialog
import com.example.worktracker.ui.components.AddCategoryDialog
import com.example.worktracker.ui.components.EditCategoryDialog
import com.example.worktracker.ui.components.TheBoysListDialog
import com.example.worktracker.ui.components.AddTheBoyDialog
import com.example.worktracker.ui.components.EditTheBoyDialog
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
// Removed: import androidx.compose.material.icons.filled.Login
// Removed: import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Login // Added AutoMirrored
import androidx.compose.material.icons.automirrored.filled.Logout // Added AutoMirrored

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    mainScreenPadding: PaddingValues,
    navController: NavController,
    viewModel: PreferencesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteOperatorConfirmDialog by remember { mutableStateOf<OperatorInfo?>(null) }
    var showDeleteTheBoyConfirmDialog by remember { mutableStateOf<TheBoysInfo?>(null) }
    var showSignOutConfirmDialog by remember { mutableStateOf(false) } // State for sign-out dialog

    val context = LocalContext.current // For Toast

    LaunchedEffect(key1 = uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearSnackbarMessage() // Important to prevent re-showing on recomposition
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(mainScreenPadding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Preferences",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Account Management Section
        Text(
            text = "Account Management",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
        )

        if (uiState.currentUser == null) {
            Button(
                onClick = { viewModel.onSignInClicked() },
                enabled = !uiState.isAccountActionInProgress,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Login, contentDescription = "Sign In Icon", modifier = Modifier.size(ButtonDefaults.IconSize)) // Updated Icon
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Sign In with Google")
            }
        } else {
            Text(
                text = "Signed in as: ${uiState.currentUser?.username ?: "User"}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Button(
                onClick = { showSignOutConfirmDialog = true },
                enabled = !uiState.isAccountActionInProgress,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sign Out Icon", modifier = Modifier.size(ButtonDefaults.IconSize)) // Updated Icon
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Sign Out")
            }
        }
        if (uiState.isAccountActionInProgress) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 8.dp))
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        // Other Preference Items from original file
        Text(
            text = "App Security & Data",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
        )
        Button(
            onClick = { viewModel.onShowSetPasswordDialog() },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Text(if (uiState.isPasswordSet) "Change Master Reset Password" else "Set Master Reset Password")
        }
        Button(
            onClick = { viewModel.onShowSmsContactDialog() },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Text(if (uiState.preferredSmsContact != null) "Change Preferred SMS Contact" else "Set Preferred SMS Contact")
        }
        uiState.preferredSmsContact?.let {
            Text(
                text = "Current SMS Contact: $it",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp, bottom = 16.dp)
            )
        }
        Button(
            onClick = { viewModel.onShowSetGeminiApiKeyDialog() },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Text(if (uiState.isGeminiApiKeySet) "Change Gemini API Key" else "Set Gemini API Key")
        }
        Text(
            text = if (uiState.isGeminiApiKeySet) "Gemini API Key: Set" else "Gemini API Key: Not Set",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 8.dp, bottom = 16.dp)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        Text(
            text = "Activity Categories",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
        )
        Button(
            onClick = { viewModel.onManageCategoriesClicked() },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Text("Manage Activity Categories")
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        // Manufacturing Components Section
        Text(
            text = "Manufacturing Components",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
        )
        Button(
            onClick = { navController.navigate(AppRoutes.MANAGE_COMPONENTS) }, 
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Text("Add/Edit Manufacturing Components")
        }
        Button(
            onClick = { navController.navigate(AppRoutes.VIEW_COMPONENTS) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Text("List of Components")
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        Text(
            text = "Operator Management",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
        )
        Button(
            onClick = { viewModel.onOperatorSectionClicked() },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Text(if (uiState.isOperatorSectionUnlocked) "Access Operator Information" else "Unlock Operator Information")
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        Text(
            text = "'The Boys' Management",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
        )
        Button(
            onClick = { viewModel.onTheBoysSectionClicked() },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Text(if (uiState.isOperatorSectionUnlocked) "Access 'The Boys' Information" else "Unlock 'The Boys' Information")
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            if (uiState.isPasswordSet) {
                Button(
                    onClick = { viewModel.onMasterResetClicked() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Master Reset (Wipe All Data)")
                }
            } else {
                Text(
                    "Set a password to enable Master Reset.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    // Sign Out Confirmation Dialog
    if (showSignOutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirmDialog = false },
            title = { Text("Confirm Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onSignOutClicked()
                        showSignOutConfirmDialog = false
                    }
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Existing Dialogs
    SetPasswordDialog(
        showDialog = uiState.showSetPasswordDialog,
        newPasswordInput = uiState.newPasswordInput,
        onNewPasswordInputChange = viewModel::onNewPasswordInputChange,
        confirmPasswordInput = uiState.confirmPasswordInput,
        onConfirmPasswordInputChange = viewModel::onConfirmPasswordInputChange,
        newPasswordError = uiState.newPasswordError,
        confirmPasswordError = uiState.confirmPasswordError,
        onConfirm = viewModel::onSavePasswordAttempt,
        onDismiss = viewModel::onDismissSetPasswordDialog
    )

    ConfirmActionPasswordDialog(
        showDialog = uiState.showMasterResetConfirmationDialog,
        title = "Confirm Master Reset",
        passwordInput = uiState.masterResetPasswordAttempt,
        onPasswordInputChange = viewModel::onMasterResetPasswordAttemptChange,
        passwordError = uiState.masterResetPasswordError,
        onConfirm = viewModel::onConfirmMasterReset,
        onDismiss = viewModel::onDismissMasterResetConfirmationDialog
    )

    SetSmsContactDialog(
        showDialog = uiState.showSmsContactDialog,
        smsContactInput = uiState.smsContactInput,
        onSmsContactInputChange = viewModel::onSmsContactInputChange,
        smsContactError = uiState.smsContactError,
        onConfirm = viewModel::onSaveSmsContact,
        onDismiss = viewModel::onDismissSmsContactDialog
    )

    SetGeminiApiKeyDialog(
        showDialog = uiState.showSetGeminiApiKeyDialog,
        apiKeyInput = uiState.geminiApiKeyInput,
        onApiKeyInputChange = viewModel::onGeminiApiKeyInputChange,
        apiKeyError = uiState.geminiApiKeyError,
        onConfirm = viewModel::onSaveGeminiApiKey,
        onDismiss = viewModel::onDismissSetGeminiApiKeyDialog
    )

    OperatorPasswordDialog(
        showDialog = uiState.showOperatorPasswordDialog,
        passwordAttempt = uiState.operatorPasswordAttempt,
        passwordError = uiState.operatorPasswordError,
        onPasswordChange = viewModel::onOperatorPasswordAttemptChange,
        onConfirm = viewModel::onUnlockSectionAttempt,
        onDismiss = viewModel::onDismissOperatorPasswordDialog
    )

    OperatorPasswordDialog(
        showDialog = uiState.showTheBoysPasswordDialog,
        passwordAttempt = uiState.operatorPasswordAttempt,
        passwordError = uiState.operatorPasswordError,
        onPasswordChange = viewModel::onOperatorPasswordAttemptChange,
        onConfirm = viewModel::onUnlockSectionAttempt,
        onDismiss = { viewModel.onDismissOperatorPasswordDialog() } // Assuming same dismissal logic as operator pwd dialog
    )

    OperatorListDialog(
        showDialog = uiState.showOperatorListDialog,
        operators = uiState.operators,
        onDismiss = viewModel::onDismissOperatorListDialog,
        onAddNewOperatorClicked = viewModel::onAddNewOperatorClicked,
        onEditOperatorClicked = viewModel::onEditOperatorClicked,
        onDeleteOperatorClicked = { operator -> showDeleteOperatorConfirmDialog = operator }
    )

    EditOperatorDialog(
        showDialog = uiState.showEditOperatorDialog,
        editingOperator = uiState.editingOperator,
        uiState = uiState,
        onIdChange = { /* ID not editable */ },
        onNameChange = viewModel::onOperatorNameChange,
        onHourlySalaryChange = viewModel::onOperatorHourlySalaryChange,
        onRoleChange = viewModel::onOperatorRoleChange,
        onPriorityChange = viewModel::onOperatorPriorityChange,
        onNotesChange = viewModel::onOperatorNotesChange,
        onNotesForAiChange = viewModel::onOperatorNotesForAiChange,
        onConfirm = viewModel::onSaveEditOperator,
        onDismiss = viewModel::onDismissEditOperatorDialog
    )

    AddOperatorDialog(
        showDialog = uiState.showAddOperatorDialog,
        uiState = uiState,
        onInputChange = viewModel::onNewOperatorInputChange,
        onNextStep = viewModel::onAddOperatorNextStep,
        onPreviousStep = viewModel::onAddOperatorPreviousStep,
        onSave = viewModel::onSaveNewOperator,
        onDismiss = viewModel::onDismissAddOperatorDialog
    )

    showDeleteOperatorConfirmDialog?.let {
        AlertDialog(
            onDismissRequest = { showDeleteOperatorConfirmDialog = null },
            title = { Text("Confirm Delete Operator") },
            text = { Text("Are you sure you want to delete operator '${it.name}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onDeleteOperator(it)
                        showDeleteOperatorConfirmDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteOperatorConfirmDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    TheBoysListDialog(
        showDialog = uiState.showTheBoysListDialog,
        theBoys = uiState.theBoysList,
        onDismiss = viewModel::onDismissTheBoysListDialog,
        onAddNewBoyClicked = viewModel::onAddNewTheBoyClicked,
        onEditBoyClicked = viewModel::onEditTheBoyClicked,
        onDeleteBoyClicked = { boy -> showDeleteTheBoyConfirmDialog = boy }
    )

    AddTheBoyDialog(
        showDialog = uiState.showAddTheBoyDialog,
        uiState = uiState,
        onInputChange = viewModel::onNewTheBoyInputChange,
        onNextStep = viewModel::onAddTheBoyNextStep,
        onPreviousStep = viewModel::onAddTheBoyPreviousStep,
        onSave = viewModel::onSaveNewTheBoy,
        onDismiss = viewModel::onDismissAddTheBoyDialog
    )

    EditTheBoyDialog(
        showDialog = uiState.showEditTheBoyDialog,
        editingTheBoy = uiState.editingTheBoy,
        uiState = uiState,
        onNameChange = viewModel::onEditBoyNameChange,
        onRoleChange = viewModel::onEditBoyRoleChange,
        onNotesChange = viewModel::onEditBoyNotesChange,
        onNotesForAiChange = viewModel::onEditBoyNotesForAiChange,
        onConfirm = viewModel::onSaveEditTheBoy,
        onDismiss = viewModel::onDismissEditTheBoyDialog
    )

    showDeleteTheBoyConfirmDialog?.let {
        AlertDialog(
            onDismissRequest = { showDeleteTheBoyConfirmDialog = null },
            title = { Text("Confirm Delete 'Boy'") },
            text = { Text("Are you sure you want to delete '${it.name}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onDeleteTheBoy(it)
                        showDeleteTheBoyConfirmDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteTheBoyConfirmDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    ManageCategoriesDialog(
        showDialog = uiState.showManageCategoriesDialog,
        categories = uiState.activityCategories,
        onDismiss = viewModel::onDismissManageCategoriesDialog,
        onAddNewCategoryClicked = viewModel::onAddNewCategoryClicked,
        onEditCategoryClicked = viewModel::onEditCategoryClicked,
        onDeleteCategoryClicked = viewModel::onDeleteCategory
    )

    AddCategoryDialog(
        showDialog = uiState.showAddCategoryDialog,
        categoryNameInput = uiState.newCategoryInput,
        categoryNameError = uiState.newCategoryError,
        onCategoryNameInputChange = viewModel::onNewCategoryInputChange,
        onConfirm = viewModel::onSaveNewCategory,
        onDismiss = viewModel::onDismissAddCategoryDialog
    )

    EditCategoryDialog(
        showDialog = uiState.showEditCategoryDialog,
        editingCategory = uiState.editingCategory,
        categoryNameInput = uiState.editCategoryInput,
        categoryNameError = uiState.editCategoryError,
        onCategoryNameInputChange = viewModel::onEditCategoryInputChange,
        onConfirm = viewModel::onSaveEditCategory,
        onDismiss = viewModel::onDismissEditCategoryDialog
    )
}
