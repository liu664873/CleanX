plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ktlint)
}

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
