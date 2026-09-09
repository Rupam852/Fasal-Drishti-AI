package com.fasaldrishti.app.ui.screens.tools

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fasaldrishti.app.ui.theme.*

data class CropNutrientProfile(
    val name: String,
    val category: String,
    val icon: String,
    val ureaPerAcre: Double,
    val dapPerAcre: Double,
    val mopPerAcre: Double,
    val zincPerAcre: Double,
    val waterLitrePerAcre: Double,
    val organicFymTons: Double
)

enum class LandUnit(val label: String, val toAcreFactor: Double) {
    ACRE("Acre", 1.0),
    BIGHA("Bigha", 0.33), // Standard regional average
    HECTARE("Hectare", 2.47),
    GUNTHA("Guntha", 0.025)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FertilizerCalculatorScreen(
    onNavigateBack: () -> Unit
) {
    val crops = remember {
        listOf(
            CropNutrientProfile("Paddy (Rice)", "Cereal Crop", "🌾", 55.0, 35.0, 20.0, 5.0, 150.0, 2.0),
            CropNutrientProfile("Wheat", "Cereal Grain", "🌾", 60.0, 40.0, 20.0, 5.0, 140.0, 2.0),
            CropNutrientProfile("Potato", "Tuber Vegetable", "🥔", 75.0, 50.0, 45.0, 6.0, 180.0, 3.5),
            CropNutrientProfile("Tomato", "Solanaceous", "🍅", 65.0, 45.0, 35.0, 5.0, 160.0, 3.0),
            CropNutrientProfile("Cotton", "Commercial Fiber", "🌱", 50.0, 30.0, 25.0, 5.0, 150.0, 2.0),
            CropNutrientProfile("Mustard", "Oilseed Crop", "🌼", 45.0, 30.0, 15.0, 8.0, 120.0, 1.5),
            CropNutrientProfile("Sugarcane", "Cash Crop", "🎋", 110.0, 60.0, 50.0, 10.0, 250.0, 5.0),
            CropNutrientProfile("Maize (Corn)", "Coarse Grain", "🌽", 60.0, 35.0, 25.0, 5.0, 150.0, 2.0),
            CropNutrientProfile("Onion", "Bulb Crop", "🧅", 50.0, 40.0, 35.0, 4.0, 140.0, 2.5),
            CropNutrientProfile("Chilli", "Spice & Vegetable", "🌶️", 55.0, 40.0, 30.0, 5.0, 150.0, 2.0)
        )
    }

    var selectedCrop by remember { mutableStateOf(crops[0]) }
    var selectedUnit by remember { mutableStateOf(LandUnit.ACRE) }
    var areaInput by remember { mutableStateOf("1") }

    val areaValue = areaInput.toDoubleOrNull() ?: 1.0
    val acreEquivalent = areaValue * selectedUnit.toAcreFactor

    val calculatedUrea = String.format("%.1f", selectedCrop.ureaPerAcre * acreEquivalent)
    val calculatedDap = String.format("%.1f", selectedCrop.dapPerAcre * acreEquivalent)
    val calculatedMop = String.format("%.1f", selectedCrop.mopPerAcre * acreEquivalent)
    val calculatedZinc = String.format("%.1f", selectedCrop.zincPerAcre * acreEquivalent)
    val calculatedWater = String.format("%.0f", selectedCrop.waterLitrePerAcre * acreEquivalent)
    val calculatedFym = String.format("%.1f", selectedCrop.organicFymTons * acreEquivalent)
    val context = androidx.compose.ui.platform.LocalContext.current
    val languageManager = remember { com.fasaldrishti.app.data.local.LanguageManager(context) }
    val currentAppLanguage by languageManager.currentLanguage.collectAsState()
    var showHelpDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
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
                            text = "Fertilizer & NPK Calculator",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            text = "Accurate ICAR Nutrient & Dosage Formulation",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }

