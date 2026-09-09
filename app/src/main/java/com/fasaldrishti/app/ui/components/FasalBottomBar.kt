package com.fasaldrishti.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fasaldrishti.app.ui.navigation.Screen
import com.fasaldrishti.app.ui.theme.EmeraldPrimary
import com.fasaldrishti.app.ui.theme.EmeraldGradientEnd
import com.fasaldrishti.app.ui.theme.EmeraldGradientStart

@Composable
fun FasalBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onScanClick: () -> Unit
) {
    // Pulse animation for scan button
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fabPulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Floating Frosted Glass Capsule
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(34.dp)
                ),
            shape = RoundedCornerShape(34.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            shadowElevation = 18.dp,
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val strings = com.fasaldrishti.app.ui.localization.LocalAppStrings.current
                BottomNavItem(
                    icon = Icons.Default.Home,
                    label = strings.navHome,
                    selected = currentRoute == Screen.Home.route,
                    onClick = { onNavigate(Screen.Home.route) },
                    modifier = Modifier.weight(1f)
                )

                BottomNavItem(
                    icon = Icons.Default.History,
                    label = strings.navHistory,
                    selected = currentRoute == Screen.History.route,
                    onClick = { onNavigate(Screen.History.route) },
                    modifier = Modifier.weight(1f)
                )

                // Placeholder for Center Pulse FAB
                Spacer(modifier = Modifier.width(68.dp))

                BottomNavItem(
                    icon = Icons.Default.ChatBubbleOutline,
                    label = strings.navDoctor,
                    selected = currentRoute == Screen.Chat.route || currentRoute.startsWith("chat"),
                    onClick = { onNavigate(Screen.Chat.createRoute()) },
                    modifier = Modifier.weight(1f)
                )

                BottomNavItem(
                    icon = Icons.Default.Person,
                    label = strings.navProfile,
                    selected = currentRoute == Screen.Profile.route,
                    onClick = { onNavigate(Screen.Profile.route) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Center Floating Glowing Bio-Scan FAB
        Box(
            modifier = Modifier
                .offset(y = (-14).dp)
                .scale(pulseScale)
                .size(64.dp)
                .shadow(16.dp, CircleShape, spotColor = EmeraldPrimary)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(EmeraldGradientStart, EmeraldGradientEnd)
                    )
                )
                .border(2.5.dp, Color.White.copy(alpha = 0.85f), CircleShape)
                .clickable { onScanClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CenterFocusStrong,
                contentDescription = "Scan Crop",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedTint by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
        label = "NavColor"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    else Color.Transparent
                )
                .padding(horizontal = 14.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = animatedTint,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = animatedTint
            )
        )
    }
}
