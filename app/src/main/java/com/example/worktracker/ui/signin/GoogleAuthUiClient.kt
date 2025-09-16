package com.example.worktracker.ui.signin

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential // Required for CustomCredential handling
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential // For GoogleIdTokenCredential.createFrom and .TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException
import com.example.worktracker.R

private const val TAG = "GoogleAuthUiClient"

sealed class SignInErrorType {
    data object NetworkError : SignInErrorType()
    data class FirebaseAuthError(val errorCode: String? = null) : SignInErrorType()
    data class CredentialManagerError(val type: String? = null) : SignInErrorType()
    data object NoSignedInUser : SignInErrorType()
    data object UnknownError : SignInErrorType()
}

class GoogleAuthUiClient(
    private val context: Context,
) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val credentialManager: CredentialManager by lazy { CredentialManager.create(context) }

    fun createSignInRequest(): GetCredentialRequest {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .build()

        return GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    private suspend fun processSignIn(credential: androidx.credentials.Credential): SignInResult {
        val idToken: String? // Changed to val as it's assigned only once per successful path

        if (credential is GoogleIdTokenCredential) {
            // This case might still be valid if CredentialManager directly returns GoogleIdTokenCredential
            idToken = credential.idToken
        } else if (credential is CustomCredential && 
                   credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            // Handle CustomCredential for Google Sign-In
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                idToken = googleIdTokenCredential.idToken
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create GoogleIdTokenCredential from CustomCredential", e)
                return SignInResult(
                    data = null,
                    errorMessage = "Failed to process Google Sign-In data: ${e.localizedMessage}",
                    errorType = SignInErrorType.CredentialManagerError(type = credential.type)
                )
            }
        } else {
            Log.w(TAG, "Unexpected credential type received: ${credential.type}")
            return SignInResult(
                data = null,
                errorMessage = "Unexpected credential type received: ${credential.type}",
                errorType = SignInErrorType.CredentialManagerError(type = credential.type)
            )
        }

        // The redundant 'if (idToken == null)' check has been removed here.
        // If code execution reaches this point, idToken is guaranteed to be non-null.

        return try {
            // idToken is non-null here, so direct usage is safe.
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val firebaseUser: FirebaseUser? = auth.signInWithCredential(firebaseCredential).await()?.user

            if (firebaseUser != null) {
                Log.i(TAG, "Firebase Sign-In successful for user: ${firebaseUser.uid}")
                SignInResult(
                    data = UserData(
                        userId = firebaseUser.uid,
                        username = firebaseUser.displayName,
                        profilePictureUrl = firebaseUser.photoUrl?.toString()
                    ),
                    errorMessage = null,
                    errorType = null
                )
            } else {
                Log.w(TAG, "Firebase Sign-In failed: firebaseUser is null after auth.signInWithCredential")
                SignInResult(
                    data = null,
                    errorMessage = "Firebase Sign-In failed: User is null after authentication.",
                    errorType = SignInErrorType.FirebaseAuthError()
                )
            }
        } catch (e: FirebaseAuthException) {
            Log.e(TAG, "FirebaseAuthException during sign-in: ${e.errorCode}", e)
            SignInResult(
                data = null,
                errorMessage = "Firebase authentication failed: ${e.localizedMessage} (Code: ${e.errorCode})",
                errorType = SignInErrorType.FirebaseAuthError(errorCode = e.errorCode)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Sign-In with ID token failed", e)
            if (e is CancellationException) throw e
            SignInResult(
                data = null,
                errorMessage = "Sign-In failed: ${e.localizedMessage}",
                errorType = SignInErrorType.UnknownError
            )
        }
    }

    suspend fun signInWithCredential(credential: androidx.credentials.Credential): SignInResult {
        Log.d(TAG, "signInWithCredential called with type: ${credential.type}")
        return processSignIn(credential)
    }

    suspend fun signOut() {
        try {
            Log.i(TAG, "Signing out user: ${auth.currentUser?.uid}")
            auth.signOut()
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            Log.i(TAG, "User signed out and credential state cleared.")
        } catch (e: Exception) {
            Log.e(TAG, "Error during sign out", e)
            if (e is CancellationException) throw e
        }
    }

    fun getSignedInUser(): UserData? {
        val firebaseUser: FirebaseUser? = auth.currentUser
        return if (firebaseUser != null) {
            Log.d(TAG, "getSignedInUser: User found with ID ${firebaseUser.uid}")
            UserData(
                userId = firebaseUser.uid,
                username = firebaseUser.displayName,
                profilePictureUrl = firebaseUser.photoUrl?.toString()
            )
        } else {
            Log.d(TAG, "getSignedInUser: No user signed in.")
            null
        }
    }
}

data class SignInResult(
    val data: UserData?,
    val errorMessage: String?,
    val errorType: SignInErrorType? = null
)

data class UserData(
    val userId: String,
    val username: String?,
    val profilePictureUrl: String?
)
