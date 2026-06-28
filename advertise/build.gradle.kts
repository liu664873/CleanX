plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.kotlin.compose)
	alias(libs.plugins.kotlin.serialization)
}

android {
	namespace = "com.pdffox.adv"
	compileSdk = 36

	defaultConfig {
		minSdk = 29

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		consumerProguardFiles("consumer-rules.pro")
	}

	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
	buildFeatures {
		compose = true
		viewBinding = true
		buildConfig = true
	}
}

kotlin {
	jvmToolchain(11)
}

dependencies {

	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.appcompat)
	implementation(libs.material)
	implementation(libs.androidx.activity)
	implementation(libs.androidx.constraintlayout)
	api(platform(libs.androidx.compose.bom))
	api(libs.androidx.compose.ui)
	testImplementation(libs.junit)
	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)
	implementation(libs.kotlinx.coroutines.android)
	implementation(libs.kotlinx.serialization.json)
	implementation(libs.androidx.work.runtime.ktx)

	implementation(platform("com.google.firebase:firebase-bom:34.2.0"))
	implementation("com.google.firebase:firebase-analytics")
	implementation("com.google.firebase:firebase-config")
	implementation("com.google.firebase:firebase-crashlytics-ndk")
	implementation("com.google.firebase:firebase-messaging")

	implementation("cn.thinkingdata.android:ThinkingAnalyticsSDK:3.0.3.1")
	implementation("com.singular.sdk:singular_sdk:12.9.1")

	implementation("com.squareup.okhttp3:okhttp:5.1.0")

	implementation(libs.glide)

	implementation("com.facebook.android:facebook-android-sdk:18.2.3")

	implementation(libs.integrity)

	implementation(libs.gson)

	implementation("com.google.android.gms:play-services-base:18.9.0")

	implementation("com.google.android.ump:user-messaging-platform:4.0.0")
	api("com.google.android.gms:play-services-ads:25.3.0")
	implementation("com.google.android.play:app-update:2.1.0")

	// Mediation adapters are intentionally not enabled in CleanX phase 1.
	// The copied CleanPartner versions include adapters that conflict under AGP 9 namespaces.

	//Androidx (Necessary)
	implementation("androidx.appcompat:appcompat:1.6.1")
	implementation("androidx.browser:browser:1.4.0")
//	api("com.github.tiktok:tiktok-business-android-sdk:1.5.0")
// replace the version with the one which suits your need
//to listen for app life cycle
	implementation("androidx.lifecycle:lifecycle-process:2.3.1")
	implementation("androidx.lifecycle:lifecycle-common-java8:2.3.1")
//to get Google install referrer
	implementation("com.android.installreferrer:installreferrer:2.2")
}
