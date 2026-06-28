package com.pdffox.adv.notification

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Locale

class PushSceneResolverTest {
	@Test
	fun `parses legacy scene names into generic scene map`() {
		val config = Gson().fromJson(
			"""
			{
			  "version": "1.0",
			  "last_updated": "2026-05-11T00:00:00Z",
			  "unused_field": true,
			  "first_trigger_time": 60,
			  "scene": {
			    "delete_photos": {
			      "enabled": true,
			      "trigger_interval": 300,
			      "messages": [
			        {
			          "title": "Default title",
			          "content": "Default content",
			          "route": "/photos",
			          "keys": [
			            {
			              "language": "fr",
			              "title": "Titre",
			              "content": "Contenu"
			            }
			          ]
			        }
			      ]
			    }
			  }
			}
			""".trimIndent(),
			PushConfig::class.java,
		)

		val scene = PushSceneResolver.scene(config, "delete_photos")

		assertNotNull(scene)
		assertEquals(300L, scene!!.trigger_interval)
		assertEquals(60L, config.first_trigger_time)
	}

	@Test
	fun `supports host defined scene names`() {
		val config = PushConfig(
			scene = mapOf(
				"host_custom_scene" to PushScene(
					enabled = true,
					trigger_interval = 10,
					messages = listOf(
						Message(
							title = "Default",
							content = "Default body",
							route = "/custom",
							keys = listOf(Key(language = "es", title = "Titulo", content = "Contenido")),
						),
					),
				),
			),
		)

		val text = PushSceneResolver.firstMessageText(config, "host_custom_scene", Locale.forLanguageTag("es"))

		assertEquals("Titulo", text?.title)
		assertEquals("Contenido", text?.content)
		assertEquals("/custom", text?.route)
	}

	@Test
	fun `returns null when host did not map a scene`() {
		val config = PushConfig(scene = mapOf("available" to PushScene(enabled = true)))

		assertNull(PushSceneResolver.scene(config, ""))
		assertNull(PushSceneResolver.scene(config, "missing"))
	}
}
