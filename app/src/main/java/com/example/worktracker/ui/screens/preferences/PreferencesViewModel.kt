package com.example.worktracker.ui.screens.preferences

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worktracker.BuildConfig
import com.example.worktracker.data.database.entity.ActivityCategory
import com.example.worktracker.data.database.entity.OperatorInfo
import com.example.worktracker.data.database.entity.TheBoysInfo // New import
import com.example.worktracker.data.repository.ActivityCategoryRepository
import com.example.worktracker.data.repository.OperatorRepository
import com.example.worktracker.data.repository.TheBoysRepository // New import
import com.example.worktracker.data.repository.WorkActivityRepository
import com.example.worktracker.di.AppModule.KEY_GEMINI_API_KEY
import com.example.worktracker.di.AppModule.KEY_MASTER_PASSWORD
import com.example.worktracker.di.AppModule.KEY_SMS_CONTACT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

// Field constants for OperatorInfo
const val FIELD_OPERATOR_ID = "operatorId"
const val FIELD_OPERATOR_NAME = "operatorName"
const val FIELD_HOURLY_SALARY = "hourlySalary"
const val FIELD_OPERATOR_ROLE = "operatorRole"
const val FIELD_OPERATOR_PRIORITY = "operatorPriority"
const val FIELD_OPERATOR_NOTES = "operatorNotes"
const val FIELD_OPERATOR_NOTES_AI = "operatorNotesAi"

// Field constants for TheBoysInfo (New)
const val FIELD_BOY_ID = "boyId"
const val FIELD_BOY_NAME = "boyName"
const val FIELD_BOY_ROLE = "boyRole"
const val FIELD_BOY_NOTES = "boyNotes"
const val FIELD_BOY_NOTES_AI = "boyNotesAi"

data class PreferencesUiState(
    // Existing states for password, SMS, Gemini, Master Reset
    val showSetPasswordDialog: Boolean = false,
    val newPasswordInput: String = "",
    val confirmPasswordInput: String = "",
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val isPasswordSet: Boolean = false,
    val snackbarMessage: String? = null,
    val showMasterResetConfirmationDialog: Boolean = false,
    val masterResetPasswordAttempt: String = "",
    val masterResetPasswordError: String? = null,
    val showSmsContactDialog: Boolean = false,
    val smsContactInput: String = "",
    val smsContactError: String? = null,
    val preferredSmsContact: String? = null,
    val showSetGeminiApiKeyDialog: Boolean = false,
    val geminiApiKeyInput: String = "",
    val geminiApiKeyError: String? = null,
    val isGeminiApiKeySet: Boolean = false,

    // Operator section unlock states
    val isOperatorSectionUnlocked: Boolean = false, // This will be shared for TheBoys too
    val showOperatorPasswordDialog: Boolean = false, // Dialog for unlocking Operator section
    val showTheBoysPasswordDialog: Boolean = false, // Dialog for unlocking TheBoys section (can be same as operator)
    val operatorPasswordAttempt: String = "",
    val operatorPasswordError: String? = null,

    // Operator list and its dialog state
    val operators: List<OperatorInfo> = emptyList(),
    val showOperatorListDialog: Boolean = false,

    // States for Editing an existing Operator
    val showEditOperatorDialog: Boolean = false,
    val editingOperator: OperatorInfo? = null,
    val operatorIdInput: String = "",
    val operatorNameInput: String = "",
    val operatorHourlySalaryInput: String = "",
    val operatorRoleInput: String = "",
    val operatorPriorityInput: String = "",
    val operatorNotesInput: String = "",
    val operatorNotesForAiInput: String = "",
    val operatorIdError: String? = null,
    val operatorNameError: String? = null,
    val operatorHourlySalaryError: String? = null,
    val operatorRoleError: String? = null,
    val operatorPriorityError: String? = null,

    // States for Adding a new Operator
    val showAddOperatorDialog: Boolean = false,
    val addOperatorStep: Int = 0,
    val newOperatorInputs: Map<String, String> = emptyMap(),
    val newOperatorErrors: Map<String, String?> = emptyMap(),

    // Activity Category Management States
    val showManageCategoriesDialog: Boolean = false,
    val activityCategories: List<ActivityCategory> = emptyList(),
    val showAddCategoryDialog: Boolean = false,
    val newCategoryInput: String = "",
    val newCategoryError: String? = null,
    val showEditCategoryDialog: Boolean = false,
    val editingCategory: ActivityCategory? = null,
    val editCategoryInput: String = "",
    val editCategoryError: String? = null,

    // States for 'The Boys' Management (New)
    val theBoysList: List<TheBoysInfo> = emptyList(),
    val showTheBoysListDialog: Boolean = false,
    val showAddTheBoyDialog: Boolean = false,
    val addTheBoyStep: Int = 0,
    val newTheBoyInputs: Map<String, String> = emptyMap(),
    val newTheBoyErrors: Map<String, String?> = emptyMap(),
    val showEditTheBoyDialog: Boolean = false,
    val editingTheBoy: TheBoysInfo? = null,
    val editBoyIdInput: String = "", // Though likely display only in edit dialog
    val editBoyNameInput: String = "",
    val editBoyRoleInput: String = "",
    val editBoyNotesInput: String = "",
    val editBoyNotesForAiInput: String = "",
    val editBoyIdError: String? = null,
    val editBoyNameError: String? = null,
    val editBoyRoleError: String? = null
)

