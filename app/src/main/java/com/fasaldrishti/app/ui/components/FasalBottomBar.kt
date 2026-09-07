package com.fasaldrishti.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fasaldrishti.app.ui.navigation.Screen
import com.fasaldrishti.app.ui.theme.GreenPrimary
import com.fasaldrishti.app.ui.theme.PrimaryGradientEnd
import com.fasaldrishti.app.ui.theme.PrimaryGradientStart

@Composable
fun FasalBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onScanClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Bottom bar surface
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 16.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    icon = Icons.Default.Home,
                    label = "Home",
                    selected = currentRoute == Screen.Home.route,
                    onClick = { onNavigate(Screen.Home.route) }
                )

                BottomNavItem(
                    icon = Icons.Default.History,
                    label = "History",
                    selected = currentRoute == Screen.History.route,
                    onClick = { onNavigate(Screen.History.route) }
                )

                // Spacer for Center elevated FAB
                Spacer(modifier = Modifier.width(56.dp))

                BottomNavItem(
                    icon = Icons.Default.ChatBubbleOutline,
                    label = "Fasal AI",
                    selected = currentRoute == Screen.Chat.route,
                    onClick = { onNavigate(Screen.Chat.route) }
                )

                BottomNavItem(
                    icon = Icons.Default.Person,
                    label = "Profile",
                    selected = currentRoute == Screen.Profile.route,
                    onClick = { onNavigate(Screen.Profile.route) }
                )
            }
        }

        // Center Elevated Scan FAB
        Box(
            modifier = Modifier
                .offset(y = (-20).dp)
                .size(62.dp)
                .shadow(12.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(PrimaryGradientStart, PrimaryGradientEnd)
                    )
                )
                .clickable { onScanClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Scan Crop",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val animatedColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        label = "NavColor"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = animatedColor,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                color = animatedColor
            )
        )
    }
}
