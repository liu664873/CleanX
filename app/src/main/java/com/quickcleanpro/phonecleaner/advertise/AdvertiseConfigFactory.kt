package com.quickcleanpro.phonecleaner.advertise

import android.app.Application
import com.pdffox.adv.AdvertiseSdkConfig
import com.pdffox.adv.AdvertiseSdkConfigs
import com.pdffox.adv.NotificationFeatureConfig
import com.quickcleanpro.phonecleaner.BuildConfig
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.config.AdvSdkConfig

object AdvertiseConfigFactory {
    fun create(context: Application, config: AdvSdkConfig): AdvertiseSdkConfig =
        AdvertiseSdkConfigs.create(context, BuildConfig.DEBUG) {
            legal(
                privacyUrl = config.privacyUrl,
                termsUrl = config.termsUrl,
            )
            defaultTopic(config.defaultTopic)
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
                releaseHost = config.server.releaseHost,
                testHost = config.server.testHost,
                parseTokenKey = config.playIntegrity.parseTokenKey,
            )
            firebase(
                analyticsEnabled = AdvertiseRuntimeCapabilities.FIREBASE_ANALYTICS_ENABLED,
                messagingEnabled = AdvertiseRuntimeCapabilities.FIREBASE_MESSAGING_ENABLED,
                subscribeDefaultTopic = AdvertiseRuntimeCapabilities.FIREBASE_SUBSCRIBE_DEFAULT_TOPIC,
            )
            remoteConfig(enabled = AdvertiseRuntimeCapabilities.REMOTE_CONFIG_ENABLED)
            thinking(
                enabled = AdvertiseRuntimeCapabilities.THINKING_ENABLED,
                appKey = config.thinking.appKey,
                serverUrl = config.thinking.serverUrl,
            )
            singular(
                enabled = AdvertiseRuntimeCapabilities.SINGULAR_ENABLED,
                apiKey = config.singular.apiKey,
                secret = config.singular.secret,
            )
            adMob(
                enabled = AdvertiseRuntimeCapabilities.ADMOB_ENABLED,
                appId = config.admob.appId,
                bannerId = config.admob.bannerId,
                interstitialId = config.admob.interstitialId,
                nativeId = config.admob.nativeId,
                openId = config.admob.openId,
                nativeIdsJson = config.admob.nativeIdsJson,
            )
            facebook(
                enabled = AdvertiseRuntimeCapabilities.FACEBOOK_ENABLED,
                appId = config.facebook.appId,
                clientToken = config.facebook.clientToken,
            )
            tiktok(
                enabled = AdvertiseRuntimeCapabilities.TIKTOK_ENABLED,
                accessToken = config.tiktok.accessToken,
                ttAppId = config.tiktok.ttAppId,
                appId = config.tiktok.appId,
            )
            safe(
                enabled = AdvertiseRuntimeCapabilities.SAFE_ENABLED,
                expectedSignatures = config.safe.expectedSignatures,
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
                cloudProjectNumber = config.playIntegrity.cloudProjectNumber,
            )
        }
}
