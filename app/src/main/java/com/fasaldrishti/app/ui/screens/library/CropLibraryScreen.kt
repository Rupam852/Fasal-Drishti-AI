package com.fasaldrishti.app.ui.screens.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fasaldrishti.app.data.local.AppLanguage
import com.fasaldrishti.app.data.local.LanguageManager
import com.fasaldrishti.app.domain.model.DiseaseInfo
import com.fasaldrishti.app.domain.repository.DiseaseRepository
import com.fasaldrishti.app.ui.components.AccordionCard
import com.fasaldrishti.app.ui.components.SeverityBadge
import com.fasaldrishti.app.ui.theme.CrimsonCoral
import com.fasaldrishti.app.ui.theme.EmeraldPrimary
import com.fasaldrishti.app.ui.theme.SolarGold

data class IndianCropCategory(
    val id: String,
    val label: String,
    val icon: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropLibraryScreen(
    diseaseRepository: DiseaseRepository,
    languageManager: LanguageManager? = null,
    onNavigateBack: () -> Unit,
    onNavigateToChat: ((String) -> Unit)? = null
) {
    val globalLanguage by (languageManager?.currentLanguage ?: kotlinx.coroutines.flow.MutableStateFlow(AppLanguage.ENGLISH)).collectAsState()
    var localLangCode by remember(globalLanguage) { mutableStateOf(globalLanguage.code) }
    var showLanguageDropdown by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf("All") }
    var diseases by remember { mutableStateOf<List<DiseaseInfo>>(emptyList()) }
    var selectedDiseaseForSheet by remember { mutableStateOf<DiseaseInfo?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val languages = listOf(
        "en" to "English",
        "hi" to "हिन्दी",
        "bn" to "বাংলা",
        "mr" to "मराठी",
        "pa" to "ਪੰਜਾਬੀ",
        "te" to "తెలుగు",
        "ta" to "தமிழ்",
        "gu" to "ગુજરાતી"
    )

    LaunchedEffect(Unit) {
        val res = diseaseRepository.getAllDiseases()
        diseases = res.getOrDefault(emptyList())
    }

    val baseCrops = listOf(
        "All" to "🌿",
        "Rice" to "🌾",
        "Wheat" to "🌾",
        "Cotton" to "🌿",
        "Sugarcane" to "🎋",
        "Chilli" to "🌶️",
        "Mustard" to "🟡",
        "Mango" to "🥭",
        "Tomato" to "🍅",
        "Potato" to "🥔",
        "Corn" to "🌽",
        "Soybean" to "🫘",
        "Citrus" to "🍊",
        "Grape" to "🍇",
        "Apple" to "🍎"
    )

    val indianCrops = remember(localLangCode) {
        baseCrops.map { (cropId, icon) ->
            IndianCropCategory(
                id = cropId,
                label = getLocalizedCropCategoryName(cropId, localLangCode),
                icon = icon
            )
        }
    }

    val filteredDiseases = diseases.filter { d ->
        val query = searchQuery.trim()
        val matchesSearch = query.isBlank() ||
                d.diseaseName.contains(query, ignoreCase = true) ||
                (d.diseaseHindi?.contains(query, ignoreCase = true) == true) ||
                d.cropName.contains(query, ignoreCase = true) ||
                (d.cropHindi?.contains(query, ignoreCase = true) == true) ||
                d.symptoms.contains(query, ignoreCase = true) ||
                (d.symptomsHindi?.contains(query, ignoreCase = true) == true) ||
                d.treatment.contains(query, ignoreCase = true) ||
                (d.treatmentHindi?.contains(query, ignoreCase = true) == true)

        val matchesCategory = selectedCategoryId == "All" ||
                d.cropName.contains(selectedCategoryId, ignoreCase = true) ||
                (d.cropHindi?.contains(selectedCategoryId, ignoreCase = true) == true)

        matchesSearch && matchesCategory
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
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
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = getLibraryTitle(localLangCode),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            text = getLibrarySubtitle(localLangCode),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                fontSize = 11.5.sp
                            )
                        )
                    }

                    // Language Selector Pill with Dropdown
                    Box {
                        Surface(
                            onClick = { showLanguageDropdown = true },
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(start = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Translate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = languages.find { it.first == localLangCode }?.second ?: "English",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showLanguageDropdown,
                            onDismissRequest = { showLanguageDropdown = false }
                        ) {
                            languages.forEach { (code, name) ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        localLangCode = code
                                        val appLang = AppLanguage.entries.find { it.code == code }
                                        if (appLang != null && languageManager != null) {
                                            languageManager.setLanguage(appLang)
                                        }
                                        showLanguageDropdown = false
                                    },
                                    leadingIcon = if (localLangCode == code) {
                                        { Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldPrimary) }
                                    } else null
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                    shadowElevation = 2.dp
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { 
                            Text(
                                getSearchPlaceholder(localLangCode),
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp)
                            ) 
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Crop Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(indianCrops) { cat ->
                        val isSelected = selectedCategoryId == cat.id
                        Surface(
                            onClick = { selectedCategoryId = cat.id },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = cat.icon, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = cat.label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                        color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (filteredDiseases.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No matching crop disease found",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try searching with crop or disease name.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredDiseases) { disease ->
                    val displayCropName = if (localLangCode != "en" && !disease.cropHindi.isNullOrBlank()) disease.cropHindi.uppercase() else disease.cropName.uppercase()
                    val displayDiseaseName = if (localLangCode != "en" && !disease.diseaseHindi.isNullOrBlank()) disease.diseaseHindi else disease.diseaseName
                    val displayTreatment = if (localLangCode != "en" && !disease.treatmentHindi.isNullOrBlank()) disease.treatmentHindi else disease.treatment

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(3.dp, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { selectedDiseaseForSheet = disease },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(EmeraldPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (disease.isHealthy) Icons.Default.CheckCircle else Icons.Default.Eco,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                SeverityBadge(severity = disease.severity)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = displayCropName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = displayDiseaseName,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = if (disease.isHealthy) "Healthy Stand" else "Dosage: ${displayTreatment.take(45)}...",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.5.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                    ),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom Sheet Detailed Diagnostic Dossier
        if (selectedDiseaseForSheet != null) {
            ModalBottomSheet(
                onDismissRequest = { selectedDiseaseForSheet = null },
                sheetState = sheetState,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                val d = selectedDiseaseForSheet!!
                val sheetCrop = if (localLangCode != "en" && !d.cropHindi.isNullOrBlank()) d.cropHindi.uppercase() else d.cropName.uppercase()
                val sheetDisease = if (localLangCode != "en" && !d.diseaseHindi.isNullOrBlank()) d.diseaseHindi else d.diseaseName
                val sheetSymptoms = if (localLangCode != "en" && !d.symptomsHindi.isNullOrBlank()) d.symptomsHindi else d.symptoms
                val sheetTreatment = if (localLangCode != "en" && !d.treatmentHindi.isNullOrBlank()) d.treatmentHindi else d.treatment

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 22.dp, vertical = 10.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = sheetCrop,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                            )
                            Text(
                                text = sheetDisease,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 19.sp
                                )
                            )
                        }
                        SeverityBadge(severity = d.severity)
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    AccordionCard(
                        title = getSymptomsSectionTitle(localLangCode),
                        icon = Icons.Default.Coronavirus,
                        content = sheetSymptoms,
                        initiallyExpanded = true,
                        accentColor = CrimsonCoral
                    )

                    AccordionCard(
                        title = getTreatmentSectionTitle(localLangCode),
                        icon = Icons.Default.Science,
                        content = sheetTreatment,
                        initiallyExpanded = true,
                        accentColor = SolarGold
                    )

                    AccordionCard(
                        title = getOrganicSectionTitle(localLangCode),
                        icon = Icons.Default.Shield,
                        content = d.prevention,
                        initiallyExpanded = false,
                        accentColor = EmeraldPrimary
                    )

                    if (onNavigateToChat != null) {
                        Button(
                            onClick = {
                                val contextMsg = "I want more information and advice about ${d.cropName} - ${d.diseaseName}. How to cure it quickly?"
                                selectedDiseaseForSheet = null
                                onNavigateToChat(contextMsg)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldPrimary,
                                contentColor = Color.Black
                            )
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (localLangCode == "hi") "AI सलाहकार से पूछें" else if (localLangCode == "bn") "AI পরামর্শদাতার সাথে কথা বলুন" else "Consult AI Salahkar",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

private fun getLibraryTitle(lang: String): String = when (lang) {
    "hi" -> "फसल रोग ज्ञानकोष"
    "bn" -> "ফসল রোগ জ্ঞানকোষ"
    "mr" -> "पीक रोग ज्ञानकोष"
    "pa" -> "ਫ਼ਸਲ ਰੋਗ ਗਿਆਨ ਕੋਸ਼"
    "te" -> "పంట వ్యాధుల గ్రంథాలయం"
    "ta" -> "பயிர் நோய் நூலகம்"
    "gu" -> "પાક રોગ જ્ઞાનકોશ"
    else -> "Crop Pathology Library"
}

private fun getLibrarySubtitle(lang: String): String = when (lang) {
    "hi" -> "रोग लक्षण, वैज्ञानिक पहचान एवं सटीक उपचार"
    "bn" -> "রোগের লক্ষণ, বৈজ্ঞানিক নির্ণয় ও প্রতিকার"
    "mr" -> "रोगांची लक्षणे, शास्त्रीय निदान व उपचार"
    "pa" -> "ਰੋਗ ਦੇ ਲੱਛਣ ਅਤੇ ਖੇਤੀਬਾੜੀ ਇਲਾਜ"
    "te" -> "వ్యాధి లక్షణాలు మరియు శాస్త్రీయ చికిత్సలు"
    "ta" -> "நோய் அறிகுறிகள் மற்றும் தீர்வுகள்"
    "gu" -> "રોગના લક્ષણો અને વૈજ્ઞાનિક ઉપચાર"
    else -> "Disease Knowledge Base & Agronomy Treatments"
}

private fun getSearchPlaceholder(lang: String): String = when (lang) {
    "hi" -> "फसल, रोग या रासायनिक उपचार खोजें..."
    "bn" -> "ফসল, রোগ বা রাসায়নিক চিকিৎসা খুঁজুন..."
    "mr" -> "पीक, रोग किंवा औषधाचे नाव शोधा..."
    else -> "Search crop, disease, or chemical treatment..."
}

private fun getSymptomsSectionTitle(lang: String): String = when (lang) {
    "hi" -> "रोग के लक्षण एवं पहचान"
    "bn" -> "রোগের লক্ষণ ও শনাক্তকরণ"
    "mr" -> "रोगाची लक्षणे व ओळख"
    else -> "Symptoms & Identification"
}

private fun getTreatmentSectionTitle(lang: String): String = when (lang) {
    "hi" -> "रासायनिक उपचार एवं सटीक खुराक"
    "bn" -> "রাসায়নিক চিকিৎসা ও মাত্রা"
    "mr" -> "रासायनिक उपचार व मात्रा"
    else -> "Chemical Treatment & Dosage"
}

private fun getOrganicSectionTitle(lang: String): String = when (lang) {
    "hi" -> "जैविक नियंत्रण एवं खेत की रोकथाम"
    "bn" -> "জৈব নিয়ন্ত্রণ ও প্রতিরোধ ব্যবস্থা"
    "mr" -> "सेंद्रिय नियंत्रण व प्रतिबंध"
    else -> "Organic Control & Prevention"
}

private fun getLocalizedCropCategoryName(cropId: String, lang: String): String = when (lang) {
    "hi" -> when (cropId) {
        "All" -> "सभी फसलें"
        "Rice" -> "धान (Rice)"
        "Wheat" -> "गेहूं (Wheat)"
        "Cotton" -> "कपास (Cotton)"
        "Sugarcane" -> "गन्ना (Sugarcane)"
        "Chilli" -> "मिर्च (Chilli)"
        "Mustard" -> "सरसों (Mustard)"
        "Mango" -> "आम (Mango)"
        "Tomato" -> "टमाटर (Tomato)"
        "Potato" -> "आलू (Potato)"
        "Corn" -> "मक्का (Corn)"
        "Soybean" -> "सोयाबीन (Soybean)"
        "Citrus" -> "नींबू (Citrus)"
        "Grape" -> "अंगूर (Grape)"
        "Apple" -> "सेब (Apple)"
        else -> cropId
    }
    "bn" -> when (cropId) {
        "All" -> "সব ফসল"
        "Rice" -> "ধান (Rice)"
        "Wheat" -> "গম (Wheat)"
        "Cotton" -> "তুলা (Cotton)"
        "Sugarcane" -> "আখ (Sugarcane)"
        "Chilli" -> "লঙ্কা (Chilli)"
        "Mustard" -> "সরিষা (Mustard)"
        "Mango" -> "আম (Mango)"
        "Tomato" -> "টমেটো (Tomato)"
        "Potato" -> "আলু (Potato)"
        "Corn" -> "ভুট্টা (Corn)"
        "Soybean" -> "সয়াবিন (Soybean)"
        "Citrus" -> "লেবু (Citrus)"
        "Grape" -> "আঙুর (Grape)"
        "Apple" -> "আপেল (Apple)"
        else -> cropId
    }
    "mr" -> when (cropId) {
        "All" -> "सर्व पिके"
        "Rice" -> "भात (Rice)"
        "Wheat" -> "गहू (Wheat)"
        "Cotton" -> "कापूस (Cotton)"
        "Sugarcane" -> "ऊस (Sugarcane)"
        "Chilli" -> "मिरची (Chilli)"
        "Mustard" -> "मोहरी (Mustard)"
        "Mango" -> "आंबा (Mango)"
        "Tomato" -> "टोमॅटो (Tomato)"
        "Potato" -> "बटाटा (Potato)"
        "Corn" -> "मका (Corn)"
        "Soybean" -> "सोयाबीन (Soybean)"
        "Citrus" -> "संत्री/लिंबू"
        "Grape" -> "द्राक्षे (Grape)"
        "Apple" -> "सफरचंद (Apple)"
        else -> cropId
    }
    "pa" -> when (cropId) {
        "All" -> "ਸਾਰੀਆਂ ਫਸਲਾਂ"
        "Rice" -> "ਝੋਨਾ (Rice)"
        "Wheat" -> "ਕਣਕ (Wheat)"
        "Cotton" -> "ਨਰਮਾ (Cotton)"
        "Sugarcane" -> "ਗੰਨਾ (Sugarcane)"
        "Tomato" -> "ਟਮਾਟਰ (Tomato)"
        "Potato" -> "ਆਲੂ (Potato)"
        "Mustard" -> "ਸਰ੍ਹੋਂ (Mustard)"
        else -> cropId
    }
    else -> if (cropId == "All") "All Crops" else cropId
}
