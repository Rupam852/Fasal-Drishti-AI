package com.fasaldrishti.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fasaldrishti.app.data.remote.UpdateManager
import com.fasaldrishti.app.domain.repository.AuthRepository
import com.fasaldrishti.app.domain.repository.DiseaseRepository
import com.fasaldrishti.app.domain.repository.ScanRepository
import com.fasaldrishti.app.ui.components.FasalBottomBar
import com.fasaldrishti.app.ui.screens.about.AboutScreen
import com.fasaldrishti.app.ui.screens.analyzing.AnalyzingScreen
import com.fasaldrishti.app.ui.screens.auth.AuthViewModel
import com.fasaldrishti.app.ui.screens.auth.LoginScreen
import com.fasaldrishti.app.ui.screens.chat.ChatScreen
import com.fasaldrishti.app.ui.screens.chat.ChatViewModel
import com.fasaldrishti.app.ui.screens.history.HistoryScreen
import com.fasaldrishti.app.ui.screens.history.HistoryViewModel
import com.fasaldrishti.app.ui.screens.home.HomeScreen
import com.fasaldrishti.app.ui.screens.home.HomeViewModel
import com.fasaldrishti.app.ui.screens.library.CropLibraryScreen
import com.fasaldrishti.app.ui.screens.onboarding.OnboardingScreen
import com.fasaldrishti.app.ui.screens.profile.ProfileScreen
import com.fasaldrishti.app.ui.screens.result.ResultScreen
import com.fasaldrishti.app.ui.screens.result.ResultViewModel
import com.fasaldrishti.app.ui.screens.scan.ScanScreen
import com.fasaldrishti.app.ui.screens.scan.ScanViewModel
import com.fasaldrishti.app.ui.screens.settings.SettingsScreen
import com.fasaldrishti.app.ui.screens.splash.SplashScreen
import com.fasaldrishti.app.ui.screens.updater.UpdaterScreen

@Composable
fun AppNavHost(
    scanRepository: ScanRepository,
    authRepository: AuthRepository,
    diseaseRepository: DiseaseRepository,
    updateManager: UpdateManager,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Splash.route

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.History.route,
        Screen.Chat.route,
        Screen.Profile.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                FasalBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onScanClick = {
                        navController.navigate(Screen.Scan.route)
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onTimeout = {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onFinish = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Login.route) {
                val authViewModel = remember { AuthViewModel(authRepository) }
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                val homeViewModel = remember { HomeViewModel(scanRepository, authRepository) }
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToScan = { navController.navigate(Screen.Scan.route) },
                    onNavigateToResult = { scanId -> navController.navigate(Screen.Result.createRoute(scanId)) },
                    onNavigateToLibrary = { navController.navigate(Screen.CropLibrary.route) }
                )
            }

            composable(Screen.Scan.route) {
                val scanViewModel = remember { ScanViewModel(scanRepository) }
                ScanScreen(
                    viewModel = scanViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAnalyzing = { imagePath ->
                        navController.navigate(Screen.Analyzing.createRoute(imagePath))
                    },
                    onScanComplete = { scanRecord ->
                        navController.navigate(Screen.Result.createRoute(scanRecord.id)) {
                            popUpTo(Screen.Scan.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = Screen.Analyzing.route,
                arguments = listOf(navArgument("imagePath") { type = NavType.StringType })
            ) { backStackEntry ->
                val imagePath = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("imagePath") ?: "", "UTF-8")
                AnalyzingScreen(imagePath = imagePath)
            }

            composable(
                route = Screen.Result.route,
                arguments = listOf(navArgument("scanId") { type = NavType.StringType })
            ) { backStackEntry ->
                val scanId = backStackEntry.arguments?.getString("scanId") ?: ""
                val resultViewModel = remember(scanId) { ResultViewModel(scanRepository, diseaseRepository) }
                ResultScreen(
                    scanId = scanId,
                    viewModel = resultViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToChat = { contextMsg ->
                        navController.navigate(Screen.Chat.createRoute(contextMsg))
                    },
                    onScanAgain = {
                        navController.navigate(Screen.Scan.route) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                )
            }

            composable(Screen.History.route) {
                val historyViewModel = remember { HistoryViewModel(scanRepository) }
                HistoryScreen(
                    viewModel = historyViewModel,
                    onNavigateToResult = { scanId -> navController.navigate(Screen.Result.createRoute(scanId)) },
                    onNavigateToScan = { navController.navigate(Screen.Scan.route) }
                )
            }

            composable(
                route = Screen.Chat.route,
                arguments = listOf(navArgument("context") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val contextParam = backStackEntry.arguments?.getString("context")?.let {
                    java.net.URLDecoder.decode(it, "UTF-8")
                }
                val chatViewModel = remember(contextParam) { ChatViewModel(diseaseRepository) }
                ChatScreen(
                    contextInfo = contextParam,
                    viewModel = chatViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Profile.route) {
                val authViewModel = remember { AuthViewModel(authRepository) }
                ProfileScreen(
                    authViewModel = authViewModel,
                    updateManager = updateManager,
                    onNavigateToHistory = { navController.navigate(Screen.History.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToAbout = { navController.navigate(Screen.About.route) },
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    updateManager = updateManager,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAbout = { navController.navigate(Screen.About.route) },
                    onNavigateToUpdater = { navController.navigate(Screen.Updater.route) }
                )
            }

            composable(Screen.About.route) {
                AboutScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(Screen.Updater.route) {
                UpdaterScreen(
                    updateManager = updateManager,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.CropLibrary.route) {
                CropLibraryScreen(
                    diseaseRepository = diseaseRepository,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
