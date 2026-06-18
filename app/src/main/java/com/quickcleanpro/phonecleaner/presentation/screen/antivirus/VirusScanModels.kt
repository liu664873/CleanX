package com.quickcleanpro.phonecleaner.presentation.screen.antivirus

import android.graphics.drawable.Drawable

enum class VirusScanMode(
    val minDurationMillis: Long,
    val stepCount: Int,
    val displayUpdateIntervalMillis: Long
) {
    Quick(minDurationMillis = 7_000L, stepCount = 3, displayUpdateIntervalMillis = 80L),
    Deep(minDurationMillis = 15_000L, stepCount = 4, displayUpdateIntervalMillis = 40L);

    fun circleStartThreshold(circleIndex: Int): Float {
        val circleDiameter = 56f
        val trackWidth = 317f
        val trackInset = 4f
        val circleLeft =
            if (stepCount <= 1) {
                0f
            } else {
                (trackWidth - circleDiameter) * circleIndex / (stepCount - 1).toFloat()
            }
        return ((circleLeft - trackInset) / (trackWidth - trackInset * 2f)).coerceIn(0f, 1f)
    }

    fun circleFillThreshold(circleIndex: Int): Float {
        val circleDiameter = 56f
        val trackWidth = 317f
        val trackInset = 4f
        val circleLeft =
            if (stepCount <= 1) {
                0f
            } else {
                (trackWidth - circleDiameter) * circleIndex / (stepCount - 1).toFloat()
            }
        val circleRight = circleLeft + circleDiameter
        return ((circleRight - trackInset) / (trackWidth - trackInset * 2f)).coerceIn(0f, 1f)
    }
}

data class VirusThreat(
    val id: String,
    val packageName: String?,
    val apkPath: String?,
    val title: String,
    val description: String,
    val isFile: Boolean,
    val icon: Drawable?
)

data class VirusScanUiState(
    val mode: VirusScanMode? = null,
    val isScanning: Boolean = false,
    val scanCompleted: Boolean = false,
    val hasAdbRisk: Boolean = false,
    val isPathMode: Boolean = false,
    val currentLabel: String = "",
    val currentIcon: Drawable? = null,
    val threats: List<VirusThreat> = emptyList(),
    val appThreatCount: Int = 0,
    val fileThreatCount: Int = 0,
    val progressFraction: Float = 0f,
    val errorMessage: String? = null
) {
    val effectiveThreatCount: Int
        get() = threats.size + if (hasAdbRisk) 1 else 0
}
