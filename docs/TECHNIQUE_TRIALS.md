# Explosion technique trials

These are original mod training activities, not canonical school/licence examinations. Start them in the notebook's Journal while standing near Instructor Sato at X75/Z52. Equip AP Shot before starting. Rooftop sections also require Blast Dash or Recoil Dodge equipped.

| Trial | Prerequisite | Server-verified objectives | Earned result |
|---|---|---|---|
| Precision Circuit | Starter school route complete; AP Shot equipped | AP Shot lanes 1, 2 and 3 in order, each from 8–16 blocks, within 45 seconds. Solid cover still blocks the ray. | +15 mastery (20→35), 125 credits, Burst Volley |
| Rooftop Recovery | Precision Circuit complete | Use Blast Dash or Recoil Dodge while airborne in the city; land on the cyan rooftop pad at X134/Y10/Z44 within 90 seconds. Ordinary stairs reach the rooftop; no fall immunity is granted. | Aerial Redirection and 100 credits; mastery retained at 35 |
| Capstone Circuit | Precision and Rooftop trials complete | Within 150 seconds: first aid for Mika, airborne recoil and rooftop landing, the three ordered long-range targets, then report to Sato. | +15 mastery (35→50), 200 credits, Howitzer Impact |

Trial objectives, checkpoint and deadline are durable player fields. The server rejects out-of-order, wrong-move, close-range and expired target events. Each completed trial has a unique reward identity. Repeating a conversation cannot pay it again. Failed/cancelled attempts give no reward and do not erase completed training. No mastery is awarded for unbounded dummy spam.

Active trials lock loadout edits, rerolls and return travel. Use Cancel current trial in the Journal to leave the attempt. A timeout, death or leaving the academy also cancels it. Assignment by a creative/operator tool deliberately cancels transient trial state while retaining completed trial/reward records.

The automated Minecraft route test reaches all eight unlocks through the real server contact, target, course, first-aid and trial handlers without using administrative grant commands. It checks the resulting mastery, 650 total credits, exactly one introductory reroll balance and zero licence privileges. It positions a test player and accelerates ticks; this is not a manual playthrough or a pacing measurement.

## Save migration

Player schema v2 adds active-trial fields. Loading schema v1 preserves existing quirk, archived mastery, story, credits, tokens, pending offers and cooldowns, then initializes the new trial state. Migration is idempotent; unknown future schemas fail visibly. The journal container remains version 1 because its outer format is unchanged. This migration is covered by automated logic tests.
