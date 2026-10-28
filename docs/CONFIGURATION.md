# Configuration and administrator reference

Server configuration: the world's `serverconfig/mha-server.toml`. Client configuration: the instance's `config/mha-client.toml`. Values are read on the appropriate side. Restart the world/server after file edits to ensure clients receive a consistent effective state; `/reload` revalidates move data and subsequent snapshots use the new effective values.

| Setting | Default | Effect |
|---|---|---|
| `combat.pvp` | false | Player quirk damage, additionally respecting Minecraft's PvP/team rules |
| `combat.friendlyFire` | false | Allied entity damage |
| `combat.damageScale` | 1.0 | Multiplies the server's base move damage; effective values are synchronised to the notebook |
| `world.protectAcademy` | true | Restricts survival block placement/breaking in the starter hub |
| `quirks.startingPool` | `["explosion"]` | Current implemented development pool; an empty list gives an explicit onboarding error, not a free quirkless result |
| `menus.themed` (client) | true | Custom title, academy-art pause screen and resource-reload presentation; earliest NeoForge bootstrap is retained |
| `hud.enabled` (client) | true | Compact six-slot technique panel |
| `hud.scale` (client) | 1.0 | 0.6–1.5; configurable in notebook |
| `hud.opacity` (client) | 0.9 | 0.3–1.0 background opacity |
| `hud.anchorX`, `hud.anchorY` (client) | 0.0, 1.0 | Normalised panel position within the current viewport; defaults to bottom left |
| `hud.compact` (client) | false | Smaller technique cells |
| `hud.numbers` (client) | true | Numeric stamina/strain below the meters |
| `hud.questTracker` (client) | true | Objective panel on sufficiently wide GUI sizes |
| `effects.reduced` (client) | false | Reduces decorative Photon particle count; server hit logic is unchanged |

The notebook's Settings page changes client toggles and scale and saves them immediately. The HUD editor keeps a separate draft until Save layout; Cancel/Escape discards it. Drag or use Alt+arrows, with Shift for fine movement. These client settings require no server restart. There is no compulsory camera shake or screen flash. The replacement hotbar, independent quest-panel anchors, separate mod-specific audio sliders and complete accessibility certification remain pending. Original attack sounds use Minecraft's Players slider; UI selection uses the master/interface path.

Move data: `data/mha/mha/moves.json`. Override that path in a data pack to tune the eight existing move IDs. Validation rejects unknown behavior IDs, missing moves, duplicates, invalid shapes/slots, non-finite values and excessive ranges/timing. `/reload` applies valid tuning. Active casts retain the numbers reserved at startup; later casts and synced guide values use new data. Geometry/behavior implementation is code, not arbitrary data-pack scripts.

## Commands

All admin operations below require vanilla permission level 2. Ordinary `/mha`, `/mha notebook`, `/mha travel academy`, and `/mha travel return` are player commands. Command targets are online players in this build.

```text
/mha inspect <player>
/mha quirk assign <player> explosion
/mha reroll grant <player>
/mha mastery set <player> <0..100>
/mha move grant <player> <move_id>
/mha move revoke <player> <move_id>
/mha reputation <player> <hero|villain|independent> <-100..100>
/mha pools list
/mha pools explosion <true|false>
/mha test all_moves <player>
/mha test resources <player>
/mha test licence_bypass <player>
/mha reset <player> preview
/mha reset <player> confirm <nonce>
/mha integration suit
/mha integration target
/mha integration attack
```

Assignment cancels active casts and restores the quirk's archived mastery; it does not fill resources, unlock everything or delete school progress. `all_moves`, `resources` and `licence_bypass` are explicitly separate testing controls. Pool changes made by command apply immediately and are saved. With only one quirk, no weighted multi-quirk distribution is advertised yet.

Reset preview lists the destructive scope and issues a player/administrator-bound nonce valid for 60 seconds. Inventory and builds remain; progression reset is separate from assignment. `resources` intentionally clears cooldowns for operator testing and must not be exposed as an ordinary-player permission.

## Persistence and network boundaries

Clients request an operation, slot and identifiers. They never supply damage, target entity, XP award, resource cost, position or licence eligibility. The server resolves those values and uses monotonic session sequences, a two-tick action rate limit, unlock checks, state checks and ability-specific cooldowns. Fake/unnegotiated connections are not sent custom snapshots.

The atomic journal is committed before an accepted cast/assignment/reroll/reward outcome. Physical reroll items are recoverable credentials, not an independent mintable currency. Failed validation or journal writes leave the previous durable profile in place. A crash can cancel an already-paid cast without refund; it cannot replay the damage from the journal. Full crash-injection, malicious packet fuzzing and real multiplayer certification remain pending.

`guide.spoilerSeason` (client, default 1, range 0–8) controls season-tagged reference disclosure in the source notebook. It does not hide essential action mechanics. The live official profile page may itself contain later spoilers; the offline notebook warns about that rather than copying them. Player records now use schema v2 with a tested idempotent v1 migration; network protocol 2 rejects older incompatible development clients.
