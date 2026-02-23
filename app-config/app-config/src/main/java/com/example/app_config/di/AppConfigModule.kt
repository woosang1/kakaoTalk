package com.example.app_config.di

import com.example.app_config.AppConfigImpl
import com.example.app_config_api.AppConfig
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppConfigModule {
    @Binds
    @Singleton
    abstract fun bindAppConfig(impl: AppConfigImpl): AppConfig
}