                // Help Guide Button (?)
                Surface(
                    onClick = { showHelpDialog = true },
                    shape = CircleShape,
                    color = EmeraldPrimary.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.45f)),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Calculator Guide",
                            modifier = Modifier.size(20.dp),
                            tint = EmeraldPrimary
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
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. SELECT CROP CHIPS
            item {
                Text(
                    text = "1. Select Target Crop",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(crops) { crop ->
                        val isSelected = crop.name == selectedCrop.name
                        Surface(
                            onClick = { selectedCrop = crop },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                            ),
                            shadowElevation = if (isSelected) 4.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = crop.icon, fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = crop.name,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Text(
                                        text = crop.category,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            color = if (isSelected) Color.Black.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. LAND AREA & UNIT INPUT
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "2. Enter Farm Land Size",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = areaInput,
                                onValueChange = { areaInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                label = { Text("Area Size") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.SquareFoot,
                                        contentDescription = null,
                                        tint = EmeraldPrimary
                                    )
                                }
                            )

                            // Unit Selector Dropdown
                            var expanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1.3f)) {
                                Surface(
                                    onClick = { expanded = true },
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth().height(56.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = selectedUnit.label,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp
                                            ),
                                            maxLines = 1
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = null
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    LandUnit.values().forEach { unit ->
                                        DropdownMenuItem(
                                            text = { Text(unit.label) },
                                            onClick = {
                                                selectedUnit = unit
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. CALCULATED DOSAGE OUTPUT CARDS
            item {
                Text(
                    text = "3. Recommended Nutrient Dosages",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DosageCard(
                            title = "Urea",
                            value = "$calculatedUrea kg",
                            subtitle = "Nitrogen (46% N)",
                            icon = Icons.Default.Science,
                            color = Color(0xFF00E5FF),
                            modifier = Modifier.weight(1f)
                        )
                        DosageCard(
                            title = "DAP",
                            value = "$calculatedDap kg",
                            subtitle = "Phosphate (18:46:0)",
                            icon = Icons.Default.Grain,
                            color = EmeraldPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DosageCard(
                            title = "MOP / Potash",
                            value = "$calculatedMop kg",
                            subtitle = "Potassium (60% K2O)",
                            icon = Icons.Default.Spa,
                            color = SolarGold,
                            modifier = Modifier.weight(1f)
                        )
                        DosageCard(
                            title = "Zinc Sulphate",
                            value = "$calculatedZinc kg",
                            subtitle = "Micronutrient",
                            icon = Icons.Default.LocalHospital,
                            color = NeonLime,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DosageCard(
                            title = "Spray Water",
                            value = "$calculatedWater Litres",
                            subtitle = "For foliar spraying",
                            icon = Icons.Default.WaterDrop,
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.weight(1f)
                        )
                        DosageCard(
                            title = "Organic FYM",
                            value = "$calculatedFym Tons",
                            subtitle = "Compost / Bio-Manure",
                            icon = Icons.Default.Forest,
                            color = Color(0xFF10B981),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 4. AGRONOMY APPLICATION TIMELINE GUIDE
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = SolarGold,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Application Schedule",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "• Basal (At Sowing/Planting): Apply 100% DAP + 100% Potash + 33% Urea.\n• Tillering / Vegetative Stage (20-25 days): Apply 1st top-dressing with 33% Urea.\n• Flowering / Booting Stage: Apply remaining 34% Urea in moist soil conditions.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }
            }
        }
    }

    if (showHelpDialog) {
        FertilizerHelpGuideDialog(
            language = currentAppLanguage,
            onDismiss = { showHelpDialog = false }
        )
    }
}

@Composable
private fun DosageCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    ),
                    maxLines = 1
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = color
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontSize = 10.5.sp
                )
            )
        }
    }
}

private data class FertilizerHelpItem(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val accentColor: Color
)

private data class FertilizerHelpGuideContent(
    val title: String,
    val subtitle: String,
    val items: List<FertilizerHelpItem>,
    val footerNote: String,
    val closeButtonText: String
)

@Composable
private fun FertilizerHelpGuideDialog(
    language: com.fasaldrishti.app.data.local.AppLanguage,
    onDismiss: () -> Unit
) {
    val content = remember(language) { getLocalizedFertilizerHelpGuide(language) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = EmeraldPrimary.copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = content.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.5.sp
                        )
                    )
                    Text(
                        text = content.subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                    )
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(content.items) { item ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, item.accentColor.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = item.accentColor.copy(alpha = 0.15f),
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = null,
                                            tint = item.accentColor,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = item.accentColor,
                                        fontSize = 13.sp
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                                )
                            )
                        }
                    }
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = EmeraldPrimary.copy(alpha = 0.08f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = content.footerNote,
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = content.closeButtonText,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                )
            }
        }
    )
}

