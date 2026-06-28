package com.pdffox.adv.adv

import com.pdffox.adv.Config
import com.pdffox.adv.adv.policy.AdPolicyManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DebugInterstitialPolicyTest {
	@Test
	fun `debug interstitial display still requires ad policy`() {
		val originalIsTest = Config.isTest
		val originalPolicy = AdPolicyManager.adPolicy

		try {
			Config.isTest = true
			AdPolicyManager.adPolicy = null

			assertFalse(AdPolicyManager.checkInterstitialAdUnit("missingInterstitialArea"))
		} finally {
			Config.isTest = originalIsTest
			AdPolicyManager.adPolicy = originalPolicy
		}
	}

	@Test
	fun `release interstitial display still requires ad policy`() {
		val originalIsTest = Config.isTest
		val originalPolicy = AdPolicyManager.adPolicy

		try {
			Config.isTest = false
			AdPolicyManager.adPolicy = null

			assertFalse(AdPolicyManager.checkInterstitialAdUnit("missingInterstitialArea"))
		} finally {
			Config.isTest = originalIsTest
			AdPolicyManager.adPolicy = originalPolicy
		}
	}

	@Test
	fun `debug interstitial preload bypasses ad load policy`() {
		val originalIsTest = Config.isTest
		val originalInterTimings = AdConfig.adload_trigger_timing_inter

		try {
			Config.isTest = true
			AdConfig.adload_trigger_timing_inter = emptyMap()

			assertTrue(AdConfig.isInterstitialLoadAllowedByPolicy(AdConfig.LOAD_TIME_ENTER_FEATURE))
		} finally {
			Config.isTest = originalIsTest
			AdConfig.adload_trigger_timing_inter = originalInterTimings
		}
	}

	@Test
	fun `release interstitial preload still uses ad load policy`() {
		val originalIsTest = Config.isTest
		val originalInterTimings = AdConfig.adload_trigger_timing_inter

		try {
			Config.isTest = false
			AdConfig.adload_trigger_timing_inter = emptyMap()

			assertFalse(AdConfig.isInterstitialLoadAllowedByPolicy(AdConfig.LOAD_TIME_ENTER_FEATURE))
		} finally {
			Config.isTest = originalIsTest
			AdConfig.adload_trigger_timing_inter = originalInterTimings
		}
	}
}
