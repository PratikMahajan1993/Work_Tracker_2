package com.example.worktracker.ui.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// SignInState is now in its own file: SignInState.kt

// Step I.2: Defined SignInUiEvent
sealed interface SignInUiEvent {
    data class NavigateToMain(val userData: UserData) : SignInUiEvent
    data class ShowError(val message: String, val errorType: SignInErrorType?) : SignInUiEvent
}

class SignInViewModel : ViewModel() {

    private val _state = MutableStateFlow(SignInState()) // Will use the imported SignInState
    val state = _state.asStateFlow()

    private val _signInUiEvent = MutableSharedFlow<SignInUiEvent>()
    val signInUiEvent = _signInUiEvent.asSharedFlow()

    fun beginSignIn() {
        _state.update { it.copy(isLoading = true) }
    }

    fun onSignInResult(result: SignInResult) {
        _state.update { it.copy(isLoading = false) }
        viewModelScope.launch {
            if (result.data != null) {
                _signInUiEvent.emit(SignInUiEvent.NavigateToMain(result.data))
            } else {
                // Prefer errorType and a generic message if specific errorMessage isn't insightful for the user
                val errorMessage = result.errorMessage ?: "An unknown sign-in error occurred."
                _signInUiEvent.emit(SignInUiEvent.ShowError(errorMessage, result.errorType))
            }
        }
    }

    // For errors caught directly in the Activity/Fragment before a SignInResult is produced
    fun onSignInActivityError(errorMessage: String, errorType: SignInErrorType) {
        _state.update { it.copy(isLoading = false) }
        viewModelScope.launch {
            _signInUiEvent.emit(SignInUiEvent.ShowError(errorMessage, errorType))
        }
    }

    fun resetState() {
        _state.update { SignInState() } // Resets isLoading to false
        // No explicit reset for SharedFlow needed for typical one-time event use cases
    }
}
