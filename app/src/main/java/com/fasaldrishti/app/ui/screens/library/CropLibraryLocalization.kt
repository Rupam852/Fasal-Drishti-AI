package com.fasaldrishti.app.ui.screens.library

import com.fasaldrishti.app.domain.model.DiseaseInfo

data class LocalizedDiseaseDossier(
    val cropName: String,
    val diseaseName: String,
    val symptoms: String,
    val treatment: String,
    val prevention: String
)

object CropLibraryLocalization {

    fun getLocalizedDossier(disease: DiseaseInfo, langCode: String): LocalizedDiseaseDossier {
        val crop = getSimpleCropName(disease.cropName, langCode)
        val diseaseName = getLocalizedDiseaseName(disease.classId, langCode, disease.diseaseName, disease.diseaseHindi)

        return when (langCode) {
            "bn" -> getBengaliDossier(disease, crop, diseaseName)
            "hi" -> getHindiDossier(disease, crop, diseaseName)
            "mr" -> getMarathiDossier(disease, crop, diseaseName)
            "pa" -> getPunjabiDossier(disease, crop, diseaseName)
            "gu" -> getGujaratiDossier(disease, crop, diseaseName)
            "te" -> getTeluguDossier(disease, crop, diseaseName)
            "ta" -> getTamilDossier(disease, crop, diseaseName)
            else -> getEnglishDossier(disease, crop, diseaseName)
        }
    }

    private fun getEnglishDossier(disease: DiseaseInfo, crop: String, diseaseName: String): LocalizedDiseaseDossier {
        return LocalizedDiseaseDossier(
            cropName = crop,
            diseaseName = disease.diseaseName,
            symptoms = disease.symptoms,
            treatment = disease.treatment,
            prevention = disease.prevention
        )
    }

    private fun getHindiDossier(disease: DiseaseInfo, crop: String, diseaseName: String): LocalizedDiseaseDossier {
        val symptoms = disease.symptomsHindi ?: disease.symptoms
        val treatment = disease.treatmentHindi ?: disease.treatment
        val prevention = if (disease.isHealthy) {
            "साप्ताहिक फसल निरीक्षण करें और खेत की मेड़ों को खरपतवार मुक्त रखें।"
        } else {
            "• जैविक उपचार: ट्राइकोडर्मा विरिडी या 5% नीम तेल (5 मिली/लीटर) का छिड़काव करें।\n• खेत की स्वच्छता: ग्रसित पत्तियों और अवशेषों को नष्ट करें, संतुलित यूरिया व पोटाश डालें।\n• फसल सुरक्षा (PHI): कटाई से 14-21 दिन पहले किसी भी रासायनिक दवा का छिड़काव रोक दें।"
        }

        return LocalizedDiseaseDossier(
            cropName = disease.cropHindi ?: crop,
            diseaseName = disease.diseaseHindi ?: diseaseName,
            symptoms = symptoms,
            treatment = treatment,
            prevention = prevention
        )
    }

