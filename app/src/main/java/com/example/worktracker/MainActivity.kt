package com.example.worktracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.worktracker.ui.MainScreen
import com.example.worktracker.ui.logproduction.LogProductionScreen
import com.example.worktracker.ui.logwork.LogWorkActivityScreen
import com.example.worktracker.ui.screens.preferences.components.ManageComponentsScreen
import com.example.worktracker.ui.screens.preferences.components.ComponentListScreen
import com.example.worktracker.ui.screens.workdetails.ROUTE_ARG_CATEGORY_NAME 
import com.example.worktracker.ui.screens.workdetails.WorkDetailsRoute
// Removed navigateToWorkDetails as we will use AppRoutes helpers directly
// import com.example.worktracker.ui.screens.workdetails.navigateToWorkDetails 
import com.example.worktracker.ui.theme.WorkTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorkTrackerTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigationHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigationHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.MAIN_SCREEN,
        modifier = modifier
    ) {
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

        composable(
            AppRoutes.SELECT_CATEGORY,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
             LogWorkActivityScreen(
                onCategorySelected = { categoryName ->
                    // Use AppRoutes helper for creating new work log (workLogId is null/0 by default)
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
