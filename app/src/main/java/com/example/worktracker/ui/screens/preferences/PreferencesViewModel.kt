package com.example.worktracker.ui.screens.preferences

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.worktracker.BuildConfig
import com.example.worktracker.data.database.AppDatabase
import com.example.worktracker.data.database.entity.ActivityCategory
import com.example.worktracker.data.database.entity.OperatorInfo
import com.example.worktracker.data.database.entity.TheBoysInfo
import com.example.worktracker.data.repository.ActivityCategoryRepository
import com.example.worktracker.data.repository.OperatorRepository
import com.example.worktracker.data.repository.TheBoysRepository
import com.example.worktracker.data.sync.IFirestoreSyncManager
import com.example.worktracker.di.AppModule.KEY_GEMINI_API_KEY
import com.example.worktracker.di.AppModule.KEY_MASTER_PASSWORD
import com.example.worktracker.di.AppModule.KEY_SMS_CONTACT
import com.example.worktracker.ui.signin.GoogleAuthUiClient
import com.example.worktracker.ui.signin.UserData
import com.example.worktracker.workers.UploadAllDataWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "PreferencesViewModel"
private const val UPLOAD_ALL_DATA_WORK_NAME = "uploadAllLocalDataToFirebase"

// Field constants for OperatorInfo
const val FIELD_OPERATOR_NAME = "operatorName"
const val FIELD_HOURLY_SALARY = "hourlySalary"
const val FIELD_OPERATOR_ROLE = "operatorRole"
const val FIELD_OPERATOR_PRIORITY = "operatorPriority"
const val FIELD_OPERATOR_NOTES = "operatorNotes"
const val FIELD_OPERATOR_NOTES_AI = "operatorNotesAi"

// Field constants for TheBoysInfo
const val FIELD_BOY_NAME = "boyName"
const val FIELD_BOY_ROLE = "boyRole"
const val FIELD_BOY_NOTES = "boyNotes"
const val FIELD_BOY_NOTES_AI = "boyNotesAi"

data class PreferencesUiState(
    val showSetPasswordDialog: Boolean = false,
    val newPasswordInput: String = "",
    val confirmPasswordInput: String = "",
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val isPasswordSet: Boolean = false,
    var snackbarMessage: String? = null,
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
    val isOperatorSectionUnlocked: Boolean = false,
    val showOperatorPasswordDialog: Boolean = false,
    val showTheBoysPasswordDialog: Boolean = false,
    val operatorPasswordAttempt: String = "",
    val operatorPasswordError: String? = null,
    val operators: List<OperatorInfo> = emptyList(),
    val showOperatorListDialog: Boolean = false,
    val showEditOperatorDialog: Boolean = false,
    val editingOperator: OperatorInfo? = null,
    val operatorNameInput: String = "",
    val operatorHourlySalaryInput: String = "",
    val operatorRoleInput: String = "",
    val operatorPriorityInput: String = "",
    val operatorNotesInput: String = "",
    val operatorNotesForAiInput: String = "",
    val operatorNameError: String? = null,
    val operatorHourlySalaryError: String? = null,
    val operatorRoleError: String? = null,
    val operatorPriorityError: String? = null,
    val showAddOperatorDialog: Boolean = false,
    val addOperatorStep: Int = 0,
    val newOperatorInputs: Map<String, String> = emptyMap(),
    val newOperatorErrors: Map<String, String?> = emptyMap(),
    val activityCategories: List<ActivityCategory> = emptyList(),
    val showManageCategoriesDialog: Boolean = false,
    val showAddCategoryDialog: Boolean = false,
    val newCategoryInput: String = "",
    val newCategoryError: String? = null,
    val showEditCategoryDialog: Boolean = false,
    val editingCategory: ActivityCategory? = null,
    val editCategoryInput: String = "",
    val editCategoryError: String? = null,
    val theBoysList: List<TheBoysInfo> = emptyList(),
    val showTheBoysListDialog: Boolean = false,
    val showAddTheBoyDialog: Boolean = false,
    val addTheBoyStep: Int = 0,
    val newTheBoyInputs: Map<String, String> = emptyMap(),
    val newTheBoyErrors: Map<String, String?> = emptyMap(),
    val showEditTheBoyDialog: Boolean = false,
    val editingTheBoy: TheBoysInfo? = null,
    val editBoyNameInput: String = "",
    val editBoyRoleInput: String = "",
    val editBoyNotesInput: String = "",
    val editBoyNotesForAiInput: String = "",
    val editBoyNameError: String? = null,
    val editBoyRoleError: String? = null,
    val currentUser: UserData? = null,
    val isAccountActionInProgress: Boolean = false,
    val isForcePushInProgress: Boolean = false
)