private fun getLocalizedFertilizerHelpGuide(language: com.fasaldrishti.app.data.local.AppLanguage): FertilizerHelpGuideContent {
    return when (language) {
        com.fasaldrishti.app.data.local.AppLanguage.HINDI -> FertilizerHelpGuideContent(
            title = "खाद एवं NPK कैलकुलेटर उपयोग गाइड",
            subtitle = "फसल अनुसार सही खाद की मात्रा और समय की जानकारी",
            items = listOf(
                FertilizerHelpItem(
                    icon = Icons.Default.Agriculture,
                    title = "1. फसल चुनें (Select Crop)",
                    description = "ऊपर दिए गए विकल्पों में से अपनी फसल (धान, गेहूं, आलू, सरसों, कपास आदि) चुनें। प्रत्येक फसल की पोषक तत्व आवश्यकता अलग होती है।",
                    accentColor = EmeraldPrimary
                ),
                FertilizerHelpItem(
                    icon = Icons.Default.SquareFoot,
                    title = "2. खेत का क्षेत्रफल डालें (Land Area)",
                    description = "अपने खेत का आकार एकड़, बीघा, हेक्टेयर या गुंठा में दर्ज करें। कैलकुलेटर अपने आप कुल खाद का हिसाब निकाल देगा।",
                    accentColor = Color(0xFF00E5FF)
                ),
                FertilizerHelpItem(
                    icon = Icons.Default.Science,
                    title = "3. प्राथमिक उर्वरक (Urea, DAP, MOP)",
                    description = "• Urea (नाइट्रोजन): पौधों के विकास व हरियाली के लिए।\n• DAP (फॉस्फोरस): मजबूत जड़ों व कल्ले फूटने के लिए।\n• MOP (पोटाश): दाना भराव, चमक व रोग प्रतिरोधक क्षमता के लिए।",
                    accentColor = SolarGold
                ),
                FertilizerHelpItem(
                    icon = Icons.Default.WaterDrop,
                    title = "4. जिंक, पानी व गोबर खाद (Zinc & FYM)",
                    description = "जिंक सल्फेट सूक्ष्म पोषक तत्वों की कमी रोकता है और गोबर खाद (FYM) मिट्टी की जैविक शक्ति बढ़ाती है।",
                    accentColor = Color(0xFF818CF8)
                )
            ),
            footerNote = "💡 बुवाई के समय DAP व पोटाश की पूरी मात्रा और यूरिया का 1/3 भाग डालें। बाकी यूरिया 2-3 बार में टॉप-ड्रेसिंग करें।",
            closeButtonText = "समझ गया (Close)"
        )

        com.fasaldrishti.app.data.local.AppLanguage.BENGALI -> FertilizerHelpGuideContent(
            title = "সার ও NPK ক্যালকুলেটর সহায়িকা",
            subtitle = "ফসলের জন্য সঠিক সারের পরিমাণ ও প্রয়োগ বিধি",
            items = listOf(
                FertilizerHelpItem(
                    icon = Icons.Default.Agriculture,
                    title = "১. ফসল নির্বাচন করুন (Select Crop)",
                    description = "তালিকা থেকে আপনার ফসল (ধান, গম, আলু, সরিষা, পাট ইত্যাদি) নির্বাচন করুন।",
                    accentColor = EmeraldPrimary
                ),
                FertilizerHelpItem(
                    icon = Icons.Default.SquareFoot,
                    title = "২. জমির পরিমাণ লিখুন (Land Area)",
                    description = "একর, বিঘা বা হেক্টরে আপনার জমির মাপ লিখুন। সঠিক সারের হিসেব স্বয়ংক্রিয়ভাবে প্রস্তুত হবে।",
                    accentColor = Color(0xFF00E5FF)
                ),
                FertilizerHelpItem(
                    icon = Icons.Default.Science,
                    title = "৩. প্রধান রাসায়নিক সার (Urea, DAP, MOP)",
                    description = "• ইউরিয়া (নাইট্রোজেন): দ্রুত বৃদ্ধি ও সবুজ পাতার জন্য।\n• ডিএপি (ফসফরাস): শিকড় বিস্তার ও কুশি তৈরির জন্য।\n• এমওপি পটাশ: দানার ওজন বৃদ্ধি ও রোগ প্রতিরোধে।",
                    accentColor = SolarGold
                ),
                FertilizerHelpItem(
                    icon = Icons.Default.WaterDrop,
                    title = "৪. জিংক ও জৈব সার (Zinc & FYM)",
                    description = "জিংক ঘাটতি প্রতিরোধ করে এবং জৈব গোবর সার মাটির উর্বরতা রক্ষা করে।",
                    accentColor = Color(0xFF818CF8)
                )
            ),
            footerNote = "💡 বপনের সময় ডিএপি ও পটাশ সম্পূর্ণ এবং ইউরিয়ার এক-তৃতীয়াংশ প্রয়োগ করুন।",
            closeButtonText = "বুঝেছি (Close)"
        )

        com.fasaldrishti.app.data.local.AppLanguage.MARATHI -> FertilizerHelpGuideContent(
            title = "खत व NPK कॅल्क्युलेटर मार्गदर्शिका",
            subtitle = "पिकासाठी खतांचे अचूक प्रमाण व वापर पद्धती",
            items = listOf(
                FertilizerHelpItem(
                    icon = Icons.Default.Agriculture,
                    title = "१. पीक निवडा (Select Crop)",
                    description = "भाता, गहू, कापूस, ऊस, कांदा इत्यादी पर्यायांमधून आपले पीक निवडा.",
                    accentColor = EmeraldPrimary
                ),
                FertilizerHelpItem(
                    icon = Icons.Default.SquareFoot,
                    title = "२. शेताचे क्षेत्र प्रविष्ट करा (Land Area)",
                    description = "एकर, गुंठा किंवा हेक्टरमध्ये शेताचे क्षेत्र प्रविष्ट करा.",
                    accentColor = Color(0xFF00E5FF)
                ),
                FertilizerHelpItem(
                    icon = Icons.Default.Science,
                    title = "३. मुख्य खते (युरिया, डीएपी, पोटॅश)",
                    description = "• युरिया: शाकीय वाढीसाठी.\n• DAP: मुळांच्या वाढीसाठी व फुटव्यांसाठी.\n• MOP (पोटॅश): दाणे भरण्यासाठी व रोगांपासून संरक्षणासाठी.",
                    accentColor = SolarGold
                ),
                FertilizerHelpItem(
                    icon = Icons.Default.WaterDrop,
                    title = "४. झिंक, पाणी व शेणखत (Zinc & FYM)",
                    description = "झिंक सूक्ष्म अन्नद्रव्यांची कमतरता दूर करते आणि शेणखताने जमिनीची सुपिकता टिकते.",
                    accentColor = Color(0xFF818CF8)
                )
            ),
            footerNote = "💡 पेरणीच्या वेळी डीएपी व पोटॅश पूर्ण द्यावे, तर युरिया २-३ हप्त्यांमध्ये विभागून द्यावा.",
            closeButtonText = "समजले (Close)"
        )

        com.fasaldrishti.app.data.local.AppLanguage.PUNJABI -> FertilizerHelpGuideContent(
            title = "ਖਾਦ ਅਤੇ NPK ਕੈਲਕੁਲੇਟਰ ਗਾਈਡ",
            subtitle = "ਫਸਲ ਲਈ ਸਹੀ ਖਾਦ ਦੀ ਮਾਤਰਾ ਅਤੇ ਵਰਤੋਂ ਦਾ ਤਰੀਕਾ",
            items = listOf(
                FertilizerHelpItem(
                    icon = Icons.Default.Agriculture,
                    title = "1. ਫ਼ਸਲ ਚੁਣੋ (Select Crop)",
                    description = "ਝੋਨਾ, ਕਣਕ, ਮੱਕੀ, ਸਰ੍ਹੋਂ ਆਦਿ ਵਿੱਚੋਂ ਆਪਣੀ ਫ਼ਸਲ ਚੁਣੋ।",
                    accentColor = EmeraldPrimary
                ),
                FertilizerHelpItem(
                    icon = Icons.Default.SquareFoot,
                    title = "2. ਜ਼ਮੀਨ ਦਾ ਰਕਬਾ ਦਰਜ ਕਰੋ (Land Area)",
                    description = "ਏਕੜ, ਬਿਘੇ ਜਾਂ ਹੈਕਟੇਅਰ ਵਿੱਚ ਆਪਣੇ ਖੇਤ ਦਾ ਮਾਪ ਦਰਜ ਕਰੋ।",
                    accentColor = Color(0xFF00E5FF)
                ),
                FertilizerHelpItem(
                    icon = Icons.Default.Science,
                    title = "3. ਮੁੱਖ ਖਾਦਾਂ (ਯੂਰੀਆ, ਡੀਏਪੀ, ਪੋਟਾਸ਼)",
                    description = "• ਯੂਰੀਆ: ਬੂਟੇ ਦੇ ਵਾਧੇ ਲਈ।\n• ਡੀਏਪੀ: ਜੜ੍ਹਾਂ ਦੇ ਮਜ਼ਬੂਤ ਵਿਕਾਸ ਲਈ।\n• ਪੋਟਾਸ਼: ਦਾਣਿਆਂ ਦੀ ਚਮਕ ਅਤੇ ਬਿਮਾਰੀਆਂ ਨਾਲ ਲੜਨ ਲਈ।",
                    accentColor = SolarGold
                ),
                FertilizerHelpItem(
                    icon = Icons.Default.WaterDrop,
                    title = "4. ਜ਼ਿੰਕ ਅਤੇ ਰੂੜੀ ਖਾਦ (Zinc & FYM)",
                    description = "ਜ਼ਿੰਕ ਸਲਫ਼ੇਟ ਘਾਟ ਦੂਰ ਕਰਦਾ ਹੈ ਅਤੇ ਦੇਸੀ ਰੂੜੀ ਜ਼ਮੀਨ ਦੀ ਉਪਜਾਊ ਸ਼ਕਤੀ ਵਧਾਉਂਦੀ ਹੈ।",
                    accentColor = Color(0xFF818CF8)
                )
            ),
            footerNote = "💡 ਬਿਜਾਈ ਵੇਲੇ ਡੀਏਪੀ ਅਤੇ ਪੋਟਾਸ਼ ਪੂਰੀ ਮਾਤਰਾ ਵਿੱਚ ਅਤੇ ਯੂਰੀਆ ਤੀਜਾ ਹਿੱਸਾ ਪਾਓ।",
            closeButtonText = "ਸਮਝ ਗਿਆ (Close)"
        )

        com.fasaldrishti.app.data.local.AppLanguage.GUJARATI -> FertilizerHelpGuideContent(
            title = "ખાતર અને NPK કેલ્ક્યુલેટર માર્ગદર્શિકા",
            subtitle = "પાક અનુસાર ખાતરની યોગ્ય માત્રા અને સમયપત્રક",
            items = listOf(
                FertilizerHelpItem(
                    icon = Icons.Default.Agriculture,
                    title = "૧. પાકની પસંદગી કરો (Select Crop)",
                    description = "ડાંગર, ઘઉં, કપાસ, મગફળી, શેરડી વગેરેમાંથી આપનો પાક પસંદ કરો.",
                    accentColor = EmeraldPrimary
                ),
                FertilizerHelpItem(
                    icon = Icons.Default.SquareFoot,
                    title = "૨. જમીનનું ક્ષેત્રફળ લખો (Land Area)",
                    description = "એકર, વીઘા કે હેક્ટરમાં જમીનનું ક્ષેત્રફળ દાખલ કરો.",
                    accentColor = Color(0xFF00E5FF)
                ),
                FertilizerHelpItem(
                    icon = Icons.Default.Science,
                    title = "૩. મુખ્ય રાસાયણિક ખાતરો (Urea, DAP, MOP)",
                    description = "• યુરિયા: પાકની વૃદ્ધિ અને હરિયાળી માટે.\n• ડીએપી: મૂળના વિકાસ માટે.\n• પોટાશ: દાણા ભરાવ અને રોગ પ્રતિકારક શક્તિ માટે.",
                    accentColor = SolarGold
                ),
                FertilizerHelpItem(
                    icon = Icons.Default.WaterDrop,
                    title = "૪. ઝિંક અને દેશી ખાતર (Zinc & FYM)",
                    description = "ઝિંક સૂક્ષ્મ પોષકતત્વોની ખામી દૂર કરે છે અને છાણિયું ખાતર જમીન ફળદ્રુપ બનાવે છે.",
                    accentColor = Color(0xFF818CF8)
                )
            ),
            footerNote = "💡 વાવણી વખતે ડીએપી અને પોટાશ પૂરેપૂરું અને યુરિયાનો ત્રીજો ભાગ આપવો.",
            closeButtonText = "સમજાયું (Close)"
        )

        com.fasaldrishti.app.data.local.AppLanguage.TELUGU -> FertilizerHelpGuideContent(
            title = "ఎరువులు & NPK కాలిక్యులేటర్ గైడ్",
            subtitle = "పంటకు తగిన ఎరువుల మోతాదు మరియు సమయాల వివరాలు",
            items = listOf(
                FertilizerHelpItem(
                    icon = Icons.Default.Agriculture,
                    title = "1. పంటను ఎంచుకోండి (Select Crop)",
                    description = "వరి, గోధుమ, పత్తి, మిరప, చెరకు వంటి పంటలలో మీ పంటను ఎంచుకోండి.",
                    accentColor = EmeraldPrimary
                ),
                FertilizerHelpItem(
                    icon = Icons.Default.SquareFoot,
                    title = "2. భూమి విస్తీర్ణం నమోదు చేయండి (Land Area)",
                    description = "ఎకరాలు లేదా హెక్టార్లలో భూమి విస్తీర్ణం నమోదు చేయండి.",
                    accentColor = Color(0xFF00E5FF)
                ),
                FertilizerHelpItem(
                    icon = Icons.Default.Science,
                    title = "3. ప్రాథమిక ఎరువులు (యూరియా, DAP, MOP)",
                    description = "• యూరియా: శాఖీయ పెరుగుదలకు.\n• DAP: వేర్ల అభివృద్ధికి.\n• పొటాష్: గింజ బరువు, నాణ్యత మరియు రోగనిరోధకతకు.",
                    accentColor = SolarGold
                ),
                FertilizerHelpItem(
                    icon = Icons.Default.WaterDrop,
                    title = "4. జింక్ & సేంద్రీయ ఎరువు (Zinc & FYM)",
                    description = "జింక్ లోపాన్ని నివారిస్తుంది మరియు పశువుల ఎరువు నేల సారాన్ని పెంచుతుంది.",
                    accentColor = Color(0xFF818CF8)
                )
            ),
            footerNote = "💡 విత్తే సమయంలో DAP మరియు పొటాష్ పూర్తిగా మరియు యూరియాలో 1/3 వంతు వేయండి.",
            closeButtonText = "అర్థమైంది (Close)"
        )

        com.fasaldrishti.app.data.local.AppLanguage.TAMIL -> FertilizerHelpGuideContent(
            title = "உரங்கள் மற்றும் NPK கால்குலேட்டர் வழிகாட்டி",
            subtitle = "பயிருக்கு ஏற்ப சரியான உர அளவு மற்றும் பயன்பாட்டு முறை",
            items = listOf(
                FertilizerHelpItem(
                    icon = Icons.Default.Agriculture,
                    title = "1. பயிரைத் தேர்ந்தெடுக்கவும் (Select Crop)",
                    description = "நெல், கரும்பு, பருத்தி, மக்காச்சோளம் போன்ற பயிர்களில் உங்கள் பயிரைத் தேர்வு செய்யவும்.",
                    accentColor = EmeraldPrimary
                ),
                FertilizerHelpItem(
                    icon = Icons.Default.SquareFoot,
                    title = "2. நிலப்பரப்பை உள்ளிடவும் (Land Area)",
                    description = "ஏக்கர் அல்லது ஹெக்டேரில் உங்கள் நிலத்தின் அளவை உள்ளிடவும்.",
                    accentColor = Color(0xFF00E5FF)
                ),
                FertilizerHelpItem(
                    icon = Icons.Default.Science,
                    title = "3. முக்கிய உரங்கள் (யுரியா, DAP, பொட்டாஷ்)",
                    description = "• யுரியா: பயிர் வளர்ச்சிக்கு.\n• DAP: வேர் வளர்ச்சிக்கு.\n• MOP பொட்டாஷ்: தானிய எடை மற்றும் நோய் எதிர்ப்பு சக்திக்கு.",
                    accentColor = SolarGold
                ),
                FertilizerHelpItem(
                    icon = Icons.Default.WaterDrop,
                    title = "4. துத்தநாகம் & இயற்கை உரம் (Zinc & FYM)",
                    description = "துத்தநாகம் ஊட்டச்சத்து குறைபாட்டை நீக்குகிறது, தொழு உரம் மண்ணை வளப்படுத்துகிறது.",
                    accentColor = Color(0xFF818CF8)
                )
            ),
            footerNote = "💡 விதைக்கும் போது DAP மற்றும் பொட்டாஷ் முழுமையாகவும், யுரியாவில் 1/3 பகுதியும் இடவும்.",
            closeButtonText = "புரிந்தது (Close)"
        )

        com.fasaldrishti.app.data.local.AppLanguage.KANNADA -> FertilizerHelpGuideContent(
            title = "ಗೊಬ್ಬರ ಮತ್ತು NPK ಕ್ಯಾಲ್ಕುಲೇಟರ್ ಮಾರ್ಗದರ್ಶಿ",
            subtitle = "ಬೆಳೆಗೆ ತಕ್ಕಂತೆ ನಿಖರವಾದ ರಸಗೊಬ್ಬರ ಪ್ರಮಾಣ ಮತ್ತು ಬಳಕೆ",
            items = listOf(
                FertilizerHelpItem(
                    icon = Icons.Default.Agriculture,
                    title = "1. ಬೆಳೆ ಆಯ್ಕೆಮಾಡಿ (Select Crop)",
                    description = "ಭತ್ತ, ಗೋಧಿ, ಹತ್ತಿ, ಕಬ್ಬು, ಮೆಕ್ಕೆಜೋಳ ಇತ್ಯಾದಿಗಳಲ್ಲಿ ನಿಮ್ಮ ಬೆಳೆ ಆಯ್ಕೆಮಾಡಿ.",
                    accentColor = EmeraldPrimary
                ),
                FertilizerHelpItem(
                    icon = Icons.Default.SquareFoot,
                    title = "2. ಜಮೀನಿನ ವಿಸ್ತೀರ್ಣ ನಮೂದಿಸಿ (Land Area)",
                    description = "ಎಕರೆ, ಗುಂಟೆ ಅಥವಾ ಹೆಕ್ಟೇರ್‌ನಲ್ಲಿ ಜಮೀನಿನ ಅಳತೆ ನಮೂದಿಸಿ.",
                    accentColor = Color(0xFF00E5FF)
                ),
                FertilizerHelpItem(
                    icon = Icons.Default.Science,
                    title = "3. ಮುಖ್ಯ ರಸಗೊಬ್ಬರಗಳು (ಯೂರಿಯಾ, DAP, ಪೊಟ್ಯಾಶ್)",
                    description = "• ಯೂರಿಯಾ: ಗಿಡದ ಬೆಳವಣಿಗೆಗೆ.\n• DAP: ಬೇರುಗಳ ಬೆಳವಣಿಗೆಗೆ.\n• MOP ಪೊಟ್ಯಾಶ್: ಧಾನ್ಯದ ತೂಕ ಮತ್ತು ರೋಗ ನಿರೋಧಕತೆಗೆ.",
                    accentColor = SolarGold
                ),
                FertilizerHelpItem(
                    icon = Icons.Default.WaterDrop,
                    title = "4. ಜಿಂಕ್ ಮತ್ತು ಸಾವಯವ ಗೊಬ್ಬರ (Zinc & FYM)",
                    description = "ಜಿಂಕ್ ಸಲ್ಫೇಟ್ ಕೊರತೆಯನ್ನು ನೀಗಿಸುತ್ತದೆ ಮತ್ತು ಕೊಟ್ಟಿಗೆ ಗೊಬ್ಬರ ಮಣ್ಣಿನ ಫಲವತ್ತತೆ ಹೆಚ್ಚಿಸುತ್ತದೆ.",
                    accentColor = Color(0xFF818CF8)
                )
            ),
            footerNote = "💡 ಬಿತ್ತನೆ ಸಮಯದಲ್ಲಿ DAP ಮತ್ತು ಪೊಟ್ಯಾಶ್ ಪೂರ್ಣವಾಗಿ ಹಾಗೂ ಯೂರಿಯಾದ 1/3 ಭಾಗ ಹಾಕಿ.",
            closeButtonText = "ಅರ್ಥವಾಯಿತು (Close)"
        )

        com.fasaldrishti.app.data.local.AppLanguage.ODIA -> FertilizerHelpGuideContent(
            title = "ଖତ ଓ NPK କ୍ୟାଲକୁଲେଟର ମାର୍ଗଦର୍ଶିକା",
            subtitle = "ଫସଲ ଅନୁଯାୟୀ ସଠିକ୍ ସାର ମାତ୍ରା ଓ ପ୍ରୟୋଗ ନିୟମ",
            items = listOf(
                FertilizerHelpItem(
                    icon = Icons.Default.Agriculture,
                    title = "୧. ଫସଲ ଚୟନ କରନ୍ତୁ (Select Crop)",
                    description = "ଧାନ, ଗହମ, ଆଳୁ, ସୋରିଷ, କପା ଇତ୍ୟାଦି ମଧ୍ୟରୁ ଆପଣଙ୍କ ଫସଲ ବାଛନ୍ତୁ।",
                    accentColor = EmeraldPrimary
                ),
                FertilizerHelpItem(
                    icon = Icons.Default.SquareFoot,
                    title = "୨. ଜମି ପରିମାଣ ଲେଖନ୍ତୁ (Land Area)",
                    description = "ଏକର, ବିଘା କିମ୍ବା ହେକ୍ଟରରେ ଆପଣଙ୍କ ଜମିର ମାପ ଦର୍ଜ କରନ୍ତୁ।",
                    accentColor = Color(0xFF00E5FF)
                ),
                FertilizerHelpItem(
                    icon = Icons.Default.Science,
                    title = "୩. ମୁଖ୍ୟ ରାସାୟନିକ ସାର (Urea, DAP, MOP)",
                    description = "• ୟୁରିଆ: ଗଛର ବୃଦ୍ଧି ପାଇଁ।\n• ଡିଏପି: ଚେର ଓ ଶାଖା ବିସ୍ତାର ପାଇଁ।\n• ଏମଓପି ପଟାସ: ଦାନାର ଚମକ ଓ ରୋଗ ପ୍ରତିରୋଧକ ଶକ୍ତି ପାଇଁ।",
                    accentColor = SolarGold
                ),
                FertilizerHelpItem(
                    icon = Icons.Default.WaterDrop,
                    title = "୪. ଜିଙ୍କ ଓ ଖତ (Zinc & FYM)",
                    description = "ଜିଙ୍କ ଅଭାବ ଦୂର କରେ ଏବଂ ଗୋବର ଖତ ଜମିର ଉର୍ବରତା ବୃଦ୍ଧି କରେ।",
                    accentColor = Color(0xFF818CF8)
                )
            ),
            footerNote = "💡 ବୁଣିବା ସମୟରେ ଡିଏପି ଓ ପଟାସ ସମ୍ପୂର୍ଣ୍ଣ ଏବଂ ୟୁରିଆର ଏକ ତୃତୀୟାଂଶ ପ୍ରୟୋଗ କରନ୍ତୁ।",
            closeButtonText = "ବୁଝିଲି (Close)"
        )

        else -> FertilizerHelpGuideContent(
            title = "Fertilizer & NPK Calculator Guide",
            subtitle = "Accurate ICAR Nutrient Formulation & Scheduling",
            items = listOf(
                FertilizerHelpItem(
                    icon = Icons.Default.Agriculture,
                    title = "1. Select Your Crop",
                    description = "Choose your targeted crop (Paddy, Wheat, Potato, Mustard, Cotton, etc.) to load ICAR standard scientific nutrient recommendations.",
                    accentColor = EmeraldPrimary
                ),
                FertilizerHelpItem(
                    icon = Icons.Default.SquareFoot,
                    title = "2. Enter Farm Area",
                    description = "Input your land size in Acres, Bigha, Hectares, or Guntha. The app computes exact commercial bag quantities automatically.",
                    accentColor = Color(0xFF00E5FF)
                ),
                FertilizerHelpItem(
                    icon = Icons.Default.Science,
                    title = "3. Primary Macro-Nutrients (Urea, DAP, MOP)",
                    description = "• Urea (46% Nitrogen): Foliage growth and chlorophyll formation.\n• DAP (18:46:0 Nitrogen & Phosphorus): Deep root system and early tillering.\n• MOP (60% Potash): Grain weight, drought tolerance, and pest resistance.",
                    accentColor = SolarGold
                ),
                FertilizerHelpItem(
                    icon = Icons.Default.WaterDrop,
                    title = "4. Secondary & Organic Balance (Zinc & FYM)",
                    description = "Zinc Sulphate prevents Khaira disease in paddy, while well-rotted FYM improves soil microbial organic carbon.",
                    accentColor = Color(0xFF818CF8)
                )
            ),
            footerNote = "💡 Apply 100% DAP & Potash + 1/3rd Urea at sowing (Basal dose). Apply remaining Urea in 2 split top-dressings.",
            closeButtonText = "Got It (Close)"
        )
    }
}
