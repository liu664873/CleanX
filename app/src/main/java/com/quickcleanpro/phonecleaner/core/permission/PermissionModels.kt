package com.quickcleanpro.phonecleaner.core.permission

import android.content.Intent

interface PermissionFeature {
    val key: String
}

interface AppPermission {
    val key: String
}

enum class CommonPermission(
    override val key: String,
) : AppPermission {
    StorageFiles("storage_files"),
    MediaImages("media_images"),
    MediaImagesWithLocation("media_images_with_location"),
    MediaVideo("media_video"),
    MediaAudio("media_audio"),
    Location("location"),
    UsageAccess("usage_access"),
    NotificationListener("notification_listener"),
    Overlay("overlay"),
    PostNotifications("post_notifications"),
}

data class PermissionSpec<F : PermissionFeature>(
    val feature: F,
    val permissions: List<AppPermission>,
)

data class PermissionStatus(
    val granted: Boolean,
    val missing: List<AppPermission>,
)

sealed interface PermissionRequestPlan {
    data object AlreadyGranted : PermissionRequestPlan

    data class RequestRuntime(
        val permissions: Array<String>,
    ) : PermissionRequestPlan

    data class OpenSettings(
        val intents: List<Intent>,
    ) : PermissionRequestPlan

    data object Unavailable : PermissionRequestPlan
}

