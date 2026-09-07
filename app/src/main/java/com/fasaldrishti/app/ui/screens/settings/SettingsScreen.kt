package com.fasaldrishti.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fasaldrishti.app.data.local.ThemeManager
import com.fasaldrishti.app.data.local.ThemeMode
import com.fasaldrishti.app.data.remote.UpdateManager
import com.fasaldrishti.app.ui.theme.CrimsonCoral
import com.fasaldrishti.app.ui.theme.EmeraldPrimary
import com.fasaldrishti.app.ui.theme.SolarGold

enum class LanguageOption { ENGLISH, HINDI }

@Composable
fun SettingsScreen(
    updateManager: UpdateManager,
    themeManager: ThemeManager,
    onNavigateBack: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToUpdater: () -> Unit
) {
    val selectedTheme by themeManager.themeMode.collectAsState()
    var selectedLanguage by remember { mutableStateOf(LanguageOption.ENGLISH) }
    var scanRemindersEnabled by remember { mutableStateOf(true) }
    var tipsEnabled by remember { mutableStateOf(true) }

    val updateInfo by updateManager.updateInfo.collectAsState()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Settings & Preferences",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. APPEARANCE SECTION
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Theme & Appearance",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ThemeSegment(
                                label = "Light",
                                icon = Icons.Default.LightMode,
                                isSelected = selectedTheme == ThemeMode.LIGHT,
                                onClick = { themeManager.setThemeMode(ThemeMode.LIGHT) },
                                modifier = Modifier.weight(1f)
                            )
                            ThemeSegment(
                                label = "Dark",
                                icon = Icons.Default.DarkMode,
                                isSelected = selectedTheme == ThemeMode.DARK,
                                onClick = { themeManager.setThemeMode(ThemeMode.DARK) },
                                modifier = Modifier.weight(1f)
                            )
                            ThemeSegment(
                                label = "System",
                                icon = Icons.Default.SettingsBrightness,
                                isSelected = selectedTheme == ThemeMode.SYSTEM,
                                onClick = { themeManager.setThemeMode(ThemeMode.SYSTEM) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 2. UPDATER PROMINENT CARD
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .clickable { onNavigateToUpdater() },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (updateInfo.hasUpdate) SolarGold.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (updateInfo.hasUpdate) SolarGold.copy(alpha = 0.15f)
                                        else EmeraldPrimary.copy(alpha = 0.15f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SystemUpdateAlt,
                                    contentDescription = null,
                                    tint = if (updateInfo.hasUpdate) SolarGold else EmeraldPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Software Updates",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (updateInfo.hasUpdate) "New version ${updateInfo.latestVersion} available" else "Fasal Drishti is up to date",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (updateInfo.hasUpdate) SolarGold else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                        fontSize = 12.sp,
                                        fontWeight = if (updateInfo.hasUpdate) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // 3. HARDWARE ACCELERATION & AI DIAGNOSTICS
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "AI Engine & Diagnostics",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        SettingsSwitchTile(
                            icon = Icons.Default.Memory,
                            title = "NNAPI Neural Accelerator",
                            subtitle = "Hardware inference delegate on mobile NPU",
                            checked = true,
                            onCheckedChange = {}
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 10.dp))

                        SettingsSwitchTile(
                            icon = Icons.Default.Speed,
                            title = "120Hz Fluid UI Lock",
                            subtitle = "Locks display refresh rate to 120 FPS",
                            checked = true,
                            onCheckedChange = {}
                        )
                    }
                }
            }

            // 4. NOTIFICATIONS
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Notifications & Reminders",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        SettingsSwitchTile(
                            icon = Icons.Default.NotificationsActive,
                            title = "Spray & Crop Health Alerts",
                            subtitle = "Weekly monitoring reminders",
                            checked = scanRemindersEnabled,
                            onCheckedChange = { scanRemindersEnabled = it }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 10.dp))

                        SettingsSwitchTile(
                            icon = Icons.Default.Lightbulb,
                            title = "Agronomy Tips & Tricks",
                            subtitle = "Weather-optimized crop advisories",
                            checked = tipsEnabled,
                            onCheckedChange = { tipsEnabled = it }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeSegment(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
        shadowElevation = if (isSelected) 3.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )
        }
    }
}

@Composable
private fun SettingsSwitchTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(EmeraldPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        fontSize = 11.5.sp
                    )
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = EmeraldPrimary
            )
        )
    }
}
