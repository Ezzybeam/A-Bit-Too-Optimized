# A Bit Too Optimized - Milestone 1: Foundation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Produce a buildable, loadable, empty Fabric client mod that compiles and runs in-game on the two modern target Minecraft version lines (26.1.x, 26.2.x) from one codebase. (1.21.11 is deferred to a later milestone because it uses the legacy obfuscated toolchain.)

**Architecture:** One Gradle project using the new `net.fabricmc.fabric-loom` plugin, driven by Stonecutter for multi-version builds. Minecraft 26.1+ ships NON-obfuscated, so there is no mappings step: omit the `mappings` line, use `implementation` (not `modImplementation`), and use the `jar` task (not `remapJar`). A single client entrypoint logs a startup line so we can confirm the mod loads. No features yet; this milestone only proves the toolchain and project skeleton work end to end.

**Deferred (1.21.11):** 1.21.11 is the last obfuscated version. Supporting it needs the legacy `net.fabricmc.fabric-loom-remap` plugin, a mappings line, `modImplementation`, `remapJar`, and Java 21. It will be added as its own Stonecutter branch with per-branch build conditionals in a later milestone. The verified 1.21.11 values were: fabric_loader 0.19.3, fabric_api 0.141.4+1.21.11, range >=1.21.11 <1.21.12.

**Tech Stack:** Java 25, Gradle 9.4.0 (wrapper), Fabric Loom 1.15.5 (`net.fabricmc.fabric-loom`, non-remapping), Stonecutter 0.9.6, no mappings (Minecraft 26.1+ is non-obfuscated), Fabric API, Fabric Loader, JUnit 5 (for the unit-test harness used in later milestones).

> Toolchain note (added after Task 2): the modern Minecraft 26.x toolchain is Gradle 9.x + Loom 1.15 + Stonecutter 0.9.x. Loom 1.12+ supports Gradle 9, and Stonecutter 0.9.x requires Gradle 9. An earlier draft of this plan pinned Gradle 8.10; that was corrected to Gradle 9.4.0.

## Global Constraints

- Mod id: `abto`. Display name: `A Bit Too Optimized`.
- Client-side only: `fabric.mod.json` declares the `client` environment. The mod must never be required on a server.
- No em dashes and no emojis, and plain ASCII only, in all PROJECT-AUTHORED files (Java source, config files we write, GUI text, log messages, docs we author). This does NOT apply to vendor or tool-generated files we do not hand-write (the Gradle wrapper `gradlew`, `gradlew.bat`, `gradle/wrapper/`), which may contain non-ASCII characters in generated comments. Do not hand-edit generated files to satisfy this rule. See CLAUDE.md.
- Java 25 toolchain for build and run. Minecraft 26.1+ requires Java 25 (Java 21 will not build or run it). JDK 25 (Temurin) is installed in a user dir at `/Users/ezzybeam/.jdks/jdk-25.0.3+9/Contents/Home` and referenced via `org.gradle.java.installations.paths` in gradle.properties so Gradle's toolchain finds it. Gradle itself runs on the PATH Java (21), which is fine; the build toolchain is 25.
- Mappings: NONE. Minecraft 26.1+ is non-obfuscated, so the new `net.fabricmc.fabric-loom` plugin does not remap. Do not add a `mappings` line. Use `implementation` (not `modImplementation`) and the `jar` task (not `remapJar`).
- Target version lines and their declared ranges (foundation = modern toolchain only):
  - `26.1.x` branch (primary, tested on 26.1.2), depends range `>=26.1 <26.2`
  - `26.2.x` branch, depends range `>=26.2 <26.3`
  - `1.21.11` is DEFERRED (legacy obfuscated toolchain) to a later milestone.
- Stonecutter is the only sanctioned way to express per-version code differences.
- Platforms: macOS and Windows are the tested platforms.

---

## File Structure

This milestone creates the project skeleton. Files and their single responsibilities:

