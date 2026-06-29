package com.quickcleanpro.phonecleaner.config

import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.route.Screen

fun FeatureKey.screenOrNull(): Screen? =
    when (this) {
        FeatureKey.JUNK_CLEAN -> Screen.Scan
        FeatureKey.ANTI_VIRUS -> Screen.AntiVirus
        FeatureKey.APP_LOCK -> Screen.AppLock
        FeatureKey.DEVICE_INFO -> Screen.DeviceInfo
        FeatureKey.BATTERY_INFO -> Screen.BatteryInfo
        FeatureKey.APP_USAGE -> Screen.AppUsage
        FeatureKey.NOTIFICATION_BAR -> Screen.NotificationBar
        FeatureKey.NOTIFICATION_CLEANER -> Screen.NotificationCleaner
        FeatureKey.WHATSAPP_CLEANER -> Screen.WhatsAppCleaner
        FeatureKey.NETWORK_USAGE -> Screen.NetworkUsage
        FeatureKey.NETWORK_SCAN -> Screen.NetworkScan
        FeatureKey.NETWORK_SPEED -> Screen.NetworkSpeed
        FeatureKey.PHOTOS -> Screen.PhotosManager
        FeatureKey.SIMILAR_PHOTOS -> Screen.SimilarPhotosManager
        FeatureKey.PHOTO_PRIVACY -> Screen.PhotoPrivacyManager
        FeatureKey.SCREENSHOTS -> Screen.ScreenshotsManager
        FeatureKey.VIDEOS -> Screen.VideosManager
        FeatureKey.AUDIOS -> Screen.AudiosManager
        FeatureKey.LARGE_FILES -> Screen.LargeFilesManager
        FeatureKey.DUPLICATE_FILES -> Screen.DuplicateFilesManager
        FeatureKey.DOCUMENTS -> Screen.DocumentsManager
    }

fun FeatureKey.titleRes(): Int =
    when (this) {
        FeatureKey.JUNK_CLEAN -> R.string.home_remove_junk
        FeatureKey.ANTI_VIRUS -> R.string.home_virus_title
        FeatureKey.APP_LOCK -> R.string.home_app_lock_title
        FeatureKey.DEVICE_INFO -> R.string.nav_device_info
        FeatureKey.BATTERY_INFO -> R.string.nav_battery_info
        FeatureKey.APP_USAGE -> R.string.nav_app_usage
        FeatureKey.NOTIFICATION_BAR -> R.string.nav_notification_bar
        FeatureKey.NOTIFICATION_CLEANER -> R.string.nav_notification_cleaner
        FeatureKey.WHATSAPP_CLEANER -> R.string.nav_whatsapp_cleaner
        FeatureKey.NETWORK_USAGE -> R.string.nav_network_usage
        FeatureKey.NETWORK_SCAN -> R.string.nav_network_scan
        FeatureKey.NETWORK_SPEED -> R.string.nav_network_speed
        FeatureKey.PHOTOS -> R.string.nav_photos
        FeatureKey.SIMILAR_PHOTOS -> R.string.nav_similar_photos
        FeatureKey.PHOTO_PRIVACY -> R.string.nav_photo_privacy
        FeatureKey.SCREENSHOTS -> R.string.nav_screenshots
        FeatureKey.VIDEOS -> R.string.nav_videos
        FeatureKey.AUDIOS -> R.string.nav_audios
        FeatureKey.LARGE_FILES -> R.string.nav_large_files
        FeatureKey.DUPLICATE_FILES -> R.string.nav_duplicate_files
        FeatureKey.DOCUMENTS -> R.string.nav_documents
    }

fun FeatureKey.iconRes(): Int =
    when (this) {
        FeatureKey.JUNK_CLEAN -> R.drawable.trash_can
        FeatureKey.ANTI_VIRUS -> R.drawable.virus_shield
        FeatureKey.APP_LOCK -> R.drawable.app_lock
        FeatureKey.DEVICE_INFO -> R.drawable.ic_device_phone
        FeatureKey.BATTERY_INFO -> R.drawable.battery
        FeatureKey.APP_USAGE -> R.drawable.ic_app_usage
        FeatureKey.NOTIFICATION_BAR -> R.drawable.ic_notification_bar
        FeatureKey.NOTIFICATION_CLEANER -> R.drawable.ic_n_notification_cleaner
        FeatureKey.WHATSAPP_CLEANER -> R.drawable.ic_whatsapp_cleaner
        FeatureKey.NETWORK_USAGE -> R.drawable.ic_network_usage
        FeatureKey.NETWORK_SCAN -> R.drawable.ic_network_scan
        FeatureKey.NETWORK_SPEED -> R.drawable.ic_network_speed
        FeatureKey.PHOTOS -> R.drawable.ic_photos
        FeatureKey.SIMILAR_PHOTOS -> R.drawable.ic_similar_photos
        FeatureKey.PHOTO_PRIVACY -> R.drawable.ic_photo_privacy
        FeatureKey.SCREENSHOTS -> R.drawable.ic_screenshots
        FeatureKey.VIDEOS -> R.drawable.ic_videos
        FeatureKey.AUDIOS -> R.drawable.ic_audios
        FeatureKey.LARGE_FILES -> R.drawable.ic_large_files
        FeatureKey.DUPLICATE_FILES -> R.drawable.ic_file_yellow
        FeatureKey.DOCUMENTS -> R.drawable.ic_documents
    }
