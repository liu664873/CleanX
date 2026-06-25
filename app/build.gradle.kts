plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ktlint)
}

fun String.asBuildConfigString(): String =
    "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""

fun configValue(name: String, defaultValue: String = ""): String =
    providers.gradleProperty(name)
        .orElse(providers.environmentVariable(name))
        .orElse(defaultValue)
        .get()

val originalTrustlookApiKey =
    configValue(
        "TRUSTLOOK_ORIGINAL_API_KEY",
        "42ce0e3abcd20e2e9e28af8318c95dd5dde8b60223fa16721ed7b542",
    )
val storagecleanerTrustlookApiKey =
    configValue(
        "TRUSTLOOK_ORIGINAL_API_KEY",
        "6ae4e8ff542e4d91d0d0332be544740a1aabaef54fd8ceae98c9aa76"
    )

val admobTestAppId = "ca-app-pub-3940256099942544~3347511713"
val admobTestAppOpenUnitId = "ca-app-pub-3940256099942544/9257395921"
val admobTestInterstitialUnitId = "ca-app-pub-3940256099942544/1033173712"
val admobTestBannerUnitId = "ca-app-pub-3940256099942544/6300978111"
val admobTestNativeUnitId = "ca-app-pub-3940256099942544/2247696110"

android {
    namespace = "com.quickcleanpro.phonecleaner"
    compileSdk {
        version =
            release(36) {
                minorApiLevel = 1
            }
    }

    flavorDimensions += "variant"

    productFlavors {
        create("storagecleaner") {
            dimension = "variant"
            applicationId = "com.phonecleanup.storage.cleaner"

            manifestPlaceholders["launcherIcon"] = configValue("STORAGECLEANER_LAUNCHER_ICON", "@mipmap/ic_launcher")
            manifestPlaceholders["roundLauncherIcon"] = configValue("STORAGECLEANER_ROUND_LAUNCHER_ICON", "@mipmap/ic_launcher_round")
            manifestPlaceholders["appTheme"] = configValue("STORAGECLEANER_APP_THEME", "@style/Theme.QuickCleanPRO")
            manifestPlaceholders["trustlookApiKey"] = storagecleanerTrustlookApiKey
            manifestPlaceholders["admobAppId"] = configValue("ADMOB_STORAGECLEANER_APP_ID", admobTestAppId)

            buildConfigField("String", "VARIANT_KEY", "storagecleaner".asBuildConfigString())
            buildConfigField("String", "THEME_KEY", "storage_cleaner".asBuildConfigString())
            buildConfigField("String", "PRIMARY_FEATURE", "JUNK_CLEAN".asBuildConfigString())

            // 只保留清理和文件管理相关功能（按需调整）
            buildConfigField(
                "String",
                "ENABLED_FEATURES",
                "JUNK_CLEAN,DEVICE_INFO,BATTERY_INFO,PHOTOS,VIDEOS,AUDIOS,LARGE_FILES,DUPLICATE_FILES,DOCUMENTS,WHATSAPP_CLEANER".asBuildConfigString()
            )
            buildConfigField("String", "HOME_FEATURE_ORDER", "JUNK_CLEAN,DUPLICATE_FILES,LARGE_FILES".asBuildConfigString())
            buildConfigField(
                "String",
                "FILE_FEATURE_ORDER",
                "PHOTOS,VIDEOS,AUDIOS,LARGE_FILES,DUPLICATE_FILES,DOCUMENTS".asBuildConfigString()
            )
            buildConfigField(
                "String",
                "TOOLBOX_FEATURE_ORDER",
                "DEVICE_INFO,BATTERY_INFO,WHATSAPP_CLEANER".asBuildConfigString()
            )

            buildConfigField("String", "TRUSTLOOK_API_KEY", storagecleanerTrustlookApiKey.asBuildConfigString())
            buildConfigField("String", "ADMOB_APP_ID", configValue("ADMOB_STORAGECLEANER_APP_ID", admobTestAppId).asBuildConfigString())
            buildConfigField("String", "ADMOB_APP_OPEN_UNIT_ID", configValue("ADMOB_STORAGECLEANER_APP_OPEN_UNIT_ID", admobTestAppOpenUnitId).asBuildConfigString())
            buildConfigField("String", "ADMOB_INTERSTITIAL_UNIT_ID", configValue("ADMOB_STORAGECLEANER_INTERSTITIAL_UNIT_ID", admobTestInterstitialUnitId).asBuildConfigString())
            buildConfigField("String", "ADMOB_BANNER_UNIT_ID", configValue("ADMOB_STORAGECLEANER_BANNER_UNIT_ID", admobTestBannerUnitId).asBuildConfigString())
            buildConfigField("String", "ADMOB_NATIVE_UNIT_ID", configValue("ADMOB_STORAGECLEANER_NATIVE_UNIT_ID", admobTestNativeUnitId).asBuildConfigString())

            buildConfigField(
                "String",
                "TERMS_OF_SERVICE_URL",
                configValue("TERMS_OF_SERVICE_STORAGECLEANER_URL", "https://sites.google.com/view/quickcleanpro-termsconditions/home").asBuildConfigString()
            )
            buildConfigField(
                "String",
                "PRIVACY_POLICY_URL",
                configValue("PRIVACY_POLICY_STORAGECLEANER_URL", "https://sites.google.com/view/quick-clean-pro-privacy-policy/home").asBuildConfigString()
            )
        }

        create("original") {
            dimension = "variant"
            applicationId = "com.quickcleanpro.phonecleaner"
            manifestPlaceholders["launcherIcon"] = configValue("ORIGINAL_LAUNCHER_ICON", "@mipmap/ic_launcher")
            manifestPlaceholders["roundLauncherIcon"] = configValue("ORIGINAL_ROUND_LAUNCHER_ICON", "@mipmap/ic_launcher_round")
            manifestPlaceholders["appTheme"] = configValue("ORIGINAL_APP_THEME", "@style/Theme.QuickCleanPRO")
            manifestPlaceholders["trustlookApiKey"] = originalTrustlookApiKey
            manifestPlaceholders["admobAppId"] = configValue("ADMOB_ORIGINAL_APP_ID", admobTestAppId)
            buildConfigField("String", "VARIANT_KEY", "original".asBuildConfigString())
            buildConfigField("String", "THEME_KEY", "quick_clean".asBuildConfigString())
            buildConfigField("String", "PRIMARY_FEATURE", "JUNK_CLEAN".asBuildConfigString())
            buildConfigField(
                "String",
                "ENABLED_FEATURES",
                "JUNK_CLEAN,ANTI_VIRUS,APP_LOCK,DEVICE_INFO,BATTERY_INFO,APP_USAGE,NOTIFICATION_BAR,NOTIFICATION_CLEANER,WHATSAPP_CLEANER,NETWORK_USAGE,NETWORK_SCAN,NETWORK_SPEED,PHOTOS,SIMILAR_PHOTOS,PHOTO_PRIVACY,SCREENSHOTS,VIDEOS,AUDIOS,LARGE_FILES,DUPLICATE_FILES,DOCUMENTS".asBuildConfigString(),
            )
            buildConfigField("String", "HOME_FEATURE_ORDER", "JUNK_CLEAN,ANTI_VIRUS,APP_LOCK".asBuildConfigString())
            buildConfigField(
                "String",
                "FILE_FEATURE_ORDER",
                "PHOTOS,SIMILAR_PHOTOS,PHOTO_PRIVACY,SCREENSHOTS,VIDEOS,AUDIOS,LARGE_FILES,DUPLICATE_FILES,DOCUMENTS".asBuildConfigString(),
            )
            buildConfigField(
                "String",
                "TOOLBOX_FEATURE_ORDER",
                "DEVICE_INFO,BATTERY_INFO,APP_USAGE,NOTIFICATION_BAR,WHATSAPP_CLEANER,NETWORK_USAGE,NETWORK_SCAN,NETWORK_SPEED,NOTIFICATION_CLEANER".asBuildConfigString(),
            )
            buildConfigField("String", "TRUSTLOOK_API_KEY", originalTrustlookApiKey.asBuildConfigString())
            buildConfigField("String", "ADMOB_APP_ID", configValue("ADMOB_ORIGINAL_APP_ID", admobTestAppId).asBuildConfigString())
            buildConfigField(
                "String",
                "ADMOB_APP_OPEN_UNIT_ID",
                configValue("ADMOB_ORIGINAL_APP_OPEN_UNIT_ID", admobTestAppOpenUnitId).asBuildConfigString(),
            )
            buildConfigField(
                "String",
                "ADMOB_INTERSTITIAL_UNIT_ID",
                configValue("ADMOB_ORIGINAL_INTERSTITIAL_UNIT_ID", admobTestInterstitialUnitId).asBuildConfigString(),
            )
            buildConfigField(
                "String",
                "ADMOB_BANNER_UNIT_ID",
                configValue("ADMOB_ORIGINAL_BANNER_UNIT_ID", admobTestBannerUnitId).asBuildConfigString(),
            )
            buildConfigField(
                "String",
                "ADMOB_NATIVE_UNIT_ID",
                configValue("ADMOB_ORIGINAL_NATIVE_UNIT_ID", admobTestNativeUnitId).asBuildConfigString(),
            )
            buildConfigField(
                "String",
                "TERMS_OF_SERVICE_URL",
                configValue("TERMS_OF_SERVICE_ORIGINAL_URL", "https://sites.google.com/view/quickcleanpro-termsconditions/home").asBuildConfigString(),
            )
            buildConfigField(
                "String",
                "PRIVACY_POLICY_URL",
                configValue("PRIVACY_POLICY_ORIGINAL_URL", "https://sites.google.com/view/quick-clean-pro-privacy-policy/home").asBuildConfigString(),
            )
        }

    }

    defaultConfig {
        minSdk = 24
        targetSdk = 36
        versionCode = 3
        versionName = "1.0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    lint {
        baseline = file("lint-baseline.xml")
    }
}

kotlin {
    jvmToolchain(17)
}

ktlint {
    android.set(true)
}

dependencies {
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.unit)
    implementation(files("libs/cloudscan_sdk_5.0.18.20250821.aar"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.lottie)
    implementation(libs.lottie.compose)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
}
