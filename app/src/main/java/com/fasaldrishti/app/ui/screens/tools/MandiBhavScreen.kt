package com.fasaldrishti.app.ui.screens.tools

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.fasaldrishti.app.data.remote.AgmarknetClient
import com.fasaldrishti.app.domain.model.MandiRecord
import com.fasaldrishti.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MandiBhavScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val agmarknetClient = remember { AgmarknetClient(context) }
    val coroutineScope = rememberCoroutineScope()

    var selectedState by remember { mutableStateOf<String?>(agmarknetClient.getSavedState()) }
    var isDetectingLocation by remember { mutableStateOf(false) }
    var isLocationAutoDetected by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCropCategory by remember { mutableStateOf("All Crops") }
    var selectedDistrict by remember { mutableStateOf("All Districts") }

    var isLiveSyncing by remember { mutableStateOf(false) }
    var stateRecords by remember { mutableStateOf<List<MandiRecord>>(emptyList()) }
    var showStatePickerSheet by remember { mutableStateOf(false) }
    var stateSearchQuery by remember { mutableStateOf("") }

    val allStates = remember { agmarknetClient.supportedStates }

    val cropCategories = listOf(
        "All Crops",
        "Wheat & Cereals",
        "Paddy & Rice",
        "Mustard & Oilseeds",
        "Pulses & Chana",
        "Vegetables & Spices",
        "Cotton & Fibres"
    )

    // Request Location Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            coroutineScope.launch {
                isDetectingLocation = true
                val detected = agmarknetClient.detectStateFromLocation()
                if (!detected.isNullOrBlank()) {
                    selectedState = detected
                    isLocationAutoDetected = true
                    agmarknetClient.setPreferredState(detected)
                }
                isDetectingLocation = false
            }
        }
    }

    fun triggerLocationDetection() {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            coroutineScope.launch {
                isDetectingLocation = true
                val detected = agmarknetClient.detectStateFromLocation()
                if (!detected.isNullOrBlank()) {
                    selectedState = detected
                    isLocationAutoDetected = true
                    agmarknetClient.setPreferredState(detected)
                }
                isDetectingLocation = false
            }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Auto-detect GPS location on screen launch if no state is saved yet
    LaunchedEffect(Unit) {
        if (selectedState.isNullOrBlank()) {
            val hasFine = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (hasFine || hasCoarse) {
                isDetectingLocation = true
                val detected = agmarknetClient.detectStateFromLocation()
                if (!detected.isNullOrBlank()) {
                    selectedState = detected
                    isLocationAutoDetected = true
                    agmarknetClient.setPreferredState(detected)
                }
                isDetectingLocation = false
            }
        }
    }

    // Load data for selected state
    fun loadStateData(state: String, isForceLiveSync: Boolean = false) {
        coroutineScope.launch {
            if (isForceLiveSync) isLiveSyncing = true
            try {
                val records = agmarknetClient.fetchLiveStateMandiRates(state)
                stateRecords = records
            } catch (_: Exception) {
                stateRecords = agmarknetClient.getStateBaselineRecords(state)
            } finally {
                isLiveSyncing = false
            }
        }
    }

    LaunchedEffect(selectedState) {
        selectedState?.let { state ->
            agmarknetClient.setPreferredState(state)
            selectedDistrict = "All Districts"
            loadStateData(state)
        }
    }

    // Extract unique districts in this state
    val stateDistricts = remember(stateRecords) {
        val dists = stateRecords.map { it.district }.distinct().sorted()
        listOf("All Districts") + dists
    }

    // Filter records within the selected state
    val filteredRecords = stateRecords.filter { record ->
        val matchesDistrict = selectedDistrict == "All Districts" || record.district.equals(selectedDistrict, ignoreCase = true)

        val matchesCategory = when (selectedCropCategory) {
            "Wheat & Cereals" -> record.commodity.contains("Wheat", ignoreCase = true) || record.commodity.contains("Maize", ignoreCase = true) || record.commodity.contains("Millet", ignoreCase = true) || record.commodity.contains("Ragi", ignoreCase = true)
            "Paddy & Rice" -> record.commodity.contains("Paddy", ignoreCase = true) || record.commodity.contains("Rice", ignoreCase = true)
            "Mustard & Oilseeds" -> record.commodity.contains("Mustard", ignoreCase = true) || record.commodity.contains("Soybean", ignoreCase = true) || record.commodity.contains("Groundnut", ignoreCase = true) || record.commodity.contains("Coconut", ignoreCase = true)
            "Pulses & Chana" -> record.commodity.contains("Gram", ignoreCase = true) || record.commodity.contains("Chana", ignoreCase = true) || record.commodity.contains("Tur", ignoreCase = true) || record.commodity.contains("Pea", ignoreCase = true)
            "Vegetables & Spices" -> record.commodity.contains("Potato", ignoreCase = true) || record.commodity.contains("Tomato", ignoreCase = true) || record.commodity.contains("Onion", ignoreCase = true) || record.commodity.contains("Garlic", ignoreCase = true) || record.commodity.contains("Chilli", ignoreCase = true) || record.commodity.contains("Turmeric", ignoreCase = true) || record.commodity.contains("Cumin", ignoreCase = true)
            "Cotton & Fibres" -> record.commodity.contains("Cotton", ignoreCase = true) || record.commodity.contains("Jute", ignoreCase = true)
            else -> true
        }

        val matchesSearch = searchQuery.isBlank() ||
                record.commodity.contains(searchQuery, ignoreCase = true) ||
                record.varietyDetail.contains(searchQuery, ignoreCase = true) ||
                record.variety.contains(searchQuery, ignoreCase = true) ||
                record.market.contains(searchQuery, ignoreCase = true) ||
                record.district.contains(searchQuery, ignoreCase = true)

        matchesDistrict && matchesCategory && matchesSearch
    }

    val languageManager = remember { com.fasaldrishti.app.data.local.LanguageManager(context) }
    val currentAppLanguage by languageManager.currentLanguage.collectAsState()
    var showHelpGuideDialog by remember { mutableStateOf(false) }

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
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Live Mandi Bhav & APMC Rates",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp
                            )
                        )
                        Text(
                            text = "Official Government Agmarknet & e-NAM Portal",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Help Guide Button (?)
                Surface(
                    onClick = { showHelpGuideDialog = true },
                    shape = CircleShape,
                    color = EmeraldPrimary.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.45f)),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Mandi Rate Guide",
                            modifier = Modifier.size(20.dp),
                            tint = EmeraldPrimary
                        )
                    }
                }
            }
        }
    ) { innerPadding ->

        val currentState = selectedState

        if (currentState == null) {
            // STATE SELECTION REQUIRED SCREEN (When location is off and no state is chosen)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Prompt Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.5.dp, EmeraldPrimary.copy(alpha = 0.45f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            EmeraldPrimary.copy(alpha = 0.18f),
                                            MaterialTheme.colorScheme.surface
                                        )
                                    )
                                )
                                .padding(24.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = EmeraldPrimary,
                                    modifier = Modifier.size(64.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Select Your State / अपना राज्य चुनें",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 19.sp
                                    ),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Apne rajya ke taaza mandi rate aur daily APMC bhav dekhne ke liye apna state select karein ya GPS se auto-detect karein.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                        fontSize = 13.5.sp,
                                        lineHeight = 20.sp
                                    ),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                // Select State Button
                                Button(
                                    onClick = { showStatePickerSheet = true },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EmeraldPrimary,
                                        contentColor = Color.Black
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Map,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Choose State / राज्य चुनें",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Auto-Detect via GPS Button
                                OutlinedButton(
                                    onClick = { triggerLocationDetection() },
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.6f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    enabled = !isDetectingLocation
                                ) {
                                    if (isDetectingLocation) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = EmeraldPrimary,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Detecting Location...",
                                            color = EmeraldPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.MyLocation,
                                            contentDescription = null,
                                            tint = EmeraldPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Auto-Detect via GPS / लोकेशन से पता करें",
                                            color = EmeraldPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Quick Selection Chips for Top States
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Popular Agricultural States / प्रमुख राज्य:",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                fontSize = 13.sp
                            ),
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        val popularStates = listOf(
                            "Uttar Pradesh",
                            "Madhya Pradesh",
                            "Punjab",
                            "Haryana",
                            "Rajasthan",
                            "Maharashtra",
                            "Gujarat",
                            "West Bengal",
                            "Bihar",
                            "Karnataka",
                            "Telangana",
                            "Andhra Pradesh",
                            "Tamil Nadu",
                            "Odisha"
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(popularStates) { state ->
                                Surface(
                                    onClick = {
                                        selectedState = state
                                        agmarknetClient.setPreferredState(state)
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationCity,
                                            contentDescription = null,
                                            tint = EmeraldPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = state,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // MAIN MANDI CONTENT (When state is selected)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. STATE SELECTOR BANNER
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showStatePickerSheet = true },
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.5.dp, EmeraldPrimary.copy(alpha = 0.4f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            EmeraldPrimary.copy(alpha = 0.16f),
                                            Color(0xFF00E5FF).copy(alpha = 0.08f)
                                        )
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = EmeraldPrimary,
                                        modifier = Modifier.size(42.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = Color.Black,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Selected State / Mandi Zone",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 10.5.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                )
                                            )
                                            if (isLocationAutoDetected) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = EmeraldPrimary.copy(alpha = 0.2f)
                                                ) {
                                                    Text(
                                                        text = "GPS Detected",
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = EmeraldPrimary,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = currentState,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 18.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = EmeraldPrimary.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Change State",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldPrimary,
                                                fontSize = 11.sp
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            tint = EmeraldPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. SEARCH BAR & LIVE SYNC BUTTON
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search crop or mandi in $currentState...") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = EmeraldPrimary)
                            },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f)
                        )

                        Surface(
                            onClick = { loadStateData(currentState, isForceLiveSync = true) },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isLiveSyncing) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = EmeraldPrimary, strokeWidth = 2.dp)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Sync",
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. DISTRICT FILTER CHIPS (IF APPLICABLE)
                if (stateDistricts.size > 2) {
                    item {
                        Column {
                            Text(
                                text = "Filter by District ($currentState)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(stateDistricts) { dist ->
                                    val isSelected = dist == selectedDistrict
                                    Surface(
                                        onClick = { selectedDistrict = dist },
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                        )
                                    ) {
                                        Text(
                                            text = dist,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. CROP CATEGORY CHIPS
                item {
                    Column {
                        Text(
                            text = "Crop Category",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(cropCategories) { cat ->
                                val isSelected = cat == selectedCropCategory
                                Surface(
                                    onClick = { selectedCropCategory = cat },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) Color(0xFF00E5FF) else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) Color(0xFF00E5FF) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                    )
                                ) {
                                    Text(
                                        text = cat,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. RESULTS COUNT BAR & GOVT ATTRIBUTION
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Showing ${filteredRecords.size} Mandi Rates in $currentState",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary,
                                fontSize = 12.sp
                            )
                        )
                        Text(
                            text = "🟢 Agmarknet Live",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // 6. MANDI RATE CARDS
                if (filteredRecords.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 35.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No mandi records found for '$searchQuery' in $currentState",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        searchQuery = ""
                                        selectedCropCategory = "All Crops"
                                        selectedDistrict = "All Districts"
                                        loadStateData(currentState, isForceLiveSync = true)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                                ) {
                                    Text(text = "Reset Filters & Sync", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    items(filteredRecords) { record ->
                        MandiCard(record = record)
                    }
                }
            }
        }
    }

    // STATE SELECTION MODAL BOTTOM SHEET
    if (showStatePickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showStatePickerSheet = false },
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Select Your State / APMC Zone",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                )
                Text(
                    text = "Choose your state to view verified local mandi wholesale rates",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Auto-Detect GPS Button in Sheet
                Surface(
                    onClick = {
                        showStatePickerSheet = false
                        triggerLocationDetection()
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = EmeraldPrimary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Auto-Detect My State from GPS",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary,
                                fontSize = 13.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = stateSearchQuery,
                    onValueChange = { stateSearchQuery = it },
                    placeholder = { Text("Search state name (e.g. Rajasthan, Bengal...)") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = EmeraldPrimary)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                val filteredStates = allStates.filter {
                    stateSearchQuery.isBlank() || it.contains(stateSearchQuery, ignoreCase = true)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredStates) { state ->
                        val isSelected = state == selectedState
                        Surface(
                            onClick = {
                                selectedState = state
                                isLocationAutoDetected = false
                                agmarknetClient.setPreferredState(state)
                                showStatePickerSheet = false
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) EmeraldPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationCity,
                                        contentDescription = null,
                                        tint = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = state,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // MANDI RATES HELP GUIDE DIALOG (LOCALIZED)
    if (showHelpGuideDialog) {
        MandiHelpGuideDialog(
            language = currentAppLanguage,
            onDismiss = { showHelpGuideDialog = false }
        )
    }
}

private data class MandiHelpItem(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val description: String,
    val accentColor: Color
)

private data class MandiHelpGuideContent(
    val title: String,
    val subtitle: String,
    val items: List<MandiHelpItem>,
    val footerNote: String,
    val closeButtonText: String
)

@Composable
private fun MandiHelpGuideDialog(
    language: com.fasaldrishti.app.data.local.AppLanguage,
    onDismiss: () -> Unit
) {
    val content = remember(language) { getLocalizedMandiHelpGuide(language) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
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

private fun getLocalizedMandiHelpGuide(language: com.fasaldrishti.app.data.local.AppLanguage): MandiHelpGuideContent {
    return when (language) {
        com.fasaldrishti.app.data.local.AppLanguage.HINDI -> MandiHelpGuideContent(
            title = "मंडी भाव एवं दरों की समझ",
            subtitle = "APMC नीलामी एवं बाजार भाव की मार्गदर्शिका",
            items = listOf(
                MandiHelpItem(
                    icon = Icons.Default.CurrencyRupee,
                    title = "1. मॉडल भाव (Modal Rate)",
                    description = "मंडी में वह औसत नीलामी मूल्य (प्रति क्विंटल / 100 किग्रा) जिस पर सबसे अधिक मात्रा में फसल बिकी है। किसान को आज यही भाव मिलने की सबसे अधिक संभावना होती है।",
                    accentColor = EmeraldPrimary
                ),
                MandiHelpItem(
                    icon = Icons.Default.Tune,
                    title = "2. न्यूनतम - अधिकतम भाव (Price Range)",
                    description = "कमजोर/नमी वाली फसल के सबसे कम दाम (Min) से लेकर उत्तम ग्रेड-A फसल के सबसे ऊंचे दाम (Max) का दायरा।",
                    accentColor = Color(0xFF00E5FF)
                ),
                MandiHelpItem(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    title = "3. दैनिक रुझान (Daily Trend)",
                    description = "कल के मुकाबले आज का भाव बढ़ा (+▲ तेजी) या घटा (-▼ मंदी) है।",
                    accentColor = SolarGold
                ),
                MandiHelpItem(
                    icon = Icons.Default.Psychology,
                    title = "4. बेचें या रोकें (AI Advisory)",
                    description = "🟢 SELL: आज मंडी में मांग और भाव अच्छा है, फसल बेचना फायदेमंद है। ⏳ HOLD: कुछ दिन बाद दाम बढ़ने की संभावना है, फसल रोक कर रखें।",
                    accentColor = Color(0xFF818CF8)
                )
            ),
            footerNote = "💡 सभी भाव प्रति क्विंटल (100 किलोग्राम) के आधार पर आधिकारिक Agmarknet एवं e-NAM पोर्टल से लिए गए हैं।",
            closeButtonText = "समझ गया (Close)"
        )

        com.fasaldrishti.app.data.local.AppLanguage.BENGALI -> MandiHelpGuideContent(
            title = "মান্ডি দর ও মূল্য সহায়িকা",
            subtitle = "APMC নিলাম ও বাজার মূল্যের সহজ নির্দেশিকা",
            items = listOf(
                MandiHelpItem(
                    icon = Icons.Default.CurrencyRupee,
                    title = "১. মডেল দর (Modal Rate)",
                    description = "যে মূল্যে মন্ডিতে সবচেয়ে বেশি পরিমাণ ফসল বিক্রি হয়েছে (প্রতি কুইন্টাল / ১০০ কেজি)। কৃষকের এই দাম পাওয়ার সম্ভাবনা সবচেয়ে বেশি।",
                    accentColor = EmeraldPrimary
                ),
                MandiHelpItem(
                    icon = Icons.Default.Tune,
                    title = "২. সর্বনিম্ন - সর্বোচ্চ দর (Price Range)",
                    description = "সাধারণ/আর্দ্র মান থেকে শুরু করে প্রিমিয়াম গ্রেড-A ফসলের মূল্যের বিস্তার।",
                    accentColor = Color(0xFF00E5FF)
                ),
                MandiHelpItem(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    title = "৩. দৈনিক প্রবণতা (Daily Trend)",
                    description = "গতকালের চেয়ে আজকের দাম বৃদ্ধি (+▲) বা হ্রাস (-▼) নির্দেশ করে।",
                    accentColor = SolarGold
                ),
                MandiHelpItem(
                    icon = Icons.Default.Psychology,
                    title = "৪. বিক্রি বা ধরে রাখা (AI Advisory)",
                    description = "🟢 SELL: বাজারে ভালো দাম ও চাহিদা রয়েছে। ⏳ HOLD: কয়েকদিন পর দর বাড়ার সম্ভাবনা রয়েছে।",
                    accentColor = Color(0xFF818CF8)
                )
            ),
            footerNote = "💡 সরকারি Agmarknet ও e-NAM পোর্টাল অনুযায়ী প্রতি কুইন্টাল (১০০ কেজি) হিসেবে মূল্য প্রদর্শিত।",
            closeButtonText = "বুঝেছি (Close)"
        )

        com.fasaldrishti.app.data.local.AppLanguage.MARATHI -> MandiHelpGuideContent(
            title = "मंडी दर व भावाची माहिती",
            subtitle = "APMC बाजारभाव व लिलाव मार्गदर्शिका",
            items = listOf(
                MandiHelpItem(
                    icon = Icons.Default.CurrencyRupee,
                    title = "१. मॉडल भाव (Modal Rate)",
                    description = "ज्या दरावर बाजारात सर्वाधिक शेतमालाची विक्री झाली आहे (प्रति क्विंटल / १०० किलो). शेतकऱ्याला हाच भाव मिळण्याची सर्वाधिक शक्यता असते.",
                    accentColor = EmeraldPrimary
                ),
                MandiHelpItem(
                    icon = Icons.Default.Tune,
                    title = "२. किमान - कमाल भाव (Price Range)",
                    description = "कमी दर्जाच्या मालापासून ते उत्कृष्ट ग्रेड-A शेतमालाच्या दराची कक्षा.",
                    accentColor = Color(0xFF00E5FF)
                ),
                MandiHelpItem(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    title = "३. दैनंदिन कल (Daily Trend)",
                    description = "कालच्या तुलनेत आज भाव वाढला (+▲) किंवा घसरला (-▼) आहे.",
                    accentColor = SolarGold
                ),
                MandiHelpItem(
                    icon = Icons.Default.Psychology,
                    title = "४. विक्री किंवा साठवणूक (AI Advisory)",
                    description = "🟢 SELL: आज बाजारात चांगला भाव आहे. ⏳ HOLD: काही दिवसांनी भाव वाढण्याची शक्यता आहे.",
                    accentColor = Color(0xFF818CF8)
                )
            ),
            footerNote = "💡 सर्व दर प्रति क्विंटल (१०० किलोग्रॅम) नुसार Agmarknet पोर्टलवरून घेतलेले आहेत.",
            closeButtonText = "समजले (Close)"
        )

        com.fasaldrishti.app.data.local.AppLanguage.PUNJABI -> MandiHelpGuideContent(
            title = "ਮੰਡੀ ਭਾਅ ਅਤੇ ਦਰਾਂ ਬਾਰੇ ਗਾਈਡ",
            subtitle = "APMC ਨਿਲਾਮੀ ਅਤੇ ਮੰਡੀ ਭਾਅ ਦੀ ਜਾਣਕਾਰੀ",
            items = listOf(
                MandiHelpItem(
                    icon = Icons.Default.CurrencyRupee,
                    title = "1. ਮਾਡਲ ਰੇਟ (Modal Rate)",
                    description = "ਉਹ ਔਸਤ ਭਾਅ ਜਿਸ ਉੱਤੇ ਮੰਡੀ ਵਿੱਚ ਸਭ ਤੋਂ ਵੱਧ ਫ਼ਸਲ ਵਿਕੀ ਹੈ (ਪ੍ਰਤੀ ਕੁਇੰਟਲ / 100 ਕਿਲੋ)।",
                    accentColor = EmeraldPrimary
                ),
                MandiHelpItem(
                    icon = Icons.Default.Tune,
                    title = "2. ਘੱਟੋ-ਘੱਟ - ਵੱਧ ਤੋਂ ਵੱਧ ਭਾਅ",
                    description = "ਆਮ ਕੁਆਲਿਟੀ ਤੋਂ ਲੈ ਕੇ ਟੌਪ ਗ੍ਰੇਡ-A ਫ਼ਸਲ ਦੇ ਭਾਅ ਦਾ ਦਾਇਰਾ।",
                    accentColor = Color(0xFF00E5FF)
                ),
                MandiHelpItem(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    title = "3. ਰੋਜ਼ਾਨਾ ਰੁਝਾਨ (Daily Trend)",
                    description = "ਕੱਲ੍ਹ ਦੇ ਮੁਕਾਬਲੇ ਅੱਜ ਦਾ ਭਾਅ ਵਧਿਆ (+▲) ਜਾਂ ਘਟਿਆ (-▼) ਹੈ।",
                    accentColor = SolarGold
                ),
                MandiHelpItem(
                    icon = Icons.Default.Psychology,
                    title = "4. ਵੇਚੋ ਜਾਂ ਰੋਕੋ (AI Advisory)",
                    description = "🟢 SELL: ਫ਼ਸਲ ਵੇਚਣ ਦਾ ਸਹੀ ਸਮਾਂ ਹੈ। ⏳ HOLD: ਕੁਝ ਦਿਨ ਫ਼ਸਲ ਰੋਕ ਕੇ ਰੱਖਣਾ ਫ਼ਾਇਦੇਮੰਦ ਹੋ ਸਕਦਾ ਹੈ।",
                    accentColor = Color(0xFF818CF8)
                )
            ),
            footerNote = "💡 ਸਾਰੇ ਭਾਅ ਪ੍ਰਤੀ ਕੁਇੰਟਲ (100 ਕਿਲੋਗ੍ਰਾਮ) ਸਰਕਾਰੀ Agmarknet ਪੋਰਟਲ ਅਨੁਸਾਰ ਹਨ।",
            closeButtonText = "ਸਮਝ ਆ ਗਿਆ (Close)"
        )

        com.fasaldrishti.app.data.local.AppLanguage.GUJARATI -> MandiHelpGuideContent(
            title = "મંડી ભાવ અને દરોની માર્ગદર્શિકા",
            subtitle = "APMC હરાજી અને બજાર ભાવની સરળ સમજૂતી",
            items = listOf(
                MandiHelpItem(
                    icon = Icons.Default.CurrencyRupee,
                    title = "૧. મોડલ ભાવ (Modal Rate)",
                    description = "જે ભાવે બજારમાં સૌથી વધુ જથ્થામાં પાક વેચાયો છે (પ્રતિ ક્વિન્ટલ / ૧૦૦ કિગ્રા).",
                    accentColor = EmeraldPrimary
                ),
                MandiHelpItem(
                    icon = Icons.Default.Tune,
                    title = "૨. ન્યૂનતમ - મહત્તમ ભાવ",
                    description = "સામાન્ય ગુણવત્તાથી લઈને શ્રેષ્ઠ ગ્રેડ-A પાકના ભાવોની શ્રેણી.",
                    accentColor = Color(0xFF00E5FF)
                ),
                MandiHelpItem(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    title = "૩. દૈનિક વલણ (Daily Trend)",
                    description = "ગઈકાલ કરતાં આજે ભાવ વધ્યો (+▲) કે ઘટ્યો (-▼) છે.",
                    accentColor = SolarGold
                ),
                MandiHelpItem(
                    icon = Icons.Default.Psychology,
                    title = "૪. વેચો કે હોલ્ડ કરો (AI Advisory)",
                    description = "🟢 SELL: આજે પાક વેચવો ફાયદાકારક છે. ⏳ HOLD: થોડા દિવસ સાચવી રાખવાથી સારો ભાવ મળી શકે છે.",
                    accentColor = Color(0xFF818CF8)
                )
            ),
            footerNote = "💡 તમામ ભાવો પ્રતિ ક્વિન્ટલ (૧૦૦ કિલોગ્રામ) ના આધારે Agmarknet પરથી છે.",
            closeButtonText = "સમજાઈ ગયું (Close)"
        )

        com.fasaldrishti.app.data.local.AppLanguage.TELUGU -> MandiHelpGuideContent(
            title = "మండీ ధరల మార్గదర్శిని",
            subtitle = "APMC వేలం మరియు మార్కెట్ ధరల వివరణ",
            items = listOf(
                MandiHelpItem(
                    icon = Icons.Default.CurrencyRupee,
                    title = "1. మోడల్ ధర (Modal Rate)",
                    description = "మార్కెట్‌లో అత్యధిక పరిమాణంలో పంట అమ్ముడైన సగటు ధర (క్వింటాల్ / 100 కేజీలకు).",
                    accentColor = EmeraldPrimary
                ),
                MandiHelpItem(
                    icon = Icons.Default.Tune,
                    title = "2. కనిష్ట - గరిష్ట ధర (Price Range)",
                    description = "సాధారణ నాణ్యత నుండి ప్రీమియం గ్రేడ్-A పంట ధరల శ్రేణి.",
                    accentColor = Color(0xFF00E5FF)
                ),
                MandiHelpItem(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    title = "3. రోజువారీ ధోరణి (Daily Trend)",
                    description = "నిన్నటితో పోలిస్తే ఈరోజు ధర పెరిగిందా (+▲) లేదా తగ్గిందా (-▼).",
                    accentColor = SolarGold
                ),
                MandiHelpItem(
                    icon = Icons.Default.Psychology,
                    title = "4. అమ్మండి లేదా నిల్వ ఉంచండి (AI Advisory)",
                    description = "🟢 SELL: ఈరోజు మంచి ధర ఉంది. ⏳ HOLD: కొద్ది రోజులు ఆగి అమ్మడం మంచిది.",
                    accentColor = Color(0xFF818CF8)
                )
            ),
            footerNote = "💡 ధరలు క్వింటాల్ (100 కేజీలు) ప్రాతిపదికన Agmarknet ద్వారా అందించబడుతున్నాయి.",
            closeButtonText = "అర్థమైంది (Close)"
        )

        com.fasaldrishti.app.data.local.AppLanguage.TAMIL -> MandiHelpGuideContent(
            title = "மண்டி விலை வழிகாட்டி",
            subtitle = "APMC ஏல விற்பனை விலை விவரங்கள்",
            items = listOf(
                MandiHelpItem(
                    icon = Icons.Default.CurrencyRupee,
                    title = "1. மாதிரி விலை (Modal Rate)",
                    description = "சந்தையில் அதிகளவில் பயிர் விற்கப்பட்ட சராசரி விலை (குவிண்டால் / 100 கிலோவுக்கு).",
                    accentColor = EmeraldPrimary
                ),
                MandiHelpItem(
                    icon = Icons.Default.Tune,
                    title = "2. குறைந்தபட்ச - அதிகபட்ச விலை",
                    description = "சராசரி தரம் முதல் முதல்-தரம் (Grade-A) வரையிலான விலை வரம்பு.",
                    accentColor = Color(0xFF00E5FF)
                ),
                MandiHelpItem(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    title = "3. தினசரி போக்கு (Daily Trend)",
                    description = "நேற்றைய விலையை விட இன்று விலை உயர்ந்துள்ளதா (+▲) அல்லது குறைந்துள்ளதா (-▼).",
                    accentColor = SolarGold
                ),
                MandiHelpItem(
                    icon = Icons.Default.Psychology,
                    title = "4. விற்கவும் அல்லது வைக்கவும் (AI Advisory)",
                    description = "🟢 SELL: இன்று நல்ல விலை உள்ளது. ⏳ HOLD: சில நாட்கள் கழித்து விற்கலாம்.",
                    accentColor = Color(0xFF818CF8)
                )
            ),
            footerNote = "💡 அனைத்து விலைகளும் அரசு Agmarknet போர்ட்டல் மூலம் குவிண்டால் வீதம் கணக்கிடப்படுகிறது.",
            closeButtonText = "புரிந்தது (Close)"
        )

        com.fasaldrishti.app.data.local.AppLanguage.KANNADA -> MandiHelpGuideContent(
            title = "ಮಂಡಿ ದರಗಳ ಮಾರ್ಗದರ್ಶಿ",
            subtitle = "APMC ಮಾರುಕಟ್ಟೆ ಹರಾಜು ದರಗಳ ವಿವರಣೆ",
            items = listOf(
                MandiHelpItem(
                    icon = Icons.Default.CurrencyRupee,
                    title = "1. ಮಾದರಿ ದರ (Modal Rate)",
                    description = "ಮಾರುಕಟ್ಟೆಯಲ್ಲಿ ಗರಿಷ್ಠ ಪ್ರಮಾಣದ ಬೆಳೆ ಮಾರಾಟವಾದ ಸರಾಸರಿ ದರ (ಕ್ವಿಂಟಾಲ್ / 100 ಕೆಜಿಗೆ).",
                    accentColor = EmeraldPrimary
                ),
                MandiHelpItem(
                    icon = Icons.Default.Tune,
                    title = "2. ಕನಿಷ್ಠ - ಗರಿಷ್ಠ ದರ (Price Range)",
                    description = "ಸಾಮಾನ್ಯ ಗುಣಮಟ್ಟದಿಂದ ಉತ್ತಮ ಗ್ರೇಡ್-A ಬೆಳೆಯ ದರ ಶ್ರೇಣಿ.",
                    accentColor = Color(0xFF00E5FF)
                ),
                MandiHelpItem(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    title = "3. ದೈನಂದಿನ ಪ್ರವೃತ್ತಿ (Daily Trend)",
                    description = "ನಿನ್ನೆಯ ದರಕ್ಕೆ ಹೋಲಿಸಿದರೆ ಇಂದು ದರ ಏರಿದೆಯೆ (+▲) ಅಥವಾ ಇಳಿದಿದೆಯೆ (-▼).",
                    accentColor = SolarGold
                ),
                MandiHelpItem(
                    icon = Icons.Default.Psychology,
                    title = "4. ಮಾರಾಟ ಅಥವಾ ಹೋಲ್ಡ್ (AI Advisory)",
                    description = "🟢 SELL: ಇಂದು ಮಾರಾಟ ಮಾಡಲು ಉತ್ತಮ ದರವಿದೆ. ⏳ HOLD: ಕೆಲ ದಿನಗಳ ನಂತರ ಮಾರಾಟ ಮಾಡುವುದು ಸೂಕ್ತ.",
                    accentColor = Color(0xFF818CF8)
                )
            ),
            footerNote = "💡 ಎಲ್ಲಾ ದರಗಳು ಕ್ವಿಂಟಾಲ್ (100 ಕೆಜಿ) ಆಧಾರದ ಮೇಲೆ Agmarknet ಪೋರ್ಟಲ್‌ನಿಂದ ನೀಡಲಾಗಿದೆ.",
            closeButtonText = "ಅರ್ಥವಾಯಿತು (Close)"
        )

        com.fasaldrishti.app.data.local.AppLanguage.ODIA -> MandiHelpGuideContent(
            title = "ମଣ୍ଡି ଦର ଓ ଭାବ ମାର୍ଗଦର୍ଶିକା",
            subtitle = "APMC ବଜାର ଦର ଏବଂ ନିଲାମ ମୂଲ୍ୟର ସରଳ ବ୍ୟାଖ୍ୟା",
            items = listOf(
                MandiHelpItem(
                    icon = Icons.Default.CurrencyRupee,
                    title = "୧. ମଡେଲ ଦର (Modal Rate)",
                    description = "ମଣ୍ଡିରେ ଯେଉଁ ହାରରେ ସର୍ବାଧିକ ଫସଲ ବିକ୍ରି ହୋଇଛି (କ୍ୱିଣ୍ଟାଲ / ୧୦୦ କିଗ୍ରା ପ୍ରତି)।",
                    accentColor = EmeraldPrimary
                ),
                MandiHelpItem(
                    icon = Icons.Default.Tune,
                    title = "୨. ସର୍ବନିମ୍ନ - ସର୍ବାଧିକ ଦର (Price Range)",
                    description = "ସାଧାରଣ ଗୁଣବତ୍ତା ଠାରୁ ପ୍ରିମିୟମ ଗ୍ରେଡ୍-A ଫସଲ ମଧ୍ୟରେ ଦରର ପରିସୀମା।",
                    accentColor = Color(0xFF00E5FF)
                ),
                MandiHelpItem(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    title = "୩. ଦୈନିକ ଧାରା (Daily Trend)",
                    description = "ଗତକାଲି ତୁଳନାରେ ଆଜିର ଦର ବୃଦ୍ଧି (+▲) କିମ୍ବା ହ୍ରାସ (-▼) ପାଇଛି।",
                    accentColor = SolarGold
                ),
                MandiHelpItem(
                    icon = Icons.Default.Psychology,
                    title = "୪. ବିକ୍ରୟ କିମ୍ବା ସଂରକ୍ଷଣ (AI Advisory)",
                    description = "🟢 SELL: ଆଜି ବିକ୍ରି କରିବା ଲାଭଦାୟକ। ⏳ HOLD: କିଛି ଦିନ ରଖିବା ଉଚିତ।",
                    accentColor = Color(0xFF818CF8)
                )
            ),
            footerNote = "💡 ସମସ୍ତ ଦର କ୍ୱିଣ୍ଟାଲ (୧୦୦ କିଲୋଗ୍ରାମ) ଆଧାରରେ ସରକାରୀ Agmarknet ପୋର୍ଟାଲରୁ ଅଣାଯାଇଛି।",
            closeButtonText = "ବୁଝିଲି (Close)"
        )

        else -> MandiHelpGuideContent(
            title = "Understanding Mandi Rates Guide",
            subtitle = "Explanation of APMC Market Auction Terms",
            items = listOf(
                MandiHelpItem(
                    icon = Icons.Default.CurrencyRupee,
                    title = "1. Modal Rate (Main Price)",
                    description = "The primary auction price per Quintal (100 kg) where the maximum quantity of crop lots were traded. This is the most realistic price a farmer can expect today.",
                    accentColor = EmeraldPrimary
                ),
                MandiHelpItem(
                    icon = Icons.Default.Tune,
                    title = "2. Min - Max Range (Quality Range)",
                    description = "The price span between the lowest bid (fair/high-moisture lots) and the highest bid (clean, dried Grade-A lots).",
                    accentColor = Color(0xFF00E5FF)
                ),
                MandiHelpItem(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    title = "3. Daily Trend (Price Movement)",
                    description = "Indicates whether today's modal rate increased (+▲ Price Up) or softened (-▼ Price Down) compared to yesterday's closing rate.",
                    accentColor = SolarGold
                ),
                MandiHelpItem(
                    icon = Icons.Default.Psychology,
                    title = "4. Sell vs Hold (AI Advisory)",
                    description = "🟢 SELL: Strong buyer bidding and healthy profit margin today. ⏳ HOLD: Market arrivals high or prices likely to increase soon.",
                    accentColor = Color(0xFF818CF8)
                )
            ),
            footerNote = "💡 All prices are quoted per Quintal (100 kg) from official Directorate of Marketing & Inspection (DMI) / Agmarknet & e-NAM portals.",
            closeButtonText = "Understood (Close)"
        )
    }
}

@Composable
private fun MandiCard(record: MandiRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Commodity Name & Sell/Hold Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.commodity,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    )
                    Text(
                        text = record.varietyDetail,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (record.isSellFavorable) EmeraldPrimary.copy(alpha = 0.15f) else SolarGold.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, if (record.isSellFavorable) EmeraldPrimary.copy(alpha = 0.4f) else SolarGold.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (record.isSellFavorable) "🟢 SELL" else "⏳ HOLD",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (record.isSellFavorable) EmeraldPrimary else SolarGold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Market & District location
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${record.market}, ${record.district} (${record.state})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontSize = 11.5.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Price Details Grid
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Modal Rate (Main Price)",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "₹${record.modalPrice}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 19.sp,
                                color = EmeraldPrimary
                            )
                        )
                        Text(
                            text = "per Quintal (100 kg)",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Min - Max Range",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "₹${record.minPrice} - ₹${record.maxPrice}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp)
                        )
                        Text(
                            text = "Low / High Quality",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Daily Trend",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.SemiBold)
                        )
                        val isPositive = record.priceChange >= 0
                        Text(
                            text = if (isPositive) "+₹${record.priceChange} ▲" else "-₹${Math.abs(record.priceChange)} ▼",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isPositive) EmeraldPrimary else CrimsonCoral,
                                fontSize = 12.5.sp
                            )
                        )
                        Text(
                            text = if (isPositive) "Price Up" else "Price Down",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = if (isPositive) EmeraldPrimary else CrimsonCoral)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // AI Market Advisory
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.aiAdvice,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                    )
                )
            }
        }
    }
}

