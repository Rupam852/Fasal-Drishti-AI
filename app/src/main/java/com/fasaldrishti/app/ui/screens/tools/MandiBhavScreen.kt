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
    var selectedCropCategory by remember { mutableStateOf("All Crops") }

    val allMandiRecords = remember {
        listOf(
            // --- UTTAR PRADESH ---
            MandiRecord(
                commodity = "Wheat (Gehu)",
                varietyDetail = "Kalyan Sona / PBW-502",
                variety = "Grade-A Milling",
                market = "Kanpur APMC Mandi",
                district = "Kanpur Nagar",
                state = "Uttar Pradesh",
                minPrice = 2380,
                maxPrice = 2580,
                modalPrice = 2490,
                priceChange = 40,
                arrivalDate = "Today (10:15 AM)",
                aiAdvice = "📈 Steady miller demand with government procurement active above MSP.",
                isSellFavorable = true
            ),
            MandiRecord(
                commodity = "Potato (Aalu)",
                varietyDetail = "Chipsona / Pukhraj Fresh",
                variety = "Chipsona Cold-Grade",
                market = "Agra APMC Mandi",
                district = "Agra",
                state = "Uttar Pradesh",
                minPrice = 1200,
                maxPrice = 1480,
                modalPrice = 1350,
                priceChange = -20,
                arrivalDate = "Today (08:30 AM)",
                aiAdvice = "⏳ Substantial arrivals from cold stores. Hold for 10-15 days for better wholesale price.",
                isSellFavorable = false
            ),
            MandiRecord(
                commodity = "Paddy (Dhaan)",
                varietyDetail = "PR-126 Common Paddy",
                variety = "Paddy Grade-A",
                market = "Varanasi Grain Mandi",
                district = "Varanasi",
                state = "Uttar Pradesh",
                minPrice = 2200,
                maxPrice = 2450,
                modalPrice = 2320,
                priceChange = 60,
                arrivalDate = "Today (09:00 AM)",
                aiAdvice = "🚀 Rice mills buying actively for export consignments. Good selling window.",
                isSellFavorable = true
            ),
            MandiRecord(
                commodity = "Mustard (Sarson)",
                varietyDetail = "Yellow / Black Hybrid",
                variety = "Mustard 42% Oil",
                market = "Mathura APMC",
                district = "Mathura",
                state = "Uttar Pradesh",
                minPrice = 5300,
                maxPrice = 5820,
                modalPrice = 5600,
                priceChange = 110,
                arrivalDate = "Today (11:00 AM)",
                aiAdvice = "🔥 Edible oil processors bidding strong. Highly favorable time to sell.",
                isSellFavorable = true
            ),
            MandiRecord(
                commodity = "Maize (Makka)",
                varietyDetail = "Yellow Feed Grade",
                variety = "Yellow Feed Grade",
                market = "Bahraich Mandi",
                district = "Bahraich",
                state = "Uttar Pradesh",
                minPrice = 2120,
                maxPrice = 2400,
                modalPrice = 2280,
                priceChange = 30,
                arrivalDate = "Today (09:45 AM)",
                aiAdvice = "🌽 Feed and starch industry off-take is high.",
                isSellFavorable = true
            ),

            // --- MADHYA PRADESH ---
            MandiRecord(
                commodity = "Wheat (Gehu)",
                varietyDetail = "Sharbati / Lokwan Premium",
                variety = "Sharbati Grade-A",
                market = "Indore APMC Mandi",
                district = "Indore",
                state = "Madhya Pradesh",
                minPrice = 2550,
                maxPrice = 3100,
                modalPrice = 2820,
                priceChange = 150,
                arrivalDate = "Today (10:30 AM)",
                aiAdvice = "🔥 High demand from premium flour mills across South India. Favorable to liquidate.",
                isSellFavorable = true
            ),
            MandiRecord(
                commodity = "Soybean (Pili)",
                varietyDetail = "JS-9560 / JS-2034",
                variety = "Yellow Soybean",
                market = "Ujjain Grain Mandi",
                district = "Ujjain",
                state = "Madhya Pradesh",
                minPrice = 4350,
                maxPrice = 4850,
                modalPrice = 4640,
                priceChange = 80,
                arrivalDate = "Today (11:15 AM)",
                aiAdvice = "📈 Crushing demand picking up; consider selling around ₹4,700 mark.",
                isSellFavorable = true
            ),
            MandiRecord(
                commodity = "Gram (Chana)",
                varietyDetail = "Desi / Vishal Chana",
                variety = "Desi Chana",
                market = "Vidisha Mandi",
                district = "Vidisha",
                state = "Madhya Pradesh",
                minPrice = 5700,
                maxPrice = 6250,
                modalPrice = 6020,
                priceChange = 130,
                arrivalDate = "Today (09:30 AM)",
                aiAdvice = "🚀 Pulses demand strong ahead of festival procurement. Excellent selling window.",
                isSellFavorable = true
            ),
            MandiRecord(
                commodity = "Garlic (Lahsun)",
                varietyDetail = "Ooty / Amleta Grade-A",
                variety = "Bulb Garlic",
                market = "Mandsaur APMC",
                district = "Mandsaur",
                state = "Madhya Pradesh",
                minPrice = 9000,
                maxPrice = 14500,
                modalPrice = 12200,
                priceChange = 400,
                arrivalDate = "Today (08:45 AM)",
                aiAdvice = "🔥 Top market rates of the season. Sell graded bold bulbs today.",
                isSellFavorable = true
            ),

            // --- PUNJAB ---
            MandiRecord(
                commodity = "Paddy (Dhaan)",
                varietyDetail = "Basmati 1121 Paddy",
                variety = "Basmati 1121 Paddy",
                market = "Amritsar Grain Market",
                district = "Amritsar",
                state = "Punjab",
                minPrice = 3900,
                maxPrice = 4520,
                modalPrice = 4280,
                priceChange = 95,
                arrivalDate = "Today (09:45 AM)",
                aiAdvice = "📈 Steady arrivals with premium export bidding. Sell in planned batches.",
                isSellFavorable = true
            ),
            MandiRecord(
                commodity = "Wheat (Gehu)",
                varietyDetail = "HD-3086 / PBW-725",
                variety = "Milling Wheat",
                market = "Ludhiana APMC",
                district = "Ludhiana",
                state = "Punjab",
                minPrice = 2350,
                maxPrice = 2500,
                modalPrice = 2440,
                priceChange = 20,
                arrivalDate = "Today (10:00 AM)",
                aiAdvice = "🌾 Government agency MSP purchase active.",
                isSellFavorable = true
            ),
            MandiRecord(
                commodity = "Cotton (Kapas)",
                varietyDetail = "Medium Staple BT",
                variety = "BT Cotton",
                market = "Bathinda Mandi",
                district = "Bathinda",
                state = "Punjab",
                minPrice = 6700,
                maxPrice = 7350,
                modalPrice = 7050,
                priceChange = -80,
                arrivalDate = "Today (10:45 AM)",
                aiAdvice = "⏳ Spinners cautious. Hold if storage allows for ₹7,500 target.",
                isSellFavorable = false
            ),

            // --- HARYANA ---
            MandiRecord(
                commodity = "Paddy (Dhaan)",
                varietyDetail = "Basmati 1509 / 1718",
                variety = "Basmati Extra Long",
                market = "Karnal Grain Market",
                district = "Karnal",
                state = "Haryana",
                minPrice = 3600,
                maxPrice = 4150,
                modalPrice = 3950,
                priceChange = 70,
                arrivalDate = "Today (08:30 AM)",
                aiAdvice = "📈 Exporters actively lifting stock. Favorable rate.",
                isSellFavorable = true
            ),
            MandiRecord(
                commodity = "Mustard (Sarson)",
                varietyDetail = "Pusa Bold 42% Oil",
                variety = "Mustard Seed",
                market = "Rewari APMC",
                district = "Rewari",
                state = "Haryana",
                minPrice = 5250,
                maxPrice = 5780,
                modalPrice = 5560,
                priceChange = 85,
                arrivalDate = "Today (11:30 AM)",
                aiAdvice = "📊 Oil expellers buying above MSP. Good profit margin.",
                isSellFavorable = true
            ),

            // --- RAJASTHAN ---
            MandiRecord(
                commodity = "Mustard (Sarson)",
                varietyDetail = "Mustard Black Seed",
                variety = "Mustard 42% Oil",
                market = "Kota APMC Mandi",
                district = "Kota",
                state = "Rajasthan",
                minPrice = 5200,
                maxPrice = 5750,
                modalPrice = 5540,
                priceChange = 75,
                arrivalDate = "Today (11:00 AM)",
                aiAdvice = "📊 Oil mill procurement active above MSP. Favorable to sell.",
                isSellFavorable = true
            ),
            MandiRecord(
                commodity = "Cumin (Jeera)",
                varietyDetail = "Unjha / Nagaur Machine Clean",
                variety = "Cumin Seed Extra Bold",
                market = "Nagaur Mandi",
                district = "Nagaur",
                state = "Rajasthan",
                minPrice = 24500,
                maxPrice = 29800,
                modalPrice = 27400,
                priceChange = 650,
                arrivalDate = "Today (09:15 AM)",
                aiAdvice = "🚀 Strong domestic and international spice demand. High realization price!",
                isSellFavorable = true
            ),
            MandiRecord(
                commodity = "Guar Seed (Gwar)",
                varietyDetail = "Guar Gum Grade",
                variety = "Commercial Seed",
                market = "Bikaner APMC",
                district = "Bikaner",
                state = "Rajasthan",
                minPrice = 5100,
                maxPrice = 5650,
                modalPrice = 5420,
                priceChange = 50,
                arrivalDate = "Today (10:30 AM)",
                aiAdvice = "📈 Steady movement into industrial gum mills.",
                isSellFavorable = true
            ),
            MandiRecord(
                commodity = "Pearl Millet (Bajra)",
                varietyDetail = "Desi Hybrid Bajra",
                variety = "Feed & Food Grade",
                market = "Jaipur Mandi",
                district = "Jaipur",
                state = "Rajasthan",
                minPrice = 2150,
                maxPrice = 2450,
                modalPrice = 2320,
                priceChange = 35,
                arrivalDate = "Today (10:00 AM)",
                aiAdvice = "🌾 Nutri-cereal demand robust in retail packaging segment.",
                isSellFavorable = true
            ),

            // --- MAHARASHTRA ---
            MandiRecord(
                commodity = "Onion (Pyaz)",
                varietyDetail = "Nashik Red Regular",
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
                commodity = "Soybean (Pili)",
                varietyDetail = "JS-335 / Yellow Regular",
                variety = "Soybean Oil Grade",
                market = "Latur APMC",
                district = "Latur",
                state = "Maharashtra",
                minPrice = 4400,
                maxPrice = 4880,
                modalPrice = 4670,
                priceChange = 60,
                arrivalDate = "Today (11:00 AM)",
                aiAdvice = "📊 Solvent extraction plants buying steadily.",
                isSellFavorable = true
            ),
            MandiRecord(
                commodity = "Cotton (Kapas)",
                varietyDetail = "Medium Staple (Shankar-6)",
                variety = "Medium Staple (Shankar-6)",
                market = "Akola APMC",
                district = "Akola",
                state = "Maharashtra",
                minPrice = 6950,
                maxPrice = 7600,
                modalPrice = 7350,
                priceChange = -90,
                arrivalDate = "Today (10:15 AM)",
                aiAdvice = "⏳ Ginners bidding slightly lower today. Hold stock for better quotes.",
                isSellFavorable = false
            ),
            MandiRecord(
                commodity = "Tomato (Tamatar)",
                varietyDetail = "Abhinav / Desi Hybrid",
                variety = "Red Table Grade",
                market = "Pimpalgaon APMC",
                district = "Nashik",
                state = "Maharashtra",
                minPrice = 1600,
                maxPrice = 2300,
                modalPrice = 2050,
                priceChange = 180,
                arrivalDate = "Today (06:45 AM)",
                aiAdvice = "🚀 Higher dispatch to Mumbai and Delhi markets today.",
                isSellFavorable = true
            ),
            MandiRecord(
                commodity = "Pigeon Pea (Tur / Arhar)",
                varietyDetail = "White / Red Marathwada",
                variety = "Whole Grain Tur",
                market = "Amravati APMC",
                district = "Amravati",
                state = "Maharashtra",
                minPrice = 9600,
                maxPrice = 11200,
                modalPrice = 10450,
                priceChange = 220,
                arrivalDate = "Today (10:45 AM)",
                aiAdvice = "🔥 Record pulses demand. Excellent realization rate for farmers.",
                isSellFavorable = true
            ),

            // --- GUJARAT ---
            MandiRecord(
                commodity = "Cotton (Kapas)",
                varietyDetail = "Medium Staple (Shankar-6)",
                variety = "Medium Staple (Shankar-6)",
                market = "Rajkot APMC",
                district = "Rajkot",
                state = "Gujarat",
                minPrice = 7050,
                maxPrice = 7750,
                modalPrice = 7460,
                priceChange = -40,
                arrivalDate = "Today (10:15 AM)",
                aiAdvice = "⏳ Global cotton index is steady. Good window for high-grade clean cotton.",
                isSellFavorable = false
            ),
            MandiRecord(
                commodity = "Groundnut (Moongfali)",
                varietyDetail = "GG-20 / Bold Kernel",
                variety = "Oil & Hand Picked",
                market = "Junagadh Mandi",
                district = "Junagadh",
                state = "Gujarat",
                minPrice = 5800,
                maxPrice = 6650,
                modalPrice = 6320,
                priceChange = 110,
                arrivalDate = "Today (09:15 AM)",
                aiAdvice = "📈 Strong oil mill demand and HPS kernel export queries. Sell today.",
                isSellFavorable = true
            ),
            MandiRecord(
                commodity = "Cumin (Jeera)",
                varietyDetail = "Machine Clean Bold",
                variety = "Unjha Premium",
                market = "Unjha APMC Mandi",
                district = "Mehsana",
                state = "Gujarat",
                minPrice = 25000,
                maxPrice = 31200,
                modalPrice = 28600,
                priceChange = 800,
                arrivalDate = "Today (08:00 AM)",
                aiAdvice = "🔥 Unjha benchmark trading at high volumes with overseas buyers active.",
                isSellFavorable = true
            ),

            // --- WEST BENGAL ---
            MandiRecord(
                commodity = "Potato (Aalu)",
                varietyDetail = "Jyoti Fresh Table",
                variety = "Jyoti Fresh",
                market = "Burdwan APMC Mandi",
                district = "Purba Bardhaman",
                state = "West Bengal",
                minPrice = 1180,
                maxPrice = 1440,
                modalPrice = 1320,
                priceChange = -30,
                arrivalDate = "Today (08:15 AM)",
                aiAdvice = "⏳ Heavy cold storage releases. Hold for 7-10 days if storage is viable.",
                isSellFavorable = false
            ),
            MandiRecord(
                commodity = "Paddy (Dhaan)",
                varietyDetail = "Swarna / Minikit Paddy",
                variety = "Common Paddy",
                market = "Medinipur Grain Mandi",
                district = "Paschim Medinipur",
                state = "West Bengal",
                minPrice = 2250,
                maxPrice = 2520,
                modalPrice = 2390,
                priceChange = 50,
                arrivalDate = "Today (09:30 AM)",
                aiAdvice = "🌾 Local rice mill procurement brisk with standard payment terms.",
                isSellFavorable = true
            ),
            MandiRecord(
                commodity = "Jute (Pat)",
                varietyDetail = "TD-5 / Tossa Jute",
                variety = "Raw Jute Grade-4",
                market = "Hooghly APMC",
                district = "Hooghly",
                state = "West Bengal",
                minPrice = 5400,
                maxPrice = 6100,
                modalPrice = 5820,
                priceChange = 120,
                arrivalDate = "Today (10:00 AM)",
                aiAdvice = "🚀 Jute Commissioner and mills lifting raw bales at healthy premiums.",
                isSellFavorable = true
            ),

            // --- BIHAR ---
            MandiRecord(
                commodity = "Maize (Makka)",
                varietyDetail = "Purnia Yellow Starch Grade",
                variety = "Yellow Maize",
                market = "Gulabbagh APMC Mandi",
                district = "Purnia",
                state = "Bihar",
                minPrice = 2180,
                maxPrice = 2480,
                modalPrice = 2360,
                priceChange = 70,
                arrivalDate = "Today (08:45 AM)",
                aiAdvice = "🔥 Largest maize trading hub seeing massive rail dispatches to poultry hubs.",
                isSellFavorable = true
            ),
            MandiRecord(
                commodity = "Paddy (Dhaan)",
                varietyDetail = "Katarni / Mansoori",
                variety = "Mansoori Grade",
                market = "Patna APMC",
                district = "Patna",
                state = "Bihar",
                minPrice = 2150,
                maxPrice = 2380,
                modalPrice = 2270,
                priceChange = 40,
                arrivalDate = "Today (09:15 AM)",
                aiAdvice = "📈 Steady mandi sales to local wholesale distributers.",
                isSellFavorable = true
            ),
            MandiRecord(
                commodity = "Wheat (Gehu)",
                varietyDetail = "Lok-1 / Desi",
                variety = "Milling Quality",
                market = "Muzaffarpur Mandi",
                district = "Muzaffarpur",
                state = "Bihar",
                minPrice = 2320,
                maxPrice = 2520,
                modalPrice = 2430,
                priceChange = 25,
                arrivalDate = "Today (10:00 AM)",
                aiAdvice = "🌾 Local consumption demand steady.",
                isSellFavorable = true
            ),

            // --- KARNATAKA ---
            MandiRecord(
                commodity = "Tomato (Tamatar)",
                varietyDetail = "Hybrid Table Grade",
                variety = "Fresh Tomato",
                market = "Kolar APMC Mandi",
                district = "Kolar",
                state = "Karnataka",
                minPrice = 1750,
                maxPrice = 2450,
                modalPrice = 2150,
                priceChange = 200,
                arrivalDate = "Today (06:00 AM)",
                aiAdvice = "🚀 South India's largest tomato hub with massive buyer participation today.",
                isSellFavorable = true
            ),
            MandiRecord(
                commodity = "Finger Millet (Ragi)",
                varietyDetail = "Brown Ragi / MR-1",
                variety = "Cleaned Grain",
                market = "Tumkur APMC",
                district = "Tumakuru",
                state = "Karnataka",
                minPrice = 3600,
                maxPrice = 4200,
                modalPrice = 3920,
                priceChange = 80,
                arrivalDate = "Today (10:30 AM)",
                aiAdvice = "🌾 Millet consumption programs driving firm prices.",
                isSellFavorable = true
            ),
            MandiRecord(
                commodity = "Pigeon Pea (Tur / Arhar)",
                varietyDetail = "Gulbarga Red Tur",
                variety = "GI Tagged Quality",
                market = "Kalaburagi APMC",
                district = "Kalaburagi",
                state = "Karnataka",
                minPrice = 9800,
                maxPrice = 11400,
                modalPrice = 10600,
                priceChange = 260,
                arrivalDate = "Today (09:30 AM)",
                aiAdvice = "🔥 Top premium for Gulbarga GI certified Tur. Sell directly to processing mills.",
                isSellFavorable = true
            ),

            // --- ANDHRA PRADESH / TELANGANA ---
            MandiRecord(
                commodity = "Red Chilli (Mirchi)",
                varietyDetail = "Teja / 334 Hot Red",
                variety = "Dried Stemless",
                market = "Guntur Mirchi Yard",
                district = "Guntur",
                state = "Andhra Pradesh",
                minPrice = 16500,
                maxPrice = 21500,
                modalPrice = 19200,
                priceChange = 550,
                arrivalDate = "Today (07:30 AM)",
                aiAdvice = "🔥 Asia's largest chilli market buzzing with spice extractors and exporter bidding.",
                isSellFavorable = true
            ),
            MandiRecord(
                commodity = "Turmeric (Haldi)",
                varietyDetail = "Nizamabad Finger Yellow",
                variety = "Finger Grade-A",
                market = "Nizamabad APMC",
                district = "Nizamabad",
                state = "Telangana",
                minPrice = 12800,
                maxPrice = 16200,
                modalPrice = 14500,
                priceChange = 420,
                arrivalDate = "Today (08:30 AM)",
                aiAdvice = "🚀 Curcumin extractors aggressively buying finger lots. Exceptional returns.",
                isSellFavorable = true
            ),
            MandiRecord(
                commodity = "Cotton (Kapas)",
                varietyDetail = "Long Staple (Bunny / Brahma)",
                variety = "Long Staple",
                market = "Warangal Enamamula Mandi",
                district = "Warangal",
                state = "Telangana",
                minPrice = 7100,
                maxPrice = 7800,
                modalPrice = 7520,
                priceChange = -30,
                arrivalDate = "Today (10:00 AM)",
                aiAdvice = "📊 Textile mill buyers active at current modal price level.",
                isSellFavorable = true
            ),

            // --- TAMIL NADU & ODISHA ---
            MandiRecord(
                commodity = "Turmeric (Haldi)",
                varietyDetail = "Erode Finger GI",
                variety = "Curcumin Rich",
                market = "Erode Regulated Market",
                district = "Erode",
                state = "Tamil Nadu",
                minPrice = 13200,
                maxPrice = 16800,
                modalPrice = 15100,
                priceChange = 460,
                arrivalDate = "Today (09:00 AM)",
                aiAdvice = "🔥 High demand from pharmaceutical and food export houses.",
                isSellFavorable = true
            ),
            MandiRecord(
                commodity = "Paddy (Dhaan)",
                varietyDetail = "Swarna / CR-1009",
                variety = "Common Paddy",
                market = "Sambalpur APMC",
                district = "Sambalpur",
                state = "Odisha",
                minPrice = 2183,
                maxPrice = 2400,
                modalPrice = 2290,
                priceChange = 30,
                arrivalDate = "Today (09:45 AM)",
                aiAdvice = "🌾 Government MSP mill procurement smoothly underway.",
                isSellFavorable = true
            )
        )
    }

    val states = listOf(
        "All India",
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
        "Odisha"
    )

    val cropCategories = listOf(
        "All Crops",
        "Wheat & Cereals",
        "Paddy & Rice",
        "Mustard & Oilseeds",
        "Pulses & Chana",
        "Vegetables & Spices",
        "Cotton & Fibres"
    )

    val filteredRecords = allMandiRecords.filter { record ->
        val matchesState = selectedState == "All India" || record.state.equals(selectedState, ignoreCase = true)
        
        val matchesCategory = when (selectedCropCategory) {
            "Wheat & Cereals" -> record.commodity.contains("Wheat", ignoreCase = true) || record.commodity.contains("Maize", ignoreCase = true) || record.commodity.contains("Millet", ignoreCase = true)
            "Paddy & Rice" -> record.commodity.contains("Paddy", ignoreCase = true) || record.commodity.contains("Rice", ignoreCase = true)
            "Mustard & Oilseeds" -> record.commodity.contains("Mustard", ignoreCase = true) || record.commodity.contains("Soybean", ignoreCase = true) || record.commodity.contains("Groundnut", ignoreCase = true)
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
                record.district.contains(searchQuery, ignoreCase = true) ||
                record.state.contains(searchQuery, ignoreCase = true)

        matchesState && matchesCategory && matchesSearch
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
                        text = "Daily Market Rates & AI Selling Advisory",
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
                    placeholder = { Text("Search crop, market or state (Wheat, Indore...)") },
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
                Column {
                    Text(
                        text = "State / Region",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
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

            // 3. CROP CATEGORY CHIPS
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

            // 4. RESULTS COUNT BAR
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Showing ${filteredRecords.size} Mandi Rates ($selectedState)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary,
                            fontSize = 12.sp
                        )
                    )
                    Text(
                        text = "🟢 Live APMC E-NAM",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // 5. MANDI RATE CARDS
            if (filteredRecords.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No mandi records found for '$searchQuery' in $selectedState",
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
                            text = "Modal Price",
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
                            text = "Min - Max Price",
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

