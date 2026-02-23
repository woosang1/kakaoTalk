plugins {
    id("kakaotalk.android.library")
    id("kakaotalk.android.hilt")
}

android {
    namespace = "com.example.app_config"

    buildFeatures {
        buildConfig = true
    }

    productFlavors {
        named("dev") {
            buildConfigField("Boolean", "IS_TEST_SERVER", "true")
            buildConfigField("String", "BASE_URL", "\"ws://192.168.0.38:8080/\"")
        }
        named("prod") {
            buildConfigField("Boolean", "IS_TEST_SERVER", "false")
            buildConfigField("String", "BASE_URL", "\"wss://api.example.com/\"")
        }
    }
}

dependencies {
    implementation(project(":app-config:app-config-api"))
    implementation(project(":core:resource"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
}


