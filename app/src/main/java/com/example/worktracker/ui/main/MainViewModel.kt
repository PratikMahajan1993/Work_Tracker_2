package com.example.worktracker.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worktracker.BuildConfig
import com.example.worktracker.data.repository.WorkActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainScreenUiState(
    val showPasswordDialog: Boolean = false,
    val passwordInput: String = "",
    val passwordError: String? = null
)

sealed interface MainScreenEvent {
    data object DatabaseWipedSuccessfully : MainScreenEvent
    data class DatabaseWipeFailed(val message: String) : MainScreenEvent
    // IncorrectPassword event is handled via passwordError in UiState for now
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: WorkActivityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainScreenUiState())
    val uiState: StateFlow<MainScreenUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<MainScreenEvent>()
    val eventFlow: SharedFlow<MainScreenEvent> = _eventFlow.asSharedFlow()

    fun onWipeDatabaseClicked() {
        _uiState.update {
            it.copy(showPasswordDialog = true, passwordInput = "", passwordError = null)
        }
    }

    fun onPasswordInputChanged(newInput: String) {
        _uiState.update { it.copy(passwordInput = newInput, passwordError = null) }
    }

    fun onPasswordDialogDismissed() {
        _uiState.update {
            it.copy(showPasswordDialog = false, passwordInput = "", passwordError = null)
        }
    }

    fun onPasswordSubmitted() {
        val enteredPassword = _uiState.value.passwordInput
        // It's important to ensure BuildConfig.DB_WIPE_PASSWORD is not empty or null
        val correctPassword = BuildConfig.DB_WIPE_PASSWORD.takeIf { it.isNotBlank() } ?: "SUPER_SECRET_FALLBACK"

        if (enteredPassword == correctPassword) {
            viewModelScope.launch {
                try {
                    repository.clearAllLogs() // This is the existing function
                    _eventFlow.emit(MainScreenEvent.DatabaseWipedSuccessfully)
                    onPasswordDialogDismissed() // Dismiss dialog on success
                } catch (e: Exception) {
                    _eventFlow.emit(MainScreenEvent.DatabaseWipeFailed(e.localizedMessage ?: "Unknown error"))
                    // Optionally keep dialog open or provide error state in dialog
                }
            }
        } else {
            _uiState.update {
                it.copy(passwordError = "Incorrect password.", passwordInput = "")
            }
        }
    }
}
