plugins {
    id("net.fabricmc.fabric-loom") version "1.15.5"
    id("dev.kikugie.stonecutter")
}

val modId = project.property("mod_id") as String
val modVersion = project.property("mod_version") as String
val mavenGroup = project.property("maven_group") as String
val minecraftVersion = project.property("minecraft_version") as String
val fabricLoaderVersion = project.property("fabric_loader_version") as String
val fabricApiVersion = project.property("fabric_api_version") as String
val modmenuVersion = project.property("modmenu_version") as String

group = mavenGroup
// Jar name pattern: a-bit-too-optimized-<minecraft_version>-<abto_version>.jar
// archivesName carries the slug + Minecraft version; version carries the mod
// version, so Gradle joins them as <archivesName>-<version>.jar.
version = modVersion
base.archivesName.set("${rootProject.name}-$minecraftVersion")

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
    withSourcesJar()
}

repositories {
    maven("https://maven.fabricmc.net/")
    maven("https://maven.terraformersmc.com/releases/")
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    // No mappings line: Minecraft 26.1+ is non-obfuscated.
    implementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    implementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
    implementation("com.terraformersmc:modmenu:$modmenuVersion")

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.processResources {
    val props = mapOf(
        "mod_id" to modId,
        "mod_version" to modVersion,
        "mc_dep_range" to (project.property("mc_dep_range") as String),
        "fabric_loader_version" to fabricLoaderVersion
    )
    inputs.properties(props)
    filesMatching("fabric.mod.json") {
        expand(props)
    }
}
