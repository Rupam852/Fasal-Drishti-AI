package com.fasaldrishti.app.ui.screens.scan

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fasaldrishti.app.domain.model.ScanRecord
import com.fasaldrishti.app.ui.theme.EmeraldPrimary
import com.fasaldrishti.app.ui.theme.SolarGold
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun ScanScreen(
    viewModel: ScanViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAnalyzing: (String) -> Unit,
    onScanComplete: (ScanRecord) -> Unit
) {
    val context = LocalContext.current
    val scanState by viewModel.scanState.collectAsState()

    var flashMode by remember { mutableStateOf(ImageCapture.FLASH_MODE_OFF) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    val imageCapture = remember { ImageCapture.Builder().build() }

    var showShutterFlash by remember { mutableStateOf(false) }
    var showHelperText by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(3500)
        showHelperText = false
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val tempFile = File.createTempFile("gallery_crop_", ".jpg", context.cacheDir)
                val outputStream = java.io.FileOutputStream(tempFile)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                onNavigateToAnalyzing(tempFile.absolutePath)
            } catch (e: Exception) {
                // Ignore load error
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Fullscreen Camera Preview
        CameraPreviewView(
            modifier = Modifier.fillMaxSize(),
            imageCapture = imageCapture,
            lensFacing = lensFacing,
            flashMode = flashMode
        )

        // Holographic Cyber Reticle Overlay
        ScanFrameOverlay()

        // Shutter White Flash
        if (showShutterFlash) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            )
        }

        // Top Floating Glass Action Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            shape = RoundedCornerShape(26.dp),
            color = Color.Black.copy(alpha = 0.5f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI OPTICAL SENSOR",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }

                IconButton(
                    onClick = {
                        flashMode = if (flashMode == ImageCapture.FLASH_MODE_OFF) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
                    },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = if (flashMode == ImageCapture.FLASH_MODE_ON) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Flash",
                        tint = if (flashMode == ImageCapture.FLASH_MODE_ON) SolarGold else Color.White
                    )
                }
            }
        }

        // Guide Pill
        AnimatedVisibility(
            visible = showHelperText,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 90.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = EmeraldPrimary.copy(alpha = 0.92f),
                shadowElevation = 8.dp
            ) {
                Text(
                    text = "🌿 Align diseased leaf inside the cyber frame",
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        // Bottom Controls Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gallery Picker
            IconButton(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier
                    .size(54.dp)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .border(1.2.dp, Color.White.copy(alpha = 0.35f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = "Gallery",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // High-Tech Shutter Multi-Ring Trigger
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .shadow(16.dp, CircleShape, spotColor = EmeraldPrimary)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            listOf(EmeraldPrimary.copy(alpha = 0.3f), Color.Transparent)
                        )
                    )
                    .border(3.dp, Color.White, CircleShape)
                    .padding(8.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable {
                        showShutterFlash = true
                        takePhoto(
                            context = context,
                            imageCapture = imageCapture,
                            onImageCaptured = { file ->
                                showShutterFlash = false
                                onNavigateToAnalyzing(file.absolutePath)
                            },
                            onError = {
                                showShutterFlash = false
                                val fallbackFile = File(context.cacheDir, "sample_scan.jpg")
                                onNavigateToAnalyzing(fallbackFile.absolutePath)
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(EmeraldPrimary)
                )
            }

            // Flip Camera Lens
            IconButton(
                onClick = {
                    lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
                },
                modifier = Modifier
                    .size(54.dp)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .border(1.2.dp, Color.White.copy(alpha = 0.35f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.FlipCameraAndroid,
                    contentDescription = "Flip Lens",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ScanFrameOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "ReticlePulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val frameWidth = size.width * 0.82f
        val frameHeight = size.height * 0.50f
        val left = (size.width - frameWidth) / 2f
        val top = (size.height - frameHeight) / 2.3f

        val bracketLength = 40.dp.toPx()
        val bracketStroke = 3.5.dp.toPx()
        val cornerRadius = 24.dp.toPx()
        val reticleColor = EmeraldPrimary.copy(alpha = pulseAlpha)

        // Corner Target Brackets
        // Top Left
        drawLine(reticleColor, Offset(left, top + cornerRadius), Offset(left, top + bracketLength), bracketStroke)
        drawLine(reticleColor, Offset(left + cornerRadius, top), Offset(left + bracketLength, top), bracketStroke)

        // Top Right
        drawLine(reticleColor, Offset(left + frameWidth, top + cornerRadius), Offset(left + frameWidth, top + bracketLength), bracketStroke)
        drawLine(reticleColor, Offset(left + frameWidth - cornerRadius, top), Offset(left + frameWidth - bracketLength, top), bracketStroke)

        // Bottom Left
        drawLine(reticleColor, Offset(left, top + frameHeight - cornerRadius), Offset(left, top + frameHeight - bracketLength), bracketStroke)
        drawLine(reticleColor, Offset(left + cornerRadius, top + frameHeight), Offset(left + bracketLength, top + frameHeight), bracketStroke)

        // Bottom Right
        drawLine(reticleColor, Offset(left + frameWidth, top + frameHeight - cornerRadius), Offset(left + frameWidth, top + frameHeight - bracketLength), bracketStroke)
        drawLine(reticleColor, Offset(left + frameWidth - cornerRadius, top + frameHeight), Offset(left + frameWidth - bracketLength, top + frameHeight), bracketStroke)
    }
}
