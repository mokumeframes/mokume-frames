# Trying the experimental checkpoint

Use a separate Minecraft instance and a disposable world. This is an unfinished development mod; preserve existing saves before testing updates.

1. Install Minecraft **Java Edition 1.21.1**, **Java 21**, and **NeoForge 21.1.217**. This is not compatible with Bedrock, Fabric, or a different Minecraft version.
2. Download the `mha-rising-heroes-jars-<commit>` artifact from the successful **Build mod JAR** run in this repository's Actions tab. Extract the ZIP.
3. Put the `mha-rising-heroes-<version>.jar` in the instance's `mods` directory. Do **not** install the `-sources.jar` as a mod.
4. Install the exact separate runtime dependencies listed in `dependency-manifest.json`: Photon 2.2.9, LDLib2 2.2.44, Player Animator 2.0.4+1.21.1, Bendy Lib 5.1, and GeckoLib 4.9.3. From the source checkout, `python3 tools/fetch_dependencies.py /path/to/instance/mods` downloads and verifies them. The existing PowerShell fetcher is also available. KilaGraph is already nested in Photon; do not add a second copy. No third-party mod binaries are included in the checkpoint artifact.
5. Check `SHA256SUMS.txt` and `checkpoint-verification.json` beside the JAR. The workflow only uploads this JAR artifact after the build, unit tests and required engine suite succeed. Failed-run evidence is uploaded separately and is not a tested checkpoint.
6. Launch the instance yourself. Read `docs/INSTALLATION.md` for the starter route and `docs/CONFIGURATION.md` for administrative testing commands. Explosion is the default starting pool; the other kits are experimental and require explicit enabling or assignment.

A headless test pass does not certify final rendering, skins, armour alignment, menus, balance or authenticated multiplayer. Those checks remain pending. Please report the tested JAR version/commit, steps to reproduce, and the instance's `logs/latest.log` or crash report for failures; remove personal details or secrets before sharing logs.
