package com.example.app_config

import com.example.app_config_api.AppConfig
import javax.inject.Inject

class AppConfigImpl @Inject constructor() : AppConfig {
    override val baseUrl: String = BuildConfig.BASE_URL
    override val isTestServer: Boolean = BuildConfig.IS_TEST_SERVER
}
