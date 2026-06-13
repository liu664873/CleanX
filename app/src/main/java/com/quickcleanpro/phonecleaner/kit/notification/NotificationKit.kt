package com.quickcleanpro.phonecleaner.kit.notification

import android.content.Context
import com.quickcleanpro.phonecleaner.data.repository.NotificationRepositoryImpl
import com.quickcleanpro.phonecleaner.domain.repository.NotificationRepository

class NotificationKit private constructor(
    val repository: NotificationRepository,
) {
    companion object {
        fun create(context: Context): NotificationKit = NotificationKit(NotificationRepositoryImpl(context.applicationContext))
    }
}
