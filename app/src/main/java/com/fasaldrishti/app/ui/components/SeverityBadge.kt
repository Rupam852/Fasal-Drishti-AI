package com.fasaldrishti.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fasaldrishti.app.ui.theme.*

@Composable
fun SeverityBadge(
    severity: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (severity.lowercase()) {
        "severe" -> RedSevereContainer to RedSevere
        "moderate" -> AmberContainer to AmberAccent
        "none", "healthy" -> GreenPrimaryContainer to GreenPrimary
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = severity,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = textColor
            )
        )
    }
}
