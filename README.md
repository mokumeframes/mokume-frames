# MHA: Rising Heroes

> **Current source checkpoint (28 October 2026): PAUSED.** Read [the browser Codex handoff](docs/BROWSER_CODEX_HANDOFF.md) first. Source now has seven experimental kits / 57 moves and 28 assignments. All 80 JUnit cases pass; the latest Minecraft run passed 14 of 15 required tests and has one unresolved Blueflame assertion. The softer player mesh and final client presentation are unobserved in Minecraft. Version metadata and older release notes below still describe historical `0.1.0-dev` packages. This is not a final or fully verified release.

Minecraft Java **1.21.1** · NeoForge **21.1.217** · Java **21** · **0.1.0-dev**

An experimental fan-mod development snapshot. It contains a buildable foundation, a partial Explosion loop and a starter academy/city region. **This is not the complete mod described in the master brief, and M0/M1 acceptance is not complete.**

## Start here

- [Installation and playable starter route](docs/INSTALLATION.md)
- [Verification results and exact limits](docs/TEST_RESULTS.md)
- [Known limitations / unfinished requirements](docs/KNOWN_LIMITATIONS.md)
- [Dependency, license and side audit](docs/DEPENDENCIES.md)
- [Configuration and administrator commands](docs/CONFIGURATION.md)
- [Milestone plan](docs/PLAN.md), [full original brief](docs/MASTER_BRIEF.txt)
- [Quirk/source status matrix](docs/CONTENT_MATRIX.csv), [region backlog](docs/REGION_BACKLOG.csv)
- [Explosion design sheet](docs/EXPLOSION_DESIGN.md), [style guide](docs/STYLE_GUIDE.md)
- [Editable asset pipeline](art/README.md)
- [Interactive interface design preview](design/menu-preview.html) — a separate design reference, not a Minecraft screenshot

## Implemented development scope

The server owns first assignment, accepted casts, damage/targeting, stamina and blast strain, ability cooldowns, six-slot loadouts with three saved presets per quirk, early unlocks, profile persistence, repeat-safe rewards and reroll transactions. Preset loading validates current unlocks and leaves resources/cooldowns intact. Player records use an atomic journal with a SavedData mirror. Creative assignment is separated from max-mastery/all-moves/resource testing commands and destructive resets.

Explosion has eight coded actions: Palm Burst, Blast Dash, AP Shot, Recoil Dodge, Stun Grenade, Burst Volley, Aerial Redirection and Howitzer Impact. All eight have server-tested acquisition through the starter route and three optional technique trials; final presentation checks remain pending. Only Explosion is registered. With no real alternative, a reroll refuses without spending anything.

The authored 144×104 starter region contains academic wings, a central entrance, a workshop, infirmary, dorm module, training targets/course and a nearby city clinic. Enrolment, a control lesson with a support alternative, a short ground course and first aid provide early progress. This route does not award a hero licence. Full school, villain and independent campaigns remain planned.

Client code includes a themed title menu, resource-reload overlay using actual reload progress, academy-art pause menu, notebook with technique search and effective server values, a schematic regional atlas, settings, remappable inputs and a compact move/stamina/strain HUD. The HUD editor supports dragging or Alt+arrow placement, scale, opacity, compact mode, numeric resources, an objective toggle and reset/save/cancel. Vanilla survival displays remain visible until a complete replacement passes parity checks. Six original synthesised cues cover startup tells, attacks, recoil and menu selection, with subtitles and retained WAV/source files; final perceptual mix remains unverified.

Photon 2.2.9, LDLib2 2.2.44, Player Animator 2.0.4+1.21.1, Bendy Lib 5.1 and GeckoLib 4.9.3 are pinned. Photon includes KilaGraph 21.1.0.15. No replacement animation library was substituted. Third-party mod binaries are not bundled.

## Background build and checks

With a Java 21 JDK:

```powershell
./tools/build.ps1 -JavaHome 'C:/path/to/jdk-21' -ServerTests
```

Or use `gradlew build` and `gradlew runGameTestServer -PmhaTestDir=run/your-new-test-directory`. The Gradle wrapper, dependency locks, SHA-256 verification metadata, runtime assets, native model source and source generators are included. `tools/fetch-dependencies.ps1` fetches the exact library files from author repositories using `dependency-manifest.json`.

No GUI or desktop automation runs from the build/test scripts. The user's preference is to keep development in the background; see AGENTS.md. The base client was launched earlier with the requested stack. Later custom presentation and the armour/VFX integration matrix remain unobserved in-game.

## Attribution and release status

Original code/art sources in this development snapshot have no separately selected public release license; mod metadata remains All Rights Reserved. The upstream MDK scaffolding's MIT notice is preserved in TEMPLATE_LICENSE.txt. Dependencies retain their own licenses and distribution conditions. MHA names and setting belong to their respective rights holders; this is an unofficial fan project. No official art, audio, models, voice recordings or third-party world downloads are included.

The academy menu painting was generated with the built-in image tool; its exact prompt and provenance are preserved in art/menu/PROVENANCE.md. Blockbench-compatible geometry and original SVG icons were authored programmatically. No unperformed Blockbench, Blender, multiplayer or visual test is claimed.
