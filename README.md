# A Bit Too Optimized

A client-side Minecraft performance tune-up mod with quality presets
(Ultra, High, Normal, Low, Very Low, Potato) plus a first-run setup wizard,
hardware detection, and its own runtime optimizations. It works with or without
other performance mods (Sodium, Lithium, and friends) and stays compatible with
waypoint mods, Bobby, Voxy, fullbright, and shaders (Iris).

This repository currently contains Milestone 1 (Foundation): a buildable,
loadable, empty mod skeleton. Features are implemented in later milestones.

## Requirements

- Java 25 (required by Minecraft 26.1 and newer). A JDK 25 is referenced via
  `org.gradle.java.installations.paths` in `gradle.properties`.

## Supported versions

Built from one codebase with Stonecutter:

- 26.1.x (primary, developed on 26.1.2)
- 26.2.x

Minecraft 1.21.11 uses the older obfuscated toolchain and is planned for a later
milestone.

## Build

- Build the active version: `./gradlew build`
- Build all versions: `./build-all.sh` (switches active version per build, then resets to the default)
- Switch active version: `./gradlew "Set active project to 26.1.2"`
- Run the dev client: `./gradlew runClient`
- Run unit tests: `./gradlew test`

Jars are written under `versions/<version>/build/libs/`.

## Install (manual)

Copy the jar for your Minecraft version from `versions/<version>/build/libs/`
into your instance's `mods` folder, alongside Fabric API.
