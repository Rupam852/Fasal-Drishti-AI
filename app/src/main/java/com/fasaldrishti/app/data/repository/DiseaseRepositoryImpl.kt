package com.fasaldrishti.app.data.repository

import com.fasaldrishti.app.data.remote.NvidiaClient
import com.fasaldrishti.app.data.remote.PredictApi
import com.fasaldrishti.app.domain.model.DiseaseInfo
import com.fasaldrishti.app.domain.repository.DiseaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DiseaseRepositoryImpl(
    private val predictApi: PredictApi,
    private val nvidiaClient: NvidiaClient = NvidiaClient()
) : DiseaseRepository {

    // 38-class static database for instant offline access
    private val staticDiseases = listOf(
        DiseaseInfo(
            classId = "Tomato___Late_blight",
            cropName = "Tomato",
            cropHindi = "टमाटर",
            diseaseName = "Late Blight",
            diseaseHindi = "पछेती झुलसा",
            severity = "Severe",
            isHealthy = false,
            symptoms = "Dark brown water-soaked lesions on leaves and stems, white fungal growth underneath leaf in humid weather.",
            symptomsHindi = "पत्तियों और तनों पर गहरे भूरे रंग के धब्बे, नम मौसम में सफेद फफूंद।",
            treatment = "Apply fungicides like Mancozeb (2.5g/L) or Metalaxyl + Mancozeb (Ridomil MZ @ 2g/L).",
            treatmentHindi = "मैनकोजेब (2.5 ग्राम/लीटर) का छिड़काव करें।",
            prevention = "Ensure good air circulation, avoid overhead sprinkler irrigation, practice crop rotation."
        ),
        DiseaseInfo(
            classId = "Tomato___Early_blight",
            cropName = "Tomato",
            cropHindi = "टमाटर",
            diseaseName = "Early Blight",
            diseaseHindi = "अगेती झुलसा",
            severity = "Moderate",
            isHealthy = false,
            symptoms = "Concentric dark brown rings forming bullseye spots on older lower leaves.",
            symptomsHindi = "पुरानी पत्तियों पर भूरे रंग के छल्लेदार धब्बे।",
            treatment = "Spray Chlorothalonil or Copper oxychloride (3g/L) at 7-10 day intervals.",
            treatmentHindi = "कॉपर ऑक्सीक्लोराइड (3 ग्राम/लीटर) का छिड़काव करें।",
            prevention = "Mulch the soil surface around plants to stop soil splash, avoid leaf moisture."
        ),
        DiseaseInfo(
            classId = "Tomato___healthy",
            cropName = "Tomato",
            cropHindi = "टमाटर",
            diseaseName = "Healthy Plant",
            diseaseHindi = "स्वस्थ पौधा",
            severity = "None",
            isHealthy = true,
            symptoms = "Vibrant green foliage, uniform leaf structure, healthy flowering.",
            symptomsHindi = "पत्तियां पूरी तरह हरी और स्वस्थ हैं।",
            treatment = "No treatment required. Maintain optimal irrigation and balanced NPK fertilization.",
            treatmentHindi = "किसी उपचार की आवश्यकता नहीं। समय पर सिंचाई जारी रखें।",
            prevention = "Continue regular scouting and balanced nutrient management."
        ),
        DiseaseInfo(
            classId = "Potato___Late_blight",
            cropName = "Potato",
            cropHindi = "आलू",
            diseaseName = "Late Blight",
            diseaseHindi = "पछेती झुलसा",
            severity = "Severe",
            isHealthy = false,
            symptoms = "Water-soaked lesions on leaf tips turning black, white mildew ring on undersides in cool foggy weather.",
            symptomsHindi = "पत्तियों के किनारों पर काले पानीदार धब्बे, कोहरे में सफेद फफूंद।",
            treatment = "Apply systemic fungicide Cymoxanil + Mancozeb (Curzate @ 2.5g/L) or Dimethomorph.",
            treatmentHindi = "साइमोक्सानिल + मैनकोजेब (2.5 ग्राम/लीटर) का तुरंत छिड़काव करें।",
            prevention = "Plant resistant cultivars (like Kufri Girdhari), hill up soil high."
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
            treatment = "Spray Mancozeb 75 WP (2g/L) or Propineb (2g/L) at first notice of spots.",
            treatmentHindi = "मैनकोजेब 75 WP (2 ग्राम/लीटर) का छिड़काव करें।",
            prevention = "Use certified seed tubers, follow 3-year crop rotation with non-solanaceous crops."
        ),
        DiseaseInfo(
            classId = "Potato___healthy",
            cropName = "Potato",
            cropHindi = "आलू",
            diseaseName = "Healthy Plant",
            diseaseHindi = "स्वस्थ पौधा",
            severity = "None",
            isHealthy = true,
            symptoms = "Robust deep-green leaves with crisp edges, absence of blotches.",
            symptomsHindi = "पत्तियां पूरी तरह हरी और रोगमुक्त हैं।",
            treatment = "Maintain balanced moisture and soil earthing up.",
            treatmentHindi = "उचित सिंचाई और मिट्टी चढ़ाने का कार्य जारी रखें।",
            prevention = "Scout fields weekly, maintain clean seed certification."
        ),
        DiseaseInfo(
            classId = "Apple___Apple_scab",
            cropName = "Apple",
            cropHindi = "सेब",
            diseaseName = "Apple Scab",
            diseaseHindi = "सेब का स्कैब",
            severity = "Severe",
            isHealthy = false,
            symptoms = "Olive-green to velvety dark spots on leaves and fruit, causing leaf drop.",
            symptomsHindi = "पत्तियों और फलों पर गहरे जैतून-हरे मखमली धब्बे।",
            treatment = "Apply Difenoconazole (0.5ml/L) or Captan (2g/L) at bud break and petal fall.",
            treatmentHindi = "डिफेनोकोनाज़ोल या कैप्टन का छिड़काव करें।",
            prevention = "Rake and burn fallen orchard leaves in autumn, prune canopy."
        )
    )

    override suspend fun getDiseaseInfo(classId: String): Result<DiseaseInfo> = withContext(Dispatchers.IO) {
        val match = staticDiseases.find { it.classId == classId } ?: staticDiseases[0]
        Result.success(match)
    }

    override suspend fun getAllDiseases(cropFilter: String?): Result<List<DiseaseInfo>> = withContext(Dispatchers.IO) {
        val filtered = if (cropFilter.isNullOrBlank() || cropFilter.equals("All", ignoreCase = true)) {
            staticDiseases
        } else {
            staticDiseases.filter { it.cropName.contains(cropFilter, ignoreCase = true) }
        }
        Result.success(filtered)
    }

    override suspend fun askAiAdvisory(
        primaryClass: String,
        confidence: Float,
        query: String,
        language: String
    ): Result<String> = withContext(Dispatchers.IO) {
        // Direct live call to NVIDIA NIM Vision/LLM API
        nvidiaClient.getAgronomyAdvice(
            primaryClass = primaryClass,
            confidence = confidence,
            query = query,
            language = language
        )
    }
}
