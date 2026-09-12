// Indian Agricultural Mock & Fallback Data for Fasal Drishti AI Command Center

export const INITIAL_STATS = {
  totalFarmers: 14850,
  farmersGrowth: '+18.4%',
  totalScans: 62410,
  scansToday: 842,
  scansGrowth: '+24.1%',
  aiAccuracy: '98.4%',
  healthyRatio: '38.2%',
  diseasedRatio: '61.8%',
  activeOutbreaks: 7,
  resolvedIssues: 1240,
};

export const CROP_DISTRIBUTION = [
  { name: 'Tomato (टमाटर)', value: 34, color: '#ef4444' },
  { name: 'Potato (आलू)', value: 24, color: '#f59e0b' },
  { name: 'Wheat (गेहूं)', value: 18, color: '#10b981' },
  { name: 'Rice / Paddy (धान)', value: 12, color: '#06b6d4' },
  { name: 'Chilli & Pepper (मिर्च)', value: 7, color: '#ec4899' },
  { name: 'Others (Corn, Apple, Cotton)', value: 5, color: '#8b5cf6' },
];

export const WEEKLY_SCAN_TRENDS = [
  { day: 'Mon', scans: 720, healthy: 280, diseased: 440, aiConfidence: 97.8 },
  { day: 'Tue', scans: 890, healthy: 340, diseased: 550, aiConfidence: 98.1 },
  { day: 'Wed', scans: 1120, healthy: 430, diseased: 690, aiConfidence: 98.5 },
  { day: 'Thu', scans: 980, healthy: 390, diseased: 590, aiConfidence: 98.2 },
  { day: 'Fri', scans: 1340, healthy: 510, diseased: 830, aiConfidence: 98.7 },
  { day: 'Sat', scans: 1560, healthy: 600, diseased: 960, aiConfidence: 98.9 },
  { day: 'Sun', scans: 1420, healthy: 550, diseased: 870, aiConfidence: 98.6 },
];

export const INDIA_OUTBREAK_ZONES = [
  {
    id: 'zone-1',
    state: 'Punjab & Haryana',
    region: 'Ludhiana, Karnal & Patiala',
    crop: 'Wheat (गेहूं)',
    disease: 'Yellow Rust (Puccinia striiformis)',
    riskLevel: 'Critical',
    affectedFarms: '2,400+ acres',
    coordinates: { x: 32, y: 25 },
    recommendation: 'Spray Propiconazole 25% EC @ 1ml/L immediately. Avoid excess nitrogen.'
  },
  {
    id: 'zone-2',
    state: 'Uttar Pradesh',
    region: 'Agra, Aligarh & Kanpur',
    crop: 'Potato (आलू)',
    disease: 'Late Blight (Phytophthora infestans)',
    riskLevel: 'Critical',
    affectedFarms: '4,100+ acres',
    coordinates: { x: 44, y: 38 },
    recommendation: 'Apply Cymoxanil 8% + Mancozeb 64% WP @ 2.5g/L on urgent basis.'
  },
  {
    id: 'zone-3',
    state: 'Maharashtra',
    region: 'Nashik, Pune & Ahmednagar',
    crop: 'Tomato (टमाटर)',
    disease: 'Early Blight & Leaf Curl Virus',
    riskLevel: 'Warning',
    affectedFarms: '1,850+ acres',
    coordinates: { x: 30, y: 60 },
    recommendation: 'Install yellow sticky traps (15/acre) and spray Azoxystrobin @ 1ml/L.'
  },
  {
    id: 'zone-4',
    state: 'West Bengal',
    region: 'Burdwan, Hooghly & Nadia',
    crop: 'Rice / Paddy (धान)',
    disease: 'Bacterial Leaf Blight (BLB)',
    riskLevel: 'Warning',
    affectedFarms: '3,200+ acres',
    coordinates: { x: 74, y: 48 },
    recommendation: 'Drain standing water for 3 days; apply Streptocycline @ 0.5g/10L + Copper Oxychloride @ 2.5g/L.'
  },
  {
    id: 'zone-5',
    state: 'Rajasthan',
    region: 'Ganganagar & Bikaner',
    crop: 'Cotton & Mustard',
    disease: 'Whitefly Infestation & Leaf Curl',
    riskLevel: 'Moderate',
    affectedFarms: '1,100+ acres',
    coordinates: { x: 24, y: 36 },
    recommendation: 'Neem Oil 10,000 PPM @ 3ml/L + Diafenthiuron 50% WP @ 1.2g/L.'
  },
  {
    id: 'zone-6',
    state: 'Andhra Pradesh & Telangana',
    region: 'Guntur & Warangal',
    crop: 'Chilli (लाल मिर्च)',
    disease: 'Black Thrips & Dieback',
    riskLevel: 'Critical',
    affectedFarms: '2,900+ acres',
    coordinates: { x: 50, y: 72 },
    recommendation: 'Spinetoram 11.7% SC @ 1ml/L or Fipronil 5% SC @ 2ml/L.'
  },
];

