plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ktlint)
}

fun String.asBuildConfigString(): String =
    "\"" + replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n") + "\""

fun configValue(name: String, defaultValue: String = ""): String =
    providers.gradleProperty(name)
        .orElse(providers.environmentVariable(name))
        .orElse(defaultValue)
        .get()

val storagecleanerTrustlookApiKey =
    configValue(
        "TRUSTLOOK_STORAGECLEANER_API_KEY",
        "6ae4e8ff542e4d91d0d0332be544740a1aabaef54fd8ceae98c9aa76"
    )

val admobTestAppId = "ca-app-pub-3940256099942544~3347511713"
val admobTestAppOpenUnitId = "ca-app-pub-3940256099942544/9257395921"
val admobTestInterstitialUnitId = "ca-app-pub-3940256099942544/1033173712"
val admobTestBannerUnitId = "ca-app-pub-3940256099942544/6300978111"
val admobTestNativeUnitId = "ca-app-pub-3940256099942544/2247696110"
val admobTestNativeIdsJson =
    """
    [
      {
        "highPriceID": "$admobTestNativeUnitId",
        "midPriceID": "$admobTestNativeUnitId",
        "lowPriceID": "$admobTestNativeUnitId"
      }
    ]
    """.trimIndent()

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
            manifestPlaceholders["advAdmobAppId"] = configValue("ADV_STORAGECLEANER_ADMOB_APP_ID", configValue("ADMOB_STORAGECLEANER_APP_ID", admobTestAppId))
            manifestPlaceholders["advFacebookAppId"] = configValue("ADV_STORAGECLEANER_FACEBOOK_APP_ID", "")
            manifestPlaceholders["advFacebookClientToken"] = configValue("ADV_STORAGECLEANER_FACEBOOK_CLIENT_TOKEN", "")
            manifestPlaceholders["persistentNotificationSpecialUseSubtype"] =
                "Keeps Storage Cleaner tools available in notifications, samples local battery history for Battery Info charts, and monitors user-selected protected apps for App Lock."

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
            buildConfigField("String", "ADV_PRIVACY_URL", configValue("PRIVACY_POLICY_STORAGECLEANER_URL", "https://sites.google.com/view/phone-cleanup-storage-cleaner/home").asBuildConfigString())
            buildConfigField("String", "ADV_TERMS_URL", configValue("TERMS_OF_SERVICE_STORAGECLEANER_URL", "https://sites.google.com/view/phonecleanupstoragecleaner/home").asBuildConfigString())
            buildConfigField("String", "ADV_DEFAULT_TOPIC", configValue("ADV_STORAGECLEANER_DEFAULT_TOPIC", "storage-cleaner-debug").asBuildConfigString())
            buildConfigField("String", "ADV_THINKING_APP_KEY", configValue("ADV_STORAGECLEANER_THINKING_APP_KEY", "").asBuildConfigString())
            buildConfigField("String", "ADV_THINKING_SERVER_URL", configValue("ADV_STORAGECLEANER_THINKING_SERVER_URL", "").asBuildConfigString())
            buildConfigField("String", "ADV_SINGULAR_API_KEY", configValue("ADV_STORAGECLEANER_SINGULAR_API_KEY", "").asBuildConfigString())
            buildConfigField("String", "ADV_SINGULAR_SECRET", configValue("ADV_STORAGECLEANER_SINGULAR_SECRET", "").asBuildConfigString())
            buildConfigField("String", "ADV_ADMOB_APP_ID", configValue("ADV_STORAGECLEANER_ADMOB_APP_ID", configValue("ADMOB_STORAGECLEANER_APP_ID", admobTestAppId)).asBuildConfigString())
            buildConfigField("String", "ADV_ADMOB_BANNER_ID", configValue("ADV_STORAGECLEANER_ADMOB_BANNER_ID", configValue("ADMOB_STORAGECLEANER_BANNER_UNIT_ID", admobTestBannerUnitId)).asBuildConfigString())
            buildConfigField("String", "ADV_ADMOB_INTERSTITIAL_ID", configValue("ADV_STORAGECLEANER_ADMOB_INTERSTITIAL_ID", configValue("ADMOB_STORAGECLEANER_INTERSTITIAL_UNIT_ID", admobTestInterstitialUnitId)).asBuildConfigString())
            buildConfigField("String", "ADV_ADMOB_NATIVE_ID", configValue("ADV_STORAGECLEANER_ADMOB_NATIVE_ID", configValue("ADMOB_STORAGECLEANER_NATIVE_UNIT_ID", admobTestNativeUnitId)).asBuildConfigString())
            buildConfigField("String", "ADV_ADMOB_NATIVE_IDS_JSON", configValue("ADV_STORAGECLEANER_ADMOB_NATIVE_IDS_JSON", admobTestNativeIdsJson).asBuildConfigString())
            buildConfigField("String", "ADV_ADMOB_OPEN_ID", configValue("ADV_STORAGECLEANER_ADMOB_OPEN_ID", configValue("ADMOB_STORAGECLEANER_APP_OPEN_UNIT_ID", admobTestAppOpenUnitId)).asBuildConfigString())
            buildConfigField("String", "ADV_FACEBOOK_APP_ID", configValue("ADV_STORAGECLEANER_FACEBOOK_APP_ID", "").asBuildConfigString())
            buildConfigField("String", "ADV_FACEBOOK_CLIENT_TOKEN", configValue("ADV_STORAGECLEANER_FACEBOOK_CLIENT_TOKEN", "").asBuildConfigString())
            buildConfigField("String", "ADV_TIKTOK_ACCESS_TOKEN", configValue("ADV_STORAGECLEANER_TIKTOK_ACCESS_TOKEN", "").asBuildConfigString())
            buildConfigField("String", "ADV_TIKTOK_TT_APP_ID", configValue("ADV_STORAGECLEANER_TIKTOK_TT_APP_ID", "").asBuildConfigString())
            buildConfigField("String", "ADV_TIKTOK_APP_ID", configValue("ADV_STORAGECLEANER_TIKTOK_APP_ID", "").asBuildConfigString())
            buildConfigField("String", "ADV_SAFE_EXPECTED_SIGNATURES", configValue("ADV_STORAGECLEANER_SAFE_EXPECTED_SIGNATURES", "").asBuildConfigString())
            buildConfigField("String", "ADV_SERVER_RELEASE_HOST", configValue("ADV_STORAGECLEANER_SERVER_RELEASE_HOST", "").asBuildConfigString())
            buildConfigField("String", "ADV_SERVER_TEST_HOST", configValue("ADV_STORAGECLEANER_SERVER_TEST_HOST", "").asBuildConfigString())
            buildConfigField("String", "ADV_PLAY_INTEGRITY_PARSE_TOKEN_KEY", configValue("ADV_STORAGECLEANER_PLAY_INTEGRITY_PARSE_TOKEN_KEY", "").asBuildConfigString())
            buildConfigField("long", "ADV_PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER", "${configValue("ADV_STORAGECLEANER_PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER", "0")}L")

            buildConfigField(
                "String",
                "TERMS_OF_SERVICE_URL",
                configValue("TERMS_OF_SERVICE_STORAGECLEANER_URL", "https://sites.google.com/view/phonecleanupstoragecleaner/home").asBuildConfigString()
            )
            buildConfigField(
                "String",
                "PRIVACY_POLICY_URL",
                configValue("PRIVACY_POLICY_STORAGECLEANER_URL", "https://sites.google.com/view/phone-cleanup-storage-cleaner/home").asBuildConfigString()
            )
        }

    }

    defaultConfig {
        minSdk = 29
        targetSdk = 36
        versionCode = 7
        versionName = "1.0.6"

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
    implementation(project(":advertise"))
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
