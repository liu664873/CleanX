package com.pdffox.adv.remoteconfig

import com.pdffox.adv.adv.policy.data.AdMapping
import com.pdffox.adv.adv.policy.data.Config as MappingConfig
import com.pdffox.adv.adv.policy.data.ConfigItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RemoteConfigRoutingTest {

	@Test
	fun `selectRouting returns nature config when nature mode is enabled`() {
		val selection = selectRouting(
			adMapping = sampleMapping(),
			context = RoutingContext(
				useNatureConfig = true,
				country = "KR",
				brand = "samsung"
			)
		)

		assertEquals("all_nature", selection?.topic)
		assertEquals("ad_policy_nature", selection?.adTag)
		assertEquals("notification_config_nature", selection?.notificationTag)
		assertEquals("adload_config_nature", selection?.preloadTag)
	}

	@Test
	fun `selectRouting matches brand and country ignoring case`() {
		val selection = selectRouting(
			adMapping = sampleMapping(),
			context = RoutingContext(
				useNatureConfig = false,
				country = "kr",
				brand = "Samsung"
			)
		)

		assertEquals("KRJA", selection?.topic)
		assertEquals("notification_config_KR", selection?.notificationTag)
	}

	@Test
	fun `selectRouting falls back when no config item matches`() {
		val selection = selectRouting(
			adMapping = sampleMapping(),
			context = RoutingContext(
				useNatureConfig = false,
				country = "US",
				brand = "google"
			)
		)

		assertNull(selection)
	}

	@Test
	fun `resolveRouting falls back field by field when targeted values are blank`() {
		val resolved = resolveRouting(
			selection = RoutingSelection(
				topic = "KRJA",
				adTag = "ad_policy",
				notificationTag = "notification_config_KR",
				preloadTag = "adload_config"
			),
			defaults = RoutingDefaults(
				adPolicy = "default-ad",
				notificationConfig = "default-notification",
				preloadConfig = "default-preload"
			),
			lookup = { tag ->
				when (tag) {
					"ad_policy" -> ""
					"notification_config_KR" -> "kr-notification"
					"adload_config" -> ""
					else -> ""
				}
			}
		)

		assertEquals("KRJA", resolved.topic)
		assertEquals("default-ad", resolved.adPolicy)
		assertEquals("kr-notification", resolved.notificationConfig)
		assertEquals("default-preload", resolved.preloadConfig)
	}

	@Test
	fun `selectRouting respects allowTargetedSelection gate`() {
		val selection = selectRouting(
			adMapping = sampleMapping(),
			context = RoutingContext(
				useNatureConfig = true,
				country = "KR",
				brand = "samsung"
			),
			allowTargetedSelection = false
		)

		assertNull(selection)
	}

	private fun sampleMapping(): AdMapping {
		return AdMapping(
			config = MappingConfig(
				ad = "ad_policy",
				notification = "notification_config",
				fcm_topic = "all",
				preload = "adload_config"
			),
			nature_config = MappingConfig(
				ad = "ad_policy_nature",
				notification = "notification_config_nature",
				fcm_topic = "all_nature",
				preload = "adload_config_nature"
			),
			configs = listOf(
				ConfigItem(
					countrys = listOf("ja", "ko", "jp", "kr"),
					brands = listOf("oppo", "samsung"),
					config = MappingConfig(
						ad = "ad_policy",
						notification = "notification_config_KR",
						fcm_topic = "KRJA",
						preload = "adload_config"
					)
				)
			)
		)
	}
}
