package com.fasaldrishti.app.ui.screens.analyzing

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fasaldrishti.app.ui.theme.EmeraldDark
import com.fasaldrishti.app.ui.theme.EmeraldPrimary
import com.fasaldrishti.app.ui.theme.ObsidianVoid
import kotlinx.coroutines.delay

@Composable
fun AnalyzingScreen(
    imagePath: String
) {
    val statusMessages = listOf(
        "🌿 Extracting Leaf Vein Topology...",
        "⚡ Running On-Device MobileNetV2 Neural Graph...",
        "🔍 Cross-referencing 38 Plant Pathology Indices...",
        "🩺 Generating Agronomy Treatment Plan..."
    )

    var currentMessageIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1200)
            currentMessageIndex = (currentMessageIndex + 1) % statusMessages.size
        }
    }

    // Rotating Radar Sweep Angle
    val infiniteTransition = rememberInfiniteTransition(label = "RadarSweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing)
        ),
        label = "SweepAngle"
    )

    // Pulsing Rings Scale
    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "RippleScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianVoid)
    ) {
        // Blurred captured image backdrop
        if (imagePath.isNotBlank()) {
            AsyncImage(
                model = imagePath,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(25.dp)
                    .alpha(0.25f),
                contentScale = ContentScale.Crop
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Bio-Scanner Holographic Radar
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(rippleScale),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(200.dp)) {
                    // Outer Glow Rings
                    drawCircle(
                        color = EmeraldPrimary.copy(alpha = 0.15f),
                        radius = size.minDimension / 2,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawCircle(
                        color = EmeraldPrimary.copy(alpha = 0.25f),
                        radius = size.minDimension / 2.8f,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                    // Rotating Radar Sweep Line
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(Color.Transparent, EmeraldPrimary.copy(alpha = 0.7f), EmeraldPrimary)
                        ),
                        startAngle = sweepAngle,
                        sweepAngle = 90f,
                        useCenter = true
                    )
                }

                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                listOf(EmeraldPrimary.copy(alpha = 0.4f), Color.Transparent)
                            )
                        )
                        .border(2.dp, EmeraldPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(46.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = EmeraldDark.copy(alpha = 0.25f),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TENSORFLOW LITE NNAPI",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Dynamic Step Ticker
            AnimatedContent(
                targetState = statusMessages[currentMessageIndex],
                transitionSpec = { fadeIn(tween(350)) togetherWith fadeOut(tween(350)) },
                label = "AnalyzingStatus"
            ) { msg ->
                Text(
                    text = msg,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    ),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        // Bottom Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 40.dp, vertical = 32.dp)
        ) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(CircleShape),
                color = EmeraldPrimary,
                trackColor = Color.White.copy(alpha = 0.15f)
            )
        }
    }
}
