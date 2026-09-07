package com.fasaldrishti.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fasaldrishti.app.ui.theme.AmberAccent
import com.fasaldrishti.app.ui.theme.GreenPrimary
import com.fasaldrishti.app.ui.theme.RedSevere

@Composable
fun ConfidenceRing(
    confidence: Float,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    strokeWidth: Dp = 8.dp
) {
    var startAnim by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (startAnim) confidence.coerceIn(0f, 1f) else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "ConfidenceProgress"
    )

    LaunchedEffect(Unit) {
        startAnim = true
    }

    val color = when {
        confidence >= 0.80f -> GreenPrimary
        confidence >= 0.60f -> AmberAccent
        else -> RedSevere
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            // Background Track
            drawArc(
                color = color.copy(alpha = 0.2f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
            // Foreground Animated Progress
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }

        Text(
            text = "${(confidence * 100).toInt()}%",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}
