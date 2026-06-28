package com.pdffox.adv.remoteconfig

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.pdffox.adv.Ads

/** Initializes Firebase Remote Config and applies local defaults before fetching. */
object RemoteConfigManager {
	private const val TAG = "RemoteConfigManager"

	@Volatile
	private var initialized = false

	fun initRemoteConfig() {
		if (initialized) {
			return
		}
		initialized = true

		val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
		val configSettings = remoteConfigSettings {
			minimumFetchIntervalInSeconds = 3600
		}
		remoteConfig.setConfigSettingsAsync(configSettings)
		remoteConfig.setDefaultsAsync(RemoteConfigDefaults.values(Ads.application))
			.addOnCompleteListener { task ->
				if (!task.isSuccessful) {
					Log.e(TAG, "setDefaultsAsync: ", task.exception)
				}
				fetchAndActivate(remoteConfig)
			}
		remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
			override fun onUpdate(configUpdate: ConfigUpdate) {
				activateAndUpdate(remoteConfig)
			}

			override fun onError(error: FirebaseRemoteConfigException) {
				Log.e(TAG, "onError: ", error)
			}
		})
	}

	private fun fetchAndActivate(remoteConfig: FirebaseRemoteConfig) {
		remoteConfig.fetchAndActivate()
			.addOnCompleteListener { task ->
				if (task.isSuccessful) {
					RemoteConfig.update(remoteConfig)
				} else {
					Log.e(TAG, "fetchAndActivate: ", task.exception)
					RemoteConfig.update(remoteConfig)
				}
			}
	}

	private fun activateAndUpdate(remoteConfig: FirebaseRemoteConfig) {
		remoteConfig.activate()
			.addOnCompleteListener { task ->
				if (task.isSuccessful) {
					RemoteConfig.update(remoteConfig)
				} else {
					Log.e(TAG, "activateAndUpdate: ", task.exception)
				}
			}
	}
}