- `settings.gradle.kts` - Gradle settings, plugin management, Stonecutter version setup (declares the two modern branches: 26.1.2, 26.2).
- `build.gradle.kts` - Loom + Fabric build config, dependencies, Java 25 toolchain, NO mappings (non-obfuscated), version values read from per-branch properties.
- `gradle.properties` - shared properties (mod id, version, group) and the JDK 25 installation path.
- `versions/26.1.2/gradle.properties` - per-branch versions for the 26.1.x line (developed on 26.1.2).
- `versions/26.2/gradle.properties` - per-branch versions for the 26.2.x line.
- `src/main/java/com/abto/AbtoClient.java` - the client entrypoint; logs a startup line. Single responsibility: prove the mod loads.
- `src/main/java/com/abto/Abto.java` - shared constants (MOD_ID, logger). Single responsibility: one place for the id and logger.
- `src/main/resources/fabric.mod.json` - mod metadata; client entrypoint, dependencies, version ranges (the Minecraft range is templated per branch).
- `src/test/java/com/abto/AbtoSmokeTest.java` - a trivial JUnit test that proves the unit-test harness runs (later milestones rely on it).
- `gradle/wrapper/...`, `gradlew`, `gradlew.bat` - Gradle wrapper, so builds are reproducible without a system Gradle.
- `.gitignore` - ignore build outputs, `.gradle`, `run/`, `versions/*/build`, IDE files.
- `README.md` - one short paragraph: what the mod is, how to build, how to switch active version.

Note on versions in this plan: the Loom, Fabric Loader, and Fabric API version strings below are concrete starting values. If a Gradle sync fails because a newer build is required, the failing task includes a step to look up the current value on the listed source and update the property. This is expected for very recent Minecraft versions.

---

## Task 1: Gradle wrapper and project skeleton

**Files:**
- Create: `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`, `gradle/wrapper/gradle-wrapper.properties`
- Create: `.gitignore`
- Create: `gradle.properties`

**Interfaces:**
- Consumes: nothing (first task).
- Produces: a Gradle wrapper invocable as `./gradlew` (macOS) and `gradlew.bat` (Windows), and `gradle.properties` defining `mod_id=abto`, `mod_version=0.1.0`, `maven_group=com.abto`.

- [ ] **Step 1: Generate the Gradle wrapper**

Run (requires a system Gradle once, or use an IDE to generate it):
```bash
cd "$PROJECT_ROOT"
gradle wrapper --gradle-version 8.10 --distribution-type all
```
Expected: creates `gradlew`, `gradlew.bat`, and `gradle/wrapper/` files.

- [ ] **Step 2: Create `gradle.properties`**

```properties
# Shared project properties
org.gradle.jvmargs=-Xmx2G
org.gradle.parallel=true

mod_id=abto
mod_version=0.1.0
mod_name=A Bit Too Optimized
maven_group=com.abto
```

- [ ] **Step 3: Create `.gitignore`**

```gitignore
.gradle/
build/
versions/*/build/
run/
*.log
.idea/
*.iml
.DS_Store
```

- [ ] **Step 4: Verify the wrapper runs**

Run:
```bash
./gradlew --version
```
Expected: prints Gradle 8.10 and JVM 21. If JVM is not 21, install Temurin 21 and set `JAVA_HOME` before continuing.

- [ ] **Step 5: Commit**

```bash
git add gradlew gradlew.bat gradle/ gradle.properties .gitignore
git commit -m "chore: add gradle wrapper and project skeleton"
```

---

## Task 2: Stonecutter settings and per-branch version properties

**Files:**
- Create: `settings.gradle.kts`
- Create: `versions/1.21.11/gradle.properties`
- Create: `versions/26.1.2/gradle.properties`
- Create: `versions/26.2/gradle.properties`

**Interfaces:**
- Consumes: `gradle.properties` from Task 1.
- Produces: a Stonecutter setup with three registered versions named `1.21.11`, `26.1.2`, `26.2`, with `26.1.2` as the active/default. Each branch exposes properties: `minecraft_version`, `mc_dep_range`, `fabric_loader_version`, `fabric_api_version`.

