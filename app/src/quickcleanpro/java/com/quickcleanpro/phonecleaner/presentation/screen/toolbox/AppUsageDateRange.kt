package com.quickcleanpro.phonecleaner.presentation.screen.toolbox

import androidx.annotation.StringRes
import com.quickcleanpro.phonecleaner.R
import java.util.Calendar

/**
 * App Usage 椤甸潰鏀寔鐨勬棩鏈熻寖鍥淬€?
 *
 * ViewModel 浣跨敤璇ユ灇涓捐绠楁煡璇㈠尯闂达紝UI 鍙礋璐ｅ睍绀?[label] 骞舵妸鐢ㄦ埛閫夋嫨鍥炰紶銆?
 */
internal enum class AppUsageDateRange(@StringRes val labelRes: Int) {
    Today(R.string.today),
    Yesterday(R.string.yesterday),
    Last7Days(R.string.last_7_days),
    Last30Days(R.string.last_30_days);

    /**
     * 璁＄畻褰撳墠鏃ユ湡鑼冨洿瀵瑰簲鐨勫紑濮嬪拰缁撴潫鏃堕棿鎴炽€?
     *
     * @param nowMillis 褰撳墠鏃堕棿鎴筹紝娴嬭瘯涓彲浼犲叆鍥哄畾鏃堕棿淇濊瘉缁撴灉绋冲畾銆?
     * @return Pair(first = 寮€濮嬫椂闂? second = 缁撴潫鏃堕棿)銆?
     */
    fun timeBounds(nowMillis: Long = System.currentTimeMillis()): Pair<Long, Long> {
        val startToday = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return when (this) {
            Today -> startToday to nowMillis
            Yesterday -> startToday - DAY_MILLIS to startToday
            Last7Days -> startToday - 6 * DAY_MILLIS to nowMillis
            Last30Days -> startToday - 29 * DAY_MILLIS to nowMillis
        }
    }

    private companion object {
        private const val DAY_MILLIS = 24L * 60L * 60L * 1000L
    }
}
