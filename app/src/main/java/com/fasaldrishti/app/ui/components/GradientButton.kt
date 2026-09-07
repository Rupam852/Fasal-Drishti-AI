package com.fasaldrishti.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fasaldrishti.app.ui.theme.PrimaryGradientEnd
import com.fasaldrishti.app.ui.theme.PrimaryGradientStart

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    gradientColors: List<Color> = listOf(PrimaryGradientStart, PrimaryGradientEnd),
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "ButtonScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(if (enabled) 6.dp else 0.dp, RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .background(
                brush = if (enabled) Brush.horizontalGradient(gradientColors)
                else Brush.horizontalGradient(listOf(Color.Gray.copy(alpha = 0.5f), Color.Gray.copy(alpha = 0.5f)))
            )
            .pointerInput(enabled) {
                if (!enabled || isLoading) return@pointerInput
                while (true) {
                    awaitPointerEventScope {
                        awaitFirstDown(requireUnconsumed = false)
                        isPressed = true
                        waitForUpOrCancellation()
                        isPressed = false
                    }
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled && !isLoading
            ) {
                onClick()
            }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.White,
                strokeWidth = 2.5.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    androidx.compose.material3.Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 8.dp)
                    )
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontSize = 16.sp
                    )
                )
            }
        }
    }
}
