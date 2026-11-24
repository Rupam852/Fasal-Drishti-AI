package com.fasaldrishti.app.ui.screens.settings

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fasaldrishti.app.data.local.ScanSettingsManager
import com.fasaldrishti.app.ui.localization.LocalAppStrings
import com.fasaldrishti.app.ui.theme.EmeraldPrimary
import com.fasaldrishti.app.ui.theme.SolarGold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class ModelDiagnosticState {
    object Idle : ModelDiagnosticState()
    data class Success(val latencyMs: Long, val detectedClass: String, val confidence: Float) : ModelDiagnosticState()
    data class Failure(val errorMessage: String) : ModelDiagnosticState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanSettingsScreen(
    scanSettingsManager: ScanSettingsManager,
    onNavigateBack: () -> Unit,
    onNavigateToOfflineModelInfo: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isTestingModel by remember { mutableStateOf(false) }
    var diagnosticState by remember { mutableStateOf<ModelDiagnosticState>(ModelDiagnosticState.Idle) }

    fun runModelDiagnostic() {
        if (isTestingModel) return
        isTestingModel = true
        coroutineScope.launch {
            try {
                val result = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                    val classifier = com.fasaldrishti.app.data.ml.TFLiteDiseaseClassifier(context)
                    val testBitmap = android.graphics.Bitmap.createBitmap(224, 224, android.graphics.Bitmap.Config.ARGB_8888)
                    val canvas = android.graphics.Canvas(testBitmap)
                    val paint = android.graphics.Paint()
                    paint.color = android.graphics.Color.rgb(45, 135, 55)
                    canvas.drawRect(0f, 0f, 224f, 224f, paint)
                    paint.color = android.graphics.Color.rgb(85, 210, 75)
                    paint.strokeWidth = 4f
                    canvas.drawLine(112f, 10f, 112f, 214f, paint)
                    for (i in 20..200 step 20) {
                        canvas.drawLine(112f, i.toFloat(), (112 + i / 3).toFloat(), (i - 10).toFloat(), paint)
                        canvas.drawLine(112f, i.toFloat(), (112 - i / 3).toFloat(), (i - 10).toFloat(), paint)
                    }

                    val startTime = System.currentTimeMillis()
                    val prediction = classifier.classifyBitmap(testBitmap)
                    val latency = System.currentTimeMillis() - startTime
                    classifier.close()

                    if (prediction.predictedClass == "Invalid_Crop" && prediction.confidence == 0.0f) {
                        throw Exception("Model failed to initialize or load weights properly on this hardware.")
                    }
                    Triple(latency, prediction.predictedClass, prediction.confidence)
                }
                diagnosticState = ModelDiagnosticState.Success(
                    latencyMs = result.first,
                    detectedClass = result.second,
                    confidence = result.third
                )
            } catch (e: Throwable) {
                diagnosticState = ModelDiagnosticState.Failure(
                    errorMessage = e.message ?: "Failed to execute on-device inference test."
                )
            } finally {
                isTestingModel = false
            }
        }
    }

    val strings = LocalAppStrings.current
    val isOfflineEnabled by scanSettingsManager.isOfflineModelEnabled.collectAsState()
    val isOnlineEnabled by scanSettingsManager.isOnlineVerificationEnabled.collectAsState()

    // Mutual-lock calculations: At least one must always remain enabled
    val isOfflineLocked = !isOnlineEnabled
    val isOnlineLocked = !isOfflineEnabled

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.scanSettingsTitle,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header Explanation Card
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = EmeraldPrimary.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = strings.scanSettingsSubtitle,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 20.sp
                            )
                        )
                    }
                }
            }

            // Card 1: Offline Model (TFLite)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(
                        1.dp,
                        if (isOfflineEnabled) EmeraldPrimary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isOfflineEnabled) EmeraldPrimary.copy(alpha = 0.15f)
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OfflineBolt,
                                        contentDescription = null,
                                        tint = if (isOfflineEnabled) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Left question mark icon in a simple circle
                                        Surface(
                                            onClick = onNavigateToOfflineModelInfo,
                                            shape = CircleShape,
                                            color = EmeraldPrimary.copy(alpha = 0.12f),
                                            border = BorderStroke(1.2.dp, EmeraldPrimary.copy(alpha = 0.45f)),
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "?",
                                                    color = EmeraldPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = strings.offlineModelToggleTitle,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            ),
                                            modifier = Modifier.clickable { onNavigateToOfflineModelInfo() }
                                        )
                                        if (isOfflineLocked) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Locked",
                                                tint = SolarGold,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = strings.offlineModelToggleSubtitle,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "ℹ️ View Datasets & 43 Recognized Crops ➔",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = EmeraldPrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.5.sp
                                        ),
                                        modifier = Modifier.clickable { onNavigateToOfflineModelInfo() }
                                    )
                                }
                            }

                            Switch(
                                checked = isOfflineEnabled,
                                onCheckedChange = { newValue ->
                                    scanSettingsManager.setOfflineModelEnabled(newValue)
                                },
                                enabled = !isOfflineLocked,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = EmeraldPrimary,
                                    disabledCheckedTrackColor = EmeraldPrimary.copy(alpha = 0.4f),
                                    disabledCheckedThumbColor = Color.White.copy(alpha = 0.8f)
                                )
                            )
                        }

                        // Locked explanation hint
                        AnimatedVisibility(visible = isOfflineLocked) {
                            Column {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SolarGold.copy(alpha = 0.12f),
                                    border = BorderStroke(0.8.dp, SolarGold.copy(alpha = 0.35f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = SolarGold,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = strings.scanSettingsLockHint,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Connected Check Your Offline Model Section
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left Test Button
                            Button(
                                onClick = { runModelDiagnostic() },
                                enabled = !isTestingModel,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldPrimary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                if (isTestingModel) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Testing...", fontSize = 12.sp)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Test", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Check Your Offline Model",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp
                                    )
                                )
                                Text(
                                    text = "Runs on-device AI test on this phone's hardware",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Card 2: Online 2nd-Tier Verification
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(
                        1.dp,
                        if (isOnlineEnabled) EmeraldPrimary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isOnlineEnabled) EmeraldPrimary.copy(alpha = 0.15f)
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudSync,
                                        contentDescription = null,
                                        tint = if (isOnlineEnabled) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = strings.onlineVerificationToggleTitle,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                        )
                                        if (isOnlineLocked) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Locked",
                                                tint = SolarGold,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = strings.onlineVerificationToggleSubtitle,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }

                            Switch(
                                checked = isOnlineEnabled,
                                onCheckedChange = { newValue ->
                                    scanSettingsManager.setOnlineVerificationEnabled(newValue)
                                },
                                enabled = !isOnlineLocked,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = EmeraldPrimary,
                                    disabledCheckedTrackColor = EmeraldPrimary.copy(alpha = 0.4f),
                                    disabledCheckedThumbColor = Color.White.copy(alpha = 0.8f)
                                )
                            )
                        }

                        // Locked explanation hint
                        AnimatedVisibility(visible = isOnlineLocked) {
                            Column {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SolarGold.copy(alpha = 0.12f),
                                    border = BorderStroke(0.8.dp, SolarGold.copy(alpha = 0.35f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = SolarGold,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = strings.scanSettingsLockHint,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Success Diagnostic Dialog
    val successState = diagnosticState as? ModelDiagnosticState.Success
    if (successState != null) {
        AlertDialog(
            onDismissRequest = { diagnosticState = ModelDiagnosticState.Idle },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(EmeraldPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Offline Model Operational ✅",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Your on-device AI model is working properly on this smartphone hardware!",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("• Status: 100% Operational & Ready", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = EmeraldPrimary)
                            Text("• Inference Speed: ${successState.latencyMs} ms", fontSize = 12.sp)
                            Text("• Neural Architecture: MobileNetV2 (Float32)", fontSize = 12.sp)
                            Text("• Active Classes: 43 Agricultural Categories", fontSize = 12.sp)
                            Text("• Internet Dependency: Zero (100% Offline)", fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { diagnosticState = ModelDiagnosticState.Idle },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Close")
                }
            }
        )
    }

    // Failure Diagnostic Dialog
    val failureState = diagnosticState as? ModelDiagnosticState.Failure
    if (failureState != null) {
        AlertDialog(
            onDismissRequest = { diagnosticState = ModelDiagnosticState.Idle },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(30.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Model Diagnostic Failed ⚠️",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "The on-device offline model encountered an error during hardware testing.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = failureState.errorMessage,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { diagnosticState = ModelDiagnosticState.Idle },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Close")
                }
            }
        )
    }
}
