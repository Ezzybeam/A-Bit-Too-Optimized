# A Bit Too Optimized - Design Spec

Date: 2026-06-29
Status: Draft for review

## Summary

A Bit Too Optimized (ABTO) is a single Fabric performance mod for Minecraft Java
Edition. It is a "tune-up" mod: it does not replace existing performance mods like
Sodium or Lithium, it cooperates with them when present and still helps when they
are absent. The mod centers on a preset system (Ultra, High, Normal, Low, Very Low,
Potato, plus Custom), a friendly first-run setup wizard, hardware detection with
manual override, a small set of its own runtime optimizations, and a clean in-game
GUI. It stays compatible with waypoint mods, Bobby, Voxy, fullbright mods and
resource packs, and shaders (Iris).

Note on style: no em dashes and no emojis anywhere in the mod (GUI text, config
file comments, log messages). Plain ASCII text only.

## Goals

- One mod jar per supported Minecraft version line, built from one codebase.
- Get as much FPS as reasonably possible through smart configuration plus a few
  original runtime optimizations.
- Work on macOS and Windows.
- Work with or without other performance mods installed (soft dependencies).
- Stay compatible with waypoint mods, Bobby, Voxy, fullbright, and shaders (Iris).
- Be approachable for non-technical users through presets and a setup wizard.

## Non-goals

- Not a modpack. This spec covers only the mod. A modpack could be made later.
- Not a rendering-engine rewrite. We do not duplicate or replace Sodium.
- Not designed to run inside Fabulously Optimized. Optional FO compatibility may be
  considered later but is out of scope now.
- The mod cannot change JVM memory (-Xmx) or garbage-collector flags at runtime.
  Those are set by the launcher before the game starts. The mod can only recommend
  them to the user.

## Supported versions

The mod targets three Minecraft version lines, built from one codebase:

- 1.21.11 (last release of the old 1.21.x numbering, "Mounts of Mayhem")
- 26.1.x (the user's main version, tested on 26.1.2)
- 26.2.x ("Chaos Cubed")

Minecraft adopted a year.drop.hotfix numbering scheme in 2026: the first number is
the year, the second is the content drop, the third is hotfixes. Each jar declares a
version range covering the hotfixes within its line (for example >=26.1 <26.2), so we
do not need a separate jar per hotfix.

Mod loader: Fabric (with Fabric API as a dependency).

## Technical requirements

- Client-side only. This is a client performance mod. Its fabric.mod.json declares
  the client environment, so it is not required on servers and does not affect
  server-side gameplay. It must not break joining vanilla or modded servers.
- Java toolchain per version line. Minecraft 26.1+ requires Java 25, so the 26.x
  builds use a Java 25 toolchain. (1.21.11, when added later, uses Java 21.) This
  spec originally assumed Java 21 throughout; corrected after the 26.x toolchain
  was confirmed. See the foundation plan for details.
- Mod id: abto (short, lowercase, no conflicts expected). Display name: A Bit Too
  Optimized.
- Distribution: published to Modrinth, one file per version line. macOS and Windows
  are the tested platforms (Linux should work but is not a tested target).
- License: to be chosen before first publish (see open questions).

## Build and multi-version strategy

- Gradle with the Fabric Loom plugin.
- Stonecutter (Gradle multi-version preprocessor) to build one source tree into the
  three version-specific jars. Version-specific code is guarded with Stonecutter
  comment directives so each branch compiles against its own Minecraft mappings.
- Most of the mod (config, presets, GUI, hardware detection, soft-dependency
  detection) is version-agnostic plain Java and needs little or no per-version code.
  The version-sensitive parts are the mixins that touch Minecraft internals
  (rendering hooks, options access), which are isolated so per-version differences
  stay small.

## Architecture overview

The mod is organized into focused units, each with one clear purpose:

1. Hardware Probe
   - Detects: allocated RAM (Runtime.maxMemory), CPU core count
     (Runtime.availableProcessors), total system RAM, and GPU name.
   - Total system RAM and GPU name are best-effort. Total RAM is read via the JVM
     OperatingSystemMXBean (or OSHI if needed). GPU name comes from the OpenGL
     GL_RENDERER string, read once the render context exists.
   - Output: a HardwareInfo record of detected values, each marked detected or
     unknown.

2. Hardware Override Store
   - Holds user corrections for any hardware value (allocated RAM display, total
     system RAM, CPU cores, GPU name).
   - Rule: the effective value is the user override if set, otherwise the detected
     value. This "detected unless corrected" rule feeds the recommendation engine.
   - Persisted in the config file.

3. Recommendation Engine
   - Input: effective hardware values (detected plus overrides).
   - Output: a recommended preset (Ultra to Potato) using simple thresholds on RAM,
     CPU cores, and a coarse GPU tier guess. Thresholds are documented constants so
     they are easy to tune.

4. Mod Presence Detector
   - Uses FabricLoader.isModLoaded(id) to check for known mods: sodium, lithium,
     iris, ferritecore, immediatelyfast, modernfix, entityculling, dynamic-fps,
     bobby, voxy, and common waypoint mods (xaeros minimap/worldmap, journeymap).
   - Output: a set of present mod ids used by the preset engine and the wizard.

5. Preset Engine (the heart)
   - A Preset is a named bundle of target settings spanning two layers:
     a. Vanilla video options (render distance, graphics mode, clouds, particles,
        entity render distance, smooth lighting, vsync, FPS limit, and similar).
     b. Detected mods' configs: when a known performance mod is present and the user
        opted in, the engine writes that mod's config to match the preset (for
        example Sodium options). Each supported mod has a small adapter that knows
        how to read and write that mod's config safely.
   - Applying a preset sets vanilla options through the game's options API and, for
     opted-in detected mods, writes their config and requests a reload where the mod
     supports it.
   - Shader awareness: each preset has a shader-aware variant. When the user says
     they use shaders (Iris present), the engine avoids settings that fight a shader
     pack and never overrides Iris's own settings.
   - Custom: when the user chooses Custom, no preset bundle is applied; the user
     controls individual settings through the config screen.

