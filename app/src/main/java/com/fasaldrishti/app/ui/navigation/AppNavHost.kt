package com.fasaldrishti.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import kotlinx.coroutines.launch
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
import com.fasaldrishti.app.ui.screens.tools.FertilizerCalculatorScreen
import com.fasaldrishti.app.ui.screens.tools.GovtSchemesScreen
import com.fasaldrishti.app.ui.screens.tools.MandiBhavScreen
import com.fasaldrishti.app.ui.screens.tools.SoilHealthScreen

import com.fasaldrishti.app.data.remote.WeatherManager

@Composable
fun AppNavHost(
    scanRepository: ScanRepository,
    authRepository: AuthRepository,
    diseaseRepository: DiseaseRepository,
    updateManager: UpdateManager,
    weatherManager: WeatherManager,
    themeManager: com.fasaldrishti.app.data.local.ThemeManager,
    languageManager: com.fasaldrishti.app.data.local.LanguageManager,
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
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                FasalBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        if (route != currentRoute) {
                            navController.navigate(route) {
                                popUpTo(Screen.Home.route)
                                launchSingleTop = true
                            }
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
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable(Screen.Splash.route) {
                val currentUser by authRepository.currentUser.collectAsState(initial = null)
                SplashScreen(
                    onTimeout = {
                        if (currentUser != null) {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.Onboarding.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
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
                val homeViewModel = remember { HomeViewModel(scanRepository, authRepository, weatherManager) }
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToScan = { navController.navigate(Screen.Scan.route) },
                    onNavigateToResult = { scanId -> navController.navigate(Screen.Result.createRoute(scanId)) },
                    onNavigateToLibrary = { navController.navigate(Screen.CropLibrary.route) },
                    onNavigateToMandi = { navController.navigate(Screen.MandiBhav.route) },
                    onNavigateToFertilizer = { navController.navigate(Screen.FertilizerCalculator.route) },
                    onNavigateToSchemes = { navController.navigate(Screen.GovtSchemes.route) },
                    onNavigateToSoilHealth = { navController.navigate(Screen.SoilHealth.route) }
                )
            }

            composable(Screen.Scan.route) {
                val scanViewModel = remember { ScanViewModel(scanRepository) }
                ScanScreen(
                    viewModel = scanViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAnalyzing = { imagePath ->
                        navController.navigate(Screen.Analyzing.createRoute(imagePath)) {
                            popUpTo(Screen.Scan.route) { inclusive = true }
                        }
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
                val rawPath = backStackEntry.arguments?.getString("imagePath") ?: ""
                val imagePath = try {
                    java.net.URLDecoder.decode(rawPath, "UTF-8")
                } catch (e: Exception) {
                    rawPath
                }
                AnalyzingScreen(
                    imagePath = imagePath,
                    scanRepository = scanRepository,
                    onAnalysisComplete = { scanRecord ->
                        navController.navigate(Screen.Result.createRoute(scanRecord.id)) {
                            popUpTo(Screen.Analyzing.route) { inclusive = true }
                        }
                    },
                    onAnalysisError = {
                        navController.popBackStack()
                    }
                )
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
                val context = androidx.compose.ui.platform.LocalContext.current
                val localChatManager = remember { com.fasaldrishti.app.data.local.LocalChatManager(context) }
                val rawContext = backStackEntry.arguments?.getString("context")
                val contextParam = rawContext?.let {
                    try {
                        if (it.contains("%") && !it.contains("% ")) {
                            java.net.URLDecoder.decode(it, "UTF-8")
                        } else {
                            it
                        }
                    } catch (e: Exception) {
                        it
                    }
                }
                val chatViewModel = remember(contextParam) { ChatViewModel(diseaseRepository, localChatManager) }
                ChatScreen(
                    contextInfo = contextParam,
                    viewModel = chatViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Profile.route) {
                val context = androidx.compose.ui.platform.LocalContext.current
                val localChatManager = remember { com.fasaldrishti.app.data.local.LocalChatManager(context) }
                val authViewModel = remember { AuthViewModel(authRepository) }
                val scans by scanRepository.getAllScans().collectAsState(initial = emptyList())
                val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
                ProfileScreen(
                    authViewModel = authViewModel,
                    updateManager = updateManager,
                    scans = scans,
                    onNavigateToHistory = {
                        navController.navigate(Screen.History.route) {
                            popUpTo(Screen.Home.route)
                            launchSingleTop = true
                        }
                    },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToAbout = { navController.navigate(Screen.About.route) },
                    onClearAllScans = {
                        coroutineScope.launch {
                            scanRepository.clearAllScans()
                        }
                    },
                    onClearChatHistory = {
                        localChatManager.clearChat()
                    },
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
                    themeManager = themeManager,
                    languageManager = languageManager,
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
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToChat = { contextMsg ->
                        navController.navigate(Screen.Chat.createRoute(contextMsg))
                    }
                )
            }

            composable(Screen.MandiBhav.route) {
                MandiBhavScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.FertilizerCalculator.route) {
                FertilizerCalculatorScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.GovtSchemes.route) {
                GovtSchemesScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.SoilHealth.route) {
                SoilHealthScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToChat = { cropName ->
                        navController.navigate(Screen.Chat.createRoute("I want to know complete farming guide and fertilizer schedule for $cropName in my soil type."))
                    }
                )
            }
        }
    }
}
