# Combat presentation checkpoint 0.1.1-dev

This checkpoint implements the requested M1 combo and shared presentation groundwork. It does not mark the seven-quirk completion brief finished. Registry IDs and profile schema 4 are preserved. Network protocol is now 6: update both clients and server together.

## Audit and Blueflame diagnosis

Pinned stack: Minecraft 1.21.1 / NeoForge 21.1.217 / Java 21 / Photon 2.2.9 / LDLib2 2.2.44 / Player Animator 2.0.4+1.21.1 / Bendy Lib 5.1 / GeckoLib 4.9.3. Exact author download URLs and SHA256 hashes remain in dependency-manifest.json. Photon contains KilaGraph; do not install a duplicate.

The earlier server failure was `blueflameWaitsForWindupAndCoolingUsesTheActiveWindow`, run with `./gradlew runGameTestServer -PmhaTestDir=run/<fresh-directory>`. It asserted health <=97 after a nominal 3-point hit against a husk. Diagnostics showed health=97.06, armour=2 and fire=true: the hit landed, but innate armour mitigated 0.06 damage. The fixture now explicitly sets armour to zero, keeping the original damage assertion. The 0.1.0 build at commit 9d368ed passed 80 unit tests and all 15 server tests. The Linux dependency-lock failure preceding it was a separate missing runtimeClasspath entry for pinned Netty epoll, resolved at cd7aa83.

Windows uses its own strict lock snapshot because Linux epoll is not resolved on Windows. No verification or test gate is disabled. Local Windows compilation also encountered NeoForm/JDK class-output and annotation-reader failures, including Java platform classes; these are recorded separately from actual mod compilation/test results.

## M1: left jab, right cross, front kick

Both hands must be empty. Click the configured Minecraft Attack binding while aiming at an entity or open air. Clicking blocks keeps normal mining; sneaking, item use, swimming, flight, riding and weapons keep vanilla controls. Release and click again after recovery. There is no auto-repeat or queued damage. A gap longer than 26 ticks after recovery restarts at the jab. The third strike loops to the jab.

| Strike | Windup | Active | Recovery | Stamina | Damage | Range |
|---|---:|---:|---:|---:|---:|---:|
| Left jab | 3t | 1t | 6t | 3 | 3 | 2.8 blocks |
| Right cross | 4t | 1t | 7t | 4 | 4 | 2.8 blocks |
| Front kick | 6t | 1t | 11t | 6 | 5 | 3 blocks |

Damage respects server scaling, armour, shields, vanilla hurt resistance, walls, teams, friendly-fire/PvP settings, academy protection and mission actors. The server chooses the stage, target, hit time and cost. Rejected spam does not charge resources. There is one target and one hit attempt per accepted strike. Kick adds bounded knockback only on a successful hit. Casting and M1 cannot overlap. Taking unguarded damage, context/quirk change, death, disconnection, equipping an item, entering a vehicle, swimming or flight cancels the chain. Basic strikes do not generate quirk mastery or count as quirk training-lane hits.

Original animation JSONs contain anticipation, distinct left/right shoulder rotations, guards, elbow bends, a raised knee and extended kick, follow-through and neutral recovery. They use a separate Player Animator layer at priority 1900 (casting 1800), short fades and the library's first-person arm rendering. No animation keys the head/camera. Quirk animations leave walking legs free. Animations for all 57 existing techniques have new kit/shape choreography and retain their server-defined durations. `tools/make_combat_animations.py` is the editable deterministic source.

## Shared HUD, notebook and FX

The bottom-left 3x2 grid retains draggable scale, margins, opacity and survival-row exclusion. It now has a single dark backing, larger consistent cells, actual key badges, active outlines, cooldown seconds/progress, cost warnings, lock drawing, EMPTY treatment and NO KEY feedback. Names appear during activation. Resource rows spell out Stamina and the actual quirk resource, with signed temperature and 60-point kinetic capacity. Electrical overload and kinetic guard fatigue have separate rows. Marks match low-resource vs high-load warnings. Notebook equipment controls validate a copy of the current profile and explain rejection; this never grants unlocks.

