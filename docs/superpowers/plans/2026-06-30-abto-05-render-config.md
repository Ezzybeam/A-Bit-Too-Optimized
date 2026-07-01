# A Bit Too Optimized - Milestone 5: Rendering Config (Batch 1) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Give ABTO its first ORIGINAL performance features: a batch of render toggles (hide clouds/stars/sun-moon/sky, disable fog, disable block texture animations, dynamic FPS) implemented via mixins, surfaced as rows in the options screen and as preset axes, on an extensible framework so more toggles are cheap to add.

**Architecture:** A mutable FeatureToggles holds every toggle (POJO for easy expansion + defaults). A static RenderToggles snapshot mirrors the active toggle state so render mixins can read it cheaply per frame. Each render mixin is a cancellable inject gated on a RenderToggles flag; it defers to a better mod when present and never touches COMPAT-mod rendering. Toggles appear as a Performance section in AbtoOptionsScreen and become PresetSettings axes so presets flip the aggressive ones.

**Tech Stack:** Java 25, Loom 1.15.5, Stonecutter 0.9.6, SpongePowered Mixin (+ MixinExtras) via Loom, Fabric API, JUnit 5. Builds on Milestones 1-4 (mixin infra, options screen, presets, config).

## Global Constraints

- Mod id `abto`; client-side only; Java 25; Gradle 9.4.0 wrapper; Loom 1.15.5; Stonecutter 0.9.6; active version 26.1.2; second 26.2; non-remapping toolchain uses `implementation()`.
- No em dashes, no emojis, plain ASCII only, in authored files and GUI text. Vendor/generated files exempt.
- Every commit message ends with a blank line then: `Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>`.
- `.superpowers/` and build outputs must not be committed.
- CORE PURITY: `com.abto.config`, `com.abto.preset`, `com.abto.hardware`, `com.abto.recommend`, `com.abto.compat`, and the RenderToggles state class MUST NOT import Minecraft/Fabric. Only `com.abto.mixin`, `com.abto.gui*`, `com.abto.platform`, entrypoints may.
- Every toggle: individually toggleable; OFF by default where it changes visuals (so the mod never surprises); respects Iris shadow passes; never hides cape/overlay/schematic rendering from COMPAT mods (Hypixel+, MinecraftCapes, Litematica); defers to a better dedicated mod when present.
- Version bumps to 0.5.0 at the end (per CLAUDE.md convention).
- Version-coupled Minecraft render APIs are javap-confirmed on the deobf jar at `~/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged-deobf/26.1.2/minecraft-merged-deobf-26.1.2.jar` before use. Mixins on 26.x need no refmap (confirmed M4).

## Batch 1 toggles
hideClouds, hideStars, hideSunMoon, hideSky, disableFog, disableBlockAnimations, dynamicFps. (Particles, weather, entity culling, glint, behind-camera culling are batch 2, noted at the end.)

---

## Risk ordering
The render pipeline is the most conflict-prone area (per the spec). Task 3 first PROVES one sky mixin (hide clouds) works in-game before the rest are built on the same pattern. If sky-render mixins do not behave, stop and revise before adding more.

---

## File Structure

- `config/FeatureToggles.java` (MODIFY: record -> mutable POJO with all toggle fields + defaults()).
- `render/RenderToggles.java` (Create; pure, no MC imports): a static snapshot of the active toggles that mixins read; `apply(FeatureToggles)` updates it; getters per flag.
- `mixin/SkyRendererMixin.java` (Create): gate cloud/star/sun-moon/sky rendering on RenderToggles.
- `mixin/FogMixin.java` (Create): disable/large-out fog when RenderToggles.disableFog.
- `mixin/BlockAnimationMixin.java` (Create): skip animated-texture ticking when RenderToggles.disableBlockAnimations.
- `mixin/FramerateLimitMixin.java` (Create): dynamic FPS when the window is unfocused/minimized.
- `preset/PresetSettings.java` (MODIFY: add the render-toggle axes).
- `preset/Presets.java` (MODIFY: set the new axes per preset).
- `preset/OptionsTarget.java` (MODIFY: add setters for the render toggles).
- `preset/PresetEngine.java` (MODIFY: apply the new axes).
- `platform/MinecraftOptionsTarget.java` (MODIFY: implement the new setters by writing FeatureToggles + refreshing RenderToggles; these are ABTO settings, not vanilla GameOptions).
- `gui/options/AbtoOptionsScreen.java` (MODIFY: add a "Performance" section with a row per toggle; Custom auto-switch when a vanilla option changes).
- `AbtoClient.java` (MODIFY: load RenderToggles from config at init).
- `src/main/resources/abto.mixins.json` (MODIFY: register the new client mixins).
- Tests: `config/FeatureTogglesTest.java`, `render/RenderTogglesTest.java`.

