package com.fasaldrishti.app

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.fasaldrishti.app.ui.navigation.AppNavHost
import com.fasaldrishti.app.ui.theme.FasalDrishtiTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission result handled
    }

    private val pendingDestinationState = androidx.compose.runtime.mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Capture notification deep link destination if present
        pendingDestinationState.value = intent?.getStringExtra("navigate_to")

        // Request notification permission on Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Enable edge-to-edge layout for Android 13+
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Force maximum screen refresh rate (90Hz, 120Hz, 144Hz) for ultra-smooth 120 FPS animations
        enableMaxDisplayRefreshRate()

        val app = application as FasalDrishtiApp

        handleAuthDeepLink(intent, app)

        setContent {
            val isConnected by app.networkMonitor.isConnected.collectAsState()

            val themeMode by app.themeManager.themeMode.collectAsState()
            val currentLanguage by app.languageManager.currentLanguage.collectAsState()
            val isDarkTheme = when (themeMode) {
                com.fasaldrishti.app.data.local.ThemeMode.LIGHT -> false
                com.fasaldrishti.app.data.local.ThemeMode.DARK -> true
                com.fasaldrishti.app.data.local.ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            val pendingDestination by pendingDestinationState

            // Normalize system font scaling so large OS font settings do not break UI layouts
            val currentDensity = androidx.compose.ui.platform.LocalDensity.current
            val normalizedDensity = androidx.compose.ui.unit.Density(
                density = currentDensity.density,
                fontScale = 1.0f
            )

            FasalDrishtiTheme(darkTheme = isDarkTheme) {
                androidx.compose.runtime.CompositionLocalProvider(
                    androidx.compose.ui.platform.LocalDensity provides normalizedDensity,
                    com.fasaldrishti.app.ui.localization.LocalAppStrings provides com.fasaldrishti.app.ui.localization.getAppStrings(currentLanguage)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavHost(
                            scanRepository = app.scanRepository,
                            authRepository = app.authRepository,
                            diseaseRepository = app.diseaseRepository,
                            updateManager = app.updateManager,
                            weatherManager = app.weatherManager,
                            themeManager = app.themeManager,
                            languageManager = app.languageManager,
                            pendingDestination = pendingDestination,
                            onClearPendingDestination = { pendingDestinationState.value = null }
                        )

                        if (!isConnected) {
                            com.fasaldrishti.app.ui.components.NoInternetDialog(
                                onRetry = {
                                    app.networkMonitor.verifyConnection()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun attachBaseContext(newBase: android.content.Context) {
        val config = newBase.resources.configuration
        config.fontScale = 1.0f
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingDestinationState.value = intent.getStringExtra("navigate_to")
        val app = application as FasalDrishtiApp
        handleAuthDeepLink(intent, app)
    }

    private fun handleAuthDeepLink(intent: Intent?, app: FasalDrishtiApp) {
        val uri = intent?.data ?: return
        if (uri.scheme == "fasaldrishti" && uri.host == "auth") {
            lifecycleScope.launch {
                app.supabaseManager.handleAuthCallback(uri)
            }
        }
    }

    /**
     * Inspects device display modes and switches window to maximum available refresh rate (e.g. 120Hz / 144Hz)
     */
    private fun enableMaxDisplayRefreshRate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                window.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
                val display = display ?: return
                val supportedModes = display.supportedModes
                val maxRefreshMode = supportedModes.maxByOrNull { it.refreshRate }

                if (maxRefreshMode != null) {
                    val layoutParams = window.attributes
                    layoutParams.preferredDisplayModeId = maxRefreshMode.modeId
                    window.attributes = layoutParams
                }
            } catch (_: Exception) {
                // Ignore if display mode switching is not permitted by manufacturer skin
            }
        }
    }
}
