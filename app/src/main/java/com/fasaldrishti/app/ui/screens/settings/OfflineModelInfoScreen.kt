package com.fasaldrishti.app.ui.screens.settings

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fasaldrishti.app.data.local.AppLanguage
import com.fasaldrishti.app.data.local.LanguageManager
import com.fasaldrishti.app.ui.theme.EmeraldPrimary
import com.fasaldrishti.app.ui.theme.SolarGold

data class CropCatalogItem(
    val cropName: String,
    val cropHindi: String,
    val cropBengali: String,
    val emoji: String,
    val category: String, // "Vegetable", "Cereal", "Fruit", "Legume"
    val diseases: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineModelInfoScreen(
    languageManager: LanguageManager,
    onNavigateBack: () -> Unit
) {
    val currentAppLanguage by languageManager.currentLanguage.collectAsState()
    var selectedLanguage by remember(currentAppLanguage) { mutableStateOf(currentAppLanguage) }
    var showLanguageMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    // Multi-Language Content Localizer
    val lang = selectedLanguage
    val tTitle = when (lang) {
        AppLanguage.HINDI -> "ऑफलाइन एआई मॉडल (TFLite)"
        AppLanguage.BENGALI -> "অফলাইন এআই মডেল (TFLite)"
        AppLanguage.PUNJABI -> "ਔਫਲਾਈਨ ਏਆਈ ਮਾਡਲ (TFLite)"
        AppLanguage.MARATHI -> "ऑफलाइन एआय मॉडेल (TFLite)"
        AppLanguage.TELUGU -> "ఆఫ్‌లైన్ AI మోడల్ (TFLite)"
        AppLanguage.TAMIL -> "ஆஃப்லைன் AI மாதிரி (TFLite)"
        AppLanguage.GUJARATI -> "ઓફલાઇન AI મોડલ (TFLite)"
        AppLanguage.KANNADA -> "ಆಫ್‌ಲೈನ್ AI ಮಾದರಿ (TFLite)"
        AppLanguage.ODIA -> "ଅଫଲାଇନ AI ମଡେଲ (TFLite)"
        else -> "Offline AI Model (TFLite)"
    }

    val tSubtitle = when (lang) {
        AppLanguage.HINDI -> "100% ऑन-डिवाइस न्यूरल विजन इंजन। बिना इंटरनेट 45 मिलीसेकंड में पत्ती रोगों की सटीक पहचान।"
        AppLanguage.BENGALI -> "১০০% অন-ডিভাইস নিউরাল ভিশন ইঞ্জিন। ইন্টারনেট ছাড়াই ৪৫ মিলিসেকেন্ডে পাতার রোগ শনাক্তকরণ।"
        else -> "100% on-device Neural Vision Engine. Diagnoses foliar crop diseases in 45 milliseconds without internet."
    }

    val tDatasetsTitle = when (lang) {
        AppLanguage.HINDI -> "प्रशिक्षण डेटासेट (Training Datasets)"
        AppLanguage.BENGALI -> "প্রশিক্ষণ ডেটাসেট (Training Datasets)"
        else -> "Training Datasets & AI Architecture"
    }

    val tCatalogTitle = when (lang) {
        AppLanguage.HINDI -> "पहचान योग्य 43 फसल और रोग सूची"
        AppLanguage.BENGALI -> "শনাক্তকরণযোগ্য ৪৩টি ফসল ও রোগের তালিকা"
        else -> "43 Detectable Crop Disease Classes"
    }

    val tSearchPlaceholder = when (lang) {
        AppLanguage.HINDI -> "फसल या रोग का नाम खोजें (उदा. आलू, टमाटर, धान)..."
        AppLanguage.BENGALI -> "ফসল বা রোগের নাম খুঁজুন (যেমন আলু, টমেটো, ধান)..."
        else -> "Search crop or disease (e.g. Potato, Rice, Tomato)..."
    }

    val tNoticeTitle = when (lang) {
        AppLanguage.HINDI -> "महत्वपूर्ण किसान निर्देश (Foliage Specialization)"
        AppLanguage.BENGALI -> "গুরুত্বপূর্ণ নির্দেশাবলী (Foliage Specialization)"
        else -> "Important Usage Notice (Leaf Foliage Specialization)"
    }

    val tNoticeBody = when (lang) {
        AppLanguage.HINDI -> "यह ऑफलाइन TFLite मॉडल विशेष रूप से 'फसल की पत्तियों' के लिए प्रशिक्षित है। आलू के कंद (tubers), टमाटर फल या मिट्टी के लिए '2nd Online Verification' चालू रखें।"
        AppLanguage.BENGALI -> "এই অফলাইন TFLite মডেলটি বিশেষভাবে 'ফসলের পাতার' জন্য প্রশিক্ষিত। আলুর কন্দ (tubers), টমেটো ফল বা মাটির জন্য '2nd Online Verification' চালু রাখুন।"
        else -> "This offline TFLite model is trained exclusively on crop leaves. For potato tubers, fruits, or uncataloged crops, please use '2nd Online Verification' for Multimodal AI analysis."
    }

    // Complete catalog of 14 crops and 43 detectable classes
    val cropsCatalog = remember {
        listOf(
            CropCatalogItem(
                cropName = "Tomato",
                cropHindi = "टमाटर (Tomato)",
                cropBengali = "টমেটো (Tomato)",
                emoji = "🍅",
                category = "Vegetable",
                diseases = listOf(
                    "Bacterial Spot (Xanthomonas)",
                    "Early Blight (Alternaria solani)",
                    "Late Blight (Phytophthora infestans)",
                    "Leaf Mold (Passalora fulva)",
                    "Septoria Leaf Spot (Septoria lycopersici)",
                    "Spider Mites (Two-spotted spider mite)",
                    "Target Spot (Corynespora cassiicola)",
                    "Yellow Leaf Curl Virus (TYLCV)",
                    "Tomato Mosaic Virus (ToMV)",
                    "Healthy Leaf"
                )
            ),
            CropCatalogItem(
                cropName = "Potato",
                cropHindi = "आलू (Potato)",
                cropBengali = "আলু (Potato)",
                emoji = "🥔",
                category = "Vegetable",
                diseases = listOf(
                    "Early Blight (Alternaria solani)",
                    "Late Blight (Phytophthora infestans)",
                    "Healthy Leaf"
                )
            ),
            CropCatalogItem(
                cropName = "Rice",
                cropHindi = "धान / चावल (Paddy / Rice)",
                cropBengali = "ধান / চাল (Paddy / Rice)",
                emoji = "🌾",
                category = "Cereal",
                diseases = listOf(
                    "Bacterial Blight (Xanthomonas oryzae)",
                    "Brown Spot (Bipolaris oryzae)",
                    "Sheath Blight (Rhizoctonia solani)",
                    "Healthy Crop"
                )
            ),
            CropCatalogItem(
                cropName = "Corn (Maize)",
                cropHindi = "मक्का / भुट्टा (Corn / Maize)",
                cropBengali = "ভুট্টা (Corn / Maize)",
                emoji = "🌽",
                category = "Cereal",
                diseases = listOf(
                    "Cercospora Leaf Spot (Gray Leaf Spot)",
                    "Common Rust (Puccinia sorghi)",
                    "Northern Leaf Blight (Exserohilum turcicum)",
                    "Healthy Leaf"
                )
            ),
            CropCatalogItem(
                cropName = "Apple",
                cropHindi = "सेब (Apple)",
                cropBengali = "আপেল (Apple)",
                emoji = "🍎",
                category = "Fruit",
                diseases = listOf(
                    "Apple Scab (Venturia inaequalis)",
                    "Black Rot (Botryosphaeria obtusa)",
                    "Cedar Apple Rust (Gymnosporangium)",
                    "Healthy Foliage"
                )
            ),
            CropCatalogItem(
                cropName = "Grape",
                cropHindi = "अंगूर (Grape)",
                cropBengali = "আঙুর (Grape)",
                emoji = "🍇",
                category = "Fruit",
                diseases = listOf(
                    "Black Rot (Guignardia bidwellii)",
                    "Esca / Black Measles",
                    "Leaf Blight (Isariopsis Leaf Spot)",
                    "Healthy Foliage"
                )
            ),
            CropCatalogItem(
                cropName = "Pepper (Bell Pepper)",
                cropHindi = "शिमला मिर्च (Capsicum / Bell Pepper)",
                cropBengali = "ক্যাপসিকাম (Bell Pepper)",
                emoji = "🫑",
                category = "Vegetable",
                diseases = listOf(
                    "Bacterial Spot (Xanthomonas campestris)",
                    "Healthy Foliage"
                )
            ),
            CropCatalogItem(
                cropName = "Orange (Citrus)",
                cropHindi = "संतरा / नींबू (Citrus / Orange)",
                cropBengali = "কমলালেবু (Citrus / Orange)",
                emoji = "🍊",
                category = "Fruit",
                diseases = listOf(
                    "Huanglongbing (Citrus Greening - Liberibacter)"
                )
            ),
            CropCatalogItem(
                cropName = "Peach",
                cropHindi = "आड़ू (Peach)",
                cropBengali = "পীচ (Peach)",
                emoji = "🍑",
                category = "Fruit",
                diseases = listOf(
                    "Bacterial Spot (Xanthomonas arboricola)",
                    "Healthy Foliage"
                )
            ),
            CropCatalogItem(
                cropName = "Cherry",
                cropHindi = "चेरी (Cherry)",
                cropBengali = "চেরি (Cherry)",
                emoji = "🍒",
                category = "Fruit",
                diseases = listOf(
                    "Powdery Mildew (Podosphaera)",
                    "Healthy Foliage"
                )
            ),
            CropCatalogItem(
                cropName = "Strawberry",
                cropHindi = "स्ट्रॉबेरी (Strawberry)",
                cropBengali = "স্ট্রবেরি (Strawberry)",
                emoji = "🍓",
                category = "Fruit",
                diseases = listOf(
                    "Leaf Scorch (Diplocarpon earlianum)",
                    "Healthy Foliage"
                )
            ),
            CropCatalogItem(
                cropName = "Squash",
                cropHindi = "कद्दू / लौकी (Squash / Gourd)",
                cropBengali = "কুমড়ো / লাউ (Squash)",
                emoji = "🎃",
                category = "Vegetable",
                diseases = listOf(
                    "Powdery Mildew (Erysiphe cichoracearum)"
                )
            ),
            CropCatalogItem(
                cropName = "Soybean",
                cropHindi = "सोयाबीन (Soybean)",
                cropBengali = "সয়াবিন (Soybean)",
                emoji = "🌱",
                category = "Legume",
                diseases = listOf(
                    "Healthy Foliage"
                )
            ),
            CropCatalogItem(
                cropName = "Blueberry & Raspberry",
                cropHindi = "बेरी (Blueberry & Raspberry)",
                cropBengali = "বেরি (Blueberry & Raspberry)",
                emoji = "🫐",
                category = "Fruit",
                diseases = listOf(
                    "Blueberry Healthy",
                    "Raspberry Healthy"
                )
            )
        )
    }

    // Filter crops based on query and category
    val filteredCrops = remember(searchQuery, selectedCategoryFilter, cropsCatalog) {
        cropsCatalog.filter { item ->
            val matchesCategory = selectedCategoryFilter == "All" || item.category.equals(selectedCategoryFilter, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() ||
                    item.cropName.contains(searchQuery, ignoreCase = true) ||
                    item.cropHindi.contains(searchQuery, ignoreCase = true) ||
                    item.cropBengali.contains(searchQuery, ignoreCase = true) ||
                    item.diseases.any { it.contains(searchQuery, ignoreCase = true) }
            matchesCategory && matchesQuery
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = tTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Language Selector Pill
                    Box {
                        Surface(
                            onClick = { showLanguageMenu = true },
                            shape = RoundedCornerShape(20.dp),
                            color = EmeraldPrimary.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f)),
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${selectedLanguage.flag} ${selectedLanguage.nativeName}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = EmeraldPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Language",
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showLanguageMenu,
                            onDismissRequest = { showLanguageMenu = false }
                        ) {
                            AppLanguage.entries.forEach { langOption ->
                                DropdownMenuItem(
                                    text = {
                                        Text("${langOption.flag} ${langOption.nativeName} (${langOption.englishName})")
                                    },
                                    onClick = {
                                        selectedLanguage = langOption
                                        languageManager.setLanguage(langOption)
                                        showLanguageMenu = false
                                    },
                                    leadingIcon = {
                                        if (langOption == selectedLanguage) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = EmeraldPrimary
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Hero Banner
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = EmeraldPrimary.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(EmeraldPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OfflineBolt,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "MobileNetV2 Edge Classifier",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "model_4_mobilenet_finetuned.tflite (13.8 MB)",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = tSubtitle,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 20.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        // Key Metric Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MetricChip(title = "Classes", value = "43 Disease Types")
                            MetricChip(title = "Latency", value = "45ms - 120ms")
                            MetricChip(title = "Internet", value = "0% Required")
                        }
                    }
                }
            }

            // Training Datasets & Provenance
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = tDatasetsTitle,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        DatasetFeatureRow(
                            emoji = "🌿",
                            title = "PlantVillage Global Repository",
                            desc = "54,303 high-resolution laboratory and field crop leaf images across 14 commercial agricultural crops with gold-standard pathologist ground truth."
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        DatasetFeatureRow(
                            emoji = "🌾",
                            title = "ICAR Indian Agriculture Database",
                            desc = "Indigenous Indian crop pathologies, specifically trained on Indian Rice Paddy (Bacterial Leaf Blight, Sheath Blight, Brown Spot) and Maize Rust."
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        DatasetFeatureRow(
                            emoji = "🔄",
                            title = "CUDA Botanical Augmentation",
                            desc = "Random zoom (15%), rotations (20%), and luminance transforms simulated across varying farm lighting, dust, and direct sunlight conditions."
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        DatasetFeatureRow(
                            emoji = "🛡️",
                            title = "Pre-Inference Foliage Quality Guard",
                            desc = "Real-time OpenCV/Color saturation & variance validator (< 3ms) that rejects blank walls, camera lenses, tables, and non-crop objects."
                        )
                    }
                }
            }

            // Important Leaf Notice
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SolarGold.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, SolarGold.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = SolarGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = tNoticeTitle,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tNoticeBody,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 18.sp
                                )
                            )
                        }
                    }
                }
            }

            // Search & Category Filters
            item {
                Column {
                    Text(
                        text = tCatalogTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(tSearchPlaceholder, fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = EmeraldPrimary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Filter chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val categories = listOf("All", "Vegetable", "Cereal", "Fruit", "Legume")
                        items(categories) { cat ->
                            val isSelected = selectedCategoryFilter == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategoryFilter = cat },
                                label = { Text(cat) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Crops List
            items(filteredCrops) { cropItem ->
                CropDetailCard(cropItem = cropItem, currentLanguage = selectedLanguage)
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun MetricChip(title: String, value: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary,
                    fontSize = 11.5.sp
                )
            )
        }
    }
}

@Composable
private fun DatasetFeatureRow(emoji: String, title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(text = emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 17.sp
                )
            )
        }
    }
}

@Composable
private fun CropDetailCard(cropItem: CropCatalogItem, currentLanguage: AppLanguage) {
    var expanded by remember { mutableStateOf(false) }

    val localizedCropTitle = when (currentLanguage) {
        AppLanguage.HINDI -> cropItem.cropHindi
        AppLanguage.BENGALI -> cropItem.cropBengali
        else -> cropItem.cropName
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = cropItem.emoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = localizedCropTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${cropItem.diseases.size} conditions recognized",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldPrimary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = cropItem.category,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    cropItem.diseases.forEach { diseaseName ->
                        val isHealthy = diseaseName.contains("Healthy", ignoreCase = true)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (isHealthy) EmeraldPrimary else Color(0xFFE53935))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = diseaseName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    color = if (isHealthy) EmeraldPrimary else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isHealthy) FontWeight.SemiBold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