    private fun getBengaliDossier(disease: DiseaseInfo, crop: String, diseaseName: String): LocalizedDiseaseDossier {
        val (symptoms, treatment, prevention) = when {
            disease.classId.contains("healthy", ignoreCase = true) -> Triple(
                "গাছের পাতা সম্পূর্ণ সতেজ ও সবুজ, বলিষ্ঠ কুশি এবং স্বাভাবিক বিকাশ পরিলক্ষিত হচ্ছে।",
                "গাছ সম্পূর্ণ সুস্থ। নিয়মিত নির্ধারিত সেচ ও সুষম সার প্রয়োগ অব্যাহত রাখুন।",
                "সাপ্তাহিক ক্ষেত পর্যবেক্ষণ করুন এবং জমির আইল আগাছামুক্ত রাখুন।"
            )

            // RICE DISEASES
            disease.classId == "Rice___Blast" || disease.classId == "Rice___Leaf_blast" -> Triple(
                "পাতায় চোখের বা নৌকার মতো ধূসর-সাদা কেন্দ্রযুক্ত বাদামী দাগ, কাণ্ড ও শিষের সংযোগস্থল পচে যাওয়া।",
                "• প্রধান রাসায়নিক ওষুধ: ট্রাইসাইক্লাজোল ৭৫% WP (বীম / বান @ ০.৬ গ্রাম/লিটার) অথবা কাসুগামাইসিন ৩% SL (২ মিলি/লিটার)\n• ১৫ লিটার পাম্পের মাত্রা: ৯-১০ গ্রাম ওষুধ প্রতি ১৫ লিটার জলে\n• প্রতি একর মাত্রা: ১২০-১৫০ গ্রাম ওষুধ (২০০ লিটার জলে)\n• স্প্রে করার সময়: শান্ত সকাল (৭:০০-৯:৩০) অথবা ঠান্ডা বিকেল (৪:০০-৬:০০)\n• ওষুধ পরিবর্তন: ১০ দিন পর আইসোপ্রোথিওলেন ৪০% EC (ফুজিওয়ান @ ১.৫ মিলি/লিটার) স্প্রে করুন",
                "• জৈব প্রতিকার: সিউডোমোনাস ফ্লুরোসেন্স @ ৫ গ্রাম/লিটার অথবা ৫% নিম তেল স্প্রে করুন\n• ফসলের যত্ন: অতিরিক্ত ইউরিয়া সার পরিহার করুন, জমিতে সঠিক জলস্তর রাখুন\n• তোলার সুরক্ষা (PHI): ধান কাটার ১৪-২১ দিন আগে স্প্রে বন্ধ করুন"
            )

            disease.classId == "Rice___Bacterial_blight" || disease.classId == "Rice___Bacterial_leaf_blight" -> Triple(
                "পাতার ডগা ও ধার থেকে নিচের দিকে হলুদ-সাদা ঢেউ খেলানো রেখা, পাতা শুকিয়ে বাদামী হওয়া।",
                "• প্রধান রাসায়নিক ওষুধ: স্ট্রেপ্টোসাইক্লিন (১ গ্রাম প্রতি ১০ লিটার) + কপার অক্সিক্লোরাইড ৫০% WP (৩ গ্রাম প্রতি লিটার)\n• ১৫ লিটার পাম্পের মাত্রা: ১.৫ গ্রাম স্ট্রেপ্টোসাইক্লিন + ৪৫ গ্রাম কপার প্রতি ১৫L পাম্প\n• প্রতি একর মাত্রা: ২০-৩০ গ্রাম স্ট্রেপ্টোসাইক্লিন + ৫০০ গ্রাম কপার (২০০ লিটার জলে)\n• স্প্রে করার সময়: বিকেলের ঠান্ডা আবহাওয়ায় স্প্রে করুন\n• ওষুধ পরিবর্তন: বৃষ্টি চলতে থাকলে ১০-১২ দিন পর পুনরায় স্প্রে করুন",
                "• জৈব প্রতিকার: তাজা গোবর নির্যাস (২০%) অথবা ট্রাইকোডার্মা স্প্রে করুন\n• ফসলের যত্ন: জমিতে ৩-৪ দিন জল জমতে দেবেন না যাতে ব্যাকটেরিয়ার বিস্তার রোধ হয়\n• তোলার সুরক্ষা (PHI): ফসল তোলার ১৪ দিন আগে স্প্রে বন্ধ রাখুন"
            )

            disease.classId == "Rice___Brown_spot" -> Triple(
                "পাতার উপর ডিম্বাকৃতি বা গোলাকার গাঢ় বাদামী দাগ যার চারপাশে হালকা হলুদ বলয় দেখা যায়।",
                "• প্রধান রাসায়নিক ওষুধ: ম্যানকোজেব ৭৫% WP (২.৫ গ্রাম/লিটার) অথবা প্রোপিকোনাজোল ২৫% EC (টিল্ট @ ১ মিলি/লিটার)\n• ১৫ লিটার পাম্পের মাত্রা: ৩৫-৪০ গ্রাম ম্যানকোজেব অথবা ১৫ মিলি টিল্ট প্রতি ১৫L পাম্প\n• প্রতি একর মাত্রা: ৫০০-৬০০ গ্রাম ওষুধ (২০০ লিটার জলে)\n• স্প্রে করার সময়: সকালে অথবা বিকেলের ঠান্ডা সময়ে স্প্রে করুন\n• ওষুধ পরিবর্তন: ১০-১২ দিন পর হেক্সাকোনাজোল ৫% EC (২ মিলি/লিটার) স্প্রে করুন",
                "• জৈব প্রতিকার: সুষম NPK ও জিঙ্ক সালফেট (১০ কেজি/একর) প্রয়োগ করুন\n• ফসলের যত্ন: মাটির নিষ্কাশন উন্নত করুন, নোংরা জল জমে থাকা এড়িয়ে চলুন\n• তোলার সুরক্ষা (PHI): ফসল তোলার ১৪ দিন আগে স্প্রে বন্ধ রাখুন"
            )

            disease.classId == "Rice___Sheath_blight" -> Triple(
                "জলের স্তরের কাছে পাতার খোলে সবুজাভ-ধূসর ডিম্বাকৃতি দাগ যা পরে সাপের চামড়ার মতো উপরে ছড়ায়।",
                "• প্রধান রাসায়নিক ওষুধ: হেক্সাকোনাজোল ৫% EC (কন্ট্যাফ @ ২ মিলি/লিটার) অথবা ভ্যালিডামাইসিন ৩% L @ ২.৫ মিলি/লিটার\n• ১৫ লিটার পাম্পের মাত্রা: ৩০ মিলি কন্ট্যাফ অথবা ৩৫ মিলি ভ্যালিডামাইসিন প্রতি ১৫L পাম্প\n• প্রতি একর মাত্রা: ৪০০ মিলি ওষুধ (২০০ লিটার জলে, কাণ্ডের গোড়ায় স্প্রে করুন)\n• স্প্রে করার সময়: স্প্রে নোজলটি জলের স্তরের কাছে কাণ্ডের দিকে রাখুন\n• ওষুধ পরিবর্তন: ১০ দিন পর থাইফ্লুজামাইড ২৪% SC (পালসার @ ০.৭৫ মিলি/লিটার) স্প্রে করুন",
                "• জৈব প্রতিকার: ট্রাইকোডার্মা ভিরিডি মেশানো গোবর সার জমিতে প্রয়োগ করুন\n• ফসলের যত্ন: অতিরিক্ত ইউরিয়া এড়িয়ে চলুন, জমির আইল আগাছামুক্ত রাখুন\n• তোলার সুরক্ষা (PHI): ধান কাটার ১৪-২১ দিন আগে স্প্রে বন্ধ করুন"
            )

            disease.classId == "Rice___Rice_hispa" -> Triple(
                "পাতার সমান্তরালে সাদা দাগ ও রেখা, পাতা শুকিয়ে পুড়ে যাওয়ার মতো বাদামী হওয়া।",
                "• প্রধান রাসায়নিক ওষুধ: ক্লোরপাইরিফস ২০% EC @ ২ মিলি/লিটার অথবা কার্বোসালফান ২৫% EC @ ২ মিলি/লিটার\n• ১৫ লিটার পাম্পের মাত্রা: ৩০ মিলি ওষুধ প্রতি ১৫L পাম্প\n• প্রতি একর মাত্রা: ৪০০-৫০০ মিলি ওষুধ (২০০ লিটার জলে)\n• স্প্রে করার সময়: শান্ত সকাল অথবা সন্ধ্যায় স্প্রে করুন",
                "• জৈব প্রতিকার: জাল দিয়ে পামরি পোকা সংগ্রহ করে নষ্ট করুন এবং ৫% নিম তেল স্প্রে করুন"
            )

            disease.classId == "Rice___Leaf_scald" -> Triple(
                "পাতার ডগায় জলপোড়া বাদামী ব্যান্ডিং দাগ যা শুকিয়ে কাঁচি বা ফিতার মতো রূপ নেয়।",
                "• প্রধান রাসায়নিক ওষুধ: প্রোপিকোনাজোল ২৫% EC (টিল্ট @ ১ মিলি/লিটার)\n• ১৫ লিটার পাম্পের মাত্রা: ১৫ মিলি টিল্ট প্রতি ১৫L পাম্প\n• প্রতি একর মাত্রা: ২০০ মিলি ওষুধ (২০০ লিটার জলে)",
                "• জৈব প্রতিকার: সুষম সার প্রয়োগ ও উপযুক্ত সেচ ব্যবস্থাপনা।"
            )

            disease.classId == "Rice___Narrow_brown_leaf_spot" -> Triple(
                "পাতার শিরার সমান্তরালে সরু রৈখিক গাঢ় বাদামী দাগ।",
                "• প্রধান রাসায়নিক ওষুধ: ম্যানকোজেব ৭৫% WP (২.৫ গ্রাম/লিটার)\n• ১৫ লিটার পাম্পের মাত্রা: ৩৫-৪০ গ্রাম ম্যানকোজেব প্রতি ১৫L পাম্প\n• প্রতি একর মাত্রা: ৫০০ গ্রাম ওষুধ (২০০ লিটার জলে)",
                "• জৈব প্রতিকার: পটাশ সারের সুষম ব্যবহার এবং ফসলের সঠিক যত্ন।"
            )

            // WHEAT DISEASES
            disease.classId == "Wheat___Yellow_rust" || disease.classId == "Wheat___Stripe_rust" -> Triple(
                "পাতার শিরার সমান্তরালে উজ্জ্বল হলুদ গুঁড়ো পাউডারের মতো সারিবদ্ধ দাগ, হাত দিলে হলুদ রঙ লাগা।",
                "• প্রধান রাসায়নিক ওষুধ: প্রোপিকোনাজোল ২৫% EC (টিল্ট @ ১ মিলি/লিটার) অথবা টেবুকোনাজোল ২৫.৯% EC @ ১ মিলি/লিটার\n• ১৫ লিটার পাম্পের মাত্রা: ১৫-২০ মিলি ওষুধ প্রতি ১৫L পাম্প\n• প্রতি একর মাত্রা: ২০০ মিলি ওষুধ (২০০ লিটার জলে)\n• স্প্রে করার সময়: সকালের রোদ ওঠার পর ও শিশির শুকানোর পর (৮:০০-১১:০০)",
                "• জৈব প্রতিকার: রোগের শুরুতে ট্রাইকোডার্মা বা নিম তেল ৫% স্প্রে করুন\n• ফসলের যত্ন: প্রতিরোধী জাত ব্যবহার করুন, অতিরিক্ত নাইট্রোজেন এড়িয়ে চলুন\n• তোলার সুরক্ষা (PHI): গম কাটার ৩০ দিন আগে স্প্রে শেষ করুন"
            )

            disease.classId == "Wheat___Brown_rust" || disease.classId == "Wheat___Leaf_rust" -> Triple(
                "পাতার উপরের পৃষ্ঠে ছোট গোলাকার বা ডিম্বাকৃতি কমলা-বাদামী রঙের বিক্ষিপ্ত দাগ।",
                "• প্রধান রাসায়নিক ওষুধ: ম্যানকোজেব ৭৫% WP @ ২.৫ গ্রাম/লিটার অথবা প্রোপিকোনাজোল ২৫% EC @ ১ মিলি/লিটার\n• ১৫ লিটার পাম্পের মাত্রা: ৩৫-৪০ গ্রাম ম্যানকোজেব অথবা ১৫ মিলি টিল্ট প্রতি ১৫L পাম্প\n• প্রতি একর মাত্রা: ৫০০-৬০০ গ্রাম ওষুধ (২০০ লিটার জলে)",
                "• জৈব প্রতিকার: সুষম পটাশ সার প্রয়োগ করুন যা পাতার রোগ প্রতিরোধ ক্ষমতা বাড়ায়"
            )

            disease.classId == "Wheat___Stem_rust" -> Triple(
                "কাণ্ড ও পাতায় লম্বাটে গাঢ় লালচে-কালো রঙের ফোস্কা যা ফেটে কালো পাউডার বের হয়।",
                "• প্রধান রাসায়নিক ওষুধ: প্রোপিকোনাজোল ২৫% EC (টিল্ট @ ১ মিলি/লিটার)\n• ১৫ লিটার পাম্পের মাত্রা: ১৫-২০ মিলি প্রতি ১৫L পাম্প\n• প্রতি একর মাত্রা: ২০০ মিলি ওষুধ (২০০ লিটার জলে)",
                "• ফসলের যত্ন: সময়মতো বপন করুন এবং রোগ প্রতিরোধী জাত রোপণ করুন"
            )

            disease.classId == "Wheat___Powdery_mildew" -> Triple(
                "পাতার উপর সাদা ময়দার মতো গুঁড়ো স্তর তৈরি হওয়া যা ধীরে ধীরে পুরো পাতায় ছড়িয়ে পড়ে।",
                "• প্রধান রাসায়নিক ওষুধ: সালফার ৮০% WDG @ ৩ গ্রাম/লিটার অথবা হেক্সাকোনাজোল ৫% EC @ ১.৫ মিলি/লিটার\n• ১৫ লিটার পাম্পের মাত্রা: ৪৫ গ্রাম সালফার প্রতি ১৫L পাম্প\n• প্রতি একর মাত্রা: ৫০০-৬০০ গ্রাম সালফার (২০০ লিটার জলে)",
                "• জৈব প্রতিকার: ৫% নিম তেল বা দুধের নির্যাস স্প্রে করুন"
            )

            disease.classId == "Wheat___Leaf_blight" -> Triple(
                "পাতায় ডিম্বাকার হালকা বাদামী পোড়া দাগ যার কেন্দ্র ধূসর এবং ধার হলুদ।",
                "• প্রধান রাসায়নিক ওষুধ: ম্যানকোজেব ৭৫% WP @ ২.৫ গ্রাম/লিটার\n• ১৫ লিটার পাম্পের মাত্রা: ৩৫-৪০ গ্রাম প্রতি ১৫L পাম্প\n• প্রতি একর মাত্রা: ৫০০ গ্রাম ওষুধ (২০০ লিটার জলে)",
                "• ফসলের যত্ন: জমিতে জল নিষ্কাশনের সুব্যবস্থা রাখুন"
            )

            disease.classId == "Wheat___Blast" || disease.classId == "Wheat___Wheat_blast" -> Triple(
                "গমের শিষের অংশবিশেষ বা পুরো শিষ সাদা হয়ে শুকিয়ে যাওয়া এবং দানা পুষ্ট না হওয়া।",
                "• প্রধান রাসায়নিক ওষুধ: টেবুকোনাজোল + ট্রাইফ্লক্সিস্ট্রোবিন (ন্যাটিভো @ ০.৫ গ্রাম/লিটার)\n• ১৫ লিটার পাম্পের মাত্রা: ৮-১০ গ্রাম ন্যাটিভো প্রতি ১৫L পাম্প\n• প্রতি একর মাত্রা: ১০০-১২০ গ্রাম ওষুধ (২০০ লিটার জলে)",
                "• ফসলের যত্ন: আক্রান্ত শিষ দ্রুত অপসারণ করুন এবং প্রত্যয়িত বীজ ব্যবহার করুন"
            )

            disease.classId == "Wheat___Fusarium_foot_rot" -> Triple(
                "কাণ্ডের গোড়া বাদামী হয়ে পচে যাওয়া এবং শিকড় দুর্বল হয়ে গাছ শুকিয়ে যাওয়া।",
                "• প্রধান রাসায়নিক ওষুধ: কার্বেনডাজিম ৫০% WP @ ২ গ্রাম/লিটার দিয়ে গোড়া ভেজানো\n• প্রতি একর মাত্রা: ৫০০ গ্রাম ওষুধ জমিতে স্প্রে / সেচের জলে প্রয়োগ",
                "• জৈব প্রতিকার: ট্রাইকোডার্মা ভিরিডি জমিতে প্রয়োগ করুন"
            )

            disease.classId == "Wheat___Black_point" -> Triple(
                "গমের দানার অগ্রভাগে বা ভ্রূণের চারপাশে গাঢ় বাদামী বা কালো ছোপ পড়া।",
                "• প্রধান রাসায়নিক ওষুধ: প্রোপিকোনাজোল ২৫% EC @ ১ মিলি/লিটার শিষ আসার সময় স্প্রে\n• প্রতি একর মাত্রা: ২০০ মিলি ওষুধ (২০০ লিটার জলে)",
                "• ফসলের যত্ন: দানা ভরার সময় জমিতে অতিরিক্ত আর্দ্রতা জমতে দেবেন না"
            )

            // POTATO DISEASES
            disease.classId == "Potato___Late_blight" -> Triple(
                "পাতার কিনারায় জলছাপের মতো দ্রুত বিস্তারকারী কালো পোড়া দাগ ও পাতার নিচে সাদা ছত্রাকের আবরণ।",
                "• প্রধান রাসায়নিক ওষুধ: সাইমোক্সানিল ৮% + ম্যানকোজেব ৬৪% (সেকোটিন @ ২ গ্রাম/লিটার) অথবা মেটালাক্সিল ৮% + ম্যানকোজেব ৬৪% (রিডোমিল গোল্ড @ ২.৫ গ্রাম/লিটার)\n• ১৫ লিটার পাম্পের মাত্রা: ৩৫-৪০ গ্রাম রিডোমিল প্রতি ১৫L পাম্প\n• প্রতি একর মাত্রা: ৫০০ গ্রাম ওষুধ (২০০ লিটার জলে)\n• স্প্রে করার সময়: কুয়াশাচ্ছন্ন বা মেঘলা সকালে অবিলম্বে স্প্রে করুন",
                "• জৈব প্রতিকার: প্রতিরোধক হিসেবে ট্রাইকোডার্মা ও বোর্দো মিশ্রণ ১% ব্যবহার করুন\n• ফসলের যত্ন: আক্রান্ত গাছ পুড়িয়ে ফেলুন এবং পরিমিত সেচ দিন\n• তোলার সুরক্ষা (PHI): আলু তোলার ১০-১৪ দিন আগে স্প্রে বন্ধ করুন"
            )

            disease.classId == "Potato___Early_blight" -> Triple(
                "পুরনো পাতায় লক্ষ্যভেদী গোল গোল রিংযুক্ত বাদামী সমকেন্দ্রিক দাগ যা ধীরে ধীরে বড় হয়।",
                "• প্রধান রাসায়নিক ওষুধ: ম্যানকোজেব ৭৫% WP (২.৫ গ্রাম/লিটার) অথবা ডিফেনোকোনাজোল ২৫% EC (স্কোর @ ০.৫ মিলি/লিটার)\n• ১৫ লিটার পাম্পের মাত্রা: ৩৫-৪০ গ্রাম ম্যানকোজেব প্রতি ১৫L পাম্প\n• প্রতি একর মাত্রা: ৫০০-৬০০ গ্রাম ওষুধ (২০০ লিটার জলে)",
                "• জৈব প্রতিকার: নিম তেল ৫% অথবা সিউডোমোনাস ফ্লুরোসেন্স স্প্রে করুন"
            )

            // TOMATO DISEASES
            disease.classId == "Tomato___Late_blight" -> Triple(
                "পাতা ও কাণ্ডে কালো বাদামী জলপোড়া দাগ এবং ফলের গায়ে খসখসে বাদামী পচন।",
                "• প্রধান রাসায়নিক ওষুধ: মেটালাক্সিল + ম্যানকোজেব (রিডোমিল @ ২.৫ গ্রাম/লিটার) অথবা কপার অক্সিক্লোরাইড ৫০% WP @ ৩ গ্রাম/লিটার\n• ১৫ লিটার পাম্পের মাত্রা: ৩৫-৪০ গ্রাম ওষুধ প্রতি ১৫L পাম্প\n• প্রতি একর মাত্রা: ৫০০ গ্রাম ওষুধ (২০০ লিটার জলে)",
                "• জৈব প্রতিকার: বোর্দো মিশ্রণ ১% স্প্রে করুন এবং আক্রান্ত অংশ কেটে ফেলুন"
            )

            disease.classId == "Tomato___Early_blight" -> Triple(
                "পাতায় গোল বাদামী সমকেন্দ্রিক বলয়যুক্ত দাগ, পাতা হলুদ হয়ে ঝরে পড়া।",
                "• প্রধান রাসায়নিক ওষুধ: ম্যানকোজেব ৭৫% WP (২.৫ গ্রাম/লিটার) অথবা অ্যাজোক্সিস্ট্রোবিন ২৩% SC @ ১ মিলি/লিটার\n• ১৫ লিটার পাম্পের মাত্রা: ৩৫-৪০ গ্রাম ম্যানকোজেব প্রতি ১৫L পাম্প\n• প্রতি একর মাত্রা: ৫০০ গ্রাম ওষুধ (২০০ লিটার জলে)",
                "• ফসলের যত্ন: নিচের আক্রান্ত পাতা ছেঁটে দিন যাতে বাতাস চলাচল বাড়ে"
            )

            disease.classId == "Tomato___Tomato_Yellow_Leaf_Curl_Virus" -> Triple(
                "পাতা উপরের দিকে কুঁকড়ে বাটির মতো হওয়া, শিরা হলুদ ও পুরু হওয়া, গাছের বৃদ্ধি থমকে যাওয়া।",
                "• প্রধান রাসায়নিক ওষুধ: সাদা মাছি দমনে থায়ামেথোক্সাম ২৫% WG (একতারা @ ০.৩ গ্রাম/লিটার) অথবা ডায়াফেন্থিউরন ৫০% WP @ ১.২ গ্রাম/লিটার\n• ১৫ লিটার পাম্পের মাত্রা: ৫ গ্রাম একতারা প্রতি ১৫L পাম্প\n• প্রতি একর মাত্রা: ৮০-১০০ গ্রাম ওষুধ (২০০ লিটার জলে)",
                "• জৈব প্রতিকার: হলুদ আঠালো ফাঁদ (১০টি/একর) এবং ৫% নিম তেল স্প্রে করুন"
            )

            disease.classId == "Tomato___Bacterial_spot" -> Triple(
                "পাতায় ছোট কালো জলীয় দাগ যার চারধারে হলুদ বলয় থাকে এবং ফলের গায়ে খসখসে দাগ।",
                "• প্রধান রাসায়নিক ওষুধ: কপার অক্সিক্লোরাইড ৫০% WP (২.৫ গ্রাম/লিটার) + স্ট্রেপ্টোসাইক্লিন (১ গ্রাম প্রতি ১০ লিটার)\n• ১৫ লিটার পাম্পের মাত্রা: ৩৫ গ্রাম কপার + ১.৫ গ্রাম স্ট্রেপ্টোসাইক্লিন",
                "• ফসলের যত্ন: রোগমুক্ত প্রত্যয়িত বীজ ব্যবহার করুন"
            )

            // COTTON DISEASES
            disease.classId == "Cotton___Leaf_curl" -> Triple(
                "পাতা উপরের বা নিচের দিকে কুঁকড়ে যাওয়া, পাতার নিচের শিরা পুরু ও কাপের মতো ফোলা।",
                "• প্রধান রাসায়নিক ওষুধ: সাদা মাছি দমনে থায়ামেথোক্সাম ২৫% WG @ ০.৩ গ্রাম/লিটার বা পাইরিপ্রোক্সিফেন ১০% EC @ ২ মিলি/লিটার\n• ১৫ লিটার পাম্পের মাত্রা: ৫ গ্রাম একতারা প্রতি ১৫L পাম্প",
                "• জৈব প্রতিকার: নিম তেল ৫% এবং হলুদ আঠালো ফাঁদ স্থাপন করুন"
            )

            disease.classId == "Cotton___Bacterial_blight" -> Triple(
                "পাতায় কোণাকৃতি জলীয় কালো দাগ ও ডালপালা কালো হয়ে শুকিয়ে যাওয়া (ব্ল্যাক আর্ম)।",
                "• প্রধান রাসায়নিক ওষুধ: কপার অক্সিক্লোরাইড ৫০% WP @ ৩ গ্রাম/লিটার + স্ট্রেপ্টোসাইক্লিন @ ১ গ্রাম প্রতি ১০ লিটার\n• ১৫ লিটার পাম্পের মাত্রা: ৪৫ গ্রাম কপার + ১.৫ গ্রাম স্ট্রেপ্টোসাইক্লিন প্রতি ১৫L পাম্প",
                "• ফসলের যত্ন: পূর্ববর্তী ফসলের অবশিষ্টাংশ পুড়িয়ে ফেলুন"
            )

            // SUGARCANE DISEASES
            disease.classId == "Sugarcane___Red_rot" -> Triple(
                "উপরের তৃতীয়-চতুর্থ পাতা শুকিয়ে যাওয়া, কাণ্ড চিরলে ভেতরে লালচে দাগ ও আড়াআড়ি সাদা ছোপ, অ্যালকোহলের গন্ধ।",
                "• প্রধান রাসায়নিক ওষুধ: কার্বেনডাজিম ৫০% WP @ ১ গ্রাম/লিটার দিয়ে বীজ শোধন অথবা থায়োফেনেট মিথাইল ৭০% WP @ ১.৫ গ্রাম/লিটার\n• ১৫ লিটার ট্যাঙ্কের মাত্রা: ১৫-২০ গ্রাম ওষুধ ১৫ লিটার জলে গুলে শোধন",
                "• জৈব প্রতিকার: জমিতে ট্রাইকোডার্মা ভিরিডি (২.৫ কেজি/একর) গোবর সারের সাথে মিশিয়ে দিন"
            )

            disease.classId == "Sugarcane___Smut" -> Triple(
                "আখের অগ্রভাগে চাবুকের মতো লম্বা কালো ঝুলযুক্ত ছড়ি তৈরি হওয়া।",
                "• প্রধান রাসায়নিক ওষুধ: ট্রায়াডিমেফন ২৫% WP (বেলেটন @ ১ গ্রাম/লিটার) দিয়ে কাটিং শোধন",
                "• ফসলের যত্ন: আক্রান্ত আখের চাবুক সাবধানে পলিথিনে মুড়ে পুড়িয়ে ফেলুন"
            )

            // CHILLI DISEASES
            disease.classId == "Chilli___Leaf_curl" -> Triple(
                "থ্রিপস ও মাইটের আক্রমণে পাতা ওপর বা নিচের দিকে কুঁকড়ে নৌকার মতো হওয়া ও গাছের বৃদ্ধি বন্ধ হওয়া।",
                "• প্রধান রাসায়নিক ওষুধ: ফিপ্রোনিল ৫% SC @ ২ মিলি/লিটার অথবা স্পাইরোমেসিফেন ২২.৯% SC @ ১ মিলি/লিটার\n• ১৫ লিটার পাম্পের মাত্রা: ৩০ মিলি ফিপ্রোনিল অথবা ১৫ মিলি ওবেরন প্রতি ১৫L পাম্প",
                "• জৈব প্রতিকার: নীল ও হলুদ আঠালো ফাঁদ এবং ৫% নিম তেল স্প্রে করুন"
            )

            disease.classId == "Chilli___Anthracnose" -> Triple(
                "পাকা লঙ্কায় গোলাকার বসে যাওয়া দাগ এবং ডাল ওপর থেকে নিচের দিকে শুকিয়ে যাওয়া (ডাইব্যাক)।",
                "• প্রধান রাসায়নিক ওষুধ: অ্যাজোক্সিস্ট্রোবিন + ডাইফেনোকোনাজোল (অ্যামিস্টার টপ @ ১ মিলি/লিটার) অথবা ম্যানকোজেব @ ২.৫ গ্রাম/লিটার\n• ১৫ লিটার পাম্পের মাত্রা: ১৫ মিলি অ্যামিস্টার টপ প্রতি ১৫L পাম্প",
                "• ফসলের যত্ন: শুকনো ডাল ছেঁটে ফেলুন এবং বীজ শোধন করুন"
            )

            // MUSTARD DISEASES
            disease.classId == "Mustard___White_rust" -> Triple(
                "পাতার নিচে সাদা চকচকে ফোস্কার মতো দাগ এবং ফুলে বিকৃতি তৈরি হওয়া।",
                "• প্রধান রাসায়নিক ওষুধ: রিডোমিল গোল্ড (মেটালাক্সিল + ম্যানকোজেব @ ২.৫ গ্রাম/লিটার)\n• ১৫ লিটার পাম্পের মাত্রা: ৩৫-৪০ গ্রাম ওষুধ প্রতি ১৫L পাম্প",
                "• জৈব প্রতিকার: ট্রাইকোডার্মা স্প্রে ও আগাম বপন নিশ্চিত করুন"
            )

            disease.classId == "Mustard___Alternaria_blight" -> Triple(
                "পাতায় ও শুঁটিতে গোল গোল গাঢ় বাদামী সমকেন্দ্রিক বলয় দাগ।",
                "• প্রধান রাসায়নিক ওষুধ: ম্যানকোজেব ৭৫% WP (২.৫ গ্রাম/লিটার) অথবা রোভ্রাল ৫০% WP @ ২ গ্রাম/লিটার\n• ১৫ লিটার পাম্পের মাত্রা: ৩৫-৪০ গ্রাম ম্যানকোজেব প্রতি ১৫L পাম্প",
                "• ফসলের যত্ন: সঠিক দূরত্ব বজায় রেখে চারা রোপণ করুন"
            )

            // MANGO DISEASES
            disease.classId == "Mango___Anthracnose" -> Triple(
                "মুকুল ও নতুন পাতায় কালো দাগ, মুকুল ঝরে যাওয়া এবং ফলের গায়ে কালো পচন দাগ।",
                "• প্রধান রাসায়নিক ওষুধ: কপার অক্সিক্লোরাইড ৫০% WP @ ৩ গ্রাম/লিটার অথবা কার্বেনডাজিম ৫০% WP @ ১ গ্রাম/লিটার\n• প্রতি লিটার জলে ২ গ্রাম ওষুধ গুলে গাছে স্প্রে করুন",
                "• ফসলের যত্ন: শুকনো ডাল ছেঁটে বোর্দো পেস্ট লাগান"
            )

            disease.classId == "Mango___Powdery_mildew" -> Triple(
                "মুকুল ও কচি পাতায় সাদা পাউডারের মতো ছাতার আবরণ এবং মুকুল শুকিয়ে ঝরে যাওয়া।",
                "• প্রধান রাসায়নিক ওষুধ: সালফার ৮০% WDG @ ৩ গ্রাম/লিটার অথবা ডেকোনাজোল @ ১ মিলি/লিটার\n• প্রতি লিটার জলে ২.৫ গ্রাম ওষুধ মিশিয়ে স্প্রে করুন",
                "• জৈব প্রতিকার: গাছে নিম তেল ৫% স্প্রে করুন"
            )

            // CORN DISEASES
            disease.classId.contains("Northern_Leaf_Blight", ignoreCase = true) -> Triple(
                "পাতায় লম্বাটে সিগারের মতো ধূসর-বাদামী দাগ যা দ্রুত বিস্তার লাভ করে।",
                "• প্রধান রাসায়নিক ওষুধ: ম্যানকোজেব ৭৫% WP @ ২.৫ গ্রাম/লিটার অথবা অ্যাজোক্সিস্ট্রোবিন @ ১ মিলি/লিটার\n• ১৫ লিটার পাম্পের মাত্রা: ৩৫-৪০ গ্রাম প্রতি ১৫L পাম্প",
                "• ফসলের যত্ন: জমিতে পরিমিত সার প্রয়োগ ও নিষ্কাশন বজায় রাখুন"
            )

            disease.classId.contains("Common_rust", ignoreCase = true) -> Triple(
                "পাতার উভয় পৃষ্ঠে গোলাকার সোনালী-বাদামী গুঁড়ো ফোস্কার মতো দাগ।",
                "• প্রধান রাসায়নিক ওষুধ: ম্যানকোজেব @ ২.৫ গ্রাম/লিটার অথবা প্রোপিকোনাজোল @ ১ মিলি/লিটার\n• ১৫ লিটার পাম্পের মাত্রা: ৩৫-৪০ গ্রাম প্রতি ১৫L পাম্প",
                "• ফসলের যত্ন: প্রতিরোধক জাত চাষ করুন"
            )

            // SOYBEAN DISEASES
            disease.classId.contains("Soybean___Rust", ignoreCase = true) -> Triple(
                "পাতার নিচের পৃষ্ঠে অসংখ্য ক্ষুদ্র বাদামী ফোস্কা এবং পাতা হলদে হয়ে ঝরে পড়া।",
                "• প্রধান রাসায়নিক ওষুধ: হেক্সাকোনাজোল ৫% EC @ ২ মিলি/লিটার অথবা প্রোপিকোনাজোল @ ১ মিলি/লিটার\n• ১৫ লিটার পাম্পের মাত্রা: ৩০ মিলি ওষুধ প্রতি ১৫L পাম্প",
                "• ফসলের যত্ন: আর্দ্র আবহাওয়ায় আগাম স্প্রে করুন"
            )

            // CITRUS DISEASES
            disease.classId.contains("Citrus___Canker", ignoreCase = true) -> Triple(
                "পাতা, ডাল ও ফলের খোসায় খসখসে উঁচু বাদামী দাগ যার চারপাশে হলুদ বলয় থাকে।",
                "• প্রধান রাসায়নিক ওষুধ: কপার অক্সিক্লোরাইড ৫০% WP @ ৩ গ্রাম/লিটার + স্ট্রেপ্টোসাইক্লিন @ ১ গ্রাম প্রতি ১০ লিটার\n• ১৫ লিটার পাম্পের মাত্রা: ৪৫ গ্রাম কপার + ১.৫ গ্রাম স্ট্রেপ্টোসাইক্লিন",
                "• ফসলের যত্ন: আক্রান্ত ডাল ছেঁটে পুড়িয়ে ফেলুন"
            )

            disease.classId.contains("greening", ignoreCase = true) -> Triple(
                "পাতায় অসমান হলুদ ছোপ (মটলিং), শিরা শক্ত হওয়া এবং ফলের আকার বিকৃত ও তেতো হওয়া।",
                "• প্রধান রাসায়নিক ওষুধ: সিট্রাস সাইলা পোকা দমনে ইমিডাক্লোপ্রিড ১৭.৮% SL @ ০.৫ মিলি/লিটার অথবা থায়ামেথোক্সাম @ ০.৩ গ্রাম/লিটার\n• ১৫ লিটার পাম্পের মাত্রা: ৭.৫ মিলি ইমিডাক্লোপ্রিড প্রতি ১৫L পাম্প",
                "• ফসলের যত্ন: সুস্থ প্রত্যয়িত কলম গাছ রোপণ করুন"
            )

            // GRAPE DISEASES
            disease.classId.contains("Downy_mildew", ignoreCase = true) -> Triple(
                "পাতার উপরে তেলতেলে হলুদ দাগ এবং পাতার নিচে সাদা তুলোর মতো ছত্রাক বৃদ্ধি।",
                "• প্রধান রাসায়নিক ওষুধ: মেটালাক্সিল + ম্যানকোজেব @ ২.৫ গ্রাম/লিটার অথবা কপার অক্সিক্লোরাইড @ ৩ গ্রাম/লিটার\n• ১৫ লিটার পাম্পের মাত্রা: ৩৫-৪০ গ্রাম ওষুধ প্রতি ১৫L পাম্প",
                "• জৈব প্রতিকার: বোর্দো মিশ্রণ ১% স্প্রে করুন"
            )

            disease.classId.contains("Black_rot", ignoreCase = true) -> Triple(
                "ফলে বৃত্তাকার কুঁচকানো কালো পচন দাগ এবং পুরো থোকা শুকিয়ে যাওয়া।",
                "• প্রধান রাসায়নিক ওষুধ: ম্যানকোজেব @ ২.৫ গ্রাম/লিটার অথবা মাইক্লোবুটানিল ১০% WP @ ০.৫ গ্রাম/লিটার\n• ১৫ লিটার পাম্পের মাত্রা: ৩৫ গ্রাম ম্যানকোজেব প্রতি ১৫L পাম্প",
                "• ফসলের যত্ন: আক্রান্ত ফল ও ডাল কেটে ফেলুন"
            )

            // APPLE DISEASES
            disease.classId.contains("Apple_scab", ignoreCase = true) -> Triple(
                "পাতায় ও ফলের গায়ে মখমলের মতো জলপাই-সবুজ থেকে কালো খসখসে ক্ষত দাগ।",
                "• প্রধান রাসায়নিক ওষুধ: ম্যানকোজেব ৭৫% WP @ ২.৫ গ্রাম/লিটার অথবা ডোডিন ৬৫% WP @ ১ গ্রাম/লিটার\n• প্রতি লিটার জলে ২ গ্রাম ওষুধ গুলে স্প্রে করুন",
                "• ফসলের যত্ন: বাগানের ঝরে পড়া পাতা পরিষ্কার করে নষ্ট করুন"
            )

            else -> Triple(
                disease.symptomsHindi ?: disease.symptoms,
                disease.treatmentHindi ?: disease.treatment,
                disease.prevention
            )
        }

        return LocalizedDiseaseDossier(
            cropName = crop,
            diseaseName = diseaseName,
            symptoms = symptoms,
            treatment = treatment,
            prevention = prevention
        )
    }

