package com.pdffox.adv.push

import com.pdffox.adv.AdvertiseResourcesConfig
import com.pdffox.adv.PushConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PushHostContractTest {
	@Test
	fun `uses configured notification deleted action when present`() {
		assertEquals(
			"com.example.CUSTOM_ACTION",
			PushHostContract.notificationDeletedAction("com.example", "com.example.CUSTOM_ACTION"),
		)
	}

	@Test
	fun `falls back to package scoped notification deleted action`() {
		assertEquals(
			"com.example.ACTION_NOTIFICATION_DELETED",
			PushHostContract.notificationDeletedAction("com.example", null),
		)
	}

	@Test
	fun `uses configured file provider authority when present`() {
		assertEquals(
			"com.example.custom.fileprovider",
			PushHostContract.fileProviderAuthority("com.example", "com.example.custom.fileprovider"),
		)
	}

	@Test
	fun `falls back to package scoped advertise file provider authority`() {
		assertEquals(
			"com.example.pdffox.adv.fileprovider",
			PushHostContract.fileProviderAuthority("com.example", ""),
		)
	}

	@Test
	fun `advertise defaults do not bundle a host push config`() {
		assertEquals(0, AdvertiseResourcesConfig().pushConfigRawResId)
	}

	@Test
	fun `push components are enabled by default`() {
		val config = PushConfig()

		assertTrue(config.enabled)
		assertTrue(config.persistentServiceEnabled)
		assertTrue(config.firebaseMessagingServiceEnabled)
		assertTrue(config.serviceStarterJobEnabled)
		assertTrue(config.bootReceiverEnabled)
		assertTrue(config.notificationDeletedReceiverEnabled)
		assertTrue(config.fileProviderEnabled)
		assertTrue(config.deletionObserverEnabled)
	}
}
