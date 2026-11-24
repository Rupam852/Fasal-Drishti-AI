package com.fasaldrishti.app.ui.screens.workflows

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.drawBehind
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
                shortName = "Crop Diagnosis Engine",
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
                        icon = Icons.Default.Rule
                    ),
                    WorkflowNode(
                        stepNumber = "04",
                        title = "Multi-Turn Streaming Response & Audio Playback",
                        subtitle = "Real-Time Chunk Rendering & Text-To-Speech",
                        description = "Streams conversational agronomic advice word-by-word into chat UI. Tapping audio icon triggers Android TextToSpeech engine in the user's localized Indian dialect.",
                        techStack = "Gemini Stream API • Android TextToSpeech",
                        inputSpec = "Guardrail Prompt Stream",
                        outputSpec = "Streaming Chat Bubble + Audio Playback",
                        latencySpec = "First Token ~650ms",
                        icon = Icons.Default.VolumeUp
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
                summary = "Fetches real-time commodity prices from Indian mandis, tracks market yard trends, and calculates Minimum Support Price (MSP) profit margins.",
                totalLatency = "~400ms Query",
                capabilityBadge = "📊 Real-Time Gov Open Data",
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
            ),
            AppWorkflow(
                id = "background_alarms",
                title = "Hardware Background Reminder & Auto-Update Engine",
                shortName = "Alarms & Auto-Updater",
                category = "System Timing & WorkManager",
                icon = Icons.Default.Alarm,
                summary = "Delivers precise spray reminders even on battery-optimizing Android 14/15 phones, combined with periodic 1-hour background WorkManager release checks.",
                totalLatency = "Exact RTC_WAKEUP (0 Drift)",
                capabilityBadge = "⏰ Hardware AlarmManager",
                nodes = listOf(
                    WorkflowNode(
                        stepNumber = "01",
                        title = "Farmer Schedule Registration",
                        subtitle = "Spray & Inspection Timing Capture",
                        description = "Farmer schedules 2nd spray (e.g. 7 days post-diagnosis) or weekly monitoring in their preferred native dialect (Hindi, Bengali, Marathi, etc.).",
                        techStack = "RemindersViewModel • Room SQLite",
                        inputSpec = "Due Timestamp + Localized Copy",
                        outputSpec = "FarmReminder Database Record",
                        latencySpec = "< 3ms",
                        icon = Icons.Default.EditCalendar
                    ),
                    WorkflowNode(
                        stepNumber = "02",
                        title = "Hardware AlarmManager Registration",
                        subtitle = "setExactAndAllowWhileIdle RTC_WAKEUP",
                        description = "Registers hardware-level RTC alarm using setExactAndAllowWhileIdle(). Acquires CPU Partial WakeLock for 10 seconds to guarantee alert delivery during Android 14/15 Doze Mode.",
                        techStack = "AlarmManager • PowerManager WakeLock",
                        inputSpec = "Exact Due Epoch Millis",
                        outputSpec = "OS Hardware Alarm PendingIntent",
                        latencySpec = "< 5ms",
                        icon = Icons.Default.Alarm
                    ),
                    WorkflowNode(
                        stepNumber = "03",
                        title = "Boot Auto-Recovery & Restore",
                        subtitle = "RECEIVE_BOOT_COMPLETED Broadcast Hook",
                        description = "When phone reboots, BootCompletedReceiver queries active non-completed reminders from Room DB and automatically reschedules every hardware alarm.",
                        techStack = "BootCompletedReceiver • CoroutineScope",
                        inputSpec = "ACTION_BOOT_COMPLETED Event",
                        outputSpec = "Restored Hardware Alarms in AlarmManager",
                        latencySpec = "< 15ms",
                        icon = Icons.Default.RestartAlt
                    ),
                    WorkflowNode(
                        stepNumber = "04",
                        title = "1-Hour Background WorkManager Update Check",
                        subtitle = "Network-Constrained Release Polling",
                        description = "WorkManager checks Render API every 1 hour only when phone is actively connected to internet. Bypasses execution when offline to conserve battery.",
                        techStack = "AppUpdateWorker • Constraints(CONNECTED)",
                        inputSpec = "1-Hour Periodic Trigger",
                        outputSpec = "Version Match / Newer Release Detected",
                        latencySpec = "~450ms (When Online)",
                        icon = Icons.Default.Update
                    ),
                    WorkflowNode(
                        stepNumber = "05",
                        title = "Deep-Link Push Notification Delivery",
                        subtitle = "Targeted Routing to Update / Reminder Screens",
                        description = "Renders high-priority notification with app logo. Tapping the update alert deep-links directly into the In-App Update Screen with safe Home backstack.",
                        techStack = "NotificationManagerCompat • DeepLink Intent",
                        inputSpec = "PendingNotification Data",
                        outputSpec = "Status Bar Push Notification",
                        latencySpec = "Instant OS Dispatch",
                        icon = Icons.Default.NotificationsActive
                    )
                )
            )
        )
    }

    // Selected workflow state (null = directory list, non-null = flowchart detail view)
    var selectedWorkflowId by remember { mutableStateOf<String?>(null) }
    val selectedWorkflow = workflows.firstOrNull { it.id == selectedWorkflowId }

    // Intercept hardware/system back button to return to directory view if inspecting a flowchart
    BackHandler(enabled = selectedWorkflow != null) {
        selectedWorkflowId = null
    }

    if (selectedWorkflow == null) {
        // LEVEL 1: WORKFLOW DIRECTORY / SELECTION SCREEN
        WorkflowsDirectoryView(
            workflows = workflows,
            onSelectWorkflow = { wf -> selectedWorkflowId = wf.id },
            onNavigateBack = onNavigateBack
        )
    } else {
        // LEVEL 2: DEDICATED ENGINEERING FLOWCHART VIEW WITH BLUEPRINT GRID
        WorkflowFlowchartDetailView(
            workflow = selectedWorkflow,
            onBackToDirectory = { selectedWorkflowId = null }
        )
    }
}

