package com.fasaldrishti.app.ui.screens.tools

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fasaldrishti.app.ui.theme.*

data class GovtScheme(
    val title: String,
    val ministryInfo: String,
    val tag: String,
    val benefitHighlight: String,
    val description: String,
    val eligibility: List<String>,
    val requiredDocs: List<String>,
    val officialUrl: String,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GovtSchemesScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("All") }

    val schemes = remember {
        listOf(
            GovtScheme(
                title = "PM-Kisan Samman Nidhi",
                ministryInfo = "Ministry of Agriculture & Farmers Welfare, GoI",
                tag = "Financial Aid",
                benefitHighlight = "₹6,000 / Year DBT (3 equal installments of ₹2,000 directly in bank account)",
                description = "Central Sector Scheme providing income support to all landholding farmer families across India for purchasing agricultural inputs and domestic needs.",
                eligibility = listOf(
                    "All small and marginal landholding farmer families.",
                    "Land ownership in state revenue records (Khatian).",
                    "Mandatory eKYC & Aadhaar-linked active bank account."
                ),
                requiredDocs = listOf("Aadhaar Card", "Land Khata/Khatian Details", "Bank Account Passbook", "Mobile Number linked to Aadhaar"),
                officialUrl = "https://pmkisan.gov.in",
                icon = Icons.Default.CurrencyRupee,
                color = EmeraldPrimary
            ),
            GovtScheme(
                title = "PM Fasal Bima Yojana (PMFBY)",
                ministryInfo = "Department of Agriculture & Farmers Welfare",
                tag = "Insurance",
                benefitHighlight = "Comprehensive crop loss protection with only 1.5% - 2% farmer premium",
                description = "Provides financial support to farmers suffering crop loss/damage arising out of unforeseen natural calamities, pest attacks, hail, drought, and post-harvest unseasonal rains.",
                eligibility = listOf(
                    "All farmers including sharecroppers and tenant farmers growing notified crops.",
                    "Available for both loanee and non-loanee farmers.",
                    "Intimation of crop loss within 72 hours of disaster."
                ),
                requiredDocs = listOf("Land Record / Sowing Certificate", "Aadhaar Card", "Bank Passbook", "Sowing Declaration"),
                officialUrl = "https://pmfby.gov.in",
                icon = Icons.Default.Shield,
                color = Color(0xFF00E5FF)
            ),
            GovtScheme(
                title = "PM-KUSUM Solar Pump Scheme",
                ministryInfo = "Ministry of New and Renewable Energy (MNRE)",
                tag = "Solar & Energy",
                benefitHighlight = "Up to 60% Govt Subsidy on standalone Solar Irrigation Pumps",
                description = "Enables farmers to install off-grid solar agriculture pumps for zero-electricity diesel-free day-time irrigation and earn extra income by selling surplus solar power to the grid.",
                eligibility = listOf(
                    "Individual farmers, group of farmers, FPOs, and water user associations.",
                    "Agricultural land with tubewell/borewell/dugwell facility.",
                    "No existing electric agricultural connection required."
                ),
                requiredDocs = listOf("Land Mutation/Parcha", "Aadhaar Card", "Bank Details", "Water Source Certificate"),
                officialUrl = "https://pmkusum.mnre.gov.in",
                icon = Icons.Default.WbSunny,
                color = SolarGold
            ),
            GovtScheme(
                title = "Kisan Credit Card (KCC)",
                ministryInfo = "Reserve Bank of India (RBI) & NABARD",
                tag = "Loans & Subsidies",
                benefitHighlight = "Loans up to ₹3 Lakh at effectively 4% interest per annum with prompt repayment",
                description = "Simplifies access to short-term formal institutional credit for cultivation, post-harvest expenses, farm asset maintenance, and dairy/fisheries activities.",
                eligibility = listOf(
                    "All farmers - individual/joint borrowers, tenant farmers, oral lessees & SHGs.",
                    "No collateral required for loans up to ₹1.60 Lakh."
                ),
                requiredDocs = listOf("Application Form", "Identity & Address Proof (Aadhaar/Voter ID)", "Land Records", "Recent Passport Photo"),
                officialUrl = "https://myscheme.gov.in/schemes/kcc",
                icon = Icons.Default.CreditCard,
                color = Color(0xFF818CF8)
            ),
            GovtScheme(
                title = "Soil Health Card Scheme",
                ministryInfo = "Department of Agriculture & Cooperation",
                tag = "Soil & Seeds",
                benefitHighlight = "Free scientific 12-parameter soil testing & customized fertilizer advice",
                description = "Provides farmers with crop-wise nutrient status (N, P, K, pH, Zinc, Iron, Organic Carbon) and dosage recommendations to stop overuse of chemical fertilizers.",
                eligibility = listOf(
                    "Every farmer in India is entitled to get a free soil health card renewed every 2-3 years."
                ),
                requiredDocs = listOf("Soil Sample from Farm", "Farmer Aadhaar & Land Survey Number"),
                officialUrl = "https://soilhealth.dac.gov.in",
                icon = Icons.Default.Grass,
                color = NeonLime
            ),
            GovtScheme(
                title = "SMAM (Agri Machinery Subsidy)",
                ministryInfo = "Mechanization & Technology Division, MoA&FW",
                tag = "Loans & Subsidies",
                benefitHighlight = "40% to 50% subsidy on Tractors, Power Tillers, Rotavators & Sprayers",
                description = "Promotes farm mechanization among small and marginal farmers with direct subsidy disbursement through DBT portals of state agriculture departments.",
                eligibility = listOf(
                    "Small, marginal and women farmers given high priority.",
                    "Farmer must not have availed machinery subsidy in previous 3 years."
                ),
                requiredDocs = listOf("Land Record (7/12 / Parcha)", "Aadhaar & Bank Account", "Quotation from Authorized Dealer"),
                officialUrl = "https://agrimachinery.nic.in",
                icon = Icons.Default.Agriculture,
                color = Color(0xFFF97316)
            )
        )
    }

    val categories = listOf("All", "Financial Aid", "Insurance", "Solar & Energy", "Loans & Subsidies", "Soil & Seeds")

    val filteredSchemes = schemes.filter {
        selectedCategory == "All" || it.tag.equals(selectedCategory, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onNavigateBack,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Sarkari Krishi Yojanaen",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                    )
                    Text(
                        text = "Direct Benefits, Subsidies & Official Portals Guide",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 11.5.sp
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. BANNER CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        EmeraldPrimary.copy(alpha = 0.18f),
                                        Color(0xFF00E5FF).copy(alpha = 0.08f)
                                    )
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = EmeraldPrimary,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Direct Benefit Schemes (DBT)",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Explore verified government agricultural programs, subsidy calculation, required paperwork, and navigate directly to official portals.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                            )
                        }
                    }
                }
            }

            // 2. CATEGORY FILTER CHIPS
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = category == selectedCategory
                        Surface(
                            onClick = { selectedCategory = category },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                            )
                        ) {
                            Text(
                                text = category,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            }

            // 3. SCHEMES LIST
            items(filteredSchemes) { scheme ->
                SchemeCard(
                    scheme = scheme,
                    onOpenUrl = { url ->
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                )
            }
        }
    }
}

@Composable
private fun SchemeCard(
    scheme: GovtScheme,
    onOpenUrl: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Icon + Title + Category Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = scheme.color.copy(alpha = 0.15f),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = scheme.icon,
                            contentDescription = null,
                            tint = scheme.color,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = scheme.color.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = scheme.tag,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = scheme.color,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = scheme.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.5.sp
                        )
                    )
                    Text(
                        text = scheme.ministryInfo,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 11.5.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Benefit Highlight Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = scheme.color.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, scheme.color.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = scheme.color,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = scheme.benefitHighlight,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = scheme.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            )

            // Collapsible Details: Eligibility & Documents
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "📋 Key Eligibility Criteria:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    scheme.eligibility.forEach { item ->
                        Text(
                            text = "• $item",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "📑 Required Documents:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    scheme.requiredDocs.forEach { doc ->
                        Text(
                            text = "✓ $doc",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = EmeraldPrimary,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Row: Toggle Details & Official Portal Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { isExpanded = !isExpanded }
                ) {
                    Text(
                        text = if (isExpanded) "Hide Details ▲" else "View Eligibility & Docs ▼",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp
                        )
                    )
                }

                Button(
                    onClick = { onOpenUrl(scheme.officialUrl) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = scheme.color),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Official Portal",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
