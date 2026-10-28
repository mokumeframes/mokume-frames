"""Package the built development mod, editable sources and verification evidence.

Usage: python tools/package_snapshot.py --evidence-dir /path/to/build-logs
This does not launch Minecraft, install mods, accept an EULA or bundle dependencies.
"""
from pathlib import Path
from datetime import datetime, timezone
import argparse, hashlib, json, os, re, shutil, zipfile
import xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parents[1]
VERSION='0.1.0-dev'
SKIP={'.git','.gradle','build','run','__pycache__','.idea','.vscode','.run'}

def sha256(path):
    digest=hashlib.sha256()
    with path.open('rb') as stream:
        for block in iter(lambda:stream.read(1024*1024),b''):digest.update(block)
    return digest.hexdigest()

def source_files():
    for base,dirs,files in os.walk(ROOT):
        dirs[:]=sorted(d for d in dirs if d not in SKIP)
        for name in sorted(files):
            if not name.endswith(('.pyc','.download','.log.lck')):yield Path(base)/name

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--evidence-dir',required=True,type=Path)
    parser.add_argument('--rebuilt-jar',type=Path,help='Optional JAR built from a fresh extraction for byte-identity verification')
    args=parser.parse_args();evidence=args.evidence_dir.resolve()
    logs={'final-build-verification.log':'build.log','interface-audio-verification.log':'minecraft-gametests.log','asset-validation.log':'assets.log'}
    for name in logs:
        if not (evidence/name).is_file():raise SystemExit('Missing evidence: '+name)
    if 'BUILD SUCCESSFUL' not in (evidence/'final-build-verification.log').read_text(errors='replace'):raise SystemExit('Build success not confirmed')
    server=(evidence/'interface-audio-verification.log').read_text(errors='replace')
    passed=re.search(r'All (\d+) required tests passed',server)
    if not passed or 'BUILD SUCCESSFUL' not in server:raise SystemExit('Explicit Minecraft GameTest success not confirmed')
    suites=[]
    for path in sorted((ROOT/'build/test-results/test').glob('TEST-*.xml')):
        suite=ET.parse(path).getroot();suites.append({k:suite.attrib[k] for k in ('name','tests','failures','errors')})
    if not suites or any(int(s['failures']) or int(s['errors']) for s in suites):raise SystemExit('JUnit results missing or failing')
    jar=ROOT/f'build/libs/mha-rising-heroes-{VERSION}.jar'
    rebuilt_verified=False
    if args.rebuilt_jar:
        if sha256(jar)!=sha256(args.rebuilt_jar):raise SystemExit('Extracted-source rebuild differs from the packaged JAR')
        rebuild_log=evidence/'package-rebuild-verification.log'
        if not rebuild_log.is_file() or 'BUILD SUCCESSFUL' not in rebuild_log.read_text(errors='replace'):raise SystemExit('Extracted-source rebuild success log missing')
        rebuilt_verified=True
    required={'META-INF/neoforge.mods.toml','dev/risingheroes/RisingHeroes.class','dev/risingheroes/client/ui/HudEditorScreen.class','mha.client.mixins.json','data/mha/dimension/academy.json','data/mha/mha/maps/academy_start.json','assets/mha/sounds.json','assets/mha/sounds/explosion/heavy.ogg'}
    with zipfile.ZipFile(jar) as archive:
        names=set(archive.namelist())
        if archive.testzip() or not required<=names:raise SystemExit('JAR contents/integrity failed')
        if any('examplemod' in n or n.startswith(('com/example/','com/lowdragmc/','software/bernie/','dev/kosmx/')) for n in names):raise SystemExit('Template entries or bundled third-party library classes found')
    inputs=list((ROOT/'src').rglob('*'))+[ROOT/'build.gradle',ROOT/'gradle.properties']
    if any(p.is_file() and p.stat().st_mtime>jar.stat().st_mtime for p in inputs):raise SystemExit('Build is older than source inputs; rebuild first')
    verification=ROOT/'verification';verification.mkdir(exist_ok=True)
    for original,target in logs.items():shutil.copy2(evidence/original,verification/target)
    if rebuilt_verified:shutil.copy2(evidence/'package-rebuild-verification.log',verification/'packaged-source-rebuild.log')
    junit=verification/'junit';junit.mkdir(exist_ok=True)
    for path in (ROOT/'build/test-results/test').glob('TEST-*.xml'):shutil.copy2(path,junit/path.name)
    summary={'snapshot':VERSION,'packaged_at_utc':datetime.now(timezone.utc).isoformat(),'minecraft':'1.21.1','neoforge':'21.1.217','java':21,'junit_tests':sum(int(s['tests']) for s in suites),'junit_failures':0,'junit_suites':suites,'minecraft_required_tests_passed':int(passed.group(1)),'jar_sha256':sha256(jar),'final_client_presentation':'Compiled; in-game visual/interaction/multiplayer checks pending','complete_master_project':False,'m0_m1_acceptance_complete':False}
    summary['packaged_source_rebuild_byte_identical']=rebuilt_verified
    (verification/'summary.json').write_text(json.dumps(summary,indent=2)+'\n')
    output=ROOT.parent;delivered=output/jar.name;shutil.copy2(jar,delivered)
    source_zip=output/f'mha-rising-heroes-source-{VERSION}.zip'
    files=sorted(source_files())
    with zipfile.ZipFile(source_zip,'w',compression=zipfile.ZIP_DEFLATED,compresslevel=9) as archive:
        for path in files:archive.write(path,Path('mha-rising-heroes')/path.relative_to(ROOT))
    with zipfile.ZipFile(source_zip) as archive:
        if archive.testzip():raise SystemExit('Source archive integrity failed')
        if any(n.endswith('.jar') and not n.endswith('gradle-wrapper.jar') for n in archive.namelist()):raise SystemExit('Unexpected binary dependency in source archive')
        if 'mha-rising-heroes/gradle/wrapper/gradle-wrapper.jar' not in archive.namelist():raise SystemExit('Gradle wrapper missing')
    manifest=output/'SHA256SUMS.txt'
    manifest.write_text(''.join(f'{sha256(p)}  {p.name}\n' for p in (delivered,source_zip)))
    print(f"PASS: {summary['junit_tests']} JUnit tests, {summary['minecraft_required_tests_passed']} Minecraft GameTests; JAR entries and archive integrity verified.")
    for path in (delivered,source_zip,manifest):print(f'{path.name}: {path.stat().st_size:,} bytes')

if __name__=='__main__':main()