@HiltViewModel
class PreferencesViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val workActivityRepository: WorkActivityRepository,
    private val operatorRepository: OperatorRepository,
    private val activityCategoryRepository: ActivityCategoryRepository,
    private val theBoysRepository: TheBoysRepository // New repository injected
) : ViewModel() {

    private val _uiState = MutableStateFlow(PreferencesUiState())
    val uiState: StateFlow<PreferencesUiState> = _uiState.asStateFlow()

    private val addOperatorFieldOrder = listOf(
        FIELD_OPERATOR_ID,
        FIELD_OPERATOR_NAME,
        FIELD_HOURLY_SALARY,
        FIELD_OPERATOR_ROLE,
        FIELD_OPERATOR_PRIORITY,
        FIELD_OPERATOR_NOTES,
        FIELD_OPERATOR_NOTES_AI
    )

    // New field order for AddTheBoyDialog
    private val addTheBoyFieldOrder = listOf(
        FIELD_BOY_ID,
        FIELD_BOY_NAME,
        FIELD_BOY_ROLE,
        FIELD_BOY_NOTES,
        FIELD_BOY_NOTES_AI
    )

    init {
        checkIfPasswordIsSet()
        loadSmsContact()
        checkIfGeminiApiKeyIsSet()
        loadOperators()
        loadActivityCategories()
        loadTheBoys() // New: Load 'The Boys' on init
    }

    private fun loadOperators() {
        operatorRepository.getAllOperators()
            .onEach { operatorList ->
                _uiState.value = _uiState.value.copy(operators = operatorList)
            }
            .launchIn(viewModelScope)
    }

    private fun loadActivityCategories() {
        activityCategoryRepository.getAllCategories()
            .onEach { categories ->
                _uiState.value = _uiState.value.copy(activityCategories = categories)
            }
            .launchIn(viewModelScope)
    }

    // New function to load 'The Boys'
    private fun loadTheBoys() {
        theBoysRepository.getAllTheBoys()
            .onEach { boysList ->
                _uiState.value = _uiState.value.copy(theBoysList = boysList)
            }
            .launchIn(viewModelScope)
    }

    private fun checkIfPasswordIsSet() {
        val isSet = sharedPreferences.contains(KEY_MASTER_PASSWORD)
        _uiState.value = _uiState.value.copy(isPasswordSet = isSet)
    }

    private fun loadSmsContact() {
        val contact = sharedPreferences.getString(KEY_SMS_CONTACT, null)
        _uiState.value = _uiState.value.copy(preferredSmsContact = contact)
    }

    private fun checkIfGeminiApiKeyIsSet() {
        val isSet = sharedPreferences.contains(KEY_GEMINI_API_KEY)
        _uiState.value = _uiState.value.copy(isGeminiApiKeySet = isSet)
    }

    fun clearSnackbarMessage() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }

    // --- Master Password & Reset (Existing - No changes needed here) ---
    fun onShowSetPasswordDialog() {
        _uiState.value = _uiState.value.copy(
            showSetPasswordDialog = true,
            newPasswordInput = "",
            confirmPasswordInput = "",
            newPasswordError = null,
            confirmPasswordError = null
        )
    }

    fun onDismissSetPasswordDialog() {
        _uiState.value = _uiState.value.copy(
            showSetPasswordDialog = false,
            newPasswordInput = "",
            confirmPasswordInput = "",
            newPasswordError = null,
            confirmPasswordError = null
        )
    }

    fun onNewPasswordInputChange(password: String) {
        _uiState.value = _uiState.value.copy(newPasswordInput = password)
        validateSetPasswords()
    }

    fun onConfirmPasswordInputChange(password: String) {
        _uiState.value = _uiState.value.copy(confirmPasswordInput = password)
        validateSetPasswords()
    }

    private fun validateSetPasswords() {
        val newPassword = _uiState.value.newPasswordInput
        val confirmPassword = _uiState.value.confirmPasswordInput
        var newPasswordError: String? = null
        var confirmPasswordError: String? = null

        if (newPassword.isNotBlank() && newPassword.length < 6) {
            newPasswordError = "Password must be at least 6 characters long."
        }
        if (confirmPassword.isNotBlank() && newPassword != confirmPassword) {
            confirmPasswordError = "Passwords do not match."
        }

        _uiState.value = _uiState.value.copy(
            newPasswordError = newPasswordError,
            confirmPasswordError = confirmPasswordError
        )
    }

    fun onSavePasswordAttempt() {
        validateSetPasswords()
        val currentState = _uiState.value
        if (currentState.newPasswordInput.isNotBlank() && currentState.newPasswordError == null && currentState.confirmPasswordError == null) {
            sharedPreferences.edit().putString(KEY_MASTER_PASSWORD, currentState.newPasswordInput).apply()
            _uiState.value = currentState.copy(
                showSetPasswordDialog = false,
                newPasswordInput = "",
                confirmPasswordInput = "",
                newPasswordError = null,
                confirmPasswordError = null,
                isPasswordSet = true,
                snackbarMessage = "Master reset password set successfully."
            )
        } else if (currentState.newPasswordInput.isBlank()) {
            _uiState.value = _uiState.value.copy(
                newPasswordError = "Password cannot be empty.",
                confirmPasswordError = null
            )
        }
    }

    fun onMasterResetClicked() {
        if (_uiState.value.isPasswordSet) {
            _uiState.value = _uiState.value.copy(
                showMasterResetConfirmationDialog = true,
                masterResetPasswordAttempt = "",
                masterResetPasswordError = null
            )
        } else {
            _uiState.value = _uiState.value.copy(snackbarMessage = "Please set a master reset password first.")
        }
    }

    fun onDismissMasterResetConfirmationDialog() {
        _uiState.value = _uiState.value.copy(
            showMasterResetConfirmationDialog = false,
            masterResetPasswordAttempt = "",
            masterResetPasswordError = null
        )
    }

    fun onMasterResetPasswordAttemptChange(password: String) {
        _uiState.value = _uiState.value.copy(masterResetPasswordAttempt = password, masterResetPasswordError = null)
    }

    fun onConfirmMasterReset() {
        val attempt = _uiState.value.masterResetPasswordAttempt
        val storedPassword = sharedPreferences.getString(KEY_MASTER_PASSWORD, null)

        if (storedPassword != null && attempt == storedPassword) {
            viewModelScope.launch {
                workActivityRepository.clearAllLogs() // Consider if other data needs wiping
                operatorRepository.getAllOperators().collect { list -> list.forEach { operatorRepository.deleteOperator(it) } } // Example for operators
                theBoysRepository.getAllTheBoys().collect { list -> list.forEach { theBoysRepository.deleteTheBoy(it) } } // Example for TheBoys
                activityCategoryRepository.getAllCategories().collect { list -> list.forEach { activityCategoryRepository.deleteCategory(it) } } // Example for categories

                _uiState.value = _uiState.value.copy(
                    showMasterResetConfirmationDialog = false,
                    masterResetPasswordAttempt = "",
                    masterResetPasswordError = null,
                    snackbarMessage = "All application data has been wiped.",
                    isOperatorSectionUnlocked = false, // Relock sections
                )
            }
        } else {
            _uiState.value = _uiState.value.copy(masterResetPasswordError = "Incorrect password.")
        }
    }

    // --- SMS Contact (Existing - No changes needed here) ---
    fun onShowSmsContactDialog() {
        val currentContact = _uiState.value.preferredSmsContact ?: ""
        _uiState.value = _uiState.value.copy(
            showSmsContactDialog = true,
            smsContactInput = currentContact,
            smsContactError = null
        )
    }

    fun onDismissSmsContactDialog() {
        _uiState.value = _uiState.value.copy(
            showSmsContactDialog = false,
            smsContactInput = "",
            smsContactError = null
        )
    }

    fun onSmsContactInputChange(contact: String) {
        _uiState.value = _uiState.value.copy(smsContactInput = contact, smsContactError = null)
    }

    fun onSaveSmsContact() {
        val contact = _uiState.value.smsContactInput
        if (contact.isNotBlank()) {
            sharedPreferences.edit().putString(KEY_SMS_CONTACT, contact).apply()
            _uiState.value = _uiState.value.copy(
                preferredSmsContact = contact,
                showSmsContactDialog = false,
                smsContactInput = "",
                smsContactError = null,
                snackbarMessage = "Preferred SMS contact saved."
            )
        } else {
            _uiState.value = _uiState.value.copy(smsContactError = "Contact cannot be empty.")
        }
    }

    // --- Gemini API Key (Existing - No changes needed here) ---
    fun onShowSetGeminiApiKeyDialog() {
        val currentApiKey = sharedPreferences.getString(KEY_GEMINI_API_KEY, "") ?: ""
        _uiState.value = _uiState.value.copy(
            showSetGeminiApiKeyDialog = true,
            geminiApiKeyInput = currentApiKey,
            geminiApiKeyError = null
        )
    }

    fun onDismissSetGeminiApiKeyDialog() {
        _uiState.value = _uiState.value.copy(
            showSetGeminiApiKeyDialog = false,
            geminiApiKeyInput = "",
            geminiApiKeyError = null
        )
    }

    fun onGeminiApiKeyInputChange(apiKey: String) {
        _uiState.value = _uiState.value.copy(geminiApiKeyInput = apiKey, geminiApiKeyError = null)
    }

    fun onSaveGeminiApiKey() {
        val apiKey = _uiState.value.geminiApiKeyInput
        if (apiKey.isNotBlank()) {
            sharedPreferences.edit().putString(KEY_GEMINI_API_KEY, apiKey).apply()
            _uiState.value = _uiState.value.copy(
                isGeminiApiKeySet = true,
                showSetGeminiApiKeyDialog = false,
                geminiApiKeyInput = "",
                geminiApiKeyError = null,
                snackbarMessage = "Gemini API Key saved successfully."
            )
        } else {
            _uiState.value = _uiState.value.copy(geminiApiKeyError = "API Key cannot be empty.")
        }
    }

    // --- Operator Info Password Protection & List Dialog (Existing) ---
    fun onOperatorSectionClicked() {
        if (!_uiState.value.isOperatorSectionUnlocked) {
            _uiState.value = _uiState.value.copy(
                showOperatorPasswordDialog = true, // Show operator-specific password dialog
                showTheBoysPasswordDialog = false, // Ensure TheBoys dialog is hidden
                operatorPasswordAttempt = "",
                operatorPasswordError = null
            )
        } else {
            _uiState.value = _uiState.value.copy(showOperatorListDialog = true)
        }
    }

    fun onDismissOperatorPasswordDialog() {
        _uiState.value = _uiState.value.copy(
            showOperatorPasswordDialog = false,
            operatorPasswordAttempt = "",
            operatorPasswordError = null
        )
    }

    fun onDismissOperatorListDialog() {
        _uiState.value = _uiState.value.copy(showOperatorListDialog = false)
    }

    fun onOperatorPasswordAttemptChange(password: String) {
        _uiState.value = _uiState.value.copy(operatorPasswordAttempt = password, operatorPasswordError = null)
    }
    
    // Generic unlock attempt, determines which list to show based on which password dialog was active
    fun onUnlockSectionAttempt() {
        val attempt = _uiState.value.operatorPasswordAttempt
        if (attempt == BuildConfig.OPERATOR_INFO_PASSWORD) { // Using same password for now
            val wasOperatorDialog = _uiState.value.showOperatorPasswordDialog
            _uiState.value = _uiState.value.copy(
                isOperatorSectionUnlocked = true,
                showOperatorPasswordDialog = false,
                showTheBoysPasswordDialog = false,
                showOperatorListDialog = wasOperatorDialog, // Show if operator dialog was the trigger
                showTheBoysListDialog = !wasOperatorDialog, // Show if TheBoys dialog was the trigger (or if neither specifically, implies TheBoys if operator not shown)
                operatorPasswordAttempt = "",
                operatorPasswordError = null,
                snackbarMessage = "Section unlocked."
            )
        } else {
            _uiState.value = _uiState.value.copy(operatorPasswordError = "Incorrect password.")
        }
    }

    // --- Edit Operator Dialog Functions (Existing - No changes needed here) ---
    fun onEditOperatorClicked(operatorInfo: OperatorInfo) {
        _uiState.value = _uiState.value.copy(
            showEditOperatorDialog = true,
            showAddOperatorDialog = false,
            editingOperator = operatorInfo,
            operatorIdInput = operatorInfo.operatorId.toString(),
            operatorNameInput = operatorInfo.name,
            operatorHourlySalaryInput = operatorInfo.hourlySalary.toString(),
            operatorRoleInput = operatorInfo.role,
            operatorPriorityInput = operatorInfo.priority.toString(),
            operatorNotesInput = operatorInfo.notes ?: "",
            operatorNotesForAiInput = operatorInfo.notesForAi ?: "",
            operatorIdError = null,
            operatorNameError = null,
            operatorHourlySalaryError = null,
            operatorRoleError = null,
            operatorPriorityError = null
        )
    }

    fun onDismissEditOperatorDialog() {
        _uiState.value = _uiState.value.copy(
            showEditOperatorDialog = false,
            editingOperator = null,
            operatorIdInput = "", operatorNameInput = "", operatorHourlySalaryInput = "",
            operatorRoleInput = "", operatorPriorityInput = "", operatorNotesInput = "",
            operatorNotesForAiInput = "", operatorIdError = null, operatorNameError = null,
            operatorHourlySalaryError = null, operatorRoleError = null, operatorPriorityError = null
        )
    }

    fun onOperatorNameChange(name: String) { _uiState.value = _uiState.value.copy(operatorNameInput = name, operatorNameError = null) }
    fun onOperatorHourlySalaryChange(salary: String) { _uiState.value = _uiState.value.copy(operatorHourlySalaryInput = salary, operatorHourlySalaryError = null) }
    fun onOperatorRoleChange(role: String) { _uiState.value = _uiState.value.copy(operatorRoleInput = role, operatorRoleError = null) }
    fun onOperatorPriorityChange(priority: String) { _uiState.value = _uiState.value.copy(operatorPriorityInput = priority, operatorPriorityError = null) }
    fun onOperatorNotesChange(notes: String) { _uiState.value = _uiState.value.copy(operatorNotesInput = notes) }
    fun onOperatorNotesForAiChange(notes: String) { _uiState.value = _uiState.value.copy(operatorNotesForAiInput = notes) }

    fun onSaveEditOperator() {
        val state = _uiState.value
        val editingOp = state.editingOperator ?: return

        val name = state.operatorNameInput
        val salaryStr = state.operatorHourlySalaryInput
        val role = state.operatorRoleInput
        val priorityStr = state.operatorPriorityInput

        var nameError: String? = null
        var salaryError: String? = null
        var roleError: String? = null
        var priorityError: String? = null

        val salary = salaryStr.toDoubleOrNull()
        val priority = priorityStr.toIntOrNull()

        if (name.isBlank()) nameError = "Name cannot be empty."
        if (salary == null) salaryError = "Salary must be a valid number."
        if (role.isBlank()) roleError = "Role cannot be empty."
        if (priority == null || priority !in 1..5) priorityError = "Priority must be a number between 1-5."

        if (nameError != null || salaryError != null || roleError != null || priorityError != null) {
            _uiState.value = state.copy(
                operatorNameError = nameError,
                operatorHourlySalaryError = salaryError,
                operatorRoleError = roleError,
                operatorPriorityError = priorityError
            )
            return
        }

        val operatorToSave = editingOp.copy(
            name = name,
            hourlySalary = salary!!,
            role = role,
            priority = priority!!,
            notes = state.operatorNotesInput.takeIf { it.isNotBlank() },
            notesForAi = state.operatorNotesForAiInput.takeIf { it.isNotBlank() }
        )

        viewModelScope.launch {
            operatorRepository.updateOperator(operatorToSave)
            _uiState.value = _uiState.value.copy(
                showEditOperatorDialog = false,
                snackbarMessage = "Operator updated successfully.",
                editingOperator = null
            )
        }
    }

    // --- Add New Operator (Multi-Step Flow) Functions (Existing - No changes needed here) ---
    fun onAddNewOperatorClicked() {
        _uiState.value = _uiState.value.copy(
            showAddOperatorDialog = true,
            showEditOperatorDialog = false,
            addOperatorStep = 0,
            newOperatorInputs = emptyMap(),
            newOperatorErrors = emptyMap()
        )
    }

    fun onDismissAddOperatorDialog() {
        _uiState.value = _uiState.value.copy(
            showAddOperatorDialog = false,
            addOperatorStep = 0,
            newOperatorInputs = emptyMap(),
            newOperatorErrors = emptyMap()
        )
    }

    fun onNewOperatorInputChange(fieldName: String, value: String) {
        val currentInputs = _uiState.value.newOperatorInputs.toMutableMap()
        currentInputs[fieldName] = value
        val currentErrors = _uiState.value.newOperatorErrors.toMutableMap()
        currentErrors[fieldName] = null
        _uiState.value = _uiState.value.copy(newOperatorInputs = currentInputs, newOperatorErrors = currentErrors)
    }

    private fun validateCurrentAddOperatorStep(): Boolean {
        val state = _uiState.value
        val currentStep = state.addOperatorStep
        if (currentStep >= addOperatorFieldOrder.size) return true

        val fieldName = addOperatorFieldOrder[currentStep]
        val value = state.newOperatorInputs[fieldName] ?: ""
        var error: String? = null

        when (fieldName) {
            FIELD_OPERATOR_ID -> {
                val id = value.toIntOrNull()
                if (id == null) error = "ID must be a number."
                else if (state.operators.any { it.operatorId == id }) error = "This ID is already in use."
            }
            FIELD_OPERATOR_NAME -> if (value.isBlank()) error = "Name cannot be empty."
            FIELD_HOURLY_SALARY -> if (value.toDoubleOrNull() == null) error = "Salary must be a valid number."
            FIELD_OPERATOR_ROLE -> if (value.isBlank()) error = "Role cannot be empty."
            FIELD_OPERATOR_PRIORITY -> {
                val priorityNum = value.toIntOrNull()
                if (priorityNum == null || priorityNum !in 1..5) error = "Priority must be a number between 1-5."
            }
        }

        if (error != null) {
            val currentErrors = state.newOperatorErrors.toMutableMap()
            currentErrors[fieldName] = error
            _uiState.value = state.copy(newOperatorErrors = currentErrors)
            return false
        }
        return true
    }

    fun onAddOperatorNextStep() {
        if (validateCurrentAddOperatorStep()) {
            val currentStep = _uiState.value.addOperatorStep
            if (currentStep < addOperatorFieldOrder.size - 1) {
                _uiState.value = _uiState.value.copy(addOperatorStep = currentStep + 1)
            } else {
                onSaveNewOperator()
            }
        }
    }

    fun onAddOperatorPreviousStep() {
        val currentStep = _uiState.value.addOperatorStep
        if (currentStep > 0) {
            _uiState.value = _uiState.value.copy(addOperatorStep = currentStep - 1)
        }
    }

    fun onSaveNewOperator() {
        var allFieldsValid = true
        val tempErrors = mutableMapOf<String, String?>()
        val inputs = _uiState.value.newOperatorInputs

        val idStr = inputs[FIELD_OPERATOR_ID] ?: ""
        val id = idStr.toIntOrNull()
        if (id == null) { tempErrors[FIELD_OPERATOR_ID] = "ID must be a number."; allFieldsValid = false }
        else if (_uiState.value.operators.any { it.operatorId == id }) { tempErrors[FIELD_OPERATOR_ID] = "This ID is already in use."; allFieldsValid = false }

        val name = inputs[FIELD_OPERATOR_NAME] ?: ""
        if (name.isBlank()) { tempErrors[FIELD_OPERATOR_NAME] = "Name cannot be empty."; allFieldsValid = false }

        val salaryStr = inputs[FIELD_HOURLY_SALARY] ?: ""
        val salary = salaryStr.toDoubleOrNull()
        if (salary == null) { tempErrors[FIELD_HOURLY_SALARY] = "Salary must be a valid number."; allFieldsValid = false }

        val role = inputs[FIELD_OPERATOR_ROLE] ?: ""
        if (role.isBlank()) { tempErrors[FIELD_OPERATOR_ROLE] = "Role cannot be empty."; allFieldsValid = false }

        val priorityStr = inputs[FIELD_OPERATOR_PRIORITY] ?: ""
        val priority = priorityStr.toIntOrNull()
        if (priority == null || priority !in 1..5) { tempErrors[FIELD_OPERATOR_PRIORITY] = "Priority must be a number between 1-5."; allFieldsValid = false }

        if (!allFieldsValid) {
            val currentErrors = _uiState.value.newOperatorErrors.toMutableMap()
            tempErrors.forEach { (key, value) -> if (value != null) currentErrors[key] = value }
            _uiState.value = _uiState.value.copy(newOperatorErrors = currentErrors)
            return
        }

        val operatorToSave = OperatorInfo(
            operatorId = id!!,
            name = name,
            hourlySalary = salary!!,
            role = role,
            priority = priority!!,
            notes = inputs[FIELD_OPERATOR_NOTES]?.takeIf { it.isNotBlank() },
            notesForAi = inputs[FIELD_OPERATOR_NOTES_AI]?.takeIf { it.isNotBlank() }
        )

        viewModelScope.launch {
            operatorRepository.insertOperator(operatorToSave)
            _uiState.value = _uiState.value.copy(
                showAddOperatorDialog = false,
                snackbarMessage = "Operator added successfully.",
                addOperatorStep = 0,
                newOperatorInputs = emptyMap(),
                newOperatorErrors = emptyMap()
            )
        }
    }

    // --- Delete Operator (Existing - No changes needed here) ---
    fun onDeleteOperator(operatorInfo: OperatorInfo) {
        viewModelScope.launch {
            operatorRepository.deleteOperator(operatorInfo)
            _uiState.value = _uiState.value.copy(snackbarMessage = "Operator deleted.")
        }
    }

    // --- 'The Boys' Management Functions (New) ---
    fun onTheBoysSectionClicked() {
        if (!_uiState.value.isOperatorSectionUnlocked) { // Using same unlock flag for simplicity
            _uiState.value = _uiState.value.copy(
                showTheBoysPasswordDialog = true, // Show TheBoys-specific password dialog (or reuse operator dialog)
                showOperatorPasswordDialog = false, // Ensure Operator dialog is hidden
                operatorPasswordAttempt = "", // Reset attempt field
                operatorPasswordError = null
            )
        } else {
            _uiState.value = _uiState.value.copy(showTheBoysListDialog = true)
        }
    }

    // Assuming the same password dialog (`OperatorPasswordDialog`) is used.
    // If a different one, then onDismissTheBoysPasswordDialog would be needed.
    // For now, `onUnlockSectionAttempt` will handle unlocking and showing the correct list.

    fun onDismissTheBoysListDialog() {
        _uiState.value = _uiState.value.copy(showTheBoysListDialog = false)
    }

    fun onAddNewTheBoyClicked() {
        _uiState.value = _uiState.value.copy(
            showAddTheBoyDialog = true,
            showEditTheBoyDialog = false,
            addTheBoyStep = 0,
            newTheBoyInputs = emptyMap(),
            newTheBoyErrors = emptyMap()
        )
    }

    fun onDismissAddTheBoyDialog() {
        _uiState.value = _uiState.value.copy(
            showAddTheBoyDialog = false,
            addTheBoyStep = 0,
            newTheBoyInputs = emptyMap(),
            newTheBoyErrors = emptyMap()
        )
    }

    fun onNewTheBoyInputChange(fieldName: String, value: String) {
        val currentInputs = _uiState.value.newTheBoyInputs.toMutableMap()
        currentInputs[fieldName] = value
        val currentErrors = _uiState.value.newTheBoyErrors.toMutableMap()
        currentErrors[fieldName] = null
        _uiState.value = _uiState.value.copy(newTheBoyInputs = currentInputs, newTheBoyErrors = currentErrors)
    }

    private fun validateCurrentAddTheBoyStep(): Boolean {
        val state = _uiState.value
        val currentStep = state.addTheBoyStep
        if (currentStep >= addTheBoyFieldOrder.size) return true

        val fieldName = addTheBoyFieldOrder[currentStep]
        val value = state.newTheBoyInputs[fieldName] ?: ""
        var error: String? = null

        when (fieldName) {
            FIELD_BOY_ID -> {
                val id = value.toIntOrNull()
                if (id == null) error = "ID must be a number."
                else if (state.theBoysList.any { it.boyId == id }) error = "This ID is already in use."
            }
            FIELD_BOY_NAME -> if (value.isBlank()) error = "Name cannot be empty."
            FIELD_BOY_ROLE -> if (value.isBlank()) error = "Role cannot be empty."
        }

        if (error != null) {
            val currentErrors = state.newTheBoyErrors.toMutableMap()
            currentErrors[fieldName] = error
            _uiState.value = state.copy(newTheBoyErrors = currentErrors)
            return false
        }
        return true
    }

    fun onAddTheBoyNextStep() {
        if (validateCurrentAddTheBoyStep()) {
            val currentStep = _uiState.value.addTheBoyStep
            if (currentStep < addTheBoyFieldOrder.size - 1) {
                _uiState.value = _uiState.value.copy(addTheBoyStep = currentStep + 1)
            }
        } // Save is handled by the Save button in the dialog directly
    }

    fun onAddTheBoyPreviousStep() {
        val currentStep = _uiState.value.addTheBoyStep
        if (currentStep > 0) {
            _uiState.value = _uiState.value.copy(addTheBoyStep = currentStep - 1)
        }
    }

    fun onSaveNewTheBoy() {
        // Final validation for all fields before saving
        var allFieldsValid = true
        val tempErrors = mutableMapOf<String, String?>()
        val inputs = _uiState.value.newTheBoyInputs

        val idStr = inputs[FIELD_BOY_ID] ?: ""
        val id = idStr.toIntOrNull()
        if (id == null) { tempErrors[FIELD_BOY_ID] = "ID must be a number."; allFieldsValid = false }
        else if (_uiState.value.theBoysList.any { it.boyId == id }) { tempErrors[FIELD_BOY_ID] = "This ID is already in use."; allFieldsValid = false }

        val name = inputs[FIELD_BOY_NAME] ?: ""
        if (name.isBlank()) { tempErrors[FIELD_BOY_NAME] = "Name cannot be empty."; allFieldsValid = false }

        val role = inputs[FIELD_BOY_ROLE] ?: ""
        if (role.isBlank()) { tempErrors[FIELD_BOY_ROLE] = "Role cannot be empty."; allFieldsValid = false }

        if (!allFieldsValid) {
            val currentErrors = _uiState.value.newTheBoyErrors.toMutableMap()
            tempErrors.forEach { (key, value) -> if (value != null) currentErrors[key] = value }
            _uiState.value = _uiState.value.copy(newTheBoyErrors = currentErrors)
            return
        }
        
        val boyToSave = TheBoysInfo(
            boyId = id!!,
            name = name,
            role = role,
            notes = inputs[FIELD_BOY_NOTES]?.takeIf { it.isNotBlank() },
            notesForAi = inputs[FIELD_BOY_NOTES_AI]?.takeIf { it.isNotBlank() }
        )

        viewModelScope.launch {
            theBoysRepository.insertTheBoy(boyToSave)
            _uiState.value = _uiState.value.copy(
                showAddTheBoyDialog = false,
                snackbarMessage = "'Boy' added successfully.",
                addTheBoyStep = 0,
                newTheBoyInputs = emptyMap(),
                newTheBoyErrors = emptyMap()
            )
        }
    }

    fun onEditTheBoyClicked(theBoy: TheBoysInfo) {
        _uiState.value = _uiState.value.copy(
            showEditTheBoyDialog = true,
            editingTheBoy = theBoy,
            editBoyIdInput = theBoy.boyId.toString(),
            editBoyNameInput = theBoy.name,
            editBoyRoleInput = theBoy.role,
            editBoyNotesInput = theBoy.notes ?: "",
            editBoyNotesForAiInput = theBoy.notesForAi ?: "",
            editBoyIdError = null, // ID is not editable
            editBoyNameError = null,
            editBoyRoleError = null
        )
    }

    fun onDismissEditTheBoyDialog() {
        _uiState.value = _uiState.value.copy(
            showEditTheBoyDialog = false,
            editingTheBoy = null,
            editBoyIdInput = "",
            editBoyNameInput = "",
            editBoyRoleInput = "",
            editBoyNotesInput = "",
            editBoyNotesForAiInput = "",
            editBoyIdError = null,
            editBoyNameError = null,
            editBoyRoleError = null
        )
    }

    // Input change handlers for EditTheBoyDialog
    fun onEditBoyNameChange(name: String) { _uiState.value = _uiState.value.copy(editBoyNameInput = name, editBoyNameError = null) }
    fun onEditBoyRoleChange(role: String) { _uiState.value = _uiState.value.copy(editBoyRoleInput = role, editBoyRoleError = null) }
    fun onEditBoyNotesChange(notes: String) { _uiState.value = _uiState.value.copy(editBoyNotesInput = notes) }
    fun onEditBoyNotesForAiChange(notes: String) { _uiState.value = _uiState.value.copy(editBoyNotesForAiInput = notes) }

    fun onSaveEditTheBoy() {
        val state = _uiState.value
        val editingBoy = state.editingTheBoy ?: return

        val name = state.editBoyNameInput
        val role = state.editBoyRoleInput
        var nameError: String? = null
        var roleError: String? = null

        if (name.isBlank()) nameError = "Name cannot be empty."
        if (role.isBlank()) roleError = "Role cannot be empty."

        if (nameError != null || roleError != null) {
            _uiState.value = state.copy(
                editBoyNameError = nameError,
                editBoyRoleError = roleError
            )
            return
        }

        val boyToSave = editingBoy.copy(
            name = name,
            role = role,
            notes = state.editBoyNotesInput.takeIf { it.isNotBlank() },
            notesForAi = state.editBoyNotesForAiInput.takeIf { it.isNotBlank() }
        )

        viewModelScope.launch {
            theBoysRepository.updateTheBoy(boyToSave)
            _uiState.value = _uiState.value.copy(
                showEditTheBoyDialog = false,
                snackbarMessage = "'Boy' details updated successfully.",
                editingTheBoy = null
            )
        }
    }

    fun onDeleteTheBoy(theBoy: TheBoysInfo) {
        viewModelScope.launch {
            theBoysRepository.deleteTheBoy(theBoy)
            // Consider if cascading deletes or updates are needed for ProductionActivity logs
            _uiState.value = _uiState.value.copy(snackbarMessage = "'Boy' deleted.")
        }
    }

    // --- Activity Category Management Functions (Existing - No changes needed here) ---
    fun onManageCategoriesClicked() {
        _uiState.value = _uiState.value.copy(showManageCategoriesDialog = true)
    }

    fun onDismissManageCategoriesDialog() {
        _uiState.value = _uiState.value.copy(showManageCategoriesDialog = false)
    }

    fun onAddNewCategoryClicked() {
        _uiState.value = _uiState.value.copy(
            showAddCategoryDialog = true,
            newCategoryInput = "",
            newCategoryError = null
        )
    }

    fun onDismissAddCategoryDialog() {
        _uiState.value = _uiState.value.copy(
            showAddCategoryDialog = false,
            newCategoryInput = "",
            newCategoryError = null
        )
    }

    fun onNewCategoryInputChange(name: String) {
        _uiState.value = _uiState.value.copy(newCategoryInput = name, newCategoryError = null)
    }

    fun onSaveNewCategory() {
        val categoryName = _uiState.value.newCategoryInput
        if (categoryName.isBlank()) {
            _uiState.value = _uiState.value.copy(newCategoryError = "Category name cannot be empty.")
            return
        }
        if (_uiState.value.activityCategories.any { it.name.equals(categoryName, ignoreCase = true) }) {
            _uiState.value = _uiState.value.copy(newCategoryError = "Category with this name already exists.")
            return
        }

        viewModelScope.launch {
            activityCategoryRepository.insertCategory(ActivityCategory(name = categoryName))
            _uiState.value = _uiState.value.copy(
                showAddCategoryDialog = false,
                newCategoryInput = "",
                newCategoryError = null,
                snackbarMessage = "Category '$categoryName' added successfully."
            )
        }
    }

    fun onEditCategoryClicked(category: ActivityCategory) {
        _uiState.value = _uiState.value.copy(
            showEditCategoryDialog = true,
            editingCategory = category,
            editCategoryInput = category.name,
            editCategoryError = null
        )
    }

    fun onDismissEditCategoryDialog() {
        _uiState.value = _uiState.value.copy(
            showEditCategoryDialog = false,
            editingCategory = null,
            editCategoryInput = "",
            editCategoryError = null
        )
    }

    fun onEditCategoryInputChange(name: String) {
        _uiState.value = _uiState.value.copy(editCategoryInput = name, editCategoryError = null)
    }

    fun onSaveEditCategory() {
        val currentCategory = _uiState.value.editingCategory ?: return
        val newName = _uiState.value.editCategoryInput

        if (newName.isBlank()) {
            _uiState.value = _uiState.value.copy(editCategoryError = "Category name cannot be empty.")
            return
        }
        if (newName.equals(currentCategory.name, ignoreCase = true)) {
            // No change, just dismiss
            onDismissEditCategoryDialog()
            return
        }
        if (_uiState.value.activityCategories.any { it.name.equals(newName, ignoreCase = true) && it.id != currentCategory.id }) {
            _uiState.value = _uiState.value.copy(editCategoryError = "Category with this name already exists.")
            return
        }

        viewModelScope.launch {
            activityCategoryRepository.updateCategory(currentCategory.copy(name = newName))
            _uiState.value = _uiState.value.copy(
                showEditCategoryDialog = false,
                editingCategory = null,
                editCategoryInput = "",
                editCategoryError = null,
                snackbarMessage = "Category '${currentCategory.name}' updated to '$newName' successfully."
            )
        }
    }

    fun onDeleteCategory(category: ActivityCategory) {
        viewModelScope.launch {
            workActivityRepository.updateCategoryNameForExistingLogs(oldCategoryName = category.name, newCategoryName = "(Deleted Category)")
            activityCategoryRepository.deleteCategory(category)
            _uiState.value = _uiState.value.copy(snackbarMessage = "Category '${category.name}' deleted. Associated logs updated.")
        }
    }
}
