package com.sila.messaging.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Sila color system — a single source of truth for every color used across
 * the app. No screen or component should hardcode a hex value; everything
 * should flow from here through [MaterialTheme.colorScheme] (see Theme.kt).
 *
 * Palette identity: a deep indigo-blue primary (distinct from stock Material
 * purple and from plain "Telegram blue"), paired with a warm violet accent
 * for highlights/badges — evokes a premium, international messaging product.
 */

// ---- Brand ----
val SilaPrimaryLight = Color(0xFF3E63DD)
val SilaPrimaryDark = Color(0xFF7C9BFF)

val SilaSecondaryLight = Color(0xFF2AB6A6)
val SilaSecondaryDark = Color(0xFF5FD9C9)

val SilaAccentLight = Color(0xFF8B5CF6)
val SilaAccentDark = Color(0xFFB79CFF)

// ---- Semantic ----
val SilaSuccess = Color(0xFF2FBE71)
val SilaSuccessDark = Color(0xFF57D98D)
val SilaWarning = Color(0xFFE8A33D)
val SilaWarningDark = Color(0xFFF2BE6B)
val SilaError = Color(0xFFE5484D)
val SilaErrorDark = Color(0xFFFF6B70)

// ---- Neutrals — Light ----
val SilaBackgroundLight = Color(0xFFFAFAFC)
val SilaSurfaceLight = Color(0xFFFFFFFF)
val SilaSurfaceVariantLight = Color(0xFFF0F1F5)
val SilaCardLight = Color(0xFFF5F6FA)
val SilaDividerLight = Color(0xFFE7E8EE)
val SilaTextPrimaryLight = Color(0xFF14151A)
val SilaTextSecondaryLight = Color(0xFF6B6E79)

// ---- Neutrals — Dark ----
val SilaBackgroundDark = Color(0xFF0D0E12)
val SilaSurfaceDark = Color(0xFF17181D)
val SilaSurfaceVariantDark = Color(0xFF1F2026)
val SilaCardDark = Color(0xFF1B1C22)
val SilaDividerDark = Color(0xFF2A2B33)
val SilaTextPrimaryDark = Color(0xFFF1F1F4)
val SilaTextSecondaryDark = Color(0xFF9A9CA8)

// ---- Message bubbles ----
val SilaBubbleSentLight = Color(0xFFDCE6FF)
val SilaBubbleReceivedLight = Color(0xFFF0F1F5)
val SilaBubbleSentDark = Color(0xFF27345C)
val SilaBubbleReceivedDark = Color(0xFF23242B)

// ---- Presence / status ----
val SilaOnlineDot = Color(0xFF34C759)
val SilaAwayDot = Color(0xFFE8A33D)
