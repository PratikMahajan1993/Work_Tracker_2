package com.example.worktracker

object AppRoutes {
    const val SIGN_IN_SCREEN = "sign_in_screen"
    const val MAIN_SCREEN = "main_screen"
    const val SELECT_CATEGORY = "select_category" // For LogWorkActivityScreen (category selection)
    // Route for WorkDetailsScreen, categoryName is part of the path, workLogId is an optional query parameter
    const val WORK_DETAILS = "work_details/{categoryName}?workLogId={workLogId}"
    const val LOG_PRODUCTION_ACTIVITY = "log_production_activity?productionLogId={productionLogId}"
    const val MANAGE_COMPONENTS = "manage_components"
    const val VIEW_COMPONENTS = "view_components"
    const val PREFERENCES_SCREEN = "preferences_screen" // Added PREFERENCES_SCREEN

    // Helper function to build the route to work details, supporting create and edit
    fun workDetailsRoute(categoryName: String, workLogId: Long? = null): String {
        val baseRoute = "work_details/$categoryName"
        return if (workLogId != null && workLogId > 0) {
            "$baseRoute?workLogId=$workLogId"
        } else {
            // For creating a new log, workLogId can be omitted or be 0/null
            // Ensure the query param key is there for NavHost definition if using nullable types with default
            "$baseRoute?workLogId=0L" // Default to 0L for new entries if NavHost expects the param
        }
    }

    // Helper function to build the route to log production activity, supporting create and edit
    fun logProductionActivityRoute(productionLogId: Long? = null): String {
        return if (productionLogId != null && productionLogId > 0) {
            "log_production_activity?productionLogId=$productionLogId"
        } else {
            // For creating a new log, productionLogId can be omitted or be 0/null
            "log_production_activity?productionLogId=0L" // Default to 0L for new entries
        }
    }

    // The old logWorkActivityRoute function might still be referenced.
    // Let's keep it but comment out the old const to avoid confusion if it's not immediately cleaned up.
    // const val LOG_WORK_ACTIVITY = "log_work_activity/{categoryName}" // This was problematic
    // Point old helper to new pattern, assuming it's for creating new logs primarily.
    fun logWorkActivityRoute(categoryName: String) = workDetailsRoute(categoryName, null)
}
