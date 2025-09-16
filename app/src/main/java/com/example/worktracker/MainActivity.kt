package com.example.worktracker

// import android.app.Activity // No longer needed for launcher
// import android.app.PendingIntent // No longer needed
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
// import androidx.activity.result.IntentSenderRequest // No longer needed
// import androidx.activity.result.contract.ActivityResultContracts // No longer needed
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.GetCredentialException
// import androidx.credentials.exceptions.GetCredentialInterruptedException // No longer needed for special handling
// import androidx.hilt.navigation.compose.hiltViewModel // Deprecated import
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel // Correct import for hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.worktracker.ui.MainScreen
import com.example.worktracker.ui.logproduction.LogProductionScreen
import com.example.worktracker.ui.logwork.LogWorkActivityScreen
import com.example.worktracker.ui.screens.preferences.PreferencesScreen
import com.example.worktracker.ui.screens.preferences.PreferencesViewModel
import com.example.worktracker.ui.screens.preferences.components.ComponentListScreen
import com.example.worktracker.ui.screens.preferences.components.ManageComponentsScreen
import com.example.worktracker.ui.screens.workdetails.ROUTE_ARG_CATEGORY_NAME
import com.example.worktracker.ui.screens.workdetails.WorkDetailsRoute
import com.example.worktracker.ui.signin.GoogleAuthUiClient
import com.example.worktracker.ui.signin.SignInScreen
import com.example.worktracker.ui.signin.SignInViewModel
import com.example.worktracker.ui.signin.SignInErrorType
import com.example.worktracker.ui.signin.SignInUiEvent
import com.example.worktracker.ui.theme.WorkTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorkTrackerTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigationHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding),
                        googleAuthUiClient = googleAuthUiClient,
                        activity = this
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigationHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    googleAuthUiClient: GoogleAuthUiClient,
    activity: ComponentActivity // Still needed for CredentialManager.create(activity)
) {
    val startDestination = if (googleAuthUiClient.getSignedInUser() != null) {
        AppRoutes.MAIN_SCREEN
    } else {
        AppRoutes.SIGN_IN_SCREEN
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(AppRoutes.SIGN_IN_SCREEN) {
            val viewModel = viewModel<SignInViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()
            val currentContext = LocalContext.current

            LaunchedEffect(key1 = Unit) {
                // This ensures that if the user is already signed in (e.g. app was minimized and reopened)
                // they are navigated directly to the main screen.
                if (googleAuthUiClient.getSignedInUser() != null) {
                    navController.navigate(AppRoutes.MAIN_SCREEN) {
                        popUpTo(AppRoutes.SIGN_IN_SCREEN) { inclusive = true }
                    }
                }
            }

            LaunchedEffect(key1 = viewModel, key2 = navController) {
                viewModel.signInUiEvent.collect {
                    event ->
                    when (event) {
                        is SignInUiEvent.NavigateToMain -> {
                            Toast.makeText(
                                currentContext.applicationContext,
                                "Sign in successful for ${event.userData.username ?: "User"}",
                                Toast.LENGTH_LONG
                            ).show()
                            navController.navigate(AppRoutes.MAIN_SCREEN) {
                                popUpTo(AppRoutes.SIGN_IN_SCREEN) { inclusive = true }
                            }
                            viewModel.resetState()
                        }
                        is SignInUiEvent.ShowError -> {
                            Toast.makeText(
                                currentContext.applicationContext,
                                event.message,
                                Toast.LENGTH_LONG
                            ).show()
                            viewModel.resetState()
                        }
                    }
                }
            }

            SignInScreen(
                state = state,
                onSignInClick = {
                    viewModel.beginSignIn()
                    activity.lifecycleScope.launch {
                        try {
                            val signInRequest = googleAuthUiClient.createSignInRequest()
                            val credentialManager = CredentialManager.create(activity)
                            val result = credentialManager.getCredential(activity, signInRequest)
                            val signInResult = googleAuthUiClient.signInWithCredential(result.credential)
                            viewModel.onSignInResult(signInResult) // ViewModel handles navigation via UiEvent
                        } catch (e: GetCredentialException) {
                            viewModel.onSignInActivityError(
                                e.localizedMessage ?: "Sign-in failed. Type: ${e.type}. Please try again.",
                                SignInErrorType.CredentialManagerError(type = e.type)
                            )
                        } catch (e: Exception) {
                             viewModel.onSignInActivityError(
                                e.localizedMessage ?: "An unexpected error occurred. Please try again.",
                                SignInErrorType.UnknownError
                            )
                        }
                    }
                },
                onContinueAsGuestClick = { // Added handler for guest navigation
                    navController.navigate(AppRoutes.MAIN_SCREEN) {
                        popUpTo(AppRoutes.SIGN_IN_SCREEN) { inclusive = true }
                    }
                }
            )
        }
        composable(
            AppRoutes.MAIN_SCREEN,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            MainScreen(
                mainActivityNavController = navController,
                onNavigateToEditWorkLog = { workLogId, categoryName ->
                    navController.navigate(AppRoutes.workDetailsRoute(categoryName, workLogId))
                },
                onNavigateToEditProductionLog = { productionLogId ->
                    navController.navigate(AppRoutes.logProductionActivityRoute(productionLogId))
                }
            )
        }

        composable(AppRoutes.PREFERENCES_SCREEN) {
            val preferencesViewModel: PreferencesViewModel = hiltViewModel() // This line uses hiltViewModel

            PreferencesScreen(
                mainScreenPadding = PaddingValues(),
                navController = navController,
                viewModel = preferencesViewModel
            )
        }

        composable(
            AppRoutes.SELECT_CATEGORY,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            LogWorkActivityScreen(
                onCategorySelected = { categoryName ->
                    navController.navigate(AppRoutes.workDetailsRoute(categoryName))
                }
            )
        }
        composable(
            route = AppRoutes.WORK_DETAILS,
            arguments = listOf(
                navArgument(ROUTE_ARG_CATEGORY_NAME) { type = NavType.StringType },
                navArgument("workLogId") { type = NavType.LongType; defaultValue = 0L }
            ),
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            WorkDetailsRoute(navController = navController)
        }
        composable(
            route = AppRoutes.LOG_PRODUCTION_ACTIVITY,
            arguments = listOf(
                navArgument("productionLogId") { type = NavType.LongType; defaultValue = 0L }
            ),
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            LogProductionScreen(navController = navController)
        }

        composable(
            AppRoutes.MANAGE_COMPONENTS,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) } 
        ) {
            ManageComponentsScreen(
                onNavigateBack = { navController.popBackStack() },
                editingComponentId = null
            )
        }

        composable(
            route = "${AppRoutes.MANAGE_COMPONENTS}/{componentId}",
            arguments = listOf(navArgument("componentId") { type = NavType.LongType }),
            enterTransition = { fadeIn(animationSpec = tween(300)) }, 
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) }
        ) { backStackEntry ->
            val componentId = backStackEntry.arguments?.getLong("componentId")
            ManageComponentsScreen(
                onNavigateBack = { navController.popBackStack() },
                editingComponentId = componentId
            )
        }

        composable(
            AppRoutes.VIEW_COMPONENTS,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            ComponentListScreen(navController = navController)
        }
    }
}