/**
 * Level 1 View: Directory of all available engineering workflows presented as interactive option cards.
 */
@Composable
fun WorkflowsDirectoryView(
    workflows: List<AppWorkflow>,
    onSelectWorkflow: (AppWorkflow) -> Unit,
    onNavigateBack: () -> Unit
) {
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
                            text = "Engineering Systems",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 19.sp
                            )
                        )
                        Text(
                            text = "Interactive Architecture & Flowchart Directory",
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
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Overview Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schema,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "System Architecture Modules",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                )
                                Text(
                                    text = "${workflows.size} Decoupled Pipelines Active",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = EmeraldPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Select any subsystem module below to open its dedicated engineering blueprint flowchart, stage-by-stage tensors, hardware delegates, and fault-tolerance branches.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                fontSize = 12.5.sp,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }
            }

            // Section Label
            item {
                Text(
                    text = "SELECT A WORKFLOW TO INSPECT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            // 7 Workflow Option Cards
            items(workflows) { wf ->
                WorkflowOptionCard(
                    workflow = wf,
                    onClick = { onSelectWorkflow(wf) }
                )
            }
        }
    }
}

/**
 * Clickable option card representing a workflow subsystem in the directory list.
 */
@Composable
fun WorkflowOptionCard(
    workflow: AppWorkflow,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Category & Capability Badge Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = EmeraldPrimary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = workflow.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp
                        ),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = workflow.capabilityBadge,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp
                        ),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Icon + Title & Short Summary
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(listOf(EmeraldPrimary.copy(alpha = 0.2f), EmeraldDark.copy(alpha = 0.3f)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = workflow.icon,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = workflow.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = workflow.summary,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp
                        ),
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Bottom Specs & Call to action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountTree,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${workflow.nodes.size} Stages",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = workflow.totalLatency,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(EmeraldPrimary.copy(alpha = 0.1f))
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Inspect Flowchart",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/**
 * Level 2 View: Dedicated Engineering Blueprint Flowchart with technical grid background.
 */
@Composable
fun WorkflowFlowchartDetailView(
    workflow: AppWorkflow,
    onBackToDirectory: () -> Unit
) {
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
                        onClick = onBackToDirectory,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Workflows",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = workflow.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = "Engineering Blueprint Flowchart • ${workflow.nodes.size} Stages",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Surface(
                        onClick = onBackToDirectory,
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "All Workflows",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        // Engineering Technical CAD / Blueprint Grid Canvas
        EngineeringGridBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Header Blueprint Summary Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.5.dp, EmeraldPrimary.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = EmeraldPrimary.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "📐 SYSTEM BLUEPRINT: ${workflow.category.uppercase()}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = EmeraldPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.5.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = workflow.capabilityBadge,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.5.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = workflow.summary,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Latency: ${workflow.totalLatency}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccountTree,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${workflow.nodes.size} Pipeline Nodes",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 1. FLOWCHART START TERMINAL
                item {
                    FlowchartTerminalNode(
                        label = "START: Pipeline Trigger & Specimen Intake",
                        isStart = true
                    )
                    FlowchartConnectorPipe()
                }

                // 2. FLOWCHART NODES & PIPES
                itemsIndexed(workflow.nodes) { index, node ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        FlowchartNodeCard(node = node)

                        if (index < workflow.nodes.size - 1) {
                            FlowchartConnectorArrow(
                                isDecisionNode = node.isDecisionNode,
                                branchA = node.branchA,
                                branchB = node.branchB
                            )
                        } else {
                            FlowchartConnectorPipe()
                        }
                    }
                }

                // 3. FLOWCHART END TERMINAL
                item {
                    FlowchartTerminalNode(
                        label = "END: Pipeline Execution Completed (Action Ready)",
                        isStart = false
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Return Button
                    Button(
                        onClick = onBackToDirectory,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Back to All System Workflows",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

/**
 * Technical drafting graph-paper / blueprint grid canvas drawn on the background.
 */
@Composable
fun EngineeringGridBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val gridColor = if (isDark) Color(0xFF10B981).copy(alpha = 0.05f) else Color(0xFF0F172A).copy(alpha = 0.045f)
    val dotColor = if (isDark) Color(0xFF10B981).copy(alpha = 0.14f) else Color(0xFF0F172A).copy(alpha = 0.09f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                val step = 26.dp.toPx()
                val width = size.width
                val height = size.height

                // Draw vertical technical lines
                var x = 0f
                while (x <= width) {
                    drawLine(
                        color = gridColor,
                        start = androidx.compose.ui.geometry.Offset(x, 0f),
                        end = androidx.compose.ui.geometry.Offset(x, height),
                        strokeWidth = 1f
                    )
                    x += step
                }

                // Draw horizontal technical lines
                var y = 0f
                while (y <= height) {
                    drawLine(
                        color = gridColor,
                        start = androidx.compose.ui.geometry.Offset(0f, y),
                        end = androidx.compose.ui.geometry.Offset(width, y),
                        strokeWidth = 1f
                    )
                    y += step
                }

                // Draw technical cross-dots at intersections every 2nd grid point
                val dotStep = step * 2
                var dotX = 0f
                while (dotX <= width) {
                    var dotY = 0f
                    while (dotY <= height) {
                        drawCircle(
                            color = dotColor,
                            radius = 1.8f,
                            center = androidx.compose.ui.geometry.Offset(dotX, dotY)
                        )
                        dotY += dotStep
                    }
                    dotX += dotStep
                }
            }
    ) {
        content()
    }
}

/**
 * Terminal start/end pill node in standard flowchart engineering syntax.
 */
@Composable
fun FlowchartTerminalNode(
    label: String,
    isStart: Boolean
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = if (isStart) EmeraldPrimary.copy(alpha = 0.15f) else Color(0xFF3B82F6).copy(alpha = 0.15f),
            border = BorderStroke(
                width = 1.5.dp,
                color = if (isStart) EmeraldPrimary else Color(0xFF3B82F6)
            ),
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isStart) EmeraldPrimary else Color(0xFF3B82F6))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isStart) EmeraldPrimary else Color(0xFF3B82F6),
                        fontSize = 11.5.sp,
                        letterSpacing = 0.5.sp
                    )
                )
            }
        }
    }
}

@Composable
fun FlowchartConnectorPipe() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(16.dp)
                .background(EmeraldPrimary.copy(alpha = 0.7f))
        )
        Icon(
            imageVector = Icons.Default.ArrowDownward,
            contentDescription = null,
            tint = EmeraldPrimary,
            modifier = Modifier.size(16.dp)
        )
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
            containerColor = if (node.isDecisionNode) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f) else MaterialTheme.colorScheme.surface
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
                    .width(2.5.dp)
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
                    .width(2.5.dp)
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
                    .width(2.5.dp)
                    .height(14.dp)
                    .background(EmeraldPrimary.copy(alpha = 0.7f))
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
