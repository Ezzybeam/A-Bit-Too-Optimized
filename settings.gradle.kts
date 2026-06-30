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
        // 1.21.11 (legacy obfuscated toolchain, Java 21, loom-remap) is deferred
        // to a later milestone. The foundation targets the modern non-obfuscated
        // toolchain only: 26.1.x (main, 26.1.2) and 26.2.x, both on Java 25.
        versions("26.1.2", "26.2")
        vcsVersion = "26.1.2"
    }
}

rootProject.name = "a-bit-too-optimized"
