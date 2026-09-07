package com.fasaldrishti.app.ui.screens.auth

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.GTranslate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fasaldrishti.app.ui.components.AppLogo
import com.fasaldrishti.app.ui.theme.EmeraldDark
import com.fasaldrishti.app.ui.theme.EmeraldPrimary
import com.fasaldrishti.app.ui.theme.ObsidianVoid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var inAppOAuthUrl by remember { mutableStateOf<String?>(null) }
    var inAppOAuthTitle by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianVoid)
    ) {
        // Glowing Ambient Light Beam Backdrop
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            EmeraldDark.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Top Branding Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 48.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppLogo(
                size = 92.dp,
                animated = true
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "फसल दृष्टि AI",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 30.sp,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Precision Agronomy & Crop Disease Vision",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = EmeraldPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            )
        }

        // Bottom Sheet Authentication Card
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 24.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 28.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome Back",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Sign in to access your crop scan history & AI prescriptions",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 13.5.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Google OAuth Button
                    AuthButton(
                        text = "Continue with Google",
                        icon = Icons.Default.GTranslate,
                        iconTint = Color(0xFF4285F4),
                        onClick = {
                            inAppOAuthTitle = "Google Sign In"
                            inAppOAuthUrl = viewModel.getOAuthUrl("google")
                        },
                        isLoading = uiState.isLoading
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // GitHub OAuth Button
                    AuthButton(
                        text = "Continue with GitHub",
                        icon = Icons.Default.Code,
                        iconTint = MaterialTheme.colorScheme.onSurface,
                        onClick = {
                            inAppOAuthTitle = "GitHub Authorization"
                            inAppOAuthUrl = viewModel.getOAuthUrl("github")
                        },
                        isLoading = uiState.isLoading
                    )

                    Spacer(modifier = Modifier.height(26.dp))

                    Text(
                        text = "By signing in, you agree to our Terms of Service & Privacy Policy",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // In-App OAuth Browser Dialog (Opens Google & GitHub sign-in inside the app)
        inAppOAuthUrl?.let { url ->
            Dialog(
                onDismissRequest = { inAppOAuthUrl = null },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                var isWebLoading by remember { mutableStateOf(true) }

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Top Navigation Header
                        TopAppBar(
                            title = {
                                Text(
                                    text = inAppOAuthTitle,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            },
                            actions = {
                                IconButton(onClick = { inAppOAuthUrl = null }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close")
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )

                        if (isWebLoading) {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = EmeraldPrimary
                            )
                        }

                        // Embedded High-Performance WebView
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { context ->
                                WebView(context).apply {
                                    @SuppressLint("SetJavaScriptEnabled")
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    settings.loadWithOverviewMode = true
                                    settings.useWideViewPort = true
                                    settings.databaseEnabled = true
                                    
                                    // Mobile Safari / Chrome modern User-Agent for flawless Google Account picker
                                    settings.userAgentString = "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36"

                                    CookieManager.getInstance().setAcceptCookie(true)
                                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                                    webViewClient = object : WebViewClient() {
                                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                            super.onPageStarted(view, url, favicon)
                                            isWebLoading = true
                                        }

                                        override fun onPageFinished(view: WebView?, url: String?) {
                                            super.onPageFinished(view, url)
                                            isWebLoading = false
                                        }

                                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                            val reqUrl = request?.url?.toString() ?: ""
                                            if (reqUrl.startsWith("fasaldrishti://auth")) {
                                                val uri = Uri.parse(reqUrl)
                                                viewModel.handleAuthCallback(uri, onSuccess = onLoginSuccess)
                                                inAppOAuthUrl = null
                                                return true
                                            }
                                            return false
                                        }
                                    }
                                    loadUrl(url)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    onClick: () -> Unit,
    isLoading: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(2.dp, RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .border(
                width = 1.2.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(28.dp)
            )
            .background(MaterialTheme.colorScheme.surface)
            .clickable(enabled = !isLoading) { onClick() }
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}