    private fun getMarathiDossier(disease: DiseaseInfo, crop: String, diseaseName: String): LocalizedDiseaseDossier {
        val symptoms = if (disease.isHealthy) "पिके पूर्णपणे निरोगी आणि सशक्त आहेत." else (disease.symptomsHindi ?: disease.symptoms)
        val treatment = if (disease.isHealthy) "कोणत्याही औषधाची गरज नाही. नियमित पाणी आणि खत व्यवस्थापन ठेवा." else (disease.treatmentHindi ?: disease.treatment)
        val prevention = if (disease.isHealthy) "पिकांची नियमित पाहणी करा." else "• सेंद्रिय उपाय: ट्रायकोडर्मा किंवा ५% निंबोळी अर्क (५ मिली/लिटर) फवारा.\n• शेती स्वच्छता: रोगट पाने नष्ट करा आणि संतुलित खतांचा वापर करा.\n• काढणी सुरक्षा: काढणीपूर्वी रासायनिक फवारणी थांबवा."

        return LocalizedDiseaseDossier(
            cropName = crop,
            diseaseName = diseaseName,
            symptoms = symptoms,
            treatment = treatment,
            prevention = prevention
        )
    }

    private fun getPunjabiDossier(disease: DiseaseInfo, crop: String, diseaseName: String): LocalizedDiseaseDossier {
        val symptoms = if (disease.isHealthy) "ਫ਼ਸਲ ਬਿਲਕੁਲ ਤੰਦਰੁਸਤ ਅਤੇ ਹਰੀ-ਭਰੀ ਹੈ।" else (disease.symptomsHindi ?: disease.symptoms)
        val treatment = if (disease.isHealthy) "ਕਿਸੇ ਦਵਾਈ ਦੀ ਲੋੜ ਨਹੀਂ। ਸਮੇਂ ਸਿਰ ਪਾਣੀ ਅਤੇ ਖਾਦ ਦਿਓ।" else (disease.treatmentHindi ?: disease.treatment)
        val prevention = if (disease.isHealthy) "ਫ਼ਸਲ ਦਾ ਨਿਯਮਿਤ ਨਿਰੀਖਣ ਕਰੋ।" else "• ਜੈਵਿਕ ਰੋਕਥਾਮ: ਟ੍ਰਾਈਕੋਡਰਮਾ ਜਾਂ 5% ਨਿੰਮ ਦਾ ਤੇਲ ਛਿੜਕੋ।\n• ਖੇਤ ਦੀ ਸਫ਼ਾਈ: ਬਿਮਾਰੀ ਵਾਲੇ ਪੱਤੇ ਨਸ਼ਟ ਕਰੋ ਅਤੇ ਸੰਤੁਲਿਤ ਖਾਦਾਂ ਵਰਤੋ।\n• ਸੁਰੱਖਿਆ: ਕਟਾਈ ਤੋਂ ਪਹਿਲਾਂ ਦਵਾਈ ਦਾ ਛਿੜਕਾਅ ਬੰਦ ਕਰੋ।"

        return LocalizedDiseaseDossier(
            cropName = crop,
            diseaseName = diseaseName,
            symptoms = symptoms,
            treatment = treatment,
            prevention = prevention
        )
    }

