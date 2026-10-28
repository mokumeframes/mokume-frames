# MHA: Rising Heroes — execution plan

Target: Minecraft Java 1.21.1, NeoForge 21.1.x, Java 21. Fan project; no official art, music, voices, skins or world downloads are bundled.

## Dependency gate (Milestone 0)

1. Resolve author-published Photon, LDLib2, Player Animator, Bendy Lib and GeckoLib 4 artifacts. Inspect loader metadata, licenses and included libraries; record URLs, versions and SHA-256 hashes.
2. Compile with an actual Gradle wrapper. Start a dedicated GameTest server and a client with this exact stack. Keep all presentation imports in client classes.
3. Build a small integration scene: server-owned hit, Photon effect, Player Animator bend, representative GeckoLib suit. Verify rig transfer explicitly. Track visual and multiplayer checks individually; compilation is not visual proof.

## Content gate (Milestone 1)

Explosion first: two starter moves, earned techniques, bounded stamina/strain, server-side cast states, six slots, versioned persistent assignment/mastery, safe administrative token, repeat-safe reroll transaction, notebook, compact HUD, training and a usable U.A. starter region. Mark development content experimental until its acceptance checks pass. With one finished quirk, rerolls must refuse without charging; never add dummy alternatives.

## Continuing roadmap

- M2: Half-Cold Half-Hot, Hardening, Electrification; populated first region; complete school-to-provisional-licence and introductory villain chains; one boss.
- M3: remaining foundation quirks, complete U.A., agencies and named regions; representative movie and explicitly mod-original content.
- M4: advanced mechanics and full requested roster, movie regions and expanded campaigns, in tested batches.
- M5: assets, accessibility, lore review, multiplayer, balance, migrations, measured performance and release packaging.

The complete original specification is preserved in `docs/MASTER_BRIEF.txt`. No roadmap entry is advertised as completed content. Test records distinguish compiled, launched, manually observed and tested. Blockers do not authorize replacing the requested stack.
