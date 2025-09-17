package com.example.worktracker.ui.signin

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential // Required for CustomCredential handling
import androidx.credentials.GetCredentialRequest
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.worktracker.R
import com.example.worktracker.data.sync.SyncWorker
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential // For GoogleIdTokenCredential.createFrom and .TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException

private const val TAG = "GoogleAuthUiClient"

sealed class SignInErrorType {
    data object NetworkError : SignInErrorType()
    data class FirebaseAuthError(val errorCode: String? = null) : SignInErrorType()
    data class CredentialManagerError(val type: String? = null) : SignInErrorType()
    data object NoSignedInUser : SignInErrorType()
    data object UnknownError : SignInErrorType()
}

class GoogleAuthUiClient(
    private val context: Context, // Application context should be provided here by Hilt or manually
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
        val idToken: String?

        if (credential is GoogleIdTokenCredential) {
            idToken = credential.idToken
        } else if (credential is CustomCredential && 
                   credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
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

        return try {
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val firebaseUser: FirebaseUser? = auth.signInWithCredential(firebaseCredential).await()?.user

            if (firebaseUser != null) {
                Log.i(TAG, "Firebase Sign-In successful for user: ${firebaseUser.uid}")

                // --- Trigger SyncWorker to download data on successful sign-in ---
                try {
                    val workManager = WorkManager.getInstance(context.applicationContext) // Use applicationContext
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                    val downloadRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                        .setConstraints(constraints)
                        .build()
                    
                    // Use REPLACE policy to ensure data downloads if user signs out and back in
                    workManager.enqueueUniqueWork(
                        "DownloadUserDataOnSignIn_${firebaseUser.uid}", // Unique name per user
                        ExistingWorkPolicy.REPLACE, 
                        downloadRequest
                    )
                    Log.i(TAG, "Enqueued SyncWorker for data download for user: ${firebaseUser.uid}")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to enqueue SyncWorker on sign-in", e)
                    // Optionally, you might want to inform the user or handle this error
                    // but the sign-in itself was successful.
                }
                // --- End of SyncWorker trigger ---

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