Note: render mixins are contract-specified with a javap step (exact render method names/classes confirmed at implementation, as in M2/M4).

---

## Task 1: Expand FeatureToggles into a mutable toggle POJO

**Files:**
- Modify: `src/main/java/com/abto/config/FeatureToggles.java`
- Test: `src/test/java/com/abto/config/FeatureTogglesTest.java`

**Interfaces:**
- Consumes: nothing.
- Produces: `FeatureToggles` as a mutable POJO (public boolean fields) with `dynamicFps` (default true), `entityCulling` (default true), `particleCulling` (default true) kept from M2, plus new fields default FALSE: `hideClouds`, `hideStars`, `hideSunMoon`, `hideSky`, `disableFog`, `disableBlockAnimations`. Static `FeatureToggles.defaults()` returns a fresh instance. (Was a record; becoming a POJO so the catalog can grow and Gson fills missing fields with the field initializers.)

- [ ] **Step 1: Write the failing test**

```java
package com.abto.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FeatureTogglesTest {
    @Test
    void defaultsAreSafe() {
        FeatureToggles t = FeatureToggles.defaults();
        // kept from M2
        assertTrue(t.dynamicFps);
        assertTrue(t.entityCulling);
        assertTrue(t.particleCulling);
        // new render toggles default OFF so the mod does not change visuals unasked
        assertFalse(t.hideClouds);
        assertFalse(t.hideStars);
        assertFalse(t.hideSunMoon);
        assertFalse(t.hideSky);
        assertFalse(t.disableFog);
        assertFalse(t.disableBlockAnimations);
    }

    @Test
    void fieldsAreMutable() {
        FeatureToggles t = FeatureToggles.defaults();
        t.hideClouds = true;
        assertTrue(t.hideClouds);
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew test --tests "com.abto.config.FeatureTogglesTest"`
Expected: FAIL (new fields do not exist / it is still a record).

- [ ] **Step 3: Replace FeatureToggles with the POJO**

```java
package com.abto.config;

/**
 * Per-feature on/off flags. A mutable POJO (not a record) so the render-toggle
 * catalog can grow and Gson fills any field missing from an older config file with
 * the field initializer below. New visual toggles default OFF so the mod never
 * changes what the player sees unless they ask.
 */
public final class FeatureToggles {

    // Kept from Milestone 2.
    public boolean dynamicFps = true;
    public boolean entityCulling = true;
    public boolean particleCulling = true;

    // Milestone 5 batch 1 (default OFF).
    public boolean hideClouds = false;
    public boolean hideStars = false;
    public boolean hideSunMoon = false;
    public boolean hideSky = false;
    public boolean disableFog = false;
    public boolean disableBlockAnimations = false;

    public static FeatureToggles defaults() {
        return new FeatureToggles();
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew test --tests "com.abto.config.FeatureTogglesTest"`
Expected: PASS. (If any code constructed FeatureToggles via its old record constructor, fix those call sites - grep `new FeatureToggles(`; AbtoConfig uses FeatureToggles.defaults() so it is unaffected.)

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/abto/config/FeatureToggles.java src/test/java/com/abto/config/FeatureTogglesTest.java
git commit -m "feat: make FeatureToggles a mutable POJO with render-toggle fields"
```

---

## Task 2: RenderToggles static snapshot (pure)

**Files:**
- Create: `src/main/java/com/abto/render/RenderToggles.java`
- Test: `src/test/java/com/abto/render/RenderTogglesTest.java`

**Interfaces:**
- Consumes: `FeatureToggles` (Task 1).
- Produces: `RenderToggles` - a static, in-memory mirror the mixins read each frame (reading the config file per frame would be far too slow). `RenderToggles.apply(FeatureToggles)` copies the flags in; getters `hideClouds()`, `hideStars()`, `hideSunMoon()`, `hideSky()`, `disableFog()`, `disableBlockAnimations()`, `dynamicFps()`. All default false/true matching FeatureToggles defaults until apply() is called. No Minecraft imports.

- [ ] **Step 1: Write the failing test**

```java
package com.abto.render;

