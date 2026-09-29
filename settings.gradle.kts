pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

plugins {
    id("de.fayard.refreshVersions") version "0.51.0"
////                            # available:"0.60.0"
////                            # available:"0.60.1"
////                            # available:"0.60.2"
////                            # available:"0.60.3"
////                            # available:"0.60.4"
////                            # available:"0.60.5"
////                            # available:"0.60.6"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
    }
}
rootProject.name = "DSU Helper"
include(":app", ":hidden-api-stub", ":magisk-module")