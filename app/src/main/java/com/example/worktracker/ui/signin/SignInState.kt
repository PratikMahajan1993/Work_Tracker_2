package com.example.worktracker.ui.signin

/**
 * Represents the state of the Sign-In UI.
 */
data class SignInState(
    val isLoading: Boolean = false
    // Removed isSignInSuccessful and signInError as per previous refactoring,
    // those are now handled by SignInUiEvent
)
