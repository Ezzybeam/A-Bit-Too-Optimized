# A Bit Too Optimized - Milestone 2: Core Logic Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the version-independent core of the mod: a JSON config store, the six-preset data model, hardware detection with manual override, a recommendation engine, a mod-presence detector, and a preset engine that applies vanilla video settings through a small interface, with the real Minecraft adapters wired in so selecting a preset actually changes settings in game.

**Architecture:** Every core unit is plain Java that takes its inputs as injected data (suppliers, predicates, or an `OptionsTarget` interface), never a static Minecraft or Fabric call. This keeps the whole core unit-testable with JUnit and free of per-version code. A thin "platform" layer at the end supplies the real Minecraft-backed and Fabric-backed implementations and wires everything into the client entrypoint.

**Tech Stack:** Java 25, Fabric Loom 1.15.5, Stonecutter 0.9.6, Gson (bundled with Fabric Loader) for config JSON, JUnit 5 for tests. Same toolchain as Milestone 1.

## Global Constraints

- Mod id `abto`; display name "A Bit Too Optimized"; client-side only.
- Java 25 build toolchain; Gradle wrapper 9.4.0; Loom 1.15.5; Stonecutter 0.9.6; active version 26.1.2; second version 26.2.
- No em dashes and no emojis, plain ASCII only, in all project-authored files (Java, JSON, docs). Vendor/generated files exempt.
- Every commit message ends with a blank line then: `Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>`.
- `.superpowers/` and build outputs must not be committed.
- CORE UNIT-TESTABILITY RULE: classes under `com.abto.config`, `com.abto.preset`, `com.abto.hardware`, `com.abto.recommend`, and `com.abto.compat` MUST NOT call Minecraft or Fabric APIs directly. They take injected data. Only classes under `com.abto.platform` and `AbtoClient` may touch Minecraft/Fabric. This is what keeps the JUnit harness working without a game runtime.
- Six presets plus Custom: ULTRA, HIGH, NORMAL, LOW, VERY_LOW, POTATO, CUSTOM. Potato targets 8 to 12 render distance for 30 to 60 FPS on weak machines.
- Run unit tests with `./gradlew test`. Build the active version with `./gradlew build`.

---

## File Structure

New packages and files (all under `src/main/java/com/abto/`, tests under `src/test/java/com/abto/`):

- `preset/GraphicsMode.java` - enum FAST, FANCY, FABULOUS. ABTO's own enum so the model does not depend on Minecraft.
- `preset/CloudMode.java` - enum OFF, FAST, FANCY.
- `preset/ParticleMode.java` - enum ALL, DECREASED, MINIMAL.
- `preset/PresetSettings.java` - record holding the ten video-setting axes. Pure data.
- `preset/Preset.java` - enum ULTRA, HIGH, NORMAL, LOW, VERY_LOW, POTATO, CUSTOM.
- `preset/Presets.java` - the data table mapping each non-custom Preset to its PresetSettings.
- `preset/OptionsTarget.java` - interface the preset engine writes settings through.
- `preset/PresetEngine.java` - applies a PresetSettings to an OptionsTarget.
- `config/HardwareOverrides.java` - record of nullable user overrides.
- `config/FeatureToggles.java` - record of per-feature on/off flags.
- `config/AbtoConfig.java` - the whole config POJO (preset choice, flags, overrides, custom settings).
- `config/ConfigStore.java` - load/save AbtoConfig as JSON with defaults.
- `hardware/GpuTier.java` - enum HIGH, MEDIUM, LOW, UNKNOWN.
- `hardware/HardwareInfo.java` - record of detected hardware values, each with a known/unknown flag.
- `hardware/HardwareProbe.java` - builds a HardwareInfo from injected suppliers.
- `hardware/EffectiveHardware.java` - applies overrides to a HardwareInfo ("detected unless corrected") and classifies GPU tier.
- `recommend/RecommendationEngine.java` - effective hardware to a recommended Preset.
- `compat/KnownMods.java` - the list of known mod ids ABTO cares about.
- `compat/ModPresenceDetector.java` - given an "is this id loaded" predicate, reports which known mods are present.
- `platform/MinecraftOptionsTarget.java` - real OptionsTarget backed by Minecraft GameOptions. (Only version-coupled file.)
- `platform/RuntimeHardware.java` - supplies real hardware values to HardwareProbe.
- `platform/FabricModPresence.java` - supplies the real isModLoaded predicate.
- `AbtoClient.java` (modify) - load config, detect hardware and mods, log the recommendation, and apply the selected preset's vanilla settings on startup.

Tests mirror these under `src/test/java/com/abto/` for every class except the three `platform/` classes and `AbtoClient` (those are verified by build plus the manual in-game check).

---

## Task 1: Preset data model (enums plus PresetSettings record)

**Files:**
- Create: `src/main/java/com/abto/preset/GraphicsMode.java`, `CloudMode.java`, `ParticleMode.java`, `PresetSettings.java`
- Test: `src/test/java/com/abto/preset/PresetSettingsTest.java`

**Interfaces:**
- Consumes: nothing.
- Produces: `PresetSettings` record with fields `(int renderDistance, int simulationDistance, GraphicsMode graphics, CloudMode clouds, ParticleMode particles, boolean entityShadows, boolean smoothLighting, int biomeBlendRadius, int mipmapLevels, double entityDistanceScaling)`. Enums `GraphicsMode{FAST,FANCY,FABULOUS}`, `CloudMode{OFF,FAST,FANCY}`, `ParticleMode{ALL,DECREASED,MINIMAL}`.

- [ ] **Step 1: Write the failing test**

```java
package com.abto.preset;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PresetSettingsTest {
    @Test
    void holdsAllTenAxes() {
        PresetSettings s = new PresetSettings(
            16, 10, GraphicsMode.FANCY, CloudMode.FAST, ParticleMode.DECREASED,
            true, true, 3, 4, 0.75);
        assertEquals(16, s.renderDistance());
        assertEquals(10, s.simulationDistance());
        assertEquals(GraphicsMode.FANCY, s.graphics());
        assertEquals(CloudMode.FAST, s.clouds());
        assertEquals(ParticleMode.DECREASED, s.particles());
        assertTrue(s.entityShadows());
        assertTrue(s.smoothLighting());
        assertEquals(3, s.biomeBlendRadius());
        assertEquals(4, s.mipmapLevels());
        assertEquals(0.75, s.entityDistanceScaling());
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew test --tests "com.abto.preset.PresetSettingsTest"`
Expected: FAIL (compilation error, classes do not exist).

- [ ] **Step 3: Create the enums and record**

`GraphicsMode.java`:
```java
package com.abto.preset;

public enum GraphicsMode {
    FAST, FANCY, FABULOUS
}
```

`CloudMode.java`:
```java
package com.abto.preset;

public enum CloudMode {
    OFF, FAST, FANCY
}
```

`ParticleMode.java`:
```java
package com.abto.preset;

public enum ParticleMode {
    ALL, DECREASED, MINIMAL
}
```

