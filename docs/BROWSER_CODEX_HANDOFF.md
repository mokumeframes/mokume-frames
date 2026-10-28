# Browser Codex handoff — MHA: Rising Heroes

**28 October 2026: development is paused at the user's request. This is an unfinished source checkpoint, not a final release.**

## Read first

1. `AGENTS.md`: no desktop/mouse/keyboard control or GUI Minecraft launch without explicit new permission.
2. This file: the current source state and exact known failure.
3. `docs/MASTER_BRIEF.txt`: the full original specification. Preserve the whole roadmap.
4. `docs/ADDITIONAL_KIT_DESIGNS.md`, `docs/FOUNDATION_KIT_DESIGNS.md`, `docs/DEPENDENCIES.md`.
5. The full archive's `verification-current/` evidence. Existing `verification/`, lower README sections, old `TEST_RESULTS.md`/`KNOWN_LIMITATIONS.md`, and content matrices are historical 0.1 checkpoint records; several counts and implementation statements are stale. This handoff supersedes them.

The archive-root `START_HERE.md` has extraction, build and cloud-environment instructions. This handoff stays with the project when importing only its folder into a repository.

## User intent and constraints

- Build the complete ambitious MHA Minecraft mod through buildable milestones. The full mod is not finished.
- Include polished loading screens, menus and compact HUD.
- Add unique powers including **Umbrakinesis**; this continuation also designed **Kinetic Reservoir**. Both are explicitly **mod-original / non-canon**, without invented official wielders.
- Add less blocky, more natural player models. An optional rounded skin surface is implemented experimentally; it is not photorealistic or visually certified.
- Show a blue-flame animation like Dabi's. The included GIF is a rendered asset study, **not Minecraft footage**. Blueflame gameplay was added experimentally and has one failing engine assertion.
- Work using source, shell and background/headless tests. **No desktop/input takeover or foreground Minecraft unless the user explicitly requests it.** This remains binding across handoffs.
- The user considered sharing, then said to keep this chat solo. No project move, friend invite, public link, remote access or GitHub push occurred.
- **Latest instruction: stop development and ZIP the project with instructions for browser Codex. Resume only on a later user request.**
- No subagents, goals or recurring automations were created.

## Exact stack

Minecraft 1.21.1, NeoForge 21.1.217, Java 21, ModDevGradle 2.0.148, Gradle wrapper 9.2.1. Preserve Photon, Player Animator, Bendy Lib and GeckoLib **4**. Lockfile, dependency SHA-256 metadata, artifact manifest, wrapper and source are included.

```text
com.lowdragmc.ldlib2:ldlib2-neoforge-1.21.1:2.2.44:all
com.lowdragmc.photon:photon-neoforge-1.21.1:2.2.9:all
dev.kosmx.player-anim:player-animation-lib-forge:2.0.4+1.21.1
io.github.kosmx.bendy-lib:bendy-lib-forge:5.1
maven.modrinth:geckolib:Grwn5rUB  (GeckoLib 4.9.3)
```

Photon nests KilaGraph 21.1.0.15; don't add a duplicate. Third-party mod JARs are not bundled. Read the licensing audit before distribution. Don't substitute PAL or another animation stack without a demonstrated blocker and user agreement.

The source machine used a local Temurin 21 JDK outside the project. It is intentionally not bundled. Set the new environment's own `JAVA_HOME`; never reuse absolute Windows paths. Java 17 is insufficient. Normal Gradle builds use committed assets and do not require Python or Node.

## Current verification and exact failure

- Latest Java compilation, assembly and all **80 JUnit tests succeeded**, with zero failures/errors/skips. XML is in archive-root `verification-current/junit/`.
- The latest combined command was `gradlew build runGameTestServer -PmhaTestDir=run/gametest-seven-kits-one --offline`. Assembly/checks succeeded before GameTest execution.
- **15 required Minecraft engine cases completed: 14 passed, one failed.** The combined Gradle command correctly exited nonzero.
- Failure: `KitMechanicsTests.blueflameWaitsForWindupAndCoolingUsesTheActiveWindow`.
- Exact message: **`The active stream did not apply its server-owned hit`**.
- Raw evidence: `verification-current/seven-kits-engine.log`, including the final 15-test/1-failure summary.
- The fixture demands Husk health `<=97` after a listed 3-damage stream hit, starting at 100 health. **One unverified hypothesis is that ordinary Husk armour reduced actual damage slightly below 3.** Diagnose health, cast age, aiming, entity storage and damage events before changing the test or balance. Don't label this hypothesis a confirmed cause or weaken the assertion merely to get green output.
- The prior four-kit checkpoint passed all 11 required tests; its historical log is also included.
- The connected 28-assignment route passed in the latest run. Fixtures use an ordinary survival ServerPlayer with an embedded connection, not an authenticated client or manually paced playthrough.
- No final Minecraft client rendering/interaction pass was performed. Compilation and HTML rendering do not prove mixin application, standard/slim skins, held-item alignment, armour, first person or remote observer correctness.
- Preview capture: 180 frames at 24 fps, 1280x720, 7.5 seconds, no page errors. GIF export: 90 frames at 12 fps, 768x432. See `design/blueflame/preview-verification.json`.
- A stripped Playwright ffmpeg binary failed to read PNG sequences. No successful WebM is included or claimed. The GIF and editable HTML scene are usable.

