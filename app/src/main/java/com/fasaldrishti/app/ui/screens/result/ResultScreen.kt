package com.fasaldrishti.app.ui.screens.result

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Diagnosis Dossier",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scan?.let {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Fasal Drishti Diagnosis Dossier:\nCondition: ${it.diseaseName}\nSeverity: ${it.severity}\nConfidence: ${(it.confidence * 100).toInt()}%"
                                )
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Diagnosis"))
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = EmeraldPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onScanAgain,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Rescan", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    }

                    GradientButton(
                        text = "Consult AI Doctor",
                        onClick = { scan?.let { onNavigateToChat(it.diseaseName) } },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(52.dp),
                        icon = Icons.Default.Chat
                    )
                }
            }
        }
    ) { innerPadding ->
        if (scan == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = EmeraldPrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // 1. HERO LEAF IMAGE WITH GLOW OVERLAY
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(290.dp)
                            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    ) {
                        if (!scan.imageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = scan.imageUrl,
                                contentDescription = scan.diseaseName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(ObsidianVoid),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Eco,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }

                        // Gradient Scrim Overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.65f),
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.75f)
                                        )
                                    )
                                )
                        )

                        // Top Floating Back Button
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .statusBarsPadding()
                                .padding(16.dp)
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                                .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }
                }

                val isInvalidCrop = scan.predictedClass == "Invalid_Crop" || scan.severity.equals("Invalid", ignoreCase = true) || scan.confidence < 0.50f

                if (isInvalidCrop) {
                    // INVALID SUBJECT / NON-CROP GUIDANCE VIEW
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .shadow(6.dp, RoundedCornerShape(26.dp)),
                            shape = RoundedCornerShape(26.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, CrimsonCoral.copy(alpha = 0.6f))
                        ) {
                            Column(modifier = Modifier.padding(22.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(CrimsonCoral.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.WarningAmber,
                                            contentDescription = null,
                                            tint = CrimsonCoral,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = "No Plant Leaf Detected",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 17.sp,
                                                color = CrimsonCoral
                                            )
                                        )
                                        Text(
                                            text = "Low Confidence Match",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "AI vision did not detect a recognized crop leaf with sufficient confidence (${(scan.confidence * 100).toInt()}%). The image may contain a non-crop object, person, animal, vehicle, or is blurry.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                        fontSize = 13.5.sp,
                                        lineHeight = 20.sp
                                    )
                                )
                            }
                        }
                    }

                    // PHOTOGRAPHY & SCANNING TIPS FOR FARMERS
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "💡 Photography Guidelines for Clear Scan",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary
                                    )
                                )

                                Row(verticalAlignment = Alignment.Top) {
                                    Text("🌿", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Center the leaf inside the camera reticle box.",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp)
                                    )
                                }

                                Row(verticalAlignment = Alignment.Top) {
                                    Text("☀️", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Ensure bright daylight or turn on camera flash in dim light.",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp)
                                    )
                                }

                                Row(verticalAlignment = Alignment.Top) {
                                    Text("🔍", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Take close-up photos of visible spots or discoloration, avoid distant shots.",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // 2. DIAGNOSTIC DOSSIER HEADER
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .shadow(6.dp, RoundedCornerShape(26.dp)),
                            shape = RoundedCornerShape(26.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(22.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    SeverityBadge(severity = scan.severity)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = scan.diseaseName,
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 21.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Diagnosed on: ${scan.timestamp}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                            fontSize = 12.sp
                                        )
                                    )
                                }

                                ConfidenceRing(
                                    confidence = scan.confidence,
                                    size = 84.dp,
                                    strokeWidth = 8.dp
                                )
                            }
                        }
                    }

                    // 3. ASK AI KRISHI DOCTOR BANNER (NVIDIA NIM)
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = EmeraldPrimary.copy(alpha = 0.35f))
                                .clip(RoundedCornerShape(24.dp))
                                .clickable {
                                    val contextMsg = "Condition: ${scan.diseaseName} (${(scan.confidence * 100).toInt()}% confidence, Severity: ${scan.severity})"
                                    onNavigateToChat(contextMsg)
                                },
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldPrimary.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(
                                                brush = Brush.radialGradient(listOf(EmeraldPrimary, EmeraldDark))
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Psychology,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = "Consult AI Agri Doctor",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                        Text(
                                            text = "Get exact spray doses & custom remedies",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.ChatBubble,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    // 3.5 ONE-TAP PDF PRESCRIPTION & WHATSAPP SHARE CARD
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .clickable {
                                    val pdfFile = com.fasaldrishti.app.util.PdfPrescriptionGenerator.generatePrescriptionPdf(context, scan)
                                    if (pdfFile != null) {
                                        com.fasaldrishti.app.util.PdfPrescriptionGenerator.sharePrescription(context, pdfFile)
                                    } else {
                                        android.widget.Toast.makeText(context, "Failed to generate PDF", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, EmeraldPrimary.copy(alpha = 0.5f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(EmeraldPrimary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PictureAsPdf,
                                            contentDescription = null,
                                            tint = EmeraldPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = "Download PDF Prescription 📄",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                        Text(
                                            text = "Share with Agri Shop or on WhatsApp",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = EmeraldPrimary
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Share",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black,
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. ACTIONABLE ACCORDION DOSSIERS
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            AccordionCard(
                                title = "Disease Symptoms & Identification",
                                icon = Icons.Default.Coronavirus,
                                content = scan.symptoms.ifBlank { diseaseInfo?.symptoms ?: "Discolored lesions, leaf necrosis, or mold patches observed on leaf surfaces." },
                                initiallyExpanded = true,
                                accentColor = CrimsonCoral
                            )

                            AccordionCard(
                                title = "Chemical Fungicide & Spray Dosages",
                                icon = Icons.Default.Science,
                                content = scan.treatment.ifBlank { diseaseInfo?.treatment ?: "Spray recommended copper-based or systemic fungicide (e.g. Mancozeb 75% WP @ 2.5g/L water)." },
                                initiallyExpanded = true,
                                accentColor = SolarGold
                            )

                            AccordionCard(
                                title = "Organic / Desi Remedies & Prevention",
                                icon = Icons.Default.Shield,
                                content = diseaseInfo?.prevention ?: "Spray 5% Neem oil extract, prune and destroy severely infected leaves, and maintain proper crop spacing.",
                                initiallyExpanded = false,
                                accentColor = EmeraldPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