6. Runtime Optimizations (the mod's own code, via mixins)
   - Dynamic FPS: lower the frame rate when the window is unfocused or minimized, to
     save GPU and battery. Configurable thresholds, can be disabled.
   - Entity and particle culling: skip rendering entities and particles that are not
     visible. Culling must respect shader shadow passes when Iris is active, so it
     does not cull things that should appear in shadows.
   - All runtime optimizations are individually toggleable and are written so they do
     not conflict with Sodium, Iris, Bobby, or Voxy. Where another mod already does
     the same job better, ABTO defers (for example, if EntityCulling is present, ABTO
     can disable its own culling to avoid duplication).

7. Config Store
   - One config file (JSON) holding: chosen preset, custom settings, shader flag,
     "apply to other mods" choice, hardware overrides, per-feature toggles, and a
     "wizard completed" flag.
   - Loaded at startup, saved on change. Sensible defaults so a missing or partial
     file still works.

8. GUI layer
   - First-run wizard screen: shown before the main menu the first time the wizard
     flag is unset.
   - Config screen: opened from Mod Menu, with a button to re-run the wizard.
   - Purpose-built screens (preset cards with short descriptions, a clear stepped
     wizard, a tidy settings list and hardware section), not a generic auto-generated
     list. Built on the game's Screen API.

   GUI redesign decision (after Milestone 3): the config UI is rebuilt in a
   Sodium-style layout - vertical tab pages on the left, option rows (label on the
   left, control on the right) in the main area, a description panel at the bottom
   that updates as the user hovers a row, and Done/Apply controls. Short preset
   names with the description shown on hover, not baked into the button label.
   Full Sodium parity: ABTO's screen REPLACES the vanilla Video Settings screen
   (the "Video Settings" button is redirected to it via a mixin, as Reese's Sodium
   Options does) and contains every vanilla video option as rows, so nothing is
   lost, plus ABTO's presets and tweaks. Vanilla options are reused via their
   existing OptionInstance widgets rather than reimplemented, so they stay correct
   and adapt across versions. This redesign introduces the project's first mixin
   (button redirect) and is the home into which later sky/culling toggles slot as
   additional rows.

   As implemented (Milestone 4): rather than the hand-drawn left-tabs + bottom-panel
   layout described above, the screen extends vanilla OptionsSubScreen and uses the
   native scrolling OptionsList with section headers ("A Bit Too Optimized" and
   "Video"); descriptions show as vanilla hover tooltips. This is Sodium-STYLE
   (native settings feel, reused controls, presets on top, full parity) and reuses
   the native list's safe clipping (avoiding the scissor crash a custom scroll layout
   risked). The exact left-tabs + persistent bottom description panel is deferred as
   optional polish.

