# PAUSED checkpoint — 28 October 2026

**The latest user instruction is to STOP development and ZIP the project with instructions for browser Codex.** Only packaging/handoff work is authorised until the user resumes. Read **BROWSER_CODEX_HANDOFF.md** in this directory for current facts; it supersedes the historical handoff below.

Current state: seven experimental kits / 57 moves, 28 assignments / 108 stages; optional softer player mesh and custom item hotbar. **80 JUnit cases pass. The latest engine run completed 15 required cases: 14 passed and one Blueflame hit assertion failed.** See the handoff and `verification-current/seven-kits-engine.log` in the archive. Player schema 4 / network protocol 5. Internal mod version remains 0.1.0-dev. A blue-flame GIF is included as an asset preview, not Minecraft footage. No GUI/client control, project sharing, remote access or automation is running. The user chose to keep this chat solo.

## Historical handoff before the latest continuation

## Current foundation

Minecraft 1.21.1, NeoForge 21.1.217, Java 21. The exact Photon/LDLib2/KilaGraph, Player Animator, Bendy Lib and GeckoLib 4 stack remains pinned. Use the local JDK at workspace `work/tools/jdk-21.0.12.1+1` for Gradle; system Java is 17. Scripts and tests launch no GUI. A fresh disposable `-PmhaTestDir=run/<unique-name>` avoids contamination from authored-map construction.

The last root-level deliverables still represent the earlier **0.1.0-dev** checkpoint with 48 JUnit/6 Minecraft tests. The live source has progressed substantially and has not yet been repackaged. The current Gradle/mod version remains 0.1.0-dev temporarily; bump to a new development version before the next package. Packaging scripts/log selection need updating for the new verification evidence. Never overwrite a claimed final release with unfinished content.

## Implemented since that package

- 28 data-driven experimental encounter assignments / 108 objective stages: ten school/hero, ten villain, eight independent. Original compressed modular 48×48 exercise cells, reserved per player/mission by the atomic journal. Bounded preflight construction; solids before contained water. This is not the complete named world or final campaign presentation.
- Real server movement markers, first aid, collision-aware escort path search, timed shelter pressure, judgement stations, combat and interrupted eight-second shutdown alternatives. Checkpoints and unique rewards persist. Provisional examination grants licence 1, advanced qualification grants the mod's licence 2. A copied credential never grants rights. `/mha missions` and `/mha licence` exist; the notebook Journal opens the board.
- An original 20-cube/7-bone GeckoLib training sentinel with native editable geometry, palette texture, idle/walk/windup sources and server-controlled charging state. Rendering remains unobserved.
- Windows atomic journal rename has a bounded retry for brief AccessDenied locks, with no non-atomic fallback. Do not inspect the live transaction file from a separate process while stress tests are writing it; a Windows reader can deny replacement. Encounter save errors pause pending outcomes rather than award them.
- Four experimental kits / 32 moves in shared data: Explosion plus Half-Cold Half-Hot, Hardening and Electrification. New design sheet and official profile sources are preserved. Distinct temperature/fatigue/charge/overload rules, hardening/counter windows, timed charge/grounding, capped clear-sight electrical chaining, temporary collision-checked ice walls/stairs with no inventory drops/piston movement and durable cleanup leases.
- Original 24 additional SVG/PNG icons, 24 timed player animations, three creative tokens and temporary block models. `tools/make_foundation_kits.py`, `make_kit_assets.py`, `make_sentinel.py` preserve source. Always use UTF-8 when reading the generator containing Japanese labels. JSON kit colors are signed Java ARGB ints.
- Per-kit mission-practice mastery is first-claim, separate from single-claim general credits. Field objective milestones gate new techniques; Explosion keeps its existing three bespoke technique circuits. The default server starting pool remains Explosion; operators can enable the other experimental IDs. None is advertised as polished.
- Searchable offline quirk index, generic source pages and technique browsing; dynamic resource HUD and kit palette. Visual delivery now follows negotiated tracking observers, with catch-up animation age for late observers. Photon decoration has distance/lifetime/global caps; it remains programmatic, without a validated exported editor project.

## Verification state

The original starter/all-eight-Explosion route plus all 28 encounter assignments passed the engine with seven required Minecraft tests before the kit expansion. Rule suite subsequently reached **68 tests passing** with four-kit ownership, resources, practice and migration checks. Four focused engine tests were added for hardening, ice collision/restoration, chaining and charge/grounding.

The vanilla `makeMockServerPlayerInLevel` overrides `isCreative()` to true, so fixtures were replaced by `TestPlayers.survival`, a connected embedded ordinary ServerPlayer with survival game mode. This is still not an authenticated client or manual pacing test. Guard testing waits out normal spawn invulnerability. The full connected campaign also runs with this survival fixture now.

The latest in-flight verification uses `run/gametest-four-kits-survival-three` and workspace `work/four-kits-engine.log`. It is testing eleven required engine cases. The previous run passed the real hardening and charge tests and the connected route, but chain and occupancy assertions ran before freshly spawned test entities became accessible. Those two fixture setups now explicitly wait for entity storage visibility. Inspect the latest log for explicit **All 11 required tests passed** before claiming a pass. Do not equate Gradle success with nonempty test selection.

Player schema and network protocol are now **4**. v1→v2→v3→v4 migrations preserve prior durable progress; new resource/stance state is initialised during migration. Unknown future versions still reject. The outer journal version remains 1 with optional durable arena/construct maps, preserving existing reads.

## Immediate next work

1. Finish/repair the latest survival-mode engine test. Check actual XML counts and explicit engine pass text.
2. Add a survival-HUD exclusion region to layout geometry: the current bottom-left move panel can overlap vanilla hotbar/health at small GUI sizes. Preserve editor draft/cancel behavior and use meaningful bounds/parity tests.
3. Finish new-kit presentation/acquisition details, cleanup on death/dimension/assignment, scoped persistence failure handling, explanatory resource values and current-kit journal instructions. Check all four kits' acquisition paths and ordinary rerolls, not only synthetic IDs.
4. Update docs/content matrices, migration notes, artifact version and packaging evidence after verification. Current README/KNOWN_LIMITATIONS/TEST_RESULTS mostly describe the old checkpoint; CAMPAIGN_IMPLEMENTATION and this file contain newer facts. Keep unobserved checks explicit.
5. Continue the full master roadmap: specialisations and mastery 65–100, full HUD/hotbar/accessibility, remaining foundation/advanced/movie/original roster, full U.A. and named regions, actual regional campaigns/parties/headquarters, equipment/audio/VFX pipelines, lore review, authenticated multiplayer and measured performance.

Full M0/M1 visual gates remain pending: standard/slim, first/third-person, observers, poses, equipment, final menus/loading, actual Photon danger correspondence, exported Photon projects and perceptual audio. No final client launch or desktop control occurred during this continuation. These limitations never authorize marking the complete mod finished.
