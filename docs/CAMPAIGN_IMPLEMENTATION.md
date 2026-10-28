# Connected encounter routes — experimental implementation

The first continuation adds 28 authored assignments and 108 objectives: ten school/hero, ten underground/villain and eight independent contracts. Definitions are in `data/mha/mha/missions.json`; `tools/make_missions.py` preserves the authored source. This is a working encounter framework and connected exercise routes. The full requested named world, story dialogue, parties, personal headquarters and final presentation are still separate unfinished requirements.

Use the notebook Journal → Campaign assignment board or `/mha missions`. Start near the academy registrar or instructor after the four starter lessons. The operations dimension contains an original compressed modular exercise set. Each player/mission reserves a unique cell through the atomic journal; cells are prepared in bounded batches with a conflict preflight. Existing survival builds are not reset or used as arenas. Solids precede water and the flood lane has containment. No entire region is force-loaded for abilities.

Objectives include movement markers, contact interactions, five-second first aid, an NPC escort using a bounded collision-aware path search, fifteen seconds of shelter defence under telegraphed pressure, judgement stations and combat. An eight-second interrupted-on-damage emergency shutdown provides an equipment/support alternative to killing the sentinels. Escorted actors do not teleport to the player. The robot is an original 20-cube GeckoLib entity with editable Blockbench geometry, palette texture and idle/walk/windup animation sources; its final visual playback is unobserved.

The school chain connects assessment, orientation, fundamentals, battle trial, USJ exercise stations, festival assessment, supervised internship, provisional examination, work study and advanced qualification. The provisional examination awards licence level 1 after its objective record is committed; the final qualification awards the mod's level 2 professional status. No physical item is consulted for access. `/mha licence` recovers an identity credential from the current server record. A copied credential grants no rights.

Completed stages, mission grades, unique reward claims and licences are durable. Leaving retains the last completed stage, and replay pays no duplicate general reward. Three first completions award one bound research/reroll balance. An active encounter locks assignment, rerolls, loadouts and technique trials. Actor ownership/session/stage checks reject cross-player interaction, damage and reward witnesses. Exercise actors drop no loot or XP.

Definitions and the mission board distinguish mod-created scenarios from source terminology. Primary references checked on 28 October 2026:

- [Official terminology](https://heroaca.com/special/terminology.html): U.A., USJ disaster zones, internships and provisional/professional licensing.
- [Official episode 57 synopsis](https://heroaca.com/episodes/3rd/ep57/): the rescue-exercise context of the provisional examination.
- [Official episode 53 synopsis](https://heroaca.com/episodes/3rd/ep53/): the provisional examination and participating schools.

No claim is made that the invented mission sequencing, robot, grades, training architecture or graduation requirements reproduce the official narrative.

## Verification

Eight campaign rule tests cover all definition paths, prerequisites, exact licence transitions, duplicate/stale evidence, first-claim rewards, stable-loadout locks, checkpoint reloads and v2→v3 migration. The combined suite reached 56 JUnit tests and seven required Minecraft GameTests passing, including a real-handler traversal of all 28 assignments/108 stages. The engine fixture begins at completed starter lessons, positions an embedded player and uses real treatment/escort/defence/shutdown handlers; it is not an authenticated client, manual pacing, party or visual test. Tests use an explicit GameTest-only level binding and short-lived fixture chunk tickets because the GameTest flat preset discards data-pack dimensions.

Failed runs exposed and repaired chunk visibility, fluid construction order, the escort driver's stopping distance and a transient Windows rename lock. Atomic replacement remains mandatory; a narrowly bounded rename retry never falls back to a non-atomic write. Encounter persistence errors pause the pending outcome and retry rather than award it.

Final validation of ordinary-world entry/return, actual menu input, robot/VFX playback, party play, boss phase counterplay and balance remains pending. Other master-brief locations and full campaigns must not be marked complete solely because these exercise routes pass.
