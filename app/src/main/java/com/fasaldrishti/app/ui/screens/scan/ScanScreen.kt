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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fasaldrishti.app.domain.model.ScanRecord
import com.fasaldrishti.app.ui.theme.GreenPrimary
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

    // Auto-hide helper text after 3 seconds
    LaunchedEffect(Unit) {
        delay(3500)
        showHelperText = false
    }

    // Gallery Picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.processGalleryUri(context, it)
        }
    }

    // Handle scan state transitions
    LaunchedEffect(scanState) {
        when (val state = scanState) {
            is ScanState.Success -> {
                onScanComplete(state.scanRecord)
            }
            else -> {}
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

        // Guide Cutout Frame Overlay & Corner Reticles
        ScanFrameOverlay()

        // Shutter White Flash Effect
        if (showShutterFlash) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            )
        }

        // Top Overlay Bar (Back + Flash Toggle)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            IconButton(
                onClick = {
                    flashMode = if (flashMode == ImageCapture.FLASH_MODE_OFF) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = if (flashMode == ImageCapture.FLASH_MODE_ON) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "Toggle Flash",
                    tint = if (flashMode == ImageCapture.FLASH_MODE_ON) Color(0xFFFFD54F) else Color.White
                )
            }
        }

        // Helper Text Banner
        AnimatedVisibility(
            visible = showHelperText,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 100.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.65f)
            ) {
                Text(
                    text = "Leaf ko frame ke andar rakhein",
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        // Bottom Controls Bar (Gallery, Capture Button, Flip Camera)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 28.dp, vertical = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gallery Picker Button
            IconButton(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.55f))
                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = "Gallery",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Circular Capture Shutter Button
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(4.dp, Color.White, CircleShape)
                    .padding(6.dp)
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
                                viewModel.processCapturedImage(file)
                            },
                            onError = {
                                showShutterFlash = false
                                // Fallback demo file
                                val fallbackFile = File(context.cacheDir, "sample_scan.jpg")
                                onNavigateToAnalyzing(fallbackFile.absolutePath)
                                viewModel.processCapturedImage(fallbackFile)
                            }
                        )
                    }
            )

            // Flip Camera Lens Button
            IconButton(
                onClick = {
                    lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
                },
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f))
                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.FlipCameraAndroid,
                    contentDescription = "Flip Camera",
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
        initialValue = 0.6f,
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

        // Draw Corner Brackets (QR Scanner style)
        val bracketLength = 36.dp.toPx()
        val bracketStroke = 4.dp.toPx()
        val cornerRadius = 24.dp.toPx()
        val reticleColor = GreenPrimary.copy(alpha = pulseAlpha)

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
