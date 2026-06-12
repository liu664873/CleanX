package com.quickcleanpro.phonecleaner.di

import com.quickcleanpro.phonecleaner.data.repository.DeviceInfoRepositoryImpl
import com.quickcleanpro.phonecleaner.domain.repository.DeviceInfoRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

val dataModule: Module = module{
    single<DeviceInfoRepository> { DeviceInfoRepositoryImpl(androidContext()) }
}
