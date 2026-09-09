package com.fasaldrishti.app.ui.screens.tools

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    var selectedState by remember { mutableStateOf(agmarknetClient.getPreferredState()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCropCategory by remember { mutableStateOf("All Crops") }
    var selectedDistrict by remember { mutableStateOf("All Districts") }

    var isLiveSyncing by remember { mutableStateOf(false) }
    var stateRecords by remember { mutableStateOf<List<MandiRecord>>(emptyList()) }
    var showStatePickerSheet by remember { mutableStateOf(false) }
    var stateSearchQuery by remember { mutableStateOf("") }

    val allStates = remember {
        listOf(
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
            "Andhra Pradesh",
            "Telangana",
            "Tamil Nadu",
            "Odisha",
            "Chhattisgarh",
            "Jharkhand",
            "Assam",
            "Kerala",
            "Himachal Pradesh",
            "Uttarakhand"
        )
    }

    val cropCategories = listOf(
        "All Crops",
        "Wheat & Cereals",
        "Paddy & Rice",
        "Mustard & Oilseeds",
        "Pulses & Chana",
        "Vegetables & Spices",
        "Cotton & Fibres"
    )

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
        agmarknetClient.setPreferredState(selectedState)
        selectedDistrict = "All Districts"
        loadStateData(selectedState)
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
                        text = "Live Mandi Bhav & APMC Rates",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                    )
                    Text(
                        text = "Official Government Agmarknet & e-NAM Portal",
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
                                    Text(
                                        text = "Selected State / Mandi Zone",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.5.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    )
                                    Text(
                                        text = selectedState,
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
                        placeholder = { Text("Search crop or mandi in $selectedState...") },
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
                        onClick = { loadStateData(selectedState, isForceLiveSync = true) },
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
                            text = "Filter by District ($selectedState)",
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
                        text = "Showing ${filteredRecords.size} Mandi Rates in $selectedState",
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
                                text = "No mandi records found for '$searchQuery' in $selectedState",
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    searchQuery = ""
                                    selectedCropCategory = "All Crops"
                                    selectedDistrict = "All Districts"
                                    loadStateData(selectedState, isForceLiveSync = true)
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

                OutlinedTextField(
                    value = stateSearchQuery,
                    onValueChange = { stateSearchQuery = it },
                    placeholder = { Text("Search state name (e.g. Uttar Pradesh, Bengal...)") },
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
