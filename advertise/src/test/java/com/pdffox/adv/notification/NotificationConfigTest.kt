package com.pdffox.adv.notification

import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationConfigTest {
	@Test
	fun `missing one hour max falls back to unlimited quota`() {
		val config = parseNotificationConfig(
			"""
			{
			  "24HMax": 50,
			  "each_trigger_sent": 10,
			  "NMax": 5,
			  "is_foreground_send": false,
			  "triggers": [],
			  "timer": []
			}
			""".trimIndent()
		)

		assertEquals(Int.MAX_VALUE, config.`1HMax`)
	}

	@Test
	fun `runtime strategy config parses trigger and timer metadata`() {
		val config = parseNotificationConfig(
			"""
			{
			  "24HMax": 50,
			  "1HMax": 20,
			  "each_trigger_sent": 10,
			  "NMax": 5,
			  "is_foreground_send": false,
			  "interval_second": 60,
			  "triggers": [
			    {
			      "id": "1",
			      "name": "screen_unlock",
			      "delay": 0
			    }
			  ],
			  "timer": [
			    {
			      "id": "timer-a",
			      "name": "timer-a",
			      "HH": 6,
			      "MM": 0
			    }
			  ]
			}
			""".trimIndent()
		)

		assertEquals(20, config.`1HMax`)
		assertEquals(60L, config.interval_second)
		assertEquals("1", config.triggers.first().id)
		assertEquals(0L, config.triggers.first().delay)
		assertEquals("timer-a", config.timer.first().id)
		assertEquals("timer-a", config.timer.first().name)
	}

	@Test
	fun `notification content accepts may metadata fields`() {
		val notices = parseNotificationContents(
			"""
			[
			  {
			    "Id": 1,
			    "AppName": "Photo Recovery",
			    "AppPackage": "com.example",
			    "Policy": 1,
			    "NoticeId": "notice-1",
			    "Title": "Lost Photos Found!",
			    "Content": "Tap to restore them instantly.",
			    "Button": "Check",
			    "Icon": "photo",
			    "Img": "https://example.com/image.png",
			    "Languages": "{\"keys\":[{\"language\":\"en\",\"title\":\"Title\",\"content\":\"Body\",\"img\":\"img\",\"button\":\"Open\"}]}",
			    "Route": "/recoverPhotos"
			  }
			]
			""".trimIndent()
		)

		assertEquals("notice-1", notices.first().NoticeId)
		assertEquals("img", parseLanguages(notices.first().Languages).keys.first().img)
	}
}