## First-run setup wizard flow

Shown once, before the title screen, when the wizard-completed flag is false:

1. Hardware summary (shown, not asked): allocated RAM, total system RAM, CPU cores,
   GPU name, each editable inline so the user can correct a wrong value.
2. Recommended preset: highlighted based on effective hardware values.
3. Pick a preset: Ultra, High, Normal, Low, Very Low, Potato, or Custom.
4. Shaders question: do you use shaders (yes or no). Adjusts the preset to be
   shader-aware. If Iris is not installed, this can be skipped or noted.
5. Apply to other installed mods: only shown if known performance mods are detected.
   Choose to tune their configs to match, or leave them alone.
6. RAM advice: shown only if allocated RAM looks low for the chosen preset. Displays
   the recommended launcher memory setting and how to change it. The mod cannot do
   this itself.
7. Finish: choices saved, wizard flag set true. Custom sends the user to the full
   config screen instead of applying a preset.

The config screen always has a "Run setup again" button that re-opens the wizard.

## Presets

Six presets plus Custom. Each is a coherent strategy, not just a slider position.

- Ultra: for high-end PCs. Maximum view distance and full visual effects, with only
  the safe culling and dynamic-FPS optimizations on top.
- High: for good gaming PCs. High view distance, most effects on.
- Normal: balanced default for average PCs.
- Low: for older or weaker PCs. Reduced effects and view distance.
- Very Low: for weak PCs. Aggressive cuts to effects and distance.
- Potato: for 6 to 10 year old laptops and PCs. Target 30 to 60 FPS at 8 to 12
  render distance, assuming 4 to 8 GB RAM. The most aggressive preset.
- Custom: user-controlled, no bundle applied.

Exact per-setting values for each preset are defined during implementation and kept
in one data table so they are easy to read and adjust.

## Compatibility

- Soft dependencies only. The mod checks who else is loaded and adapts. Nothing in
  the supported-mod list is required.
