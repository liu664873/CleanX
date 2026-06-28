# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class  com.pdffox.adv.adv.AdvIDs {
    *;
}

-keep class  com.pdffox.adv.log.LogAdData {
    *;
}

-keep class  com.pdffox.adv.log.LogAdParam {
    *;
}

-keep class  com.pdffox.adv.Config {
    *;
}
-keep class com.pdffox.adv.adv.AdConfig {
    *;
}
-keep class  com.pdffox.adv.adv.IpGeoDetail {
    *;
}

-keep class  com.pdffox.adv.adv.CidrRange {
    *;
}

-keep class  com.pdffox.adv.push.PushData {
    *;
}

-keepattributes Signature
-keepattributes *Annotation*
-keepclassmembers class kotlin.Metadata { *; }



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

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# 保留 Gson 反射相关类
-keep class com.google.gson.** {
 *;
 }
-keepattributes Signature
-keepattributes *Annotation*
-keepclassmembers class kotlin.Metadata { *; }
