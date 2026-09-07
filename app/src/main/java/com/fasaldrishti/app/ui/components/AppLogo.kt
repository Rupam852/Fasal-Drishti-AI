package com.fasaldrishti.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fasaldrishti.app.R
import com.fasaldrishti.app.ui.theme.EmeraldDark
import com.fasaldrishti.app.ui.theme.EmeraldPrimary

/**
 * Official Real Fasal Drishti App Logo Component.
 * Displays the authentic high-resolution crop vision logo with animated glow and dynamic elevation.
 */
@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    animated: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "LogoGlow")
    val pulseScale by if (animated) {
        infiniteTransition.animateFloat(
            initialValue = 0.96f,
            targetValue = 1.04f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "PulseScale"
        )
    } else {
        rememberUpdatedState(1f)
    }

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                if (animated) {
                    scaleX = pulseScale
                    scaleY = pulseScale
                }
            }
            .shadow(12.dp, RoundedCornerShape(24.dp), spotColor = EmeraldPrimary.copy(alpha = 0.45f))
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF142017),
                        Color(0xFF090E0A)
                    )
                )
            )
            .border(
                1.5.dp,
                Brush.linearGradient(listOf(EmeraldPrimary, Color.White.copy(alpha = 0.6f), EmeraldDark)),
                RoundedCornerShape(24.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = "Fasal Drishti Real Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(size * 0.88f)
                .clip(RoundedCornerShape(18.dp))
        )
    }
}