## Implemented foundation

### Saves, acquisition and combat

Player schema **4**, protocol **5**. Tested older migrations preserve durable progress; unknown future schemas reject. `HeroLedger` owns an atomic fsynced JSON transaction journal and SavedData mirror, with copy-before-mutation rollback and a bounded Windows rename-lock retry that preserves atomicity. Don't inspect a live Windows journal through a separate process during write stress tests.

Exactly-once authoritative starting assignment, per-quirk archived mastery/unlocks, six slots, remappable Z/X/C/V, R utility, G capstone, J notebook and M atlas. Creative assignment tokens exist for all seven kits and require creative/operator permission. Admin tools, bound introductory/earned reroll balance, persisted nonrefreshable distinct offers, no-alternative no-charge, keep-current still spends, and three durable presets per quirk are implemented. Default random pool remains **Explosion**; operators can enable additional experimental kits through `/mha pools` or assign them. Targeted attunement and select-and-cast parity remain unfinished.

Server owns startup/active/recovery, resource reservation, cooldowns, unlock/slot checks, bounded range/LOS/target queries, team/PvP/protected-contact rules and movement impulses. No terrain-destructive attacks. First-claim objective/reward identities prevent farming currency. The 45–90-minute reroll economy target is untested.

### Seven experimental kits / 57 move records

- Explosion: 8 moves, blast strain, recoil movement, tested starter/three-trial route to all eight.
- Half-Cold Half-Hot: 8 moves, signed temperature, alternating sides, balanced mixed attacks, temporary ice walls/stairs.
- Hardening: 8 moves, finite reduction/counter windows, fatigue and transient attribute modifiers.
- Electrification: 8 moves, charge/overload, timed charge/grounding, clear-sight chains capped at three, finite overload recovery.
- Blueflame: 8 moves, heat-limited streams, aimed/radial releases, ordinary grounded footwork and exposed cooling. One unresolved engine assertion as above. Dabi/basic blue-flame principle/Japanese label verified from his official profile; no invented TV first episode or late identity spoiler is published offline.
- Umbrakinesis: 8 **mod-original** moves. Owner-bound six-second marks, consumed bind/pull, brief resisted control, projectile-only veil, one finite labelled decoy distracting at most eight hostile mobs, light-sensitive shade recovery/cost and a finite radial release. No player-screen blindness, wall vision or intangibility.
- Kinetic Reservoir: 9 **mod-original** moves. 0–60 storage from legitimate hostile guarded physical/projectile damage only, per-hit cap, guard fatigue/break, out-of-combat decay, paid releases/vault, and an uncharged ordinary jab. No self/environmental/allied charging or recursive reflection.

Core files: `Kits`, `QuirkResources`, `ImpactStorage`. Server mechanics: `QuirkRuntime`, `UniqueQuirks`, `DamageResolver`. Per-kit field milestones and first-claim mission practice unlock the new kits through independent/support-friendly objectives without duplicating general currency.

Temporary ice leases are committed before placement, refuse occupied/foreign/protected positions, cap at 128 blocks per owner/2048 globally, and clean at most 16 records/tick without forced chunks. Only the expected custom state is removed; later builds remain. Context cleanup now durably expires unloaded owner leases immediately, for cleanup on natural load.

### Missions and map

28 assignments / 108 stages: 10 hero/school, 10 villain, 8 independent. The operations dimension uses journal-reserved per-player/mission 48x48 modular training cells and bounded conflict-aware construction. Real movement/interact/aid/escort/defend/judgement/combat/boss/shutdown objectives, durable checkpoints, grades and single-claim rewards. Licence 1 comes from completed examination records; licence 2 is an adapted advanced qualification. Copied credential items never grant rights.