    private fun getGujaratiDossier(disease: DiseaseInfo, crop: String, diseaseName: String): LocalizedDiseaseDossier {
        val symptoms = if (disease.isHealthy) "પાક તંદુરસ્ત અને લીલોછમ છે." else (disease.symptomsHindi ?: disease.symptoms)
        val treatment = if (disease.isHealthy) "કોઈ દવાની જરૂર નથી. નિયમિત પાણી અને ખાતર આપો." else (disease.treatmentHindi ?: disease.treatment)
        val prevention = if (disease.isHealthy) "પાકનું નિયમિત નિરીક્ષણ કરો." else "• જૈવિક નિયંત્રણ: ટ્રાઇકોડર્મા અથવા 5% લીમડાનું તેલ છાંટો.\n• ખેતરની સ્વચ્છતા: રોગગ્રસ્ત પાંદડા બાળી નાખો.\n• સુરક્ષા: લણણી પહેલાં છંટકાવ બંધ કરો."

        return LocalizedDiseaseDossier(
            cropName = crop,
            diseaseName = diseaseName,
            symptoms = symptoms,
            treatment = treatment,
            prevention = prevention
        )
    }

    private fun getTeluguDossier(disease: DiseaseInfo, crop: String, diseaseName: String): LocalizedDiseaseDossier {
        val symptoms = if (disease.isHealthy) "పంట సంపూర్ణ ఆరోగ్యంగా ఉంది." else disease.symptoms
        val treatment = if (disease.isHealthy) "ఎలాంటి మందులు అవసరం లేదు. సకాలంలో నీటి పారుదల అందించండి." else disease.treatment
        val prevention = if (disease.isHealthy) "నిరంతరం పంటను గమనించండి." else "• సేంద్రీయ నివారణ: ట్రైకోడెర్మా లేదా 5% వేపనూనె పిచికారీ చేయండి.\n• పొలం పరిశుభ్రత: వ్యాధి సోకిన ఆకులను తొలగించండి.\n• భద్రత: కోతకు ముందు మందుల పిచికారీని నిలిపివేయండి."

        return LocalizedDiseaseDossier(
            cropName = crop,
            diseaseName = diseaseName,
            symptoms = symptoms,
            treatment = treatment,
            prevention = prevention
        )
    }

