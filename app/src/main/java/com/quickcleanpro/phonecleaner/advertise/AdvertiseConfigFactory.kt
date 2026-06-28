package com.quickcleanpro.phonecleaner.advertise

import android.app.Application
import com.pdffox.adv.AdvertiseSdkConfig
import com.pdffox.adv.AdvertiseSdkConfigs
import com.pdffox.adv.NotificationFeatureConfig
import com.quickcleanpro.phonecleaner.BuildConfig
import com.quickcleanpro.phonecleaner.R

object AdvertiseConfigFactory {
    fun create(context: Application): AdvertiseSdkConfig =
        AdvertiseSdkConfigs.create(context, BuildConfig.DEBUG) {
            legal(
                privacyUrl = BuildConfig.ADV_PRIVACY_URL,
                termsUrl = BuildConfig.ADV_TERMS_URL,
            )
            defaultTopic(BuildConfig.ADV_DEFAULT_TOPIC)
            resources(
                adPolicyRawResId = R.raw.ad_policy,
                adLoadConfigRawResId = R.raw.adload_config,
                nativeAdPolicyRawResId = R.raw.native_ad_policy,
                nativeAdIdsRawResId = R.raw.native_ad_ids,
                cloudCidrsRawResId = com.pdffox.adv.R.raw.cloud,
                googleCidrsRawResId = com.pdffox.adv.R.raw.google,
            )
            server(
                enabled = AdvertiseRuntimeCapabilities.SERVER_ENABLED,
                releaseHost = BuildConfig.ADV_SERVER_RELEASE_HOST,
                testHost = BuildConfig.ADV_SERVER_TEST_HOST,
                parseTokenKey = BuildConfig.ADV_PLAY_INTEGRITY_PARSE_TOKEN_KEY,
            )
            firebase(
                analyticsEnabled = AdvertiseRuntimeCapabilities.FIREBASE_ANALYTICS_ENABLED,
                messagingEnabled = AdvertiseRuntimeCapabilities.FIREBASE_MESSAGING_ENABLED,
                subscribeDefaultTopic = AdvertiseRuntimeCapabilities.FIREBASE_SUBSCRIBE_DEFAULT_TOPIC,
            )
            remoteConfig(enabled = AdvertiseRuntimeCapabilities.REMOTE_CONFIG_ENABLED)
            thinking(
                enabled = AdvertiseRuntimeCapabilities.THINKING_ENABLED,
                appKey = BuildConfig.ADV_THINKING_APP_KEY,
                serverUrl = BuildConfig.ADV_THINKING_SERVER_URL,
            )
            singular(
                enabled = AdvertiseRuntimeCapabilities.SINGULAR_ENABLED,
                apiKey = BuildConfig.ADV_SINGULAR_API_KEY,
                secret = BuildConfig.ADV_SINGULAR_SECRET,
            )
            adMob(
                enabled = AdvertiseRuntimeCapabilities.ADMOB_ENABLED,
                appId = BuildConfig.ADV_ADMOB_APP_ID,
                bannerId = BuildConfig.ADV_ADMOB_BANNER_ID,
                interstitialId = BuildConfig.ADV_ADMOB_INTERSTITIAL_ID,
                nativeId = BuildConfig.ADV_ADMOB_NATIVE_ID,
                openId = BuildConfig.ADV_ADMOB_OPEN_ID,
                nativeIdsJson = BuildConfig.ADV_ADMOB_NATIVE_IDS_JSON,
            )
            facebook(
                enabled = AdvertiseRuntimeCapabilities.FACEBOOK_ENABLED,
                appId = BuildConfig.ADV_FACEBOOK_APP_ID,
                clientToken = BuildConfig.ADV_FACEBOOK_CLIENT_TOKEN,
            )
            tiktok(
                enabled = AdvertiseRuntimeCapabilities.TIKTOK_ENABLED,
                accessToken = BuildConfig.ADV_TIKTOK_ACCESS_TOKEN,
                ttAppId = BuildConfig.ADV_TIKTOK_TT_APP_ID,
                appId = BuildConfig.ADV_TIKTOK_APP_ID,
            )
            safe(
                enabled = AdvertiseRuntimeCapabilities.SAFE_ENABLED,
                expectedSignatures = BuildConfig.ADV_SAFE_EXPECTED_SIGNATURES,
                expectedPackageName = BuildConfig.APPLICATION_ID,
                rejectDebuggableBuilds = AdvertiseRuntimeCapabilities.SAFE_REJECT_DEBUGGABLE_BUILDS,
                rejectDebuggerAttached = AdvertiseRuntimeCapabilities.SAFE_REJECT_DEBUGGER_ATTACHED,
                killProcessOnFailure = AdvertiseRuntimeCapabilities.SAFE_KILL_PROCESS_ON_FAILURE,
            )
            push(
                enabled = AdvertiseRuntimeCapabilities.PUSH_ENABLED,
                persistentServiceEnabled = AdvertiseRuntimeCapabilities.PUSH_PERSISTENT_SERVICE_ENABLED,
                firebaseMessagingServiceEnabled = AdvertiseRuntimeCapabilities.PUSH_FIREBASE_MESSAGING_SERVICE_ENABLED,
                serviceStarterJobEnabled = AdvertiseRuntimeCapabilities.PUSH_SERVICE_STARTER_JOB_ENABLED,
                bootReceiverEnabled = AdvertiseRuntimeCapabilities.PUSH_BOOT_RECEIVER_ENABLED,
                notificationDeletedReceiverEnabled = AdvertiseRuntimeCapabilities.PUSH_NOTIFICATION_DELETED_RECEIVER_ENABLED,
                fileProviderEnabled = AdvertiseRuntimeCapabilities.PUSH_FILE_PROVIDER_ENABLED,
                deletionObserverEnabled = AdvertiseRuntimeCapabilities.PUSH_DELETION_OBSERVER_ENABLED,
            )
            notifications(NotificationFeatureConfig(enabled = AdvertiseRuntimeCapabilities.NOTIFICATIONS_ENABLED))
            playIntegrity(
                enabled = AdvertiseRuntimeCapabilities.PLAY_INTEGRITY_ENABLED,
                cloudProjectNumber = BuildConfig.ADV_PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER,
            )
        }
}