export const MOCK_SCANS = [
  {
    id: 'scan-fd-8901',
    farmerName: 'Rameshwar Patel',
    farmerPhone: '+91 98765 43210',
    location: 'Indore, Madhya Pradesh',
    cropName: 'Tomato',
    diseaseName: 'Tomato Early Blight (Alternaria solani)',
    confidence: 0.984,
    severity: 'High',
    status: 'Verified by Agronomist',
    imageUrl: 'https://images.unsplash.com/photo-1592841200221-a6898f307baa?auto=format&fit=crop&w=600&q=80',
    symptoms: 'Concentric dark brown rings with chlorotic yellow halo on lower leaves.',
    treatment: 'Spray Mancozeb 75% WP @ 2.5g/L water. Repeat after 7 days.',
    createdAt: '10 mins ago',
    verified: true
  },
  {
    id: 'scan-fd-8902',
    farmerName: 'Gurpreet Singh',
    farmerPhone: '+91 98140 11223',
    location: 'Ludhiana, Punjab',
    cropName: 'Wheat',
    diseaseName: 'Wheat Yellow Rust (Puccinia striiformis)',
    confidence: 0.971,
    severity: 'Critical',
    status: 'Pending Review',
    imageUrl: 'https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b?auto=format&fit=crop&w=600&q=80',
    symptoms: 'Linear yellow pustules arranged in parallel stripes along leaf veins.',
    treatment: 'Tilt / Propiconazole 25% EC @ 1ml per liter water.',
    createdAt: '28 mins ago',
    verified: false
  },
  {
    id: 'scan-fd-8903',
    farmerName: 'Subhasish Mondal',
    farmerPhone: '+91 94331 88765',
    location: 'Burdwan, West Bengal',
    cropName: 'Rice',
    diseaseName: 'Rice Blast (Magnaporthe oryzae)',
    confidence: 0.965,
    severity: 'High',
    status: 'Verified by Agronomist',
    imageUrl: 'https://images.unsplash.com/photo-1536939459926-301728717817?auto=format&fit=crop&w=600&q=80',
    symptoms: 'Spindle-shaped diamond lesions with greyish center and reddish-brown borders.',
    treatment: 'Tricyclazole 75% WP @ 0.6g/L water at early tillering stage.',
    createdAt: '1 hour ago',
    verified: true
  },
  {
    id: 'scan-fd-8904',
    farmerName: 'Dnyaneshwar Shinde',
    farmerPhone: '+91 99220 54321',
    location: 'Nashik, Maharashtra',
    cropName: 'Potato',
    diseaseName: 'Potato Late Blight (Phytophthora infestans)',
    confidence: 0.992,
    severity: 'Critical',
    status: 'Emergency Alert Sent',
    imageUrl: 'https://images.unsplash.com/photo-1518977676601-b53f82aba655?auto=format&fit=crop&w=600&q=80',
    symptoms: 'Water-soaked irregular black lesions on leaf tips with white mildew under moist weather.',
    treatment: 'Metalaxyl 8% + Mancozeb 64% WP @ 2.5g/L. Repeat in 5 days if humidity > 85%.',
    createdAt: '2 hours ago',
    verified: true
  },
  {
    id: 'scan-fd-8905',
    farmerName: 'Anil Kumar Yadav',
    farmerPhone: '+91 97980 65432',
    location: 'Varanasi, Uttar Pradesh',
    cropName: 'Tomato',
    diseaseName: 'Tomato Leaf Healthy',
    confidence: 0.998,
    severity: 'Healthy',
    status: 'Auto Confirmed',
    imageUrl: 'https://images.unsplash.com/photo-1592841200221-a6898f307baa?auto=format&fit=crop&w=600&q=80',
    symptoms: 'Lush green foliage, no fungal, bacterial or viral lesions found.',
    treatment: 'Continue balanced NPK fertigation (19:19:19) and preventive bio-spray.',
    createdAt: '3 hours ago',
    verified: true
  },
  {
    id: 'scan-fd-8906',
    farmerName: 'Venkat Reddy',
    farmerPhone: '+91 98480 33445',
    location: 'Guntur, Andhra Pradesh',
    cropName: 'Chilli',
    diseaseName: 'Chilli Leaf Curl Virus (Begomovirus)',
    confidence: 0.958,
    severity: 'High',
    status: 'Pending Review',
    imageUrl: 'https://images.unsplash.com/photo-1588252303782-cb80119abd6d?auto=format&fit=crop&w=600&q=80',
    symptoms: 'Upward curling of leaves, stunted growth, puckering and thickened veins.',
    treatment: 'Control whitefly vector using Acetamiprid 20% SP @ 0.5g/L + Neem oil 5ml/L.',
    createdAt: '4 hours ago',
    verified: false
  }
];

