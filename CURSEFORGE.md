# A Bit Too Optimized

A client-side Minecraft performance mod that boosts your FPS the moment you
install it, then hands you the deepest render configuration around if you want
to go further. Works on its own, and stacks cleanly with Sodium.

## Boost with zero configuration

Install it and get more frames right away, no settings to touch:

- Entity culling: stops drawing entities fully hidden behind blocks.
- Leaves culling: skips the hidden interior faces of leaf blocks.

Both are on by default, cause no visible change, and defer to Sodium when
Sodium is installed, so you can run both together for even more.

## Configure anything

Open Video Settings and you get a Sodium-style tabbed screen (or switch to a
Minecraft-style list, your choice), both with full access to every vanilla
video option plus:

- Per-type particles: disable any particle in the game individually.
- Per-type animations: freeze any animated texture individually.
- Sky and fog: hide clouds, stars, sun and moon, the sky, disable fog.
- Weather: turn off rain, snow, and weather particles.
- Entities: hide item frames, armor stands, paintings, and name tags (all,
  players only, or mobs only).
- Blocks: hide beacon beams, moving pistons, the enchanting table book, and
  sign text.
- Colors: flatten biome colors.
- HUD: an on-screen FPS, coordinates, and facing readout.
- Fun: recolor grass, foliage, and water.

Plus quality presets from Ultra down to Potato and a first-run setup wizard.

## Compatibility

- Minecraft 26.1.x and 26.2, Fabric.
- Requires Fabric API.
- Soft dependencies only. Compatible with Sodium, Iris shaders, and other
  common client mods.

This is an alpha. Please report anything odd on the issue tracker.

## Development note

This mod was built with AI assistance (Claude) under the author's direction.
All features were chosen by the author, and every build was tested in-game and
verified before release.