import com.abto.config.FeatureToggles;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RenderTogglesTest {
    @Test
    void applyMirrorsFeatureToggles() {
        FeatureToggles t = FeatureToggles.defaults();
        t.hideClouds = true;
        t.disableFog = true;
        RenderToggles.apply(t);
        assertTrue(RenderToggles.hideClouds());
        assertTrue(RenderToggles.disableFog());
        assertFalse(RenderToggles.hideStars());
    }

    @Test
    void applyIsIdempotentAndOverwrites() {
        FeatureToggles on = FeatureToggles.defaults();
        on.hideStars = true;
        RenderToggles.apply(on);
        assertTrue(RenderToggles.hideStars());
        RenderToggles.apply(FeatureToggles.defaults()); // all render toggles off again
        assertFalse(RenderToggles.hideStars());
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew test --tests "com.abto.render.RenderTogglesTest"`
Expected: FAIL (class does not exist).

- [ ] **Step 3: Create RenderToggles**

```java
package com.abto.render;

import com.abto.config.FeatureToggles;

/**
 * A static, in-memory snapshot of the active render toggles. Render mixins read
 * these getters every frame, so this must be a cheap field read - never a config
 * file load. Updated via apply() when the config loads or the user changes a
 * toggle. Client render is single threaded, so plain static fields are fine.
 * No Minecraft imports (keeps it unit testable).
 */
public final class RenderToggles {

    private static volatile boolean hideClouds;
    private static volatile boolean hideStars;
    private static volatile boolean hideSunMoon;
    private static volatile boolean hideSky;
    private static volatile boolean disableFog;
    private static volatile boolean disableBlockAnimations;
    private static volatile boolean dynamicFps = true;

    private RenderToggles() {
    }

    public static void apply(FeatureToggles t) {
        hideClouds = t.hideClouds;
        hideStars = t.hideStars;
        hideSunMoon = t.hideSunMoon;
        hideSky = t.hideSky;
        disableFog = t.disableFog;
        disableBlockAnimations = t.disableBlockAnimations;
        dynamicFps = t.dynamicFps;
    }

    public static boolean hideClouds() { return hideClouds; }
    public static boolean hideStars() { return hideStars; }
    public static boolean hideSunMoon() { return hideSunMoon; }
    public static boolean hideSky() { return hideSky; }
    public static boolean disableFog() { return disableFog; }
    public static boolean disableBlockAnimations() { return disableBlockAnimations; }
    public static boolean dynamicFps() { return dynamicFps; }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew test --tests "com.abto.render.RenderTogglesTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/abto/render/RenderToggles.java src/test/java/com/abto/render/RenderTogglesTest.java
git commit -m "feat: add RenderToggles snapshot for render mixins to read"
```

---

## Task 3: Sky mixin - PROVE hide clouds, then add stars/sun-moon/sky

**Files:**
- Create: `src/main/java/com/abto/mixin/SkyRendererMixin.java`
- Modify: `src/main/resources/abto.mixins.json` (add SkyRendererMixin)
- Modify: `src/main/java/com/abto/AbtoClient.java` (call `RenderToggles.apply(config.featureToggles)` at init so the flags are live)

**Interfaces:**
- Consumes: `RenderToggles` (Task 2).
- Produces: sky elements are skipped when their toggle is on.

- [ ] **Step 1: Discover the sky render methods via javap**

```bash
JAR="$HOME/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged-deobf/26.1.2/minecraft-merged-deobf-26.1.2.jar"
unzip -l "$JAR" | grep -iE "SkyRenderer|LevelRenderer|DimensionSpecialEffects"
javap -p -cp "$JAR" net.minecraft.client.renderer.SkyRenderer 2>/dev/null | grep -iE "cloud|star|sun|moon|sky|render" | head
# If sky rendering lives on LevelRenderer instead:
javap -p -cp "$JAR" net.minecraft.client.renderer.LevelRenderer 2>/dev/null | grep -iE "renderClouds|renderSky|renderStars|renderSnowAndRain" | head
```
Record the class and the exact method names for clouds, stars, sun/moon, and the sky/skybox. (In recent versions these are on `net.minecraft.client.renderer.SkyRenderer`: e.g. renderClouds, renderStars, renderSunMoonAndStars / renderSun / renderMoon, renderSkyDisc/renderEndSky. Use whatever javap shows.)

- [ ] **Step 2: Write SkyRendererMixin (prove clouds first)**

Target the confirmed class. For EACH element, @Inject at HEAD, cancellable, and cancel when its RenderToggles flag is on:
```java
package com.abto.mixin;

import com.abto.render.RenderToggles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
// import the confirmed sky renderer class

@Mixin(/* confirmed sky renderer class, e.g. net.minecraft.client.renderer.SkyRenderer.class */)
public class SkyRendererMixin {

    @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
    private void abto$maybeSkipClouds(/* match the real params */ CallbackInfo ci) {
        if (RenderToggles.hideClouds()) {
            ci.cancel();
        }
    }
    // Then, once clouds is confirmed working in game, add the same pattern for:
    //   renderStars   -> RenderToggles.hideStars()
    //   renderSun/Moon (or the combined method) -> RenderToggles.hideSunMoon()
    //   the sky disc/gradient method -> RenderToggles.hideSky()
    // matching each method's real signature (CallbackInfo params) from javap.
}
```
Add `SkyRendererMixin` to abto.mixins.json client list. Add `RenderToggles.apply(config.featureToggles);` at the end of AbtoClient.onInitializeClient (after config load) so the flags reflect the saved config.

- [ ] **Step 3: Build and prove clouds in game**

Run: `./gradlew build --stacktrace` then `./build-all.sh`. Both versions must build (defaultRequire=1 confirms the injection targets resolve). Manual (maintainer): set `"hideClouds": true` in config/abto.json featureToggles, launch, confirm clouds are gone. THEN extend the mixin to stars/sun-moon/sky and rebuild. Record which method names were used per version and any Stonecutter guard needed.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/abto/mixin/SkyRendererMixin.java src/main/resources/abto.mixins.json src/main/java/com/abto/AbtoClient.java
git commit -m "feat: hide clouds/stars/sun-moon/sky via toggleable sky mixin"
```

---

## Task 4: Fog disable mixin

**Files:**
- Create: `src/main/java/com/abto/mixin/FogMixin.java`
- Modify: `src/main/resources/abto.mixins.json`

**Interfaces:**
- Consumes: `RenderToggles.disableFog()`.
- Produces: fog is removed/pushed far out when the toggle is on.

- [ ] **Step 1: Discover the fog setup via javap**

```bash
JAR="$HOME/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged-deobf/26.1.2/minecraft-merged-deobf-26.1.2.jar"
unzip -l "$JAR" | grep -iE "FogRenderer|/Fog"
javap -p -cp "$JAR" net.minecraft.client.renderer.fog.FogRenderer 2>/dev/null | grep -iE "setupFog|computeFog|getFog|render" | head
```
Fog handling moved to a fog renderer/environment in recent versions. Record the method that produces the fog parameters (start/end distance or a FogParameters). Strategy: @Inject/@ModifyReturnValue (MixinExtras) to return a "no fog" result (very large end distance, or the parameters that disable fog) when disableFog is on. Confirm the exact method and return type.

- [ ] **Step 2: Write FogMixin**

Using the confirmed method, when `RenderToggles.disableFog()` is true, override the fog result so fog is effectively off (push the far plane out / return cleared fog parameters). Do not disable fog that gameplay relies on for correctness beyond visibility (this is a visual/FPS toggle). Add `FogMixin` to abto.mixins.json.

- [ ] **Step 3: Build and verify**

Run: `./gradlew build --stacktrace` and `./build-all.sh`. Both build. Manual: set `"disableFog": true`, confirm fog is gone. Guard per version only if the fog method differs; record it.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/abto/mixin/FogMixin.java src/main/resources/abto.mixins.json
git commit -m "feat: disable fog via toggle mixin"
```

---

## Task 5: Disable block texture animations

**Files:**
- Create: `src/main/java/com/abto/mixin/BlockAnimationMixin.java`
- Modify: `src/main/resources/abto.mixins.json`

**Interfaces:**
- Consumes: `RenderToggles.disableBlockAnimations()`.
- Produces: animated textures (water, lava, fire, portal, etc.) stop ticking when the toggle is on, saving the per-frame texture upload work.

- [ ] **Step 1: Discover the texture animation tick via javap**

```bash
JAR="$HOME/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged-deobf/26.1.2/minecraft-merged-deobf-26.1.2.jar"
unzip -l "$JAR" | grep -iE "SpriteContents|TextureAtlasSprite|SpriteTicker"
javap -p -cp "$JAR" net.minecraft.client.renderer.texture.SpriteContents\$Ticker 2>/dev/null | grep -iE "tick" | head
javap -p -cp "$JAR" net.minecraft.client.renderer.texture.SpriteContents 2>/dev/null | grep -iE "tick|Ticker" | head
```
The animated-sprite ticker's `tick()` (on SpriteContents.Ticker or the animated sprite) advances animation frames and uploads them. Record the exact class and tick method.

- [ ] **Step 2: Write BlockAnimationMixin**

@Inject at HEAD of the sprite animation `tick`, cancellable; cancel when `RenderToggles.disableBlockAnimations()` is on, so frames stop advancing (the texture holds its current frame - acceptable and standard for this optimization). Add to abto.mixins.json.

- [ ] **Step 3: Build and verify**

Run: `./gradlew build --stacktrace` and `./build-all.sh`. Both build. Manual: set `"disableBlockAnimations": true`, confirm water/lava/fire stop animating and FPS improves in heavy scenes. Guard per version only if the ticker differs; record it.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/abto/mixin/BlockAnimationMixin.java src/main/resources/abto.mixins.json
git commit -m "feat: disable animated block textures via toggle mixin"
```

---

## Task 6: Dynamic FPS (lower framerate when unfocused/minimized)

**Files:**
- Create: `src/main/java/com/abto/mixin/FramerateLimitMixin.java`
- Modify: `src/main/resources/abto.mixins.json`

**Interfaces:**
- Consumes: `RenderToggles.dynamicFps()`.
- Produces: when dynamicFps is on and the window is not focused (or is minimized), the effective framerate limit drops sharply to save GPU/battery.

- [ ] **Step 1: Discover the framerate-limit method via javap**

```bash
JAR="$HOME/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged-deobf/26.1.2/minecraft-merged-deobf-26.1.2.jar"
javap -p -cp "$JAR" net.minecraft.client.Minecraft 2>/dev/null | grep -iE "getFramerateLimit|isWindowActive|hasWindow|window" | head
javap -p -cp "$JAR" net.minecraft.client.Minecraft 2>/dev/null | grep -iE "isWindowActive|getWindow" | head
```
`Minecraft.getFramerateLimit()` returns the current cap the render loop uses. `Minecraft` also exposes window focus (e.g. isWindowActive()). Record both.

- [ ] **Step 2: Write FramerateLimitMixin**

@Inject (MixinExtras @ModifyReturnValue) on `getFramerateLimit`: when `RenderToggles.dynamicFps()` is on and the window is not active, return a low cap (for example 10 when minimized, 30 when merely unfocused). Otherwise return the original. Use the confirmed window-focus accessor. Add to abto.mixins.json.

- [ ] **Step 3: Build and verify**

Run: `./gradlew build --stacktrace` and `./build-all.sh`. Both build. Manual: alt-tab away, confirm FPS drops; refocus, confirm it restores.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/abto/mixin/FramerateLimitMixin.java src/main/resources/abto.mixins.json
git commit -m "feat: dynamic FPS when window is unfocused or minimized"
```

---

## Task 7: Surface the toggles in the options screen + Custom auto-switch

**Files:**
- Modify: `src/main/java/com/abto/gui/options/AbtoOptionsScreen.java`

**Interfaces:**
- Consumes: `FeatureToggles`, `ConfigStore`, `RenderToggles`, `Preset`.
- Produces: a "Performance" section in the screen with an on/off button per batch-1 toggle; changing one saves config and calls `RenderToggles.apply(...)` so it takes effect live. When the user changes any VANILLA option, the selected preset switches to CUSTOM and is saved (so Custom stops being a dead option).

- [ ] **Step 1: Add the Performance section**

In `addOptions()`, after the Video section, add:
```java
this.list.addHeader(Component.literal("Performance (A Bit Too Optimized)"));
this.list.addSmall(List.<AbstractWidget>of(
    toggle("Hide clouds", () -> load().featureToggles.hideClouds, (c, v) -> c.featureToggles.hideClouds = v),
    toggle("Hide stars", () -> load().featureToggles.hideStars, (c, v) -> c.featureToggles.hideStars = v)));
this.list.addSmall(List.<AbstractWidget>of(
    toggle("Hide sun & moon", () -> load().featureToggles.hideSunMoon, (c, v) -> c.featureToggles.hideSunMoon = v),
    toggle("Hide sky", () -> load().featureToggles.hideSky, (c, v) -> c.featureToggles.hideSky = v)));
this.list.addSmall(List.<AbstractWidget>of(
    toggle("Disable fog", () -> load().featureToggles.disableFog, (c, v) -> c.featureToggles.disableFog = v),
    toggle("Disable block animations", () -> load().featureToggles.disableBlockAnimations,
        (c, v) -> c.featureToggles.disableBlockAnimations = v)));
this.list.addSmall(List.<AbstractWidget>of(
    toggle("Dynamic FPS", () -> load().featureToggles.dynamicFps, (c, v) -> c.featureToggles.dynamicFps = v)));
```
Add a `toggle(...)` helper that builds a Button labeled `"<name>: ON/OFF"`, and on click: load config, flip the field via the setter, save, `RenderToggles.apply(cfg.featureToggles)`, update the button message. Add a `load()` helper returning `ConfigStore.load(configPath())`. Reuse the existing configPath(). All ASCII.

- [ ] **Step 2: Custom auto-switch on vanilla change**

When a vanilla OptionInstance value changes, the selected preset no longer matches. The simplest reliable hook: override `onClose()` (or wrap the list's apply) so that if any vanilla option differs from the selected preset's expected value, set `selectedPreset = CUSTOM` and save. A lighter approach acceptable for this task: whenever the user opens the Video section and edits an option, mark Custom on close - implement by comparing, on `onClose()`, the live options to the selected preset's PresetSettings via a read-only OptionsTarget snapshot; if they differ and the preset is not already CUSTOM, set CUSTOM and save. Keep it simple and documented; if a full comparison is too broad here, at minimum set CUSTOM whenever the Video options are touched (a boolean dirty flag set by a mixin-free listener is out of scope; use the onClose comparison). Record the chosen approach.

- [ ] **Step 3: Build and verify**

Run: `./gradlew build --stacktrace`, `./build-all.sh`, `./gradlew test`. All pass/both build. Manual: toggles appear in a Performance section, flip live, persist; hand-editing a video option then reopening shows preset = Custom.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/abto/gui/options/AbtoOptionsScreen.java
git commit -m "feat: performance toggle rows + custom auto-switch in options screen"
```

---

## Task 8: Make the toggles preset axes

**Files:**
- Modify: `src/main/java/com/abto/preset/PresetSettings.java`, `Presets.java`, `OptionsTarget.java`, `PresetEngine.java`
- Modify: `src/main/java/com/abto/platform/MinecraftOptionsTarget.java`
- Test: update `src/test/java/com/abto/preset/PresetsTest.java`, `PresetEngineTest.java`

**Interfaces:**
- Consumes: the toggles.
- Produces: `PresetSettings` gains boolean axes `hideClouds, hideStars, hideSunMoon, hideSky, disableFog, disableBlockAnimations` (existing 10 axes unchanged, appended); `Presets` sets them (Potato and Very Low: all true; Low: hideStars+disableBlockAnimations true, rest false; Normal/High/Ultra: all false). `OptionsTarget` gains `setHideClouds(boolean)` ... `setDisableBlockAnimations(boolean)`; `PresetEngine.apply` writes them; `MinecraftOptionsTarget` implements them by mutating the loaded FeatureToggles, saving config, and calling `RenderToggles.apply(...)`.

- [ ] **Step 1: Update PresetSettings test then the record**

Extend `PresetSettingsTest` to construct with the new axes and assert them; extend the `PresetSettings` record with the six new boolean components appended after `entityDistanceScaling`. Run the test.

- [ ] **Step 2: Update the Presets table and its test**

Set the six new booleans per preset as above; extend `PresetsTest` to assert Potato has hideStars/disableFog/disableBlockAnimations true and Ultra has them false. Run.

- [ ] **Step 3: Extend OptionsTarget + PresetEngine + FakeTarget test**

Add the six setters to `OptionsTarget`; have `PresetEngine.apply` call them from the PresetSettings; extend `PresetEngineTest`'s FakeTarget to capture them and assert they are written. Run.

- [ ] **Step 4: Implement the setters in MinecraftOptionsTarget**

Each new setter loads the config, sets the matching `featureToggles` field, saves, and calls `RenderToggles.apply(cfg.featureToggles)`. (These are ABTO settings, not vanilla GameOptions.) Build.

- [ ] **Step 5: Build all + test**

Run: `./gradlew test` (all pass), `./build-all.sh` (both build).

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/abto/preset/ src/main/java/com/abto/platform/MinecraftOptionsTarget.java src/test/java/com/abto/preset/
git commit -m "feat: render toggles as preset axes (Potato/Very Low enable the aggressive ones)"
```

---

## Task 9: Version bump and final build

**Files:**
- Modify: `gradle.properties` (`mod_version=0.5.0`)

- [ ] **Step 1: Bump to 0.5.0**

Set `mod_version=0.5.0` in gradle.properties.

- [ ] **Step 2: Full build + test**

Run: `./gradlew test` (all pass), `./build-all.sh` (both build; dist holds only `a-bit-too-optimized-26.1.2-0.5.0.jar` and `a-bit-too-optimized-26.2-0.5.0.jar`).

- [ ] **Step 3: Commit**

```bash
git add gradle.properties
git commit -m "chore: bump mod version to 0.5.0 (render config batch 1)"
```

---

## Milestone Done When

- `./gradlew test` passes (FeatureToggles, RenderToggles, updated Presets/PresetEngine, plus all prior).
- `./gradlew build` + `./build-all.sh` succeed on both 26.1.2 and 26.2.
- Manual (maintainer): the Performance section shows the toggles; hide clouds/stars/sun-moon/sky, disable fog, disable block animations, and dynamic FPS each work in game and persist; Potato flips the aggressive ones on; Custom is selected after hand-editing a video option; no crash; capes/Litematica/Hypixel+ rendering is unaffected.

## Notes for Later (Batch 2 and beyond)
- Batch 2 toggles (same pattern - FeatureToggles field + RenderToggles flag + mixin + row + preset axis): all-particles-off and per-category particle disables; disable weather rendering + weather particles; entity culling (defer to EntityCulling), disable entity shadows, disable glint animation; behind-camera culling (opt-in, off by default, stutter warning).
- Iris/shader compat: gate the sky/fog mixins so they do not fight a loaded shader pack (check FabricLoader.isModLoaded("iris") and skip or adjust when a shader pack is active); verify with Iris installed.
- Defer-to-better-mod: when EntityCulling is present, ABTO's entity culling stays off.
- M6 remains the Sodium config adapters (Fabulously Optimized parity).
