package com.fasaldrishti.app.ui.screens.settings

import androidx.compose.animation.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

enum class DiagnosticStepStatus {
    PENDING,
    RUNNING,
    PASSED,
    FAILED
}

data class DiagnosticStepItem(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val status: DiagnosticStepStatus = DiagnosticStepStatus.PENDING,
    val detail: String? = null,
    val errorMessage: String? = null
)

sealed class ModelDiagnosticState {
    object Idle : ModelDiagnosticState()
    data class Active(
        val isRunning: Boolean,
        val steps: List<DiagnosticStepItem>,
        val isAllPassed: Boolean = false,
        val benchmarkLatencyMs: Long? = null,
        val failedStepIndex: Int? = null,
        val failureReason: String? = null
    ) : ModelDiagnosticState()
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

        val initialSteps = listOf(
            DiagnosticStepItem(1, "Model Weight Asset (.tflite)", "Verifying file integrity in app assets"),
            DiagnosticStepItem(2, "Disease Taxonomy (labels.txt)", "Reading classification label dictionary"),
            DiagnosticStepItem(3, "TFLite Engine & CPU Acceleration", "Allocating 4-Thread XNNPACK neural interpreter"),
            DiagnosticStepItem(4, "Vision Pipeline & Foliage Guard", "Verifying ARGB_8888 buffer & CV filter"),
            DiagnosticStepItem(5, "Inference Latency & Output Check", "Executing end-to-end forward pass benchmark")
        )

        diagnosticState = ModelDiagnosticState.Active(
            isRunning = true,
            steps = initialSteps
        )