    private fun getTamilDossier(disease: DiseaseInfo, crop: String, diseaseName: String): LocalizedDiseaseDossier {
        val symptoms = if (disease.isHealthy) "பயிர் ஆரோக்கியமாகவும் செழிப்பாகவும் உள்ளது." else disease.symptoms
        val treatment = if (disease.isHealthy) "மருந்து தேவையில்லை. முறையான பாசனம் மற்றும் உரமிடுதல் தொடரவும்." else disease.treatment
        val prevention = if (disease.isHealthy) "பயிரை தொடர்ந்து கண்காணிக்கவும்." else "• இயற்கை முறை: ட்ரைக்கோடெர்மா அல்லது 5% வேப்பெண்ணெய் தெளிக்கவும்.\n• வயல் தூய்மை: பாதிக்கப்பட்ட இலைகளை அழிக்கவும்.\n• பாதுகாப்பு: அறுவடைக்கு முன் மருந்து தெளிப்பதை தவிர்க்கவும்."

        return LocalizedDiseaseDossier(
            cropName = crop,
            diseaseName = diseaseName,
            symptoms = symptoms,
            treatment = treatment,
            prevention = prevention
        )
    }

    fun getLocalizedDiseaseName(classId: String, lang: String, fallbackEnglish: String, fallbackHindi: String?): String {
        return when (lang) {
            "bn" -> getBengaliDiseaseName(classId) ?: fallbackEnglish
            "hi" -> fallbackHindi ?: fallbackEnglish
            "mr" -> getMarathiDiseaseName(classId) ?: fallbackHindi ?: fallbackEnglish
            "pa" -> getPunjabiDiseaseName(classId) ?: fallbackHindi ?: fallbackEnglish
            "gu" -> getGujaratiDiseaseName(classId) ?: fallbackHindi ?: fallbackEnglish
            "te" -> getTeluguDiseaseName(classId) ?: fallbackEnglish
            "ta" -> getTamilDiseaseName(classId) ?: fallbackEnglish
            else -> fallbackEnglish
        }
    }

