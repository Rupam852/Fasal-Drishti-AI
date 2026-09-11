package com.fasaldrishti.app.ui.screens.chat

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
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
import androidx.compose.ui.layout.ContentScale
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
import coil.compose.AsyncImage
import com.fasaldrishti.app.domain.model.ChatMessage
import com.fasaldrishti.app.ui.theme.CrimsonCoral
import com.fasaldrishti.app.ui.theme.EmeraldDark
import com.fasaldrishti.app.ui.theme.EmeraldPrimary
import com.fasaldrishti.app.ui.theme.ObsidianVoid
import com.fasaldrishti.app.ui.theme.SolarGold
import com.fasaldrishti.app.util.VoiceAssistantManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

@Composable
fun ChatScreen(
    contextInfo: String?,
    viewModel: ChatViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAiConfig: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    var inputText by remember { mutableStateOf("") }
    var attachedImageUri by remember { mutableStateOf<Uri?>(null) }
    var attachedBase64 by remember { mutableStateOf<String?>(null) }
    var isCompressingImage by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            attachedImageUri = uri
            isCompressingImage = true
            coroutineScope.launch {
                val compressed = compressImageUriToBase64(context, uri)
                attachedBase64 = compressed?.second
                isCompressingImage = false
            }
        }
    }

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

    val hasContext = !uiState.contextInfo.isNullOrBlank()
    val suggestedChips = remember(uiState.contextInfo) {
        if (hasContext) {
            listOf(
                "🧪 Exact Dosage & Spray Chart",
                "🌿 Organic & Bio Remedies",
                "⏳ Recovery Timeline & PHI",
                "🌧️ Weather & Rain Precautions",
                "🇮🇳 Explain in Hindi",
                "🌾 Explain in Bengali",
                "Is this disease contagious?"
            )
        } else {
            listOf(
                "🧪 Fertilizer (NPK) Dosage Help",
                "🌾 Rice & Wheat Disease Advice",
                "🌧️ Is today suitable for spraying?",
                "🏛️ PM-Kisan Yojana Details",
                "🇮🇳 Hindi me samjhaiye",
                "🌾 বাংলা ভাষায় বলুন"
            )
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.backButton
                        )
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
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(start = 14.dp, end = 14.dp, top = 2.dp, bottom = 8.dp)
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

                // Attached Photo Preview
                AnimatedVisibility(
                    visible = attachedImageUri != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    attachedImageUri?.let { uri ->
                        Box(
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .size(76.dp)
                                .shadow(4.dp, RoundedCornerShape(14.dp))
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.5.dp, EmeraldPrimary, RoundedCornerShape(14.dp))
                        ) {
                            AsyncImage(
                                model = uri,
                                contentDescription = "Attached crop image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            if (isCompressingImage) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.55f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = EmeraldPrimary,
                                        strokeWidth = 2.5.dp
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    attachedImageUri = null
                                    attachedBase64 = null
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.65f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
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
                            .padding(start = 6.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // + Photo Attachment Button
                        IconButton(
                            onClick = {
                                imagePickerLauncher.launch("image/*")
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    if (attachedImageUri != null) EmeraldPrimary.copy(alpha = 0.2f)
                                    else EmeraldPrimary.copy(alpha = 0.10f)
                                )
                        ) {
                            Icon(
                                imageVector = if (attachedImageUri != null) Icons.Default.AddPhotoAlternate else Icons.Default.Add,
                                contentDescription = "Attach crop photo",
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

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

                        Spacer(modifier = Modifier.width(4.dp))

                        val canSend = (inputText.isNotBlank() || attachedImageUri != null) && !isCompressingImage
                        IconButton(
                            onClick = {
                                if (canSend) {
                                    viewModel.sendMessage(
                                        userText = inputText,
                                        imageUri = attachedImageUri?.toString(),
                                        base64Image = attachedBase64
                                    )
                                    inputText = ""
                                    attachedImageUri = null
                                    attachedBase64 = null
                                }
                            },
                            enabled = canSend,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    if (canSend) EmeraldPrimary
                                    else Color.Transparent
                                )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (canSend) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
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
            // Pinned Consultation Banner (Plant Specific vs General Mode)
            if (uiState.contextInfo != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, EmeraldPrimary.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Spa,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Active Plant Consultation",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = EmeraldPrimary,
                                                fontSize = 12.sp
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = EmeraldPrimary.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "TARGETED AI",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 9.sp,
                                                    color = EmeraldPrimary
                                                ),
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = uiState.contextInfo ?: "Diagnosed Crop Condition",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.5.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "🎯 AI Salahkar is providing targeted chemical dosages, organic remedies & recovery advice specifically for this diagnosis.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp
                                )
                            )
                        }
                    }
                }
            } else if (uiState.messages.size <= 2) {
                // General Mode Welcome Hero Card (when opened from Home Screen)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "🌾 24x7 Digital Agronomist (Fasal Salah)",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Ask any farming question or tap the mic 🎙️ to speak in Hindi or your mother tongue.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                        fontSize = 11.5.sp,
                                        lineHeight = 15.sp
                                    )
                                )
                            }
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
                    },
                    onRetry = { id, query ->
                        viewModel.retryFailedMessage(id, query)
                    },
                    onNavigateToAiConfig = onNavigateToAiConfig
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
    onToggleSpeak: (String, String) -> Unit = { _, _ -> },
    onRetry: (String, String) -> Unit = { _, _ -> },
    onNavigateToAiConfig: () -> Unit = {}
) {
    val isUser = message.isUser
    val isError = message.isError
    val isApiKeyError = message.isApiKeyError

    val formattedText = remember(message.text) {
        if (isUser || isError) AnnotatedString(message.text)
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
                    .background(
                        when {
                            isError && isApiKeyError -> SolarGold.copy(alpha = 0.2f)
                            isError -> CrimsonCoral.copy(alpha = 0.2f)
                            else -> EmeraldDark
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        isError && isApiKeyError -> Icons.Default.Key
                        isError -> Icons.Default.Warning
                        else -> Icons.Default.Psychology
                    },
                    contentDescription = null,
                    tint = when {
                        isError && isApiKeyError -> SolarGold
                        isError -> CrimsonCoral
                        else -> Color.White
                    },
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
            color = when {
                isUser -> EmeraldPrimary
                isError && isApiKeyError -> SolarGold.copy(alpha = 0.08f)
                isError -> CrimsonCoral.copy(alpha = 0.08f)
                else -> MaterialTheme.colorScheme.surface
            },
            border = when {
                isUser -> null
                isError && isApiKeyError -> androidx.compose.foundation.BorderStroke(1.2.dp, SolarGold.copy(alpha = 0.6f))
                isError -> androidx.compose.foundation.BorderStroke(1.2.dp, CrimsonCoral.copy(alpha = 0.6f))
                else -> androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            },
            shadowElevation = 2.dp,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Attached Image in Bubble (if sent by user)
                if (!message.imageUri.isNullOrBlank()) {
                    AsyncImage(
                        model = message.imageUri,
                        contentDescription = "Attached crop image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp)
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                // If it is an Error Message, show Header Badge
                if (isError) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = if (isApiKeyError) "API Key Invalid / Expired" else "AI Agronomist is temporarily unreachable",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isApiKeyError) SolarGold else CrimsonCoral,
                                fontSize = 13.sp
                            )
                        )
                    }
                }

                Text(
                    text = formattedText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp,
                        fontSize = 14.5.sp
                    )
                )

                // Error Action Buttons (Retry / Open Settings)
                if (isError) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Retry Button
                        Button(
                            onClick = { onRetry(message.id, message.failedQuery ?: "") },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isApiKeyError) SolarGold else CrimsonCoral
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Retry",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                        }

                        // Settings Button (if API key error)
                        if (isApiKeyError) {
                            OutlinedButton(
                                onClick = onNavigateToAiConfig,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SolarGold.copy(alpha = 0.8f)),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = SolarGold,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Settings",
                                    color = SolarGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                )
                            }
                        }
                    }
                }

                // Speaker / Audio Playback Button for AI responses (when not error)
                if (!isUser && !isError) {
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

/**
 * Downsamples and compresses high-resolution gallery/camera photos (4MB-15MB)
 * to a lightweight max 800x800 JPEG (quality 80, ~80KB-120KB) and encodes to Base64.
 * Prevents multimodal model payload limits, network latency & fallback failures.
 */
private suspend fun compressImageUriToBase64(context: Context, uri: Uri): Pair<String, String>? = withContext(Dispatchers.IO) {
    try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        if (originalBitmap == null) return@withContext null

        val maxDimension = 800
        val width = originalBitmap.width
        val height = originalBitmap.height
        val scale = if (width > maxDimension || height > maxDimension) {
            maxDimension.toFloat() / maxOf(width, height)
        } else {
            1.0f
        }

        val scaledBitmap = if (scale < 1.0f) {
            Bitmap.createScaledBitmap(
                originalBitmap,
                (width * scale).toInt(),
                (height * scale).toInt(),
                true
            )
        } else {
            originalBitmap
        }

        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)

        Pair(uri.toString(), base64String)
    } catch (e: Exception) {
        null
    }
}

