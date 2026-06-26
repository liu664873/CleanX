package com.quickcleanpro.phonecleaner.presentation.theme

import androidx.compose.ui.graphics.Color

/** StorageCleaner 变体主题 — 带有 "清洁蓝" 视觉风格。 */
object StorageCleanerTheme : VariantTheme {

    override val colors =
        VariantColors(
            // ======================== Page / Surface ========================
            pageBackground = Color(0xFFF0F3F7),
            cardBackground = Color.White,
            surfaceBackground = Color(0xFFF7FAFD),
            softPanelBackground = Color.White,
            subtlePanelBackground = Color(0xFFEFF6FC),
            gradientBackgroundTop = Color(0xFFE3ECFD),
            gradientBackgroundBottom = Color(0xFFDFEBF5),

            // ======================== Primary / Accent ========================
            primary = Color(0xFF4179FC),
            primaryDeep = Color(0xFF1AA7EC),
            primarySoft = Color(0xFFEAF7FE),

            // ======================== Text ========================
            textPrimary = Color(0xFF2D3748),
            textSecondary = Color(0xFF7F8EAA),
            textMuted = Color(0xFF8190A5),
            textOnPrimary = Color.White,

            // ======================== Semantic ========================
            success = Color(0xFF19C979),
            successBackground = Color(0xFFEFFFF4),
            successBorder = Color(0xFFC5F7D4),
            warning = Color(0xFFECAD1A),
            danger = Color(0xFFFF6B3D),
            dangerHot = Color(0xFFEC521A),
            dangerSoft = Color(0xFFEE4D52),
            orange = Color(0xFFEC751A),
            orangeHot = Color(0xFFEC521A),

            // ======================== Divider / Border ========================
            divider = Color(0xFFE2EAF3),
            dividerSubtle = Color(0x0D1D2959),
            dividerNavy = Color(0x261D2959),
            dividerDeep = Color(0x332D3748),

            // ======================== Navy family ========================
            navy = Color(0xFF1D2959),
            navyMuted = Color(0xA61D2959),
            navyText = Color(0xA61D2959),
            navyOnPrimary = Color.White,

            // ======================== Tab ========================
            tabActiveBackground = Color.White,
            tabInactiveText = Color(0xFF8DA3D5),

            // ======================== Pin (AppLock) ========================
            pinSelected = Color(0xFF1D2959),
            pinUnselectedBorder = Color(0xFF66749A),
            pinError = Color(0xFFEC521A),
            pinKeyBackground = Color.White.copy(alpha = 0.65f),
            pinPlaceholderText = Color(0x591D2959),

            // ======================== Storage indicators ========================
            storageBlue = Color(0xFF2281FD),
            storageYellow = Color(0xFFFDBB22),
            storageOrange = Color(0xFFFD6F22),

            // ======================== Virus / Scan ========================
            virusTrackInactive = Color(0xFF9FA6C5),
            virusTrackLine = Color(0xFF8392A7),
            virusBackgroundCard = Color(0xFFF6F7FB),
            virusScanGradientStart = Color(0xFFFFFFFF),

            // ======================== Notification ========================
            notificationGradientStart = Color(0xFF90C5FB),
            notificationGradientEnd = Color(0xFF88C9FB),
            notificationBlueBackground = Color(0xFFDEF4FF),
            notificationBlueIcon = Color(0xFF1AA7EC),
            notificationBadgeRed = Color(0xFFFE6361),
            notificationBadgeGreen = Color(0xFF30C85A),
            notificationPromptBackground = Color(0xFFEDF5FF),
            notificationTitleText = Color(0xFF1D2959),
            notificationMutedRow = Color(0xFFE7EDF5),

            // ======================== Splash ========================
            splashTextMuted = Color(0xFFD9D9D9),
            splashButtonBg = Color(0xFFD9D9D9),

            // ======================== Permissions / Settings ========================
            permissionsDeclinedBackground = Color(0xFFFEF3F2),
            permissionsDeclinedBorder = Color(0xFFFCC5C2),
            settingsNavBackground = Color(0xFFF6F7FB),
        )

    override val dimens = VariantDimens()

    override val shapes = VariantShapes()

    override val typography = VariantTypography()
}
