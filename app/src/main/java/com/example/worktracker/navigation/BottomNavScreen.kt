package com.example.worktracker.navigation

            import androidx.compose.material.icons.Icons
            import androidx.compose.material.icons.automirrored.filled.List // Added import
            import androidx.compose.material.icons.filled.Assessment
            import androidx.compose.material.icons.filled.Home
            import androidx.compose.material.icons.filled.Settings
            import androidx.compose.material.icons.filled.SmartToy
            import androidx.compose.ui.graphics.vector.ImageVector

            sealed class BottomNavScreen(val route: String, val label: String, val icon: ImageVector) {
                object Dashboard : BottomNavScreen("dashboard", "Dashboard", Icons.Default.Home)
                object History : BottomNavScreen("history", "History", Icons.AutoMirrored.Filled.List) // Changed here
                object Reports : BottomNavScreen("reports", "Reports", Icons.Default.Assessment)
                object ChatBot : BottomNavScreen("chatbot", "ChatBot", Icons.Default.SmartToy)
                object Preferences : BottomNavScreen("preferences_route", "Preferences", Icons.Default.Settings)
            }
            