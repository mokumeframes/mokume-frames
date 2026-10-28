# Cloud continuation — 28 October 2026

The user resumed development and requested a newly built JAR at the first checkpoint so they can try it. The first checkpoint is to diagnose the failing Blueflame engine case, retain the other mechanics, and build a clearly versioned experimental package. Do not substitute the archive's old JAR for this deliverable.

## Current machine and evidence

- Active Git checkout: `/workspace/mokume-frames`; original extraction: `/workspace/mha-checkpoint/mha-rising-heroes`; the original archive and its historical evidence remain intact. All 488 manifest checksums matched before edits.
- Java 21 JDK installed at `/workspace/toolchains/jdk-21.0.8+9`, verified against Adoptium's published SHA-256 checksum. The preinstalled system Java is a runtime without javac.
- Gradle wrapper 9.2.1 downloaded with its committed distribution checksum. Gradle user home: `/workspace/gradle-cache`. Its local properties select the inherited HTTP proxy and `/etc/ssl/certs/java/cacerts`; TLS and dependency verification remain enabled.
- Fixed the Linux wrapper's CRLF line endings and executable permission; `.gitattributes` now preserves LF for `gradlew`.
- All **80 existing JUnit tests passed** on this cloud machine, with zero failures, errors, or skips. This was a standalone Java 21/JUnit Platform 1.11.4 run of all existing test sources against the dependency-free core (excluding Minecraft-dependent HeroLedger), HudLayout, MenuLayout and committed resources, with pinned Gson 2.10.1. It was not a full Gradle or Minecraft engine run. Runner: `/workspace/mha-validation/run-pure-tests.py`; XML reports: `/workspace/mha-validation/junit/`. The runner currently points to the original extraction, so update its root before testing subsequent edits in the active checkout. Gson matched committed verification metadata; the JUnit console artifact matched its published SHA-256.
- The full Gradle build stopped at `createMinecraftArtifacts`: dependency repositories returned proxy HTTP 403. No new mod JAR or engine test results were produced.

## Required network change

The environment draft now preserves the package-manager preset and adds these custom domains: `maven.neoforged.net`, `maven.firstdark.dev`, `maven.kosmx.dev`, `api.modrinth.com`, `cdn.modrinth.com`, `libraries.minecraft.net`, `piston-meta.mojang.com`, `piston-data.mojang.com`, `launchermeta.mojang.com`, and `resources.download.minecraft.net`.

Draft saving does not apply network policy or publish the environment. The user must review/save the environment settings and publish; then retry the actual dependency operation. No token or secret is needed for these public artifacts. Do not bypass the network policy or checksum verification.

## Next build and diagnosis

Use the existing checkout; do not create a worktree. Preserve the requested stack and background-only workflow in AGENTS.md. From the source directory:

```bash
export JAVA_HOME=/workspace/toolchains/jdk-21.0.8+9
export GRADLE_USER_HOME=/workspace/gradle-cache
./gradlew --no-daemon build
./gradlew --no-daemon runGameTestServer -PmhaTestDir=run/gametest-cloud-baseline
```

Use the supported permission flow if the command sandbox prohibits proxy connections. Check full runner outcomes and counts. Use a fresh game directory for each substantive engine run.

The Blueflame fixture checks health <=97 after a 3-damage playerAttack against a Husk. DamageResolver uses vanilla damage, so intrinsic armour is a plausible explanation, but remains unconfirmed. The source waits for entity storage and checks at roughly cast age 14, after startup 8 and before the next pulse at age 16. Inspect actual health, armour, aim, candidate targets, fire state, and cast state before changing balance or the assertion. Keep the pre-windup and cooling assertions meaningful. No gameplay or test assertions have been changed yet.

Once diagnosed, fix the underlying issue or isolate the timing fixture from armour deliberately; run all 80 unit cases and all 15 required engine cases, bump matching build/mod metadata, and provide the new JAR with installation notes and exact verification limits. Client rendering and authenticated multiplayer remain unobserved.
