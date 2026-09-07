package com.fasaldrishti.app.ui.screens.result

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fasaldrishti.app.ui.components.AccordionCard
import com.fasaldrishti.app.ui.components.ConfidenceRing
import com.fasaldrishti.app.ui.components.GradientButton
import com.fasaldrishti.app.ui.components.SeverityBadge
import com.fasaldrishti.app.ui.theme.*

@Composable
fun ResultScreen(
    scanId: String,
    viewModel: ResultViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    onScanAgain: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(scanId) {
        viewModel.loadScanResult(scanId)
    }

    val scan = uiState.scanRecord
    val diseaseInfo = uiState.diseaseInfo

    val severityColor = when (scan?.severity?.lowercase()) {
        "severe" -> RedSevere
        "moderate" -> AmberAccent
        "none", "healthy" -> GreenPrimary
        else -> GreenPrimary
    }

    Scaffold(
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 16.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Share Button
                    OutlinedButton(
                        onClick = {
                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Fasal Drishti Diagnosis:\nCrop: ${scan?.cropName}\nDisease: ${scan?.diseaseName}\nConfidence: ${(scan?.confidence ?: 0f) * 100}%\nTreatment: ${scan?.treatment}"
                                )
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Diagnosis"))
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Scan Again Primary CTA Button
                    GradientButton(
                        text = "Scan Again",
                        icon = Icons.Default.CameraAlt,
                        onClick = onScanAgain,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { innerPadding ->
        if (scan == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Hero Image with dynamic colored glow border
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                            .border(
                                width = 3.dp,
                                color = severityColor.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                            )
                    ) {
                        AsyncImage(
                            model = scan.imageUrl,
                            contentDescription = scan.diseaseName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Top Left Back Button Overlay
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .statusBarsPadding()
                                .padding(16.dp)
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Disease Header & Confidence Section
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                SeverityBadge(severity = scan.severity)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = scan.diseaseName,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp
                                    )
                                )
                                Text(
                                    text = "Crop: ${scan.cropName}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                )
                            }

                            ConfidenceRing(
                                confidence = scan.confidence,
                                size = 76.dp,
                                strokeWidth = 7.dp
                            )
                        }
                    }
                }

                // Ask AI CTA Banner Button
                item {
                    OutlinedButton(
                        onClick = {
                            val contextMsg = "Crop: ${scan.cropName}, Disease: ${scan.diseaseName} (${(scan.confidence * 100).toInt()}% confidence)"
                            onNavigateToChat(contextMsg)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .height(54.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Ask AI for more details",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            )
                        }
                    }
                }

                // Expandable Accordion Cards (Symptoms, Treatment, Prevention)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AccordionCard(
                            title = "Symptoms",
                            icon = Icons.Default.Coronavirus,
                            content = scan.symptoms.ifBlank { diseaseInfo?.symptoms ?: "Water-soaked lesions on leaf surfaces." },
                            initiallyExpanded = true
                        )

                        AccordionCard(
                            title = "Treatment Recommendations",
                            icon = Icons.Default.MedicalServices,
                            content = scan.treatment.ifBlank { diseaseInfo?.treatment ?: "Apply approved fungicide." },
                            initiallyExpanded = true
                        )

                        AccordionCard(
                            title = "Prevention Tips",
                            icon = Icons.Default.Shield,
                            content = diseaseInfo?.prevention ?: "Ensure good aeration between plants, crop rotation, and avoid overhead sprinkler watering.",
                            initiallyExpanded = false
                        )
                    }
                }
            }
        }
    }
}
