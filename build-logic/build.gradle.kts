import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    `kotlin-dsl`}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    implementation(libs.findLibrary("android-gradlePlugin").get())
    implementation(libs.findLibrary("kotlin-gradlePlugin").get())
    implementation(libs.findLibrary("compose-compiler-gradle-plugin").get())
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.6")
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "kakaotalk.android.application"
            implementationClass = "com.example.build_logic.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "kakaotalk.android.library"
            implementationClass = "com.example.build_logic.AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "kakaotalk.android.compose"
            implementationClass = "com.example.build_logic.AndroidComposeConventionPlugin"
        }
        register("androidHilt") {
            id = "kakaotalk.android.hilt"
            implementationClass = "com.example.build_logic.AndroidHiltConventionPlugin"
        }
    }
}


