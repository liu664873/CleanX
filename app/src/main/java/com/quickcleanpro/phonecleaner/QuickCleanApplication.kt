package com.quickcleanpro.phonecleaner

import android.app.Application
import com.quickcleanpro.phonecleaner.di.dataModule
import com.quickcleanpro.phonecleaner.di.presentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class QuickCleanApplication : Application() {
    companion object {
        lateinit var instance: QuickCleanApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        startKoin {
            androidLogger()
            androidContext(this@QuickCleanApplication)
            modules(dataModule, presentationModule)
        }
    }
}
