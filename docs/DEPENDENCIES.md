# Dependency and distribution audit

Checked 28 October 2026. This table records author releases and metadata; successful runtime checks belong in TEST_RESULTS.md.

| Component | Pinned artifact / version | Runtime side | Author evidence and distribution |
|---|---|---|---|
| Minecraft | 1.21.1 | Both | [NeoForge prerequisites](https://docs.neoforged.net/docs/1.21.1/gettingstarted/). Minecraft is not redistributed. |
| Java | Eclipse Temurin 21.0.12.1+1, x64 | Both | [Adoptium API](https://api.adoptium.net/v3/assets/latest/21/hotspot?architecture=x64&image_type=jdk&os=windows&vendor=eclipse); build targets Java 21. JDK downloaded to workspace tools, not installed system-wide. |
| NeoForge | 21.1.217 | Both | [Official Maven](https://maven.neoforged.net/releases/net/neoforged/neoforge/21.1.217/); matches Photon's documented build baseline. LGPL-2.1. |
| ModDevGradle | 2.0.148 | Build | [Official 1.21.1 MDK](https://github.com/NeoForgeMDKs/MDK-1.21.1-ModDevGradle). Gradle 9.2.1 wrapper supplied by MDK. |
| Photon | `com.lowdragmc.photon:photon-neoforge-1.21.1:2.2.9:all` | Both installed; playback client only | [Author integration guide](https://low-drag-mc.github.io/LowDragMC-Doc/en/photon2/java-api/), [source/license](https://github.com/Low-Drag-MC/Photon/tree/1.21). Current LICENSE is CC BY-NC-SA 4.0 with additional port/distribution terms; Gradle metadata still says GPL-3.0. Treat this discrepancy conservatively: no Photon binary bundled in our release. Obtain upstream separately. |
| LDLib2 | `com.lowdragmc.ldlib2:ldlib2-neoforge-1.21.1:2.2.44:all` | Both | [Author project](https://github.com/Low-Drag-MC/LDLib2), [published release](https://modrinth.com/mod/ldlib/version/s7Uru4V0). LGPL-3.0-only. Required explicitly; Photon does not supply it. |
| KilaGraph | 21.1.0.15 inside Photon's `all` JAR | Both | Photon nested dependency. [Author documentation](https://low-drag-mc.github.io/LowDragMC-Doc/en/photon2/java-api/). Do not install a second copy. MIT. |
| Player Animator | `dev.kosmx.player-anim:player-animation-lib-forge:2.0.4+1.21.1` | Both | [Author Maven metadata](https://maven.kosmx.dev/dev/kosmx/player-anim/player-animation-lib-forge/maven-metadata.xml), [source](https://github.com/KosmX/minecraftPlayerAnimator/tree/1.21). Despite artifact suffix `forge`, this release has `neoforge.mods.toml`. MIT. Author states no further development except major fixes. Retained as requested; no PAL substitution. |
| Bendy Lib | `io.github.kosmx.bendy-lib:bendy-lib-forge:5.1` | Client; also safe to install on server only if startup test passes | [Author Maven](https://maven.kosmx.dev/io/github/kosmx/bendy-lib/bendy-lib-forge/5.1/), [author source](https://github.com/KosmX/bendy-lib/tree/1.21). Published JAR inspected: mod id `bendylib`, CC-BY-4.0, Minecraft range `[1.21,)`, client-only mixins. Current branch targets 1.21.4, so a broad range alone is not compatibility proof. |
| GeckoLib | 4.9.3 NeoForge 1.21.1 (`maven.modrinth:geckolib:Grwn5rUB`) | Both required by this mod | [Author-linked release](https://modrinth.com/mod/geckolib/version/Grwn5rUB), [4.x source branch](https://github.com/bernie-g/geckolib/tree/1.21.1). MIT. Compile against actual 4.9.3 artifact; no GeckoLib 5 examples. |

No third-party mod JAR is embedded in Rising Heroes. Development resolution uses upstream repositories. The installation fetcher downloads exact artifacts with SHA-256 checks; it is not a rehost. Keep upstream license/notice information when making a modpack. This fan project does not imply permission to redistribute official MHA art/audio or claim official endorsement.

## Integration risks to verify

- GeckoLib transfers the base humanoid's pose, but does not automatically transfer Bendy Lib's deforming cuboids. Segmented costume limbs need explicit joint transforms.
- Test shoulder origin, elbow/knee bend, held-item attachment and standard/slim proportions independently.
- Photon API imports must remain in client-only classes. Its successful dedicated-server load must be tested with LDLib2 and nested KilaGraph present.
- Author-maintained source and published binary may differ. JAR metadata, resolved hashes, compile results and launch logs take precedence over branch names.
