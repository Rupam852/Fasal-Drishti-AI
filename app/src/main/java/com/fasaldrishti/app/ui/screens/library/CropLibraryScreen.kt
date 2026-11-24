package com.fasaldrishti.app.ui.screens.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.rotate
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
        val dossier = CropLibraryLocalization.getLocalizedDossier(d, localLangCode)
        val matchesSearch = query.isBlank() ||
                dossier.diseaseName.contains(query, ignoreCase = true) ||
                dossier.cropName.contains(query, ignoreCase = true) ||
                d.diseaseName.contains(query, ignoreCase = true) ||
                (d.diseaseHindi?.contains(query, ignoreCase = true) == true) ||
                d.cropName.contains(query, ignoreCase = true) ||
                (d.cropHindi?.contains(query, ignoreCase = true) == true) ||
                dossier.symptoms.contains(query, ignoreCase = true) ||
                dossier.treatment.contains(query, ignoreCase = true)

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

                    // Language Selector Pill with Modern Dropdown
                    Box {
                        val rotation by androidx.compose.animation.core.animateFloatAsState(
                            targetValue = if (showLanguageDropdown) 180f else 0f,
                            label = "lang_chevron"
                        )

                        Surface(
                            onClick = { showLanguageDropdown = !showLanguageDropdown },
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                            shadowElevation = 2.dp,
                            modifier = Modifier.padding(start = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Translate,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = languages.find { it.first == localLangCode }?.second ?: "English",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 11.5.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .rotate(rotation)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showLanguageDropdown,
                            onDismissRequest = { showLanguageDropdown = false },
                            modifier = Modifier
                                .width(215.dp)
                                .shadow(10.dp, RoundedCornerShape(22.dp))
                                .clip(RoundedCornerShape(22.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f), RoundedCornerShape(22.dp))
                        ) {
                            Text(
                                text = "SELECT LANGUAGE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                    fontSize = 10.sp,
                                    letterSpacing = 0.8.sp
                                ),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

                            languages.forEach { (code, name) ->
                                val isSelected = localLangCode == code
                                val englishSubtitle = when (code) {
                                    "en" -> "English"
                                    "hi" -> "Hindi"
                                    "bn" -> "Bengali"
                                    "mr" -> "Marathi"
                                    "pa" -> "Punjabi"
                                    "te" -> "Telugu"
                                    "ta" -> "Tamil"
                                    "gu" -> "Gujarati"
                                    else -> ""
                                }

                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = name,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                                    color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurface,
                                                    fontSize = 13.5.sp
                                                )
                                            )
                                            if (englishSubtitle.isNotBlank() && englishSubtitle != name) {
                                                Text(
                                                    text = englishSubtitle,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                                        fontSize = 10.5.sp
                                                    )
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        localLangCode = code
                                        showLanguageDropdown = false
                                    },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) EmeraldPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = EmeraldPrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            } else {
                                                Text(
                                                    text = name.take(1),
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                )
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isSelected) EmeraldPrimary.copy(alpha = 0.10f) else Color.Transparent)
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
                    val dossier = CropLibraryLocalization.getLocalizedDossier(disease, localLangCode)
                    val displayCropName = dossier.cropName.uppercase()
                    val displayDiseaseName = dossier.diseaseName
                    val displayTreatment = dossier.treatment

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

        // Bottom Sheet Detailed Diagnostic Dossier (100% Instant Offline)
        if (selectedDiseaseForSheet != null) {
            ModalBottomSheet(
                onDismissRequest = { selectedDiseaseForSheet = null },
                sheetState = sheetState,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                val d = selectedDiseaseForSheet!!
                val dossier = CropLibraryLocalization.getLocalizedDossier(d, localLangCode)

                val sheetCrop = dossier.cropName.uppercase()
                val sheetDisease = dossier.diseaseName
                val sheetSymptoms = dossier.symptoms
                val sheetTreatment = dossier.treatment
                val sheetPrevention = dossier.prevention

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
                        content = sheetPrevention,
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
                                text = when (localLangCode) {
                                    "hi" -> "AI सलाहकार से पूछें"
                                    "bn" -> "AI পরামর্শদাতার সাথে কথা বলুন"
                                    "mr" -> "AI सल्लागाराशी बोला"
                                    "pa" -> "AI ਸਲਾਹਕਾਰ ਨਾਲ ਗੱਲ ਕਰੋ"
                                    "gu" -> "AI સલાહકાર સાથે વાત કરો"
                                    "te" -> "AI సలహాదారుని సంప్రదించండి"
                                    "ta" -> "AI ஆலோசகருடன் பேசுங்கள்"
                                    else -> "Consult AI Salahkar"
                                },
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
    "pa" -> "ਰੋਗ ਦੇ ਲੱਛਣ ਅਤੇ ਪਛਾਣ"
    "te" -> "వ్యాధి లక్షణాలు మరియు గుర్తింపు"
    "ta" -> "நோய் அறிகுறிகள் மற்றும் அடையாளம்"
    "gu" -> "રોગના લક્ષણો અને ઓળખ"
    else -> "Symptoms & Identification"
}

private fun getTreatmentSectionTitle(lang: String): String = when (lang) {
    "hi" -> "रासायनिक उपचार एवं सटीक खुराक"
    "bn" -> "রাসায়নিক চিকিৎসা ও মাত্রা"
    "mr" -> "रासायनिक उपचार व मात्रा"
    "pa" -> "ਕੈਮੀਕਲ ਇਲਾਜ ਅਤੇ ਮਾਤਰਾ"
    "te" -> "రసాయన చికిత్స మరియు మోతాదు"
    "ta" -> "ரசாயன சிகிச்சை மற்றும் அளவு"
    "gu" -> "રાસાયણિક સારવાર અને માત્રા"
    else -> "Chemical Treatment & Dosage"
}

private fun getOrganicSectionTitle(lang: String): String = when (lang) {
    "hi" -> "जैविक नियंत्रण एवं खेत की रोकथाम"
    "bn" -> "জৈব নিয়ন্ত্রণ ও প্রতিরোধ ব্যবস্থা"
    "mr" -> "सेंद्रिय नियंत्रण व प्रतिबंध"
    "pa" -> "ਜੈਵਿਕ ਕੰਟਰੋਲ ਅਤੇ ਰੋਕਥਾਮ"
    "te" -> "సేంద్రీయ నియంత్రణ మరియు నివారణ"
    "ta" -> "இயற்கை கட்டுப்பாடு மற்றும் தடுப்பு"
    "gu" -> "જૈવિક નિયંત્રણ અને નિવારણ"
    else -> "Organic Control & Prevention"
}

fun getSimpleCropName(cropName: String, lang: String): String {
    val clean = cropName.trim()
    return when (lang) {
        "hi" -> when {
            clean.contains("Rice", ignoreCase = true) -> "धान"
            clean.contains("Wheat", ignoreCase = true) -> "गेहूं"
            clean.contains("Cotton", ignoreCase = true) -> "कपास"
            clean.contains("Sugarcane", ignoreCase = true) -> "गन्ना"
            clean.contains("Chilli", ignoreCase = true) -> "मिर्च"
            clean.contains("Mustard", ignoreCase = true) -> "सरसों"
            clean.contains("Mango", ignoreCase = true) -> "आम"
            clean.contains("Tomato", ignoreCase = true) -> "टमाटर"
            clean.contains("Potato", ignoreCase = true) -> "आलू"
            clean.contains("Corn", ignoreCase = true) || clean.contains("Maize", ignoreCase = true) -> "मक्का"
            clean.contains("Soybean", ignoreCase = true) -> "सोयाबीन"
            clean.contains("Citrus", ignoreCase = true) -> "नींबू"
            clean.contains("Grape", ignoreCase = true) -> "अंगूर"
            clean.contains("Apple", ignoreCase = true) -> "सेब"
            else -> clean
        }
        "bn" -> when {
            clean.contains("Rice", ignoreCase = true) -> "ধান"
            clean.contains("Wheat", ignoreCase = true) -> "গম"
            clean.contains("Cotton", ignoreCase = true) -> "তুলা"
            clean.contains("Sugarcane", ignoreCase = true) -> "আখ"
            clean.contains("Chilli", ignoreCase = true) -> "লঙ্কা"
            clean.contains("Mustard", ignoreCase = true) -> "সরিষা"
            clean.contains("Mango", ignoreCase = true) -> "আম"
            clean.contains("Tomato", ignoreCase = true) -> "টমেটো"
            clean.contains("Potato", ignoreCase = true) -> "আলু"
            clean.contains("Corn", ignoreCase = true) || clean.contains("Maize", ignoreCase = true) -> "ভুট্টা"
            clean.contains("Soybean", ignoreCase = true) -> "সয়াবিন"
            clean.contains("Citrus", ignoreCase = true) -> "লেবু"
            clean.contains("Grape", ignoreCase = true) -> "আঙুর"
            clean.contains("Apple", ignoreCase = true) -> "আপেল"
            else -> clean
        }
        "mr" -> when {
            clean.contains("Rice", ignoreCase = true) -> "भात"
            clean.contains("Wheat", ignoreCase = true) -> "गहू"
            clean.contains("Cotton", ignoreCase = true) -> "कापूस"
            clean.contains("Sugarcane", ignoreCase = true) -> "ऊस"
            clean.contains("Chilli", ignoreCase = true) -> "मिरची"
            clean.contains("Mustard", ignoreCase = true) -> "मोहरी"
            clean.contains("Mango", ignoreCase = true) -> "आंबा"
            clean.contains("Tomato", ignoreCase = true) -> "टोमॅटो"
            clean.contains("Potato", ignoreCase = true) -> "बटाटा"
            clean.contains("Corn", ignoreCase = true) || clean.contains("Maize", ignoreCase = true) -> "मका"
            clean.contains("Soybean", ignoreCase = true) -> "सोयाबीन"
            clean.contains("Citrus", ignoreCase = true) -> "संत्री/लिंबू"
            clean.contains("Grape", ignoreCase = true) -> "द्राक्षे"
            clean.contains("Apple", ignoreCase = true) -> "सफरचंद"
            else -> clean
        }
        "pa" -> when {
            clean.contains("Rice", ignoreCase = true) -> "ਝੋਨਾ"
            clean.contains("Wheat", ignoreCase = true) -> "ਕਣਕ"
            clean.contains("Cotton", ignoreCase = true) -> "ਨਰਮਾ"
            clean.contains("Sugarcane", ignoreCase = true) -> "ਗੰਨਾ"
            clean.contains("Tomato", ignoreCase = true) -> "ਟਮਾਟਰ"
            clean.contains("Potato", ignoreCase = true) -> "ਆਲੂ"
            clean.contains("Mustard", ignoreCase = true) -> "ਸਰ੍ਹੋਂ"
            else -> clean
        }
        "te" -> when {
            clean.contains("Rice", ignoreCase = true) -> "వరి"
            clean.contains("Wheat", ignoreCase = true) -> "గోధుమ"
            clean.contains("Cotton", ignoreCase = true) -> "పత్తి"
            clean.contains("Sugarcane", ignoreCase = true) -> "చెరకు"
            clean.contains("Tomato", ignoreCase = true) -> "టమోటా"
            clean.contains("Potato", ignoreCase = true) -> "బంగాళాదుంప"
            else -> clean
        }
        "ta" -> when {
            clean.contains("Rice", ignoreCase = true) -> "நெல்"
            clean.contains("Wheat", ignoreCase = true) -> "கோதுமை"
            clean.contains("Cotton", ignoreCase = true) -> "பருத்தி"
            clean.contains("Sugarcane", ignoreCase = true) -> "கரும்பு"
            clean.contains("Tomato", ignoreCase = true) -> "தக்காளி"
            clean.contains("Potato", ignoreCase = true) -> "உருளைக்கிழங்கு"
            else -> clean
        }
        "gu" -> when {
            clean.contains("Rice", ignoreCase = true) -> "ડાંગર"
            clean.contains("Wheat", ignoreCase = true) -> "ઘઉં"
            clean.contains("Cotton", ignoreCase = true) -> "કપાસ"
            clean.contains("Sugarcane", ignoreCase = true) -> "શેરડી"
            clean.contains("Tomato", ignoreCase = true) -> "ટામેટા"
            clean.contains("Potato", ignoreCase = true) -> "બટાટા"
            clean.contains("Mustard", ignoreCase = true) -> "રાયડો"
            else -> clean
        }
        else -> clean
    }
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
