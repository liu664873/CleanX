package com.pdffox.adv

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element

class AdvertiseSdkConfigsTest {
	@Test
	fun `uses debug default topic when host topic is blank`() {
		val config = AdvertiseSdkConfigs.create("com.example", isDebug = true) {
			defaultTopic("")
		}

		assertEquals("com.example", config.packageName)
		assertEquals("debug-all", config.defaultTopic)
	}

	@Test
	fun `uses release default topic when host topic is blank`() {
		val config = AdvertiseSdkConfigs.create("com.example", isDebug = false) {
			defaultTopic("")
		}

		assertEquals("all", config.defaultTopic)
	}

	@Test
	fun `keeps host configured topic`() {
		val config = AdvertiseSdkConfigs.create("com.example", isDebug = true) {
			defaultTopic("custom-topic")
		}

		assertEquals("custom-topic", config.defaultTopic)
	}

	@Test
	fun `does not enforce safe signatures in debug by default`() {
		val config = AdvertiseSdkConfigs.create("com.example", isDebug = true) {
			safe(
				enabled = true,
				expectedSignatures = "aa:bb, cc:dd",
				rejectDebuggableBuilds = true,
				rejectDebuggerAttached = true,
				killProcessOnFailure = true,
			)
		}

		assertTrue(config.safe.enabled)
		assertTrue(config.safe.expectedSignatures.isEmpty())
		assertFalse(config.safe.rejectDebuggableBuilds)
		assertFalse(config.safe.rejectDebuggerAttached)
		assertFalse(config.safe.killProcessOnFailure)
	}

	@Test
	fun `parses safe signatures in release`() {
		val config = AdvertiseSdkConfigs.create("com.example", isDebug = false) {
			safe(
				enabled = true,
				expectedSignatures = "aa:bb, cc:dd\n ee:ff",
			)
		}

		assertEquals(setOf("aa:bb", "cc:dd", "ee:ff"), config.safe.expectedSignatures)
		assertTrue(config.safe.rejectDebuggableBuilds)
		assertTrue(config.safe.rejectDebuggerAttached)
		assertTrue(config.safe.killProcessOnFailure)
	}

	@Test
	fun `maps host feature configuration into sdk config`() {
		val config = AdvertiseSdkConfigs.create("com.example", isDebug = false) {
			legal(privacyUrl = "https://example.com/privacy", termsUrl = "https://example.com/terms")
			resources(
				adLoadConfigRawResId = 123,
				nativeAdPolicyRawResId = 234,
				nativeAdIdsRawResId = 345,
				pushConfigRawResId = 456,
			)
			server(
				enabled = true,
				releaseHost = "https://api.example.com",
				testHost = "https://test.example.com",
				parseTokenKey = "token-key",
			)
			firebase(
				analyticsEnabled = true,
				messagingEnabled = true,
				subscribeDefaultTopic = true,
			)
			remoteConfig(enabled = true)
			thinking(enabled = true, appKey = "thinking-key", serverUrl = "https://thinking.example.com")
			singular(enabled = true, apiKey = "singular-key", secret = "singular-secret")
			adMob(
				enabled = true,
				appId = "app-id",
				bannerId = "banner-id",
				interstitialId = "interstitial-id",
				nativeId = "native-id",
				openId = "open-id",
			)
			facebook(enabled = true, appId = "facebook-id", clientToken = "facebook-token")
			tiktok(enabled = true, accessToken = "access-token", ttAppId = "tt-app-id", appId = "")
			push(
				enabled = true,
				persistentServiceEnabled = true,
				firebaseMessagingServiceEnabled = true,
				serviceStarterJobEnabled = true,
				bootReceiverEnabled = true,
				notificationDeletedReceiverEnabled = true,
				fileProviderEnabled = true,
				deletionObserverEnabled = true,
				sceneKeys = PushSceneKeyConfig(imageDeleted = "image-scene"),
			)
			notifications(NotificationFeatureConfig(enabled = true, smallIconResId = 456))
			playIntegrity(enabled = true, cloudProjectNumber = 789L)
		}

		assertEquals("https://example.com/privacy", config.privacyUrl)
		assertEquals("https://example.com/terms", config.termsUrl)
		assertEquals(123, config.resources.adLoadConfigRawResId)
		assertEquals(234, config.resources.nativeAdPolicyRawResId)
		assertEquals(345, config.resources.nativeAdIdsRawResId)
		assertEquals(456, config.resources.pushConfigRawResId)
		assertTrue(config.server.enabled)
		assertEquals("token-key", config.server.parseTokenKey)
		assertTrue(config.firebase.analyticsEnabled)
		assertTrue(config.firebase.messagingEnabled)
		assertTrue(config.firebase.subscribeDefaultTopic)
		assertTrue(config.remoteConfig.enabled)
		assertEquals("thinking-key", config.thinking.appKey)
		assertEquals("singular-key", config.singular.apiKey)
		assertEquals("app-id", config.adMob.appId)
		assertEquals("facebook-id", config.facebook.appId)
		assertEquals(null, config.tiktok.appId)
		assertTrue(config.push.enabled)
		assertEquals("image-scene", config.push.sceneKeys.imageDeleted)
		assertEquals(456, config.notifications.smallIconResId)
		assertEquals(789L, config.playIntegrity.cloudProjectNumber)
	}