`PresetSettings.java`:
```java
package com.abto.preset;

/**
 * One coherent bundle of vanilla video settings. Pure data, no Minecraft types,
 * so it stays version independent and unit testable.
 */
public record PresetSettings(
    int renderDistance,
    int simulationDistance,
    GraphicsMode graphics,
    CloudMode clouds,
    ParticleMode particles,
    boolean entityShadows,
    boolean smoothLighting,
    int biomeBlendRadius,
    int mipmapLevels,
    double entityDistanceScaling
) {
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew test --tests "com.abto.preset.PresetSettingsTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/abto/preset/ src/test/java/com/abto/preset/PresetSettingsTest.java
git commit -m "feat: add preset settings data model"
```

---

## Task 2: Preset enum and the preset data table

**Files:**
- Create: `src/main/java/com/abto/preset/Preset.java`, `src/main/java/com/abto/preset/Presets.java`
- Test: `src/test/java/com/abto/preset/PresetsTest.java`

**Interfaces:**
- Consumes: `PresetSettings`, `GraphicsMode`, `CloudMode`, `ParticleMode` from Task 1.
- Produces: enum `Preset{ULTRA,HIGH,NORMAL,LOW,VERY_LOW,POTATO,CUSTOM}`; `Presets.settingsFor(Preset)` returning the `PresetSettings` for a non-custom preset and throwing `IllegalArgumentException` for `CUSTOM`; `Presets.ORDER` a `List<Preset>` of the six tunable presets from strongest (ULTRA) to lightest (POTATO).

- [ ] **Step 1: Write the failing test**

```java
package com.abto.preset;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PresetsTest {
    @Test
    void everyTunablePresetHasSettings() {
        for (Preset p : Presets.ORDER) {
            assertNotNull(Presets.settingsFor(p), "missing settings for " + p);
        }
        assertEquals(6, Presets.ORDER.size());
        assertFalse(Presets.ORDER.contains(Preset.CUSTOM));
    }

    @Test
    void customHasNoSettings() {
        assertThrows(IllegalArgumentException.class, () -> Presets.settingsFor(Preset.CUSTOM));
    }

    @Test
    void renderDistanceDecreasesFromUltraToPotato() {
        int prev = Integer.MAX_VALUE;
        for (Preset p : Presets.ORDER) {
            int rd = Presets.settingsFor(p).renderDistance();
            assertTrue(rd <= prev, "render distance should not increase from " + p);
            prev = rd;
        }
    }

    @Test
    void potatoTargetsEightToTwelveRenderDistance() {
        int rd = Presets.settingsFor(Preset.POTATO).renderDistance();
        assertTrue(rd >= 8 && rd <= 12, "potato render distance should be 8 to 12, was " + rd);
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew test --tests "com.abto.preset.PresetsTest"`
Expected: FAIL (classes do not exist).

- [ ] **Step 3: Create Preset and Presets**

`Preset.java`:
```java
package com.abto.preset;

public enum Preset {
    ULTRA, HIGH, NORMAL, LOW, VERY_LOW, POTATO, CUSTOM
}
```

`Presets.java`:
```java
package com.abto.preset;

import java.util.List;
import java.util.Map;

/**
 * The preset table. Each non-custom preset is a coherent strategy, not just a
 * slider position. Values live here so they are easy to read and tune.
 * Order: strongest visuals first (ULTRA) to lightest (POTATO).
 */
public final class Presets {

    public static final List<Preset> ORDER = List.of(
        Preset.ULTRA, Preset.HIGH, Preset.NORMAL, Preset.LOW, Preset.VERY_LOW, Preset.POTATO);

    private static final Map<Preset, PresetSettings> TABLE = Map.of(
        Preset.ULTRA, new PresetSettings(
            32, 12, GraphicsMode.FABULOUS, CloudMode.FANCY, ParticleMode.ALL,
            true, true, 7, 4, 1.0),
        Preset.HIGH, new PresetSettings(
            24, 12, GraphicsMode.FANCY, CloudMode.FAST, ParticleMode.ALL,
            true, true, 5, 4, 1.0),
        Preset.NORMAL, new PresetSettings(
            16, 10, GraphicsMode.FANCY, CloudMode.FAST, ParticleMode.DECREASED,
            true, true, 3, 4, 0.75),
        Preset.LOW, new PresetSettings(
            12, 8, GraphicsMode.FAST, CloudMode.OFF, ParticleMode.DECREASED,
            false, true, 1, 2, 0.5),
        Preset.VERY_LOW, new PresetSettings(
            10, 6, GraphicsMode.FAST, CloudMode.OFF, ParticleMode.MINIMAL,
            false, false, 0, 1, 0.5),
        Preset.POTATO, new PresetSettings(
            8, 5, GraphicsMode.FAST, CloudMode.OFF, ParticleMode.MINIMAL,
            false, false, 0, 0, 0.5)
    );

    private Presets() {
    }

    public static PresetSettings settingsFor(Preset preset) {
        PresetSettings settings = TABLE.get(preset);
        if (settings == null) {
            throw new IllegalArgumentException("No settings table entry for preset: " + preset);
        }
        return settings;
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew test --tests "com.abto.preset.PresetsTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/abto/preset/Preset.java src/main/java/com/abto/preset/Presets.java src/test/java/com/abto/preset/PresetsTest.java
git commit -m "feat: add preset enum and preset value table"
```

---

## Task 3: Hardware model and probe (injected suppliers)

**Files:**
- Create: `src/main/java/com/abto/hardware/HardwareInfo.java`, `src/main/java/com/abto/hardware/HardwareProbe.java`
- Test: `src/test/java/com/abto/hardware/HardwareProbeTest.java`

**Interfaces:**
- Consumes: nothing.
- Produces: record `HardwareInfo(long allocatedRamMb, int cpuCores, OptionalLong totalRamMb, Optional<String> gpuName)` where `totalRamMb` and `gpuName` are empty when unknown. `HardwareProbe.detect(LongSupplier maxMemoryBytes, IntSupplier cores, Supplier<OptionalLong> totalRamMb, Supplier<Optional<String>> gpuName)` returns a `HardwareInfo`, converting bytes to whole megabytes.

- [ ] **Step 1: Write the failing test**

```java
package com.abto.hardware;

import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.OptionalLong;
import static org.junit.jupiter.api.Assertions.*;

class HardwareProbeTest {
    @Test
    void convertsBytesToMegabytesAndPassesValuesThrough() {
        HardwareInfo info = HardwareProbe.detect(
            () -> 4096L * 1024 * 1024,   // 4096 MB allocated
            () -> 8,
            () -> OptionalLong.of(16384),
            () -> Optional.of("NVIDIA GeForce RTX 4070"));
        assertEquals(4096, info.allocatedRamMb());
        assertEquals(8, info.cpuCores());
        assertEquals(OptionalLong.of(16384), info.totalRamMb());
        assertEquals(Optional.of("NVIDIA GeForce RTX 4070"), info.gpuName());
    }

    @Test
    void unknownTotalRamAndGpuAreEmpty() {
        HardwareInfo info = HardwareProbe.detect(
            () -> 2048L * 1024 * 1024,
            () -> 4,
            OptionalLong::empty,
            Optional::empty);
        assertTrue(info.totalRamMb().isEmpty());
        assertTrue(info.gpuName().isEmpty());
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew test --tests "com.abto.hardware.HardwareProbeTest"`
Expected: FAIL (classes do not exist).

