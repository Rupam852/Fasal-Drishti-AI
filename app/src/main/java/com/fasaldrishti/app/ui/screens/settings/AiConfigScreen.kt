package com.fasaldrishti.app.ui.screens.settings

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fasaldrishti.app.data.local.*
import com.fasaldrishti.app.data.remote.SupabaseManager
import com.fasaldrishti.app.ui.theme.EmeraldPrimary
import com.fasaldrishti.app.ui.theme.NeonLime
import com.fasaldrishti.app.ui.theme.SolarGold
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiConfigScreen(
    aiConfigManager: AiConfigManager,
    supabaseManager: SupabaseManager? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val configState by aiConfigManager.configState.collectAsState()

    val languageManager = remember { LanguageManager(context) }
    val currentLang by languageManager.currentLanguage.collectAsState()

    // Local edit state for custom mode
    var isCustomMode by remember(configState.isCustomMode) { mutableStateOf(configState.isCustomMode) }
    var primaryProvider by remember(configState.primaryProvider) { mutableStateOf(configState.primaryProvider) }
    var selectedGeminiModel by remember(configState.geminiModel) { mutableStateOf(configState.geminiModel) }
    var geminiApiKey by remember(configState.geminiApiKey) { mutableStateOf(configState.geminiApiKey) }
    var isGeminiKeyVisible by remember { mutableStateOf(false) }

    var selectedNvidiaModel by remember(configState.nvidiaModel) { mutableStateOf(configState.nvidiaModel) }
    var nvidiaApiKey by remember(configState.nvidiaApiKey) { mutableStateOf(configState.nvidiaApiKey) }
    var isNvidiaKeyVisible by remember { mutableStateOf(false) }

    // Dropdown expanded states
    var geminiDropdownExpanded by remember { mutableStateOf(false) }
    var nvidiaDropdownExpanded by remember { mutableStateOf(false) }

    // Diagnostic Testing Dialog State
    var showDiagnosticDialog by remember { mutableStateOf(false) }
    var isRunningDiagnostic by remember { mutableStateOf(false) }
    var diagnosticResults by remember { mutableStateOf<List<DiagnosticResult>>(emptyList()) }
    var diagnosticTitle by remember { mutableStateOf("") }

    // Cooldown countdown for default test
    var remainingCooldown by remember { mutableStateOf(aiConfigManager.getRemainingDefaultCooldownSeconds()) }

    LaunchedEffect(Unit) {
        while (true) {
            val cooldown = aiConfigManager.getRemainingDefaultCooldownSeconds()
            remainingCooldown = cooldown
            delay(1000)
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onNavigateBack,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "AI Configuration",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                    )
                    Text(
                        text = "Manage AI Engines, Models & API Keys",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 11.5.sp
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. MODE SELECTOR (DEFAULT vs CUSTOM)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "AI Engine Operating Mode",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Choose between built-in managed cloud models or your own custom API keys.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontSize = 11.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Default Mode Chip/Button
                            Surface(
                                onClick = {
                                    isCustomMode = false
                                    aiConfigManager.setMode(false)
                                    Toast.makeText(context, "Switched to Default AI Models", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = if (!isCustomMode) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(
                                    1.dp,
                                    if (!isCustomMode) EmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = if (!isCustomMode) Color.Black else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Default AI",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (!isCustomMode) Color.Black else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Text(
                                        text = "Pre-Configured",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.5.sp,
                                            color = if (!isCustomMode) Color.Black.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                            }

                            // Custom Mode Chip/Button
                            Surface(
                                onClick = {
                                    isCustomMode = true
                                    aiConfigManager.setMode(true)
                                    Toast.makeText(context, "Switched to Custom AI Configuration", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = if (isCustomMode) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(
                                    1.dp,
                                    if (isCustomMode) EmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = null,
                                        tint = if (isCustomMode) Color.Black else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Custom AI",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isCustomMode) Color.Black else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Text(
                                        text = "Own Keys & Models",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.5.sp,
                                            color = if (isCustomMode) Color.Black.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. VIEW BASED ON MODE: DEFAULT vs CUSTOM
            if (!isCustomMode) {
                // ==========================================
                // DEFAULT MODE VIEW
                // ==========================================
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Managed High-Availability Engines",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Zero configuration required by farmer",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Default Engine 1: Gemini
                            DefaultEngineItem(
                                title = "Primary: Google Gemini Vision AI",
                                subtitle = "gemini-3.7-flash • Multimodal leaf diagnosis & treatment",
                                icon = Icons.Default.Psychology,
                                badge = "Active",
                                badgeColor = EmeraldPrimary
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Default Engine 2: NVIDIA NIM
                            DefaultEngineItem(
                                title = "Secondary: NVIDIA NIM Agronomy",
                                subtitle = "meta/llama-3.2-11b-vision-instruct • Cloud fallback",
                                icon = Icons.Default.Memory,
                                badge = "Standby",
                                badgeColor = SolarGold
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Check Default AI Models Button (with 30s Cooldown)
                            Button(
                                onClick = {
                                    if (remainingCooldown > 0) {
                                        Toast.makeText(context, "Please wait $remainingCooldown seconds before checking again.", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }

                                    aiConfigManager.recordDefaultTestAttempt()
                                    remainingCooldown = 30
                                    diagnosticTitle = "Default AI Diagnostics"
                                    showDiagnosticDialog = true
                                    isRunningDiagnostic = true
                                    diagnosticResults = emptyList()

                                    coroutineScope.launch {
                                        val results = mutableListOf<DiagnosticResult>()

                                        // Test Gemini Default with Cascade (verifies best working active model live)
                                        val defaultGeminiKey = supabaseManager?.getRemoteConfig("gemini_api_key", "") ?: ""
                                        val testKey = if (defaultGeminiKey.isNotBlank()) defaultGeminiKey else "AIzaSy_DEFAULT"
                                        val defaultGeminiModel = supabaseManager?.getRemoteConfig("gemini_model_name", "gemini-3.7-flash") ?: "gemini-3.7-flash"
                                        val geminiRes = aiConfigManager.testGeminiCascade(defaultGeminiModel, testKey)
                                        results.add(geminiRes)
                                        diagnosticResults = results.toList()
                                        delay(400)

                                        // Test NVIDIA Default
                                        val defaultNvidiaKey = supabaseManager?.getRemoteConfig("nvidia_nim_api_key", "") ?: ""
                                        val nvidiaRes = aiConfigManager.testNvidia("meta/llama-3.2-11b-vision-instruct", defaultNvidiaKey)
                                        results.add(nvidiaRes)
                                        diagnosticResults = results.toList()

                                        isRunningDiagnostic = false
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                            ) {
                                if (remainingCooldown > 0) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Check Default Models ($remainingCooldown s)",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.NetworkCheck,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Check Default AI Models (Test)",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // ==========================================
                // CUSTOM MODE VIEW
                // ==========================================
                // 2A. PRIORITY TOGGLE: PRIMARY vs SECONDARY
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Engine Priority Order",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Which engine should handle the primary crop diagnosis first?",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Choice 1: Gemini Primary, NVIDIA Secondary
                            PriorityOptionCard(
                                title = "Google Gemini (Primary) ➔ NVIDIA NIM (Secondary)",
                                subtitle = "Fastest multimodal diagnosis with NVIDIA fallback",
                                isSelected = primaryProvider == AiProvider.GEMINI,
                                onClick = { primaryProvider = AiProvider.GEMINI }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Choice 2: NVIDIA Primary, Gemini Secondary
                            PriorityOptionCard(
                                title = "NVIDIA NIM (Primary) ➔ Google Gemini (Secondary)",
                                subtitle = "Specialized vision LLM first with Gemini fallback",
                                isSelected = primaryProvider == AiProvider.NVIDIA,
                                onClick = { primaryProvider = AiProvider.NVIDIA }
                            )
                        }
                    }
                }

                // 2B. GOOGLE GEMINI CUSTOM CONFIG
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(
                            1.dp,
                            if (primaryProvider == AiProvider.GEMINI) EmeraldPrimary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Psychology,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Google Gemini Engine",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (primaryProvider == AiProvider.GEMINI) EmeraldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = if (primaryProvider == AiProvider.GEMINI) "PRIMARY" else "SECONDARY",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = if (primaryProvider == AiProvider.GEMINI) EmeraldPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Gemini Model Dropdown
                            Text(
                                text = "Select Gemini Model",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            ExposedDropdownMenuBox(
                                expanded = geminiDropdownExpanded,
                                onExpandedChange = { geminiDropdownExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = selectedGeminiModel,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = geminiDropdownExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = EmeraldPrimary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = geminiDropdownExpanded,
                                    onDismissRequest = { geminiDropdownExpanded = false }
                                ) {
                                    AiConfigManager.AVAILABLE_GEMINI_MODELS.forEach { model ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = model.name,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        if (model.isRecommended) {
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Text(
                                                                text = "★ Best",
                                                                color = EmeraldPrimary,
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.ExtraBold
                                                            )
                                                        }
                                                    }
                                                    Text(
                                                        text = model.description,
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            fontSize = 10.5.sp,
                                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                        )
                                                    )
                                                }
                                            },
                                            onClick = {
                                                selectedGeminiModel = model.id
                                                geminiDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Gemini API Key Input
                            Text(
                                text = "Gemini API Key",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = geminiApiKey,
                                onValueChange = { geminiApiKey = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Paste your Gemini API key (AIzaSy...)") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                visualTransformation = if (isGeminiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    IconButton(onClick = { isGeminiKeyVisible = !isGeminiKeyVisible }) {
                                        Icon(
                                            imageVector = if (isGeminiKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle Key Visibility"
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = EmeraldPrimary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }
                }

                // 2C. NVIDIA NIM CUSTOM CONFIG
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(
                            1.dp,
                            if (primaryProvider == AiProvider.NVIDIA) SolarGold.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Memory,
                                        contentDescription = null,
                                        tint = SolarGold,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "NVIDIA NIM Engine",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (primaryProvider == AiProvider.NVIDIA) SolarGold.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = if (primaryProvider == AiProvider.NVIDIA) "PRIMARY" else "SECONDARY",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = if (primaryProvider == AiProvider.NVIDIA) SolarGold else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // NVIDIA Model Dropdown
                            Text(
                                text = "Select NVIDIA Model",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            ExposedDropdownMenuBox(
                                expanded = nvidiaDropdownExpanded,
                                onExpandedChange = { nvidiaDropdownExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = selectedNvidiaModel,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = nvidiaDropdownExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SolarGold,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = nvidiaDropdownExpanded,
                                    onDismissRequest = { nvidiaDropdownExpanded = false }
                                ) {
                                    AiConfigManager.AVAILABLE_NVIDIA_MODELS.forEach { model ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = model.name,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        if (model.isRecommended) {
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Text(
                                                                text = "★ Best",
                                                                color = SolarGold,
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.ExtraBold
                                                            )
                                                        }
                                                    }
                                                    Text(
                                                        text = model.description,
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            fontSize = 10.5.sp,
                                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                        )
                                                    )
                                                }
                                            },
                                            onClick = {
                                                selectedNvidiaModel = model.id
                                                nvidiaDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // NVIDIA API Key Input
                            Text(
                                text = "NVIDIA API Key",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = nvidiaApiKey,
                                onValueChange = { nvidiaApiKey = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Paste your NVIDIA API key (nvapi-...)") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                visualTransformation = if (isNvidiaKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    IconButton(onClick = { isNvidiaKeyVisible = !isNvidiaKeyVisible }) {
                                        Icon(
                                            imageVector = if (isNvidiaKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle Key Visibility"
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SolarGold,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }
                }

                // 2D. SAVE CONFIGURATION BUTTON
                item {
                    Button(
                        onClick = {
                            val newConfig = AiConfigState(
                                isCustomMode = true,
                                primaryProvider = primaryProvider,
                                secondaryProvider = if (primaryProvider == AiProvider.GEMINI) AiProvider.NVIDIA else AiProvider.GEMINI,
                                geminiModel = selectedGeminiModel,
                                geminiApiKey = geminiApiKey.trim(),
                                nvidiaModel = selectedNvidiaModel,
                                nvidiaApiKey = nvidiaApiKey.trim()
                            )
                            aiConfigManager.saveConfig(newConfig)
                            Toast.makeText(context, "Custom AI Configuration Saved Locally!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save Configuration",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                // 2E. CHECK CUSTOM CONFIGURATION BUTTON
                item {
                    OutlinedButton(
                        onClick = {
                            diagnosticTitle = "Custom AI Diagnostics"
                            showDiagnosticDialog = true
                            isRunningDiagnostic = true
                            diagnosticResults = emptyList()

                            coroutineScope.launch {
                                val results = mutableListOf<DiagnosticResult>()

                                // Test Custom Gemini
                                val geminiRes = aiConfigManager.testGemini(selectedGeminiModel, geminiApiKey.trim())
                                results.add(geminiRes)
                                diagnosticResults = results.toList()
                                delay(400)

                                // Test Custom NVIDIA
                                val nvidiaRes = aiConfigManager.testNvidia(selectedNvidiaModel, nvidiaApiKey.trim())
                                results.add(nvidiaRes)
                                diagnosticResults = results.toList()

                                isRunningDiagnostic = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, EmeraldPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Troubleshoot,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Check Your Custom AI Configuration (Test)",
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

    // ==========================================
    // DIAGNOSTIC RESULTS MODAL DIALOG
    // ==========================================
    if (showDiagnosticDialog) {
        Dialog(
            onDismissRequest = {
                if (!isRunningDiagnostic) showDiagnosticDialog = false
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(vertical = 24.dp),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HealthAndSafety,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = diagnosticTitle,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                                )
                                Text(
                                    text = if (isRunningDiagnostic) "Pinging AI server endpoints..." else "Test completed",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                )
                            }
                        }

                        if (!isRunningDiagnostic) {
                            IconButton(onClick = { showDiagnosticDialog = false }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close"
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    if (diagnosticResults.isEmpty() && isRunningDiagnostic) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 30.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = EmeraldPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            diagnosticResults.forEach { result ->
                                DiagnosticItemCard(result = result)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { showDiagnosticDialog = false },
                        enabled = !isRunningDiagnostic,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text(
                            text = if (isRunningDiagnostic) "Testing..." else "Done",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DefaultEngineItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badge: String,
    badgeColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = badgeColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = badge,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp,
                        color = badgeColor
                    )
                )
            }
        }
    }
}

@Composable
private fun PriorityOptionCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) EmeraldPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(
            1.dp,
            if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = EmeraldPrimary)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
            }
        }
    }
}

@Composable
private fun DiagnosticItemCard(result: DiagnosticResult) {
    val statusColor = if (result.isSuccess) EmeraldPrimary else Color(0xFFEF4444)
    val statusIcon = if (result.isSuccess) Icons.Default.CheckCircle else Icons.Default.Cancel

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = statusColor.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = statusIcon,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = result.provider.displayName,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )
                    Text(
                        text = result.model,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = result.message,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.5.sp,
                        color = if (result.isSuccess) EmeraldPrimary else statusColor
                    )
                )
            }
        }
    }
}
