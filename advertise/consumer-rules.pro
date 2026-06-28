# Consumer R8 rules for apps that depend on the advertise module.
# The app module enables minification in release, so keep rules that protect
# serialization, reflection, and SDK integration must live here.

-keep class com.pdffox.adv.adv.AdvIDs {
    *;
}

-keep class com.pdffox.adv.log.LogAdData {
    *;
}

-keep class com.pdffox.adv.log.LogAdParam {
    *;
}

-keep class com.pdffox.adv.Config {
    *;
}

-keep class com.pdffox.adv.adv.AdConfig {
    *;
}

-keep class com.pdffox.adv.adv.IpGeoDetail {
    *;
}

-keep class com.pdffox.adv.adv.CidrRange {
    *;
}

-keep class com.pdffox.adv.push.PushData {
    *;
}

-keep class com.pdffox.adv.remoteconfig.RemoteConfig {
    *;
}

-keep class com.pdffox.adv.Root {
    *;
}

-keep class com.pdffox.adv.adv.policy.data.AdMapping {
    *;
}

-keep class com.pdffox.adv.adv.policy.data.Config {
    *;
}

-keep class com.pdffox.adv.adv.policy.data.ConfigItem {
    *;
}

-keep class com.pdffox.adv.remoteconfig.RemoteConfigManager {
    *;
}

-keep class com.pdffox.adv.adv.policy.AdPolicyManager {
    *;
}

-keep class com.pdffox.adv.adv.policy.AdPlayRecordManager {
    *;
}

-keep class com.pdffox.adv.adv.policy.data.AdPolicy {
    *;
}

-keep class com.pdffox.adv.adv.policy.data.AdNetwork {
    *;
}

-keep class com.pdffox.adv.adv.policy.data.AdUnit {
    *;
}

-keep class com.pdffox.adv.adv.policy.data.FrequencyCaps {
    *;
}

-keep class com.pdffox.adv.adv.policy.NativePolicyManager {
    *;
}

-keep class com.pdffox.adv.adv.policy.NativeAdPlayRecordManager {
    *;
}

-keep class com.pdffox.adv.adv.policy.data.AdNativePolicy {
    *;
}

-keep class com.pdffox.adv.adv.NativeAdId {
    *;
}

-keep class com.pdffox.adv.adv.NativeAdContent {
    *;
}

-keep class com.tiktok.** { *; }
-keep class com.android.billingclient.api.** { *; }
-keep class androidx.lifecycle.** { *; }
-keep class com.google.gson.** { *; }

-dontwarn cn.thinkingdata.ta_apt.TRoute

-keepattributes Signature
-keepattributes *Annotation*
-keepclassmembers class kotlin.Metadata { *; }

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}