- [ ] **Step 3: Create HardwareInfo and HardwareProbe**

`HardwareInfo.java`:
```java
package com.abto.hardware;

import java.util.Optional;
import java.util.OptionalLong;

/**
 * Detected hardware values. Values that could not be read are represented as
 * empty Optionals rather than guesses, so the recommendation engine can be
 * conservative about what it does not know.
 */
public record HardwareInfo(
    long allocatedRamMb,
    int cpuCores,
    OptionalLong totalRamMb,
    Optional<String> gpuName
) {
}
```

`HardwareProbe.java`:
```java
package com.abto.hardware;

import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * Builds a HardwareInfo from injected suppliers. Takes suppliers rather than
 * calling Runtime or OpenGL directly so it can be unit tested without a JVM
 * runtime or render context. The real suppliers live in com.abto.platform.
 */
public final class HardwareProbe {

    private HardwareProbe() {
    }

    public static HardwareInfo detect(
            LongSupplier maxMemoryBytes,
            IntSupplier cpuCores,
            Supplier<OptionalLong> totalRamMb,
            Supplier<Optional<String>> gpuName) {
        long allocatedMb = maxMemoryBytes.getAsLong() / (1024 * 1024);
        return new HardwareInfo(allocatedMb, cpuCores.getAsInt(), totalRamMb.get(), gpuName.get());
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew test --tests "com.abto.hardware.HardwareProbeTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/abto/hardware/HardwareInfo.java src/main/java/com/abto/hardware/HardwareProbe.java src/test/java/com/abto/hardware/HardwareProbeTest.java
git commit -m "feat: add hardware info model and injectable probe"
```

---

## Task 4: Hardware overrides and effective values

**Files:**
- Create: `src/main/java/com/abto/config/HardwareOverrides.java`, `src/main/java/com/abto/hardware/GpuTier.java`, `src/main/java/com/abto/hardware/EffectiveHardware.java`
- Test: `src/test/java/com/abto/hardware/EffectiveHardwareTest.java`

**Interfaces:**
- Consumes: `HardwareInfo` (Task 3).
- Produces: record `HardwareOverrides(Long totalRamMb, Long allocatedRamMb, Integer cpuCores, String gpuName)` (null means no override). `EffectiveHardware.resolve(HardwareInfo detected, HardwareOverrides overrides)` returns an `EffectiveHardware` record `(long allocatedRamMb, int cpuCores, OptionalLong totalRamMb, Optional<String> gpuName, GpuTier gpuTier)` applying "override if set, otherwise detected" and classifying the GPU tier. Enum `GpuTier{HIGH,MEDIUM,LOW,UNKNOWN}`. `EffectiveHardware.classifyGpu(Optional<String> name)` is the static classifier.

- [ ] **Step 1: Write the failing test**

```java
package com.abto.hardware;

import com.abto.config.HardwareOverrides;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.OptionalLong;
import static org.junit.jupiter.api.Assertions.*;

class EffectiveHardwareTest {
    private static final HardwareOverrides NONE = new HardwareOverrides(null, null, null, null);

    @Test
    void detectedValuesUsedWhenNoOverride() {
        HardwareInfo detected = new HardwareInfo(4096, 8, OptionalLong.of(16384), Optional.of("Radeon RX 7800"));
        EffectiveHardware e = EffectiveHardware.resolve(detected, NONE);
        assertEquals(4096, e.allocatedRamMb());
        assertEquals(8, e.cpuCores());
        assertEquals(OptionalLong.of(16384), e.totalRamMb());
        assertEquals(Optional.of("Radeon RX 7800"), e.gpuName());
    }

    @Test
    void overrideWins() {
        HardwareInfo detected = new HardwareInfo(2048, 4, OptionalLong.empty(), Optional.empty());
        HardwareOverrides ov = new HardwareOverrides(32768L, 8192L, 12, "NVIDIA RTX 4090");
        EffectiveHardware e = EffectiveHardware.resolve(detected, ov);
        assertEquals(8192, e.allocatedRamMb());
        assertEquals(12, e.cpuCores());
        assertEquals(OptionalLong.of(32768), e.totalRamMb());
        assertEquals(Optional.of("NVIDIA RTX 4090"), e.gpuName());
    }

    @Test
    void gpuTierClassification() {
        assertEquals(GpuTier.HIGH, EffectiveHardware.classifyGpu(Optional.of("NVIDIA GeForce RTX 4070")));
        assertEquals(GpuTier.HIGH, EffectiveHardware.classifyGpu(Optional.of("AMD Radeon RX 7800 XT")));
        assertEquals(GpuTier.MEDIUM, EffectiveHardware.classifyGpu(Optional.of("NVIDIA GeForce GTX 1660")));
        assertEquals(GpuTier.LOW, EffectiveHardware.classifyGpu(Optional.of("Intel UHD Graphics 620")));
        assertEquals(GpuTier.UNKNOWN, EffectiveHardware.classifyGpu(Optional.empty()));
        assertEquals(GpuTier.UNKNOWN, EffectiveHardware.classifyGpu(Optional.of("Some Mystery GPU")));
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew test --tests "com.abto.hardware.EffectiveHardwareTest"`
Expected: FAIL (classes do not exist).

- [ ] **Step 3: Create the classes**

`GpuTier.java`:
```java
package com.abto.hardware;

public enum GpuTier {
    HIGH, MEDIUM, LOW, UNKNOWN
}
```

`HardwareOverrides.java`:
```java
package com.abto.config;

/**
 * User corrections for hardware values. A null field means "no override, use the
 * detected value". Stored in the config file.
 */
public record HardwareOverrides(
    Long totalRamMb,
    Long allocatedRamMb,
    Integer cpuCores,
    String gpuName
) {
    public static HardwareOverrides none() {
        return new HardwareOverrides(null, null, null, null);
    }
}
```

