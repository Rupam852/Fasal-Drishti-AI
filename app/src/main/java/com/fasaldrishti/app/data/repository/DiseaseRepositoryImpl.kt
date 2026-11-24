package com.fasaldrishti.app.data.repository

import com.fasaldrishti.app.data.remote.GeminiClient
import com.fasaldrishti.app.data.remote.NvidiaClient
import com.fasaldrishti.app.data.remote.PredictApi
import com.fasaldrishti.app.domain.model.DiseaseInfo
import com.fasaldrishti.app.domain.repository.DiseaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DiseaseRepositoryImpl(
    private val predictApi: PredictApi,
    private val geminiClient: GeminiClient? = null,
    private val nvidiaClient: NvidiaClient = NvidiaClient()
) : DiseaseRepository {

    // Comprehensive Indian Agricultural Pathology Knowledge Database
    private val staticDiseases = listOf(
        // ==========================================
        // 🌾 1. RICE / PADDY (धान)
        // ==========================================
        DiseaseInfo(
            classId = "Rice___Blast",
            cropName = "Rice",
            cropHindi = "धान (चावल)",
            diseaseName = "Rice Blast",
            diseaseHindi = "ब्लास्ट / झोंका रोग",
            severity = "Severe",
            isHealthy = false,
            symptoms = "Spindle-shaped diamond lesions with gray-white centers and reddish-brown borders on leaf blades, neck rot, and node rot.",
            symptomsHindi = "पत्तियों पर आंख या नाव के आकार के भूरे धब्बे जिनके केंद्र में राख जैसा रंग होता है, तने और बाली का सड़ना।",
            treatment = "• Recommended Chemical: Tricyclazole 75% WP @ 0.6g/L (Beam / Baan) or Kasugamycin 3% SL @ 2ml/L\n• 15-Liter Knapsack Tank Dose: 9-10g per 15-liter spray pump\n• Per Acre Field Dose: 120-150g in 200L water per acre\n• Spray Timing: Calm morning (7:00-9:30 AM) or cool evening (4:00-6:00 PM)\n• Resistance Management: Alternate with Isoprothiolane 40% EC (Fujione @ 1.5ml/L) after 10 days",
            treatmentHindi = "• मुख्य रासायनिक दवा: ट्राइसाइक्लाजोल 75% WP (बीम / बान @ 0.6 ग्राम/लीटर) या कासुगामाइसिन 3% SL (2 मिली/लीटर)\n• 15 लीटर टंकी खुराक: 9-10 ग्राम दवा प्रति 15L पंप\n• प्रति एकड़ मात्रा: 120-150 ग्राम दवा (200 लीटर पानी में)\n• छिड़काव समय: शांत सुबह (7:00-9:30 AM) या ठंडी शाम (4:00-6:00 PM)\n• दवा बदलना: 10 दिन बाद आइसोप्रोपियोलेन 40% EC (फ्यूजीवान @ 1.5 मिली/लीटर) से स्प्रे करें",
            prevention = "• Organic Remedy: Spray Pseudomonas fluorescens @ 5g/L or 5% Neem oil\n• Crop Sanitation: Avoid excess urea/nitrogen fertilizer, maintain standing water in field\n• Harvest Safety (PHI): Maintain 14-21 days safety interval before grain harvest"
        ),
        DiseaseInfo(
            classId = "Rice___Bacterial_blight",
            cropName = "Rice",
            cropHindi = "धान (चावल)",
            diseaseName = "Bacterial Leaf Blight",
            diseaseHindi = "जीवाणु पत्ती झुलसा (BLB)",
            severity = "Severe",
            isHealthy = false,
            symptoms = "Water-soaked stripes starting from leaf tips turning yellow-white with wavy margins, severe kresek (wilting).",
            symptomsHindi = "पत्तियों के किनारों से शुरू होकर नीचे की ओर जाने वाली पीली-सफेद लहरदार धारियां, पौधों का मुरझाना।",
            treatment = "• Recommended Chemical: Streptocycline @ 1g/10L mixed with Copper Oxychloride 50% WP @ 3g/L\n• 15-Liter Knapsack Tank Dose: 1.5g Streptocycline + 45g Copper Oxychloride per 15L spray pump\n• Per Acre Field Dose: 20-30g Streptocycline + 500g Copper Oxychloride in 200L water\n• Spray Timing: Spray in cool evening hours\n• Resistance Management: Repeat after 10-12 days if rains continue",
            treatmentHindi = "• मुख्य रासायनिक दवा: स्ट्रेप्टोसाइक्लिन (1 ग्राम प्रति 10 लीटर) + कॉपर ऑक्सीक्लोराइड 50% WP (3 ग्राम प्रति लीटर)\n• 15 लीटर टंकी खुराक: 1.5 ग्राम स्ट्रेप्टोसाइक्लिन + 45 ग्राम कॉपर प्रति 15L पंप\n• प्रति एकड़ मात्रा: 30 ग्राम स्ट्रेप्टोसाइक्लिन + 500 ग्राम कॉपर (200 लीटर पानी में)\n• छिड़काव समय: शाम के ठंडे समय छिड़काव करें\n• दवा बदलना: बारिश जारी रहने पर 10-12 दिन बाद दोबारा छिड़कें",
            prevention = "• Organic Remedy: Spray fresh cow dung extract supernatant (20%) or Trichoderma\n• Crop Sanitation: Drain standing field water for 3-4 days to stop bacterial swimming spread\n• Harvest Safety (PHI): Maintain 14 days waiting period before grain harvest"
        ),
        DiseaseInfo(
            classId = "Rice___Brown_spot",
            cropName = "Rice",
            cropHindi = "धान (चावल)",
            diseaseName = "Brown Spot",
            diseaseHindi = "भूरा धब्बा रोग",
            severity = "Moderate",
            isHealthy = false,
            symptoms = "Oval or circular dark brown spots with yellow halos across the entire leaf surface, grain discoloration.",
            symptomsHindi = "पत्तियों पर छोटे अंडाकार गहरे भूरे धब्बे, दानों पर बदरंग धब्बे।",
            treatment = "• Recommended Chemical: Mancozeb 75% WP @ 2.5g/L or Propiconazole 25% EC (Tilt @ 1ml/L)\n• 15-Liter Knapsack Tank Dose: 35-40g Mancozeb OR 15ml Tilt per 15L spray pump\n• Per Acre Field Dose: 500-600g Mancozeb in 200L water per acre\n• Spray Timing: Spray in morning or cool evening hours\n• Resistance Management: Alternate with Hexaconazole 5% EC (2ml/L) after 10-12 days",
            treatmentHindi = "• मुख्य रासायनिक दवा: मैनकोजेब 75% WP (2.5 ग्राम/लीटर) या प्रोपिकोनाज़ोल 25% EC (टिल्ट @ 1 मिली/लीटर)\n• 15 लीटर टंकी खुराक: 35-40 ग्राम मैनकोजेब अथवा 15 मिली टिल्ट प्रति 15L पंप\n• प्रति एकड़ मात्रा: 500-600 ग्राम दवा (200 लीटर पानी में)\n• छिड़काव समय: शांत सुबह या ठंडी शाम को छिड़कें\n• दवा बदलना: 10-12 दिन बाद हेक्साकोनाज़ोल 5% EC (2 मिली/लीटर) से स्प्रे करें",
            prevention = "• Organic Remedy: Apply balanced NPK + Zinc sulphate (10kg/acre), spray Pseudomonas fluorescens\n• Crop Sanitation: Improve soil drainage, avoid stagnant muddy water\n• Harvest Safety (PHI): Maintain 14 days waiting period before harvesting rice"
        ),
        DiseaseInfo(
            classId = "Rice___Sheath_blight",
            cropName = "Rice",
            cropHindi = "धान (चावल)",
            diseaseName = "Sheath Blight",
            diseaseHindi = "शीथ ब्लाइट (तना झुलसा)",
            severity = "Severe",
            isHealthy = false,
            symptoms = "Greenish-gray oval spots on leaf sheaths near water level, spreading upward forming snake-skin patterns.",
            symptomsHindi = "पानी की सतह के पास तने पर हरे-ग्रे अंडाकार धब्बे जो बाद में सांप की खाल जैसे दिखते हैं।",
            treatment = "• Recommended Chemical: Hexaconazole 5% EC (Contaf @ 2ml/L) or Validamycin 3% L @ 2.5ml/L\n• 15-Liter Knapsack Tank Dose: 30ml Contaf OR 35ml Validamycin per 15L spray pump\n• Per Acre Field Dose: 400ml Contaf in 200L water (target lower stem base)\n• Spray Timing: Direct spray nozzle at the base/sheath of the tillers near water level\n• Resistance Management: Alternate with Thifluzamide 24% SC (Pulsor @ 0.75ml/L) after 10 days",
            treatmentHindi = "• मुख्य रासायनिक दवा: हेक्साकोनाज़ोल 5% EC (कॉन्टाफ @ 2 मिली/लीटर) या वैलिडामाइसिन 3% L @ 2.5 मिली/लीटर\n• 15 लीटर टंकी खुराक: 30 मिली कॉन्टाफ अथवा 35 मिली वैलिडामाइसिन प्रति 15L पंप\n• प्रति एकड़ मात्रा: 400 मिली दवा (200 लीटर पानी में, तने के निचले हिस्से पर स्प्रे करें)\n• छिड़काव समय: स्प्रे का नोजल पानी की सतह के पास तनों पर रखें\n• दवा बदलना: 10 दिन बाद थिफ्लूजामाइड 24% SC (पल्सर @ 0.75 मिली/लीटर) से स्प्रे करें",
            prevention = "• Organic Remedy: Apply Trichoderma viride enriched farmyard manure in soil\n• Crop Sanitation: Reduce urea overdose, remove weed hosts (Echinochloa) from field bunds\n• Harvest Safety (PHI): Maintain 14-21 days safety period before harvesting paddy"
        ),
        DiseaseInfo(
            classId = "Rice___healthy",
            cropName = "Rice",
            cropHindi = "धान (चावल)",
            diseaseName = "Healthy Rice Crop",
            diseaseHindi = "स्वस्थ धान की फसल",
            severity = "None",
            isHealthy = true,
            symptoms = "Vibrant lush-green erect leaves, strong tillering, healthy root system.",
            symptomsHindi = "पत्तियां पूरी तरह हरी, मजबूत कल्ले और स्वस्थ जड़ें।",
            treatment = "No disease detected. Continue scheduled urea split application and irrigation.",
            treatmentHindi = "किसी दवाई की आवश्यकता नहीं। समय पर सिंचाई और संतुलित खाद जारी रखें।",
            prevention = "Weekly crop scouting, keep field bunds weed-free."
        ),

        // ==========================================
        // 🌾 2. WHEAT (गेहूं)
        // ==========================================
        DiseaseInfo(
            classId = "Wheat___Yellow_rust",
            cropName = "Wheat",
            cropHindi = "गेहूं",
            diseaseName = "Yellow Stripe Rust",
            diseaseHindi = "पीला रतुआ (हल्दी रोग)",
            severity = "Severe",
            isHealthy = false,
            symptoms = "Bright yellow pustules arranged in parallel linear stripes along the leaf veins, rubbing off yellow powder on fingers.",
            symptomsHindi = "पत्तियों पर समानांतर कतारों में चमकीले पीले रंग के पाउडर जैसे दाने, उंगली लगाने पर पीला रंग लगना।",
            treatment = "• Recommended Chemical: Propiconazole 25% EC (Tilt @ 1ml/L) or Tebuconazole 25.9% EC @ 1ml/L\n• 15-Liter Knapsack Tank Dose: 15-20ml per 15-liter spray pump\n• Per Acre Field Dose: 200ml in 200L water per acre\n• Spray Timing: Calm sunny morning (8:00-11:00 AM) after dew dries\n• Resistance Management: Single timely spray at disease initiation usually protects flag leaf completely",
            treatmentHindi = "• मुख्य रासायनिक दवा: प्रोपिकोनाज़ोल 25% EC (टिल्ट @ 1 मिली/लीटर) या टेबुकोनाज़ोल 25.9% EC @ 1 मिली/लीटर\n• 15 लीटर टंकी खुराक: 15-20 मिली दवा प्रति 15L पंप\n• प्रति एकड़ मात्रा: 200 मिली दवा (200 लीटर पानी में)\n• छिड़काव समय: ओस सूखने के बाद धूप वाली सुबह (8:00-11:00 AM)\n• दवा बदलना: लक्षण दिखते ही पहला स्प्रे झंडा पत्ती (Flag Leaf) को पूरी तरह बचा लेता है",
            prevention = "• Organic Remedy: Spray Trichoderma viride or 5% Neem oil at early tillering\n• Crop Sanitation: Grow resistant varieties (HD 2967, DBW 187, DBW 222), avoid excessive late urea\n• Harvest Safety (PHI): Maintain 30 days interval before harvesting wheat grain"
        ),
        DiseaseInfo(
            classId = "Wheat___Brown_rust",
            cropName = "Wheat",
            cropHindi = "गेहूं",
            diseaseName = "Brown Leaf Rust",
            diseaseHindi = "भूरा रतुआ",
            severity = "Moderate",
            isHealthy = false,
            symptoms = "Small round or oval orange-brown scattered pustules on upper leaf blades.",
            symptomsHindi = "पत्तियों की ऊपरी सतह पर बिखरे हुए नारंगी-भूरे रंग के धब्बे।",
            treatment = "• Recommended Chemical: Mancozeb 75% WP @ 2.5g/L or Propiconazole 25% EC (Tilt @ 1ml/L)\n• 15-Liter Knapsack Tank Dose: 35-40g Mancozeb OR 15ml Tilt per 15L spray pump\n• Per Acre Field Dose: 500-600g in 200L water per acre\n• Spray Timing: Calm morning after morning dew has evaporated\n• Resistance Management: Single timely spray at first rust pustule appearance",
            treatmentHindi = "• मुख्य रासायनिक दवा: मैनकोजेब 75% WP (2.5 ग्राम/लीटर) या प्रोपिकोनाज़ोल 25% EC (टिल्ट @ 1 मिली/लीटर)\n• 15 लीटर टंकी खुराक: 35-40 ग्राम मैनकोजेब अथवा 15 मिली टिल्ट प्रति 15L पंप\n• प्रति एकड़ मात्रा: 500-600 ग्राम दवा (200 लीटर पानी में)\n• छिड़काव समय: सुबह की ओस सूखने के बाद शांत मौसम में छिड़कें\n• दवा बदलना: पत्तियों पर पहला धब्बा दिखते ही समय पर एक स्प्रे करें",
            prevention = "• Organic Remedy: Spray Trichoderma viride preventive spray at jointing stage\n• Crop Sanitation: Avoid late sowing, apply balanced potash (MOP) to strengthen flag leaf\n• Harvest Safety (PHI): Maintain 25-30 days waiting period before grain harvest"
        ),
        DiseaseInfo(
            classId = "Wheat___healthy",
            cropName = "Wheat",
            cropHindi = "गेहूं",
            diseaseName = "Healthy Wheat Crop",
            diseaseHindi = "स्वस्थ गेहूं की फसल",
            severity = "None",
            isHealthy = true,
            symptoms = "Crisp deep-green foliage, robust earheads, vigorous flag leaf.",
            symptomsHindi = "गहरे हरे रंग की स्वस्थ पत्तियां और मजबूत बालियां।",
            treatment = "Healthy stand. Provide light irrigation at CRI and flowering stages.",
            treatmentHindi = "कोई उपचार आवश्यक नहीं। कल्ले फूटते समय और दाना भरते समय हल्की सिंचाई करें।",
            prevention = "Regular monitoring for aphid infestation during cloudy winter days."
        ),

        // ==========================================
        // 🌿 3. COTTON (कपास / रुई)
        // ==========================================
        DiseaseInfo(
            classId = "Cotton___Leaf_curl",
            cropName = "Cotton",
            cropHindi = "कपास",
            diseaseName = "Cotton Leaf Curl Virus",
            diseaseHindi = "पत्ती मरोड़ रोग (CLCuD)",
            severity = "Severe",
            isHealthy = false,
            symptoms = "Upward/downward leaf curling, vein thickening, leaf enation (cup-like growth under leaf), stunted plants.",
            symptomsHindi = "पत्तियों का ऊपर या नीचे की ओर मुड़ना, नसों का मोटा होना, पत्ती के नीचे छोटी पत्ती जैसी गांठ बनना।",
            treatment = "• Recommended Chemical: Thiamethoxam 25% WG (Actara @ 0.3g/L) or Diafenthiuron 50% WP (Pegasus @ 1.2g/L) to control whitefly\n• 15-Liter Knapsack Tank Dose: 5g Actara OR 18g Pegasus per 15L spray pump\n• Per Acre Field Dose: 80g Actara in 200L water per acre\n• Spray Timing: Spray in cool evening or early morning on leaf undersides\n• Resistance Management: Alternate with Pyriproxyfen 10% EC (Lano @ 2ml/L) after 10 days",
            treatmentHindi = "• मुख्य रासायनिक दवा: सफेद मक्खी रोकथाम हेतु थायमेथॉक्सम 25% WG (एकतारा @ 0.3 ग्राम/लीटर) या डायफेंथियूरॉन 50% WP (पेगासस @ 1.2 ग्राम/लीटर)\n• 15 लीटर टंकी खुराक: 5 ग्राम एकतारा अथवा 18 ग्राम पेगासस प्रति 15L पंप\n• प्रति एकड़ मात्रा: 80 ग्राम एकतारा (200 लीटर पानी में)\n• छिड़काव समय: सुबह या शाम को पत्तियों की निचली सतह पर छिड़कें\n• दवा बदलना: 10 दिन बाद पाइरीप्रॉक्सीफेन 10% EC (2 मिली/लीटर) से स्प्रे करें",
            prevention = "• Organic Remedy: Spray 5% Neem oil (5ml/L) or castor oil sticky traps\n• Crop Sanitation: Eradicate weed hosts (Kanghi booti, Peeli booti) from field borders\n• Harvest Safety (PHI): Maintain 15-20 days waiting period before boll picking"
        ),
        DiseaseInfo(
            classId = "Cotton___Bacterial_blight",
            cropName = "Cotton",
            cropHindi = "कपास",
            diseaseName = "Bacterial Blight / Angular Leaf Spot",
            diseaseHindi = "जीवाणु झुलसा / कोणीय धब्बा (Black Arm)",
            severity = "Severe",
            isHealthy = false,
            symptoms = "Angular water-soaked spots bounded by leaf veinlets, blackening of petioles and stems ('Black Arm').",
            symptomsHindi = "पत्तियों की नसों के बीच कोणीय पानीदार काले धब्बे, टहनियों का काला पड़कर सूखना।",
            treatment = "• Recommended Chemical: Copper Oxychloride 50% WP @ 3g/L mixed with Streptocycline @ 1g/10L\n• 15-Liter Knapsack Tank Dose: 45g Copper Oxychloride + 1.5g Streptocycline per 15L spray pump\n• Per Acre Field Dose: 500g Copper + 20g Streptocycline in 200L water\n• Spray Timing: Calm morning or cool evening hours\n• Resistance Management: Repeat spray after 10-12 days if humid cloudy weather continues",
            treatmentHindi = "• मुख्य रासायनिक दवा: कॉपर ऑक्सीक्लोराइड 50% WP (3 ग्राम/लीटर) + स्ट्रेप्टोसाइक्लिन (1 ग्राम प्रति 10 लीटर)\n• 15 लीटर टंकी खुराक: 45 ग्राम कॉपर + 1.5 ग्राम स्ट्रेप्टोसाइक्लिन प्रति 15L पंप\n• प्रति एकड़ मात्रा: 500 ग्राम कॉपर + 20 ग्राम स्ट्रेप्टोसाइक्लिन (200 लीटर पानी में)\n• छिड़काव समय: सुबह या शाम को समान रूप से छिड़काव करें\n• दवा बदलना: नम मौसम रहने पर 10-12 दिन बाद दोबारा दोहराएं",
            prevention = "• Organic Remedy: Spray Pseudomonas fluorescens @ 5g/L foliar spray\n• Crop Sanitation: Destroy previous crop residue, avoid high nitrogen fertilisation\n• Harvest Safety (PHI): Maintain 15 days waiting period before picking cotton"
        ),
        DiseaseInfo(
            classId = "Cotton___healthy",
            cropName = "Cotton",
            cropHindi = "कपास",
            diseaseName = "Healthy Cotton Crop",
            diseaseHindi = "स्वस्थ कपास की फसल",
            severity = "None",
            isHealthy = true,
            symptoms = "Broad vibrant green leaves, healthy square formation and boll development.",
            symptomsHindi = "स्वस्थ चौड़ी पत्तियां, अच्छी कलियां और टिंडे का विकास।",
            treatment = "Crop is disease-free. Maintain balanced nitrogen-potash fertilization.",
            treatmentHindi = "किसी दवाई की आवश्यकता नहीं। समय पर सिंचाई और कीट निगरानी करें।",
            prevention = "Install yellow sticky traps (10/acre) for sucking pest monitoring."
        ),

        // ==========================================
        // 🎋 4. SUGARCANE (गन्ना)
        // ==========================================
        DiseaseInfo(
            classId = "Sugarcane___Red_rot",
            cropName = "Sugarcane",
            cropHindi = "गन्ना",
            diseaseName = "Red Rot Disease",
            diseaseHindi = "लाल सड़न रोग (कैंसर ऑफ केन)",
            severity = "Severe",
            isHealthy = false,
            symptoms = "Third and fourth leaves wither, split cane shows internal reddening with characteristic transverse white patches and alcohol odor.",
            symptomsHindi = "ऊपर की तीसरी-चौथी पत्ती सूखना, गन्ना चीरने पर अंदर लाल रंग और सफेद आड़े धब्बे दिखना, शराब जैसी गंध।",
            treatment = "Uproot and burn diseased clumps. Dip seed setts in Carbendazim 50 WP @ 1 g/L for 15 mins before planting.",
            treatmentHindi = "संक्रमित गन्नों को जड़ से उखाड़कर जलाएं। बुवाई से पहले बीजों को कार्बेन्डाजिम (1 ग्राम/लीटर) में 15 मिनट डुबोएं।",
            prevention = "Rotate with paddy, use healthy certified setts, avoid ratoon crop from infected field."
        ),
        DiseaseInfo(
            classId = "Sugarcane___Smut",
            cropName = "Sugarcane",
            cropHindi = "गन्ना",
            diseaseName = "Sugarcane Smut",
            diseaseHindi = "कंडुआ / चाबुक रोग",
            severity = "Moderate",
            isHealthy = false,
            symptoms = "Long black whip-like dusty structure protruding from the central growing shoot tip.",
            symptomsHindi = "पौधे के मुख्य सिरे से लंबी काली चाबुक जैसी संरचना निकलना जिसपर काला पाउडर होता है।",
            treatment = "Cut whip in plastic bag to prevent spore spread and burn. Spray Triadimefon 25 WP @ 1 g/L.",
            treatmentHindi = "चाबुक को पॉलीथीन में बंद करके काटें और नष्ट करें। ट्राइडिमेफॉन (1 ग्राम/लीटर) का छिड़काव करें।",
            prevention = "Use smut-resistant varieties (Co 0238, CoLk 94184), hot water treatment of seed setts."
        ),
        DiseaseInfo(
            classId = "Sugarcane___healthy",
            cropName = "Sugarcane",
            cropHindi = "गन्ना",
            diseaseName = "Healthy Sugarcane",
            diseaseHindi = "स्वस्थ गन्ना",
            severity = "None",
            isHealthy = true,
            symptoms = "Thick healthy green stalks, broad upright foliage, rich sucrose content.",
            symptomsHindi = "मजबूत तने, स्वस्थ हरी पत्तियां और भरपूर मिठास।",
            treatment = "No treatment needed. Continue earthing up and adequate furrow irrigation.",
            treatmentHindi = "कोई उपचार आवश्यक नहीं। मिट्टी चढ़ाने और सिंचाई का कार्य सुचारू रखें।",
            prevention = "Proper field drainage during monsoon season."
        ),

        // ==========================================
        // 🌶️ 5. CHILLI / PEPPER (मिर्च)
        // ==========================================
        DiseaseInfo(
            classId = "Chilli___Leaf_curl",
            cropName = "Chilli",
            cropHindi = "मिर्च",
            diseaseName = "Chilli Leaf Curl Virus (Murda Rog)",
            diseaseHindi = "मिर्च का पत्ती मरोड़ रोग (मुर्रा / चुर्रा-मुर्रा)",
            severity = "Severe",
            isHealthy = false,
            symptoms = "Puckering and upward curling of leaves, leaf size reduction, clustered crowded bushy appearance with poor fruit set.",
            symptomsHindi = "पत्तियों का ऊपर की ओर मुड़कर नाव जैसा बनना, पत्तियां छोटी होना और पौधे का झाड़ीदार हो जाना।",
            treatment = "• Recommended Chemical: Imidacloprid 17.8% SL (Confidor @ 0.5ml/L) or Diafenthiuron 50% WP (Pegasus @ 1.2g/L)\n• 15-Liter Knapsack Tank Dose: 7-8ml Confidor OR 18g Pegasus per 15L spray pump\n• Per Acre Field Dose: 100ml in 200L water per acre\n• Spray Timing: Calm morning or evening hours\n• Resistance Management: Alternate with Fipronil 5% SC (2ml/L) after 8-10 days to break vector resistance",
            treatmentHindi = "• मुख्य रासायनिक दवा: इमिडाक्लोप्रिड 17.8% SL (कॉन्फिडोर @ 0.5 मिली/लीटर) या डायफेंथियूरॉन 50% WP (पेगासस @ 1.2 ग्राम/लीटर)\n• 15 लीटर टंकी खुराक: 7-8 मिली कॉन्फिडोर अथवा 18 ग्राम पेगासस प्रति 15L पंप\n• प्रति एकड़ मात्रा: 100 मिली दवा (200 लीटर पानी में)\n• छिड़काव समय: शांत सुबह या ठंडी शाम को छिड़कें\n• दवा बदलना: कीटों की प्रतिरोधक क्षमता रोकने के लिए 10 दिन बाद फिप्रोनिल 5% SC (2 मिली/लीटर) डालें",
            prevention = "• Organic Remedy: Spray 5% Neem oil (5ml/L) every 7 days as repellent\n• Crop Sanitation: Install 15 yellow & blue sticky traps per acre for thrips/whitefly monitoring\n• Harvest Safety (PHI): Maintain 5-7 days interval before harvesting green chillies"
        ),
        DiseaseInfo(
            classId = "Chilli___Anthracnose",
            cropName = "Chilli",
            cropHindi = "मिर्च",
            diseaseName = "Anthracnose / Dieback / Fruit Rot",
            diseaseHindi = "एंथ्रेक्नोज / फल सड़न / डाईबैक",
            severity = "Severe",
            isHealthy = false,
            symptoms = "Drying of twigs from top downwards ('Dieback'), sunken circular water-soaked spots on fruits with orange-black concentric rings.",
            symptomsHindi = "टहनियों का ऊपर से नीचे की ओर सूखना, पके और कच्चे फलों पर धंसे हुए काले-नारंगी गोल धब्बे।",
            treatment = "Spray Azoxystrobin 23 SC @ 1 ml/L or Difenoconazole 25 EC @ 0.5 ml/L or Mancozeb @ 2.5 g/L.",
            treatmentHindi = "एज़ोक्सीस्ट्रोबिन (1 मिली/लीटर) या डिफेनोकोनाज़ोल (0.5 मिली/लीटर) का 10 दिन के अंतराल पर छिड़काव करें।",
            prevention = "Seed treatment with Thiram (2g/kg), collect and destroy infected chilli fruits."
        ),
        DiseaseInfo(
            classId = "Chilli___healthy",
            cropName = "Chilli",
            cropHindi = "मिर्च",
            diseaseName = "Healthy Chilli Crop",
            diseaseHindi = "स्वस्थ मिर्च की फसल",
            severity = "None",
            isHealthy = true,
            symptoms = "Smooth glossy dark-green foliage, vigorous blooming and abundant pod setting.",
            symptomsHindi = "चमकदार हरी पत्तियां, भरपूर फूल और स्वस्थ तीखी मिर्चियां।",
            treatment = "Stand is healthy. Apply micronutrient spray (Multiplex / Microla) for fruit shine.",
            treatmentHindi = "पौधे स्वस्थ हैं। फूलों और फलों की वृद्धि हेतु सूक्ष्म पोषक तत्व का हल्का छिड़काव करें।",
            prevention = "Maintain soil moisture without water stagnation."
        ),

        // ==========================================
        // 🟡 6. MUSTARD (सरसों / राई)
        // ==========================================
        DiseaseInfo(
            classId = "Mustard___White_rust",
            cropName = "Mustard",
            cropHindi = "सरसों (राई)",
            diseaseName = "White Rust & Staghead",
            diseaseHindi = "सफेद रतुआ (व्हाइट रस्ट / स्टैगहेड)",
            severity = "Severe",
            isHealthy = false,
            symptoms = "White chalky blister-like pustules on lower leaf surfaces, floral parts malformed into grotesque stagheads.",
            symptomsHindi = "पत्तियों की निचली सतह पर सफेद उभरे हुए छाले जैसे धब्बे, फूलों का विकृत होकर हिरन के सींग जैसा बनना।",
            treatment = "Spray Metalaxyl 8% + Mancozeb 64% WP (Ridomil Gold @ 2 g/L) or Mancozeb @ 2.5 g/L.",
            treatmentHindi = "रिडोमिल गोल्ड (2 ग्राम/लीटर) या मैनकोजेब 75 WP (2.5 ग्राम/लीटर) का तुरंत छिड़काव करें।",
            prevention = "Early sowing before mid-October, destroy diseased crop residue."
        ),
        DiseaseInfo(
            classId = "Mustard___Alternaria_blight",
            cropName = "Mustard",
            cropHindi = "सरसों (राई)",
            diseaseName = "Alternaria Black Spot",
            diseaseHindi = "अल्टरनेरिया काला धब्बा",
            severity = "Moderate",
            isHealthy = false,
            symptoms = "Concentric brown-black target spots on leaves, stems, and seed pods leading to shriveled grains.",
            symptomsHindi = "पत्तियों और फलियों पर काले-भूरे रंग के छल्लेदार धब्बे, फलियों में दाने सिकुड़ना।",
            treatment = "Spray Iprodione 50 WP @ 2 g/L or Mancozeb @ 2.5 g/L at pod formation.",
            treatmentHindi = "इप्रोडिओन (2 ग्राम/लीटर) या मैनकोजेब (2.5 ग्राम/लीटर) का छिड़काव करें।",
            prevention = "Use clean disease-free certified seeds, treat seeds with Trichoderma viride."
        ),
        DiseaseInfo(
            classId = "Mustard___healthy",
            cropName = "Mustard",
            cropHindi = "सरसों (राई)",
            diseaseName = "Healthy Mustard Crop",
            diseaseHindi = "स्वस्थ सरसों की फसल",
            severity = "None",
            isHealthy = true,
            symptoms = "Broad vibrant leaves, profuse yellow flowering, plump healthy siliquae (pods).",
            symptomsHindi = "स्वस्थ चौड़ी पत्तियां, चमकीले पीले फूल और दानों से भरी फलियां।",
            treatment = "No treatment needed. Monitor for mustard aphid (माहू) during flowering.",
            treatmentHindi = "कोई उपचार आवश्यक नहीं। माहू कीट की नियमित निगरानी रखें।",
            prevention = "Ensure proper row spacing (30cm x 10cm)."
        ),

        // ==========================================
        // 🥭 7. MANGO (आम)
        // ==========================================
        DiseaseInfo(
            classId = "Mango___Anthracnose",
            cropName = "Mango",
            cropHindi = "आम",
            diseaseName = "Mango Anthracnose",
            diseaseHindi = "आम का एंथ्रेक्नोज (काला धब्बा)",
            severity = "Severe",
            isHealthy = false,
            symptoms = "Black irregular necrotic spots on leaves, blossom blight with withered dark panicles, black tear-staining on fruits.",
            symptomsHindi = "पत्तियों पर काले अनियमित धब्बे, बौर (फूल) का सूखकर काला पड़ना, फलों पर काले धब्बे।",
            treatment = "Spray Carbendazim 50 WP @ 1 g/L or Copper Oxychloride 50 WP @ 3 g/L at blossom emergence and fruit set.",
            treatmentHindi = "कार्बेन्डाजिम 50 WP (1 ग्राम/लीटर) या कॉपर ऑक्सीक्लोराइड (3 ग्राम/लीटर) का बौर निकलने पर छिड़काव करें।",
            prevention = "Prune dead criss-cross branches in October, hot water treatment of harvested fruits (52°C for 5 min)."
        ),
        DiseaseInfo(
            classId = "Mango___Powdery_mildew",
            cropName = "Mango",
            cropHindi = "आम",
            diseaseName = "Powdery Mildew",
            diseaseHindi = "चूर्णिल आसिता (भभूतिया रोग)",
            severity = "Moderate",
            isHealthy = false,
            symptoms = "White powdery flour-like coating covering flower panicles and young tender leaves causing flower and fruit drop.",
            symptomsHindi = "बौर और कोमल पत्तियों पर सफेद आटे जैसा पाउडर जमना जिससे बौर और छोटे फल झड़ जाते हैं।",
            treatment = "Spray Wettable Sulphur 80 WP @ 2.5 g/L or Hexaconazole 5 EC @ 1 ml/L.",
            treatmentHindi = "घुलनशील गंधक (सल्फर 2.5 ग्राम/लीटर) या हेक्साकोनाज़ोल (1 मिली/लीटर) का छिड़काव करें।",
            prevention = "Spray before flower buds open, avoid dense orchard shading."
        ),
        DiseaseInfo(
            classId = "Mango___healthy",
            cropName = "Mango",
            cropHindi = "आम",
            diseaseName = "Healthy Mango Tree",
            diseaseHindi = "स्वस्थ आम का पेड़",
            severity = "None",
            isHealthy = true,
            symptoms = "Lush glossy leathery leaves, dense floral bloom, clean developing fruitlets.",
            symptomsHindi = "चमकदार स्वस्थ पत्तियां, भरपूर बौर और स्वस्थ टिकोरे।",
            treatment = "Tree is healthy. Provide irrigation during fruit development stage.",
            treatmentHindi = "पेड़ स्वस्थ है। फलों के विकास के समय नियमित थाला सिंचाई करें।",
            prevention = "Apply tree grease band on trunk to stop mealybug nymphs."
        ),

        // ==========================================
        // 🍅 8. TOMATO (टमाटर)
        // ==========================================
        DiseaseInfo(
            classId = "Tomato___Late_blight",
            cropName = "Tomato",
            cropHindi = "टमाटर",
            diseaseName = "Late Blight",
            diseaseHindi = "पछेती झुलसा",
            severity = "Severe",
            isHealthy = false,
            symptoms = "Dark brown water-soaked lesions on leaves and stems, white fungal mold underneath in humid fog.",
            symptomsHindi = "पत्तियों और तनों पर गहरे भूरे रंग के धब्बे, नम मौसम में सफेद फफूंद।",
            treatment = "• Recommended Chemical: Metalaxyl 8% + Mancozeb 64% WP (Ridomil Gold @ 2.5g/L) or Cymoxanil + Mancozeb (Curzate @ 2.5g/L)\n• 15-Liter Knapsack Tank Dose: 35-40g per 15-liter spray pump\n• Per Acre Field Dose: 600-700g in 200L water per acre\n• Spray Timing: Spray immediately within 24 hours of cool foggy weather\n• Resistance Management: Alternate with Dimethomorph 50% WP (1g/L) after 7-10 days",
            treatmentHindi = "• मुख्य रासायनिक दवा: मेटालैक्सिल 8% + मैनकोजेब 64% WP (रिडोमिल गोल्ड @ 2.5 ग्राम/लीटर) या साइमोक्सानिल + मैनकोजेब (कर्जेट @ 2.5 ग्राम/लीटर)\n• 15 लीटर टंकी खुराक: 35-40 ग्राम दवा प्रति 15L पंप\n• प्रति एकड़ मात्रा: 600-700 ग्राम दवा (200 लीटर पानी में)\n• छिड़काव समय: कोहरे या नम मौसम के 24 घंटे के अंदर तुरंत छिड़कें\n• दवा बदलना: 7-10 दिन बाद डाइमथोमोर्फ 50% WP (1 ग्राम/लीटर) से स्प्रे करें",
            prevention = "• Organic Remedy: Foliar spray of Trichoderma viride @ 5g/L or sour buttermilk (500ml/15L)\n• Crop Sanitation: Destroy infected foliage immediately, avoid sprinkler/overhead irrigation\n• Harvest Safety (PHI): Maintain 7 days waiting period before fruit picking"
        ),
        DiseaseInfo(
            classId = "Tomato___Early_blight",
            cropName = "Tomato",
            cropHindi = "टमाटर",
            diseaseName = "Early Blight",
            diseaseHindi = "अगेती झुलसा",
            severity = "Moderate",
            isHealthy = false,
            symptoms = "Concentric dark brown rings forming target-board spots on older lower leaves.",
            symptomsHindi = "पुरानी पत्तियों पर भूरे रंग के छल्लेदार धब्बे।",
            treatment = "• Recommended Chemical: Mancozeb 75% WP @ 2.5g/L (Dithane M-45) or Chlorothalonil 75% WP @ 2g/L (Kavach)\n• 15-Liter Knapsack Tank Dose: 35-40g per 15-liter spray pump\n• Per Acre Field Dose: 600-800g in 200L water per acre\n• Spray Timing: Calm morning (7:00-9:30 AM) or cool evening (4:00-6:00 PM)\n• Resistance Management: Alternate with Azoxystrobin 23% SC @ 1ml/L after 8-10 days",
            treatmentHindi = "• मुख्य रासायनिक दवा: मैनकोजेब 75% WP (डाइथेन M-45 @ 2.5 ग्राम/लीटर) या क्लोरोथैलोनिल 75% WP (कवच)\n• 15 लीटर टंकी खुराक: 35-40 ग्राम दवा प्रति 15L पंप\n• प्रति एकड़ मात्रा: 600-800 ग्राम दवा (200 लीटर पानी में)\n• छिड़काव समय: शांत सुबह (7:00-9:30 AM) या ठंडी शाम (4:00-6:00 PM)\n• दवा बदलना: 8-10 दिन बाद एजॉक्सीस्ट्रोबिन 23% SC से स्प्रे करें",
            prevention = "• Organic Remedy: Spray 5% Neem oil (5ml/L) or sour buttermilk (500ml/15L pump)\n• Crop Sanitation: Prune lower leaves touching soil, use straw mulch, stake plants with trellis\n• Harvest Safety (PHI): Maintain 7-10 days waiting interval before harvesting tomatoes"
        ),
        DiseaseInfo(
            classId = "Tomato___Tomato_Yellow_Leaf_Curl_Virus",
            cropName = "Tomato",
            cropHindi = "टमाटर",
            diseaseName = "Tomato Yellow Leaf Curl Virus",
            diseaseHindi = "पत्ती मरोड़ पीला मोज़ेक (TYLCV)",
            severity = "Severe",
            isHealthy = false,
            symptoms = "Severe leaf curling, cup-shaped deformation, yellowing margins, bushy stunted plant.",
            symptomsHindi = "पत्तियों का मुड़ना, किनारों का पीला पड़ना, पौधे का बौना रह जाना और फल न लगना।",
            treatment = "• Recommended Chemical: Target whitefly vector: Imidacloprid 17.8% SL (Confidor @ 0.5ml/L) or Diafenthiuron 50% WP @ 1.2g/L\n• 15-Liter Knapsack Tank Dose: 7-8ml Confidor OR 18g Diafenthiuron per 15L spray pump\n• Per Acre Field Dose: 100ml in 200L water per acre (add sticker/surfactant)\n• Spray Timing: Spray in morning or evening on leaf undersides\n• Resistance Management: Alternate with Acetamiprid 20% SP @ 0.3g/L after 10 days",
            treatmentHindi = "• मुख्य रासायनिक दवा: सफेद मक्खी नियंत्रण हेतु इमिडाक्लोप्रिड 17.8% SL (कॉन्फिडोर @ 0.5 मिली/लीटर) या डायफेंथियूरॉन 50% WP @ 1.2 ग्राम/लीटर\n• 15 लीटर टंकी खुराक: 7-8 मिली कॉन्फिडोर अथवा 18 ग्राम डायफेंथियूरॉन प्रति 15L पंप\n• प्रति एकड़ मात्रा: 100 मिली दवा (200 लीटर पानी में, चिपको स्टीकर मिलाएं)\n• छिड़काव समय: सुबह या शाम पत्तियों की निचली सतह पर छिड़कें\n• दवा बदलना: 10 दिन बाद एसिटामिप्रिड 20% SP (0.3 ग्राम/लीटर) से स्प्रे करें",
            prevention = "• Organic Remedy: Spray 5% Neem oil (5ml/L) regularly as insect repellent\n• Crop Sanitation: Install 15-20 yellow sticky traps per acre, rogue out stunted viral plants\n• Harvest Safety (PHI): Maintain 5 days waiting period before tomato harvest"
        ),
        DiseaseInfo(
            classId = "Tomato___Bacterial_spot",
            cropName = "Tomato",
            cropHindi = "टमाटर",
            diseaseName = "Bacterial Spot",
            diseaseHindi = "जीवाणु धब्बा रोग",
            severity = "Moderate",
            isHealthy = false,
            symptoms = "Small, dark greasy spots with yellow halos on leaves and fruits.",
            symptomsHindi = "पत्तियों पर छोटे, काले तैलीय धब्बे जिनके चारों ओर पीला घेरा होता है।",
            treatment = "• Recommended Chemical: Copper Hydroxide 53.8% DF @ 2g/L mixed with Streptocycline @ 1g/10L\n• 15-Liter Knapsack Tank Dose: 30g Copper Hydroxide + 1.5g Streptocycline per 15L spray pump\n• Per Acre Field Dose: 400g Copper + 20g Streptocycline in 200L water per acre\n• Spray Timing: Spray in cool evening hours\n• Resistance Management: Repeat after 10-12 days if wet weather persists",
            treatmentHindi = "• मुख्य रासायनिक दवा: कॉपर हाइड्रॉक्साइड 53.8% DF (2 ग्राम/लीटर) + स्ट्रेप्टोसाइक्लिन (1 ग्राम प्रति 10 लीटर)\n• 15 लीटर टंकी खुराक: 30 ग्राम कॉपर + 1.5 ग्राम स्ट्रेप्टोसाइक्लिन प्रति 15L पंप\n• प्रति एकड़ मात्रा: 400 ग्राम कॉपर + 20 ग्राम स्ट्रेप्टोसाइक्लिन (200 लीटर पानी में)\n• छिड़काव समय: शाम के ठंडे समय पत्तियों पर छिड़कें\n• दवा बदलना: बारिश का मौसम जारी रहने पर 10-12 दिन बाद दोबारा छिड़कें",
            prevention = "• Organic Remedy: Spray cow dung slurry filtrate or Pseudomonas fluorescens @ 5g/L\n• Crop Sanitation: Avoid overhead sprinkler watering, disinfect pruning shears between rows\n• Harvest Safety (PHI): Maintain 7 days waiting period before picking"
        ),
        DiseaseInfo(
            classId = "Tomato___healthy",
            cropName = "Tomato",
            cropHindi = "टमाटर",
            diseaseName = "Healthy Tomato Plant",
            diseaseHindi = "स्वस्थ टमाटर का पौधा",
            severity = "None",
            isHealthy = true,
            symptoms = "Vibrant deep green foliage, robust stems, healthy yellow blossoms and plump fruits.",
            symptomsHindi = "पत्तियां पूरी तरह हरी और स्वस्थ हैं, अच्छे फल और फूल।",
            treatment = "Stand is healthy. Apply 19:19:19 NPK water-soluble fertilizer for fruit sizing.",
            treatmentHindi = "किसी उपचार की आवश्यकता नहीं। समय पर सिंचाई और खाद जारी रखें।",
            prevention = "Regular staking and sucker pruning for good ventilation."
        ),

        // ==========================================
        // 🥔 9. POTATO (आलू)
        // ==========================================
        DiseaseInfo(
            classId = "Potato___Late_blight",
            cropName = "Potato",
            cropHindi = "आलू",
            diseaseName = "Late Blight",
            diseaseHindi = "पछेती झुलसा",
            severity = "Severe",
            isHealthy = false,
            symptoms = "Water-soaked lesions on leaf margins rapidly turning black, white fungal growth underneath in cool foggy weather.",
            symptomsHindi = "पत्तियों के किनारों पर काले पानीदार धब्बे, कोहरे में सफेद फफूंद।",
            treatment = "• Recommended Chemical: Cymoxanil 8% + Mancozeb 64% WP (Curzate @ 2.5g/L) or Dimethomorph 50% WP (1g/L)\n• 15-Liter Knapsack Tank Dose: 35-40g Curzate OR 15g Dimethomorph per 15L spray pump\n• Per Acre Field Dose: 600-750g in 200L water per acre\n• Spray Timing: Urgent - spray within 24 hours of cool foggy/cloudy morning\n• Resistance Management: Alternate with Fenamidone 10% + Mancozeb 50% WDG (Sectin @ 2g/L) after 7-10 days",
            treatmentHindi = "• मुख्य रासायनिक दवा: साइमोक्सानिल 8% + मैनकोजेब 64% WP (कर्जेट @ 2.5 ग्राम/लीटर) या डाइमथोमोर्फ 50% WP (1 ग्राम/लीटर)\n• 15 लीटर टंकी खुराक: 35-40 ग्राम कर्जेक्ट अथवा 15 ग्राम डाइमथोमोर्फ प्रति 15L पंप\n• प्रति एकड़ मात्रा: 600-750 ग्राम दवा (200 लीटर पानी में)\n• छिड़काव समय: अति आवश्यक - कोहरे या बादल वाले मौसम के 24 घंटे के अंदर छिड़कें\n• दवा बदलना: 7-10 दिन बाद फेनामिडोन + मैनकोजेब (सेक्टिन @ 2 ग्राम/लीटर) डालें",
            prevention = "• Organic Remedy: Spray 5% Neem oil (5ml/L) or Trichoderma viride preventive spray\n• Crop Sanitation: Cut and destroy haulms (potato foliage) 10-12 days before tuber digging\n• Harvest Safety (PHI): Maintain 10 days waiting period before harvesting potatoes"
        ),
        DiseaseInfo(
            classId = "Potato___Early_blight",
            cropName = "Potato",
            cropHindi = "आलू",
            diseaseName = "Early Blight",
            diseaseHindi = "अगेती झुलसा",
            severity = "Moderate",
            isHealthy = false,
            symptoms = "Dark brown circular spots with concentric target-board rings on older foliage.",
            symptomsHindi = "पुरानी पत्तियों पर भूरे छल्लेदार धब्बे।",
            treatment = "• Recommended Chemical: Mancozeb 75% WP @ 2.5g/L or Propineb 70% WP @ 2g/L (Antracol)\n• 15-Liter Knapsack Tank Dose: 35-40g per 15-liter spray pump\n• Per Acre Field Dose: 700-800g in 200L water per acre\n• Spray Timing: Calm morning (7:00-9:30 AM) or cool evening (4:00-6:00 PM)\n• Resistance Management: Alternate with Difenoconazole 25% EC (Score @ 0.5ml/L) after 10 days",
            treatmentHindi = "• मुख्य रासायनिक दवा: मैनकोजेब 75% WP (2.5 ग्राम/लीटर) या प्रोपिनेब 70% WP (एंट्राकोल @ 2 ग्राम/लीटर)\n• 15 लीटर टंकी खुराक: 35-40 ग्राम दवा प्रति 15L पंप\n• प्रति एकड़ मात्रा: 700-800 ग्राम दवा (200 लीटर पानी में)\n• छिड़काव समय: शांत सुबह (7:00-9:30 AM) या ठंडी शाम (4:00-6:00 PM)\n• दवा बदलना: 10 दिन बाद डिफेनोकोनाजोल (स्कोर @ 0.5 मिली/लीटर) डालें",
            prevention = "• Organic Remedy: Spray 5% Neem oil (5ml/L) or Trichoderma viride foliar spray\n• Crop Sanitation: Earthing up (mitti chadhana) to protect tubers from fungal spore wash-down\n• Harvest Safety (PHI): Maintain 10-14 days waiting period before digging tubers"
        ),
        DiseaseInfo(
            classId = "Potato___healthy",
            cropName = "Potato",
            cropHindi = "आलू",
            diseaseName = "Healthy Potato Crop",
            diseaseHindi = "स्वस्थ आलू की फसल",
            severity = "None",
            isHealthy = true,
            symptoms = "Deep-green bushy plants with clean leaves and healthy stolon development.",
            symptomsHindi = "पत्तियां पूरी तरह हरी और रोगमुक्त हैं, स्वस्थ कंद।",
            treatment = "Crop is healthy. Keep soil consistently moist during tuber bulking.",
            treatmentHindi = "उचित सिंचाई और मिट्टी चढ़ाने का कार्य जारी रखें।",
            prevention = "Stop irrigation 10 days before harvest for skin hardening."
        ),

        // ==========================================
        // 🌽 10. CORN / MAIZE (मक्का)
        // ==========================================
        DiseaseInfo(
            classId = "Corn_(maize)___Northern_Leaf_Blight",
            cropName = "Corn",
            cropHindi = "मक्का",
            diseaseName = "Northern Leaf Blight",
            diseaseHindi = "उत्तरी पत्ती झुलसा (टर्सिकम ब्लाइट)",
            severity = "Severe",
            isHealthy = false,
            symptoms = "Long elliptical grayish-green or tan cigar-shaped lesions on leaves.",
            symptomsHindi = "पत्तियों पर लंबे सिगार के आकार के भूरे-ग्रे रंग के बड़े धब्बे।",
            treatment = "Spray Mancozeb 75 WP @ 2.5 g/L or Azoxystrobin @ 1 ml/L.",
            treatmentHindi = "मैनकोजेब 75 WP (2.5 ग्राम/लीटर) का छिड़काव करें।",
            prevention = "Plant tolerant hybrid seeds, plow down crop debris."
        ),
        DiseaseInfo(
            classId = "Corn_(maize)___Common_rust_",
            cropName = "Corn",
            cropHindi = "मक्का",
            diseaseName = "Common Rust",
            diseaseHindi = "सामान्य मक्का रतुआ",
            severity = "Moderate",
            isHealthy = false,
            symptoms = "Golden-brown to cinnamon-brown pustules on both upper and lower leaf surfaces.",
            symptomsHindi = "पत्तियों की दोनों सतहों पर भूरे-लाल रंग के उभरे हुए दाने।",
            treatment = "Spray Mancozeb 75 WP @ 2 g/L at first appearance.",
            treatmentHindi = "मैनकोजेब 75 WP (2 ग्राम/लीटर) का छिड़काव करें।",
            prevention = "Grow rust-resistant hybrids, avoid overhead irrigation."
        ),
        DiseaseInfo(
            classId = "Corn_(maize)___healthy",
            cropName = "Corn",
            cropHindi = "मक्का",
            diseaseName = "Healthy Corn Stand",
            diseaseHindi = "स्वस्थ मक्का की फसल",
            severity = "None",
            isHealthy = true,
            symptoms = "Broad sturdy deep-green leaves, robust tassels, well-filled cobs.",
            symptomsHindi = "मजबूत चौड़ी पत्तियां, स्वस्थ भुट्टे और दाने।",
            treatment = "Crop is healthy. Top-dress with urea before tasseling stage.",
            treatmentHindi = "कोई उपचार आवश्यक नहीं। नर मंजरी निकलने से पहले यूरिया दें।",
            prevention = "Scout for Fall Armyworm (सैनिक कीट) during vegetative stage."
        ),

        // ==========================================
        // 🫘 11. SOYBEAN (सोयाबीन)
        // ==========================================
        DiseaseInfo(
            classId = "Soybean___Rust",
            cropName = "Soybean",
            cropHindi = "सोयाबीन",
            diseaseName = "Soybean Rust",
            diseaseHindi = "सोयाबीन रतुआ रोग",
            severity = "Severe",
            isHealthy = false,
            symptoms = "Tiny tan-to-dark brown polygonal lesions on lower leaves with volcano-like pustules.",
            symptomsHindi = "पत्तियों पर छोटे गहरे भूरे कोणीय धब्बे जिनके अंदर से बारीक पाउडर निकलता है।",
            treatment = "Spray Hexaconazole 5 EC @ 1.5 ml/L or Propiconazole 25 EC @ 1 ml/L.",
            treatmentHindi = "हेक्साकोनाज़ोल 5 EC (1.5 मिली/लीटर) या प्रोपिकोनाज़ोल का छिड़काव करें।",
            prevention = "Early planting, avoid late-maturing susceptible varieties."
        ),
        DiseaseInfo(
            classId = "Soybean___healthy",
            cropName = "Soybean",
            cropHindi = "सोयाबीन",
            diseaseName = "Healthy Soybean",
            diseaseHindi = "स्वस्थ सोयाबीन",
            severity = "None",
            isHealthy = true,
            symptoms = "Trifoliate lush green foliage, profuse pod clusters, nodules on roots.",
            symptomsHindi = "स्वस्थ हरी पत्तियां, फलियों का भरपूर गुच्छा और जड़ों में राइजोबियम गांठे।",
            treatment = "Stand is disease-free. Maintain weed-free conditions during first 45 days.",
            treatmentHindi = "पौधे स्वस्थ हैं। पहले 45 दिनों तक खेत को खरपतवार मुक्त रखें।",
            prevention = "Inoculate seeds with Rhizobium culture before sowing."
        ),

        // ==========================================
        // 🍊 12. CITRUS / ORANGE / LEMON (नींबू / संतरा)
        // ==========================================
        DiseaseInfo(
            classId = "Citrus___Canker",
            cropName = "Citrus",
            cropHindi = "नींबू / संतरा",
            diseaseName = "Citrus Canker",
            diseaseHindi = "नींबू का कैंकर रोग (खुरंड)",
            severity = "Severe",
            isHealthy = false,
            symptoms = "Raised corky brown crater-like lesions with yellow halos on leaves, twigs, and fruit rinds.",
            symptomsHindi = "पत्तियों, टहनियों और फलों पर उभरे हुए खुरदरे भूरे रंग के छाले जैसे धब्बे जिनके चारों ओर पीला घेरा होता है।",
            treatment = "Spray Streptocycline @ 1 g per 10 L + Copper Oxychloride 50 WP @ 30 g per 10 L.",
            treatmentHindi = "स्ट्रेप्टोसाइक्लिन (1 ग्राम/10L) + कॉपर ऑक्सीक्लोराइड (30 ग्राम/10L) का नई पत्तियों पर छिड़काव करें।",
            prevention = "Prune infected twigs before monsoon, control leaf miner insects."
        ),
        DiseaseInfo(
            classId = "Orange___Haunglongbing_(Citrus_greening)",
            cropName = "Citrus",
            cropHindi = "नींबू / संतरा",
            diseaseName = "Citrus Greening (Huanglongbing)",
            diseaseHindi = "सिट्रस ग्रीनिंग रोग",
            severity = "Severe",
            isHealthy = false,
            symptoms = "Asymmetrical blotchy leaf mottle, yellow veins, small lopsided bitter green fruits.",
            symptomsHindi = "पत्तियों पर अनियमित पीलापन, नसें पीली होना, छोटे टेढ़े-मेढ़े खट्टे-कड़वे फल।",
            treatment = "Control Asian Citrus Psyllid vector with Imidacloprid 17.8 SL @ 0.5 ml/L.",
            treatmentHindi = "साइला कीट नियंत्रण हेतु इमिडाक्लोप्रिड या थायमेथॉक्सम का छिड़काव करें।",
            prevention = "Plant certified disease-free nursery saplings, remove severely declining trees."
        ),
        DiseaseInfo(
            classId = "Citrus___healthy",
            cropName = "Citrus",
            cropHindi = "नींबू / संतरा",
            diseaseName = "Healthy Citrus Tree",
            diseaseHindi = "स्वस्थ नींबू/संतरा का पेड़",
            severity = "None",
            isHealthy = true,
            symptoms = "Deep glossy green leathery leaves, sweet fragrant blossom, clean shiny rinds.",
            symptomsHindi = "चमकदार स्वस्थ पत्तियां, भरपूर खुशबूदार फूल और चमकदार रसदार फल।",
            treatment = "Orchard is healthy. Apply Zinc Sulphate + Ferrous Sulphate foliar spray for flush greening.",
            treatmentHindi = "पेड़ स्वस्थ हैं। फल वृद्धि हेतु सूक्ष्म पोषक तत्वों का हल्का छिड़काव करें।",
            prevention = "Maintain ring basin irrigation to prevent stem collar rot."
        ),

        // ==========================================
        // 🍇 13. GRAPE (अंगूर)
        // ==========================================
        DiseaseInfo(
            classId = "Grape___Downy_mildew",
            cropName = "Grape",
            cropHindi = "अंगूर",
            diseaseName = "Grape Downy Mildew",
            diseaseHindi = "डाउनी मिल्ड्यू (मृदुरोमिल आसिता)",
            severity = "Severe",
            isHealthy = false,
            symptoms = "Yellowish translucent 'oil spots' on upper leaf surface, dense white downy cottony mold underneath.",
            symptomsHindi = "पत्ती की ऊपरी सतह पर तेल जैसे पीले धब्बे, निचली सतह पर सफेद कपास जैसी फफूंद।",
            treatment = "Spray Metalaxyl 8% + Mancozeb 64% (2.5 g/L) or Dimethomorph (1 g/L).",
            treatmentHindi = "रिडोमिल गोल्ड (2.5 ग्राम/लीटर) या डाइमथोमोर्फ (1 ग्राम/लीटर) का तुरंत छिड़काव करें।",
            prevention = "Canopy pruning for sunlight and aeration, protective spray before rain."
        ),
        DiseaseInfo(
            classId = "Grape___Black_rot",
            cropName = "Grape",
            cropHindi = "अंगूर",
            diseaseName = "Grape Black Rot",
            diseaseHindi = "काला सड़न रोग",
            severity = "Severe",
            isHealthy = false,
            symptoms = "Small reddish-brown circular leaf spots, berries turn black, shrivel, and mummify into hard black raisins.",
            symptomsHindi = "पत्तियों पर लाल-भूरे गोल धब्बे, अंगूर के दाने काले पड़कर सिकुड़ना।",
            treatment = "Spray Mancozeb 75 WP @ 2.5 g/L or Myclobutanil 10 WP @ 0.5 g/L.",
            treatmentHindi = "मैनकोजेब 75 WP (2.5 ग्राम/लीटर) का छिड़काव करें।",
            prevention = "Remove mummified berries from vines during winter pruning."
        ),
        DiseaseInfo(
            classId = "Grape___healthy",
            cropName = "Grape",
            cropHindi = "अंगूर",
            diseaseName = "Healthy Grapevine",
            diseaseHindi = "स्वस्थ अंगूर की बेल",
            severity = "None",
            isHealthy = true,
            symptoms = "Broad clean fan-shaped leaves, robust canes, tight healthy berry clusters.",
            symptomsHindi = "स्वस्थ चौड़ी पत्तियां और अंगूरों के खूबसूरत बड़े गुच्छे।",
            treatment = "Vines are healthy. Provide balanced potassium-magnesium nutrition for berry sweetness.",
            treatmentHindi = "बेल स्वस्थ है। मिठास बढ़ाने के लिए पोटाश और सूक्ष्म पोषक दें।",
            prevention = "Regular canopy management to avoid humidity build-up."
        ),

        // ==========================================
        // 🍎 14. APPLE (सेब)
        // ==========================================
        DiseaseInfo(
            classId = "Apple___Apple_scab",
            cropName = "Apple",
            cropHindi = "सेब",
            diseaseName = "Apple Scab",
            diseaseHindi = "सेब का स्कैब (दाग रोग)",
            severity = "Severe",
            isHealthy = false,
            symptoms = "Olive-green to velvety dark circular spots on leaves and fruit, causing leaf drop and cracked scabbed fruit.",
            symptomsHindi = "पत्तियों और फलों पर गहरे जैतून-हरे मखमली धब्बे, फलों की त्वचा फटना।",
            treatment = "Apply Difenoconazole 25 EC @ 0.5 ml/L or Captan 50 WP @ 2.5 g/L at bud-break and petal-fall.",
            treatmentHindi = "डिफेनोकोनाज़ोल (0.5 मिली/लीटर) या कैप्टन (2.5 ग्राम/लीटर) का छिड़काव करें।",
            prevention = "Rake and burn fallen orchard leaves in autumn, prune tree canopy."
        ),
        DiseaseInfo(
            classId = "Apple___healthy",
            cropName = "Apple",
            cropHindi = "सेब",
            diseaseName = "Healthy Apple Tree",
            diseaseHindi = "स्वस्थ सेब का पेड़",
            severity = "None",
            isHealthy = true,
            symptoms = "Lush green foliage, vigorous shoots, clean unblemished red/green fruit development.",
            symptomsHindi = "स्वस्थ पत्तियां और बेदाग रसीले सेब।",
            treatment = "Orchard is healthy. Apply calcium chloride spray during fruit sizing to prevent bitter pit.",
            treatmentHindi = "पेड़ स्वस्थ हैं। फलों की चमक और गुणवत्ता के लिए कैल्शियम का छिड़काव करें।",
            prevention = "Ensure winter dormant copper spray for overall orchard sanitation."
        ),

        // ==========================================
        // 🌾 15. RICE / PADDY (धान / चावल)
        // ==========================================
        DiseaseInfo(
            classId = "Rice___Leaf_blast",
            cropName = "Rice",
            cropHindi = "धान",
            diseaseName = "Rice Leaf Blast",
            diseaseHindi = "धान का झोंका रोग (ब्लास्ट)",
            severity = "Severe",
            isHealthy = false,
            symptoms = "Spindle-shaped or diamond lesions with grey-white centers and brownish margins on leaves, causing rapid drying.",
            symptomsHindi = "पत्तियों पर आंख या नाव के आकार के धब्बे जिनके बीच का भाग राख जैसा सफेद और किनारे भूरे होते हैं।",
            treatment = "Spray Tricyclazole 75 WP @ 0.6 g/L or Kasugamycin 3% SL @ 2.5 ml/L.",
            treatmentHindi = "ट्राइसाइक्लाजोल 75 WP (0.6 ग्राम/लीटर) या कासुगामाइसिन (2.5 मिली/लीटर) का छिड़काव करें।",
            prevention = "Avoid excess nitrogen fertilizer, use resistant seeds (Pusa 1460, IR 64)."
        ),
        DiseaseInfo(
            classId = "Rice___Bacterial_leaf_blight",
            cropName = "Rice",
            cropHindi = "धान",
            diseaseName = "Bacterial Leaf Blight",
            diseaseHindi = "जीवाणु पत्ती झुलसा रोग (BLB)",
            severity = "Severe",
            isHealthy = false,
            symptoms = "Water-soaked stripes along leaf margins turning yellow-white and wavy, milky bacterial ooze under humid morning.",
            symptomsHindi = "पत्तियों के किनारों से शुरू होकर अंदर की ओर बढ़ती पीली-सफेद लहरदार धारियां, पत्तियां सूखना।",
            treatment = "Spray Copper Oxychloride 50 WP @ 2.5 g/L + Streptocycline @ 0.1 g/L.",
            treatmentHindi = "कॉपर ऑक्सीक्लोराइड (2.5 ग्राम/लीटर) + स्ट्रेप्टोसाइक्लिन (1 ग्राम प्रति 10 लीटर) का छिड़काव करें।",
            prevention = "Drain standing water from infected fields temporarily to restrict bacterial spread."
        ),
        DiseaseInfo(
            classId = "Rice___Brown_spot",
            cropName = "Rice",
            cropHindi = "धान",
            diseaseName = "Rice Brown Spot",
            diseaseHindi = "धान का भूरा धब्बा रोग",
            severity = "Moderate",
            isHealthy = false,
            symptoms = "Small circular to oval dark brown sesame-seed shaped spots on leaves and glumes with yellow halos.",
            symptomsHindi = "पत्तियों पर तिल के आकार के गोल-अंडाकार भूरे धब्बे, जिनके चारों ओर पीला छल्ला होता है।",
            treatment = "Spray Propiconazole 25 EC @ 1 ml/L or Mancozeb 75 WP @ 2 g/L.",
            treatmentHindi = "प्रोपिकोनाज़ोल 25 EC (1 मिली/लीटर) या मैनकोजेब (2 ग्राम/लीटर) का छिड़काव करें।",
            prevention = "Soil testing and balanced potassium (potash) and zinc application."
        ),
        DiseaseInfo(
            classId = "Rice___Sheath_blight",
            cropName = "Rice",
            cropHindi = "धान",
            diseaseName = "Rice Sheath Blight",
            diseaseHindi = "शीथ ब्लाइट (तना झुलसा)",
            severity = "Severe",
            isHealthy = false,
            symptoms = "Oval or snake-skin greenish-grey water-soaked spots just above water line on leaf sheath, spreading upward.",
            symptomsHindi = "पानी की सतह के पास तने के खोल पर सांप की केंचुली जैसे हरे-भूरे धब्बे जो ऊपर की ओर फैलते हैं।",
            treatment = "Spray Hexaconazole 5 SC @ 2 ml/L or Validamycin 3L @ 2 ml/L directed towards plant base.",
            treatmentHindi = "हेक्साकोनाज़ोल 5 SC (2 मिली/लीटर) या वैलिडामाइसिन (2 मिली/लीटर) तने के निचले भाग पर छिड़कें।",
            prevention = "Avoid overly dense planting and excessive urea."
        ),
        DiseaseInfo(
            classId = "Rice___Rice_hispa",
            cropName = "Rice",
            cropHindi = "धान",
            diseaseName = "Rice Hispa",
            diseaseHindi = "धान का हिस्पा कीट",
            severity = "Moderate",
            isHealthy = false,
            symptoms = "White parallel streaks on leaves scraped by adult spiny black beetles, leaving transparent papery leaf tips.",
            symptomsHindi = "काले कांटेदार भृंग द्वारा पत्तियों की हरी सतह खुरच कर बनाई गई सफेद समानांतर धारियां।",
            treatment = "Spray Chlorpyrifos 20 EC @ 2 ml/L or Quinalphos 25 EC @ 2 ml/L.",
            treatmentHindi = "क्लोरपायरीफॉस 20 EC (2 मिली/लीटर) या क्विनालफॉस (2 मिली/लीटर) का छिड़काव करें।",
            prevention = "Clip leaf tips before transplanting to destroy egg colonies."
        ),
        DiseaseInfo(
            classId = "Rice___Leaf_scald",
            cropName = "Rice",
            cropHindi = "धान",
            diseaseName = "Rice Leaf Scald",
            diseaseHindi = "धान का पत्ती झुलसन रोग",
            severity = "Moderate",
            isHealthy = false,
            symptoms = "Zonate banding pattern on leaf tips with alternating light and dark brown chevrons.",
            symptomsHindi = "पत्ती के ऊपरी हिस्से पर हल्के और गहरे भूरे रंग की पट्टियां बनना।",
            treatment = "Spray Carbendazim 50 WP @ 1 g/L or Azoxystrobin @ 1 ml/L.",
            treatmentHindi = "कार्बेन्डाजिम 50 WP (1 ग्राम/लीटर) का छिड़काव करें।",
            prevention = "Use treated seeds and balanced fertilizers."
        ),
        DiseaseInfo(
            classId = "Rice___Narrow_brown_leaf_spot",
            cropName = "Rice",
            cropHindi = "धान",
            diseaseName = "Narrow Brown Leaf Spot",
            diseaseHindi = "संकीर्ण भूरा पत्ती धब्बा",
            severity = "Moderate",
            isHealthy = false,
            symptoms = "Short, narrow, linear reddish-brown lesions parallel to leaf veins.",
            symptomsHindi = "पत्तियों की शिराओं के समानांतर पतली, लंबी लाल-भूरे रंग की रेखाएं।",
            treatment = "Apply Propiconazole 25 EC @ 1 ml/L at boot stage.",
            treatmentHindi = "प्रोपिकोनाज़ोल 25 EC (1 मिली/लीटर) का छिड़काव करें।",
            prevention = "Crop rotation and potassium soil enrichment."
        ),
        DiseaseInfo(
            classId = "Rice___healthy",
            cropName = "Rice",
            cropHindi = "धान",
            diseaseName = "Healthy Rice Crop",
            diseaseHindi = "स्वस्थ धान की फसल",
            severity = "None",
            isHealthy = true,
            symptoms = "Vibrant emerald green erect tillers, uniform canopy, healthy panicle formation without foliar spotting.",
            symptomsHindi = "स्वस्थ हरी पत्तियां और भरपूर बालियां।",
            treatment = "Crop is completely healthy. Maintain 2-3 inches standing water during grain filling stage.",
            treatmentHindi = "फसल पूरी तरह स्वस्थ है। दाना भरते समय खेत में नमी बनाए रखें।",
            prevention = "Maintain clean irrigation canals and periodic monitoring."
        ),

        // ==========================================
        // 🌾 16. WHEAT (गेहूं)
        // ==========================================
        DiseaseInfo(
            classId = "Wheat___Leaf_rust",
            cropName = "Wheat",
            cropHindi = "गेहूं",
            diseaseName = "Wheat Leaf Rust (Brown Rust)",
            diseaseHindi = "गेहूं का भूरा रतुआ (ताम्र रोग)",
            severity = "Severe",
            isHealthy = false,
            symptoms = "Small round to oval orange-brown powdery pustules scattered randomly across upper leaf surfaces.",
            symptomsHindi = "पत्तियों की ऊपरी सतह पर नारंगी-भूरे रंग के बिखरे हुए पाउडर जैसे फफोले (रतुआ)।",
            treatment = "Spray Propiconazole 25 EC (Tilt) @ 1 ml/L or Tebuconazole 250 EC @ 1 ml/L.",
            treatmentHindi = "प्रोपिकोनाज़ोल 25 EC (1 मिली/लीटर) या टेबुकोनाज़ोल का तुरंत छिड़काव करें।",
            prevention = "Sow rust-resistant varieties like HD 2967, DBW 187, PBW 550."
        ),
        DiseaseInfo(
            classId = "Wheat___Stripe_rust",
            cropName = "Wheat",
            cropHindi = "गेहूं",
            diseaseName = "Stripe Rust (Yellow Rust)",
            diseaseHindi = "पीला रतुआ (हल्दी रोग)",
            severity = "Severe",
            isHealthy = false,
            symptoms = "Bright yellow linear stripes of powdery pustules arranged in parallel rows like yellow stitching.",
            symptomsHindi = "पत्तियों पर हल्दी जैसे पीले रंग की लंबी समानांतर धारियों में पाउडर जैसे फफोले।",
            treatment = "Immediate spray of Propiconazole 25 EC @ 1 ml/L in 200 L water per acre.",
            treatmentHindi = "तुरंत प्रोपिकोनाज़ोल 25 EC (1 मिली/लीटर) का 200 लीटर पानी में घोल बनाकर छिड़काव करें।",
            prevention = "Monitor fields early in December-January during cool humid weather."
        ),
        DiseaseInfo(
            classId = "Wheat___Stem_rust",
            cropName = "Wheat",
            cropHindi = "गेहूं",
            diseaseName = "Stem Rust (Black Rust)",
            diseaseHindi = "काला रतुआ (तना रतुआ)",
            severity = "Severe",
            isHealthy = false,
            symptoms = "Dark reddish-brown to black elongated pustules rupturing the epidermis on stems and leaf sheaths.",
            symptomsHindi = "तने और पत्तियों पर गहरे लाल-काले रंग के उभरे हुए फफोले जो छूने पर काले पाउडर जैसे छूटते हैं।",
            treatment = "Spray Mancozeb 75 WP @ 2.5 g/L or Propiconazole @ 1 ml/L.",
            treatmentHindi = "मैनकोजेब (2.5 ग्राम/लीटर) या प्रोपिकोनाज़ोल का छिड़काव करें।",
            prevention = "Avoid late sowing, destroy alternate host barberry bushes."
        ),
        DiseaseInfo(
            classId = "Wheat___Powdery_mildew",
            cropName = "Wheat",
            cropHindi = "गेहूं",
            diseaseName = "Wheat Powdery Mildew",
            diseaseHindi = "गेहूं का चूर्णिल आसिता (सफेद फफूंद)",
            severity = "Moderate",
            isHealthy = false,
            symptoms = "Fluffy white to greyish cottony fungal patches on leaves and lower stems, turning dull grey with black specks.",
            symptomsHindi = "पत्तियों और तनों पर सफेद पाउडर या रूई जैसी फफूंद की परत जमना।",
            treatment = "Spray Wettable Sulphur 80 WP @ 3 g/L or Hexaconazole 5 SC @ 2 ml/L.",
            treatmentHindi = "घुलनशील गंधक (3 ग्राम/लीटर) या हेक्साकोनाज़ोल (2 मिली/लीटर) का छिड़काव करें।",
            prevention = "Avoid dense seed rate and over-irrigation in winter."
        ),
        DiseaseInfo(
            classId = "Wheat___Leaf_blight",
            cropName = "Wheat",
            cropHindi = "गेहूं",
            diseaseName = "Wheat Leaf Blight",
            diseaseHindi = "गेहूं का पत्ती झुलसा रोग",
            severity = "Moderate",
            isHealthy = false,
            symptoms = "Oval to irregular brown necrotic spots on leaves, coalescing into large dead blotches with yellow borders.",
            symptomsHindi = "पत्तियों पर अंडाकार भूरे धब्बे जो आपस में मिलकर पूरी पत्ती को झुलसा देते हैं।",
            treatment = "Spray Azoxystrobin + Difenoconazole @ 1 ml/L or Mancozeb @ 2.5 g/L.",
            treatmentHindi = "एज़ोक्सीस्ट्रोबिन + डिफेनोकोनाज़ोल (1 मिली/लीटर) का छिड़काव करें।",
            prevention = "Seed treatment with Thiram or Carbendazim @ 2.5 g/kg seed."
        ),
        DiseaseInfo(
            classId = "Wheat___Blast",
            cropName = "Wheat",
            cropHindi = "गेहूं",
            diseaseName = "Wheat Blast",
            diseaseHindi = "गेहूं का ब्लास्ट रोग",
            severity = "Severe",
            isHealthy = false,
            symptoms = "Bleaching of spikes and heads while canopy stays green, black/grey lesions on rachis node.",
            symptomsHindi = "पौधा हरा रहने के बावजूद गेहूं की बालियों का अचानक सफेद पड़ना और दाने न बनना।",
            treatment = "Spray Tebuconazole + Trifloxystrobin @ 1 g/L at heading stage.",
            treatmentHindi = "टेबुकोनाज़ोल + ट्राइफ्लोक्सीस्ट्रोबिन (1 ग्राम/लीटर) का छिड़काव करें।",
            prevention = "Use certified blast-free seeds, avoid field moisture stagnation."
        ),
        DiseaseInfo(
            classId = "Wheat___Wheat_blast",
            cropName = "Wheat",
            cropHindi = "गेहूं",
            diseaseName = "Wheat Blast",
            diseaseHindi = "गेहूं का ब्लास्ट रोग",
            severity = "Severe",
            isHealthy = false,
            symptoms = "Premature whitening of head and silvery bleached spikes with shriveled grains.",
            symptomsHindi = "बालियों का समय से पहले सूखकर सफेद होना और दानों का सिकुड़ना।",
            treatment = "Spray Tebuconazole + Trifloxystrobin @ 1 g/L at heading stage.",
            treatmentHindi = "टेबुकोनाज़ोल + ट्राइफ्लोक्सीस्ट्रोबिन (1 ग्राम/लीटर) का छिड़काव करें।",
            prevention = "Use certified blast-free seeds, avoid field moisture stagnation."
        ),
        DiseaseInfo(
            classId = "Wheat___Fusarium_foot_rot",
            cropName = "Wheat",
            cropHindi = "गेहूं",
            diseaseName = "Fusarium Foot Rot",
            diseaseHindi = "फ्यूजेरियम पाद गलन रोग",
            severity = "Severe",
            isHealthy = false,
            symptoms = "Dark brown necrosis at plant crown and lower stems, pinkish mycelium in humid conditions, lodging.",
            symptomsHindi = "जड़ों और तने के जोड़ पर गहरा भूरा सड़न, पौधे का आसानी से उखड़ जाना।",
            treatment = "Drench with Carbendazim 50 WP @ 2 g/L around plant roots.",
            treatmentHindi = "कार्बेन्डाजिम (2 ग्राम/लीटर) की जड़ों के पास ड्रेंचिंग करें।",
            prevention = "Improve soil drainage and avoid repetitive wheat mono-cropping."
        ),
        DiseaseInfo(
            classId = "Wheat___Black_point",
            cropName = "Wheat",
            cropHindi = "गेहूं",
            diseaseName = "Black Point Disease",
            diseaseHindi = "गेहूं का काला दाना रोग",
            severity = "Moderate",
            isHealthy = false,
            symptoms = "Dark brown to black discoloration at the embryo end of wheat kernels.",
            symptomsHindi = "गेहूं के दानों के भ्रूण वाले सिरे पर गहरा काला या भूरा धब्बा पड़ना।",
            treatment = "Spray Mancozeb 75 WP @ 2.5 g/L at grain milk stage.",
            treatmentHindi = "दूधिया अवस्था में मैनकोजेब (2.5 ग्राम/लीटर) का छिड़काव करें।",
            prevention = "Harvest immediately upon maturity to avoid rain exposure on ripe spikes."
        ),
        DiseaseInfo(
            classId = "Wheat___healthy",
            cropName = "Wheat",
            cropHindi = "गेहूं",
            diseaseName = "Healthy Wheat Crop",
            diseaseHindi = "स्वस्थ गेहूं की फसल",
            severity = "None",
            isHealthy = true,
            symptoms = "Stout green tillers, clean upright flag leaves, plump developing grain heads without pustules.",
            symptomsHindi = "स्वस्थ हरी पत्तियां और दानों से भरी मजबूत बालियां।",
            treatment = "Crop is completely healthy. Provide timely irrigation at flowering and milk stages.",
            treatmentHindi = "फसल पूरी तरह स्वस्थ है। फूल आने और दाना बनते समय उचित सिंचाई करें।",
            prevention = "Regular monitoring and balanced NPK nutrition."
        ),
        DiseaseInfo(
            classId = "Wheat___healthy_leaf",
            cropName = "Wheat",
            cropHindi = "गेहूं",
            diseaseName = "Healthy Wheat Leaf",
            diseaseHindi = "स्वस्थ गेहूं की पत्ती",
            severity = "None",
            isHealthy = true,
            symptoms = "Clean unblemished linear green flag leaves without any rust pustules or yellow streaks.",
            symptomsHindi = "बेदाग हरी पत्तियां बिना किसी रतुआ या धब्बे के।",
            treatment = "Foliage is healthy. Maintain micronutrient spray (Zinc + Iron) if needed.",
            treatmentHindi = "पत्तियां स्वस्थ हैं। आवश्यकतानुसार जिंक और सूक्ष्म पोषक का स्प्रे करें।",
            prevention = "Maintain clean field boundaries and weed control."
        )
    )

    override suspend fun getDiseaseInfo(classId: String): Result<DiseaseInfo> = withContext(Dispatchers.IO) {
        val match = staticDiseases.find { it.classId.equals(classId, ignoreCase = true) }
            ?: staticDiseases.find { classId.contains(it.cropName, ignoreCase = true) }
            ?: staticDiseases[0]
        Result.success(match)
    }

    override suspend fun getAllDiseases(cropFilter: String?): Result<List<DiseaseInfo>> = withContext(Dispatchers.IO) {
        val filtered = if (cropFilter.isNullOrBlank() || cropFilter.equals("All", ignoreCase = true)) {
            staticDiseases
        } else {
            staticDiseases.filter {
                it.cropName.contains(cropFilter, ignoreCase = true) ||
                (it.cropHindi?.contains(cropFilter, ignoreCase = true) == true)
            }
        }
        Result.success(filtered)
    }

    override suspend fun askAiAdvisory(
        primaryClass: String,
        confidence: Float,
        query: String,
        language: String,
        base64Image: String?,
        base64Images: List<String>,
        conversationHistory: List<com.fasaldrishti.app.domain.model.ChatMessage>
    ): Result<String> = withContext(Dispatchers.IO) {
        // 1. PRIMARY: Try Google Gemini Multimodal AI
        if (geminiClient != null) {
            val geminiResult = geminiClient.getAgronomyAdvice(
                primaryClass = primaryClass,
                confidence = confidence,
                query = query,
                language = language,
                base64Image = base64Image,
                base64Images = base64Images,
                conversationHistory = conversationHistory
            )
            if (geminiResult.isSuccess) {
                return@withContext geminiResult
            }
        }

        // 2. SECONDARY / FALLBACK: NVIDIA NIM Vision / LLM API
        nvidiaClient.getAgronomyAdvice(
            primaryClass = primaryClass,
            confidence = confidence,
            query = query,
            language = language,
            conversationHistory = conversationHistory
        )
    }
}
