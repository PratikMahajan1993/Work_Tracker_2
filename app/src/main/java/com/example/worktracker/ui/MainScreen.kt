package com.example.worktracker.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.worktracker.AppRoutes
import com.example.worktracker.navigation.BottomNavScreen
import com.example.worktracker.ui.reports.ReportsHubScreen
import com.example.worktracker.ui.screens.dashboard.DashboardScreen
import com.example.worktracker.ui.screens.history.HistoryScreen
import com.example.worktracker.ui.screens.preferences.PreferencesScreen
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.PaddingValues

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen(
    mainActivityNavController: NavController,
    // Added navigation lambdas for editing
    onNavigateToEditWorkLog: (workLogId: Long, categoryName: String) -> Unit,
    onNavigateToEditProductionLog: (productionLogId: Long) -> Unit
) {
    val bottomNavController = rememberNavController()
    val screens = listOf(
        BottomNavScreen.Dashboard,
        BottomNavScreen.History,
        BottomNavScreen.Reports,
        BottomNavScreen.ChatBot,
        BottomNavScreen.Preferences
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = {
                            Text(
                                screen.label,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                            )
                        },
                        selected = currentRoute == screen.route,
                        onClick = {
                            if (currentRoute != screen.route) {
                                bottomNavController.navigate(screen.route) {
                                    popUpTo(bottomNavController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavScreen.Dashboard.route,
            modifier = Modifier // Removed .padding(innerPadding) here
        ) {
            composable(
                BottomNavScreen.Dashboard.route,
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) },
                popEnterTransition = { fadeIn(animationSpec = tween(300)) },
                popExitTransition = { fadeOut(animationSpec = tween(300)) }
            ) {
                DashboardScreen(
                    paddingValues = innerPadding,
                    onNavigateToLogWork = {
                        mainActivityNavController.navigate(AppRoutes.SELECT_CATEGORY) // Updated Route
                    },
                    onNavigateToLogProduction = { 
                        mainActivityNavController.navigate(AppRoutes.logProductionActivityRoute()) // Use helper for consistency
                    }
                )
            }
            composable(
                BottomNavScreen.History.route,
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) },
                popEnterTransition = { fadeIn(animationSpec = tween(300)) },
                popExitTransition = { fadeOut(animationSpec = tween(300)) }
            ) {
                HistoryScreen(
                    paddingValues = innerPadding,
                    onNavigateToLogWork = {
                        mainActivityNavController.navigate(AppRoutes.SELECT_CATEGORY) // Updated Route
                    },
                    onNavigateToLogProduction = { 
                        mainActivityNavController.navigate(AppRoutes.logProductionActivityRoute()) // Use helper
                    },
                    onNavigateToEditWorkLog = onNavigateToEditWorkLog, // Pass lambda
                    onNavigateToEditProductionLog = onNavigateToEditProductionLog // Pass lambda
                )
            }
            composable(
                BottomNavScreen.Reports.route,
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) },
                popEnterTransition = { fadeIn(animationSpec = tween(300)) },
                popExitTransition = { fadeOut(animationSpec = tween(300)) }
            ) {
                ReportsHubScreen(mainScreenPadding = innerPadding) 
            }
            composable(
                BottomNavScreen.ChatBot.route,
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) },
                popEnterTransition = { fadeIn(animationSpec = tween(300)) },
                popExitTransition = { fadeOut(animationSpec = tween(300)) }
            ) {
                Text("ChatBot Screen (Coming Soon!)", modifier = Modifier.padding(innerPadding))
            }
            composable(
                BottomNavScreen.Preferences.route,
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) },
                popEnterTransition = { fadeIn(animationSpec = tween(300)) },
                popExitTransition = { fadeOut(animationSpec = tween(300)) }
            ) {
                PreferencesScreen(mainScreenPadding = innerPadding, navController = mainActivityNavController)
            }
        }
    }
}