`EffectiveHardware.java`:
```java
package com.abto.hardware;

import com.abto.config.HardwareOverrides;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * The hardware values the recommendation engine actually uses: detected values
 * unless the user corrected them. Also classifies the GPU into a coarse tier
 * from its name string, which is the only signal we have for GPU power.
 */
public record EffectiveHardware(
    long allocatedRamMb,
    int cpuCores,
    OptionalLong totalRamMb,
    Optional<String> gpuName,
    GpuTier gpuTier
) {
    public static EffectiveHardware resolve(HardwareInfo detected, HardwareOverrides overrides) {
        long allocated = overrides.allocatedRamMb() != null
            ? overrides.allocatedRamMb() : detected.allocatedRamMb();
        int cores = overrides.cpuCores() != null
            ? overrides.cpuCores() : detected.cpuCores();
        OptionalLong total = overrides.totalRamMb() != null
            ? OptionalLong.of(overrides.totalRamMb()) : detected.totalRamMb();
        Optional<String> gpu = overrides.gpuName() != null
            ? Optional.of(overrides.gpuName()) : detected.gpuName();
        return new EffectiveHardware(allocated, cores, total, gpu, classifyGpu(gpu));
    }

    public static GpuTier classifyGpu(Optional<String> name) {
        if (name.isEmpty()) {
            return GpuTier.UNKNOWN;
        }
        String n = name.get().toLowerCase(Locale.ROOT);
        if (n.contains("rtx") || n.contains("rx 6") || n.contains("rx 7") || n.contains("rx 9")
                || n.contains("arc a7")) {
            return GpuTier.HIGH;
        }
        if (n.contains("gtx") || n.contains("rx 5") || n.contains("arc a3") || n.contains("iris xe")) {
            return GpuTier.MEDIUM;
        }
        if (n.contains("intel") || n.contains("uhd") || n.contains("hd graphics") || n.contains("vega")) {
            return GpuTier.LOW;
        }
        return GpuTier.UNKNOWN;
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew test --tests "com.abto.hardware.EffectiveHardwareTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/abto/config/HardwareOverrides.java src/main/java/com/abto/hardware/GpuTier.java src/main/java/com/abto/hardware/EffectiveHardware.java src/test/java/com/abto/hardware/EffectiveHardwareTest.java
git commit -m "feat: add hardware overrides and effective-value resolution"
```

---

## Task 5: Recommendation engine

**Files:**
- Create: `src/main/java/com/abto/recommend/RecommendationEngine.java`
- Test: `src/test/java/com/abto/recommend/RecommendationEngineTest.java`

**Interfaces:**
- Consumes: `EffectiveHardware`, `GpuTier` (Task 4), `Preset` (Task 2).
- Produces: `RecommendationEngine.recommend(EffectiveHardware hw)` returning a non-custom `Preset`. Uses allocated RAM and CPU cores to pick a base tier, then nudges up or down one step by GPU tier. Unknown values lean conservative (lower preset).

- [ ] **Step 1: Write the failing test**

```java
package com.abto.recommend;

import com.abto.hardware.EffectiveHardware;
import com.abto.hardware.GpuTier;
import com.abto.preset.Preset;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.OptionalLong;
import static org.junit.jupiter.api.Assertions.*;

class RecommendationEngineTest {
    private static EffectiveHardware hw(long allocMb, int cores, GpuTier tier) {
        return new EffectiveHardware(allocMb, cores, OptionalLong.of(allocMb * 2), Optional.of("x"), tier);
    }

    @Test
    void strongMachineGetsUltra() {
        assertEquals(Preset.ULTRA, RecommendationEngine.recommend(hw(10000, 12, GpuTier.HIGH)));
    }

    @Test
    void midMachineGetsNormal() {
        assertEquals(Preset.NORMAL, RecommendationEngine.recommend(hw(4096, 4, GpuTier.MEDIUM)));
    }

    @Test
    void weakLaptopGetsPotato() {
        assertEquals(Preset.POTATO, RecommendationEngine.recommend(hw(1800, 2, GpuTier.LOW)));
    }

    @Test
    void lowGpuTierNudgesDownOneStep() {
        Preset withMedium = RecommendationEngine.recommend(hw(6000, 6, GpuTier.MEDIUM));
        Preset withLow = RecommendationEngine.recommend(hw(6000, 6, GpuTier.LOW));
        assertTrue(withLow.ordinal() > withMedium.ordinal(),
            "LOW gpu tier should recommend a lighter preset than MEDIUM at the same RAM and cores");
    }

    @Test
    void neverReturnsCustom() {
        assertNotEquals(Preset.CUSTOM, RecommendationEngine.recommend(hw(512, 1, GpuTier.UNKNOWN)));
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew test --tests "com.abto.recommend.RecommendationEngineTest"`
Expected: FAIL (class does not exist).

- [ ] **Step 3: Create RecommendationEngine**

```java
package com.abto.recommend;

import com.abto.hardware.EffectiveHardware;
import com.abto.hardware.GpuTier;
import com.abto.preset.Preset;
import com.abto.preset.Presets;

/**
 * Maps effective hardware to a recommended preset. Thresholds are documented
 * constants so they are easy to tune. RAM and cores pick a base tier; GPU tier
 * nudges it one step. The base tier uses the weaker of the RAM signal and the
 * core signal, so a machine with lots of RAM but few cores is not over-rated.
 */
public final class RecommendationEngine {

    // Allocated-RAM thresholds in megabytes, from strongest to weakest.
    private static final long ULTRA_RAM_MB = 8000;
    private static final long HIGH_RAM_MB = 6000;
    private static final long NORMAL_RAM_MB = 4000;
    private static final long LOW_RAM_MB = 3000;
    private static final long VERY_LOW_RAM_MB = 2000;

    // CPU-core thresholds, from strongest to weakest.
    private static final int ULTRA_CORES = 8;
    private static final int HIGH_CORES = 6;
    private static final int NORMAL_CORES = 4;
    private static final int LOW_CORES = 4;
    private static final int VERY_LOW_CORES = 2;

    private RecommendationEngine() {
    }

    public static Preset recommend(EffectiveHardware hw) {
        int ramIndex = ramTierIndex(hw.allocatedRamMb());
        int coreIndex = coreTierIndex(hw.cpuCores());
        // Higher index means lighter preset; take the weaker (higher) of the two.
        int base = Math.max(ramIndex, coreIndex);
        base += gpuNudge(hw.gpuTier());
        base = Math.max(0, Math.min(Presets.ORDER.size() - 1, base));
        return Presets.ORDER.get(base);
    }

    private static int ramTierIndex(long ramMb) {
        if (ramMb >= ULTRA_RAM_MB) return 0;   // ULTRA
        if (ramMb >= HIGH_RAM_MB) return 1;     // HIGH
        if (ramMb >= NORMAL_RAM_MB) return 2;   // NORMAL
        if (ramMb >= LOW_RAM_MB) return 3;      // LOW
        if (ramMb >= VERY_LOW_RAM_MB) return 4; // VERY_LOW
        return 5;                                // POTATO
    }

    private static int coreTierIndex(int cores) {
        if (cores >= ULTRA_CORES) return 0;
        if (cores >= HIGH_CORES) return 1;
        if (cores >= NORMAL_CORES) return 2;
        if (cores >= LOW_CORES) return 3;
        if (cores >= VERY_LOW_CORES) return 4;
        return 5;
    }

    private static int gpuNudge(GpuTier tier) {
        return switch (tier) {
            case HIGH -> -1;   // one step stronger
            case LOW -> 1;     // one step lighter
            case MEDIUM, UNKNOWN -> 0;
        };
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew test --tests "com.abto.recommend.RecommendationEngineTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/abto/recommend/RecommendationEngine.java src/test/java/com/abto/recommend/RecommendationEngineTest.java
git commit -m "feat: add hardware-to-preset recommendation engine"
```

