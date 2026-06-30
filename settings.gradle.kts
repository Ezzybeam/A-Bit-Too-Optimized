pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.fabricmc.net/")
        mavenCentral()
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.6"
}

stonecutter {
    create(rootProject) {
        versions("1.21.11", "26.1.2", "26.2")
        vcsVersion = "26.1.2"
    }
}

rootProject.name = "a-bit-too-optimized"