    private fun getBengaliDiseaseName(classId: String): String? = when {
        classId.contains("healthy", ignoreCase = true) -> "সুস্থ ফসল"
        classId == "Rice___Blast" || classId == "Rice___Leaf_blast" -> "ব্লাস্ট / পাতা পোড়া রোগ"
        classId == "Rice___Bacterial_blight" || classId == "Rice___Bacterial_leaf_blight" -> "ব্যাকটেরিয়াল পাতার ধ্বসা (BLB)"
        classId == "Rice___Brown_spot" -> "বাদামী দাগ রোগ (ব্রাউন স্পট)"
        classId == "Rice___Sheath_blight" -> "শীথ ব্লাইট (খোল পচা রোগ)"
        classId == "Rice___Rice_hispa" -> "ধানের পামরি পোকা (হিস্পা)"
        classId == "Rice___Leaf_scald" -> "পাতার স্ক্যাল্ড রোগ"
        classId == "Rice___Narrow_brown_leaf_spot" -> "সরু বাদামী দাগ রোগ"
        classId == "Wheat___Yellow_rust" || classId == "Wheat___Stripe_rust" -> "হলুদ মরিচা (ইয়েলো রাস্ট)"
        classId == "Wheat___Brown_rust" || classId == "Wheat___Leaf_rust" -> "বাদামী মরিচা রোগ"
        classId == "Wheat___Stem_rust" -> "কালো মরিচা রোগ"
        classId == "Wheat___Powdery_mildew" -> "পাউডারি মিলডিউ (সাদা গুঁড়ো)"
        classId == "Wheat___Leaf_blight" -> "পাতার ব্লাইট (ঝলসানো রোগ)"
        classId == "Wheat___Blast" || classId == "Wheat___Wheat_blast" -> "গমের ব্লাস্ট রোগ"
        classId == "Wheat___Fusarium_foot_rot" -> "ফিউসারিয়াম গোড়া পচা রোগ"
        classId == "Wheat___Black_point" -> "ব্ল্যাক পয়েন্ট রোগ"
        classId == "Potato___Late_blight" -> "আলুর নাবি ধ্বসা রোগ (লেট ব্লাইট)"
        classId == "Potato___Early_blight" -> "আলুর আগাম ধ্বসা রোগ (আর্লি ব্লাইট)"
        classId == "Tomato___Late_blight" -> "টমেটোর নাবি ধ্বসা রোগ"
        classId == "Tomato___Early_blight" -> "টমেটোর আগাম ধ্বসা রোগ"
        classId == "Tomato___Tomato_Yellow_Leaf_Curl_Virus" -> "হলুদ পাতা কোঁকড়ানো ভাইরাস (TYLCV)"
        classId == "Tomato___Bacterial_spot" -> "ব্যাকটেরিয়াল পাতার দাগ"
        classId == "Cotton___Leaf_curl" -> "তুলা পাতা কোঁকড়ানো ভাইরাস"
        classId == "Cotton___Bacterial_blight" -> "ব্যাকটেরিয়াল ব্লাইট (ব্ল্যাক আর্ম)"
        classId == "Sugarcane___Red_rot" -> "আখের লাল পচা রোগ (রেড রট)"
        classId == "Sugarcane___Smut" -> "আখের ঝুল রোগ (স্মাট)"
        classId == "Chilli___Leaf_curl" -> "লঙ্কার পাতা কোঁকড়ানো রোগ"
        classId == "Chilli___Anthracnose" -> "লঙ্কার ডাইব্যাক ও ফল পচা রোগ"
        classId == "Mustard___White_rust" -> "সরিষার সাদা মরিচা রোগ"
        classId == "Mustard___Alternaria_blight" -> "অল্টারনারিয়া পাতা ঝলসানো রোগ"
        classId == "Mango___Anthracnose" -> "আমের অ্যানথ্রাকনোজ (কালো দাগ)"
        classId == "Mango___Powdery_mildew" -> "আমের পাউডারি মিলডিউ"
        classId.contains("Northern_Leaf_Blight", ignoreCase = true) -> "ভুট্টার নর্দার্ন লিফ ব্লাইট"
        classId.contains("Common_rust", ignoreCase = true) -> "ভুট্টার সাধারণ মরিচা রোগ"
        classId.contains("Soybean___Rust", ignoreCase = true) -> "সয়াবিনের মরিচা রোগ"
        classId.contains("Citrus___Canker", ignoreCase = true) -> "লেবুর ক্যাঙ্কার রোগ"
        classId.contains("greening", ignoreCase = true) -> "সিট্রাস গ্রিনিং রোগ"
        classId.contains("Downy_mildew", ignoreCase = true) -> "আঙুরের ডাউনি মিলডিউ"
        classId.contains("Black_rot", ignoreCase = true) -> "আঙুরের ব্ল্যাক রট (কালো পচা)"
        classId.contains("Apple_scab", ignoreCase = true) -> "আপেলের স্ক্যাব রোগ"
        else -> null
    }