---

## Task 6: Mod presence detector

**Files:**
- Create: `src/main/java/com/abto/compat/KnownMods.java`, `src/main/java/com/abto/compat/ModPresenceDetector.java`
- Test: `src/test/java/com/abto/compat/ModPresenceDetectorTest.java`

**Interfaces:**
- Consumes: nothing.
- Produces: `KnownMods.IDS` a `List<String>` of mod ids ABTO cares about. `ModPresenceDetector.detect(Predicate<String> isLoaded)` returns a `Set<String>` of the known ids that are present. Convenience: `ModPresenceDetector.isPresent(Set<String> present, String id)` returning boolean. The predicate is injected so this is testable without Fabric.

- [ ] **Step 1: Write the failing test**

```java
package com.abto.compat;

import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class ModPresenceDetectorTest {
    @Test
    void reportsOnlyKnownLoadedMods() {
        Set<String> loaded = Set.of("sodium", "iris", "some-unknown-mod");
        Set<String> present = ModPresenceDetector.detect(loaded::contains);
        assertTrue(present.contains("sodium"));
        assertTrue(present.contains("iris"));
        assertFalse(present.contains("some-unknown-mod"), "unknown mods are not reported");
        assertFalse(present.contains("lithium"), "lithium was not loaded");
    }

    @Test
    void emptyWhenNothingLoaded() {
        assertTrue(ModPresenceDetector.detect(id -> false).isEmpty());
    }

    @Test
    void knownIdsIncludeTheExpectedMods() {
        assertTrue(KnownMods.IDS.containsAll(Set.of(
            "sodium", "lithium", "iris", "bobby", "voxy")));
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew test --tests "com.abto.compat.ModPresenceDetectorTest"`
Expected: FAIL (classes do not exist).

- [ ] **Step 3: Create the classes**

`KnownMods.java`:
```java
package com.abto.compat;

import java.util.List;

/**
 * Mod ids ABTO knows how to cooperate with. Presence is a soft signal only;
 * none of these are required. Ids are Fabric mod ids as published on Modrinth.
 */
public final class KnownMods {

    public static final List<String> IDS = List.of(
        "sodium",
        "lithium",
        "iris",
        "ferritecore",
        "immediatelyfast",
        "modernfix",
        "entityculling",
        "dynamic_fps",
        "bobby",
        "voxy",
        "xaerominimap",
        "xaeroworldmap",
        "journeymap"
    );

    private KnownMods() {
    }
}
```

`ModPresenceDetector.java`:
```java
package com.abto.compat;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Reports which of the known mods are loaded. Takes an "is this id loaded"
 * predicate so it can be unit tested without a Fabric runtime; the real
 * predicate (FabricLoader::isModLoaded) is supplied in com.abto.platform.
 */
public final class ModPresenceDetector {

    private ModPresenceDetector() {
    }

    public static Set<String> detect(Predicate<String> isLoaded) {
        Set<String> present = new LinkedHashSet<>();
        for (String id : KnownMods.IDS) {
            if (isLoaded.test(id)) {
                present.add(id);
            }
        }
        return present;
    }

    public static boolean isPresent(Set<String> present, String id) {
        return present.contains(id);
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew test --tests "com.abto.compat.ModPresenceDetectorTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/abto/compat/ src/test/java/com/abto/compat/ModPresenceDetectorTest.java
git commit -m "feat: add injectable mod presence detector"
```

---

## Task 7: Config store (JSON load and save with defaults)

**Files:**
- Create: `src/main/java/com/abto/config/FeatureToggles.java`, `src/main/java/com/abto/config/AbtoConfig.java`, `src/main/java/com/abto/config/ConfigStore.java`
- Test: `src/test/java/com/abto/config/ConfigStoreTest.java`

**Interfaces:**
- Consumes: `Preset` (Task 2), `PresetSettings` (Task 1), `HardwareOverrides` (Task 4).
- Produces: `FeatureToggles(boolean dynamicFps, boolean entityCulling, boolean particleCulling)` with `FeatureToggles.defaults()`. `AbtoConfig` mutable POJO with fields: `int configVersion`, `Preset selectedPreset`, `boolean wizardCompleted`, `boolean usesShaders`, `boolean applyToOtherMods`, `PresetSettings customSettings` (nullable), `HardwareOverrides hardwareOverrides`, `FeatureToggles featureToggles`; plus `AbtoConfig.defaults()`. `ConfigStore.load(Path file)` returns an `AbtoConfig` (defaults if the file is missing or corrupt, backing up a corrupt file to `<file>.bak`). `ConfigStore.save(Path file, AbtoConfig config)` writes pretty JSON.

- [ ] **Step 1: Write the failing test**

```java
package com.abto.config;

import com.abto.preset.Preset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class ConfigStoreTest {
    @Test
    void missingFileGivesDefaults(@TempDir Path dir) {
        AbtoConfig config = ConfigStore.load(dir.resolve("abto.json"));
        assertEquals(Preset.NORMAL, config.selectedPreset);
        assertFalse(config.wizardCompleted);
        assertTrue(config.applyToOtherMods);
        assertNotNull(config.hardwareOverrides);
        assertNotNull(config.featureToggles);
    }

    @Test
    void saveThenLoadRoundTrips(@TempDir Path dir) {
        Path file = dir.resolve("abto.json");
        AbtoConfig config = AbtoConfig.defaults();
        config.selectedPreset = Preset.POTATO;
        config.wizardCompleted = true;
        config.usesShaders = true;
        config.hardwareOverrides = new HardwareOverrides(null, 4096L, 4, "Intel UHD");
        ConfigStore.save(file, config);

        AbtoConfig loaded = ConfigStore.load(file);
        assertEquals(Preset.POTATO, loaded.selectedPreset);
        assertTrue(loaded.wizardCompleted);
        assertTrue(loaded.usesShaders);
        assertEquals(4096L, loaded.hardwareOverrides.allocatedRamMb());
        assertEquals("Intel UHD", loaded.hardwareOverrides.gpuName());
    }

    @Test
    void corruptFileFallsBackToDefaultsAndBacksUp(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("abto.json");
        Files.writeString(file, "{ this is not valid json ");
        AbtoConfig config = ConfigStore.load(file);
        assertEquals(Preset.NORMAL, config.selectedPreset);
        assertTrue(Files.exists(dir.resolve("abto.json.bak")), "corrupt file should be backed up");
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew test --tests "com.abto.config.ConfigStoreTest"`
Expected: FAIL (classes do not exist).

- [ ] **Step 3: Create the classes**

