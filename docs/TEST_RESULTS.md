# Verification record — 28 October 2026

Status words are deliberately distinct: **compiled**, **launched**, **manually observed**, **automatically tested**, **pending**.

| Area | Evidence / result | Limit |
|---|---|---|
| Exact dependency resolution | Gradle lockfile and SHA-256 verification metadata; five artifact hashes in dependency-manifest.json | Hashes pin the retrieved author artifacts; no claim that branch metadata alone proves runtime compatibility |
| Foundation compile | Successful Gradle build on Java 21.0.12.1+1 | Compilation does not prove visuals |
| Base client launch | Minecraft NeoForge 1.21.1 title and world-creation screens manually observed, all requested libraries in client log | This launch preceded later gameplay/menu changes. Client was closed after the user asked to avoid desktop control; no subsequent GUI launch/control |
| Final development build | `gradlew build`, Java 21, verification metadata enabled | Final custom client UI/rig/effect playback not manually exercised |
| Packaged-source rebuild | Fresh archive extraction, `build --offline --no-build-cache`; all 48 JUnit tests executed and the rebuilt JAR matched the delivered JAR byte for byte | Uses already-cached Gradle/NeoForge/dependency artifacts; not a clean-machine download/installation test |
| Pure logic + menu bounds | **48 tests passed**: 21 core rule cases, 8 trial/migration cases, 6 saved-preset cases, 6 title-layout cases and 7 HUD-layout cases | Synthetic alternative quirk IDs test the offer algorithm; they are not registered playable quirks |
| Dedicated Minecraft engine | **6 required GameTests passed** with Photon, LDLib2, KilaGraph, Player Animator, Bendy Lib and GeckoLib present | GameTest engine, not an ordinary authenticated public/network server |
| Real server damage | Visible hostile target acquired and damaged; a solid wall blocks acquisition | One focused hit case, not a full combat/PvP balance suite |
| Durable transaction journal | Mastery, school progress, pending offers and spent-token state restored from disk; invalid mutation and failed disk-write rollback checked | v1-to-v2 migration is separately tested; no full power-loss/crash-injection campaign |
| Dimension JSON | Decoded through Minecraft 1.21.1's actual LevelStem codec, verified FlatLevelSource | GameTestServer's preset excludes data-pack dimensions; production travel still needs a normal-world test |
| Authored map | Bounded builder completed 24,806 blocks and produced three persisted/loaded NPC contacts; entry clearance checked | Tested in a disposable GameTest world via a hook explicitly forbidden on normal servers |
| Asset contracts | JSON parse, 8 animation durations, icon sizes, armour hierarchy/source agreement, map bounds/spawn clearance, bed pairing, structure count and texture references passed | Structural validation is not a rendering/rig test |
| Original audio | Six mono 44.1 kHz Vorbis cues exported and decoded; duration, finite samples and headroom checked; event paths/subtitles validated | No in-game listening/multiplayer mix test; generation and numeric validation do not establish perceptual quality |
| Interface design previews | Three HTML renders inspected using a hidden browser: title, loading and notebook | Separate design previews, explicitly not in-game screenshots. The 68% preview is a simulated design state; runtime uses actual reload progress |
| Ordinary server, campaign playthrough, real multiplayer | **Pending** | The starter/trial route was exercised through server handlers without admin grants; no authenticated two-client test, full school-to-licence/villain route or eight-user benchmark |

## Executed rule cases

Exactly-once assignment; empty-pool refusal; creative/operator token permission; mastery archive restoration without resource/global reset; locked move spoof rejection; mastery-plus-trial unlocks; startup cost and cooldown reservation; independent resource/recovery/cooldown gates; airborne/underwater restrictions; slot-type enforcement; cooldown preservation on swap; no-cost one-quirk reroll refusal; distinct offers excluding current; saved offers after serialisation/reopening; keep-current cost; stale/unadvertised choice rejection; small pools; combat restrictions; unique reward identity; unknown save version refusal; copy-before-commit isolation; invalid/duplicate data rejection. Menu geometry was tested at logical sizes 320×240, 426×240, 640×360, 960×540, 853×360 and 1280×540.

Saved-preset cases cover persistence/copy isolation, resources and cooldown retention, combat/recovery/trial locks, revoked or incorrectly slotted moves, per-quirk separation and invalid preset numbers. HUD bounds cover both panel densities, optional numbers, scales 0.6/1.0/1.5, left/centre/right anchors, resize/drag round trips, off-screen clamping and non-finite input rejection at the six GUI sizes. These are geometry/rule tests, not visual, narration or real-input certification.

## Minecraft test names

- `presentationLibrariesLoadOnDedicatedServer`
- `serverAttackUsesRangeAndLineOfSight`
- `criticalJournalRestoresProgressAndPendingOffers`
- `academyDimensionUsesTheActualMinecraftCodec`
- `authoredAcademyConstructsAndContainsUsableContacts`
- `schoolStarterAndAllExplosionTrialsAreEarnableWithoutAdminGrants`

The successful combined build/GameTest command used a fresh disposable test directory and reported **All 6 required tests passed**. Its reported seconds reflect accelerated GameTest ticking and must not be cited as an FPS/TPS benchmark. Initial failed runs were repaired, not counted as passes. The first empty test selection's Gradle success was specifically rejected as test proof.

## Remaining M0 presentation gate

Standard/slim; first/third person; remote observer; walking/sprinting/crouching/jumping/swimming/riding; interrupted casts; equipment/partial-set changes; shoulders/elbows/knees/held items/attachments; actual Photon effect area/readability; exported Photon project; final loading/title/notebook scale checks. These require actual in-game rendering/network observation and were left pending after desktop-control stopped.

The background workflow does not grant permission to silently mark these checks complete or replace the requested stack.

The academy-art pause screen, draggable HUD editor and cast-bound sound cleanup are compiled. Pause preserves Minecraft's existing resume, options, LAN/reporting, mod-list and disconnect actions. Final keyboard/mouse interaction, pause behavior and audio interruption/observer checks remain pending in-game.

## Continued server-loop verification

The end-to-end handler test now earns all eight Explosion actions, 50 mastery, 650 credits and exactly one introductory token balance, with no licence privilege. It checks duplicate contact handling and includes real target line-of-sight/range acquisition, course checkpoints, timed first aid and the three technique trials. It positions an embedded test player and invokes server handlers; it does not constitute an authenticated client/manual playthrough or a pacing measurement.
