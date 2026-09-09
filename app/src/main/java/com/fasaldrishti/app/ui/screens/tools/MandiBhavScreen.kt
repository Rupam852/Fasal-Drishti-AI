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
import com.fasaldrishti.app.domain.model.MandiRecord
import com.fasaldrishti.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MandiBhavScreen(
    onNavigateBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedState by remember { mutableStateOf("All India") }

    val allMandiRecords = remember {
        listOf(
            MandiRecord(
                commodity = "Wheat (Gehu)",
                hindiName = "गेहूं (शरबती / लोकवान)",
                variety = "Sharbati Grade-A",
                market = "Indore APMC Mandi",
                district = "Indore",
                state = "Madhya Pradesh",
                minPrice = 2450,
                maxPrice = 2850,
                modalPrice = 2680,
                priceChange = 120,
                arrivalDate = "Today (10:30 AM)",
                aiAdvice = "🔥 Strong export demand. Favorable market window to sell high-grade grain.",
                isSellFavorable = true
            ),
            MandiRecord(
                commodity = "Paddy (Dhaan)",
                hindiName = "धान (बासमती 1121)",
                variety = "Basmati 1121 Paddy",
                market = "Karnal Grain Market",
                district = "Karnal",
                state = "Punjab",
                minPrice = 3800,
                maxPrice = 4450,
                modalPrice = 4220,
                priceChange = 90,
                arrivalDate = "Today (09:45 AM)",
                aiAdvice = "📈 Steady arrivals with premium bidding. Sell in batches to capture peak price.",
                isSellFavorable = true
            ),
            MandiRecord(
                commodity = "Potato (Aalu)",
                hindiName = "आलू (ज्योति / पुखराज)",
                variety = "Jyoti Fresh",
                market = "Burdwan APMC Mandi",
                district = "Purba Bardhaman",
                state = "West Bengal",
                minPrice = 1150,
                maxPrice = 1420,
                modalPrice = 1310,
                priceChange = -30,
                arrivalDate = "Today (08:15 AM)",
                aiAdvice = "⏳ Supply surge from cold storages. Hold for 7-10 days if proper storage is available.",
                isSellFavorable = false
            ),
            MandiRecord(
                commodity = "Tomato (Tamatar)",
                hindiName = "टमाटर (हाइब्रिड)",
                variety = "Desi Hybrid",
                market = "Azadpur Mandi",
                district = "North Delhi",
                state = "All India",
                minPrice = 1800,
                maxPrice = 2500,
                modalPrice = 2200,
                priceChange = 250,
                arrivalDate = "Today (06:30 AM)",
                aiAdvice = "🚀 Sharp price spike due to regional festival demand. Excellent selling opportunity today!",
                isSellFavorable = true
            ),
            MandiRecord(
                commodity = "Mustard (Sarson)",
                hindiName = "सरसों (काला दाना)",
                variety = "Mustard 42% Oil",
                market = "Kota APMC Mandi",
                district = "Kota",
                state = "Rajasthan",
                minPrice = 5200,
                maxPrice = 5750,
                modalPrice = 5540,
                priceChange = 75,
                arrivalDate = "Today (11:00 AM)",
                aiAdvice = "📊 Oil mill procurement active above MSP. Good profit margin.",
                isSellFavorable = true
            ),
            MandiRecord(
                commodity = "Cotton (Kapas)",
                hindiName = "कपास (मध्यम रेशा)",
                variety = "Medium Staple (Shankar-6)",
                market = "Rajkot APMC",
                district = "Rajkot",
                state = "Gujarat",
                minPrice = 6900,
                maxPrice = 7650,
                modalPrice = 7380,
                priceChange = -110,
                arrivalDate = "Today (10:15 AM)",
                aiAdvice = "⏳ Global cotton index softer today. Hold stock if looking for ₹7,800+ range.",
                isSellFavorable = false
            ),
            MandiRecord(
                commodity = "Onion (Pyaz)",
                hindiName = "प्याज (लाल नासिक)",
                variety = "Nashik Red Regular",
                market = "Lasalgaon Mandi",
                district = "Nashik",
                state = "Maharashtra",
                minPrice = 1950,
                maxPrice = 2650,
                modalPrice = 2380,
                priceChange = 140,
                arrivalDate = "Today (07:00 AM)",
                aiAdvice = "📈 Steady outward transport to South and East zones. Good time to liquidate fresh stock.",
                isSellFavorable = true
            ),
            MandiRecord(
                commodity = "Maize (Makka)",
                hindiName = "मक्का (पीला)",
                variety = "Yellow Feed Grade",
                market = "Bahraich Mandi",
                district = "Bahraich",
                state = "Uttar Pradesh",
                minPrice = 2100,
                maxPrice = 2380,
                modalPrice = 2270,
                priceChange = 40,
                arrivalDate = "Today (09:00 AM)",
                aiAdvice = "🌽 Poultry feed and starch mills buying steadily at current modal rate.",
                isSellFavorable = true
            )
        )
    }

    val states = listOf("All India", "Uttar Pradesh", "West Bengal", "Punjab", "Madhya Pradesh", "Rajasthan", "Maharashtra", "Gujarat")

    val filteredRecords = allMandiRecords.filter { record ->
        val matchesState = selectedState == "All India" || record.state.equals(selectedState, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() ||
                record.commodity.contains(searchQuery, ignoreCase = true) ||
                record.hindiName.contains(searchQuery, ignoreCase = true) ||
                record.market.contains(searchQuery, ignoreCase = true) ||
                record.district.contains(searchQuery, ignoreCase = true)
        matchesState && matchesSearch
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
                        text = "दैनिक मंडी भाव व AI बाजार सलाह",
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
            // 1. SEARCH BAR
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search crop or mandi (गेहूं, आलू, Indore...)") },
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
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 2. STATE FILTER CHIPS
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(states) { state ->
                        val isSelected = state == selectedState
                        Surface(
                            onClick = { selectedState = state },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                            )
                        ) {
                            Text(
                                text = state,
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

            // 3. MANDI RATE CARDS
            if (filteredRecords.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No mandi records found for '$searchQuery'",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        )
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
                        text = record.hindiName,
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
                            text = "Modal (औसत भाव)",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                        )
                        Text(
                            text = "₹${record.modalPrice}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = EmeraldPrimary
                            )
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Range (न्यूनतम - अधिकतम)",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                        )
                        Text(
                            text = "₹${record.minPrice} - ₹${record.maxPrice}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Daily Trend",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
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