`FeatureToggles.java`:
```java
package com.abto.config;

public record FeatureToggles(
    boolean dynamicFps,
    boolean entityCulling,
    boolean particleCulling
) {
    public static FeatureToggles defaults() {
        return new FeatureToggles(true, true, true);
    }
}
```

`AbtoConfig.java`:
```java
package com.abto.config;

import com.abto.preset.Preset;
import com.abto.preset.PresetSettings;

/**
 * The whole on-disk config. A mutable POJO so Gson can populate it and callers
 * can update single fields. configVersion lets future versions migrate.
 */
public final class AbtoConfig {

    public static final int CURRENT_VERSION = 1;

    public int configVersion = CURRENT_VERSION;
    public Preset selectedPreset = Preset.NORMAL;
    public boolean wizardCompleted = false;
    public boolean usesShaders = false;
    public boolean applyToOtherMods = true;
    public PresetSettings customSettings = null;
    public HardwareOverrides hardwareOverrides = HardwareOverrides.none();
    public FeatureToggles featureToggles = FeatureToggles.defaults();

    public static AbtoConfig defaults() {
        return new AbtoConfig();
    }

    /** Replaces any null fields (e.g. from a partial JSON file) with defaults. */
    public void fillMissingWithDefaults() {
        if (selectedPreset == null) selectedPreset = Preset.NORMAL;
        if (hardwareOverrides == null) hardwareOverrides = HardwareOverrides.none();
        if (featureToggles == null) featureToggles = FeatureToggles.defaults();
        if (configVersion <= 0) configVersion = CURRENT_VERSION;
    }
}
```

`ConfigStore.java`:
```java
package com.abto.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Loads and saves AbtoConfig as JSON. Never throws on a missing or corrupt file:
 * it falls back to defaults and backs up a corrupt file so the user does not lose
 * a working game over a bad edit.
 */
public final class ConfigStore {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private ConfigStore() {
    }

    public static AbtoConfig load(Path file) {
        if (!Files.exists(file)) {
            return AbtoConfig.defaults();
        }
        try {
            String json = Files.readString(file);
            AbtoConfig config = GSON.fromJson(json, AbtoConfig.class);
            if (config == null) {
                return AbtoConfig.defaults();
            }
            config.fillMissingWithDefaults();
            return config;
        } catch (JsonSyntaxException | IOException e) {
            backupCorruptFile(file);
            return AbtoConfig.defaults();
        }
    }

    public static void save(Path file, AbtoConfig config) {
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            Files.writeString(file, GSON.toJson(config));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to save ABTO config", e);
        }
    }

    private static void backupCorruptFile(Path file) {
        try {
            Path backup = file.resolveSibling(file.getFileName().toString() + ".bak");
            Files.copy(file, backup, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
            // Best effort: if we cannot back up, we still continue with defaults.
        }
    }
}
```

