package com.fasaldrishti.app.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// 🌿 RADIANT BIO-TECH ECO PALETTE
// ==========================================

// Neon & Emerald Accents
val EmeraldPrimary = Color(0xFF00E676)          // Radiant Neon Emerald
val EmeraldPrimaryVariant = Color(0xFF10B981)   // Crisp Mint Emerald
val EmeraldDark = Color(0xFF059669)             // Deep Forest Emerald
val EmeraldContainerLight = Color(0xFFD1FAE5)   // Soft Mint Container
val EmeraldContainerDark = Color(0xFF064E3B)    // Deep Glass Container

// Solar Amber & Orange (Moderate / Warnings)
val SolarGold = Color(0xFFF59E0B)               // Electric Solar Gold
val SolarGoldVariant = Color(0xFFFBBF24)        // Warm Sunlight
val SolarContainerLight = Color(0xFFFEF3C7)     // Warm Amber Tint
val SolarContainerDark = Color(0xFF78350F)      // Dark Amber Glass

// Crimson Coral (Severe / Urgent Alert)
val CrimsonCoral = Color(0xFFEF4444)            // Bio-Hazard Red
val CrimsonCoralVariant = Color(0xFFF43F5E)     // Vivid Rose Red
val CrimsonContainerLight = Color(0xFFFEE2E2)   // Soft Rose Tint
val CrimsonContainerDark = Color(0xFF7F1D1D)    // Dark Rose Glass

// Healthy Leaf Neon
val NeonLime = Color(0xFF84CC16)                // High-Vibe Green
val NeonLimeContainer = Color(0xFFECFCCB)

// ==========================================
// 🌌 DEEP OBSIDIAN DARK THEME (OLED Glass)
// ==========================================
val ObsidianVoid = Color(0xFF070B09)            // Deepest OLED Black
val ObsidianSurface = Color(0xFF0E1612)         // Acrylic Surface
val ObsidianElevated = Color(0xFF16221C)        // Raised Frosted Card
val ObsidianCardBorder = Color(0xFF23352C)      // 1px Subtle Glass Border
val ObsidianGlassHighlight = Color(0x3300E676)  // Neon Ambient Glow

// Dark Mode Text
val TextPrimaryDark = Color(0xFFF3F4F6)         // Crisp Bright White
val TextSecondaryDark = Color(0xFF9CA3AF)       // Soft Slate Grey
val TextMutedDark = Color(0xFF6B7280)           // Dark Muted Grey

// ==========================================
// ❄️ PEARLESCENT FROST LIGHT THEME
// ==========================================
val FrostBackground = Color(0xFFF4F8F5)         // Pristine Nature White
val FrostSurface = Color(0xFFFFFFFF)            // Pure White Card
val FrostElevated = Color(0xFFE8F2EC)           // Soft Sage Acrylic
val FrostCardBorder = Color(0xFFD1E3D8)         // Soft Sage Outline

// Light Mode Text
val TextPrimaryLight = Color(0xFF0F172A)        // Deep Charcoal Slate
val TextSecondaryLight = Color(0xFF475569)      // Cool Slate
val TextMutedLight = Color(0xFF94A3B8)          // Soft Slate

// ==========================================
// 🌈 FUTURISTIC GRADIENTS
// ==========================================
val EmeraldGradientStart = Color(0xFF00E676)
val EmeraldGradientEnd = Color(0xFF059669)

val ObsidianGradientStart = Color(0xFF0E1612)
val ObsidianGradientEnd = Color(0xFF16221C)

val SolarGradientStart = Color(0xFFF59E0B)
val SolarGradientEnd = Color(0xFFD97706)

val CrimsonGradientStart = Color(0xFFEF4444)
val CrimsonGradientEnd = Color(0xFFBE123C)

// Backward Compatibility Aliases
val GreenPrimary = EmeraldPrimaryVariant
val GreenOnPrimary = Color.White
val GreenPrimaryContainer = EmeraldContainerLight
val GreenOnPrimaryContainer = Color(0xFF064E3B)
val GreenSecondary = EmeraldDark
val GreenSecondaryContainer = EmeraldContainerLight
val AmberAccent = SolarGold
val AmberContainer = SolarContainerLight
val RedSevere = CrimsonCoral
val RedSevereContainer = CrimsonContainerLight
val BackgroundLight = FrostBackground
val SurfaceLight = FrostSurface
val SurfaceVariantLight = FrostElevated
val BackgroundDark = ObsidianVoid
val SurfaceDark = ObsidianSurface
val SurfaceElevatedDark = ObsidianElevated
val PrimaryGradientStart = EmeraldGradientStart
val PrimaryGradientEnd = EmeraldGradientEnd
val AmberGradientStart = SolarGradientStart
val AmberGradientEnd = SolarGradientEnd
