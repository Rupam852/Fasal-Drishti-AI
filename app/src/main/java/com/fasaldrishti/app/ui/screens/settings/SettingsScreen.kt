package com.fasaldrishti.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.fasaldrishti.app.data.remote.UpdateManager
import com.fasaldrishti.app.ui.theme.AmberAccent
import com.fasaldrishti.app.ui.theme.RedSevere

enum class ThemeOption { LIGHT, DARK, SYSTEM }
enum class LanguageOption { ENGLISH, HINDI }

@Composable
fun SettingsScreen(
    updateManager: UpdateManager,
    onNavigateBack: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToUpdater: () -> Unit
) {
    var selectedTheme by remember { mutableStateOf(ThemeOption.SYSTEM) }
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
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Appearance Theme Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Appearance",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Segmented Control
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
                                isSelected = selectedTheme == ThemeOption.LIGHT,
                                onClick = { selectedTheme = ThemeOption.LIGHT },
                                modifier = Modifier.weight(1f)
                            )
                            ThemeSegment(
                                label = "Dark",
                                icon = Icons.Default.DarkMode,
                                isSelected = selectedTheme == ThemeOption.DARK,
                                onClick = { selectedTheme = ThemeOption.DARK },
                                modifier = Modifier.weight(1f)
                            )
                            ThemeSegment(
                                label = "System",
                                icon = Icons.Default.SettingsBrightness,
                                isSelected = selectedTheme == ThemeOption.SYSTEM,
                                onClick = { selectedTheme = ThemeOption.SYSTEM },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Language Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Language / भाषा",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedLanguage = LanguageOption.ENGLISH }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("English", style = MaterialTheme.typography.bodyMedium)
                            RadioButton(
                                selected = selectedLanguage == LanguageOption.ENGLISH,
                                onClick = { selectedLanguage = LanguageOption.ENGLISH }
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedLanguage = LanguageOption.HINDI }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("हिंदी (Hindi)", style = MaterialTheme.typography.bodyMedium)
                            RadioButton(
                                selected = selectedLanguage == LanguageOption.HINDI,
                                onClick = { selectedLanguage = LanguageOption.HINDI }
                            )
                        }
                    }
                }
            }

            // Notifications Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Notifications",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Weekly Scan Reminders", style = MaterialTheme.typography.bodyMedium)
                                Text("Alerts to inspect high-risk fields", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)))
                            }
                            Switch(
                                checked = scanRemindersEnabled,
                                onCheckedChange = { scanRemindersEnabled = it }
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Agronomy & Weather Tips", style = MaterialTheme.typography.bodyMedium)
                                Text("Seasonal spray guidelines", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)))
                            }
                            Switch(
                                checked = tipsEnabled,
                                onCheckedChange = { tipsEnabled = it }
                            )
                        }
                    }
                }
            }

            // Updater & About Action Cards
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        // App Updater Item with Red Dot Indicator
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToUpdater() }
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box {
                                    Icon(
                                        imageVector = Icons.Default.SystemUpdateAlt,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    // Small Red Notification Dot if update is available
                                    if (updateInfo.hasUpdate) {
                                        Box(
                                            modifier = Modifier
                                                .size(9.dp)
                                                .align(Alignment.TopEnd)
                                                .clip(CircleShape)
                                                .background(RedSevere)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "App Updater",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                    )
                                    Text(
                                        text = if (updateInfo.hasUpdate) "Update available: v${updateInfo.latestVersion}" else "App is up to date (v${updateInfo.currentVersion})",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (updateInfo.hasUpdate) AmberAccent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            fontWeight = if (updateInfo.hasUpdate) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )

                        // About Fasal Drishti Item
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToAbout() }
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = "About Fasal Drishti & Contributors",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
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
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
