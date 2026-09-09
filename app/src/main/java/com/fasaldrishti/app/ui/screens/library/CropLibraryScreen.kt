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
    onNavigateBack: () -> Unit,
    onNavigateToChat: ((String) -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf("All") }
    var diseases by remember { mutableStateOf<List<DiseaseInfo>>(emptyList()) }
    var selectedDiseaseForSheet by remember { mutableStateOf<DiseaseInfo?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        val res = diseaseRepository.getAllDiseases()
        diseases = res.getOrDefault(emptyList())
    }

    val indianCrops = listOf(
        IndianCropCategory("All", "All Crops", "🌿"),
        IndianCropCategory("Rice", "Rice", "🌾"),
        IndianCropCategory("Wheat", "Wheat", "🌾"),
        IndianCropCategory("Cotton", "Cotton", "🌿"),
        IndianCropCategory("Sugarcane", "Sugarcane", "🎋"),
        IndianCropCategory("Chilli", "Chilli", "🌶️"),
        IndianCropCategory("Mustard", "Mustard", "🟡"),
        IndianCropCategory("Mango", "Mango", "🥭"),
        IndianCropCategory("Tomato", "Tomato", "🍅"),
        IndianCropCategory("Potato", "Potato", "🥔"),
        IndianCropCategory("Corn", "Corn", "🌽"),
        IndianCropCategory("Soybean", "Soybean", "🫘"),
        IndianCropCategory("Citrus", "Citrus", "🍊"),
        IndianCropCategory("Grape", "Grape", "🍇"),
        IndianCropCategory("Apple", "Apple", "🍎")
    )

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
                    Column {
                        Text(
                            text = "Crop Pathology Library",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 19.sp
                            )
                        )
                        Text(
                            text = "Disease Knowledge Base & Agronomy Treatments",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                fontSize = 11.5.sp
                            )
                        )
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
                                "Search crop, disease, or chemical treatment...",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            ) 
                        },
                        leadingIcon = { 
                            Icon(
                                Icons.Default.Search, 
                                contentDescription = null, 
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            ) 
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search", modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(indianCrops) { cat ->
                        val isSelected = selectedCategoryId == cat.id
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.clickable { selectedCategoryId = cat.id }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${cat.icon} ${cat.label}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 12.sp
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
                                text = disease.cropName.uppercase(),
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
                                text = disease.diseaseName,
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
                                    text = if (disease.isHealthy) "Healthy Stand" else "Dosage: ${disease.treatment.take(45)}...",
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
                                text = d.cropName.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                            )
                            Text(
                                text = d.diseaseName,
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
                        title = "Symptoms & Identification",
                        icon = Icons.Default.Coronavirus,
                        content = d.symptoms,
                        initiallyExpanded = true,
                        accentColor = CrimsonCoral
                    )

                    AccordionCard(
                        title = "Chemical Treatment & Dosage",
                        icon = Icons.Default.Science,
                        content = d.treatment,
                        initiallyExpanded = true,
                        accentColor = SolarGold
                    )

                    AccordionCard(
                        title = "Organic Control & Prevention",
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
                                text = "Consult AI Agri Doctor",
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
