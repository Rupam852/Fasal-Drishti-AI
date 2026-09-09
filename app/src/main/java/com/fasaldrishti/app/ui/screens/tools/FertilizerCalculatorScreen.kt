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
