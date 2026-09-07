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
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.fasaldrishti.app.ui.navigation.AppNavHost
import com.fasaldrishti.app.ui.theme.FasalDrishtiTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge layout for Android 13+
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Force maximum screen refresh rate (90Hz, 120Hz, 144Hz) for ultra-smooth 120 FPS animations
        enableMaxDisplayRefreshRate()

        val app = application as FasalDrishtiApp

        handleAuthDeepLink(intent, app)

        setContent {
            FasalDrishtiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavHost(
                        scanRepository = app.scanRepository,
                        authRepository = app.authRepository,
                        diseaseRepository = app.diseaseRepository,
                        updateManager = app.updateManager,
                        weatherManager = app.weatherManager
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
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
