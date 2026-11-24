package com.fasaldrishti.app.util

import com.fasaldrishti.app.domain.model.FarmReminder
import com.fasaldrishti.app.domain.model.ReminderType

object ReminderTranslator {

    fun translateReminder(reminder: FarmReminder, targetLang: String): Pair<String, String> {
        val cropName = reminder.cropName.ifBlank { "Crop" }
        val diseaseName = reminder.diseaseName.ifBlank { "Condition" }

        val (titleTemplate, msgTemplate) = when (reminder.reminderType) {
            ReminderType.SPRAY -> getSprayTemplates(targetLang)
            ReminderType.INSPECTION -> getInspectionTemplates(targetLang)
            ReminderType.WEATHER -> getWeatherTemplates(targetLang)
        }

        val title = titleTemplate.replace("{crop}", cropName).replace("{disease}", diseaseName)
        val message = msgTemplate.replace("{crop}", cropName).replace("{disease}", diseaseName)
            .replace("{msg}", reminder.messageEn)

        return Pair(title, message)
    }

    private fun getSprayTemplates(lang: String): Pair<String, String> = when (lang) {
        "hi" -> Pair(
            "{crop}: {disease} दूसरा स्प्रे रिमाइंडर 💧",
            "{crop} के खेत में {disease} के नियंत्रण के लिए सुझाई गई दवा का दूसरा स्प्रे समय पर लगाएं। 15L नैप्सैक पंप में सही मात्रा का ध्यान रखें।"
        )
        "bn" -> Pair(
            "{crop}: {disease} দ্বিতীয় স্প্রে রিমাইন্ডার 💧",
            "{crop} ক্ষেতে {disease} দমনের জন্য নির্ধারিত রাসায়নিকের দ্বিতীয় স্প্রে প্রয়োগের সময় হয়েছে। ১৫ লিটার ন্যাপস্যাক ট্যাংকের সঠিক মাত্রা নিশ্চিত করুন।"
        )
        "mr" -> Pair(
            "{crop}: {disease} दुसरी फवारणी आठवण 💧",
            "{crop} पिकातील {disease} नियंत्रणासाठी दुसरी फवारणी वेळेवर करा. १५ लिटर पंपासाठी योग्य मात्रा वापरा."
        )
        "pa" -> Pair(
            "{crop}: {disease} ਦੂਜਾ ਸਪਰੇਅ ਰੀਮਾਈਂਡਰ 💧",
            "{crop} ਵਿੱਚ {disease} ਦੀ ਰੋਕਥਾਮ ਲਈ ਦੂਜੀ ਸਪਰੇਅ ਦਾ ਸਮਾਂ ਆ ਗਿਆ ਹੈ। 15L ਪੰਪ ਦੀ ਸਹੀ ਖੁਰਾਕ ਵਰਤੋ।"
        )
        "te" -> Pair(
            "{crop}: {disease} రెండవ స్ప్రే రిమైండర్ 💧",
            "{crop} లో {disease} నివారణకు రెండవ స్ప్రే సమయం వచ్చింది. 15L పంపు మోతాదు సరిగ్గా పాటించండి."
        )
        "ta" -> Pair(
            "{crop}: {disease} இரண்டாவது தெளிப்பு நினைவூட்டல் 💧",
            "{crop} பயிரில் {disease} கட்டுப்படுத்த இரண்டாவது தெளிப்பு நேரம் வந்துவிட்டது. 15L தெளிப்பான் அளவை சரியாக பயன்படுத்தவும்."
        )
        "gu" -> Pair(
            "{crop}: {disease} બીજો છંટકાવ રિમાઇન્ડર 💧",
            "{crop} માં {disease} ના નિયંત્રણ માટે બીજો છંટકાવ કરવાનો સમય થઈ ગયો છે. 15L પંપ માટે યોગ્ય માત્રા વાપરો."
        )
        else -> Pair(
            "{crop}: {disease} 2nd Spray Due 💧",
            "Time for 2nd follow-up spray for {disease} on {crop}. Check dosage instructions and spray in calm morning hours."
        )
    }

