import os
import sys
from reportlab.lib.pagesizes import letter
from reportlab.lib import colors
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import inch
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, KeepTogether, HRFlowable
)
from reportlab.pdfgen import canvas

class NumberedCanvas(canvas.Canvas):
    def __init__(self, *args, **kwargs):
        super(NumberedCanvas, self).__init__(*args, **kwargs)
        self._saved_page_states = []

    def showPage(self):
        self._saved_page_states.append(dict(self.__dict__))
        self._startPage()

    def save(self):
        num_pages = len(self._saved_page_states)
        for state in self._saved_page_states:
            self.__dict__.update(state)
            self.draw_page_decorations(num_pages)
            super(NumberedCanvas, self).showPage()
        super(NumberedCanvas, self).save()

    def draw_page_decorations(self, page_count):
        self.saveState()
        self.setFont("Helvetica", 8)
        self.setFillColor(colors.HexColor("#64748B"))
        
        # Header (pages > 1)
        if self._pageNumber > 1:
            self.drawString(54, 11 * inch - 36, "Fasal Drishti (फसल दृष्टि) — Smart India Hackathon (SIH) Technical Dossier")
            self.setStrokeColor(colors.HexColor("#E2E8F0"))
            self.setLineWidth(0.5)
            self.line(54, 11 * inch - 42, 8.5 * inch - 54, 11 * inch - 42)
        
        # Footer
        footer_text = f"Page {self._pageNumber} of {page_count}"
        self.drawRightString(8.5 * inch - 54, 36, footer_text)
        self.drawString(54, 36, "CONFIDENTIAL & PROPRIETARY — SIH INNOVATION PROJECT")
        self.setStrokeColor(colors.HexColor("#E2E8F0"))
        self.setLineWidth(0.5)
        self.line(54, 46, 8.5 * inch - 54, 46)
        
        self.restoreState()