    private fun getMarathiDiseaseName(classId: String): String? = when {
        classId.contains("healthy", ignoreCase = true) -> "निरोगी पीक"
        classId == "Rice___Blast" || classId == "Rice___Leaf_blast" -> "भाताचा करपा / ब्लास्ट रोग"
        classId == "Rice___Bacterial_blight" || classId == "Rice___Bacterial_leaf_blight" -> "जीवाणू करपा (BLB)"
        classId == "Rice___Brown_spot" -> "तपकिरी ठिपके रोग"
        classId == "Rice___Sheath_blight" -> "खोडावरील करपा (शीथ ब्लाइट)"
        classId == "Wheat___Yellow_rust" || classId == "Wheat___Stripe_rust" -> "पिवळा तांबेरा (हळद्या रोग)"
        classId == "Wheat___Brown_rust" || classId == "Wheat___Leaf_rust" -> "तपकिरी तांबेरा रोग"
        classId == "Wheat___Stem_rust" -> "काळा तांबेरा"
        classId == "Wheat___Powdery_mildew" -> "भुरी रोग (पावडरी मिल्ड्यू)"
        classId == "Potato___Late_blight" -> "बटाट्याचा पिछेता करपा"
        classId == "Potato___Early_blight" -> "बटाट्याचा अगेती करपा"
        classId == "Tomato___Late_blight" -> "टोमॅटोचा लेट ब्लाइट (करपा)"
        classId == "Tomato___Early_blight" -> "टोमॅटोचा अर्ली ब्लाइट"
        classId == "Cotton___Leaf_curl" -> "कापूस पाने आकसणे रोग"
        classId == "Cotton___Bacterial_blight" -> "जिवाणू करपा (ब्लॅक आर्म)"
        classId == "Sugarcane___Red_rot" -> "ऊसाचा तांब्या (रेड रॉट)"
        classId == "Sugarcane___Smut" -> "काणी रोग (स्मट)"
        classId == "Chilli___Leaf_curl" -> "मिरचीचा चुरडा-मुरडा रोग"
        classId == "Chilli___Anthracnose" -> "फळसड व फांदी वाळणे"
        classId == "Mustard___White_rust" -> "पांढरा तांबेरा"
        classId == "Mustard___Alternaria_blight" -> "अल्टरनेरिया करपा"
        classId == "Mango___Anthracnose" -> "आंब्यावरील करपा"
        classId == "Mango___Powdery_mildew" -> "आंब्यावरील भुरी रोग"
        classId.contains("Northern_Leaf_Blight", ignoreCase = true) -> "मक्याचा मोठा करपा"
        classId.contains("Common_rust", ignoreCase = true) -> "मक्याचा तांबेरा"
        classId.contains("Citrus___Canker", ignoreCase = true) -> "लिंबावरील खैऱ्या (कँकर)"
        classId.contains("Downy_mildew", ignoreCase = true) -> "द्राक्षावरील केवडा"
        classId.contains("Apple_scab", ignoreCase = true) -> "सफरचंदाचा स्कॅब"
        else -> null
    }

