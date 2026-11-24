package com.fasaldrishti.app.ui.screens.workflows

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fasaldrishti.app.ui.theme.EmeraldDark
import com.fasaldrishti.app.ui.theme.EmeraldPrimary

data class WorkflowNode(
    val stepNumber: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val techStack: String,
    val inputSpec: String,
    val outputSpec: String,
    val latencySpec: String,
    val icon: ImageVector,
    val isDecisionNode: Boolean = false,
    val branchA: String? = null,
    val branchB: String? = null
)

data class AppWorkflow(
    val id: String,
    val title: String,
    val shortName: String,
    val category: String,
    val icon: ImageVector,
    val summary: String,
    val totalLatency: String,
    val capabilityBadge: String,
    val nodes: List<WorkflowNode>
)

@Composable
fun WorkflowsScreen(
    onNavigateBack: () -> Unit
) {
    val workflows = remember {
        listOf(
            AppWorkflow(
                id = "diagnosis",
                title = "Crop Disease Diagnosis Pipeline",
                shortName = "Scan to Treatment",
                category = "Core AI Vision Engine",
                icon = Icons.Default.CameraAlt,
                summary = "End-to-end diagnosis flow: from camera viewfinder capture to on-device TFLite neural inference, Room DB dossier matching, and cloud multimodal 2nd opinion.",
                totalLatency = "~85ms (Offline) / ~1.4s (Cloud)",
                capabilityBadge = "⚡ Hybrid Zero-Latency",
                nodes = listOf(
                    WorkflowNode(
                        stepNumber = "01",
                        title = "Specimen Acquisition & Framing",
                        subtitle = "CameraX High-Speed Sensor Capture",
                        description = "Captures leaf image via CameraX or selects from device gallery. Focuses crop area using aspect-ratio bounding box to isolate infected foliage from background clutter.",
                        techStack = "Android CameraX API • Intent Picker",
                        inputSpec = "Raw Camera Sensor Stream (1080p/4K)",
                        outputSpec = "Specimen Bitmap (RGB_8888)",
                        latencySpec = "< 20ms",
                        icon = Icons.Default.CameraAlt
                    ),
                    WorkflowNode(
                        stepNumber = "02",
                        title = "On-Device Preprocessing & Tensor Normalization",
                        subtitle = "Matrix Scaling & Channel Normalization",
                        description = "Center-crops the region of interest, downsamples to exactly 224x224 pixels with bilinear sampling, and converts 8-bit integer RGB channels to Float32 values normalized between [0.0, 1.0].",
                        techStack = "Android Graphics Matrix • ByteBuffer",
                        inputSpec = "High-Res Bitmap",
                        outputSpec = "1 x 224 x 224 x 3 Float32 Tensor",
                        latencySpec = "~8ms",
                        icon = Icons.Default.Tune
                    ),
                    WorkflowNode(
                        stepNumber = "03",
                        title = "Dual-Engine TFLite Neural Inference",
                        subtitle = "Specialist & Base Model Routing",
                        description = "Smart Model Router checks target crop. Routes Rice to specialized Rice model (rice_leaf_disease.tflite), Wheat to Wheat model (wheat_leaf_disease.tflite), or 46-class Multi-Crop MobileNetV3 engine. Accelerated with Android NNAPI GPU/NPU delegates.",
                        techStack = "TensorFlow Lite C++ Runtime • NNAPI Delegate",
                        inputSpec = "224x224 Float32 Tensor Buffer",
                        outputSpec = "62-Class Softmax Probability Array",
                        latencySpec = "~45ms (On-Device)",
                        icon = Icons.Default.Memory
                    ),
                    WorkflowNode(
                        stepNumber = "04",
                        title = "Offline Practical Dossier Retrieval",
                        subtitle = "Room SQLite 4-Point Prescription Guide",
                        description = "Matches predicted disease class with local database. Automatically retrieves active chemical salt, 15L knapsack tank dose, per-acre volume, spray timing, resistance rotation, and organic bio-remedies.",
                        techStack = "Room Database • Kotlin Coroutines",
                        inputSpec = "Disease Class Key (e.g. Potato___Early_blight)",
                        outputSpec = "Full DiseaseInfo Practical Dossier",
                        latencySpec = "< 5ms",
                        icon = Icons.Default.Storage
                    ),
                    WorkflowNode(
                        stepNumber = "05",
                        title = "Cloud Multimodal Verification Decision",
                        subtitle = "Conditional 2nd-Opinion Cloud Routing",
                        description = "If internet is available and online verification is toggled ON, the uncompressed specimen is dispatched to Google Gemini 2.5 Flash / NVIDIA NIM for secondary pathology cross-verification.",
                        techStack = "NetworkMonitor • OkHttp • Gemini REST API",
                        inputSpec = "Device Network Capability State",
                        outputSpec = "Verification Status Badge + Enriched Dosage",
                        latencySpec = "Instant (Offline) / ~1.2s (Online)",
                        icon = Icons.AutoMirrored.Filled.AltRoute,
                        isDecisionNode = true,
                        branchA = "Offline: Bypass Cloud -> Instant On-Device Result (<5ms)",
                        branchB = "Online: Gemini / NVIDIA 2nd Multimodal Verification (~1.2s)"
                    ),
                    WorkflowNode(
                        stepNumber = "06",
                        title = "Diagnosis Dossier & Action Hub Presentation",
                        subtitle = "Interactive Medical Result Screen",
                        description = "Renders verified disease name, severity gauge, highlighted 15-liter knapsack pump dosage card, organic sanitation accordion, AI Salah chatbot link, and 1-click PDF download button.",
                        techStack = "Jetpack Compose • Material 3",
                        inputSpec = "Complete ScanRecord Entity",
                        outputSpec = "Interactive Result Screen UI",
                        latencySpec = "60 FPS Native UI",
                        icon = Icons.Default.Verified
                    )
                )
            ),
            AppWorkflow(
                id = "hybrid",
                title = "Hybrid Offline/Online Fallback Engine",
                shortName = "Offline / Cloud Hybrid",
                category = "Resilience & Fallback Architecture",
                icon = Icons.Default.SyncAlt,
                summary = "Guarantees zero-failure operation in rural farmlands. If network drops or cloud APIs hit quota limits, the app seamlessly falls back to 100% on-device neural inference without user disruption.",
                totalLatency = "0ms Downtime",
                capabilityBadge = "🛡️ 100% Offline Resilient",
                nodes = listOf(
                    WorkflowNode(
                        stepNumber = "01",
                        title = "Connectivity State Monitoring",
                        subtitle = "Real-Time Android Network Callbacks",
                        description = "Continuously tracks network availability, cellular signal stability, and bandwidth via Android ConnectivityManager. Emits reactive StateFlow updates.",
                        techStack = "ConnectivityManager • StateFlow",
                        inputSpec = "System Network State Change Events",
                        outputSpec = "NetworkStatus: Available | Lost | Metered",
                        latencySpec = "< 1ms",
                        icon = Icons.Default.Wifi
                    ),
                    WorkflowNode(
                        stepNumber = "02",
                        title = "Adaptive Path Router Decision",
                        subtitle = "Conditional Traffic Branching",
                        description = "Dynamically evaluates whether to dispatch cloud requests or execute 100% locally on-device based on real-time internet connectivity.",
                        techStack = "NetworkMonitor • Dispatchers.Default",
                        inputSpec = "NetworkStatus + User Preferences",
                        outputSpec = "Selected Execution Pipeline",
                        latencySpec = "< 1ms",
                        icon = Icons.AutoMirrored.Filled.AltRoute,
                        isDecisionNode = true,
                        branchA = "No Internet: Route to Offline TFLite Engine (100% Local)",
                        branchB = "Active Internet: Dispatch to Cloud API Pool"
                    ),
                    WorkflowNode(
                        stepNumber = "03",
                        title = "Multi-Key API Pool & Load Balancing",
                        subtitle = "Dynamic Key Rotation & Failover",
                        description = "Maintains a resilient pool of Gemini 2.5 Flash and NVIDIA Vision API keys. Distributes requests and automatically switches to backup keys if rate limits (HTTP 429) occur.",
                        techStack = "AiConfigManager • OkHttp Interceptor",
                        inputSpec = "Base64 Image Payload + Key Index",
                        outputSpec = "HTTP 200 JSON Payload or Key Failover",
                        latencySpec = "~950ms",
                        icon = Icons.Default.Key
                    ),
                    WorkflowNode(
                        stepNumber = "04",
                        title = "Graceful Degradation & Local Fallback",
                        subtitle = "Zero-Crash Exception Guard",
                        description = "If the cloud times out (>8s) or network drops during upload, the catch block immediately saves the offline TFLite diagnosis and marks it with '⚡ Rapid On-Device Screening'.",
                        techStack = "Kotlin Coroutines runCatching",
                        inputSpec = "SocketTimeoutException / Network Drop",
                        outputSpec = "Guaranteed Safe Offline Result",
                        latencySpec = "< 5ms",
                        icon = Icons.Default.Security
                    )
                )
            ),
            AppWorkflow(
                id = "ai_salah",
                title = "AI Salah Agronomist Consultation",
                shortName = "AI Salah Chatbot",
                category = "Conversational Agronomy AI",
                icon = Icons.Default.Psychology,
                summary = "AI Chatbot assistant that helps farmers understand exact tank dosages, crop mixing calculations, and seasonal spray advice in Hindi, Hinglish, and regional languages.",
                totalLatency = "Streaming Responses (~900ms)",
                capabilityBadge = "🎙️ Voice & Multilingual",
                nodes = listOf(
                    WorkflowNode(
                        stepNumber = "01",
                        title = "Contextual Diagnosis Payload Injection",
                        subtitle = "Scan Metadata Memory Pre-Loading",
                        description = "When farmer taps 'Consult AI Salah' from a diagnosis result, current scan details (crop, disease, severity, and knapsack dosage) are automatically injected into session memory.",
                        techStack = "ScanRecord • SavedStateHandle",
                        inputSpec = "Active Scan ID & Disease Meta",
                        outputSpec = "Context Memory Key-Value Pairs",
                        latencySpec = "< 2ms",
                        icon = Icons.Default.DataArray
                    ),
                    WorkflowNode(
                        stepNumber = "02",
                        title = "Farmer Vernacular & Voice Input Processing",
                        subtitle = "Speech-To-Text & Language Handling",
                        description = "Accepts queries via text or microphone voice input in Hindi, Hinglish, Marathi, Bengali, Telugu, Tamil, and English. Converts voice to text via Android SpeechRecognizer.",
                        techStack = "Android SpeechRecognizer • TextWatcher",
                        inputSpec = "Microphone Audio Stream / User Text",
                        outputSpec = "Sanitized Query String",
                        latencySpec = "~250ms",
                        icon = Icons.Default.Mic
                    ),
                    WorkflowNode(
                        stepNumber = "03",
                        title = "ICAR Agronomic System Prompt Engineering",
                        subtitle = "Agricultural Domain Guardrails",
                        description = "Injects strict Indian agronomy rules: standard 15-liter knapsack pump formulas, non-toxic organic alternatives, Pre-Harvest Interval (PHI) safety, and Kisan Helpline (1800-180-1551) advisory.",
                        techStack = "Context Builder • Gemini Prompt Engine",
                        inputSpec = "Farmer Prompt + Diagnosis Context",
                        outputSpec = "Agronomist Guardrail Prompt",
                        latencySpec = "< 5ms",
                        icon = Icons.Default.AutoAwesome
                    ),
                    WorkflowNode(
                        stepNumber = "04",
                        title = "Gemini 2.5 Flash Response Generation",
                        subtitle = "Streaming Markdown & Actionable Prescriptions",
                        description = "Generates actionable, easy-to-follow spray advice, chemical salt safety, and field hygiene instructions. Rendered in clean markdown with bold dosage values.",
                        techStack = "Google Gemini 2.5 Flash • OkHttp SSE",
                        inputSpec = "Structured LLM Prompt Payload",
                        outputSpec = "Farmer-Friendly Markdown Stream",
                        latencySpec = "~900ms",
                        icon = Icons.Default.ChatBubble
                    )
                )
            ),
            AppWorkflow(
                id = "pdf_engine",
                title = "PDF Medical Prescription & Dossier Engine",
                shortName = "PDF Prescription Engine",
                category = "Document Rendering & Sharing",
                icon = Icons.Default.Description,
                summary = "Generates professional medical-prescription style A4 reports for Indian farmers to present directly to agricultural chemical retailers (Krishi Kendra / Agri Shops).",
                totalLatency = "< 120ms Generation",
                capabilityBadge = "📄 100% On-Device Offline PDF",
                nodes = listOf(
                    WorkflowNode(
                        stepNumber = "01",
                        title = "Dossier Aggregation & Extraction",
                        subtitle = "Chemical & Organic Separation",
                        description = "Extracts crop name, disease pathology, confidence score, and specimen photo path. Cleanly splits scan.treatment into pure chemical spray items and organic bio-remedies.",
                        techStack = "ScanRecord • Kotlin String Parsers",
                        inputSpec = "ScanRecord Database Entity",
                        outputSpec = "Structured Prescription Data Map",
                        latencySpec = "< 2ms",
                        icon = Icons.Default.DataObject
                    ),
                    WorkflowNode(
                        stepNumber = "02",
                        title = "A4 Page Setup & Geometry Engine",
                        subtitle = "ISO 216 Standard Canvas (595 x 842 points)",
                        description = "Allocates native Android PdfDocument page. Computes responsive margins (Left: 28pt, Right: 567pt) with zero-overlap guarantees and vertical bounding budgets.",
                        techStack = "android.graphics.pdf.PdfDocument",
                        inputSpec = "595 x 842 Points A4 Geometry Target",
                        outputSpec = "Native Canvas Reference",
                        latencySpec = "< 4ms",
                        icon = Icons.Default.AspectRatio
                    ),
                    WorkflowNode(
                        stepNumber = "03",
                        title = "Specimen Image Downsampling & Embedding",
                        subtitle = "Memory-Safe Bitmap Sampling",
                        description = "Safely loads high-resolution leaf photo with BitmapFactory inSampleSize downsampling. Renders specimen into a framed thumbnail (84x84 pt) on the right of the diagnosis card.",
                        techStack = "BitmapFactory (inSampleSize) • Canvas",
                        inputSpec = "High-Res JPEG / PNG File Path",
                        outputSpec = "84x84 Scaled Embedded Bitmap",
                        latencySpec = "~15ms",
                        icon = Icons.Default.Image
                    ),
                    WorkflowNode(
                        stepNumber = "04",
                        title = "Collision-Free Typography & Dosage Engine",
                        subtitle = "Hanging Indent & Water Droplet Badges",
                        description = "Draws medical header banner, wrapped disease name (bounded to 388pt to prevent overlap), chemical RX box with 💧 15L Tank Dose badge, dedicated organic remedy box, and KVK helpline notice.",
                        techStack = "Android Paint • Typeface • Custom Wrap Engine",
                        inputSpec = "Parsed Prescription Bullets",
                        outputSpec = "Collision-Free Vector Layout Canvas",
                        latencySpec = "~25ms",
                        icon = Icons.AutoMirrored.Filled.FormatAlignLeft
                    ),
                    WorkflowNode(
                        stepNumber = "05",
                        title = "FileProvider & 1-Click WhatsApp Sharing",
                        subtitle = "Secure Content URI & Intent Dispatch",
                        description = "Writes generated PDF to app cache directory. Creates a secure content:// URI via FileProvider and triggers Android ACTION_SEND sheet to share directly via WhatsApp, Telegram, or wireless printer.",
                        techStack = "FileProvider • Intent.ACTION_SEND",
                        inputSpec = "Rendered PDF Output Stream",
                        outputSpec = "Secure content:// URI for Sharing",
                        latencySpec = "~12ms",
                        icon = Icons.Default.Share
                    )
                )
            ),
            AppWorkflow(
                id = "weather_risk",
                title = "Agro-Weather & Outbreak Risk Advisory",
                shortName = "Weather & Spray Risk",
                category = "Predictive Crop Intelligence",
                icon = Icons.Default.WbSunny,
                summary = "Correlates live temperature, humidity, and rainfall forecasts to calculate fungal spore germination risk and recommend ideal spray windows.",
                totalLatency = "~450ms Refresh",
                capabilityBadge = "🌤️ Hourly Spray Forecast",
                nodes = listOf(
                    WorkflowNode(
                        stepNumber = "01",
                        title = "GPS Geocoding & Field Coordinates",
                        subtitle = "Low-Power Location Acquisition",
                        description = "Retrieves farm coordinates using Google Fused Location Provider with battery-optimized coarse/fine location caching.",
                        techStack = "FusedLocationProviderClient • Android GPS",
                        inputSpec = "Device Hardware Location Sensor",
                        outputSpec = "Latitude & Longitude Coordinates",
                        latencySpec = "~150ms",
                        icon = Icons.Default.MyLocation
                    ),
                    WorkflowNode(
                        stepNumber = "02",
                        title = "Agro-Meteorological Forecast Query",
                        subtitle = "7-Day Hourly Climate Timeseries",
                        description = "Queries Open-Meteo & IMD agro-climate endpoints for temperature, relative humidity, wind speed, and precipitation probability.",
                        techStack = "WeatherManager • Retrofit / OkHttp",
                        inputSpec = "GPS Coordinates (Lat/Long)",
                        outputSpec = "Hourly Weather Timeseries JSON",
                        latencySpec = "~350ms",
                        icon = Icons.Default.Cloud
                    ),
                    WorkflowNode(
                        stepNumber = "03",
                        title = "Pathogen Outbreak Index Computation",
                        subtitle = "Blight & Mildew Spore Germination Logic",
                        description = "Correlates high relative humidity (>85%) and warm temperatures (18-26°C) to calculate Late Blight, Early Blight, and fungal spore infection risk levels.",
                        techStack = "Agronomic Risk Matrix Algorithm",
                        inputSpec = "Temperature + Humidity + Rain Probability",
                        outputSpec = "Pathogen Risk Level: LOW | MODERATE | SEVERE",
                        latencySpec = "< 2ms",
                        icon = Icons.Default.Analytics
                    ),
                    WorkflowNode(
                        stepNumber = "04",
                        title = "Optimal Spray Window Recommendation",
                        subtitle = "Wind & Rain Drift Prevention",
                        description = "Identifies calm morning spray windows (6:30-9:30 AM with wind <10 km/h) to prevent chemical drift or rain wash-off, maximizing pesticide efficiency.",
                        techStack = "Advisory Rule Engine",
                        inputSpec = "Wind Velocity + Precipitation Forecast",
                        outputSpec = "Actionable Farmer Spray Advice",
                        latencySpec = "< 1ms",
                        icon = Icons.Default.CheckCircle
                    )
                )
            ),
            AppWorkflow(
                id = "mandi_rates",
                title = "Live Mandi Bhav & APMC Rate Engine",
                shortName = "Mandi Bhav & APMC",
                category = "Market Logistics & Price Transparency",
                icon = Icons.Default.Storefront,
                summary = "Syncs official daily mandi arrival rates from AGMARKNET and data.gov.in, comparing current modal prices against Government Minimum Support Price (MSP).",
                totalLatency = "~400ms Refresh",
                capabilityBadge = "🏛️ Govt AGMARKNET Sync",
                nodes = listOf(
                    WorkflowNode(
                        stepNumber = "01",
                        title = "Commodity & Market Selection",
                        subtitle = "Target APMC Market Mapping",
                        description = "Farmer selects crop commodity (e.g. Potato, Tomato, Wheat, Paddy) and chooses state or local APMC market yard.",
                        techStack = "MandiViewModel • State Machine",
                        inputSpec = "Commodity Name + State / APMC Code",
                        outputSpec = "Target Market Query Parameters",
                        latencySpec = "< 1ms",
                        icon = Icons.Default.FilterAlt
                    ),
                    WorkflowNode(
                        stepNumber = "02",
                        title = "AGMARKNET / data.gov.in Data Synchronization",
                        subtitle = "Government Open Data API Fetch",
                        description = "Queries official daily arrivals, minimum price, maximum price, and prevailing modal rates per quintal directly from government open data portals.",
                        techStack = "OkHttp • JSON Deserializer",
                        inputSpec = "Government Portal Endpoint",
                        outputSpec = "Sanitized Mandi Price Table",
                        latencySpec = "~400ms",
                        icon = Icons.Default.Sync
                    ),
                    WorkflowNode(
                        stepNumber = "03",
                        title = "MSP Variance & Market Trend Analysis",
                        subtitle = "Profitability & Rate Trend Indicators",
                        description = "Calculates price variance against central Minimum Support Price (MSP) benchmarks and displays green/red price trend indicators to help farmers decide when to sell.",
                        techStack = "Economic Analysis Engine",
                        inputSpec = "Modal Price vs Central MSP Reference",
                        outputSpec = "Price Variance (+/- ₹/Quintal) & Trends",
                        latencySpec = "< 2ms",
                        icon = Icons.AutoMirrored.Filled.TrendingUp
                    )
                )
            )
        )
    }

    var selectedWorkflowId by remember { mutableStateOf(workflows.first().id) }
    val currentWorkflow = workflows.firstOrNull { it.id == selectedWorkflowId } ?: workflows.first()

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
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
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "System Workflows",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 19.sp
                            )
                        )
                        Text(
                            text = "Interactive Architecture & Flowcharts",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. WORKFLOW TABS / SELECTOR (Horizontal Scrollable)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    workflows.forEach { wf ->
                        val isSelected = wf.id == selectedWorkflowId
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedWorkflowId = wf.id },
                            leadingIcon = {
                                Icon(
                                    imageVector = wf.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            label = {
                                Text(
                                    text = wf.shortName,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.5.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }

            // 2. ACTIVE WORKFLOW OVERVIEW CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.5.dp, EmeraldPrimary.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = EmeraldPrimary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = currentWorkflow.category.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = EmeraldPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = currentWorkflow.capabilityBadge,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = currentWorkflow.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = currentWorkflow.summary,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Latency: ${currentWorkflow.totalLatency}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccountTree,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "${currentWorkflow.nodes.size} Pipeline Stages",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 3. FLOWCHART TITLE
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schema,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Interactive Architecture Flowchart",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    )
                }
            }

            // 4. FLOWCHART NODES & CONNECTOR ARROWS
            itemsIndexed(currentWorkflow.nodes) { index, node ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    FlowchartNodeCard(
                        node = node
                    )

                    // Draw connecting arrow if not last node
                    if (index < currentWorkflow.nodes.size - 1) {
                        FlowchartConnectorArrow(
                            isDecisionNode = node.isDecisionNode,
                            branchA = node.branchA,
                            branchB = node.branchB
                        )
                    }
                }
            }

            // 5. SYSTEM ARCHITECTURE SUMMARY BANNER
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Engineering Architecture Notes",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Fasal Drishti AI utilizes a decoupled edge-first design: offline neural inference runs locally on the mobile processor without sending images over cellular networks unless the farmer explicitly requests cloud 2nd-tier multimodal verification. All technical pipelines are optimized for zero battery drain and low memory footprints.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.5.sp,
                                lineHeight = 17.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FlowchartNodeCard(
    node: WorkflowNode
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (node.isDecisionNode) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (node.isDecisionNode) 1.5.dp else 1.dp,
            color = if (node.isDecisionNode) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Step Number, Title, Expand Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Step Badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (node.isDecisionNode) Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))
                            else Brush.linearGradient(listOf(EmeraldPrimary, EmeraldDark))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = node.stepNumber,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (node.isDecisionNode) {
                            Text(
                                text = "◇ DECISION ROUTE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFD97706),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 9.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = node.title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                    }
                    Text(
                        text = node.subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.5.sp
                        )
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tech Stack & Latency Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = node.techStack,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = EmeraldPrimary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "⏱️ ${node.latencySpec}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = EmeraldPrimary
                        ),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            // Expandable Technical Deep-Dive
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    Text(
                        text = node.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Input & Output Spec Cards
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row {
                                Text(
                                    text = "📥 Input: ",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Text(
                                    text = node.inputSpec,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row {
                                Text(
                                    text = "📤 Output: ",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = EmeraldPrimary
                                    )
                                )
                                Text(
                                    text = node.outputSpec,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FlowchartConnectorArrow(
    isDecisionNode: Boolean,
    branchA: String?,
    branchB: String?
) {
    if (isDecisionNode && branchA != null && branchB != null) {
        // Dual-Branching Connector
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(14.dp)
                    .background(Color(0xFFF59E0B))
            )

            // Branching Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldPrimary.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "🌿 BRANCH A",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 9.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = branchA,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0284C7).copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.4f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "☁️ BRANCH B",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF0284C7),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 9.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = branchB,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(14.dp)
                    .background(EmeraldPrimary)
            )
            Icon(
                imageVector = Icons.Default.ArrowDownward,
                contentDescription = null,
                tint = EmeraldPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    } else {
        // Standard Straight Downward Connector
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(14.dp)
                    .background(EmeraldPrimary.copy(alpha = 0.6f))
            )
            Icon(
                imageVector = Icons.Default.ArrowDownward,
                contentDescription = null,
                tint = EmeraldPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
