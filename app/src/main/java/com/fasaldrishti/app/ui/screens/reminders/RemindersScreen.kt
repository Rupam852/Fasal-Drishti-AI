package com.fasaldrishti.app.ui.screens.reminders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fasaldrishti.app.domain.model.FarmReminder
import com.fasaldrishti.app.domain.model.ReminderType
import com.fasaldrishti.app.ui.theme.EmeraldDark
import com.fasaldrishti.app.ui.theme.EmeraldPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    viewModel: RemindersViewModel,
    onNavigateBack: () -> Unit
) {
    val reminders by viewModel.reminders.collectAsState()
    val untranslatedCount by viewModel.untranslatedCount.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val isTranslating by viewModel.isTranslating.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    var isIgnoringBattery by remember { mutableStateOf(com.fasaldrishti.app.util.BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context)) }
    var canScheduleExact by remember { mutableStateOf(com.fasaldrishti.app.util.BatteryOptimizationHelper.canScheduleExactAlarms(context)) }

    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                isIgnoringBattery = com.fasaldrishti.app.util.BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context)
                canScheduleExact = com.fasaldrishti.app.util.BatteryOptimizationHelper.canScheduleExactAlarms(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var activeFilter by remember { mutableStateOf<ReminderType?>(null) }
    var showLanguageMenu by remember { mutableStateOf(false) }

    val languages = listOf(
        "en" to "English",
        "hi" to "हिन्दी (Hindi)",
        "bn" to "বাংলা (Bengali)",
        "mr" to "मराठी (Marathi)",
        "pa" to "ਪੰਜਾਬੀ (Punjabi)",
        "te" to "తెలుగు (Telugu)",
        "ta" to "தமிழ் (Tamil)",
        "gu" to "ગુજરાતી (Gujarati)"
    )

    val pendingReminders = reminders.filter { !it.isCompleted }
    val completedReminders = reminders.filter { it.isCompleted }

    val filteredList = if (activeFilter == null) {
        pendingReminders
    } else {
        pendingReminders.filter { it.reminderType == activeFilter }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Farm Reminders",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        if (untranslatedCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE53935))
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Language Selector Pill for Reminders
                    Box {
                        Surface(
                            onClick = { showLanguageMenu = true },
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Translate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = languages.find { it.first == selectedLanguage }?.second?.takeWhile { it != ' ' } ?: "English",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showLanguageMenu,
                            onDismissRequest = { showLanguageMenu = false }
                        ) {
                            languages.forEach { (code, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        viewModel.setReminderLanguage(code)
                                        showLanguageMenu = false
                                    },
                                    leadingIcon = if (selectedLanguage == code) {
                                        { Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldPrimary) }
                                    } else null
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Translation Warning Banner
            if (untranslatedCount > 0 && selectedLanguage != "en") {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFDE68A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Translate,
                                    contentDescription = null,
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Please translate before notification",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF92400E),
                                            fontSize = 13.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE53935))
                                    )
                                }
                                Text(
                                    text = "$untranslatedCount reminders ready to translate to ${languages.find { it.first == selectedLanguage }?.second ?: ""}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF78350F),
                                        fontSize = 11.5.sp
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = { viewModel.translateAllNow() },
                                enabled = !isTranslating,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                if (isTranslating) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "Translate Now",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Summary Counters & Action Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ReminderStatPill(
                        title = "Active Tasks",
                        count = pendingReminders.size.toString(),
                        color = EmeraldPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    ReminderStatPill(
                        title = "Completed",
                        count = completedReminders.size.toString(),
                        color = Color(0xFF0284C7),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 3. Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = activeFilter == null,
                        onClick = { activeFilter = null },
                        label = { Text("All (${pendingReminders.size})") }
                    )
                    FilterChip(
                        selected = activeFilter == ReminderType.SPRAY,
                        onClick = { activeFilter = ReminderType.SPRAY },
                        label = { Text("💧 Spray") }
                    )
                    FilterChip(
                        selected = activeFilter == ReminderType.INSPECTION,
                        onClick = { activeFilter = ReminderType.INSPECTION },
                        label = { Text("🌾 Inspection") }
                    )
                }
            }

            // 4. Pending Reminders List
            if (filteredList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "All Farm Tasks Up to Date!",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "No pending spray or inspection reminders.",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            FilledTonalButton(
                                onClick = { viewModel.addInspectionReminder("Rice Field", daysDelay = 5) },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add 5-Day Rice Inspection")
                            }
                        }
                    }
                }
            } else {
                items(filteredList, key = { it.id }) { reminder ->
                    ReminderCard(
                        reminder = reminder,
                        onMarkDone = { viewModel.markCompleted(reminder.id) },
                        onPause = { days -> viewModel.pauseReminder(reminder.id, days) },
                        onResume = { viewModel.resumeReminder(reminder.id) },
                        onDelete = { viewModel.deleteReminder(reminder.id) },
                        onSpeak = { viewModel.speakReminder(reminder) }
                    )
                }
            }

            // 5. Completed Reminders Section (if any)
            if (completedReminders.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Completed Reminders (${completedReminders.size})",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
                items(completedReminders, key = { it.id }) { reminder ->
                    CompletedReminderRow(
                        reminder = reminder,
                        onDelete = { viewModel.deleteReminder(reminder.id) }
                    )
                }
            }

            // 6. NOTIFICATION TESTING & DIAGNOSTICS SECTION
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
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
                                    imageVector = Icons.Default.Science,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Notification Live Testing",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Verify in-app & background notification delivery",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Test Button 1: Immediate in-app test
                        Surface(
                            onClick = { viewModel.sendImmediateTestNotification() },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "⚡ Send Test Notification Now",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Triggers status bar alert immediately while app is open",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Test Button 2: 10-second delayed background test
                        Surface(
                            onClick = { viewModel.scheduleDelayed10SecNotification() },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "⏱️ Send Notification After 10 Seconds",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Tap & close or minimize app now to test background arrival",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "🔋 Background Delivery & Battery Settings (Android 14/15)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "To guarantee notifications arrive on time after swiping the app away, allow background activity below:",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Battery Optimization Status & Toggle
                        Surface(
                            onClick = {
                                com.fasaldrishti.app.util.BatteryOptimizationHelper.requestIgnoreBatteryOptimizations(context)
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isIgnoringBattery) Color(0xFFECFDF5) else Color(0xFFFFFBEB),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isIgnoringBattery) Color(0xFF10B981).copy(alpha = 0.5f) else Color(0xFFF59E0B).copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isIgnoringBattery) Icons.Default.CheckCircle else Icons.Default.BatteryAlert,
                                    contentDescription = null,
                                    tint = if (isIgnoringBattery) Color(0xFF059669) else Color(0xFFD97706),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isIgnoringBattery) "Background Activity: Unrestricted ✅" else "Disable Battery Optimization ⚡",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isIgnoringBattery) Color(0xFF047857) else Color(0xFFB45309),
                                            fontSize = 12.5.sp
                                        )
                                    )
                                    Text(
                                        text = if (isIgnoringBattery) "App is allowed to deliver alerts when closed or idle" else "Tap here to allow timely alerts when app is swiped away",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 10.5.sp
                                        )
                                    )
                                }
                                if (!isIgnoringBattery) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFD97706)
                                    ) {
                                        Text(
                                            text = "Allow",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (!canScheduleExact) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                onClick = {
                                    com.fasaldrishti.app.util.BatteryOptimizationHelper.openExactAlarmSettings(context)
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFEF2F2),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Enable Exact Alarms Permission ⏰",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFB91C1C),
                                                fontSize = 12.5.sp
                                            )
                                        )
                                        Text(
                                            text = "Tap to grant Alarms & Reminders permission",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 10.5.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ReminderCard(
    reminder: FarmReminder,
    onMarkDone: () -> Unit,
    onPause: (days: Int) -> Unit,
    onResume: () -> Unit,
    onDelete: () -> Unit,
    onSpeak: () -> Unit
) {
    val currentTime = System.currentTimeMillis()
    val isOverdue = currentTime > reminder.dueDate && !reminder.isPaused
    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(reminder.dueDate))

    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isOverdue) Color(0xFFEF4444).copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Type Badge + Urgency + Voice Speak + More Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (typeBg, typeColor, typeText) = when (reminder.reminderType) {
                        ReminderType.SPRAY -> Triple(Color(0xFFECFDF5), Color(0xFF047857), "💧 SPRAY")
                        ReminderType.INSPECTION -> Triple(Color(0xFFF0F9FF), Color(0xFF0369A1), "🌾 INSPECTION")
                        ReminderType.WEATHER -> Triple(Color(0xFFFEF3C7), Color(0xFFB45309), "🌧️ WEATHER")
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = typeBg
                    ) {
                        Text(
                            text = typeText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = typeColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    if (reminder.isPaused) {
                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFE2E8F0)) {
                            Text(
                                text = "⏸️ PAUSED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF475569),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    } else if (isOverdue) {
                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFFEE2E2)) {
                            Text(
                                text = "🔴 OVERDUE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFB91C1C),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Smart Idea 2: Voice Audio Speak Button
                    IconButton(
                        onClick = onSpeak,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Read aloud",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // More Options Dropdown
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            if (reminder.isPaused) {
                                DropdownMenuItem(
                                    text = { Text("Resume Reminder") },
                                    onClick = {
                                        onResume()
                                        showMenu = false
                                    },
                                    leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null) }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text("Pause for 2 Days") },
                                    onClick = {
                                        onPause(2)
                                        showMenu = false
                                    },
                                    leadingIcon = { Icon(Icons.Default.Pause, contentDescription = null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Pause for 7 Days") },
                                    onClick = {
                                        onPause(7)
                                        showMenu = false
                                    },
                                    leadingIcon = { Icon(Icons.Default.Pause, contentDescription = null) }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Delete", color = Color(0xFFDC2626)) },
                                onClick = {
                                    onDelete()
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFDC2626)) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = reminder.displayTitle,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Message description
            Text(
                text = reminder.displayMessage,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = 18.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Due Date & Mark Done Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Due: $dateStr",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Button(
                    onClick = onMarkDone,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mark Done",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun CompletedReminderRow(
    reminder: FarmReminder,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = reminder.displayTitle,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = "Completed",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ReminderStatPill(
    title: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Surface(
                shape = CircleShape,
                color = color
            ) {
                Text(
                    text = count,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}
