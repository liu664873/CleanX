package com.pdffox.adv.remoteconfig

import com.pdffox.adv.adv.policy.NativePolicyManager
import com.pdffox.adv.adv.policy.data.parseAdNativePolicy
import com.pdffox.adv.adv.policy.data.parseAdPolicy
import com.pdffox.adv.notification.parseLanguages
import com.pdffox.adv.notification.parseNotificationConfig
import com.pdffox.adv.notification.parseNotificationContents
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteConfigSchemaCompatibilityTest {
	@Test
	fun `ad policy parses after unused fields are removed`() {
		val policy = parseAdPolicy(
			"""
			{
			  "package_name": "",
			  "global_ad_switch": true,
			  "first_open_enabled": true,
			  "limited": 50,
			  "limited_loadtime_seconds": 86400,
			  "ad_units": [
			    {
			      "areakey": "openPageAdv",
			      "rate": 1,
			      "frequency_caps": {
			        "max_per_hour": 4,
			        "max_per_day": 20,
			        "interval_seconds": 0
			      }
			    }
			  ]
			}
			""".trimIndent()
		)

		assertNotNull(policy)
		assertTrue(policy.global_ad_switch)
		assertEquals("openPageAdv", policy.ad_units.first().areakey)
	}

	@Test
	fun `ad policy ignores legacy unused fields when still present`() {
		val policy = parseAdPolicy(
			"""
			{
			  "package_name": "",
			  "platform": "android",
			  "global_ad_switch": true,
			  "first_open_enabled": true,
			  "limited": 50,
			  "limited_loadtime_seconds": 86400,
			  "ad_network": { "aggregator": "admob" },
			  "ad_units": [
			    {
			      "areakey": "openPageAdv",
			      "ad_format": "open",
			      "rate": 1,
			      "frequency_caps": {
			        "max_per_hour": 4,
			        "max_per_day": 20,
			        "interval_seconds": 0
			      }
			    }
			  ]
			}
			""".trimIndent()
		)

		assertNotNull(policy)
		assertEquals(50, policy.limited)
	}

	@Test
	fun `native policy parses after unused total caps and ad format are removed`() {
		val policy = parseAdNativePolicy(
			"""
			{
			  "limited": 500,
			  "limited_loadtime_seconds": 86400,
			  "ad_units": [
			    {
			      "areakey": "openMainNativeAdv",
			      "rate": 1,
			      "frequency_caps": {
			        "max_per_hour": 50,
			        "max_per_day": 50,
			        "interval_seconds": 0
			      }
			    }
			  ]
			}
			""".trimIndent()
		)

		assertNotNull(policy)
		assertEquals("openMainNativeAdv", policy.ad_units.first().areakey)

		NativePolicyManager.setPolicy(policy)
		assertEquals(policy.ad_units.first(), NativePolicyManager.getAdUnit("openMainNativeAdv"))
	}

	@Test
	fun `notification config parses after unused fields are removed`() {
		val config = parseNotificationConfig(
			"""
			{
			  "24HMax": 50,
			  "each_trigger_sent": 1,
			  "NMax": 5,
			  "is_foreground_send": false,
			  "triggers": [
			    {
			      "name": "screen_unlock",
			      "offset_second": 0,
			      "interval_second": 300
			    },
			    {
			      "name": "screen_on",
			      "offset_second": 0,
			      "interval_second": 0,
			      "configs": [
			        {
			          "name": "6-a",
			          "delay": 5,
			          "offset_second": 0,
			          "interval_second": 900
			        }
			      ]
			    }
			  ],
			  "timer": [
			    {
			      "HH": 9,
			      "MM": 0
			    }
			  ]
			}
			""".trimIndent()
		)

		assertNotNull(config)
		assertEquals("screen_unlock", config.triggers.first().name)
		assertEquals("screen_on", config.triggers[1].name)
		assertEquals(null, config.triggers[1].delay)
		assertEquals(9, config.timer.first().HH)
	}

	@Test
	fun `notification content parses after unused metadata and image fields are removed`() {
		val notices = parseNotificationContents(
			"""
			[
			  {
			    "Title": "Lost Photos Found!",
			    "Content": "Tap to restore them instantly.",
			    "Button": "Check",
			    "Languages": "{\"keys\":[{\"language\":\"ko\",\"title\":\"사진 발견\",\"content\":\"탭하여 복원하세요.\",\"button\":\"확인\"}]}",
			    "Route": "/recoverPhotos"
			  }
			]
			""".trimIndent()
		)

		assertEquals(1, notices.size)
		assertEquals("Lost Photos Found!", notices.first().Title)

		val languages = parseLanguages(notices.first().Languages)
		assertEquals("확인", languages.keys.first().button)
	}
}
