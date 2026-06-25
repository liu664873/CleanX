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

-keepattributes Signature,*Annotation*,InnerClasses,EnclosingMethod,AnnotationDefault

# Keep classes and members explicitly marked for reflection or framework lookup.
-keep @androidx.annotation.Keep class * { *; }
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <fields>;
    @androidx.annotation.Keep <methods>;
    @androidx.annotation.Keep <init>(...);
}

# Android framework entry points and native methods.
-keep class * extends android.app.Application { public <init>(); }
-keep class * extends android.app.Activity { public <init>(); }
-keep class * extends android.app.Service { public <init>(); }
-keep class * extends android.content.BroadcastReceiver { public <init>(); }
-keep class * extends android.content.ContentProvider { public <init>(); }
-keep class * extends android.service.notification.NotificationListenerService { public <init>(); }
-keepclasseswithmembernames class * {
    native <methods>;
}

# Enum APIs are used by UI state and routing helpers.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Project bean/model/state classes.
# Broad keep by package; name-suffix rules below are redundant and have been removed
# to let R8 shrink unused data classes.
-keep class com.quickcleanpro.phonecleaner.common.** { *; }
-keep class com.quickcleanpro.phonecleaner.domain.model.** { *; }
-keep class com.quickcleanpro.phonecleaner.domain.state.** { *; }
-keep class com.quickcleanpro.phonecleaner.presentation.common.state.** { *; }
# Only keep navigation Screen sealed classes (reflection-based routing).
-keep class com.quickcleanpro.phonecleaner.presentation.navigation.Screen { *; }
-keep class com.quickcleanpro.phonecleaner.presentation.navigation.Screen$* { *; }
-keep class com.quickcleanpro.phonecleaner.presentation.common.route.Screen { *; }
-keep class com.quickcleanpro.phonecleaner.presentation.common.route.Screen$* { *; }

# App component classes that are resolved by manifest, Koin, or callbacks.
-keep class com.quickcleanpro.phonecleaner.di.** { *; }
-keep class com.quickcleanpro.phonecleaner.QuickCleanApplication { *; }
-keep class com.quickcleanpro.phonecleaner.MainActivity { *; }
-keep class com.quickcleanpro.phonecleaner.data.source.notification.PersistentNotificationService { *; }
-keep class com.quickcleanpro.phonecleaner.data.source.notification.QuickCleanNotificationListener { *; }
-keep class com.quickcleanpro.phonecleaner.data.source.applock.LockScreenOverlayService { *; }
-keep class com.quickcleanpro.phonecleaner.domain.repository.** { *; }
-keep class com.quickcleanpro.phonecleaner.domain.usecase.** { *; }
-keep class com.quickcleanpro.phonecleaner.data.local.** { *; }
-keep class com.quickcleanpro.phonecleaner.data.repository.** { *; }
-keep class com.quickcleanpro.phonecleaner.data.source.** { *; }
-keep class com.quickcleanpro.phonecleaner.presentation.screen.**.*ViewModel { *; }
-keep class com.quickcleanpro.phonecleaner.util.** { *; }
-keep class com.quickcleanpro.phonecleaner.utils.** { *; }

# Trustlook cloud scan SDK and SDK callback/data classes.
-keep class com.trustlook.** { *; }
-dontwarn com.trustlook.**

# Koin dependency injection.
-keep class org.koin.** { *; }
-keep class kotlin.reflect.** { *; }
-dontwarn org.koin.**
-dontwarn kotlin.reflect.**

# Lottie animation runtime.
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

# Kotlin, coroutines, AndroidX, Compose, AppCompat, Okio/OkHttp optional classes.
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.coroutines.**
-dontwarn androidx.compose.**
-dontwarn androidx.lifecycle.**
-dontwarn androidx.navigation.**
-dontwarn androidx.appcompat.**
-dontwarn com.android.internal.os.PowerProfile
-dontwarn okio.**
-dontwarn okhttp3.**
-dontwarn javax.annotation.**
-dontwarn org.jetbrains.annotations.**
-dontwarn org.codehaus.mojo.animal_sniffer.**
