package com.fasaldrishti.app.ui.screens.chat

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.compose.ui.graphics.graphicsLayer
import com.fasaldrishti.app.domain.model.ChatMessage
import com.fasaldrishti.app.ui.theme.CrimsonCoral
import com.fasaldrishti.app.ui.theme.EmeraldDark
import com.fasaldrishti.app.ui.theme.EmeraldPrimary
import com.fasaldrishti.app.ui.theme.ObsidianVoid
import com.fasaldrishti.app.ui.theme.SolarGold
import com.fasaldrishti.app.util.VoiceAssistantManager

@Composable
fun ChatScreen(
    contextInfo: String?,
    viewModel: ChatViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val voiceAssistant = remember { VoiceAssistantManager(context) }
    val isListening by voiceAssistant.isListening.collectAsState()
    val isSpeaking by voiceAssistant.isSpeaking.collectAsState()
    val currentlySpeakingId by voiceAssistant.currentlySpeakingId.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            voiceAssistant.destroy()
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            voiceAssistant.startListening(languageCode = uiState.selectedLanguage) { recognized ->
                inputText = recognized
            }
        }
    }

    LaunchedEffect(contextInfo) {
        viewModel.initContext(contextInfo)
    }

    var showClearConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.messages.size, uiState.isAiTyping) {
        if (uiState.messages.isNotEmpty()) {
            kotlinx.coroutines.delay(100)
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    val strings = com.fasaldrishti.app.ui.localization.LocalAppStrings.current
    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(strings.chatClearHistoryTitle, fontWeight = FontWeight.Bold) },
            text = { Text(strings.chatClearHistoryConfirm) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearChat()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(strings.chatClearHistoryButton, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(strings.chatCancelButton)
                }
            }
        )
    }

    val suggestedChips = listOf(
        "🇮🇳 Explain in Hindi",
        "🧪 Chemical Spray & Dosage",
        "🌿 Organic & Bio Remedies",
        "🌾 Explain in Bengali",
        "🌾 Explain in Marathi",
        "Is it contagious to other crops?"
    )

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (contextInfo != null) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = strings.backButton
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .shadow(4.dp, CircleShape, spotColor = EmeraldPrimary)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(listOf(EmeraldPrimary, EmeraldDark))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = strings.chatTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp
                        )
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    var showLanguageMenu by remember { mutableStateOf(false) }
                    val supportedLanguages = listOf(
                        "English",
                        "Hinglish",
                        "हिन्दी (Hindi)",
                        "বাংলা (Bengali)",
                        "मराठी (Marathi)",
                        "ਪੰਜਾਬੀ (Punjabi)",
                        "ગુજરાતી (Gujarati)",
                        "తెలుగు (Telugu)",
                        "தமிழ் (Tamil)",
                        "ಕನ್ನಡ (Kannada)",
                        "മലയാളം (Malayalam)",
                        "ଓଡ଼ିଆ (Odia)"
                    )

                    Box {
                        Surface(
                            onClick = { showLanguageMenu = true },
                            shape = RoundedCornerShape(20.dp),
                            color = EmeraldPrimary.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Translate,
                                    contentDescription = "Language",
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = uiState.selectedLanguage.substringBefore(" "),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showLanguageMenu,
                            onDismissRequest = { showLanguageMenu = false },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface)
                                .heightIn(max = 360.dp)
                        ) {
                            Text(
                                text = "🌐 AI Response Language",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldPrimary
                                ),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            HorizontalDivider(modifier = Modifier.padding(bottom = 4.dp))
                            supportedLanguages.forEach { lang ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = if (lang == "English") "English (Default)" else lang,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (uiState.selectedLanguage == lang) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (uiState.selectedLanguage == lang) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                                                )
                                            )
                                            if (uiState.selectedLanguage == lang) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = EmeraldPrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        viewModel.setResponseLanguage(lang)
                                        showLanguageMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = { showClearConfirmDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Clear Chat",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(start = 14.dp, end = 14.dp, top = 2.dp, bottom = 4.dp)
            ) {
                // Quick suggested reply chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    items(suggestedChips) { chip ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                            modifier = Modifier.clickable {
                                viewModel.sendMessage(chip)
                            }
                        ) {
                            Text(
                                text = chip,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }

                // Chat Input Row
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = {
                                Text(if (isListening) "🎙️ Listening..." else strings.chatTypePlaceholder)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            maxLines = 3
                        )

                        // Voice Mic Button
                        val micTransition = rememberInfiniteTransition(label = "micPulse")
                        val micScale by micTransition.animateFloat(
                            initialValue = 1.0f,
                            targetValue = 1.25f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(600, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "micScale"
                        )

                        IconButton(
                            onClick = {
                                if (isListening) {
                                    voiceAssistant.stopListening()
                                } else {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (hasPermission) {
                                        voiceAssistant.startListening(languageCode = uiState.selectedLanguage) { recognized ->
                                            inputText = recognized
                                        }
                                    } else {
                                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .then(if (isListening) Modifier.graphicsLayer(scaleX = micScale, scaleY = micScale) else Modifier)
                                .clip(CircleShape)
                                .background(
                                    if (isListening) CrimsonCoral.copy(alpha = 0.2f)
                                    else Color.Transparent
                                )
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Voice Input",
                                tint = if (isListening) CrimsonCoral else EmeraldPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    viewModel.sendMessage(inputText)
                                    inputText = ""
                                }
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    if (inputText.isNotBlank()) EmeraldPrimary
                                    else Color.Transparent
                                )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (inputText.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Pinned Context Card
            if (uiState.contextInfo != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = EmeraldDark
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Diagnosed Context: ${uiState.contextInfo}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
            }

            // Message List
            items(uiState.messages) { message ->
                ChatMessageBubble(
                    message = message,
                    isSpeaking = isSpeaking && currentlySpeakingId == message.id,
                    onToggleSpeak = { text, id ->
                        voiceAssistant.speak(text, id, uiState.selectedLanguage)
                    }
                )
            }

            // Typing Indicator
            if (uiState.isAiTyping) {
                item {
                    Row(
                        modifier = Modifier.padding(start = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "AI Salahkar is thinking...",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(
    message: ChatMessage,
    isSpeaking: Boolean = false,
    onToggleSpeak: (String, String) -> Unit = { _, _ -> }
) {
    val isUser = message.isUser
    val formattedText = remember(message.text) {
        if (isUser) AnnotatedString(message.text)
        else parseMarkdownToAnnotatedString(message.text)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(EmeraldDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (isUser) 20.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 20.dp
            ),
            color = if (isUser) EmeraldPrimary
            else MaterialTheme.colorScheme.surface,
            border = if (!isUser) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)) else null,
            shadowElevation = 2.dp,
            modifier = Modifier.widthIn(max = 310.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = formattedText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp,
                        fontSize = 14.5.sp
                    )
                )

                // Speaker / Audio Playback Button for AI responses
                if (!isUser) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = { onToggleSpeak(message.text, message.id) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSpeaking) EmeraldPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSpeaking) EmeraldPrimary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = if (isSpeaking) "Stop Audio" else "Listen Audio",
                                    tint = if (isSpeaking) EmeraldPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isSpeaking) "Speaking..." else "Listen",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSpeaking) EmeraldPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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

/**
 * Converts raw AI Markdown (bold **text**, bullets * or -, headers ###)
 * into clean, formatted Jetpack Compose AnnotatedString without ugly asterisks.
 */
private fun parseMarkdownToAnnotatedString(rawText: String): AnnotatedString {
    return buildAnnotatedString {
        val lines = rawText.lines()
        lines.forEachIndexed { lineIdx, line ->
            var trimmedLine = line.trim()

            // Header (# or ## or ###) -> Strip # and style as Bold
            val isHeader = trimmedLine.startsWith("### ") || trimmedLine.startsWith("## ") || trimmedLine.startsWith("# ")
            if (isHeader) {
                trimmedLine = trimmedLine.replace(Regex("^#{1,6}\\s*"), "")
            }

            // Bullet (* Point or - Point) -> Replace with clean bullet dot
            val isBullet = trimmedLine.startsWith("* ") || trimmedLine.startsWith("- ") || trimmedLine.startsWith("+ ")
            if (isBullet) {
                append("• ")
                trimmedLine = trimmedLine.substring(2).trim()
            }

            // Bold pattern (**bold**)
            val boldPattern = Regex("\\*\\*(.*?)\\*\\*")
            val matches = boldPattern.findAll(trimmedLine).toList()

            if (isHeader) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(trimmedLine)
                }
            } else if (matches.isEmpty()) {
                // Strip single stray asterisks e.g. *dosage* -> dosage
                val cleaned = trimmedLine.replace(Regex("(?<!\\*)\\*(?!\\*)"), "")
                append(cleaned)
            } else {
                var cursor = 0
                for (match in matches) {
                    if (match.range.first > cursor) {
                        val beforeText = trimmedLine.substring(cursor, match.range.first)
                        val cleanedBefore = beforeText.replace(Regex("(?<!\\*)\\*(?!\\*)"), "")
                        append(cleanedBefore)
                    }
                    val boldText = match.groupValues[1]
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(boldText)
                    }
                    cursor = match.range.last + 1
                }
                if (cursor < trimmedLine.length) {
                    val remainingText = trimmedLine.substring(cursor)
                    val cleanedRemaining = remainingText.replace(Regex("(?<!\\*)\\*(?!\\*)"), "")
                    append(cleanedRemaining)
                }
            }

            if (lineIdx < lines.size - 1) {
                append("\n")
            }
        }
    }
}
