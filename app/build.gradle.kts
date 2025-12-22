plugins {
    id("kakaotalk.android.application")
}

android {
    namespace = "com.example.kakaotalk"

    defaultConfig {
        applicationId = "com.example.kakaotalk"
        versionCode = 1
        versionName = "1.0.0"
    }

    buildFeatures {
        buildConfig = true
    }

    productFlavors {
        named("dev") {
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
        }
        named("prod") {
            // Production defaults
        }
    }
}

dependencies {
    implementation(project(":app-config:app-config"))
    implementation(project(":core:base"))
    implementation(project(":core:ui"))
    implementation(project(":core:utils"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.kotlinx.collections.immutable)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}