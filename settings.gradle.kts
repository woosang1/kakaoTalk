pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "kakaoTalk"

// Application Module
include(":app")

// App Config Modules
include(":app-config:app-config")
include(":app-config:app-config-api")

// Core Modules
include(":core:ui")
include(":core:designsystem")
include(":core:utils")
include(":core:network")
include(":core:model")
include(":core:database")
include(":core:resource")
include(":core:base")
include(":core:testing")
include(":core:navigation")