- [ ] **Step 1: Create `settings.gradle.kts`**

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
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
```

- [ ] **Step 2: Create `versions/1.21.11/gradle.properties`**

```properties
minecraft_version=1.21.11
mc_dep_range=>=1.21.11 <1.21.12
fabric_loader_version=0.17.2
fabric_api_version=0.116.0+1.21.11
```

- [ ] **Step 3: Create `versions/26.1.2/gradle.properties`**

```properties
minecraft_version=26.1.2
mc_dep_range=>=26.1 <26.2
fabric_loader_version=0.17.2
fabric_api_version=0.120.0+26.1
```

- [ ] **Step 4: Create `versions/26.2/gradle.properties`**

```properties
minecraft_version=26.2
mc_dep_range=>=26.2 <26.3
fabric_loader_version=0.17.2
fabric_api_version=0.122.0+26.2
```

- [ ] **Step 5: Verify Stonecutter is recognized**

Run:
```bash
./gradlew stonecutterVersions --stacktrace
```
Expected: lists `1.21.11`, `26.1.2`, `26.2` with `26.1.2` active. If the plugin version 0.9.6 is rejected, check https://plugins.gradle.org/plugin/dev.kikugie.stonecutter for the current version and update it in `settings.gradle.kts`, then re-run.

- [ ] **Step 6: Commit**

```bash
git add settings.gradle.kts versions/
git commit -m "chore: configure stonecutter with three version branches"
```

---

## Task 3: Loom build configuration (modern, non-obfuscated)

**Files:**
- Create: `build.gradle.kts`

**Interfaces:**
- Consumes: per-branch properties from Task 2 (`minecraft_version`, `fabric_loader_version`, `fabric_api_version`), shared properties from Task 1 (`mod_id`, `mod_version`, `maven_group`).
- Produces: a Loom build that compiles against the active branch with NO mappings (non-obfuscated) and produces a jar via the standard `jar` task. The jar is named `a-bit-too-optimized-<minecraft_version>-<abto_version>.jar` (archivesName = `${rootProject.name}-${minecraft_version}`, version = the mod version).

Reference implementation: the official Fabric example mod, 26.1 branch:
https://github.com/FabricMC/fabric-example-mod/tree/26.1 (use its build.gradle as the source of truth for the modern non-remapping setup).

- [ ] **Step 1: Create `build.gradle.kts`**

Key differences from a pre-26.1 setup (do NOT use the old form):
- NO `mappings(...)` line. Minecraft is non-obfuscated; adding mappings fails with "Cannot use Mojang mappings in a non-obfuscated environment".
- Use `implementation(...)`, NOT `modImplementation(...)`, for fabric-loader and fabric-api.
- The build artifact is the standard `jar` task output; there is no `remapJar`.
- Java toolchain language version is 25, not 21.

```kotlin
plugins {
    id("net.fabricmc.fabric-loom") version "1.15.5"
    id("dev.kikugie.stonecutter")
}

// Read properties by their real snake_case keys. Do NOT use `by project`, which
// would look up a key matching the val name (e.g. mavenGroup) instead of maven_group.
val modId = project.property("mod_id") as String
val modVersion = project.property("mod_version") as String
val mavenGroup = project.property("maven_group") as String
val minecraftVersion = project.property("minecraft_version") as String
val fabricLoaderVersion = project.property("fabric_loader_version") as String
val fabricApiVersion = project.property("fabric_api_version") as String

group = mavenGroup
// Jar name pattern: a-bit-too-optimized-<minecraft_version>-<abto_version>.jar
version = modVersion
base.archivesName.set("${rootProject.name}-$minecraftVersion")

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
    withSourcesJar()
}

