package com.quickcleanpro.phonecleaner.kit.device

import android.content.Context
import com.quickcleanpro.phonecleaner.data.repository.DeviceInfoRepositoryImpl
import com.quickcleanpro.phonecleaner.domain.repository.DeviceInfoRepository

object DeviceKit {
    fun create(context: Context): DeviceInfoRepository =
        DeviceInfoRepositoryImpl(context.applicationContext)
}