Note on Gson: it is bundled with Fabric Loader, so `com.google.gson` is on the classpath. If `./gradlew test` cannot resolve it at test compile time, add `testImplementation("com.google.code.gson:gson:2.11.0")` to build.gradle.kts and record that in the report.

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew test --tests "com.abto.config.ConfigStoreTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/abto/config/ src/test/java/com/abto/config/ConfigStoreTest.java
git commit -m "feat: add json config store with defaults and corruption recovery"
```

---

## Task 8: Preset engine against an OptionsTarget interface

**Files:**
- Create: `src/main/java/com/abto/preset/OptionsTarget.java`, `src/main/java/com/abto/preset/PresetEngine.java`
- Test: `src/test/java/com/abto/preset/PresetEngineTest.java`

**Interfaces:**
- Consumes: `PresetSettings`, `GraphicsMode`, `CloudMode`, `ParticleMode`, `Preset`, `Presets`.
- Produces: interface `OptionsTarget` with setters for each axis (`setRenderDistance(int)`, `setSimulationDistance(int)`, `setGraphics(GraphicsMode)`, `setClouds(CloudMode)`, `setParticles(ParticleMode)`, `setEntityShadows(boolean)`, `setSmoothLighting(boolean)`, `setBiomeBlendRadius(int)`, `setMipmapLevels(int)`, `setEntityDistanceScaling(double)`, and `save()`). `PresetEngine.apply(Preset preset, boolean usesShaders, OptionsTarget target)`: for CUSTOM it does nothing and returns false; otherwise it writes every axis of `Presets.settingsFor(preset)` to the target, applies the shader-aware adjustment (when `usesShaders` is true it does not force a graphics mode lighter than FANCY, because some shader packs need fancy-level features), calls `target.save()`, and returns true.

- [ ] **Step 1: Write the failing test**

```java
package com.abto.preset;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PresetEngineTest {

    /** A fake OptionsTarget that records what was written. */
    static final class FakeTarget implements OptionsTarget {
        Integer renderDistance, simulationDistance, biomeBlend, mipmaps;
        GraphicsMode graphics;
        CloudMode clouds;
        ParticleMode particles;
        Boolean entityShadows, smoothLighting;
        Double entityDistanceScaling;
        int saves = 0;

        public void setRenderDistance(int v) { renderDistance = v; }
        public void setSimulationDistance(int v) { simulationDistance = v; }
        public void setGraphics(GraphicsMode v) { graphics = v; }
        public void setClouds(CloudMode v) { clouds = v; }
        public void setParticles(ParticleMode v) { particles = v; }
        public void setEntityShadows(boolean v) { entityShadows = v; }
        public void setSmoothLighting(boolean v) { smoothLighting = v; }
        public void setBiomeBlendRadius(int v) { biomeBlend = v; }
        public void setMipmapLevels(int v) { mipmaps = v; }
        public void setEntityDistanceScaling(double v) { entityDistanceScaling = v; }
        public void save() { saves++; }
    }

    @Test
    void applyingPresetWritesEveryAxisAndSaves() {
        FakeTarget t = new FakeTarget();
        boolean applied = PresetEngine.apply(Preset.LOW, false, t);
        assertTrue(applied);
        PresetSettings expected = Presets.settingsFor(Preset.LOW);
        assertEquals(expected.renderDistance(), t.renderDistance);
        assertEquals(expected.simulationDistance(), t.simulationDistance);
        assertEquals(expected.graphics(), t.graphics);
        assertEquals(expected.clouds(), t.clouds);
        assertEquals(expected.particles(), t.particles);
        assertEquals(expected.entityShadows(), t.entityShadows);
        assertEquals(expected.smoothLighting(), t.smoothLighting);
        assertEquals(expected.biomeBlendRadius(), t.biomeBlend);
        assertEquals(expected.mipmapLevels(), t.mipmaps);
        assertEquals(expected.entityDistanceScaling(), t.entityDistanceScaling);
        assertEquals(1, t.saves);
    }

    @Test
    void customAppliesNothing() {
        FakeTarget t = new FakeTarget();
        boolean applied = PresetEngine.apply(Preset.CUSTOM, false, t);
        assertFalse(applied);
        assertNull(t.graphics);
        assertEquals(0, t.saves);
    }

    @Test
    void shaderAwareKeepsGraphicsAtLeastFancy() {
        FakeTarget t = new FakeTarget();
        // POTATO normally uses FAST graphics; with shaders on we keep at least FANCY.
        PresetEngine.apply(Preset.POTATO, true, t);
        assertEquals(GraphicsMode.FANCY, t.graphics);
    }

    @Test
    void withoutShadersPotatoStaysFast() {
        FakeTarget t = new FakeTarget();
        PresetEngine.apply(Preset.POTATO, false, t);
        assertEquals(GraphicsMode.FAST, t.graphics);
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew test --tests "com.abto.preset.PresetEngineTest"`
Expected: FAIL (classes do not exist).

- [ ] **Step 3: Create the interface and engine**

`OptionsTarget.java`:
```java
package com.abto.preset;

/**
 * The surface the preset engine writes settings through. A Minecraft-backed
 * implementation lives in com.abto.platform; tests use a fake. This keeps the
 * engine free of Minecraft types and unit testable.
 */
public interface OptionsTarget {
    void setRenderDistance(int chunks);
    void setSimulationDistance(int chunks);
    void setGraphics(GraphicsMode mode);
    void setClouds(CloudMode mode);
    void setParticles(ParticleMode mode);
    void setEntityShadows(boolean enabled);
    void setSmoothLighting(boolean enabled);
    void setBiomeBlendRadius(int radius);
    void setMipmapLevels(int levels);
    void setEntityDistanceScaling(double scale);
    void save();
}
```

`PresetEngine.java`:
```java
package com.abto.preset;

/**
 * Applies a preset's vanilla video settings to an OptionsTarget. The only place
 * preset values become real settings. Shader awareness lives here: with shaders
 * on, we do not push the graphics mode below FANCY because some shader packs
 * rely on fancy-level rendering.
 */
public final class PresetEngine {

    private PresetEngine() {
    }

    /**
     * @return true if a preset bundle was applied, false for CUSTOM (which leaves
     *         the user in control of individual settings).
     */
    public static boolean apply(Preset preset, boolean usesShaders, OptionsTarget target) {
        if (preset == Preset.CUSTOM) {
            return false;
        }
        PresetSettings s = Presets.settingsFor(preset);
        target.setRenderDistance(s.renderDistance());
        target.setSimulationDistance(s.simulationDistance());
        target.setGraphics(shaderAwareGraphics(s.graphics(), usesShaders));
        target.setClouds(s.clouds());
        target.setParticles(s.particles());
        target.setEntityShadows(s.entityShadows());
        target.setSmoothLighting(s.smoothLighting());
        target.setBiomeBlendRadius(s.biomeBlendRadius());
        target.setMipmapLevels(s.mipmapLevels());
        target.setEntityDistanceScaling(s.entityDistanceScaling());
        target.save();
        return true;
    }

    private static GraphicsMode shaderAwareGraphics(GraphicsMode mode, boolean usesShaders) {
        if (usesShaders && mode == GraphicsMode.FAST) {
            return GraphicsMode.FANCY;
        }
        return mode;
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew test --tests "com.abto.preset.PresetEngineTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/abto/preset/OptionsTarget.java src/main/java/com/abto/preset/PresetEngine.java src/test/java/com/abto/preset/PresetEngineTest.java
git commit -m "feat: add preset engine writing through an options interface"
```

---

## Task 9: Platform adapters and client wiring

**Files:**
- Create: `src/main/java/com/abto/platform/RuntimeHardware.java`, `src/main/java/com/abto/platform/FabricModPresence.java`, `src/main/java/com/abto/platform/MinecraftOptionsTarget.java`
- Modify: `src/main/java/com/abto/AbtoClient.java`
- No new unit tests (these touch Minecraft/Fabric and are verified by build plus the manual in-game check). The logic they call is already covered by Tasks 1 to 8.

**Interfaces:**
- Consumes: everything from Tasks 1 to 8.
- Produces: `RuntimeHardware` static suppliers (`maxMemoryBytes()`, `cpuCores()`, `totalRamMb()`, `gpuName()`); `FabricModPresence.isLoaded()` returning a `Predicate<String>`; `MinecraftOptionsTarget` implementing `OptionsTarget` over the real `GameOptions`. `AbtoClient.onInitializeClient` loads config, builds effective hardware, logs the recommendation, and applies the selected preset.

This task is integration glue; its correctness is in compiling against the real Minecraft API and loading in game. Follow these steps.

- [ ] **Step 1: Create RuntimeHardware**

```java
package com.abto.platform;

import java.lang.management.ManagementFactory;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * Real hardware values for the HardwareProbe. Allocated RAM and core count are
 * reliable; total system RAM is read via the JVM OperatingSystemMXBean and may
 * be unavailable on some JVMs (returned empty). GPU name is filled in separately
 * from the OpenGL renderer string once the render context exists.
 */
public final class RuntimeHardware {

    private RuntimeHardware() {
    }

    public static long maxMemoryBytes() {
        return Runtime.getRuntime().maxMemory();
    }

    public static int cpuCores() {
        return Runtime.getRuntime().availableProcessors();
    }

    public static OptionalLong totalRamMb() {
        try {
            java.lang.management.OperatingSystemMXBean base = ManagementFactory.getOperatingSystemMXBean();
            if (base instanceof com.sun.management.OperatingSystemMXBean os) {
                long bytes = os.getTotalMemorySize();
                if (bytes > 0) {
                    return OptionalLong.of(bytes / (1024 * 1024));
                }
            }
        } catch (Throwable ignored) {
            // Some JVMs do not expose com.sun.management; treat as unknown.
        }
        return OptionalLong.empty();
    }

    public static Optional<String> gpuName() {
        // Filled in elsewhere once the GL context exists; unknown at init time.
        return Optional.empty();
    }
}
```

Note: `com.sun.management.OperatingSystemMXBean.getTotalMemorySize()` is the Java 14+ name (older `getTotalPhysicalMemorySize()` is deprecated). On Java 25 use `getTotalMemorySize()`.

- [ ] **Step 2: Create FabricModPresence**

```java
package com.abto.platform;

import net.fabricmc.loader.api.FabricLoader;
import java.util.function.Predicate;

/** Supplies the real "is this mod loaded" predicate for the detector. */
public final class FabricModPresence {

    private FabricModPresence() {
    }

    public static Predicate<String> isLoaded() {
        return id -> FabricLoader.getInstance().isModLoaded(id);
    }
}
```

- [ ] **Step 3: Create MinecraftOptionsTarget (version-coupled)**

The exact `GameOptions` method and field names depend on the Minecraft version. The mod uses Mojang official mappings. Look up the correct accessors against the active version (26.1.2). The structure below is the contract; fill the body of each setter against the real `GameOptions` API, and use `MinecraftClient.getInstance().options`. Many options are `SimpleOption<T>` accessed via `options.getViewDistance().setValue(...)` style. If an accessor name differs between 26.1.2 and 26.2, guard that single line with a Stonecutter comment.

```java
package com.abto.platform;

import com.abto.preset.CloudMode;
import com.abto.preset.GraphicsMode;
import com.abto.preset.OptionsTarget;
import com.abto.preset.ParticleMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;

/**
 * OptionsTarget backed by the real Minecraft GameOptions. This is the only
 * version-coupled class in the core feature set; everything it is handed comes
 * from version-independent code.
 */
public final class MinecraftOptionsTarget implements OptionsTarget {

    private final GameOptions options;

    public MinecraftOptionsTarget(MinecraftClient client) {
        this.options = client.options;
    }

    @Override
    public void setRenderDistance(int chunks) {
        options.getViewDistance().setValue(chunks);
    }

    @Override
    public void setSimulationDistance(int chunks) {
        options.getSimulationDistance().setValue(chunks);
    }

    @Override
    public void setGraphics(GraphicsMode mode) {
        options.getGraphicsMode().setValue(switch (mode) {
            case FAST -> net.minecraft.client.option.GraphicsMode.FAST;
            case FANCY -> net.minecraft.client.option.GraphicsMode.FANCY;
            case FABULOUS -> net.minecraft.client.option.GraphicsMode.FABULOUS;
        });
    }

    @Override
    public void setClouds(CloudMode mode) {
        options.getCloudRenderMode().setValue(switch (mode) {
            case OFF -> net.minecraft.client.option.CloudRenderMode.OFF;
            case FAST -> net.minecraft.client.option.CloudRenderMode.FAST;
            case FANCY -> net.minecraft.client.option.CloudRenderMode.FANCY;
        });
    }

    @Override
    public void setParticles(ParticleMode mode) {
        options.getParticles().setValue(switch (mode) {
            case ALL -> net.minecraft.client.option.ParticlesMode.ALL;
            case DECREASED -> net.minecraft.client.option.ParticlesMode.DECREASED;
            case MINIMAL -> net.minecraft.client.option.ParticlesMode.MINIMAL;
        });
    }

    @Override
    public void setEntityShadows(boolean enabled) {
        options.getEntityShadows().setValue(enabled);
    }

    @Override
    public void setSmoothLighting(boolean enabled) {
        options.getAo().setValue(enabled);
    }

    @Override
    public void setBiomeBlendRadius(int radius) {
        options.getBiomeBlendRadius().setValue(radius);
    }

    @Override
    public void setMipmapLevels(int levels) {
        options.getMipmapLevels().setValue(levels);
    }

    @Override
    public void setEntityDistanceScaling(double scale) {
        options.getEntityDistanceScaling().setValue(scale);
    }

    @Override
    public void save() {
        options.write();
    }
}
```

Verification for this step: it must COMPILE against 26.1.2. If a getter name is wrong, the compiler error names the symbol; find the correct accessor in the mapped `GameOptions` (use the IDE's autocomplete or `javap` on the mapped class) and fix it. Do not invent names.

- [ ] **Step 4: Wire AbtoClient**

Replace the body of `src/main/java/com/abto/AbtoClient.java` with:

```java
package com.abto;

import com.abto.compat.ModPresenceDetector;
import com.abto.config.AbtoConfig;
import com.abto.config.ConfigStore;
import com.abto.hardware.EffectiveHardware;
import com.abto.hardware.HardwareInfo;
import com.abto.hardware.HardwareProbe;
import com.abto.platform.FabricModPresence;
import com.abto.platform.MinecraftOptionsTarget;
import com.abto.platform.RuntimeHardware;
import com.abto.preset.Preset;
import com.abto.preset.PresetEngine;
import com.abto.recommend.RecommendationEngine;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import java.nio.file.Path;
import java.util.Set;

public final class AbtoClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Path configFile = FabricLoader.getInstance().getConfigDir().resolve("abto.json");
        AbtoConfig config = ConfigStore.load(configFile);

        HardwareInfo detected = HardwareProbe.detect(
            RuntimeHardware::maxMemoryBytes,
            RuntimeHardware::cpuCores,
            RuntimeHardware::totalRamMb,
            RuntimeHardware::gpuName);
        EffectiveHardware effective = EffectiveHardware.resolve(detected, config.hardwareOverrides);
        Preset recommended = RecommendationEngine.recommend(effective);

        Set<String> presentMods = ModPresenceDetector.detect(FabricModPresence.isLoaded());

        Abto.LOGGER.info("A Bit Too Optimized loaded. Selected preset: {}. Recommended for this machine: {}. Known perf mods present: {}.",
            config.selectedPreset, recommended, presentMods);

        if (config.selectedPreset != Preset.CUSTOM) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.options != null) {
                boolean applied = PresetEngine.apply(
                    config.selectedPreset, config.usesShaders, new MinecraftOptionsTarget(client));
                Abto.LOGGER.info("Applied preset {} to vanilla options: {}.", config.selectedPreset, applied);
            }
        }
    }
}
```

- [ ] **Step 5: Build the active version**

Run: `./gradlew build --stacktrace`
Expected: BUILD SUCCESSFUL on 26.1.2. If `MinecraftOptionsTarget` fails to compile, fix the option accessor names against the real mapped `GameOptions` (Step 3 note) and rebuild. Do not proceed until it compiles.

- [ ] **Step 6: Build the other version**

Run: `./build-all.sh`
Expected: both 26.1.2 and 26.2 build. If a `GameOptions` accessor differs on 26.2, guard that one line with a Stonecutter `//?` directive and rebuild.