These compressed training sets are **not** complete canon regions or final campaign presentation. Solo NPC teamwork exists; actual cooperative parties, agency/crew headquarters and full regional story operations do not.

The authored 144x104 academy/city starter has 24,806 blocks, classroom wings, workshop, infirmary, dorm module, lanes, course and clinic, reproducible in its own dimension with foreign-build refusal. Full U.A., named series/movie regions and canon geography are incomplete. The schematic atlas is not the full map requirement.

### Presentation/assets

Custom title/loading driven by real reload progress; themed pause retaining vanilla actions; six-tab notebook, mission board, offline source/bookmark index and schematic atlas. HUD editor has draft drag/Alt+arrows, scale, opacity, compact/numbers/objectives, reset/save/cancel.

Latest move-panel geometry reserves at least 56 logical units above survival displays, expanding for armour/air/mount rows. A custom nine-slot item hotbar delegates item rendering/decorations to vanilla for counts, durability, glint and cooldowns, supports handed offhand/attack indicator, preserves spectator handling, and can be toggled. Vanilla health/food/armour/air/XP/mount information remains; full custom health/food bars are pending.

Optional softer-player surface: `SoftAvatar`, `PlayerModelMixin`, `ModelPartMixin`, original rounded skin UVs, modest shaping, and a custom native Bendy `BendableCuboid` using the existing player rig and slim/standard cube data. Toggle in presentation options. It is experimental, not a certified human replacement. Armour remains separate and needs clipping/pose checks. Editable free-format `art/models/soft_player.bbmodel`, matching mesh JSON and an original preview skin are included; generated through code, with no claim of manual Blender/Blockbench operation.

The Cadet GeckoLib suit and original training sentinel retain native sources/runtime exports. All 57 moves have icon/animation inputs. `make_kit_assets.py` plus `extra_kit_art.py` generate 49 non-Explosion SVG silhouettes and timed animations. Six original synthesised sounds and provenance remain; the latest three kits still share generic cast sounds and need distinct final audio.

`design/blueflame/` contains the animation GIF, poster, editable preview, native move timing and model inputs. Bending and flame shading there are approximate; this is not a Minecraft recording. Preview-only Three.js 0.180.0 includes MIT license and verified npm SHA-512 provenance and is outside Minecraft resources.

Photon remains a bounded programmatic adapter, without a validated editor `.fx` project or full danger-area correspondence. Protocol 5 caches the server-effective move catalogue by revision, sending it on connect/change and sending profile snapshots separately. The catalogue cap is still 128 IDs and must be deliberately expanded for the full future roster.

## Resume checklist — only after user asks

1. Investigate the exact Blueflame failure above; retain 80 passing pure cases and the other 14 engine cases. Run tests in fresh disposable directories and check nonempty selection/explicit counts.
2. Audit new-kit lifecycle and failed-save paths. Broader per-player tick exception containment and periodic-journal-flush failure handling remain necessary.
3. Verify softer-player mixin/runtime integration from exact pinned sources. Compilation is not client proof; leave visual checks pending under the no-GUI constraint.
4. Complete presentation details: keyboard pagination in the expanded index, custom health/food, select-and-cast, reduced motion/camera options and localisation. New token translation keys were **not added before the stop request**; names/tooltips can show untranslated IDs. Asset validator's final summary still says eight animations although its loop reads all moves.
5. Update stale docs/matrices, bump version metadata and update packaging evidence selection. Internal version is still `0.1.0-dev`. `tools/package_snapshot.py` intentionally requires fully passing engine tests and hardcodes older log names; don't bypass that release gate for this paused source ZIP.
6. Continue the full master brief: specialisations/mastery 65–100, targeted acquisition, alignment quests, equipment, full main-series/movie/original roster, populated source-checked regions, parties/headquarters/campaigns, native Photon projects, full audio, authenticated multiplayer/rig/accessibility and measured performance. Do not pad completed content with generic reskins or call this finished.

## Authoring notes

Use UTF-8 (some generators contain Japanese). `make_foundation_kits.py` now calls `make_extra_kits.py`; the latter is also idempotent on existing foundation data. `make_kit_assets.py` imports `extra_kit_art.py`. `export_soft_avatar.py` writes native/preview mesh inputs.

Normal Gradle builds use committed runtime assets. Optional source-machine Python packages: Pillow 12.3.0, nbtlib 2.0.4, numpy 2.3.5, soundfile 0.14.0. The archive-root `handoff-reference/` recorder has machine-specific Playwright/Chromium paths and needs porting for re-recording; it is not a build prerequisite.