- Waypoint mods (Xaero's, JourneyMap): no rendering conflicts; ABTO does not touch
  their overlays.
- Bobby (far chunk caching) and Voxy (LOD rendering): ABTO must not cull or disable
  chunk content these mods provide, and presets must not force a render distance that
  defeats them.
- Fullbright mods and resource packs: ABTO does not override gamma or brightness
  beyond what a preset explicitly includes, and never fights a fullbright resource
  pack.
- Shaders (Iris): ABTO never overrides Iris settings, culling respects shadow
  passes, and presets have shader-aware variants.
- Resource and texture packs (including HD/PBR packs): ABTO never overrides pack
  choices and its settings must not break pack rendering.
- Essential: ABTO must coexist with Essential. Note: Sodium itself sometimes
  conflicts with Essential; that is a Sodium/Essential issue ABTO cannot fix, but
  ABTO must not add to it, and where ABTO provides its own optimization (without
  Sodium) it gives an Essential-compatible path.
- Cosmetic and overlay mods that hook rendering must keep working: Hypixel+
  (Hypixel Mod / "Hypixel Plus"), MinecraftCapes and other cape mods, and
  Litematica (schematic rendering). ABTO's culling and render tweaks must not hide
  capes, schematic overlays, or HUD elements these mods draw.
- Not built to run inside Fabulously Optimized.

## Performance approach and FPS goal

- Stretch goal: help the user reach about 1000 FPS looking at the open sky on
  capable hardware (baseline measured at 600 to 700 FPS uncapped). This is a target
  to chase, not a guarantee; achievable FPS depends on the machine.
- The biggest sky-FPS levers, in order, and where they live:
  1. Render fewer sky elements: clouds, stars, sun and moon, sky gradient, and fog.
     Vanilla exposes only clouds and (indirectly) fog; the rest need ABTO's own
     render mixins (Milestone 4) or Sodium Extra when present (Milestone 5).
  2. Sodium when installed: ABTO writes Sodium's config to a high-performance
     profile that matches the chosen preset (Milestone 5 adapter).
  3. Lower-overhead vanilla settings the preset already covers (render distance,
     graphics mode, particles, entity distance) - Milestone 2.
- Config parity with Fabulously Optimized "and more": FO's performance is mostly
  Sodium plus Lithium with tuned configs (Sodium Extra, Reese's Sodium Options,
  Lithium). ABTO's preset engine targets the same knobs FO tunes (via the mod-config
  adapters in Milestone 5) and adds its own sky/render toggles (Milestone 4) so it
  helps even when those mods are absent. Parity is an ongoing effort tracked per
  adapter, not a single task.
- New preset axes for sky and render reduction (added to the preset model as the
  Milestone 4 render features land): hideClouds, hideStars, hideSunMoon, hideSky,
  reduceFog, plus a "max FPS" framerate-limit axis (default unlimited). Each is an
  ABTO-owned toggle implemented by our mixins, independent of other mods.

## Culling settings (Milestone 4 feature toggles)

Two additional Milestone 4 toggles, each defers to a better existing mod when one
is present (no double work, no conflict):

- Occlusion culling (do-not-render-behind-walls): skip rendering blocks/block
  entities and entities fully hidden behind solid geometry. Honest scope: Sodium
  already does chunk occlusion culling, and EntityCulling does entity occlusion;
  when either is present ABTO disables its own version for that category. ABTO's
  value is covering this when those mods are absent. Must respect shader shadow
  passes (do not cull things that should cast shadows under Iris).
- Behind-camera culling (frustum-based): do not render, and optionally do not keep
  loaded, content outside the view; load/show on demand when the camera turns to
  face it. Honest scope: vanilla already frustum-culls RENDERING every frame, so
  the rendering win is small; the optional not-loading part is opt-in and OFF by
  default because unloading then reloading as the camera turns can cause stutter
  and pop-in. Provided as an aggressive option for players who prioritize raw FPS
  over smoothness, with a clear warning in the config.

Both are individual toggles in FeatureToggles, off-by-default where they risk
visual artifacts, and never cull cape/overlay/schematic rendering from the COMPAT
mods (Hypixel+, MinecraftCapes, Litematica).

## Rendering config catalog (the "control anything" goal)

Goal: the deepest rendering config practical - all of Fabulously Optimized's
toggles and more. FO's toggles come from many separate mods; ABTO implements them
as its own mixins so they work without those mods. This is an ongoing catalog built
in batches on a shared, extensible pattern: each entry is one FeatureToggles field +
one render mixin (or a vanilla option), surfaced as a row in a "Performance" section
of the options screen and available as a preset axis. Every entry is individually
toggleable, off-by-default where it risks a visual artifact, respects Iris shadow
passes, and never hides COMPAT-mod rendering (capes, Hypixel+ overlays, Litematica
schematics). Where a better dedicated mod is present (EntityCulling, etc.), the
matching ABTO entry defers to it.

Catalog (grouped; the milestone builds the highest-FPS batch first, the rest follow):
- Sky/atmosphere: hideClouds, hideStars, hideSunMoon, hideSky (gradient/void),
  disableFog (or fog-distance reduction).
- Particles: allParticlesOff (a hard off beyond vanilla Minimal), plus per-category
  disables for heavy ones (block-break, splash/rain, etc.).