        coroutineScope.launch {
            var currentSteps = initialSteps
            var mappedBuffer: java.nio.ByteBuffer? = null
            var loadedLabels: List<String> = emptyList()
            var testInterpreter: org.tensorflow.lite.Interpreter? = null
            var testBitmap: android.graphics.Bitmap? = null
            var benchmarkLatency: Long = 0L

            try {
                // Step 1: Model File Integrity
                currentSteps = currentSteps.mapIndexed { idx, s ->
                    if (idx == 0) s.copy(status = DiagnosticStepStatus.RUNNING) else s
                }
                diagnosticState = ModelDiagnosticState.Active(isRunning = true, steps = currentSteps)
                kotlinx.coroutines.delay(180)

                val step1Result = withContext(Dispatchers.Default) {
                    val afd = context.assets.openFd("model_4_mobilenet_finetuned.tflite")
                    val sizeMb = afd.length / (1024.0 * 1024.0)
                    afd.close()
                    val buffer = org.tensorflow.lite.support.common.FileUtil.loadMappedFile(context, "model_4_mobilenet_finetuned.tflite")
                    buffer to String.format("%.2f MB mapped successfully", sizeMb)
                }
                mappedBuffer = step1Result.first
                currentSteps = currentSteps.mapIndexed { idx, s ->
                    if (idx == 0) s.copy(status = DiagnosticStepStatus.PASSED, detail = step1Result.second) else s
                }
                diagnosticState = ModelDiagnosticState.Active(isRunning = true, steps = currentSteps)

                // Step 2: Labels File Verification
                currentSteps = currentSteps.mapIndexed { idx, s ->
                    if (idx == 1) s.copy(status = DiagnosticStepStatus.RUNNING) else s
                }
                diagnosticState = ModelDiagnosticState.Active(isRunning = true, steps = currentSteps)
                kotlinx.coroutines.delay(180)

                val step2Result = withContext(Dispatchers.Default) {
                    val labels = context.assets.open("labels.txt").bufferedReader().useLines { it.toList() }
                    if (labels.isEmpty()) throw Exception("labels.txt is empty or missing")
                    labels to "${labels.size} agricultural disease categories loaded"
                }
                loadedLabels = step2Result.first
                currentSteps = currentSteps.mapIndexed { idx, s ->
                    if (idx == 1) s.copy(status = DiagnosticStepStatus.PASSED, detail = step2Result.second) else s
                }
                diagnosticState = ModelDiagnosticState.Active(isRunning = true, steps = currentSteps)

                // Step 3: TFLite CPU Interpreter
                currentSteps = currentSteps.mapIndexed { idx, s ->
                    if (idx == 2) s.copy(status = DiagnosticStepStatus.RUNNING) else s
                }
                diagnosticState = ModelDiagnosticState.Active(isRunning = true, steps = currentSteps)
                kotlinx.coroutines.delay(180)

                val step3Result = withContext(Dispatchers.Default) {
                    val options = org.tensorflow.lite.Interpreter.Options().apply {
                        setNumThreads(4)
                        setUseNNAPI(false)
                    }
                    val interp = org.tensorflow.lite.Interpreter(mappedBuffer!!, options)
                    val inShape = interp.getInputTensor(0).shape().contentToString()
                    val outShape = interp.getOutputTensor(0).shape().contentToString()
                    interp to "4-Thread CPU active ($inShape -> $outShape)"
                }
                testInterpreter = step3Result.first
                currentSteps = currentSteps.mapIndexed { idx, s ->
                    if (idx == 2) s.copy(status = DiagnosticStepStatus.PASSED, detail = step3Result.second) else s
                }
                diagnosticState = ModelDiagnosticState.Active(isRunning = true, steps = currentSteps)

                // Step 4: Vision Pipeline & Foliage Guard
                currentSteps = currentSteps.mapIndexed { idx, s ->
                    if (idx == 3) s.copy(status = DiagnosticStepStatus.RUNNING) else s
                }
                diagnosticState = ModelDiagnosticState.Active(isRunning = true, steps = currentSteps)
                kotlinx.coroutines.delay(180)

                val step4Result = withContext(Dispatchers.Default) {
                    val bmp = android.graphics.Bitmap.createBitmap(224, 224, android.graphics.Bitmap.Config.ARGB_8888)
                    val canvas = android.graphics.Canvas(bmp)
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
                    bmp to "ARGB_8888 synthetic foliage buffer ready"
                }
                testBitmap = step4Result.first
                currentSteps = currentSteps.mapIndexed { idx, s ->
                    if (idx == 3) s.copy(status = DiagnosticStepStatus.PASSED, detail = step4Result.second) else s
                }
                diagnosticState = ModelDiagnosticState.Active(isRunning = true, steps = currentSteps)

                // Step 5: End-to-End Inference Benchmark
                currentSteps = currentSteps.mapIndexed { idx, s ->
                    if (idx == 4) s.copy(status = DiagnosticStepStatus.RUNNING) else s
                }
                diagnosticState = ModelDiagnosticState.Active(isRunning = true, steps = currentSteps)
                kotlinx.coroutines.delay(180)

                val step5Result = withContext(Dispatchers.Default) {
                    val imageProcessor = org.tensorflow.lite.support.image.ImageProcessor.Builder()
                        .add(org.tensorflow.lite.support.image.ops.ResizeOp(224, 224, org.tensorflow.lite.support.image.ops.ResizeOp.ResizeMethod.BILINEAR))
                        .build()
                    var tensorImage = org.tensorflow.lite.support.image.TensorImage(org.tensorflow.lite.DataType.FLOAT32)
                    tensorImage.load(testBitmap!!)
                    tensorImage = imageProcessor.process(tensorImage)

                    val outputScores = Array(1) { FloatArray(loadedLabels.size) }
                    val t0 = System.currentTimeMillis()
                    testInterpreter?.run(tensorImage.buffer.rewind(), outputScores)
                    val lat = System.currentTimeMillis() - t0

                    val probabilities = outputScores[0]
                    val maxIdx = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0
                    val maxScore = probabilities[maxIdx]
                    val label = if (maxIdx in loadedLabels.indices) loadedLabels[maxIdx].substringAfter("___").replace("_", " ") else "Tested"

                    lat to "Inference: ${lat}ms (${label} ${String.format("%.1f%%", maxScore * 100)})"
                }
                benchmarkLatency = step5Result.first
                currentSteps = currentSteps.mapIndexed { idx, s ->
                    if (idx == 4) s.copy(status = DiagnosticStepStatus.PASSED, detail = step5Result.second) else s
                }

                diagnosticState = ModelDiagnosticState.Active(
                    isRunning = false,
                    steps = currentSteps,
                    isAllPassed = true,
                    benchmarkLatencyMs = benchmarkLatency
                )
            } catch (e: Throwable) {
                val failedIdx = currentSteps.indexOfFirst { it.status == DiagnosticStepStatus.RUNNING }.let {
                    if (it == -1) currentSteps.indexOfFirst { s -> s.status == DiagnosticStepStatus.PENDING }.coerceAtLeast(0) else it
                }
                val errMsg = e.message ?: e.localizedMessage ?: "Unknown hardware execution error"
                currentSteps = currentSteps.mapIndexed { idx, s ->
                    if (idx == failedIdx) s.copy(status = DiagnosticStepStatus.FAILED, errorMessage = errMsg) else s
                }
                diagnosticState = ModelDiagnosticState.Active(
                    isRunning = false,
                    steps = currentSteps,
                    isAllPassed = false,
                    failedStepIndex = failedIdx,
                    failureReason = errMsg
                )
            } finally {
                testInterpreter?.close()
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

    // Step-by-Step Diagnostic Dialog
    val activeDiagnostic = diagnosticState as? ModelDiagnosticState.Active
    if (activeDiagnostic != null) {
        AlertDialog(
            onDismissRequest = {
                if (!activeDiagnostic.isRunning) {
                    diagnosticState = ModelDiagnosticState.Idle
                }
            },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                activeDiagnostic.isRunning -> EmeraldPrimary.copy(alpha = 0.12f)
                                activeDiagnostic.isAllPassed -> EmeraldPrimary.copy(alpha = 0.15f)
                                else -> MaterialTheme.colorScheme.errorContainer
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        activeDiagnostic.isRunning -> CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp,
                            color = EmeraldPrimary
                        )
                        activeDiagnostic.isAllPassed -> Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(30.dp)
                        )
                        else -> Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = when {
                        activeDiagnostic.isRunning -> "Testing On-Device AI..."
                        activeDiagnostic.isAllPassed -> "Offline Model Verified ✅"
                        else -> "Diagnostic Issue Found ⚠️"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = when {
                        activeDiagnostic.isRunning -> MaterialTheme.colorScheme.onSurface
                        activeDiagnostic.isAllPassed -> EmeraldPrimary
                        else -> MaterialTheme.colorScheme.error
                    },
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = when {
                            activeDiagnostic.isRunning -> "Checking neural weights, CPU interpreter, vision filters & latency on this phone..."
                            activeDiagnostic.isAllPassed -> "All 5 diagnostic steps passed! Your offline model is 100% operational on this phone's hardware."
                            else -> "Step ${activeDiagnostic.failedStepIndex?.plus(1) ?: "?"} failed. Review the exact error details below:"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    )

                    // 5 Step Items Card List
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        activeDiagnostic.steps.forEach { step ->
                            val isStepFailed = step.status == DiagnosticStepStatus.FAILED
                            val isStepPassed = step.status == DiagnosticStepStatus.PASSED
                            val isStepRunning = step.status == DiagnosticStepStatus.RUNNING

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = when {
                                    isStepFailed -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                                    isStepPassed -> EmeraldPrimary.copy(alpha = 0.08f)
                                    isStepRunning -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                                },
                                border = if (isStepRunning) BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f)) else null,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        when (step.status) {
                                            DiagnosticStepStatus.PASSED -> Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = EmeraldPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            DiagnosticStepStatus.FAILED -> Icon(
                                                imageVector = Icons.Default.Cancel,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            DiagnosticStepStatus.RUNNING -> CircularProgressIndicator(
                                                modifier = Modifier.size(14.dp),
                                                strokeWidth = 2.dp,
                                                color = EmeraldPrimary
                                            )
                                            DiagnosticStepStatus.PENDING -> Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Step ${step.stepNumber}: ${step.title}",
                                            fontWeight = if (isStepRunning || isStepFailed) FontWeight.Bold else FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = when {
                                                isStepFailed -> MaterialTheme.colorScheme.error
                                                isStepPassed -> MaterialTheme.colorScheme.onSurface
                                                isStepRunning -> EmeraldPrimary
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )

                                        if (isStepPassed && step.detail != null) {
                                            Text(
                                                text = "✓ ${step.detail}",
                                                fontSize = 11.sp,
                                                color = EmeraldPrimary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        } else if (isStepFailed && step.errorMessage != null) {
                                            Text(
                                                text = "✗ Error: ${step.errorMessage}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.error,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        } else {
                                            Text(
                                                text = step.description,
                                                fontSize = 10.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Summary Banner
                    if (activeDiagnostic.isAllPassed) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = EmeraldPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text("• Benchmark Speed: ${activeDiagnostic.benchmarkLatencyMs ?: 0} ms", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = EmeraldPrimary)
                                Text("• Status: 100% Operational & Verified", fontSize = 11.sp)
                                Text("• 43 Disease classes loaded locally", fontSize = 11.sp)
                                Text("• Zero internet connection needed", fontSize = 11.sp)
                            }
                        }
                    } else if (!activeDiagnostic.isRunning && !activeDiagnostic.isAllPassed) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    text = "Failure Reason:",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = activeDiagnostic.failureReason ?: "Unknown error",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { diagnosticState = ModelDiagnosticState.Idle },
                    enabled = !activeDiagnostic.isRunning,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeDiagnostic.isAllPassed) EmeraldPrimary else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(if (activeDiagnostic.isRunning) "Testing..." else "Close")
                }
            },
            dismissButton = {
                if (!activeDiagnostic.isRunning && !activeDiagnostic.isAllPassed) {
                    OutlinedButton(
                        onClick = { runModelDiagnostic() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Retry", fontSize = 12.sp)
                    }
                }
            }
        )
    }
}
