# A Bit Too Optimized - Milestone 4: Sodium-style Video Settings Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace Minecraft's Video Settings screen with a Sodium-style ABTO options screen (left tab pages, option rows with the control on the right, a hover-updated description panel at the bottom, Done/Apply), reusing every vanilla video option via its existing widget plus ABTO's presets and tweaks, redirected from the vanilla button via the project's first mixin.

**Architecture:** A small reusable options-screen framework lives in `com.abto.gui.options` (pure page/row model, unit-tested) plus the version-coupled `Screen` that renders it. Vanilla options are reused through their existing `OptionInstance` widgets (not reimplemented). A mixin (the project's first) redirects the vanilla "Video Settings" button to the ABTO screen. ABTO content (presets, tweaks) is added as extra pages. All persistence/apply continues to go through the existing tested core (ConfigStore, PresetEngine, Presets).

**Tech Stack:** Java 25, Fabric Loom 1.15.5, Stonecutter 0.9.6, Fabric API, Mod Menu, SpongePowered Mixin (via Loom), JUnit 5. Builds on Milestones 1-3.

## Global Constraints

- Mod id `abto`; client-side only; Java 25; Gradle wrapper 9.4.0; Loom 1.15.5; Stonecutter 0.9.6; active version 26.1.2; second version 26.2; non-remapping toolchain uses `implementation()`.
- No em dashes, no emojis, plain ASCII only, in all project-authored files and GUI text. Vendor/generated files exempt.
- Every commit message ends with a blank line then: `Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>`.
- `.superpowers/` and build outputs must not be committed.
- CORE UNIT-TESTABILITY RULE: `com.abto.gui.options.model` (the page/row data model) and other `com.abto.*` core packages MUST NOT import Minecraft/Fabric. Only `com.abto.gui` Screen classes, `com.abto.mixin`, `com.abto.platform`, and entrypoints may.
- Version-coupled Minecraft APIs (Screen, OptionInstance, the Video Settings screen class, the options-screen button) use Mojang official mappings; verify exact names with javap on the deobf jar at `~/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged-deobf/26.1.2/minecraft-merged-deobf-26.1.2.jar` (as in Milestone 2 Task 9 and Milestone 3).
- The mod version bumps to 0.4.0 at the end of this milestone (per CLAUDE.md convention).
- This milestone REPLACES the Milestone 3 `AbtoConfigScreen` (the cramped button screen) with the new Sodium-style screen. The first-run wizard (AbtoWizardScreen) and its launcher stay.

---

## Risks and verify-first ordering

The two riskiest assumptions are validated before any framework is built:
- Task 1 proves mixins compile and load on both 26.1.2 and 26.2 (first mixin in the project).
- Task 2 proves the vanilla "Video Settings" button can be redirected to an ABTO screen.
- Task 3 proves vanilla `OptionInstance` widgets can be reused in a custom screen.
If any of these fails, stop and revise the approach before continuing (escalate to the human).

---

## File Structure

- `src/main/resources/abto.mixins.json` - mixin config (client mixins, refmap).
- `src/main/java/com/abto/mixin/OptionsScreenMixin.java` - redirects the Video Settings button to the ABTO screen. Version-coupled.
- `src/main/java/com/abto/gui/options/model/OptionPageId.java` - enum of ABTO's own page ids (PRESETS, VIDEO, TWEAKS). Pure.
- `src/main/java/com/abto/gui/options/model/PageModel.java` - holds the ordered pages and the selected page; selection logic. Pure, unit-tested.
- `src/main/java/com/abto/gui/options/AbtoOptionsScreen.java` - the Sodium-style screen (tabs, rows area, description panel, Done/Apply). Version-coupled.
- `src/main/java/com/abto/gui/options/OptionRowList.java` - the scrollable list widget of rows for the active page, with hover->description. Version-coupled.
- `src/main/java/com/abto/gui/options/VanillaVideoRows.java` - collects vanilla video OptionInstances and exposes their widgets + descriptions. Version-coupled.
- `src/main/java/com/abto/gui/options/PresetRows.java` - ABTO's preset/tweaks rows (preset selector, shaders, apply-to-mods, run-setup). Version-coupled but delegates to tested core.
- Modify: `src/main/resources/fabric.mod.json` - register the mixin config.
- Modify: `build.gradle.kts` - ensure the mixin annotation processor / Loom mixin setup is active (Loom usually auto-configures; confirm).
- Modify: `src/main/java/com/abto/platform/ModMenuIntegration.java` - open `AbtoOptionsScreen` instead of `AbtoConfigScreen`.
- Delete: `src/main/java/com/abto/gui/AbtoConfigScreen.java` (replaced) - and remove its references; keep AbtoWizardScreen.
- `src/test/java/com/abto/gui/options/model/PageModelTest.java` - unit tests for the page model.

Note: the version-coupled GUI/mixin tasks are contract-specified with a javap verification step; exact Minecraft method/field names and pixel layout are confirmed during implementation, not guessed (same approach proven in Milestones 2-3).

---

## Task 1: Mixin infrastructure (first mixin) - prove it loads

**Files:**
- Create: `src/main/resources/abto.mixins.json`
- Create: `src/main/java/com/abto/mixin/TitleScreenProbeMixin.java` (a trivial proof mixin, removed or kept harmless later)
- Modify: `src/main/resources/fabric.mod.json` (add `"mixins": ["abto.mixins.json"]`)
- Modify: `build.gradle.kts` if Loom needs an explicit mixin block

**Interfaces:**
- Consumes: nothing new.
- Produces: a working mixin pipeline. After this task, mixins in `com.abto.mixin` are applied on both versions.

- [ ] **Step 1: Create the mixin config `abto.mixins.json`**

```json
{
  "required": true,
  "minVersion": "0.8",
  "package": "com.abto.mixin",
  "compatibilityLevel": "JAVA_21",
  "client": [
    "TitleScreenProbeMixin"
  ],
  "injectors": {
    "defaultRequire": 1
  }
}
```
Note: confirm the `compatibilityLevel` the toolchain accepts for Java 25 (it may be `JAVA_21` or a newer token); if the build rejects it, use the highest level the mixin version supports and record it.

- [ ] **Step 2: Create a trivial proof mixin**

Verify the TitleScreen mapped class via javap, then:
```java
package com.abto.mixin;

import com.abto.Abto;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenProbeMixin {
    @Inject(method = "init", at = @At("TAIL"))
    private void abto$onTitleInit(CallbackInfo ci) {
        Abto.LOGGER.info("ABTO mixin active.");
    }
}
```
Confirm the `init` method name on `TitleScreen` via javap; adjust if the mapped name differs.

- [ ] **Step 3: Register the mixin in fabric.mod.json**

Add a top-level key:
```json
  "mixins": ["abto.mixins.json"],
```

- [ ] **Step 4: Build and confirm the mixin applies**

Run: `./gradlew build --stacktrace` then `./build-all.sh`.
Expected: BUILD SUCCESSFUL on both versions. The mixin compiles and the refmap is generated. If Loom needs an explicit `loom { mixin { defaultRefmapName.set("abto.refmap.json") } }` block to build, add it and record that. If `runClient` is run, the log shows "ABTO mixin active." on the title screen (manual, optional here).

- [ ] **Step 5: Commit**

```bash
git add src/main/resources/abto.mixins.json src/main/java/com/abto/mixin/ src/main/resources/fabric.mod.json build.gradle.kts
git commit -m "build: add mixin infrastructure with a proof mixin"
```

---

## Task 2: Redirect the Video Settings button via mixin

**Files:**
- Create: `src/main/java/com/abto/gui/options/AbtoOptionsScreen.java` (minimal stub for now: extends Screen, ctor `(Screen parent)`, title, empty)
- Create: `src/main/java/com/abto/mixin/OptionsScreenMixin.java`
- Modify: `src/main/resources/abto.mixins.json` (add `OptionsScreenMixin`, can drop the probe mixin from `client` or keep it)

**Interfaces:**
- Consumes: Task 1 mixin pipeline.
- Produces: clicking "Video Settings" in the vanilla Options screen opens `AbtoOptionsScreen` instead of the vanilla video settings screen.

- [ ] **Step 1: Identify the target via javap**

The vanilla Options screen builds a "Video Settings" button whose action opens the video settings screen. Find the Options screen class and how it opens video settings:
```bash
JAR="$HOME/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged-deobf/26.1.2/minecraft-merged-deobf-26.1.2.jar"
unzip -l "$JAR" | grep -iE "OptionsScreen|VideoSettings"
javap -cp "$JAR" net.minecraft.client.gui.screens.options.OptionsScreen | grep -iE "init|video|button|<init>"
javap -cp "$JAR" net.minecraft.client.gui.screens.options.VideoSettingsScreen | grep -iE "<init>"
```
Record the exact class names and the construction site. The redirect strategy: mixin into the lambda/method that creates the VideoSettingsScreen (or into OptionsScreen building the button) and instead push `new AbtoOptionsScreen(currentScreen)`. A robust approach used by Reese's Sodium Options is to `@Redirect`/`@WrapOperation` the `new VideoSettingsScreen(...)` construction, or `@Inject` at the button's onPress and cancel + open ours. Choose the approach that the mapped code allows and record it.

- [ ] **Step 2: Create the AbtoOptionsScreen stub**

```java
package com.abto.gui.options;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class AbtoOptionsScreen extends Screen {
    private final Screen parent;

    public AbtoOptionsScreen(Screen parent) {
        super(Component.literal("A Bit Too Optimized - Video"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        // Filled in by later tasks.
    }

    @Override
    public void onClose() {
        this.minecraft.setScreenAndShow(parent);
    }
}
```

- [ ] **Step 3: Write the redirect mixin**

Using the javap-confirmed target, write `OptionsScreenMixin` that opens `AbtoOptionsScreen` where vanilla would open the video settings screen. Example shape (adapt to the real mapped names and the chosen injection point):
```java
package com.abto.mixin;

import com.abto.gui.options.AbtoOptionsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import org.spongepowered.asm.mixin.Mixin;
// import the injection annotations that match the chosen strategy

@Mixin(OptionsScreen.class)
public class OptionsScreenMixin {
    // Inject/redirect so that the Video Settings action opens AbtoOptionsScreen
    // with the current screen as parent instead of the vanilla VideoSettingsScreen.
}
```
Add `OptionsScreenMixin` to `abto.mixins.json`.

- [ ] **Step 4: Build and verify**

Run: `./gradlew build --stacktrace` and `./build-all.sh`. Both versions must build. Manual check (record as pending for the maintainer if not run here): in game, Options -> Video Settings opens the ABTO screen (currently empty) and Done/Esc returns to Options. If the mixin fails to apply on 26.2 due to a different target, guard with a Stonecutter directive or use a version-appropriate injection point and record it.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/abto/gui/options/AbtoOptionsScreen.java src/main/java/com/abto/mixin/OptionsScreenMixin.java src/main/resources/abto.mixins.json
git commit -m "feat: redirect vanilla Video Settings button to ABTO screen via mixin"
```

---

## Task 3: Page model (pure, unit-tested)

**Files:**
- Create: `src/main/java/com/abto/gui/options/model/OptionPageId.java`, `src/main/java/com/abto/gui/options/model/PageModel.java`
- Test: `src/test/java/com/abto/gui/options/model/PageModelTest.java`

**Interfaces:**
- Consumes: nothing.
- Produces: enum `OptionPageId{PRESETS, VIDEO, TWEAKS}`. `PageModel` constructed with the ordered list of page ids; methods `List<OptionPageId> pages()`, `OptionPageId selected()`, `void select(OptionPageId)`, `int selectedIndex()`. Default selected is the first page. No Minecraft imports.

- [ ] **Step 1: Write the failing test**

```java
package com.abto.gui.options.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PageModelTest {
    @Test
    void defaultsToFirstPage() {
        PageModel m = new PageModel(List.of(OptionPageId.PRESETS, OptionPageId.VIDEO, OptionPageId.TWEAKS));
        assertEquals(OptionPageId.PRESETS, m.selected());
        assertEquals(0, m.selectedIndex());
    }

    @Test
    void selectChangesPage() {
        PageModel m = new PageModel(List.of(OptionPageId.PRESETS, OptionPageId.VIDEO, OptionPageId.TWEAKS));
        m.select(OptionPageId.VIDEO);
        assertEquals(OptionPageId.VIDEO, m.selected());
        assertEquals(1, m.selectedIndex());
    }

    @Test
    void selectingUnknownPageIsIgnored() {
        PageModel m = new PageModel(List.of(OptionPageId.PRESETS, OptionPageId.VIDEO));
        m.select(OptionPageId.TWEAKS); // not in this model
        assertEquals(OptionPageId.PRESETS, m.selected());
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew test --tests "com.abto.gui.options.model.PageModelTest"`
Expected: FAIL (classes do not exist).

- [ ] **Step 3: Create the classes**

`OptionPageId.java`:
```java
package com.abto.gui.options.model;

public enum OptionPageId {
    PRESETS, VIDEO, TWEAKS
}
```

`PageModel.java`:
```java
package com.abto.gui.options.model;

import java.util.List;

/**
 * Holds the ordered tab pages and which one is selected. Pure logic so the
 * Sodium-style screen stays a thin renderer.
 */
public final class PageModel {

    private final List<OptionPageId> pages;
    private OptionPageId selected;

    public PageModel(List<OptionPageId> pages) {
        if (pages.isEmpty()) {
            throw new IllegalArgumentException("PageModel needs at least one page");
        }
        this.pages = List.copyOf(pages);
        this.selected = this.pages.get(0);
    }

    public List<OptionPageId> pages() {
        return pages;
    }

    public OptionPageId selected() {
        return selected;
    }

    public int selectedIndex() {
        return pages.indexOf(selected);
    }

    public void select(OptionPageId page) {
        if (pages.contains(page)) {
            selected = page;
        }
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew test --tests "com.abto.gui.options.model.PageModelTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/abto/gui/options/model/ src/test/java/com/abto/gui/options/model/PageModelTest.java
git commit -m "feat: add option page model for the sodium-style screen"
```

---

## Task 4: Reuse vanilla video OptionInstances - prove it

**Files:**
- Create: `src/main/java/com/abto/gui/options/VanillaVideoRows.java`

**Interfaces:**
- Consumes: Minecraft `Options` and `OptionInstance`.
- Produces: `VanillaVideoRows.collect(Minecraft client)` returning a `List<OptionInstance<?>>` of the vanilla video options (render distance, brightness, gui scale, fullscreen, vsync, graphics, clouds, particles, etc.), and `VanillaVideoRows.makeWidget(OptionInstance<?> opt, Options options, int width)` returning the option's own widget. Version-coupled.

- [ ] **Step 1: Discover the OptionInstance API and the vanilla video option list via javap**

```bash
JAR="$HOME/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged-deobf/26.1.2/minecraft-merged-deobf-26.1.2.jar"
javap -cp "$JAR" net.minecraft.client.OptionInstance | grep -iE "createButton|button|<init>|getTooltip"
javap -cp "$JAR" net.minecraft.client.Options | grep -iE "renderDistance|gamma|guiScale|fullscreen|vsync|graphic|cloud|particle|fov|bobbing|mipmap|biomeBlend|entityShadows|entityDistance"
javap -cp "$JAR" net.minecraft.client.gui.screens.options.VideoSettingsScreen | grep -iE "options|OPTIONS|field"
```
The vanilla VideoSettingsScreen has a list/array of OptionInstance it renders; replicate that list by calling the corresponding `Options` getters. Record the real getter names and the OptionInstance widget-creation method (commonly `createButton(Options)` returning an `AbstractWidget`).

- [ ] **Step 2: Implement VanillaVideoRows**

Write `collect(...)` to return the list of vanilla video OptionInstances (use the same set the vanilla VideoSettingsScreen uses), and `makeWidget(...)` to call the OptionInstance's own widget-creation method at the given width. Use the javap-confirmed names. Keep it a thin adapter - no reimplementation of option behavior.

- [ ] **Step 3: Build and verify**

Run: `./gradlew build --stacktrace` and `./build-all.sh`. Both versions build. The proof that reuse works is compilation against the real OptionInstance widget API plus, when the screen renders them (Task 6), the controls behave like vanilla. Record the confirmed method names in the report.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/abto/gui/options/VanillaVideoRows.java
git commit -m "feat: collect and reuse vanilla video option widgets"
```

---

## Task 5: ABTO preset and tweak rows

**Files:**
- Create: `src/main/java/com/abto/gui/options/PresetRows.java`

**Interfaces:**
- Consumes: `PresetButtonList` (M3), `Presets`/`Preset`/`PresetEngine` (M2), `ConfigStore`/`AbtoConfig` (M2), `MinecraftOptionsTarget` (M2).
- Produces: `PresetRows` exposing the widgets and descriptions for ABTO's own rows: a preset selector (cycles or lists the presets, short name, description on hover), a "Use shaders" toggle, an "Apply to other mods" toggle, and a "Run setup again" button. Each row, when changed, persists via ConfigStore and (for preset) applies via PresetEngine, exactly as the M3 screen did.

- [ ] **Step 1: Build PresetRows against the row/widget API**

Reuse the persistence/apply logic from the old AbtoConfigScreen (load config, set field, save, apply preset for non-CUSTOM). Present the preset as a Sodium-style row: a value label showing the selected preset's short name with left/right arrows or a cycle button; the description for the hovered/selected preset comes from `PresetButtonList.entries()`. Toggles use the same on/off button pattern. All text plain ASCII. Use the widget API confirmed in Tasks 2-4 and Milestone 3 (Button.builder(...).bounds(...).build(), Component.literal).

- [ ] **Step 2: Build**

Run: `./gradlew build --stacktrace` and `./build-all.sh`. Both versions build.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/abto/gui/options/PresetRows.java
git commit -m "feat: add ABTO preset and tweak rows"
```

---

## Task 6: Sodium-style screen - tabs, scrollable rows, description panel

**Files:**
- Create: `src/main/java/com/abto/gui/options/OptionRowList.java`
- Modify: `src/main/java/com/abto/gui/options/AbtoOptionsScreen.java` (replace the stub with the full screen)

**Interfaces:**
- Consumes: `PageModel` (Task 3), `VanillaVideoRows` (Task 4), `PresetRows` (Task 5), `OptionPageId`.
- Produces: the full Sodium-style screen: a left column of tab buttons (one per PageModel page), a scrollable main area listing the active page's rows (label left, control right), a description panel along the bottom that shows the hovered row's description text, and Done/Apply buttons. PRESETS page = PresetRows; VIDEO page = VanillaVideoRows widgets; TWEAKS page = the shaders/apply-to-mods/run-setup rows (or fold these into PRESETS and use TWEAKS for the M4 future toggles - implementer's call, record it).

- [ ] **Step 1: Discover the scroll/list and rendering API via javap**

```bash
JAR="$HOME/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged-deobf/26.1.2/minecraft-merged-deobf-26.1.2.jar"
javap -cp "$JAR" net.minecraft.client.gui.components.AbstractSelectionList | grep -iE "<init>|setScrollAmount|renderWidget|addEntry"
javap -cp "$JAR" net.minecraft.client.gui.GuiGraphics | grep -iE "drawString|fill|enableScissor|blit"
javap -cp "$JAR" net.minecraft.client.gui.screens.Screen | grep -iE "render|font|width|height"
```
Use either the vanilla `AbstractSelectionList`/`ContainerObjectSelectionList` (which handles scrolling and clipping safely - preferred to avoid the scissor pitfalls seen in Milestone 3) or a manual scroll with safe bounds via `ButtonColumn`-style clamping. Record the choice.

- [ ] **Step 2: Implement OptionRowList and AbtoOptionsScreen**

Build the scrollable list of rows for the active page using the chosen API. Each row draws its label and places the option's control widget. Hovering a row updates the bottom description panel (use the row's description text). Tab buttons call `pageModel.select(...)` then rebuild. Done applies/saves and closes to parent; Apply (if present) saves without closing. Reuse `MinecraftOptionsTarget`/`ConfigStore` for ABTO rows; vanilla rows persist through their own OptionInstance behavior (they write to Options; call `options.save()` on close). All text plain ASCII. Ensure NO widget is placed off-screen (use the list widget's own clipping, or ButtonColumn clamping) so the Milestone 3 scissor crash cannot recur.

- [ ] **Step 3: Build and verify**

Run: `./gradlew build --stacktrace` and `./build-all.sh`. Both versions build. Manual check (maintainer): Options -> Video Settings opens the ABTO screen; tabs switch pages; vanilla options work; presets apply; hovering shows descriptions; Done saves and returns; no crash at GUI scale 4.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/abto/gui/options/OptionRowList.java src/main/java/com/abto/gui/options/AbtoOptionsScreen.java
git commit -m "feat: sodium-style options screen with tabs, scrolling rows, description panel"
```

---

## Task 7: Point Mod Menu at the new screen, retire the old config screen

**Files:**
- Modify: `src/main/java/com/abto/platform/ModMenuIntegration.java` (open `AbtoOptionsScreen`)
- Modify: `src/main/java/com/abto/gui/AbtoWizardScreen.java` (the "Run setup again" lives in the new screen now; the wizard's Finish still returns to parent - confirm no dangling reference to AbtoConfigScreen)
- Delete: `src/main/java/com/abto/gui/AbtoConfigScreen.java`

**Interfaces:**
- Consumes: `AbtoOptionsScreen`.
- Produces: Mod Menu opens the new Sodium-style screen; `AbtoConfigScreen` is gone with no remaining references.

- [ ] **Step 1: Update ModMenuIntegration**

```java
import com.abto.gui.options.AbtoOptionsScreen;
// ...
@Override
public ConfigScreenFactory<?> getModConfigScreenFactory() {
    return parent -> new AbtoOptionsScreen(parent);
}
```

- [ ] **Step 2: Remove AbtoConfigScreen and fix references**

Delete `src/main/java/com/abto/gui/AbtoConfigScreen.java`. Grep for `AbtoConfigScreen` across the source and remove/redirect every reference (the wizard's "Run setup again" button, if it pointed back to AbtoConfigScreen, should point to AbtoOptionsScreen or just close). Run:
```bash
grep -rn "AbtoConfigScreen" src/ || echo "no references remain"
```
Expected: no references remain.

- [ ] **Step 3: Build and test**

Run: `./gradlew test` (all unit tests pass), `./gradlew build --stacktrace` (26.1.2), `./build-all.sh` (both versions).

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/abto/platform/ModMenuIntegration.java src/main/java/com/abto/gui/AbtoWizardScreen.java
git rm src/main/java/com/abto/gui/AbtoConfigScreen.java
git commit -m "feat: open sodium-style screen from mod menu, retire old config screen"
```

---

## Task 8: Version bump and final build

**Files:**
- Modify: `gradle.properties` (`mod_version=0.4.0`)

**Interfaces:**
- Consumes: everything above.
- Produces: 0.4.0 jars for both versions.

- [ ] **Step 1: Bump the version**

Set `mod_version=0.4.0` in gradle.properties (per the CLAUDE.md per-milestone convention).

- [ ] **Step 2: Full build and test**

Run: `./gradlew test` (all pass), then `./build-all.sh`. Confirm `dist/` holds `a-bit-too-optimized-26.1.2-0.4.0.jar` and `a-bit-too-optimized-26.2-0.4.0.jar` only.

- [ ] **Step 3: Commit**

```bash
git add gradle.properties
git commit -m "chore: bump mod version to 0.4.0 (sodium-style video settings)"
```

---

## Milestone Done When

- `./gradlew test` passes all unit tests (PageModel plus all prior).
- `./gradlew build` and `./build-all.sh` succeed on both 26.1.2 and 26.2.
- Manual check (maintainer): clicking "Video Settings" opens the ABTO Sodium-style screen; the left tabs switch pages; the VIDEO page shows working vanilla options (render distance, brightness, fullscreen, etc.); the PRESETS page applies presets and toggles shaders/apply-to-mods; hovering a row shows its description in the bottom panel; Done saves and returns; no crash at GUI scale 4; Mod Menu opens the same screen.

## Notes for Later Milestones

- The original "sky/render mixins" milestone (hide clouds/stars/sun-moon/sky/fog, occlusion and behind-camera culling, dynamic FPS, entity culling) becomes the NEXT milestone (M5). Those land as new PresetSettings/FeatureToggles axes AND as extra rows on the TWEAKS page of this screen - the framework built here is their home.
- Sodium config adapters (Fabulously Optimized parity) follow as M6.
- The mixin infrastructure added here is reused by M5's render mixins.
- GPU-name detection (GL_RENDERER) is still pending a render-thread hook; the wizard shows GPU as unknown until then.
