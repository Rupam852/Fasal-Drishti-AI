package com.fasaldrishti.app.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fasaldrishti.app.ui.components.AppLogo
import com.fasaldrishti.app.ui.theme.PrimaryGradientEnd
import com.fasaldrishti.app.ui.theme.PrimaryGradientStart
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.8f,
        animationSpec = tween(durationMillis = 700, easing = OvershootInterpolatorEase),
        label = "LogoScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "LogoAlpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(1800)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(PrimaryGradientStart, PrimaryGradientEnd)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scale)
                .alpha(alpha)
        ) {
            // Premium Single Rice Stalk + AI Optical Eye Logo
            AppLogo(
                size = 110.dp,
                animated = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Fasal Drishti",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp,
                    letterSpacing = 1.sp
                )
            )

            Text(
                text = "AI se apni fasal ki suraksha",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 15.sp
                )
            )
        }

        // Bottom 3 Pulsing Dots
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "Dots")
            (0..2).forEach { index ->
                val dotAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 600, delayMillis = index * 200),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "DotAlpha$index"
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .alpha(dotAlpha)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
    }
}

private val OvershootInterpolatorEase = Easing { x ->
    val tension = 2.0f
    val t = x - 1.0f
    t * t * ((tension + 1) * t + tension) + 1.0f
}
