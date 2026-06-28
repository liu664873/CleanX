pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://www.jitpack.io") }
        maven { url = uri("https://maven.singular.net/") }
        maven { url = uri("https://cboost.jfrog.io/artifactory/chartboost-ads/") }
        maven { url = uri("https://artifact.bytedance.com/repository/pangle") }
        maven { url = uri("https://android-sdk.is.com/") }
        maven { url = uri("https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea") }
        maven { url = uri("https://jfrog.anythinktech.com/artifactory/overseas_sdk") }
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://www.jitpack.io") }
        maven { url = uri("https://repository.liferay.com/nexus/content/repositories/public/") }
        maven { url = uri("https://maven.singular.net/") }
        maven { url = uri("https://cboost.jfrog.io/artifactory/chartboost-ads/") }
        maven { url = uri("https://artifact.bytedance.com/repository/pangle") }
        maven { url = uri("https://android-sdk.is.com/") }
        maven { url = uri("https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea") }
        maven { url = uri("https://jfrog.anythinktech.com/artifactory/overseas_sdk") }
        flatDir {
            dirs("app/libs")
        }
    }
}

rootProject.name = "cleanx"
include(":app")
include(":advertise")
