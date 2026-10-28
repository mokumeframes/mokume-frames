# Install and try the development build

This is an experimental foundation/initial-loop snapshot, not a content-complete release. Back up worlds before using development mods.

## Player installation

1. Use Minecraft Java **1.21.1**, Java **21**, and NeoForge **21.1.217**. Install NeoForge from its official distribution.
2. Put `mha-rising-heroes-0.1.0-dev.jar` and the five JARs in `dependency-manifest.json` in the instance's `mods` folder. Photon `all` includes KilaGraph; do not add a duplicate KilaGraph file. Do not mix other Minecraft versions.
3. To fetch the exact library artifacts from their author repositories with checksum verification, run `tools/fetch-dependencies.ps1 -Destination 'your-instance/mods'`. Third-party binaries are deliberately separate from this source package and the Rising Heroes JAR.
4. Create or load a survival world. The server assigns the only current development quirk, Explosion, once and saves it before reveal. Death/rejoin/dimension transfer uses the same profile.
5. Press **J** for the notebook or use `/mha notebook` to recover it. Keys **Z/X/C/V** cast the four technique slots; **R** is utility and **G** capstone. **M** opens the regional atlas. All bindings are under Minecraft Controls → MHA: Rising Heroes; actual bindings appear in the HUD. Empty/locked slots reject casts.

Use `/mha travel academy` or the notebook's travel button. The first request prepares the separate `mha:academy` region in bounded batches. Retry after preparation. The server stops construction on conflicting blocks. Existing survival builds are not a placement destination. `/mha travel return` restores the recorded survival destination after collision/border checks. Combat/recovery restricts travel.

## Starter route

The map is an original compressed game adaptation, not a canon-accurate city survey.

| Step | Where / activity | Durable result |
|---|---|---|
| Enrol | Registrar Aki at X39/Z38, central entrance | School stage 1; starter jacket/boots; 25 credits |
| Control | Hit the three separate marked lanes at X78/Z20,30,40 with a technique; alternatively treat Mika at the city clinic X114/Z76 for five seconds | Mastery 5, control trial, AP Shot, 50 credits |
| Movement | Reach teal checkpoints (74,73), (79,77), (74,82), (80,86) in order; ground routes are valid | Mastery 12, movement trial, Recoil Dodge, 50 credits |
| First field rescue | Return to Mika at X114/Z76 and remain within four blocks for five seconds; damage/leaving cancels first aid | Mastery 20, rescue trial, Stun Grenade, 100 credits, introductory bound reroll balance/credential |

Equip earned techniques on the notebook's Techniques page. Four technique slots, one utility slot, one capstone slot. After starter training, visit Instructor Sato and use the Journal to start Precision Circuit, Rooftop Recovery and Capstone Circuit. These earn Burst Volley, Aerial Redirection and Howitzer Impact respectively; all eight actions are now earnable. See TECHNIQUE_TRIALS.md for the precise prerequisites and checkpoints.

The Techniques page also has three saved presets for the current quirk. Choose 1–3, then Save preset or Load preset. These actions require leaving combat/recovery and finishing/cancelling an active trial. Loading rechecks unlocks and never refreshes resources or cooldowns.

Open Settings → HUD layout editor, or use its button in the themed pause menu. Drag the outlined move panel, or hold Alt and use arrow keys (Alt+Shift for finer movement). Scale, opacity, compact mode, resource numbers and the objective tracker are adjustable. Save layout writes the client configuration; Escape/Cancel discards the draft. Vanilla survival displays remain active. Attack audio follows the Players volume slider; menu confirmation follows the master/interface path. Enable Minecraft subtitles for the sound captions.

**Only Explosion is implemented.** There are no eligible alternatives; reroll generation explains this and spends nothing. The three-offer engine has synthetic-pool logic tests, not three additional playable quirks. Token value is the player-bound server balance; copied or transferred credential items cannot mint value. General rank, story, credits and archived mastery are not reset by quirk assignment.

The current route does **not** award a provisional licence. Later school exams, professional status, villain and independent campaigns remain in the backlog. An operator-only licence bypass exists for testing, separate from ordinary progression. No physical licence item grants privileges.

## Dedicated server

Install NeoForge 21.1.217 and the same mod/library set on a Java 21 server. Bendy Lib is a client requirement; its presence on a dedicated GameTest server was also tested. Use your normal server launcher and review/accept Minecraft's EULA yourself where required. The automated checks use Minecraft's dedicated GameTest engine; an ordinary public/network server installation was not completed in this snapshot.

Keep `world/data/mha-transactions.json` with the world. It is the authoritative atomic journal for player progression, token balances and reward identities. `mha_heroes.dat` is the SavedData mirror. Do not delete only the journal or copy profiles between unrelated worlds casually. Unknown save versions and corrupt journals fail visibly rather than granting fresh rolls.

PvP and friendly fire default off. Quirk attacks do not edit terrain. The academy hub is protected from survival block edits; creative operators can edit it. External region-protection integrations are not yet implemented. Read `CONFIGURATION.md` and `TEST_RESULTS.md` before multiplayer deployment.

## Build without desktop control

Use a 64-bit Java 21 JDK and run:

```powershell
./tools/build.ps1 -JavaHome 'C:/path/to/jdk-21' -ServerTests
```

The script builds and runs JUnit plus dedicated Minecraft GameTests. It never launches a GUI. Python asset generators require Pillow and nbtlib; generated runtime assets are already included. A normal `gradlew build` needs no Python. Developer client launch is intentionally manual (`gradlew runClient`); no desktop/input automation is scheduled.
