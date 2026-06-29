package com.quickcleanpro.phonecleaner.presentation.ads

enum class AdFormat {
    Splash,
    Interstitial,
    Native
}

enum class AdPlacement(
    val format: AdFormat,
    val placementId: String
) {
    SplashLaunch(AdFormat.Splash, "splash_launch"),
    InterstitialToolLaunch(AdFormat.Interstitial, "interstitial_tool_launch"),
    InterstitialScanResult(AdFormat.Interstitial, "interstitial_scan_result"),
    InterstitialCleanResult(AdFormat.Interstitial, "interstitial_clean_result"),
    HomeNative(AdFormat.Native, "home_native"),
    ToolboxNative(AdFormat.Native, "toolbox_native"),
    ResultNative(AdFormat.Native, "result_native"),
    FileListNative(AdFormat.Native, "file_list_native")
}
