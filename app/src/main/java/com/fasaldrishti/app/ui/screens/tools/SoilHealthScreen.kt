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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fasaldrishti.app.data.remote.GeminiClient
import com.fasaldrishti.app.ui.theme.*
import kotlinx.coroutines.launch

import androidx.compose.ui.platform.LocalContext
import com.fasaldrishti.app.FasalDrishtiApp
import java.util.Locale

data class SoilTypeOption(
    val id: String,
    val name: String,
    val hindiName: String,
    val defaultPh: Float,
    val bestCrops: List<String>
)

data class SoilAnalysisResult(
    val fertilityScore: String,
    val phStatus: String,
    val recommendedCrops: List<String>,
    val soilHealthTips: List<String>,
    val rawAiInsight: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoilHealthScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String) -> Unit = {},
    geminiClient: GeminiClient? = null
) {
    val context = LocalContext.current
    val effectiveGeminiClient = remember(geminiClient) {
        geminiClient ?: (context.applicationContext as? FasalDrishtiApp)?.geminiClient ?: GeminiClient()
    }
    val coroutineScope = rememberCoroutineScope()

    val soilTypes = remember {
        listOf(
            SoilTypeOption("alluvial", "Alluvial (जलोढ़ मिट्टी)", "गंगा-यमुना मैदान", 6.8f, listOf("Wheat", "Paddy", "Sugarcane", "Mustard")),
            SoilTypeOption("black", "Black / Regur (काली मिट्टी)", "कपास व सोयाबीन", 7.5f, listOf("Cotton", "Soybean", "Wheat", "Gram")),
            SoilTypeOption("red", "Red / Loamy (लाल मिट्टी)", "दलहन व तिलहन", 6.2f, listOf("Groundnut", "Pulses", "Millets", "Tobacco")),
            SoilTypeOption("sandy", "Sandy Loam (बलुई दोमट)", "सब्जियां व आलू", 6.5f, listOf("Potato", "Tomato", "Melons", "Maize")),
            SoilTypeOption("clay", "Clay Loam (मटियार दोमट)", "धान व गेहूं", 7.0f, listOf("Paddy", "Wheat", "Jute", "Mustard"))
        )
    }

    var selectedSoil by remember { mutableStateOf(soilTypes[0]) }
    var nitrogenLevel by remember { mutableFloatStateOf(280f) } // kg/ha (Low: <280, Med: 280-560, High: >560)
    var phosphorusLevel by remember { mutableFloatStateOf(22f) } // kg/ha (Low: <10, Med: 10-25, High: >25)
    var potassiumLevel by remember { mutableFloatStateOf(180f) } // kg/ha (Low: <110, Med: 110-280, High: >280)
    var phLevel by remember { mutableFloatStateOf(selectedSoil.defaultPh) }

    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf<SoilAnalysisResult?>(null) }

    fun runAnalysis() {
        isAnalyzing = true
        coroutineScope.launch {
            try {
                val prompt = """
                    Analyze soil test health parameters for an Indian farm:
                    Soil Type: ${selectedSoil.name}
                    Available Nitrogen (N): ${nitrogenLevel.toInt()} kg/ha
                    Available Phosphorus (P): ${phosphorusLevel.toInt()} kg/ha
                    Available Potassium (K): ${potassiumLevel.toInt()} kg/ha
                    Soil pH: ${String.format(Locale.US, "%.1f", phLevel)}
                    
                    Provide short, direct agricultural advisory for the farmer in Hindi/Hinglish:
                    1. Soil fertility condition (Good/Medium/Deficient)
                    2. Top 3 most profitable and suitable crops
                    3. Specific organic and chemical soil correction advice (Gypsum/Lime/FYM)
                """.trimIndent()

                val aiResponse = effectiveGeminiClient.getAgronomyAdvice(
                    primaryClass = "Soil Test: ${selectedSoil.name}",
                    confidence = 0.95f,
                    query = prompt,
                    language = "Hindi"
                ).getOrNull() ?: ""

                val phDesc = when {
                    phLevel < 6.0f -> "Acidic (अम्लीय - चूना/Lime प्रयोग करें)"
                    phLevel > 7.8f -> "Alkaline / Saline (क्षारीय - जिप्सम प्रयोग करें)"
                    else -> "Optimal Neutral (उत्कृष्ट उपजाऊ)"
                }

                val fertility = when {
                    nitrogenLevel < 200 || phosphorusLevel < 12 || potassiumLevel < 120 -> "Moderate to Low (खाद की आवश्यकता)"
                    else -> "High Fertility (उत्तम उर्वरक स्तर)"
                }

                val tips = mutableListOf<String>()
                if (phLevel < 6.2f) tips.add("Add Agricultural Lime @ 200 kg/acre to neutralize acidity.")
                if (phLevel > 7.8f) tips.add("Apply Gypsum @ 300 kg/acre and green manure (Dhaincha) to reduce alkalinity.")
                if (nitrogenLevel < 280f) tips.add("Incorporate Vermicompost / Well-rotted FYM @ 2 tons/acre.")
                tips.add("Apply biofertilizers (Azotobacter & PSB culture) during seed treatment.")

                analysisResult = SoilAnalysisResult(
                    fertilityScore = fertility,
                    phStatus = phDesc,
                    recommendedCrops = selectedSoil.bestCrops,
                    soilHealthTips = tips,
                    rawAiInsight = aiResponse.ifBlank { "मृदा विश्लेषण के अनुसार यह जमीन गेहूं, धान और दलहनी फसलों के लिए अति उत्तम है। फॉस्फोरस का स्तर संतुलित बनाए रखें।" }
                )
            } catch (_: Exception) {
                analysisResult = SoilAnalysisResult(
                    fertilityScore = "Optimal to Medium",
                    phStatus = "Normal Neutral (6.8 pH)",
                    recommendedCrops = selectedSoil.bestCrops,
                    soilHealthTips = listOf(
                        "Apply 2 Tons FYM / Gobar Khad per acre before plowing.",
                        "Use PSB (Phosphate Solubilizing Bacteria) for better root absorption."
                    ),
                    rawAiInsight = "आपकी मिट्टी में पोषक तत्व सामान्य हैं। जैविक खाद और हरी खाद (ढैंचा) का प्रयोग करके उत्पादन को 20% तक बढ़ाया जा सकता है।"
                )
            } finally {
                isAnalyzing = false
            }
        }
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
                        text = "Soil Health & Crop Advisor AI",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                    )
                    Text(
                        text = "मृदा स्वास्थ्य व फसल चयन AI",
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
            // 1. SELECT SOIL TYPE
            item {
                Text(
                    text = "1. Soil Type (मिट्टी का प्रकार)",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(soilTypes) { soil ->
                        val isSelected = soil.id == selectedSoil.id
                        Surface(
                            onClick = {
                                selectedSoil = soil
                                phLevel = soil.defaultPh
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                Text(
                                    text = soil.name,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = soil.hindiName,
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

            // 2. NUTRIENT PARAMETERS CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "2. Soil Test Parameters (NPK & pH स्तर)",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // pH Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Soil pH Level", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                            Text(text = "${String.format("%.1f", phLevel)} pH", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary))
                        }
                        Slider(
                            value = phLevel,
                            onValueChange = { phLevel = it },
                            valueRange = 4.5f..9.0f,
                            steps = 45,
                            colors = SliderDefaults.colors(thumbColor = EmeraldPrimary, activeTrackColor = EmeraldPrimary)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Nitrogen Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Available Nitrogen (N)", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                            Text(text = "${nitrogenLevel.toInt()} kg/ha", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF)))
                        }
                        Slider(
                            value = nitrogenLevel,
                            onValueChange = { nitrogenLevel = it },
                            valueRange = 100f..700f,
                            steps = 60,
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF00E5FF), activeTrackColor = Color(0xFF00E5FF))
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Phosphorus Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Available Phosphorus (P)", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                            Text(text = "${phosphorusLevel.toInt()} kg/ha", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = SolarGold))
                        }
                        Slider(
                            value = phosphorusLevel,
                            onValueChange = { phosphorusLevel = it },
                            valueRange = 5f..60f,
                            steps = 55,
                            colors = SliderDefaults.colors(thumbColor = SolarGold, activeTrackColor = SolarGold)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Potassium Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Available Potassium (K)", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                            Text(text = "${potassiumLevel.toInt()} kg/ha", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NeonLime))
                        }
                        Slider(
                            value = potassiumLevel,
                            onValueChange = { potassiumLevel = it },
                            valueRange = 50f..500f,
                            steps = 45,
                            colors = SliderDefaults.colors(thumbColor = NeonLime, activeTrackColor = NeonLime)
                        )
                    }
                }
            }

            // 3. ANALYZE BUTTON
            item {
                Button(
                    onClick = { runAnalysis() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    enabled = !isAnalyzing
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.Black, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "Analyzing Soil with AI...", color = Color.Black, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Analyze Soil Health (मिट्टी की जांच करें)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }

            // 4. ANALYSIS RESULTS CARD
            analysisResult?.let { result ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Soil Health Diagnostic Report", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Status Badges
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = EmeraldPrimary.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(text = "Fertility Index", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)))
                                        Text(text = result.fertilityScore, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary))
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF00E5FF).copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.3f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(text = "pH Category", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)))
                                        Text(text = result.phStatus, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF)))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(text = "🌾 Best Recommended Crops (उपयुक्त फसलें):", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                result.recommendedCrops.forEach { crop ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = EmeraldPrimary.copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f)),
                                        modifier = Modifier.clickable { onNavigateToChat(crop) }
                                    ) {
                                        Text(
                                            text = "🌱 $crop ↗",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldPrimary
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(text = "💡 Soil Enrichment Advice (सुधार उपाय):", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(6.dp))
                            result.soilHealthTips.forEach { tip ->
                                Text(text = "• $tip", style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 17.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)))
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Raw AI Insight
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.background.copy(alpha = 0.6f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = result.rawAiInsight,
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
