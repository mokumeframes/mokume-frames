"""Record exact resolved author artifacts. This manifest fetches; it does not redistribute them."""
from pathlib import Path
import json,hashlib
ROOT=Path(__file__).resolve().parents[1]
CACHE=Path.home()/'.gradle/caches/modules-2/files-2.1'
specs=[
 ('Photon','com.lowdragmc.photon','photon-neoforge-1.21.1','2.2.9','photon-neoforge-1.21.1-2.2.9-all.jar','https://maven.firstdark.dev/snapshots/com/lowdragmc/photon/photon-neoforge-1.21.1/2.2.9/photon-neoforge-1.21.1-2.2.9-all.jar','Author custom license; see docs/DEPENDENCIES.md'),
 ('LDLib2','com.lowdragmc.ldlib2','ldlib2-neoforge-1.21.1','2.2.44','ldlib2-neoforge-1.21.1-2.2.44-all.jar','https://maven.firstdark.dev/snapshots/com/lowdragmc/ldlib2/ldlib2-neoforge-1.21.1/2.2.44/ldlib2-neoforge-1.21.1-2.2.44-all.jar','LGPL-3.0-only'),
 ('Player Animator','dev.kosmx.player-anim','player-animation-lib-forge','2.0.4+1.21.1','player-animation-lib-forge-2.0.4+1.21.1.jar','https://maven.kosmx.dev/dev/kosmx/player-anim/player-animation-lib-forge/2.0.4+1.21.1/player-animation-lib-forge-2.0.4+1.21.1.jar','MIT'),
 ('Bendy Lib','io.github.kosmx.bendy-lib','bendy-lib-forge','5.1','bendy-lib-forge-5.1.jar','https://maven.kosmx.dev/io/github/kosmx/bendy-lib/bendy-lib-forge/5.1/bendy-lib-forge-5.1.jar','CC-BY-4.0'),
 ('GeckoLib','maven.modrinth','geckolib','Grwn5rUB','geckolib-Grwn5rUB.jar','https://api.modrinth.com/maven/maven/modrinth/geckolib/Grwn5rUB/geckolib-Grwn5rUB.jar','MIT')
]
artifacts=[]
for name,group,artifact,version,filename,url,license in specs:
    files=list((CACHE/group/artifact/version).glob('*/'+filename))
    if len(files)!=1:raise RuntimeError(f'Expected one resolved artifact for {name}: {files}')
    blob=files[0].read_bytes()
    artifacts.append(dict(name=name,filename=filename,url=url,sha256=hashlib.sha256(blob).hexdigest(),bytes=len(blob),license=license))
obj=dict(minecraft='1.21.1',neoforge='21.1.217',java=21,kilagraph='21.1.0.15 nested inside Photon; do not add another JAR',artifacts=artifacts)
(ROOT/'dependency-manifest.json').write_text(json.dumps(obj,indent=2)+'\n',encoding='utf-8')
print('Pinned SHA-256 and download URLs for five resolved library JARs.')
