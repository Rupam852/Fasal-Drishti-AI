package com.fasaldrishti.app.ui.screens.analyzing

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fasaldrishti.app.domain.model.AiApiKeyException
import com.fasaldrishti.app.domain.model.ScanRecord
import com.fasaldrishti.app.domain.repository.ScanRepository
import com.fasaldrishti.app.ui.theme.CrimsonCoral
import com.fasaldrishti.app.ui.theme.EmeraldDark
import com.fasaldrishti.app.ui.theme.EmeraldPrimary
import com.fasaldrishti.app.ui.theme.ObsidianVoid
import com.fasaldrishti.app.ui.theme.SolarGold
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun AnalyzingScreen(
    imagePath: String,
    scanRepository: ScanRepository,
    onAnalysisComplete: (ScanRecord) -> Unit,
    onAnalysisError: (String) -> Unit,
    onNavigateToAiConfig: () -> Unit = {}
) {
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isApiKeyError by remember { mutableStateOf(false) }
    var isUnreachableError by remember { mutableStateOf(false) }
    var retryTrigger by remember { mutableIntStateOf(0) }
    var dotCount by remember { mutableIntStateOf(1) }

    // Cycle through dots: . -> .. -> ... -> .... -> .
    LaunchedEffect(errorMessage) {
        if (errorMessage == null) {
            while (true) {
                delay(350)
                dotCount = (dotCount % 4) + 1
            }
        }
    }

    // Execute scan asynchronously and require AI verification
    LaunchedEffect(imagePath, retryTrigger) {
        if (imagePath.isNotBlank()) {
            val file = File(imagePath)
            if (file.exists()) {
                errorMessage = null
                isApiKeyError = false
                isUnreachableError = false
                val result = scanRepository.performScan(file)
                result.onSuccess { record ->
                    delay(500) // Brief smooth transition delay
                    onAnalysisComplete(record)
                }.onFailure { err ->
                    if (err is AiApiKeyException) {
                        isApiKeyError = true
                        errorMessage = err.localizedMessage ?: "Aapki Custom AI Key expire ya galat ho sakti hai. Kripya Settings ➔ AI Engine me jakar nayi key dalein ya Default Engine chunein."
                    } else {
                        isUnreachableError = true
                        errorMessage = err.localizedMessage ?: "Unable to connect to AI engines due to network or quota limit. Please try again shortly."
                    }
                }
            } else {
                isUnreachableError = true
                errorMessage = "Image file not found. Please retake photo."
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianVoid)
    ) {
        // Blurred captured image backdrop
        if (imagePath.isNotBlank()) {
            AsyncImage(
                model = File(imagePath),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(25.dp)
                    .alpha(0.28f),
                contentScale = ContentScale.Crop
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (errorMessage != null) {
                // 🛑 HIGH-END ERROR MODAL CARD
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(24.dp, RoundedCornerShape(28.dp), spotColor = if (isApiKeyError) SolarGold.copy(alpha = 0.5f) else CrimsonCoral.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.5.dp, if (isApiKeyError) SolarGold.copy(alpha = 0.6f) else CrimsonCoral.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Error Icon Badge
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isApiKeyError) SolarGold.copy(alpha = 0.15f) else CrimsonCoral.copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isApiKeyError) Icons.Default.Key else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isApiKeyError) SolarGold else CrimsonCoral,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Error Title
                        Text(
                            text = if (isApiKeyError) "API Key Invalid / Expired" else "AI Agronomist is temporarily unreachable",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Error Message
                        Text(
                            text = if (isApiKeyError) {
                                "Aapki Custom AI Key expire ya galat ho sakti hai. Kripya Settings ➔ AI Engine me jakar nayi key dalein ya Default Engine chunein."
                            } else {
                                "Unable to connect to AI engines due to network or quota limit. Please try again shortly."
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Action Buttons
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // 1. Retry Analysis Button
                            Button(
                                onClick = { retryTrigger++ },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Retry Analysis",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }

                            // 2. Settings Button (if API key error)
                            if (isApiKeyError) {
                                OutlinedButton(
                                    onClick = onNavigateToAiConfig,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.5.dp, SolarGold.copy(alpha = 0.8f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = null,
                                        tint = SolarGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Open AI Settings",
                                        color = SolarGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }

                            // 3. Retake / Back Button
                            OutlinedButton(
                                onClick = { onAnalysisError(errorMessage ?: "Analysis cancelled") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Retake Photo",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            } else {
                // 1. Futuristic Dot Grid Wave Animation
                DotGridWaveAnimation(
                    modifier = Modifier.size(220.dp),
                    gridSize = 7,
                    dotColor = EmeraldPrimary
                )

                Spacer(modifier = Modifier.height(36.dp))

                // 2. Stationary "Analyzing..." text with fixed position and pulsing dots
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Analyzing",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = ".".repeat(dotCount),
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp
                        ),
                        modifier = Modifier.width(42.dp) // Fixed width so 'Analyzing' never jumps!
                    )
                }
            }
        }

        // Bottom Progress Bar (only during active scanning)
        if (errorMessage == null) {
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
}

@Composable
fun DotGridWaveAnimation(
    modifier: Modifier = Modifier,
    gridSize: Int = 7,
    dotColor: Color = EmeraldPrimary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "DotGridWave")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing)
        ),
        label = "WavePhase"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val cellWidth = width / (gridSize + 1)
        val cellHeight = height / (gridSize + 1)
        val centerX = width / 2f
        val centerY = height / 2f
        val maxDist = kotlin.math.hypot(centerX, centerY)

        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                val x = cellWidth * (col + 1)
                val y = cellHeight * (row + 1)
                val dist = kotlin.math.hypot(x - centerX, y - centerY)

                // 2D Wave radiating from center
                val wave = kotlin.math.sin((dist / maxDist) * 6.5f - wavePhase)
                val normalizedWave = ((wave + 1f) / 2f) // 0.0 to 1.0

                // Radius scales smoothly with wave
                val radius = (2.5f + normalizedWave * 4.2f).dp.toPx()
                // Alpha pulses with wave
                val alpha = 0.20f + normalizedWave * 0.80f

                // Peaked dot halo
                if (normalizedWave > 0.65f) {
                    drawCircle(
                        color = dotColor.copy(alpha = (normalizedWave - 0.65f) * 0.5f),
                        radius = radius * 1.9f,
                        center = androidx.compose.ui.geometry.Offset(x, y)
                    )
                }

                // Main Core Dot
                drawCircle(
                    color = dotColor.copy(alpha = alpha),
                    radius = radius,
                    center = androidx.compose.ui.geometry.Offset(x, y)
                )
            }
        }
    }
}
