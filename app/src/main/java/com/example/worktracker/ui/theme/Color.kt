package com.example.worktracker.ui.theme

import androidx.compose.ui.graphics.Color

// Provided Green Palette (using descriptive names for clarity)
val AppGreenLightest = Color(0xFFAAD688)
val AppGreenLighter = Color(0xFF98C377)
val AppGreenMid = Color(0xFF8BBD78)
val AppGreenDarker = Color(0xFF5EA758)
val AppGreenDarkest = Color(0xFF47894B)

// Light Theme Colors
val Light_Primary = AppGreenMid            // Main brand color
val Light_OnPrimary = Color(0xFFFFFFFF)    // White text on primary
val Light_PrimaryContainer = AppGreenLighter // Lighter version for containers
val Light_OnPrimaryContainer = Color(0xFF000000) // Black text on primary container

val Light_Secondary = AppGreenDarker      // Accent color, slightly deeper
val Light_OnSecondary = Color(0xFFFFFFFF)  // White text on secondary
val Light_SecondaryContainer = AppGreenLighter // Reusing lighter green for secondary container
val Light_OnSecondaryContainer = Color(0xFF000000) // Black text on secondary container

val Light_Tertiary = AppGreenDarkest      // Deepest green for strong accents
val Light_OnTertiary = Color(0xFFFFFFFF)   // White text on tertiary
val Light_TertiaryContainer = AppGreenLighter // Reusing lighter green for tertiary container
val Light_OnTertiaryContainer = Color(0xFF000000) // Black text on tertiary container

// Standard system colors for light theme
val Light_Background = Color(0xFFFFFFFF)
val Light_OnBackground = Color(0xFF000000)
val Light_Surface = Color(0xFFFFFFFF)
val Light_OnSurface = Color(0xFF000000)
val Light_Error = Color(0xFFB00020) // Standard Material Error Red
val Light_OnError = Color(0xFFFFFFFF)


// Dark Theme Colors (Adjusted for dark mode readability, using the palette)
val Dark_Primary = AppGreenLightest       // Lighter green for dark mode primary
val Dark_OnPrimary = Color(0xFF000000)     // Black text on primary
val Dark_PrimaryContainer = AppGreenDarker // Darker green for dark mode primary container
val Dark_OnPrimaryContainer = Color(0xFFFFFFFF) // White text on primary container

val Dark_Secondary = AppGreenLighter      // Mid-light green for dark mode secondary
val Dark_OnSecondary = Color(0xFF000000)   // Black text on secondary
val Dark_SecondaryContainer = AppGreenDarkest // Darkest green for dark mode secondary container
val Dark_OnSecondaryContainer = Color(0xFFFFFFFF) // White text on secondary container

val Dark_Tertiary = AppGreenMid           // Mid green for dark mode tertiary
val Dark_OnTertiary = Color(0xFF000000)    // Black text on tertiary
val Dark_TertiaryContainer = AppGreenDarkest // Reusing darkest green for dark mode tertiary container
val Dark_OnTertiaryContainer = Color(0xFFFFFFFF) // White text on tertiary container

// Standard system colors for dark theme
val Dark_Background = Color(0xFF1C1B1F) // Very dark grey
val Dark_OnBackground = Color(0xFFE6E1E5) // Light grey
val Dark_Surface = Color(0xFF1C1B1F)
val Dark_OnSurface = Color(0xFFE6E1E5)
val Dark_Error = Color(0xFFCF6679) // Dark theme error red
val Dark_OnError = Color(0xFF000000)
