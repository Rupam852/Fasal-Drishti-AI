package com.fasaldrishti.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fasaldrishti.app.ui.theme.AmberAccent
import com.fasaldrishti.app.ui.theme.GreenPrimary
import com.fasaldrishti.app.ui.theme.PrimaryGradientEnd
import com.fasaldrishti.app.ui.theme.PrimaryGradientStart

/**
 * Premium Fasal Drishti App Logo Component.
 * Features a single golden-emerald rice stalk gracefully curving into an AI optical vision lens.
 * Automatically adapts its glow and tones according to dynamic Material You light/dark themes.
 */
@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    animated: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "LogoGlow")
    val pulseGlow by if (animated) {
        infiniteTransition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "PulseGlow"
        )
    } else {
        rememberUpdatedState(1f)
    }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Box(
        modifier = modifier
            .size(size)
            .shadow(8.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(
                brush = Brush.radialGradient(
                    colors = if (isDark) listOf(
                        Color(0xFF1E281E),
                        Color(0xFF0F160F)
                    ) else listOf(
                        Color(0xFFE8F5E9),
                        Color(0xFFC8E6C9)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.75f)) {
            val w = this.size.width
            val h = this.size.height

            // 1. Draw Curved Rice Stalk Stem
            val stemPath = Path().apply {
                moveTo(w * 0.22f, h * 0.78f)
                cubicTo(
                    w * 0.28f, h * 0.52f,
                    w * 0.44f, h * 0.32f,
                    w * 0.72f, h * 0.22f
                )
                cubicTo(
                    w * 0.80f, h * 0.20f,
                    w * 0.84f, h * 0.22f,
                    w * 0.82f, h * 0.26f
                )
                cubicTo(
                    w * 0.72f, h * 0.36f,
                    w * 0.52f, h * 0.58f,
                    w * 0.22f, h * 0.78f
                )
                close()
            }

            drawPath(
                path = stemPath,
                brush = Brush.linearGradient(
                    colors = listOf(GreenPrimary, AmberAccent),
                    start = Offset(w * 0.2f, h * 0.8f),
                    end = Offset(w * 0.8f, h * 0.2f)
                )
            )

            // 2. Draw Golden Rice Grains along stalk
            val grains = listOf(
                Offset(w * 0.76f, h * 0.22f) to (w * 0.08f),
                Offset(w * 0.68f, h * 0.27f) to (w * 0.09f),
                Offset(w * 0.60f, h * 0.34f) to (w * 0.09f),
                Offset(w * 0.52f, h * 0.42f) to (w * 0.09f),
                Offset(w * 0.44f, h * 0.50f) to (w * 0.08f)
            )

            grains.forEachIndexed { idx, (pos, grainLen) ->
                val grainPath = Path().apply {
                    moveTo(pos.x, pos.y)
                    quadraticBezierTo(pos.x + grainLen * 0.6f, pos.y - grainLen * 0.3f, pos.x + grainLen, pos.y)
                    quadraticBezierTo(pos.x + grainLen * 0.5f, pos.y + grainLen * 0.3f, pos.x, pos.y)
                    close()
                }
                drawPath(
                    path = grainPath,
                    color = Color(0xFFFFD54F)
                )
            }

            // 3. Draw AI Optical Vision Arc & Pupil (दृष्टि)
            val visionArc = Path().apply {
                moveTo(w * 0.38f, h * 0.66f)
                quadraticBezierTo(w * 0.58f, h * 0.80f, w * 0.78f, h * 0.64f)
                quadraticBezierTo(w * 0.58f, h * 0.72f, w * 0.38f, h * 0.66f)
                close()
            }
            drawPath(path = visionArc, color = PrimaryGradientEnd)

            // Center Optical Aperture Iris
            drawCircle(
                color = Color(0xFFFFC107).copy(alpha = pulseGlow),
                radius = w * 0.11f,
                center = Offset(w * 0.58f, h * 0.58f)
            )
            drawCircle(
                color = if (isDark) Color(0xFF0F160F) else Color(0xFF1B5E20),
                radius = w * 0.06f,
                center = Offset(w * 0.58f, h * 0.58f)
            )
        }
    }
}
