package com.quickcleanpro.phonecleaner.kit.network

import android.content.Context
import com.quickcleanpro.phonecleaner.data.repository.AppUsageRepositoryImpl
import com.quickcleanpro.phonecleaner.data.repository.NetworkRepositoryImpl
import com.quickcleanpro.phonecleaner.domain.repository.AppUsageRepository
import com.quickcleanpro.phonecleaner.domain.repository.NetworkRepository

class NetworkKit private constructor(
    val appUsageRepository: AppUsageRepository,
    val networkRepository: NetworkRepository
) {
    companion object {
        fun create(context: Context): NetworkKit {
            val appContext = context.applicationContext
            return NetworkKit(
                appUsageRepository = AppUsageRepositoryImpl(appContext),
                networkRepository = NetworkRepositoryImpl(appContext)
            )
        }
    }
}