    private fun getPunjabiDiseaseName(classId: String): String? = when {
        classId.contains("healthy", ignoreCase = true) -> "ਤੰਦਰੁਸਤ ਫ਼ਸਲ"
        classId == "Rice___Blast" || classId == "Rice___Leaf_blast" -> "ਝੋਨੇ ਦਾ ਝੁਲਸ ਰੋਗ (ਬਲਾਸਟ)"
        classId == "Rice___Bacterial_blight" || classId == "Rice___Bacterial_leaf_blight" -> "ਬੈਕਟੀਰੀਅਲ ਪੱਤਾ ਝੁਲਸ (BLB)"
        classId == "Rice___Brown_spot" -> "ਭੂਰੇ ਧੱਬੇ ਰੋਗ"
        classId == "Rice___Sheath_blight" -> "ਸ਼ੀਥ ਬਲਾਈਟ (ਤਣਾ ਝੁਲਸ)"
        classId == "Wheat___Yellow_rust" || classId == "Wheat___Stripe_rust" -> "ਪੀਲੀ ਕੁੰਗੀ (ਹਲਦੀ ਰੋਗ)"
        classId == "Wheat___Brown_rust" || classId == "Wheat___Leaf_rust" -> "ਭੂਰੀ ਕੁੰਗੀ ਰੋਗ"
        classId == "Wheat___Stem_rust" -> "ਕਾਲੀ ਕੁੰਗੀ ਰੋਗ"
        classId == "Wheat___Powdery_mildew" -> "ਚਿੱਟਾ ਪਾਊਡਰੀ ਰੋਗ"
        classId == "Potato___Late_blight" -> "ਆਲੂ ਦਾ ਪਿਛੇਤਾ ਝੁਲਸ"
        classId == "Potato___Early_blight" -> "ਆਲੂ ਦਾ ਅਗੇਤਾ ਝੁਲਸ"
        classId == "Tomato___Late_blight" -> "ਟਮਾਟਰ ਦਾ ਪਿਛੇਤਾ ਝੁਲਸ"
        classId == "Tomato___Early_blight" -> "ਟਮਾਟਰ ਦਾ ਅਗੇਤਾ ਝੁਲਸ"
        classId == "Cotton___Leaf_curl" -> "ਪੱਤਾ ਮਰੋੜ ਰੋਗ (CLCuD)"
        classId == "Cotton___Bacterial_blight" -> "ਬੈਕਟੀਰੀਅਲ ਝੁਲਸ ਰੋਗ"
        classId == "Sugarcane___Red_rot" -> "ਗੰਨੇ ਦਾ ਰੱਤਾ ਰੋਗ (ਰੈੱਡ ਰੋਟ)"
        classId == "Sugarcane___Smut" -> "ਕਾਂਗਿਆਰੀ ਰੋਗ (ਸਮੱਟ)"
        classId == "Chilli___Leaf_curl" -> "ਮਿਰਚ ਦਾ ਮਰੋੜੀਆ ਰੋਗ"
        classId == "Chilli___Anthracnose" -> "ਫਲ ਸੜਨ ਰੋਗ"
        classId == "Mustard___White_rust" -> "ਚਿੱਟੀ ਕੁੰਗੀ (ਵ੍ਹਾਈਟ ਰਸਟ)"
        classId == "Mustard___Alternaria_blight" -> "ਅਲਟਰਨੇਰੀਆ ਝੁਲਸ"
        else -> null
    }

    private fun getGujaratiDiseaseName(classId: String): String? = when {
        classId.contains("healthy", ignoreCase = true) -> "તંદુરસ્ત પાક"
        classId == "Rice___Blast" || classId == "Rice___Leaf_blast" -> "ડાંગરનો ગેરુ / બ્લાસ્ટ રોગ"
        classId == "Rice___Bacterial_blight" || classId == "Rice___Bacterial_leaf_blight" -> "બેક્ટેરિયલ સુકારો (BLB)"
        classId == "Rice___Brown_spot" -> "કથ્થઈ ટપકાંનો રોગ"
        classId == "Wheat___Yellow_rust" || classId == "Wheat___Stripe_rust" -> "પીળો ગેરુ (હળદરિયો રોગ)"
        classId == "Wheat___Brown_rust" || classId == "Wheat___Leaf_rust" -> "કથ્થઈ ગેરુ રોગ"
        classId == "Cotton___Leaf_curl" -> "કપાસનો લીફ કર્લ વાયરસ"
        classId == "Cotton___Bacterial_blight" -> "બેક્ટેરિયલ સુકારો (બ્લેક આર્મ)"
        classId == "Sugarcane___Red_rot" -> "શેરડીનો લાલ સડો (રેડ રોટ)"
        classId == "Sugarcane___Smut" -> "શેરડીનો કાબરી રોગ (સ્મટ)"
        classId == "Chilli___Leaf_curl" -> "મરચીનો કોકડવા રોગ"
        classId == "Chilli___Anthracnose" -> "ફળનો સડો અને ડાળી સુકારો"
        classId == "Mustard___White_rust" -> "સફેદ ગેરુ (વ્હાઇટ રસ્ટ)"
        classId == "Tomato___Late_blight" -> "ટામેટાનો પાછોતરો સુકારો"
        classId == "Potato___Late_blight" -> "બટાટાનો પાછોતરો સુકારો"
        else -> null
    }

    private fun getTeluguDiseaseName(classId: String): String? = when {
        classId.contains("healthy", ignoreCase = true) -> "ఆరోగ్యకరమైన పంట"
        classId == "Rice___Blast" || classId == "Rice___Leaf_blast" -> "వరి అగ్గితెగులు (బ్లాస్ట్)"
        classId == "Rice___Bacterial_blight" || classId == "Rice___Bacterial_leaf_blight" -> "బాక్టీరియల్ ఆకు ఎండు తెగులు (BLB)"
        classId == "Rice___Brown_spot" -> "గోధుమ రంగు మచ్చ తెగులు"
        classId == "Rice___Sheath_blight" -> "మట్ట కుళ్లు తెగులు"
        classId == "Wheat___Yellow_rust" || classId == "Wheat___Stripe_rust" -> "పసుపు కుంకుమ తెగులు"
        classId == "Cotton___Leaf_curl" -> "పత్తి ఆకుముడత వైరస్"
        classId == "Chilli___Leaf_curl" -> "మిరప ఆకుముడత తెగులు"
        classId == "Chilli___Anthracnose" -> "కాయకుళ్లు తెగులు"
        classId == "Tomato___Late_blight" -> "టమోటా లేట్ బ్లైట్"
        classId == "Potato___Late_blight" -> "బంగాళాదుంప లేట్ బ్లైట్"
        else -> null
    }

    private fun getTamilDiseaseName(classId: String): String? = when {
        classId.contains("healthy", ignoreCase = true) -> "ஆரோக்கியமான பயிர்"
        classId == "Rice___Blast" || classId == "Rice___Leaf_blast" -> "நெல் குலை நோய் (பிளாஸ்ட்)"
        classId == "Rice___Bacterial_blight" || classId == "Rice___Bacterial_leaf_blight" -> "பாக்டீரியா இலை கருகல் (BLB)"
        classId == "Rice___Brown_spot" -> "பழுப்பு புள்ளி நோய்"
        classId == "Cotton___Leaf_curl" -> "பருத்தி இலைச்சுருள் நோய்"
        classId == "Sugarcane___Red_rot" -> "கரும்பு செவ்வழுகல் நோய்"
        classId == "Chilli___Leaf_curl" -> "மிளகாய் இலைச்சுருட்டல் நோய்"
        classId == "Tomato___Late_blight" -> "தக்காளி பின் பருவ கருகல்"
        classId == "Potato___Late_blight" -> "உருளைக்கிழங்கு பின் பருவ கருகல்"
        else -> null
    }
}
