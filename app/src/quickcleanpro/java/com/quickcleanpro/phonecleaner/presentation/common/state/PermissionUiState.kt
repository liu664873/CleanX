package com.quickcleanpro.phonecleaner.presentation.common.state

/**
 * 椤甸潰鏉冮檺鎻愮ず鐘舵€併€?
 *
 * 缁熶竴鎵胯浇鏉冮檺鏄惁宸叉巿鏉冦€佹彁绀烘爣棰樸€佽鏄庢枃妗堝拰鎸夐挳鏂囧瓧锛?
 * 璁?UI 鍙牴鎹姸鎬佸睍绀烘彁绀哄崱鐗囷紝涓嶇洿鎺ュ垽鏂叿浣撶郴缁熸潈闄愮粏鑺傘€?
 */
data class PermissionUiState(
    val granted: Boolean,
    val title: String,
    val description: String,
    val actionLabel: String
) {
    /** 鏄惁闇€瑕佸湪椤甸潰涓婃樉绀烘巿鏉冩彁绀恒€?*/
    val shouldShowPrompt: Boolean get() = !granted
}
