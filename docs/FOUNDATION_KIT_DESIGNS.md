# M2 foundation kits — implementation design

Every value is held in the shared validated move catalogue. These are experimental game adaptations; no additional kit is a polished release entry until its mechanics, acquisition, presentation and multiplayer checks pass. All abilities preserve ordinary survival controls and non-destructive terrain policy. The first integration batch uses eight actions per kit and the common six-slot limit. Specialisation variants and mastery 65–100 remain later work.

## Half-Cold Half-Hot

Known original user: Shoto Todoroki. The [official profile](https://www.heroaca.com/character/chara_group01/01-05/) supports ice from the right and heat/flame from the left. English move labels below are working game labels; timing, geometry and balance are adaptations. First verified TV demonstrations are not inferred from character introduction.

Role: area control and rescue traversal. Stamina measures exertion, while a signed temperature resource runs from -100 to +100. Ice moves lower it, fire moves raise it. Opposite-side use and natural recovery restore balance. Extremes block further same-side overuse; they confer no damage immunity. Thermal Shock and the capstone require controlled temperature. Water weakens the practical use of fire; underwater fire casts refuse with a useful instruction. Walls/ramps are finite, capped, collision-safe temporary constructs with checked restoration, never a way to replace containers/builds or mint block drops.

Eight actions: Ice Shot (precision ray), Flame Jet (short cone), Ice Wall (bounded cover), Ice Ramp (temporary stepped route), Flame Sweep (wider pressure), Flashfreeze (bounded slow with boss resistance), Thermal Shock (balanced precision burst), Flashfreeze Heatwave (interruptible capstone). Narrow ice/fire starter attacks support immediate control practice, with a utility ramp. Counters: cover, flanking, baiting thermal extremes, recovery windows. Rescue utility uses temporary traversal and cover. No permanent ice mountains, phasing or guaranteed freeze-lock. Intended later branches trade precision/cost against shelter size/duration.

## Hardening

Known original user: Eijiro Kirishima / Red Riot. The [official profile](https://heroaca.com/character/chara_group01/01-08/) supports whole-body hardening used for offence and defence. The mod's guard and movement actions are explicitly adaptations, rather than invented canonical named techniques.

Role: close protection and bounded melee. Stamina plus fatigue prevent permanent guarding. Short hardening windows reduce incoming damage without invulnerability; legitimate blocked attacks add fatigue. Counter Guard primes one bounded follow-up after a real enemy impact. Rooted defence trades movement freedom for resistance to displacement. Ordinary weapons remain useful while guarding. No projected magical shield or transferred stone skin is attributed to the actual quirk.

Eight actions: Harden, Braced Step, Hardened Strike, Counter Guard, Stone Anchor, Breakline, Bodyguard Advance and Unbreakable. The latter is a finite capstone stance, not immunity. Support lessons provide a valid initial route before extra attacks unlock. Counters: range, cover, fatigue pressure and punishable stance recovery. Bodyguard utility requires physically taking position between the threat and an ally. Intended later branches trade counterattack reach against guard efficiency and movement.

## Electrification

Known original user: Denki Kaminari / Chargebolt. The [official profile](https://www.heroaca.com/character/chara_group01/01-11/) supports bodily electrical discharge and a temporary overload drawback when output exceeds tolerance. Precise aiming and grounding mechanics are bounded adaptations; ordinary footwork remains complementary movement rather than electrical teleportation.

Role: controlled ranged pressure and short disruption. Stamina, charge and overload are distinct. Discharges spend charge and add load; charge recovers and grounded recovery lowers load. A high-output capstone can force a visible recovery period, with restrained movement slowdown while ordinary survival remains usable. There is no permanent cognitive penalty or camera effect. Underwater electrical casts refuse rather than creating an uncontrolled friendly-fire exploit.

Eight actions: Spark Touch, Grounding Step, Directional Arc, Static Net, Charge Capacitor, Chain Discharge, Grounding Stance and Maximum Discharge. Chaining has a strict target/hop count, per-edge range and line of sight; it never recursively reflects or chains between allies. Brief control respects bosses and repeated-control limits. Counters: cover, separating targets, baiting overload and attacking the recharge window. Intended later branches trade precise discharge reach against safer load handling. Any future support-device aiming must be identified as equipment.

## Shared acceptance

The server checks ownership, unlock, slot, startup/active/recovery, resources, cooldown, underwater/ground requirements and stable-loadout mission state. Changing kit preserves global progress and archived mastery, retains paid restrictions and removes old active modifiers/constructs. Source notebook entries use the same effective numbers as the server. Creative tokens are separate and permission-checked; unfinished kits never become advertised completed content through a token or a random-pool entry.