	@Test
	fun `public app open controls expose current defaults`() {
		assertEquals(Ads.guidePageSwapTime, AdvertiseSdk.guidePageSwapTime)
		assertTrue(AdvertiseSdk.isAppOpenAdEnabled)
		assertFalse(AdvertiseSdk.suppressNextAppOpenAd)
	}

	@Test
	fun `local config samples no longer contain removed guide and policy switches`() {
		val sampleDir = findRepoFile("advertise/src/main/res/raw")
		val remoteConfig = sampleDir
			.walkTopDown()
			.filter { it.isFile && it.extension == "json" }
			.joinToString(separator = "\n") { it.readText() }

		assertFalse(remoteConfig.contains("isNewAdPolicy"))
		assertFalse(remoteConfig.contains("ignoreGuide"))
		assertFalse(remoteConfig.contains("hasOpenLaungPage"))
	}

	@Test
	fun `manifest components are fixed enabled without placeholders`() {
		val manifestFile = findRepoFile("advertise/src/main/AndroidManifest.xml")
		val manifestText = manifestFile.readText()
		val removedPlaceholders = listOf(
			"advPersistentServiceEnabled",
			"advFirebaseMessagingServiceEnabled",
			"advServiceStarterJobEnabled",
			"advNotificationDeletedReceiverEnabled",
			"advBootReceiverEnabled",
			"advFileProviderEnabled",
		)

		removedPlaceholders.forEach { placeholder ->
			assertFalse("Manifest should not contain $placeholder", manifestText.contains(placeholder))
		}

		val androidNamespace = "http://schemas.android.com/apk/res/android"
		val document = DocumentBuilderFactory.newInstance()
			.apply { isNamespaceAware = true }
			.newDocumentBuilder()
			.parse(manifestFile)
		val expectedComponents = mapOf(
			"service" to listOf(
				".notification.CommonService",
				".push.MyFirebaseMessagingService",
				".push.ServiceStarterJobService",
			),
			"receiver" to listOf(
				".notification.NotificationDeletedReceiver",
				".notification.BootReceiver",
			),
			"provider" to listOf("androidx.core.content.FileProvider"),
		)

		expectedComponents.forEach { (tagName, componentNames) ->
			componentNames.forEach { componentName ->
				val component = document.findComponent(tagName, androidNamespace, componentName)
				assertNotNull("$tagName $componentName should exist", component)
				assertEquals(
					"$tagName $componentName should be fixed enabled",
					"true",
					component!!.getAttributeNS(androidNamespace, "enabled"),
				)
			}
		}
	}

	private fun findRepoFile(relativePath: String): File {
		return generateSequence(File("").absoluteFile) { it.parentFile }
			.map { File(it, relativePath) }
			.first { it.exists() }
	}

	private fun org.w3c.dom.Document.findComponent(
		tagName: String,
		androidNamespace: String,
		componentName: String,
	): Element? {
		val nodes = getElementsByTagName(tagName)
		return (0 until nodes.length)
			.asSequence()
			.map { nodes.item(it) as Element }
			.firstOrNull { it.getAttributeNS(androidNamespace, "name") == componentName }
	}
}
