package com.fasaldrishti.app.ui.screens.updater

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fasaldrishti.app.data.remote.UpdateManager
import com.fasaldrishti.app.ui.components.GradientButton
import com.fasaldrishti.app.ui.theme.AmberAccent
import com.fasaldrishti.app.ui.theme.GreenPrimary
import com.fasaldrishti.app.ui.theme.RedSevere
import kotlinx.coroutines.launch

@Composable
fun UpdaterScreen(
    updateManager: UpdateManager,
    onNavigateBack: () -> Unit
) {
    val updateInfo by updateManager.updateInfo.collectAsState()
    val autoCheckEnabled by updateManager.autoCheckEnabled.collectAsState()
    val coroutineScope = rememberCoroutineScope()

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
                    text = "App Updater",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
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
            // Version Overview Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(
                                    if (updateInfo.hasUpdate) AmberAccent.copy(alpha = 0.15f)
                                    else GreenPrimary.copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (updateInfo.hasUpdate) Icons.Default.SystemUpdateAlt else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (updateInfo.hasUpdate) AmberAccent else GreenPrimary,
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (updateInfo.hasUpdate) "Update Available!" else "Fasal Drishti is Up to Date",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Installed Version: v${updateInfo.currentVersion} ${if (updateInfo.hasUpdate) "→ Latest: v${updateInfo.latestVersion}" else ""}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        )
                    }
                }
            }

            // Auto-check on App Open Toggle Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-Check for Updates",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Automatically checks when opening app & sends notification",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            )
                        }
                        Switch(
                            checked = autoCheckEnabled,
                            onCheckedChange = { updateManager.setAutoCheckEnabled(it) }
                        )
                    }
                }
            }

            // Release Notes Card if update is available
            if (updateInfo.hasUpdate) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.NewReleases,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "What's New in v${updateInfo.latestVersion}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = updateInfo.releaseNotes,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    lineHeight = 22.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                                )
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Direct Download Button
                            GradientButton(
                                text = "Download & Install Update",
                                icon = Icons.Default.Download,
                                onClick = {
                                    updateManager.openDownloadUrl(updateInfo.downloadUrl)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Manual Check Button
            item {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            updateManager.checkForUpdates(isAutoCheck = false)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    enabled = !updateInfo.isChecking
                ) {
                    if (updateInfo.isChecking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Checking server...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Check for Updates Now",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