    private fun getInspectionTemplates(lang: String): Pair<String, String> = when (lang) {
        "hi" -> Pair(
            "🌾 {crop} साप्ताहिक खेत स्वास्थ्य निरीक्षण",
            "{crop} के खेत का निरीक्षण किए हुए कई दिन हो गए हैं। नए पत्तों पर किसी भी प्रकार के धब्बे या कीट की जांच के लिए नया AI स्कैन करें।"
        )
        "bn" -> Pair(
            "🌾 {crop} সাপ্তাহিক ক্ষেত স্বাস্থ্য পর্যবেক্ষণ",
            "{crop} ক্ষেত পরিদর্শন করার কয়েক দিন পেরিয়ে গেছে। নতুন পাতার রোগ বা পোকা শনাক্ত করতে ক্যামেরা দিয়ে একটি নতুন AI স্ক্যান করুন।"
        )
        "mr" -> Pair(
            "🌾 {crop} साप्ताहिक शेत आरोग्य तपासणी",
            "{crop} शेताची तपासणी करून काही दिवस झाले आहेत. पानांवर नवीन डाग किंवा कीड तपासण्यासाठी नवीन AI स्कॅन करा."
        )
        "pa" -> Pair(
            "🌾 {crop} ਹਫ਼ਤਾਵਾਰੀ ਖੇਤ ਨਿਰੀਖਣ",
            "{crop} ਦੇ ਖੇਤ ਦਾ ਨਿਰੀਖਣ ਕਰਨ ਦਾ ਸਮਾਂ ਆ ਗਿਆ ਹੈ। ਨਵੇਂ ਪੱਤਿਆਂ ਦੀ ਜਾਂਚ ਲਈ ਨਵਾਂ AI ਸਕੈਨ ਕਰੋ।"
        )
        "te" -> Pair(
            "🌾 {crop} వారపు పొలం తనిఖీ",
            "{crop} పొలాన్ని పరిశీలించి కొద్ది రోజులు గడిచాయి. ఆకులపై వ్యాధుల కోసం కొత్త AI స్కాన్ చేయండి."
        )
        "ta" -> Pair(
            "🌾 {crop} வாராந்திர வயல் ஆய்வு",
            "{crop} பயிரை ஆய்வு செய்ய நேரம் வந்துவிட்டது. இலைகளில் நோய் உள்ளதா என புதிய AI ஸ்கேன் செய்யவும்."
        )
        "gu" -> Pair(
            "🌾 {crop} સાપ્તાહિક ખેતર નિરીક્ષણ",
            "{crop} ના ખેતરનું નિરીક્ષણ કરવાનો સમય આવી ગયો છે. રોગની તપાસ માટે નવું AI સ્કેન કરો."
        )
        else -> Pair(
            "🌾 {crop} Weekly Field Inspection",
            "It has been a few days since your last field check. Perform an AI scan to check for new leaf spots or pest symptoms."
        )
    }

    private fun getWeatherTemplates(lang: String): Pair<String, String> = when (lang) {
        "hi" -> Pair(
            "🌧️ {crop} मौसम एवं स्प्रे चेतावनी",
            "{crop} के क्षेत्र में मौसम परिवर्तन या बारिश की संभावना है। रासायनिक छिड़काव तेज हवा या बारिश में न करें।"
        )
        "bn" -> Pair(
            "🌧️ {crop} আবহাওয়া ও স্প্রে সতর্কতা",
            "{crop} অঞ্চলে বৃষ্টি বা প্রতিকূল আবহাওয়ার সম্ভাবনা রয়েছে। বৃষ্টির আগে বা দমকা বাতাসে স্প্রে করা এড়িয়ে চলুন।"
        )
        "mr" -> Pair(
            "🌧️ {crop} हवामान व फवारणी इशारा",
            "{crop} परिसरात पाऊस किंवा प्रतिकूल हवामानाची शक्यता आहे. पावसात फवारणी करू नका."
        )
        "pa" -> Pair(
            "🌧️ {crop} ਮੌਸਮ ਅਤੇ ਸਪਰੇਅ ਚੇਤਾਵਨੀ",
            "{crop} ਖੇਤਰ ਵਿੱਚ ਮੀਂਹ ਪੈਣ ਦੀ ਸੰਭਾਵਨਾ ਹੈ। ਮੀਂਹ ਦੌਰਾਨ ਸਪਰੇਅ ਕਰਨ ਤੋਂ ਬਚੋ।"
        )
        "te" -> Pair(
            "🌧️ {crop} వాతావరణ హెచ్చరిక",
            "{crop} ప్రాంతంలో వర్షం పడే అవకాశం ఉంది. వర్షంలో రసాయన స్ప్రే చేయవద్దు."
        )
        "ta" -> Pair(
            "🌧️ {crop} வானிலை எச்சரிக்கை",
            "{crop} பகுதியில் மழை பெய்ய வாய்ப்புள்ளது. மழையில் தெளிக்க வேண்டாம்."
        )
        "gu" -> Pair(
            "🌧️ {crop} હવામાન ચેતવણી",
            "{crop} વિસ્તારમાં વરસાદની સંભાવના છે. વરસાદમાં છંટકાવ કરવાનું ટાળો."
        )
        else -> Pair(
            "🌧️ {crop} Weather & Spray Advisory",
            "Unfavorable weather or rain forecast detected for {crop}. Postpone chemical spraying until winds are calm and foliage is dry."
        )
    }
}
