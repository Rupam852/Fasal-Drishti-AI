package com.fasaldrishti.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fasaldrishti.app.data.remote.UpdateManager
import com.fasaldrishti.app.ui.components.StatChip
import com.fasaldrishti.app.ui.screens.auth.AuthViewModel
import com.fasaldrishti.app.ui.theme.AmberAccent
import com.fasaldrishti.app.ui.theme.GreenPrimary
import com.fasaldrishti.app.ui.theme.RedSevere

data class AchievementBadge(
    val title: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    updateManager: UpdateManager,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onLogout: () -> Unit
) {
    val authState by authViewModel.uiState.collectAsState()
    val user = authState.user
    val updateInfo by updateManager.updateInfo.collectAsState()

    val badges = listOf(
        AchievementBadge("First Scan", Icons.Default.CameraAlt, GreenPrimary),
        AchievementBadge("Green Guardian", Icons.Default.Shield, Color(0xFF00897B)),
        AchievementBadge("7-Day Streak", Icons.Default.LocalFireDepartment, AmberAccent),
        AchievementBadge("AI Pioneer", Icons.Default.Psychology, Color(0xFF5E35B1))
    )

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Profile",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                )

                Box {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    // Red Notification Dot on Settings Icon if update is available
                    if (updateInfo.hasUpdate) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = (-2).dp, y = (2).dp)
                                .clip(CircleShape)
                                .background(RedSevere)
                        )
                    }
                }
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
            // User Avatar & Name Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
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
                                .size(90.dp)
                                .clip(CircleShape)
                                .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(54.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = user?.name ?: "Ramesh Kumar",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        )

                        Text(
                            text = user?.email ?: "ramesh.farmer@example.com",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        )
                    }
                }
            }

            // Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatChip(
                        title = "Total Scans",
                        value = "${user?.totalScans ?: 12}",
                        icon = Icons.Default.Analytics,
                        iconTint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatChip(
                        title = "Healthy",
                        value = "${user?.healthyCount ?: 8}",
                        icon = Icons.Default.CheckCircle,
                        iconTint = GreenPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatChip(
                        title = "Diseased",
                        value = "${user?.diseasedCount ?: 4}",
                        icon = Icons.Default.Warning,
                        iconTint = AmberAccent,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Achievement Badges Section
            item {
                Column {
                    Text(
                        text = "Achievements",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(badges) { badge ->
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(badge.color.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = badge.icon,
                                            contentDescription = null,
                                            tint = badge.color,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = badge.title,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Navigation Actions Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        ProfileMenuItem(
                            title = "My Scan History",
                            icon = Icons.Default.History,
                            onClick = onNavigateToHistory
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )
                        ProfileMenuItem(
                            title = "Settings & Appearance",
                            icon = Icons.Default.Settings,
                            onClick = onNavigateToSettings,
                            showBadge = updateInfo.hasUpdate
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )
                        ProfileMenuItem(
                            title = "About Fasal Drishti",
                            icon = Icons.Default.Info,
                            onClick = onNavigateToAbout
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )
                        ProfileMenuItem(
                            title = "Logout",
                            icon = Icons.Default.Logout,
                            textColor = RedSevere,
                            iconColor = RedSevere,
                            onClick = onLogout
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileMenuItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    showBadge: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
                if (showBadge) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .align(Alignment.TopEnd)
                            .clip(CircleShape)
                            .background(RedSevere)
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
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