export const MOCK_MANDI_RATES = [
  {
    id: 'mandi-1',
    state: 'Maharashtra',
    district: 'Nashik',
    market: 'Lasalgaon APMC',
    commodity: 'Tomato (टमाटर)',
    variety: 'Hybrid Desi Red',
    minPrice: 1800,
    maxPrice: 2650,
    modalPrice: 2350,
    unit: '₹/Quintal',
    trend: 'up',
    change: '+₹150',
    arrivalQty: '480 Tonnes',
    lastUpdated: 'Today, 06:30 AM'
  },
  {
    id: 'mandi-2',
    state: 'Delhi (NCT)',
    district: 'North Delhi',
    market: 'Azadpur Mandi',
    commodity: 'Potato (आलू)',
    variety: 'Jyoti / Pukhraj',
    minPrice: 1350,
    maxPrice: 1900,
    modalPrice: 1720,
    unit: '₹/Quintal',
    trend: 'down',
    change: '-₹40',
    arrivalQty: '1,250 Tonnes',
    lastUpdated: 'Today, 07:15 AM'
  },
  {
    id: 'mandi-3',
    state: 'Punjab',
    district: 'Khanna',
    market: 'Khanna Grain Market',
    commodity: 'Wheat (गेहूं)',
    variety: 'HD-2967 / Sharbati',
    minPrice: 2275,
    maxPrice: 2450,
    modalPrice: 2380,
    unit: '₹/Quintal',
    trend: 'up',
    change: '+₹25 (Above MSP)',
    arrivalQty: '3,100 Tonnes',
    lastUpdated: 'Today, 08:00 AM'
  },
  {
    id: 'mandi-4',
    state: 'Andhra Pradesh',
    district: 'Guntur',
    market: 'Guntur Mirchi Yard',
    commodity: 'Red Chilli (सूखी मिर्च)',
    variety: 'Teja / 334 Sannam',
    minPrice: 18500,
    maxPrice: 24000,
    modalPrice: 21500,
    unit: '₹/Quintal',
    trend: 'up',
    change: '+₹600',
    arrivalQty: '35,000 Bags',
    lastUpdated: 'Today, 09:10 AM'
  },
  {
    id: 'mandi-5',
    state: 'West Bengal',
    district: 'Purba Bardhaman',
    market: 'Memari APMC',
    commodity: 'Paddy / Rice (धान)',
    variety: 'Swarna / Minikit',
    minPrice: 2183,
    maxPrice: 2350,
    modalPrice: 2260,
    unit: '₹/Quintal',
    trend: 'stable',
    change: '₹0',
    arrivalQty: '890 Tonnes',
    lastUpdated: 'Today, 06:45 AM'
  },
  {
    id: 'mandi-6',
    state: 'Madhya Pradesh',
    district: 'Indore',
    market: 'Indore Choithram Mandi',
    commodity: 'Soybean (सोयाबीन)',
    variety: 'JS-9560 Yellow',
    minPrice: 4200,
    maxPrice: 4850,
    modalPrice: 4620,
    unit: '₹/Quintal',
    trend: 'up',
    change: '+₹110',
    arrivalQty: '2,400 Tonnes',
    lastUpdated: 'Today, 07:30 AM'
  }
];

export const MOCK_BROADCASTS = [
  {
    id: 'bc-1',
    title: '⚠️ Unseasonal Rain & Hailstorm Alert for North India',
    message: 'Heavy rain & thunderstorms predicted across Punjab, Haryana, and West UP in the next 48 hours. Farmers are advised to postpone pesticide sprays and ensure proper drainage in potato & wheat fields.',
    targetRegion: 'Punjab, Haryana, Uttar Pradesh',
    severity: 'High Alert',
    sentAt: '2026-09-12 14:30',
    recipientsCount: 8450,
    status: 'Delivered'
  },
  {
    id: 'bc-2',
    title: '🌾 PM-Kisan 19th Installment Released: Check Status in App',
    message: 'The 19th installment under PM-Kisan Samman Nidhi has been credited to registered farmers bank accounts. Check your e-KYC status directly from the Government Schemes section.',
    targetRegion: 'All India',
    severity: 'Info',
    sentAt: '2026-09-10 10:00',
    recipientsCount: 14850,
    status: 'Delivered'
  }
];

export const AI_ENGINES_CONFIG = [
  {
    id: 'gemini-flash',
    name: 'Google Gemini 2.5 Flash Vision',
    provider: 'Google AI Cloud',
    purpose: 'Realtime Agronomist Chat & High-Speed Prescription Generation',
    status: 'Active & Healthy',
    latency: '340ms',
    uptime: '99.98%',
    totalCallsToday: 4120,
    keyConfigKey: 'gemini_api_key'
  },
  {
    id: 'nvidia-nim',
    name: 'NVIDIA NIM (Llama-3.2-11b-Vision)',
    provider: 'NVIDIA AI Foundation',
    purpose: 'Deep Agronomic Diagnostic Engine & Multi-Turn Consultations',
    status: 'Active & Healthy',
    latency: '410ms',
    uptime: '99.92%',
    totalCallsToday: 3890,
    keyConfigKey: 'nvidia_nim_api_key'
  },
  {
    id: 'mobilenet-tflite',
    name: 'On-Device MobileNetV2 + TFLite (38 Classes)',
    provider: 'Local Edge AI',
    purpose: 'Instant Offline Zero-Latency Disease Classification',
    status: 'Active (On Device)',
    latency: '42ms',
    uptime: '100%',
    totalCallsToday: 18450,
    keyConfigKey: 'on_device_model'
  }
];
