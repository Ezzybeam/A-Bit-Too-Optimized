# Changelog

All notable changes to A Bit Too Optimized.

## 0.9.0 - First public alpha

First feature-complete alpha. Client-side, works with or without Sodium.

### Automatic FPS boost (no configuration needed)
- Entity occlusion culling: skips drawing entities fully hidden behind solid
  blocks. On by default.
- Leaves culling: skips the hidden interior faces between leaf blocks. On by
  default.
- Both are safe (no visible change) and defer to Sodium when Sodium is
  installed, so ABTO stacks with Sodium instead of fighting it.

### Deep render configuration
- Per-type particles: disable any of the game's registered particle types
  individually, plus master, block, and rain-splash groups.
- Per-type animations: freeze any animated texture individually (water, lava,
  fire, portal, and every other animated sprite), plus a master toggle.
- Sky and fog: hide clouds, stars, sun and moon, the sky, and disable fog.
- Weather: disable rain and snow, and weather particles.
- Entities: hide item frames, armor stands, paintings, and name tags (all, or
  players only, or mobs only).
- Blocks: hide beacon beams, moving pistons, the enchanting table book, and
  sign text.
- Colors: flatten biome colors to a fixed shade.
- HUD overlay: show FPS, coordinates, and facing direction.
- Fun: recolor grass, foliage, and water.

### Interface
- Two settings styles, switchable live: a Sodium-style tabbed screen and a
  Minecraft-style list. Both replace the vanilla Video Settings and keep full
  access to all vanilla video options.
- First-run setup wizard, hardware detection, and quality presets (Ultra
  through Potato).

### Compatibility
- Multi-version: Minecraft 26.1.x and 26.2.
- Soft dependencies only; compatible with Sodium, shaders (Iris), and other
  common client mods.