def build_pdf(filename="Fasal_Drishti_AI_SIH_Project_Report.pdf"):
    doc = SimpleDocTemplate(
        filename,
        pagesize=letter,
        leftMargin=45,
        rightMargin=45,
        topMargin=54,
        bottomMargin=54
    )
    
    styles = getSampleStyleSheet()
    
    # Custom Brand Colors
    c_primary = colors.HexColor("#059669")     # Emerald Dark
    c_primary_light = colors.HexColor("#ECFDF5") # Mint Light
    c_secondary = colors.HexColor("#0D9488")   # Teal
    c_accent = colors.HexColor("#D97706")      # Amber
    c_dark = colors.HexColor("#0F172A")        # Slate Dark 900
    c_text = colors.HexColor("#334155")        # Slate 700
    c_card_bg = colors.HexColor("#F8FAFC")     # Slate 50
    c_border = colors.HexColor("#CBD5E1")      # Slate 300
    
    # Typography Styles
    title_style = ParagraphStyle(
        'CoverTitle',
        parent=styles['Heading1'],
        fontName='Helvetica-Bold',
        fontSize=24,
        leading=28,
        textColor=c_dark,
        alignment=0,
        spaceAfter=6
    )
    
    subtitle_style = ParagraphStyle(
        'CoverSubtitle',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=13,
        leading=16,
        textColor=c_primary,
        spaceAfter=15
    )
    
    h1_style = ParagraphStyle(
        'Heading1_Custom',
        parent=styles['Heading1'],
        fontName='Helvetica-Bold',
        fontSize=15,
        leading=19,
        textColor=c_dark,
        spaceBefore=14,
        spaceAfter=6,
        keepWithNext=True
    )
    
    h2_style = ParagraphStyle(
        'Heading2_Custom',
        parent=styles['Heading2'],
        fontName='Helvetica-Bold',
        fontSize=11.5,
        leading=15,
        textColor=c_secondary,
        spaceBefore=10,
        spaceAfter=4,
        keepWithNext=True
    )

    body_style = ParagraphStyle(
        'Body_Custom',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9.5,
        leading=13.5,
        textColor=c_text,
        spaceAfter=6
    )
    
    bullet_style = ParagraphStyle(
        'Bullet_Custom',
        parent=body_style,
        leftIndent=12,
        firstLineIndent=-12,
        spaceAfter=4
    )
    
    card_title_style = ParagraphStyle(
        'CardTitle',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=10,
        leading=13,
        textColor=c_dark
    )
    
    table_cell = ParagraphStyle(
        'TableCell',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=8.5,
        leading=11.5,
        textColor=c_text
    )

    table_header = ParagraphStyle(
        'TableHeader',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=9,
        leading=12,
        textColor=colors.white
    )

    story = []

    # -------------------------------------------------------------
    # COVER / HEADER BANNER
    # -------------------------------------------------------------
    banner_data = [
        [
            Paragraph("<b>SMART INDIA HACKATHON (SIH) — TECHNICAL PROJECT REPORT</b>", ParagraphStyle('B1', fontName='Helvetica-Bold', fontSize=10, textColor=c_primary)),
            Paragraph("<b>VERSION: v1.0.5 (PROD READY)</b>", ParagraphStyle('B2', fontName='Helvetica-Bold', fontSize=9, textColor=c_accent, alignment=2))
        ]
    ]
    banner_table = Table(banner_data, colWidths=[340, 180])
    banner_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), c_primary_light),
        ('PADDING', (0,0), (-1,-1), 8),
        ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
        ('BOTTOMPADDING', (0,0), (-1,-1), 8),
        ('LINEBELOW', (0,0), (-1,-1), 1.5, c_primary)
    ]))
    story.append(banner_table)
    story.append(Spacer(1, 15))

    story.append(Paragraph("🌾 Fasal Drishti (फसल दृष्टि)", title_style))
    story.append(Paragraph("AI-Driven Precision Agronomy, Offline Vision Diagnosis & Multilingual Farmer Advisory System", subtitle_style))
    
    meta_info = [
        [Paragraph("<b>Theme / Domain:</b> Agriculture, FoodTech & Rural Development", table_cell), Paragraph("<b>Target User:</b> 140M+ Indian Farmers, Agronomists & KVKs", table_cell)],
        [Paragraph("<b>Primary Innovation:</b> Dual-Tier Hybrid Vision AI (Offline Edge + Cloud LLM)", table_cell), Paragraph("<b>Languages Supported:</b> 10 Indian Regional Languages + Voice AI", table_cell)]
    ]
    meta_table = Table(meta_info, colWidths=[260, 260])
    meta_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), c_card_bg),
        ('BOX', (0,0), (-1,-1), 1, c_border),
        ('INNERGRID', (0,0), (-1,-1), 0.5, c_border),
        ('PADDING', (0,0), (-1,-1), 6)
    ]))
    story.append(meta_table)
    story.append(Spacer(1, 14))

    # -------------------------------------------------------------
    # 1. EXECUTIVE SUMMARY & PROBLEM STATEMENT
    # -------------------------------------------------------------
    story.append(Paragraph("1. Executive Summary & Problem Statement", h1_style))
    story.append(Paragraph(
        "Agriculture is the primary livelihood for over 58% of India's population. However, Indian farmers suffer an estimated annual crop yield loss of <b>₹1,50,000+ Crore</b> due to delayed pest/disease detection, unguided pesticide overuse, improper NPK fertilizer balancing, and volatile market middlemen.",
        body_style
    ))
    story.append(Paragraph(
        "<b>Core Pain Points Addressed:</b>", h2_style
    ))
    story.append(Paragraph("• <b>Lack of Rural Connectivity:</b> Most cutting-edge AI requires high-speed cloud internet. When farmers are in remote fields, cloud-only apps fail completely.", bullet_style))
    story.append(Paragraph("• <b>Language & Illiteracy Barrier:</b> 70%+ rural farmers cannot read complex chemical technical names or English apps.", bullet_style))
    story.append(Paragraph("• <b>Imbalanced Fertilizer Usage:</b> Rampant over-fertilization (excessive Urea) degrades soil health, increases input costs by 30%, and lowers produce quality.", bullet_style))
    story.append(Paragraph("• <b>Exploitative Mandi Middlemen:</b> Farmers lack real-time APMC mandi prices and market intelligence, forcing distress sales.", bullet_style))
    story.append(Paragraph(
        "<b>The Fasal Drishti Solution:</b> An offline-first, native Android ecosystem integrating <b>sub-100ms on-device TFLite MobileNetV2 vision classification</b>, secondary <b>Google Gemini & NVIDIA NIM multimodal reasoning</b>, <b>ICAR-calibrated NPK calculators</b>, <b>GPS-enabled Live Mandi rates</b>, <b>1-tap PDF doctor prescriptions</b>, and <b>10-language Voice AI</b>.",
        body_style
    ))
    story.append(Spacer(1, 10))

    # -------------------------------------------------------------
    # 2. COMPLETE TECHNICAL STACK MATRIX
    # -------------------------------------------------------------
    story.append(Paragraph("2. Complete Tech Stack & Architecture Matrix", h1_style))
    
    stack_data = [
        [Paragraph("<b>Component Layer</b>", table_header), Paragraph("<b>Technologies / Frameworks</b>", table_header), Paragraph("<b>Key Responsibilities & Capabilities</b>", table_header)],
        
        [
            Paragraph("<b>Android Frontend</b>", table_cell),
            Paragraph("Kotlin 1.9.22<br/>Jetpack Compose M3<br/>CameraX API<br/>Coroutines & Flow", table_cell),
            Paragraph("100% Declarative reactive UI, 120Hz smooth refresh rate, custom camera reticle scanner, edge-to-edge glassmorphism.", table_cell)
        ],
        [
            Paragraph("<b>Edge AI Engine<br/>(On-Device)</b>", table_cell),
            Paragraph("TensorFlow Lite 2.14<br/>MobileNetV2 Fine-Tuned<br/>NNAPI GPU Acceleration", table_cell),
            Paragraph("Offline inference in sub-100ms. 38 PlantVillage disease classes across 14 major Indian crops. Zero internet required.", table_cell)
        ],
        [
            Paragraph("<b>Cloud Reasoning<br/>(Vision-LLM)</b>", table_cell),
            Paragraph("Google Gemini 2.5 Flash<br/>NVIDIA NIM Agronomy<br/>Multi-Key Failover Vault", table_cell),
            Paragraph("Deep multimodal reasoning for complex symptoms, localized chemical dosage, organic alternatives, and prevention timeline.", table_cell)
        ],
        [
            Paragraph("<b>Backend & Cloud Database</b>", table_cell),
            Paragraph("Supabase (PostgreSQL 15)<br/>Supabase Auth (OAuth)<br/>FastAPI Microservice (Opt)", table_cell),
            Paragraph("Row-Level Security (RLS) encrypted scan sync, cloud chat history, dynamic secure API key rotation, automated update distribution.", table_cell)
        ],
        [
            Paragraph("<b>Local Storage<br/>(Offline Cache)</b>", table_cell),
            Paragraph("Room SQLite DB<br/>SharedPreferences<br/>Encrypted Local Cache", table_cell),
            Paragraph("Stores scan history, diagnosed prescriptions, and audio locally for instant retrieval with zero network connection.", table_cell)
        ],
        [
            Paragraph("<b>Voice & NLP Engine</b>", table_cell),
            Paragraph("Android STT / TTS Engine<br/>Multilingual Speech Synth<br/>Audio Waveform Canvas", table_cell),
            Paragraph("Natural voice input and audio prescription readback in 10 Indian regional languages for illiterate and hands-busy farmers.", table_cell)
        ],
        [
            Paragraph("<b>APIs & External Feeds</b>", table_cell),
            Paragraph("Agmarknet / APMC API<br/>Open-Meteo & OpenWeather<br/>iText / Android PdfDoc", table_cell),
            Paragraph("GPS-driven live market rates, 7-day hyper-local agricultural weather forecasts, and 1-tap printable PDF prescription generation.", table_cell)
        ]
    ]

    stack_table = Table(stack_data, colWidths=[100, 150, 270])
    stack_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), c_dark),
        ('ALIGN', (0,0), (-1,-1), 'LEFT'),
        ('VALIGN', (0,0), (-1,-1), 'TOP'),
        ('GRID', (0,0), (-1,-1), 0.5, c_border),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, c_card_bg]),
        ('PADDING', (0,0), (-1,-1), 5)
    ]))
    story.append(stack_table)
    story.append(Spacer(1, 12))

    story.append(PageBreak())

    # -------------------------------------------------------------
    # 3. DUAL-TIER AI INFERENCE PIPELINE
    # -------------------------------------------------------------
    story.append(Paragraph("3. Deep-Dive: Dual-Tier Hybrid AI Architecture", h1_style))
    story.append(Paragraph(
        "Fasal Drishti solves the fundamental trade-off between <b>speed/offline availability</b> and <b>deep contextual reasoning</b> using a novel Two-Tier Inference Pipeline:",
        body_style
    ))

    pipeline_steps = [
        [
            Paragraph("<b>TIER 1: ON-DEVICE EDGE VISION (Instant Offline Diagnosis)</b>", ParagraphStyle('P1', fontName='Helvetica-Bold', fontSize=9.5, textColor=c_primary)),
        ],
        [
            Paragraph(
                "• <b>Architecture:</b> Custom fine-tuned MobileNetV2 convolutional neural network (`model_4_mobilenet_finetuned.keras` converted to 8-bit quantized `.tflite`).<br/>"
                "• <b>Input:</b> 224x224x3 RGB image tensor normalized to [-1, 1].<br/>"
                "• <b>Output:</b> Softmax probability vector across 38 crop disease classes.<br/>"
                "• <b>Performance:</b> Inference completes in <b>45ms - 85ms</b> on mid-range Android devices with zero data consumed.<br/>"
                "• <b>Offline Knowledge Base:</b> Mapped to on-device disease encyclopedia containing symptoms, chemical treatments, and organic remedies in English & Hindi.",
                body_style
            )
        ],
        [
            Paragraph("<b>TIER 2: MULTIMODAL CLOUD LLM (Contextual Agronomy Reasoning)</b>", ParagraphStyle('P2', fontName='Helvetica-Bold', fontSize=9.5, textColor=c_secondary)),
        ],
        [
            Paragraph(
                "• <b>Model:</b> Google Gemini 2.5 Flash & NVIDIA NIM Agronomy Vision.<br/>"
                "• <b>Trigger:</b> Farmer connects to internet or requests deeper conversational advice.<br/>"
                "• <b>Capability:</b> Analyzes multi-leaf pest damages, environmental stress factors (sunburn, nutrient chlorosis), suggests exact tank-mix ratios (e.g. 2.5g Mancozeb / Litre water), and answers follow-up voice questions in native dialect.<br/>"
                "• <b>Dynamic API Key Vault:</b> Multi-key automated rotation stored in encrypted Supabase vault with zero downtime failover.",
                body_style
            )
        ]
    ]

    pipeline_table = Table(pipeline_steps, colWidths=[520])
    pipeline_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), c_primary_light),
        ('BACKGROUND', (0,1), (-1,1), colors.white),
        ('BACKGROUND', (0,2), (-1,2), colors.HexColor("#F0FDFA")),
        ('BACKGROUND', (0,3), (-1,3), colors.white),
        ('BOX', (0,0), (-1,-1), 1, c_border),
        ('INNERGRID', (0,0), (-1,-1), 0.5, c_border),
        ('PADDING', (0,0), (-1,-1), 7)
    ]))
    story.append(pipeline_table)
    story.append(Spacer(1, 12))

    # -------------------------------------------------------------
    # 4. COMPREHENSIVE FEATURE HIGHLIGHTS (8 PILLARS)
    # -------------------------------------------------------------
    story.append(Paragraph("4. Core Features & Capabilities Breakdown", h1_style))

    features = [
        ("1. CameraX Smart Leaf Scanner", "Live camera viewfinder with real-time target reticle, shutter animations, photo gallery picker, and flash toggle for night field inspections."),
        ("2. ICAR Fertilizer & NPK Calculator", "Scientific calculation of Urea (46% N), DAP (18:46:0), MOP Potash (60% K2O), Zinc Sulphate, and FYM compost. Supports Acre, Bigha (regional ratio), Hectare, and Guntha with 10-language popup guides."),
        ("3. AI Soil Health & pH Advisor", "Interprets soil texture (Alluvial, Black, Red, Clay, Sandy), NPK nutrient indexes, and pH scale (Acidic <6.0 to Alkaline >7.5) with corrective soil amendment prescriptions."),
        ("4. GPS-Auto Live Mandi Bhav", "Automatic GPS state detection, APMC commodity price tracker (Min, Max, Modal price / Quintal), historical price trends, and AI-powered Sell vs Hold recommendations."),
        ("5. 10-Language Multilingual Voice AI", "Voice-to-Text speech queries and Text-to-Speech audio prescription player in English, Hindi, Bengali, Marathi, Punjabi, Gujarati, Telugu, Tamil, Kannada, and Odia."),
        ("6. 1-Tap PDF Prescription & WhatsApp Share", "Instantly generates printable agronomy doctor prescription PDFs with crop photo, diagnosed disease, chemical & organic dosages, safety rules, and direct WhatsApp sharing button."),
        ("7. Sarkari Krishi Yojanaen Portal", "In-app step-by-step guidance for PM-Kisan Samman Nidhi, PM Fasal Bima Yojana (PMFBY), Kisan Credit Card (KCC), and Soil Health Card with direct official application portal links."),
        ("8. In-App Auto-Updater & Offline Sync", "Embedded GitHub release auto-updater that notifies farmers of updates, downloads APKs in the background, and syncs history to Supabase cloud whenever online.")
    ]

    for title, desc in features:
        story.append(Paragraph(f"• <b>{title}:</b> {desc}", bullet_style))

    story.append(Spacer(1, 10))

    # -------------------------------------------------------------
    # 5. COMPETITIVE ADVANTAGE & INNOVATION
    # -------------------------------------------------------------
    story.append(Paragraph("5. Competitive Advantage & Innovation Matrix", h1_style))
    
    comp_data = [
        [Paragraph("<b>Evaluation Metric</b>", table_header), Paragraph("<b>Existing Solutions (e.g. Plantix / BharatAgri)</b>", table_header), Paragraph("<b>Fasal Drishti (Our Innovation)</b>", table_header)],
        [
            Paragraph("<b>Offline Edge Inference</b>", table_cell),
            Paragraph("❌ Mostly fails without active internet connection.", table_cell),
            Paragraph("✅ <b>100% On-Device TFLite sub-100ms inference</b> with local SQLite database fallback.", table_cell)
        ],
        [
            Paragraph("<b>Regional Land Units</b>", table_cell),
            Paragraph("❌ Only standard Acres or Hectares.", table_cell),
            Paragraph("✅ <b>Acre, Bigha, Hectare & Guntha</b> with calibrated regional conversion factors.", table_cell)
        ],
        [
            Paragraph("<b>Doctor Prescription</b>", table_cell),
            Paragraph("❌ Simple text output on screen.", table_cell),
            Paragraph("✅ <b>Formatted PDF Doctor Slip</b> with QR verification and 1-tap WhatsApp sharing.", table_cell)
        ],
        [
            Paragraph("<b>Multilingual Voice AI</b>", table_cell),
            Paragraph("❌ Text-only in limited languages.", table_cell),
            Paragraph("✅ <b>Full STT & TTS Voice Engine</b> across 10 Indian Regional Languages.", table_cell)
        ],
        [
            Paragraph("<b>Mandi Price Integration</b>", table_cell),
            Paragraph("❌ Disconnected or manual state search.", table_cell),
            Paragraph("✅ <b>GPS Auto-Location detection</b> + Agmarknet real-time APMC mandi sync.", table_cell)
        ]
    ]

    comp_table = Table(comp_data, colWidths=[120, 190, 210])
    comp_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), c_dark),
        ('VALIGN', (0,0), (-1,-1), 'TOP'),
        ('GRID', (0,0), (-1,-1), 0.5, c_border),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, c_card_bg]),
        ('PADDING', (0,0), (-1,-1), 5)
    ]))
    story.append(comp_table)

    story.append(PageBreak())

    # -------------------------------------------------------------
    # 6. SOCIO-ECONOMIC IMPACT & VIABILITY
    # -------------------------------------------------------------
    story.append(Paragraph("6. Socio-Economic Impact & Scalability", h1_style))
    
    impact_items = [
        [
            Paragraph("<b>Metric</b>", table_header),
            Paragraph("<b>Projected Farmer Impact</b>", table_header),
            Paragraph("<b>National Agriculture Benefit</b>", table_header)
        ],
        [
            Paragraph("<b>Crop Loss Reduction</b>", table_cell),
            Paragraph("<b>18% - 30% yield protection</b> via early detection of blight, rust, and pest infestation.", table_cell),
            Paragraph("Saves crores in national food grain losses and stabilizes food security.", table_cell)
        ],
        [
            Paragraph("<b>Input Cost Optimization</b>", table_cell),
            Paragraph("<b>20% - 25% cost reduction</b> by eliminating unnecessary chemical spraying and over-fertilization.", table_cell),
            Paragraph("Reduces soil nitrate toxicity, groundwater contamination, and chemical runoff.", table_cell)
        ],
        [
            Paragraph("<b>Fair Price Realization</b>", table_cell),
            Paragraph("<b>10% - 15% better crop revenue</b> by tracking Live Mandi rates before selling.", table_cell),
            Paragraph("Empowers smallholder farmers against exploitative middleman commission agents.", table_cell)
        ]
    ]
    impact_table = Table(impact_items, colWidths=[120, 200, 200])
    impact_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), c_dark),
        ('GRID', (0,0), (-1,-1), 0.5, c_border),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, c_card_bg]),
        ('PADDING', (0,0), (-1,-1), 5)
    ]))
    story.append(impact_table)
    story.append(Spacer(1, 14))

    # -------------------------------------------------------------
    # 7. HACKATHON JURY Q&A CHEATSHEET
    # -------------------------------------------------------------
    story.append(Paragraph("7. Smart India Hackathon Jury Q&A Cheatsheet", h1_style))
    
    qa_list = [
        ("Q1: What happens if there is no internet in the farmer's remote village?",
         "Fasal Drishti's core vision classifier runs completely on-device using TensorFlow Lite and MobileNetV2 in under 100ms. All primary symptoms, chemical treatments, and organic remedies are stored in the local SQLite database. Internet is only required for secondary Gemini cloud reasoning or live Mandi updates."),
        
        ("Q2: How does your app ensure high accuracy and avoid false diagnoses?",
         "We utilize a two-tier verification mechanism: The local TFLite model provides immediate classification confidence score. If confidence is below 75% or complex co-infections are suspected, the app seamlessly escalates the image to Google Gemini 2.5 Flash / NVIDIA NIM for multi-modal visual reasoning and cross-validation."),
        
        ("Q3: How do you handle language diversity among Indian farmers?",
         "The app is natively localized in 10 major Indian languages (Hindi, Bengali, Marathi, Punjabi, Gujarati, Telugu, Tamil, Kannada, Odia, English). It includes an interactive Voice Assistant with Speech-to-Text and Audio Playback so farmers who cannot read can simply speak and listen to the remedy."),
         
        ("Q4: Is the fertilizer dosage accurate for different farm sizes?",
         "Yes. Unlike generic calculators, our NPK engine uses ICAR standard crop nutrient absorption constants (Urea 46% N, DAP 18:46:0, MOP 60% K2O, Zinc, FYM) and converts native land units (Acre, Bigha, Hectare, Guntha) with localized precision math."),
         
        ("Q5: What is your business model and deployment strategy?",
         "B2G & B2B hybrid model: Partnering with State Agriculture Departments, Krishi Vigyan Kendras (KVKs), and FPOs (Farmer Producer Organizations) for subsidized rollout, alongside premium API services for Agri-input manufacturers and pesticide distributors.")
    ]

    for q, a in qa_list:
        story.append(Paragraph(f"<b>{q}</b>", h2_style))
        story.append(Paragraph(f"<b>Answer:</b> {a}", body_style))
        story.append(Spacer(1, 4))

    story.append(Spacer(1, 10))

    # -------------------------------------------------------------
    # 8. FUTURE ROADMAP & SCALING
    # -------------------------------------------------------------
    story.append(Paragraph("8. Future Roadmap & Technology Extensions", h1_style))
    story.append(Paragraph("• <b>Multispectral Satellite NDVI Integration:</b> Sentinel-2 satellite imagery ingestion for farm-level vegetative health & drought indices.", bullet_style))
    story.append(Paragraph("• <b>IoT Soil Moisture & Sensor Telemetry:</b> Bluetooth/LoRaWAN connection to on-field soil probes for live automated NPK & pH data.", bullet_style))
    story.append(Paragraph("• <b>Autonomous Drone Spraying Interface:</b> Geo-tagging diseased crop patches for precision drone pesticide delivery.", bullet_style))
    story.append(Paragraph("• <b>FPO Community Marketplace:</b> Direct farmer-to-buyer bulk trading platform eliminating traditional mandi intermediaries.", bullet_style))

    story.append(Spacer(1, 15))
    story.append(HRFlowable(width="100%", thickness=1, color=c_border, spaceBefore=5, spaceAfter=10))
    story.append(Paragraph("<b>Project Lead & Developer:</b> Rupam Das | <b>Repository:</b> github.com/Rupam852/Fasal-Drishti-AI", ParagraphStyle('F1', fontName='Helvetica-Bold', fontSize=9, textColor=c_primary, alignment=1)))
    story.append(Paragraph("Smart India Hackathon (SIH) — Innovation for Atmanirbhar Krishi 🇮🇳", ParagraphStyle('F2', fontName='Helvetica', fontSize=8.5, textColor=c_text, alignment=1)))

    doc.build(story, canvasmaker=NumberedCanvas)
    print(f"Successfully generated SIH Technical Report PDF: {filename}")

if __name__ == '__main__':
    build_pdf()
