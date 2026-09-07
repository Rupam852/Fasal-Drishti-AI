package com.fasaldrishti.app.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Home : Screen("home")
    object Scan : Screen("scan")
    object Analyzing : Screen("analyzing/{imagePath}") {
        fun createRoute(imagePath: String) = "analyzing/${java.net.URLEncoder.encode(imagePath, "UTF-8")}"
    }
    object Result : Screen("result/{scanId}") {
        fun createRoute(scanId: String) = "result/$scanId"
    }
    object History : Screen("history")
    object Chat : Screen("chat?context={context}") {
        fun createRoute(context: String? = null) = if (context != null) "chat?context=${java.net.URLEncoder.encode(context, "UTF-8")}" else "chat"
    }
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object About : Screen("about")
    object Updater : Screen("updater")
    object CropLibrary : Screen("crop_library")
}