@HiltViewModel
class PreferencesViewModel @Inject constructor(
    @param:ApplicationContext private val applicationContext: Context,
    private val sharedPreferences: SharedPreferences,
    private val appDatabase: AppDatabase,
    private val operatorRepository: OperatorRepository,
    private val activityCategoryRepository: ActivityCategoryRepository,
    private val theBoysRepository: TheBoysRepository,
    private val googleAuthUiClient: GoogleAuthUiClient,
    private val workManager: WorkManager,
    private val firestoreSyncManager: IFirestoreSyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PreferencesUiState())
    val uiState: StateFlow<PreferencesUiState> = _uiState.asStateFlow()

    private val addOperatorFieldOrder = listOf(
        FIELD_OPERATOR_NAME,
        FIELD_HOURLY_SALARY,
        FIELD_OPERATOR_ROLE,
        FIELD_OPERATOR_PRIORITY,
        FIELD_OPERATOR_NOTES,
        FIELD_OPERATOR_NOTES_AI
    )

    private val addTheBoyFieldOrder = listOf(
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
        loadTheBoys()
        loadCurrentUser()
        observeForcePushWorker()
    }

    private fun loadCurrentUser() {
        _uiState.update { it.copy(currentUser = googleAuthUiClient.getSignedInUser()) }
    }

    fun onForcePushAllLocalDataToFirebase() {
        if (_uiState.value.currentUser == null) {
            _uiState.update { it.copy(snackbarMessage = "You must be signed in to push data.") }
            return
        }
        if (_uiState.value.isForcePushInProgress) {
            _uiState.update { it.copy(snackbarMessage = "Data push already in progress.") }
            return
        }
        _uiState.update { it.copy(isForcePushInProgress = true, snackbarMessage = "Starting full data push to cloud...") }
        val uploadWorkRequest = OneTimeWorkRequestBuilder<UploadAllDataWorker>().build()
        workManager.enqueueUniqueWork(UPLOAD_ALL_DATA_WORK_NAME, ExistingWorkPolicy.REPLACE, uploadWorkRequest)
    }

    private fun observeForcePushWorker() {
        viewModelScope.launch {
            workManager.getWorkInfosForUniqueWorkLiveData(UPLOAD_ALL_DATA_WORK_NAME)
                .asFlow()
                .collect { workInfoList ->
                    val workInfo = workInfoList?.firstOrNull() ?: return@collect
                    val snackbarMessage = when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> "All local data successfully pushed to cloud."
                        WorkInfo.State.FAILED -> "Failed to push all local data. Check logs."
                        WorkInfo.State.CANCELLED -> "Data push cancelled."
                        else -> null
                    }
                    val isRunning = workInfo.state == WorkInfo.State.ENQUEUED || workInfo.state == WorkInfo.State.RUNNING
                    _uiState.update { it.copy(isForcePushInProgress = isRunning, snackbarMessage = snackbarMessage ?: it.snackbarMessage) }
                }
        }
    }

    fun onSignInClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAccountActionInProgress = true) }
            try {
                val signInRequest = googleAuthUiClient.createSignInRequest()
                val credentialManager = CredentialManager.create(applicationContext)
                val result = credentialManager.getCredential(applicationContext, signInRequest)
                val signInResult = googleAuthUiClient.signInWithCredential(result.credential)
                if (signInResult.data != null) {
                    _uiState.update { it.copy(currentUser = signInResult.data, snackbarMessage = "Signed in as ${signInResult.data.username}") }
                } else {
                    _uiState.update { it.copy(snackbarMessage = signInResult.errorMessage) }
                }
            } catch (e: GetCredentialException) {
                _uiState.update { it.copy(snackbarMessage = e.message) }
            }
            _uiState.update { it.copy(isAccountActionInProgress = false) }
        }
    }

    fun onSignOutClicked() {
        viewModelScope.launch {
            googleAuthUiClient.signOut()
            _uiState.update { it.copy(currentUser = null, snackbarMessage = "Signed out") }
        }
    }

    private fun loadOperators() {
        operatorRepository.getAllOperators().onEach { operators -> _uiState.update { it.copy(operators = operators) } }.launchIn(viewModelScope)
    }

    private fun loadActivityCategories() {
        activityCategoryRepository.getAllCategories().onEach { categories -> _uiState.update { it.copy(activityCategories = categories) } }.launchIn(viewModelScope)
    }

    private fun loadTheBoys() {
        theBoysRepository.getAllTheBoys().onEach { boys -> _uiState.update { it.copy(theBoysList = boys) } }.launchIn(viewModelScope)
    }

    private fun checkIfPasswordIsSet() {
        _uiState.update { it.copy(isPasswordSet = sharedPreferences.contains(KEY_MASTER_PASSWORD)) }
    }

    private fun loadSmsContact() {
        _uiState.update { it.copy(preferredSmsContact = sharedPreferences.getString(KEY_SMS_CONTACT, null)) }
    }

    private fun checkIfGeminiApiKeyIsSet() {
        _uiState.update { it.copy(isGeminiApiKeySet = sharedPreferences.contains(KEY_GEMINI_API_KEY)) }
    }

    fun clearSnackbarMessage() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun onShowSetPasswordDialog() {
        _uiState.update { it.copy(showSetPasswordDialog = true) }
    }

    fun onDismissSetPasswordDialog() {
        _uiState.update { it.copy(showSetPasswordDialog = false) }
    }

    fun onNewPasswordInputChange(password: String) {
        _uiState.update { it.copy(newPasswordInput = password) }
    }

    fun onConfirmPasswordInputChange(password: String) {
        _uiState.update { it.copy(confirmPasswordInput = password) }
    }

    fun onSavePasswordAttempt() {
        val state = _uiState.value
        if (state.newPasswordInput.length >= 6 && state.newPasswordInput == state.confirmPasswordInput) {
            sharedPreferences.edit().putString(KEY_MASTER_PASSWORD, state.newPasswordInput).apply()
            _uiState.update { it.copy(showSetPasswordDialog = false, isPasswordSet = true, snackbarMessage = "Password set") }
        } else {
            _uiState.update { it.copy(newPasswordError = "Passwords must match and be at least 6 characters.", confirmPasswordError = "Passwords must match and be at least 6 characters.") }
        }
    }

    fun onMasterResetClicked() {
        _uiState.update { it.copy(showMasterResetConfirmationDialog = true) }
    }

    fun onDismissMasterResetConfirmationDialog() {
        _uiState.update { it.copy(showMasterResetConfirmationDialog = false) }
    }

    fun onMasterResetPasswordAttemptChange(password: String) {
        _uiState.update { it.copy(masterResetPasswordAttempt = password) }
    }

    fun onConfirmMasterReset() {
        val attempt = _uiState.value.masterResetPasswordAttempt
        val storedPassword = sharedPreferences.getString(KEY_MASTER_PASSWORD, null)
        val userId = _uiState.value.currentUser?.userId

        if (storedPassword != null && attempt == storedPassword) {
            viewModelScope.launch {
                try {
                    if (userId != null) {
                        firestoreSyncManager.deleteAllUserData(userId)
                    }
                    withContext(Dispatchers.IO) {
                        appDatabase.clearAllTables()
                        appDatabase.theBoysInfoDao().resetAutoIncrement()
                        appDatabase.operatorInfoDao().resetAutoIncrement()
                        sharedPreferences.edit().clear().apply()
                        googleAuthUiClient.signOut()
                    }
                    _uiState.value = PreferencesUiState(snackbarMessage = "All application data and settings have been wiped.")
                } catch (e: Exception) {
                    Log.e(TAG, "Error during master reset: ", e)
                    _uiState.update { it.copy(snackbarMessage = "Error during master reset: ${e.localizedMessage}") }
                } finally {
                    _uiState.update { it.copy(showMasterResetConfirmationDialog = false, masterResetPasswordAttempt = "") }
                }
            }
        } else {
            _uiState.update { it.copy(masterResetPasswordError = "Incorrect password.") }
        }
    }

    fun onShowSmsContactDialog() {
        _uiState.update { it.copy(showSmsContactDialog = true) }
    }

    fun onDismissSmsContactDialog() {
        _uiState.update { it.copy(showSmsContactDialog = false) }
    }

    fun onSmsContactInputChange(contact: String) {
        _uiState.update { it.copy(smsContactInput = contact) }
    }

    fun onSaveSmsContact() {
        sharedPreferences.edit().putString(KEY_SMS_CONTACT, _uiState.value.smsContactInput).apply()
        _uiState.update { it.copy(showSmsContactDialog = false, preferredSmsContact = _uiState.value.smsContactInput) }
    }

    fun onShowSetGeminiApiKeyDialog() {
        _uiState.update { it.copy(showSetGeminiApiKeyDialog = true) }
    }

    fun onDismissSetGeminiApiKeyDialog() {
        _uiState.update { it.copy(showSetGeminiApiKeyDialog = false) }
    }

    fun onGeminiApiKeyInputChange(apiKey: String) {
        _uiState.update { it.copy(geminiApiKeyInput = apiKey) }
    }

    fun onSaveGeminiApiKey() {
        sharedPreferences.edit().putString(KEY_GEMINI_API_KEY, _uiState.value.geminiApiKeyInput).apply()
        _uiState.update { it.copy(showSetGeminiApiKeyDialog = false, isGeminiApiKeySet = true) }
    }

    fun onOperatorSectionClicked() {
        if (_uiState.value.isOperatorSectionUnlocked) {
            _uiState.update { it.copy(showOperatorListDialog = true) }
        } else {
            _uiState.update { it.copy(showOperatorPasswordDialog = true) }
        }
    }

    fun onDismissOperatorPasswordDialog() {
        _uiState.update { it.copy(showOperatorPasswordDialog = false, showTheBoysPasswordDialog = false) }
    }

    fun onOperatorPasswordAttemptChange(password: String) {
        _uiState.update { it.copy(operatorPasswordAttempt = password) }
    }

    fun onUnlockSectionAttempt() {
        if (_uiState.value.operatorPasswordAttempt == BuildConfig.OPERATOR_INFO_PASSWORD) {
            _uiState.update { it.copy(isOperatorSectionUnlocked = true, showOperatorPasswordDialog = false, showTheBoysPasswordDialog = false) }
        } else {
            _uiState.update { it.copy(operatorPasswordError = "Incorrect password.") }
        }
    }

    fun onDismissOperatorListDialog() {
        _uiState.update { it.copy(showOperatorListDialog = false) }
    }

    fun onAddNewOperatorClicked() {
        _uiState.update { it.copy(showAddOperatorDialog = true, addOperatorStep = 0, newOperatorInputs = emptyMap(), newOperatorErrors = emptyMap()) }
    }

    fun onDismissAddOperatorDialog() {
        _uiState.update { it.copy(showAddOperatorDialog = false) }
    }
    
    fun onNewOperatorInputChange(fieldName: String, value: String) {
        _uiState.update { it.copy(newOperatorInputs = _uiState.value.newOperatorInputs + (fieldName to value)) }
    }

    fun onAddOperatorNextStep() {
        _uiState.update { it.copy(addOperatorStep = _uiState.value.addOperatorStep + 1) }
    }

    fun onAddOperatorPreviousStep() {
        _uiState.update { it.copy(addOperatorStep = _uiState.value.addOperatorStep - 1) }
    }
    
    fun onSaveNewOperator() {
        val inputs = _uiState.value.newOperatorInputs
        val operator = OperatorInfo(
            name = inputs[FIELD_OPERATOR_NAME]!!,
            hourlySalary = inputs[FIELD_HOURLY_SALARY]!!.toDouble(),
            role = inputs[FIELD_OPERATOR_ROLE]!!,
            priority = inputs[FIELD_OPERATOR_PRIORITY]!!.toInt(),
            notes = inputs[FIELD_OPERATOR_NOTES],
            notesForAi = inputs[FIELD_OPERATOR_NOTES_AI]
        )
        viewModelScope.launch {
            operatorRepository.insertOperator(operator)
            _uiState.update { it.copy(showAddOperatorDialog = false, newOperatorInputs = emptyMap(), addOperatorStep = 0) }
        }
    }
    
    fun onEditOperatorClicked(operator: OperatorInfo) {
        _uiState.update { it.copy(
            editingOperator = operator, 
            showEditOperatorDialog = true,
            operatorNameInput = operator.name,
            operatorHourlySalaryInput = operator.hourlySalary.toString(),
            operatorRoleInput = operator.role,
            operatorPriorityInput = operator.priority.toString(),
            operatorNotesInput = operator.notes ?: "",
            operatorNotesForAiInput = operator.notesForAi ?: ""
        )}
    }

    fun onDismissEditOperatorDialog() {
        _uiState.update { it.copy(showEditOperatorDialog = false) }
    }

    fun onOperatorNameChange(name: String) { _uiState.update { it.copy(operatorNameInput = name) } }
    fun onOperatorHourlySalaryChange(salary: String) { _uiState.update { it.copy(operatorHourlySalaryInput = salary) } }
    fun onOperatorRoleChange(role: String) { _uiState.update { it.copy(operatorRoleInput = role) } }
    fun onOperatorPriorityChange(priority: String) { _uiState.update { it.copy(operatorPriorityInput = priority) } }
    fun onOperatorNotesChange(notes: String) { _uiState.update { it.copy(operatorNotesInput = notes) } }
    fun onOperatorNotesForAiChange(notesForAi: String) { _uiState.update { it.copy(operatorNotesForAiInput = notesForAi) } }

    fun onSaveEditOperator() {
        val state = _uiState.value
        val operator = state.editingOperator!!.copy(
            name = state.operatorNameInput,
            hourlySalary = state.operatorHourlySalaryInput.toDouble(),
            role = state.operatorRoleInput,
            priority = state.operatorPriorityInput.toInt(),
            notes = state.operatorNotesInput,
            notesForAi = state.operatorNotesForAiInput
        )
        viewModelScope.launch {
            operatorRepository.updateOperator(operator)
            _uiState.update { it.copy(showEditOperatorDialog = false) }
        }
    }
    
    fun onDeleteOperator(operator: OperatorInfo) {
        viewModelScope.launch {
            operatorRepository.deleteOperator(operator)
        }
    }
    
    fun onTheBoysSectionClicked() {
        if (_uiState.value.isOperatorSectionUnlocked) {
            _uiState.update { it.copy(showTheBoysListDialog = true) }
        } else {
            _uiState.update { it.copy(showTheBoysPasswordDialog = true) }
        }
    }

    fun onDismissTheBoysListDialog() {
        _uiState.update { it.copy(showTheBoysListDialog = false) }
    }
    
    fun onAddNewTheBoyClicked() {
        _uiState.update { it.copy(showAddTheBoyDialog = true, addTheBoyStep = 0, newTheBoyInputs = emptyMap(), newTheBoyErrors = emptyMap()) }
    }
    
    fun onDismissAddTheBoyDialog() {
        _uiState.update { it.copy(showAddTheBoyDialog = false) }
    }

    fun onNewTheBoyInputChange(fieldName: String, value: String) {
        _uiState.update { it.copy(newTheBoyInputs = _uiState.value.newTheBoyInputs + (fieldName to value)) }
    }

    fun onAddTheBoyNextStep() {
        _uiState.update { it.copy(addTheBoyStep = _uiState.value.addTheBoyStep + 1) }
    }

    fun onAddTheBoyPreviousStep() {
        _uiState.update { it.copy(addTheBoyStep = _uiState.value.addTheBoyStep - 1) }
    }
    
    fun onSaveNewTheBoy() {
        val inputs = _uiState.value.newTheBoyInputs
        val boy = TheBoysInfo(
            name = inputs[FIELD_BOY_NAME]!!,
            role = inputs[FIELD_BOY_ROLE]!!,
            notes = inputs[FIELD_BOY_NOTES],
            notesForAi = inputs[FIELD_BOY_NOTES_AI]
        )
        viewModelScope.launch {
            theBoysRepository.insertTheBoy(boy)
            _uiState.update { it.copy(showAddTheBoyDialog = false, newTheBoyInputs = emptyMap(), addTheBoyStep = 0) }
        }
    }

    fun onEditTheBoyClicked(boy: TheBoysInfo) {
        _uiState.update { it.copy(
            editingTheBoy = boy, 
            showEditTheBoyDialog = true,
            editBoyNameInput = boy.name,
            editBoyRoleInput = boy.role,
            editBoyNotesInput = boy.notes ?: "",
            editBoyNotesForAiInput = boy.notesForAi ?: ""
        )}
    }

    fun onDismissEditTheBoyDialog() {
        _uiState.update { it.copy(showEditTheBoyDialog = false) }
    }

    fun onEditBoyNameChange(name: String) { _uiState.update { it.copy(editBoyNameInput = name) } }
    fun onEditBoyRoleChange(role: String) { _uiState.update { it.copy(editBoyRoleInput = role) } }
    fun onEditBoyNotesChange(notes: String) { _uiState.update { it.copy(editBoyNotesInput = notes) } }
    fun onEditBoyNotesForAiChange(notesForAi: String) { _uiState.update { it.copy(editBoyNotesForAiInput = notesForAi) } }
    
    fun onSaveEditTheBoy() {
        val state = _uiState.value
        val boy = state.editingTheBoy!!.copy(
            name = state.editBoyNameInput,
            role = state.editBoyRoleInput,
            notes = state.editBoyNotesInput,
            notesForAi = state.editBoyNotesForAiInput
        )
        viewModelScope.launch {
            theBoysRepository.updateTheBoy(boy)
            _uiState.update { it.copy(showEditTheBoyDialog = false) }
        }
    }
    
    fun onDeleteTheBoy(boy: TheBoysInfo) {
        viewModelScope.launch {
            theBoysRepository.deleteTheBoy(boy)
        }
    }
    
    fun onManageCategoriesClicked() {
        _uiState.update { it.copy(showManageCategoriesDialog = true) }
    }

    fun onDismissManageCategoriesDialog() {
        _uiState.update { it.copy(showManageCategoriesDialog = false) }
    }
    
    fun onAddNewCategoryClicked() {
        _uiState.update { it.copy(showAddCategoryDialog = true) }
    }
    
    fun onDismissAddCategoryDialog() {
        _uiState.update { it.copy(showAddCategoryDialog = false) }
    }

    fun onNewCategoryInputChange(name: String) {
        _uiState.update { it.copy(newCategoryInput = name) }
    }
    
    fun onSaveNewCategory() {
        val category = ActivityCategory(name = _uiState.value.newCategoryInput)
        viewModelScope.launch {
            activityCategoryRepository.insertCategory(category)
            _uiState.update { it.copy(showAddCategoryDialog = false) }
        }
    }

    fun onEditCategoryClicked(category: ActivityCategory) {
        _uiState.update { it.copy(editingCategory = category, showEditCategoryDialog = true) }
    }

    fun onDismissEditCategoryDialog() {
        _uiState.update { it.copy(showEditCategoryDialog = false) }
    }

    fun onEditCategoryInputChange(name: String) {
        _uiState.update { it.copy(editCategoryInput = name) }
    }
    
    fun onSaveEditCategory() {
        val category = _uiState.value.editingCategory!!.copy(name = _uiState.value.editCategoryInput)
        viewModelScope.launch {
            activityCategoryRepository.updateCategory(category)
            _uiState.update { it.copy(showEditCategoryDialog = false) }
        }
    }
    
    fun onDeleteCategory(category: ActivityCategory) {
        viewModelScope.launch {
            activityCategoryRepository.deleteCategory(category)
        }
    }
}