Photon decoration is now directional, hand-offset, kit-coloured and scaled by ray/ordinary/capstone shape. Half-Cold Half-Hot uses anatomical right ice, left fire, and two origins for combined moves. Explosion uses both palm offsets. Live roots follow the caster's aim; cancellation destroys only that cast's effects. Distance/detail/lifetime/global caps remain. These offsets approximate animated palms; they are not validated skeletal attachment points. This is programmatic Photon integration, not an exported Photon-editor project. No new flashing or camera shake is added.

## Quirk completion order and remaining work

1. Explosion: existing eight-move unlock/equip/persist route retained and regression-tested by the campaign suite; new distinct palm, precision, propulsion and finisher animation silhouettes. Still needs measured progression pacing, controllable acceleration/steering, fully recognisable Howitzer travel/spin choreography and final layered smoke/pressure FX.
2. Half-Cold Half-Hot: thermal rules and safe leased ice retained; new correct-side gestures/origins and signed meter. Still needs visually checked ice growth/melting, combined attack choreography and complete canon technique review.
3. Hardening: finite guarded damage reduction, fatigue and Unbreakable retained; new brace/strike poses and fatigue warning. Full hardened skin/silhouette transformation and deterioration remain unfinished.
4. Electrification: existing bounded charge, overload, grounding and cover-aware chain retained; new discharge poses and separate Charge/Overload rows. Support-equipment precision gating and target-connected Photon arcs remain unfinished.
5. Umbrakinesis (original): owner-bound marks, finite decoys and low-light resource rules retained; new low reaching poses and Shade meter. Actual shadow concealment, attachment to another player's shadow, and safe shadow-entry/exit travel are NOT implemented by this checkpoint. Existing movement remains ordinary footwork.
6. Kinetic Reservoir (original): hostile-impact-only storage, capacity, conversion, decay and loop prevention retained; new compressed brace/release poses and both resource rows. Inward absorption FX and visually tested storage-dependent intensity remain unfinished.
7. Blue Flame: corrected engine fixture, heat/cooling rules and bounded stream retained; new extended/sweeping poses, blue directional decoration and Heat warning. Body damage from overheating, better channel condition checks and renderer-supported heat distortion remain unfinished.

The broad completion brief is retained as the roadmap, in the above order. This checkpoint does not add unverified advanced canon abilities or awakenings.

## Source checks

Reviewed official character profiles for [Bakugo](https://heroaca.com/character/chara_group01/01-02/), [Todoroki](https://heroaca.com/character/chara_group01/01-05/), [Kirishima](https://heroaca.com/character/chara_group01/01-08/), [Kaminari](https://heroaca.com/character/chara_group01/01-11/), and [Dabi](https://heroaca.com/character/chara_group06/06-06/). Basic principles are canon; all numerical timings, costs, M1 choreography and original-quark mechanics are game adaptations. Named advanced technique localisation/execution still requires primary episode review before being advertised as accurate. The exact Player Animator and Photon source JARs from the configured author Maven repositories were inspected for animation decoding, fades, first-person configuration, emitter shapes and executor lifecycle.

## Verification boundaries

New tests cover chain reset, ranges, timing, no repeated hits, spam cost, no mastery farming, cover, cancellation and independent server players. Asset tests decode every animation through the pinned Player Animator parser; layout tests cover GUI coordinate spaces corresponding to 720p/1080p/1440p at common scales. Existing engine tests cover Explosion progression/persistence, seven-kit mechanics, safe ice and bounded kinetic storage. Test outcomes are recorded in the delivered verification report.

The GUI screenshot mentioned in the pasted brief was not attached to this chat; implementation follows its written description and the actual source. No running-client screenshots or gameplay footage have been captured. First/third person, slim/standard skins, equipment, bright/dark backgrounds and authenticated observer clients need visual playtesting. Engine tests use embedded connected survival players; they do not prove authenticated multiplayer rendering or animation quality.
