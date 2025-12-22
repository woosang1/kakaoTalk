plugins {
    id("kakaotalk.android.library")
    id("kakaotalk.android.compose")
    id("kakaotalk.android.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.network"
}

dependencies {
    implementation(project(":app-config:app-config-api"))
    implementation(project(":core:model"))
    implementation(project(":core:utils"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.retrofit2.retrofit)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.retrofit2.kotlinx.serialization.converter)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.coil.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}