- Block/texture animations: disableBlockAnimations (animated textures: water, lava,
  fire, portal, sea lantern, magma, and similar) - a real FPS win.
- Weather: disableWeatherRendering (rain/snow), disableWeatherParticles.
- Entities: entityCulling (off-screen/occluded), disableEntityShadows,
  disableGlintAnimation (item/armor enchant glint).
- Render behavior: behindCameraCulling, dynamicFps (lower FPS when unfocused or
  minimized), maxFps (framerate limit).

Custom preset behavior (fix): selecting a preset applies its bundle; when the user
then hand-edits any option, the selected preset auto-switches to Custom so the UI
reflects that current settings no longer match a named preset. Custom itself applies
nothing (keeps the user's settings).

## Companion mods (separate future projects)

These are separate mods, not part of the main ABTO mod's milestones. Each gets its
own spec, plan, and milestone cycle if and when we build it.

- Chunk cache (working name): caches a server's chunks to disk so rejoining loads
  the world fast, and to load singleplayer worlds faster. Stores blocks only, not
  entities. Load order on a server: first visit caches chunks as the server sends
  them; later visits load cached chunks immediately, then entities, then the chunks
  the server newly sends, then cache those new chunks. Honest scope: this overlaps
  almost exactly with the existing Bobby mod (per-server chunk caching, instant
  reload, view beyond render distance). Decision deferred: build our own variant
  only if it offers something Bobby does not; otherwise ABTO simply stays
  compatible with Bobby (already a goal) and recommends it. The main ABTO mod must
  not duplicate or fight a chunk cache it detects (Bobby or our own).

## Release archiving

- Every published build is archived. The build process keeps a versioned copy of
  each version line's jar (named a-bit-too-optimized-<minecraft_version>-<abto_version>.jar)
  under an archive directory, so older mod versions remain available and are not
  overwritten by a new build. Archiving is gitignored build output, not committed
  source.

## Error handling

- Hardware detection failures degrade gracefully: an unknown value is shown as
  unknown and the user can fill it in. The recommendation engine treats unknown as
  conservative (leans toward a safer, lower preset).
- Config file missing or corrupt: fall back to defaults, back up the bad file, log a
  plain message, and continue.
- A mod-config adapter that fails (for example a detected mod changed its config
  format) is caught per adapter, logged, and skipped, so one bad adapter never blocks
  applying the rest of a preset.
- Applying a preset is best-effort and reports which parts succeeded.

## Testing

- Unit-testable units: Recommendation Engine (hardware values to preset), Override
  Store rule (detected unless corrected), Config Store load and save with defaults
  and corrupt input, and the preset data table (every preset defines every setting it
  claims to).
- Mod Presence Detector and adapters: tested with simulated present and absent mods.
- Manual in-game testing per version line (1.21.11, 26.1.2, 26.2) on macOS and
  Windows: wizard appears once, presets apply, GUI looks right, and no crashes with
  Sodium, Iris, Bobby, Voxy, and a waypoint mod installed together.

## Risks

- Toolchain availability for new versions. Stonecutter, Fabric Loom, Fabric API, and
  usable mappings must all be available for 26.1.x and 26.2.x. These are very recent
  versions, so a first task is confirming the toolchain works for each branch before
  building features. If a branch is not yet supported, start with the branches that
  are (26.1.x is the main target) and add the rest when their toolchain lands.
- Mod-config adapter drift. Other mods can change their config formats between
  versions. Adapters are isolated and fail safely (logged and skipped) so this never
  blocks the rest of a preset.
- Mixin conflicts. The mod's own optimizations (culling, dynamic FPS) are the most
  conflict-prone part. They are individually toggleable and defer to better mods when
  those are present.

## Open questions for implementation

- Exact per-setting values for each preset.
- Whether to use a config UI library for the detailed-settings list or build it fully
  custom (the wizard and preset picker are custom regardless).
- Final recommendation-engine thresholds for GPU tiers.
- License: LGPL-3.0-or-later (decided). First public release is 1.0.0 (feature-complete); no early beta.
