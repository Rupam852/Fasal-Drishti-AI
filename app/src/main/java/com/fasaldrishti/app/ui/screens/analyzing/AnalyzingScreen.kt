package com.fasaldrishti.app.ui.screens.analyzing

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fasaldrishti.app.ui.theme.PrimaryGradientEnd
import com.fasaldrishti.app.ui.theme.PrimaryGradientStart
import kotlinx.coroutines.delay

@Composable
fun AnalyzingScreen(
    imagePath: String
) {
    val statusMessages = listOf(
        "Photo analyze ho rahi hai...",
        "Disease pattern check kar rahe hain...",
        "MobileNet neural layers evaluating...",
        "Confidence calculate kar rahe hain..."
    )

    var currentMessageIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1500)
            currentMessageIndex = (currentMessageIndex + 1) % statusMessages.size
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "LaserScan")
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LaserY"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Background blurred preview of captured crop leaf
        AsyncImage(
            model = imagePath,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(20.dp)
                .alpha(0.35f),
            contentScale = ContentScale.Crop
        )

        // Center Scanning Reticle & Laser line
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(PrimaryGradientEnd.copy(alpha = 0.4f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(80.dp)
                )

                // Laser scan line overlay
                Canvas(modifier = Modifier.size(160.dp)) {
                    val y = size.height * laserY
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, Color(0xFF69F0AE), Color.Transparent)
                        ),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 4.dp.toPx()
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Rotating status text with animated crossfade
            AnimatedContent(
                targetState = statusMessages[currentMessageIndex],
                transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(400)) },
                label = "StatusText"
            ) { msg ->
                Text(
                    text = msg,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp
                    )
                )
            }
        }

        // Indeterminate gradient progress bar at bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 40.dp, vertical = 40.dp)
        ) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = Color(0xFF69F0AE),
                trackColor = Color.White.copy(alpha = 0.2f)
            )
        }
    }
}