repositories {
    maven("https://maven.fabricmc.net/")
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    // No mappings line: Minecraft 26.1+ is non-obfuscated.
    implementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    implementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
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
```

Note: the exact Loom configuration API for non-obfuscated builds is evolving. Treat the fabric-example-mod 26.1 branch build.gradle as authoritative; if `implementation` for fabric-loader/fabric-api does not resolve or Loom expects a different configuration name, follow the example mod and record the deviation.

- [ ] **Step 2: Read the gradle.properties values into the build**

Confirm `build.gradle.kts` reads `mc_dep_range` (used in `processResources`). It is declared per branch in Task 2. No code change if Step 1 already includes it; this step is a verification read.

Run:
```bash
./gradlew properties --stacktrace | grep -E "minecraft_version|fabric_loader_version|fabric_api_version|mc_dep_range"
```
Expected: prints the active branch (26.1.2) values.

- [ ] **Step 3: Verify dependencies resolve for the active branch**

Run:
```bash
./gradlew dependencies --configuration modImplementation --stacktrace
```
Expected: resolves `fabric-loader` and `fabric-api` without error. If a version is not found, look up the current value: Fabric Loader and API at https://fabricmc.net/develop/ , then update the failing branch property in `versions/26.1.2/gradle.properties` and re-run.

- [ ] **Step 4: Commit**

```bash
git add build.gradle.kts
git commit -m "chore: add loom build config with mojang mappings"
```

---

## Task 4: Mod metadata (fabric.mod.json), client-only

**Files:**
- Create: `src/main/resources/fabric.mod.json`

**Interfaces:**
- Consumes: `processResources` templating from Task 3 (`${mod_id}`, `${mod_version}`, `${mc_dep_range}`, `${fabric_loader_version}`).
- Produces: metadata that registers a client entrypoint `com.abto.AbtoClient`, marks the mod client-environment, and declares dependencies. Later milestones add more entrypoints under the same `client` list.

- [ ] **Step 1: Create `src/main/resources/fabric.mod.json`**

```json
{
  "schemaVersion": 1,
  "id": "${mod_id}",
  "version": "${mod_version}",
  "name": "A Bit Too Optimized",
  "description": "A performance tune-up mod with quality presets. Client side only.",
  "authors": ["ezzybeam"],
  "license": "ARR",
  "environment": "client",
  "entrypoints": {
    "client": ["com.abto.AbtoClient"]
  },
  "depends": {
    "fabricloader": ">=${fabric_loader_version}",
    "minecraft": "${mc_dep_range}",
    "java": ">=25",
    "fabric-api": "*"
  }
}
```

Note: `license` is set to `ARR` (all rights reserved) as a placeholder license string only; the actual license choice is a spec open question and is decided before first publish. This is a metadata string, not a code placeholder.

- [ ] **Step 2: Verify templating expands at build time**

Run:
```bash
./gradlew processResources --stacktrace
```
Then inspect the generated file:
```bash
cat build/resources/main/fabric.mod.json
```
Expected: `${mod_id}` became `abto`, `${mc_dep_range}` became `>=26.1 <26.2`, etc. No unexpanded `${...}` tokens remain.

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/fabric.mod.json
git commit -m "feat: add client-only mod metadata"
```

---

## Task 5: Shared constants and client entrypoint

**Files:**
- Create: `src/main/java/com/abto/Abto.java`
- Create: `src/main/java/com/abto/AbtoClient.java`

**Interfaces:**
- Consumes: Fabric Loader's `ClientModInitializer`, the `fabric.mod.json` entrypoint from Task 4.
- Produces: `Abto.MOD_ID` (String constant `"abto"`) and `Abto.LOGGER` (an SLF4J `Logger`), used by every later milestone. `AbtoClient.onInitializeClient()` logs one startup line.

- [ ] **Step 1: Create `src/main/java/com/abto/Abto.java`**

```java
package com.abto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Abto {
    public static final String MOD_ID = "abto";
    public static final Logger LOGGER = LoggerFactory.getLogger("A Bit Too Optimized");

    private Abto() {
    }
}
```

- [ ] **Step 2: Create `src/main/java/com/abto/AbtoClient.java`**

```java
package com.abto;

import net.fabricmc.api.ClientModInitializer;

public final class AbtoClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Abto.LOGGER.info("A Bit Too Optimized loaded. No features yet, skeleton only.");
    }
}
```

- [ ] **Step 3: Compile the active branch**

Run:
```bash
./gradlew build --stacktrace
```
Expected: BUILD SUCCESSFUL. Produces `versions/26.1.2/build/libs/a-bit-too-optimized-26.1.2-0.1.0.jar` (name reflects the active branch). If compilation fails on a Minecraft symbol, that is unexpected at this stage since no Minecraft APIs are used yet; re-check imports.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/abto/
git commit -m "feat: add shared constants and client entrypoint"
```

---

## Task 6: Unit-test harness smoke test

**Files:**
- Create: `src/test/java/com/abto/AbtoSmokeTest.java`

**Interfaces:**
- Consumes: JUnit 5 dependency from Task 3, `Abto.MOD_ID` from Task 5.
- Produces: proof that `./gradlew test` runs JUnit tests. Later milestones (Config Store, Recommendation Engine, Override Store) depend on this harness working.

- [ ] **Step 1: Write the failing test**

```java
package com.abto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AbtoSmokeTest {
    @Test
    void modIdIsAbto() {
        assertEquals("abto", Abto.MOD_ID);
    }
}
```

- [ ] **Step 2: Run the test to confirm the harness works**

Run:
```bash
./gradlew test --stacktrace
```
Expected: PASS. (This is a smoke test for the harness itself, so it passes immediately. If `./gradlew test` reports "no tests found" or a JUnit platform error, fix the `useJUnitPlatform()` and `testImplementation` lines from Task 3 before continuing.)

- [ ] **Step 3: Commit**

```bash
git add src/test/java/com/abto/AbtoSmokeTest.java
git commit -m "test: add unit-test harness smoke test"
```

---

## Task 7: Build all three branches and run in-game

**Files:**
- Create: `README.md`

**Interfaces:**
- Consumes: everything from Tasks 1 to 6.
- Produces: three remapped jars (one per branch) and a confirmed in-game load on the primary branch. No new code interfaces.

- [ ] **Step 1: Build every branch**

Run:
```bash
./gradlew chiseledBuild --stacktrace
```
Expected: `./build-all.sh` builds both modern branches (Stonecutter 0.9.6 does not expose the older `chiseledBuild` registration API, so the script switches the active version per build). Two jars appear under `versions/*/build/libs/`, named `a-bit-too-optimized-26.1.2-0.1.0.jar` and `a-bit-too-optimized-26.2-0.1.0.jar`. If a branch fails to resolve dependencies, update that branch's `versions/<v>/gradle.properties` using https://fabricmc.net/develop/ and re-run.

- [ ] **Step 2: Switch active branch and launch the dev client**

Run:
```bash
./gradlew "Set active project to 26.1.2"
./gradlew runClient --stacktrace
```
Expected: Minecraft 26.1.2 launches. In the log, find the line:
`A Bit Too Optimized loaded. No features yet, skeleton only.`
Reach the title screen with no crash. Close the game.

- [ ] **Step 3: Sanity-check the jar in a real instance (manual)**

Copy `a-bit-too-optimized-26.1.2-0.1.0.jar` into your 26.1.2 Modrinth instance's `mods` folder (alongside Fabric API), launch through the launcher, and confirm the same log line appears and the game reaches the title screen. This confirms the real distribution path works, not just the dev runtime.

- [ ] **Step 4: Create `README.md`**

```markdown
# A Bit Too Optimized

A client-side Minecraft performance tune-up mod with quality presets
(Ultra, High, Normal, Low, Very Low, Potato) plus a first-run setup wizard.
Works with or without other performance mods. Targets Minecraft 1.21.11,
26.1.x, and 26.2.x via Stonecutter.

## Build

- Requires Java 21.
- Build the active version: `./gradlew build`
- Build all versions: `./gradlew chiseledBuild`
- Switch active version: `./gradlew "Set active project to 26.1.2"`
- Run the dev client: `./gradlew runClient`

Jars are written to `build/libs/` (active version) or `versions/*/build/libs/`
(all versions).
```

- [ ] **Step 5: Commit**

```bash
git add README.md
git commit -m "docs: add build readme and confirm multi-version build"
```

---

## Milestone Done When

- `./gradlew build` succeeds on the 26.1.2 branch and produces a jar.
- `./gradlew chiseledBuild` produces jars for every branch whose toolchain is available (at minimum 1.21.11 and 26.1.2; 26.2 if its Fabric API is published).
- `./gradlew test` runs and passes the smoke test.
- The dev client and a real 26.1.2 instance both load the mod and log the startup line, reaching the title screen with no crash.

## Notes for Later Milestones

- Mojang mappings mean Minecraft symbol names follow the official mapping. Stonecutter guards are only needed where the actual API changed between 1.21.11, 26.1.x, and 26.2.x.
- The culling mixins in Milestone 4 must account for 26.2's optional Vulkan backend, which 26.1 does not have. Plan a backend check there.
- The unit-testable units (Config Store, Hardware Probe override rule, Recommendation Engine, Mod Presence Detector) belong in Milestone 2 and use the harness proven in Task 6.
