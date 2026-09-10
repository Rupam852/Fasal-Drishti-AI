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
    val statusMessages = listOf(
        "🌿 Extracting Leaf Vein Topology...",
        "⚡ Running On-Device Neural Vision Model...",
        "🤖 AI Multimodal Pathologist Cross-Verification...",
        "🩺 Computing Precise Treatment & Spray Dosages..."
    )

    var currentMessageIndex by remember { mutableIntStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isApiKeyError by remember { mutableStateOf(false) }
    var isUnreachableError by remember { mutableStateOf(false) }
    var retryTrigger by remember { mutableIntStateOf(0) }

    // Cycle through telemetry steps while analyzing
    LaunchedEffect(errorMessage) {
        if (errorMessage == null) {
            while (true) {
                delay(1100)
                currentMessageIndex = (currentMessageIndex + 1) % statusMessages.size
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

    // Rotating Radar Sweep Angle
    val infiniteTransition = rememberInfiniteTransition(label = "RadarSweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing)
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
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f))
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
                            text = "HYBRID AI VISION ENGINE",
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
                        textAlign = TextAlign.Center
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
