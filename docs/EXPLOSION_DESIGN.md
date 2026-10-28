# Explosion — first integration kit

Status: design, not a completed release entry. Source medium: main anime/manga. Known original user: Katsuki Bakugo. Name and palm-explosion principle checked against the [official character page](https://heroaca.com/character/chara_group01/01-02/). No first-demonstration episode claimed until checked from primary episode material. Later forms and identities are omitted from this initial entry.

Identity: emitter; close/mid-range pressure and recoil mobility. Resource: stamina (0–100) plus blast strain (0–100). Strain recovers away from casts; a cast cannot exceed the strain cap. All numbers below are mod balance/adaptations, not canonical measurements. All proposed names are treated as adaptations pending move-name source checks.

| ID | Action | Startup / active / recovery ticks | Stamina / strain | Cooldown ticks | Shape / range | Base damage | Mastery / trial |
|---|---|---|---|---|---|---|---|
| palm_burst | Press; palm cone | 4 / 1 / 8 | 12 / 12 | 24 | LOS cone, 4 blocks | 5 | Starter |
| blast_dash | Press; facing impulse | 2 / 1 / 8 | 18 / 15 | 60 | Safe bounded motion | 0 | Starter utility slot |
| ap_shot | Press; precision ray | 9 / 1 / 12 | 20 / 18 | 60 | Stops at solid block, 16 blocks | 7 | 5; target-control course |
| recoil_dodge | Press; short reverse impulse | 1 / 1 / 6 | 14 / 9 | 45 | No teleport, no invulnerability | 0 | 12; traversal course |
| stun_grenade | Press; disorienting pulse | 12 / 1 / 15 | 24 / 22 | 120 | LOS sphere, 5 blocks | 2 | 20; rescue trial |
| burst_volley | Press; three staged bursts | 8 / 9 / 15 | 28 / 25 | 100 | Three LOS cones, 6 blocks | 3 per pulse | 35; combat-control trial |
| aerial_redirection | Press; midair steering | 1 / 1 / 6 | 16 / 10 | 50 | Facing impulse; airborne only | 0 | 35; aerial course |
| howitzer_impact | Press; telegraphed blast | 30 / 1 / 30 | 55 / 55 | 600 | LOS sphere, 7 blocks | 14 | 50; capstone trial |

Every damaging move uses ordinary resistible damage, respects teams/PvP policy, cannot damage through walls and never edits terrain. Water prevents demanding palm attacks (mod counterplay); no passive drowning immunity. Shields/armour follow Minecraft damage rules. Bosses do not receive special hard stuns. Reassignment/death/dimension transfer cancels active casts and their presentation. Costs/cooldowns are paid on accepted startup and are not refunded on interruption. All targeting and motion are computed on the server.

Training dummies provide only capped introductory credit; ordinary repeated casting grants none. Mastery comes from unique objectives or bounded legitimate target contributions. Precision and Propulsion specialisations are planned, with range/cost and mobility/recovery tradeoffs; neither is implemented by this design document.