- [ ] **Step 7: Run the full test suite**

Run: `./gradlew test`
Expected: all tests from Tasks 1 to 8 pass.

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/abto/platform/ src/main/java/com/abto/AbtoClient.java
git commit -m "feat: wire platform adapters and apply selected preset on startup"
```

---

## Milestone Done When

- `./gradlew test` passes all unit tests for Tasks 1 to 8.
- `./gradlew build` succeeds on 26.1.2 and `./build-all.sh` builds both 26.1.2 and 26.2.
- On startup the mod logs the selected preset, the recommended preset for the machine, and which known performance mods are present.
- With a non-custom preset selected in `config/abto.json`, the game's video settings actually change to match that preset (manual check: set `"selectedPreset": "POTATO"`, launch, confirm render distance and graphics drop).

## Notes for Later Milestones

- Milestone 3 (GUI) builds the first-run wizard and config screen on top of this core: the wizard reads the recommendation and writes the chosen preset and overrides back through ConfigStore, then calls PresetEngine.
- Milestone 5 (mod-config adapters) adds the second layer to the preset engine: when `applyToOtherMods` is true and a known mod is present, write that mod's config too. The adapters plug in alongside OptionsTarget.
- GPU name detection from the OpenGL `GL_RENDERER` string is deferred until the render context exists; RuntimeHardware.gpuName currently returns empty, so the recommendation leans on RAM and cores until then. Wiring real GPU detection is a small follow-up once a render-thread hook is added.
