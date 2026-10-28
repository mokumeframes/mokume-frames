"""Download the exact separate runtime library mods from upstream with SHA-256 checks.
Normal Gradle builds already resolve their own compile dependencies. Python 3 standard library only.
"""
from pathlib import Path
import argparse,hashlib,json,os,urllib.request

ROOT=Path(__file__).resolve().parents[1]
def sha256(path):
    digest=hashlib.sha256()
    with path.open('rb') as stream:
        for block in iter(lambda:stream.read(1024*1024),b''):digest.update(block)
    return digest.hexdigest()
def main():
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('destination',type=Path);args=parser.parse_args()
    destination=args.destination.resolve();destination.mkdir(parents=True,exist_ok=True)
    manifest=json.loads((ROOT/'dependency-manifest.json').read_text(encoding='utf-8'))
    for artifact in manifest['artifacts']:
        name=artifact['filename']
        if Path(name).name!=name:raise SystemExit('Unsafe artifact filename in manifest')
        final=destination/name
        if final.exists():
            if sha256(final)!=artifact['sha256']:raise SystemExit(f'Existing checksum mismatch; file preserved: {final}')
            print('Verified existing '+artifact['name']);continue
        partial=destination/(name+'.download')
        if partial.exists():raise SystemExit(f'Previous partial download exists; inspect it before retrying: {partial}')
        request=urllib.request.Request(artifact['url'],headers={'User-Agent':'MHA-Rising-Heroes-dependency-fetcher'})
        try:
            with urllib.request.urlopen(request,timeout=90) as response,partial.open('xb') as stream:
                size=0
                for block in iter(lambda:response.read(1024*1024),b''):
                    size+=len(block)
                    if size>artifact['bytes']:raise ValueError('Upstream length exceeds pinned artifact size')
                    stream.write(block)
            if partial.stat().st_size!=artifact['bytes'] or sha256(partial)!=artifact['sha256']:raise ValueError('Upstream content changed or the download is incomplete')
            os.rename(partial,final)
        except Exception as error:raise SystemExit(f"Could not verify {artifact['name']}: {error}. Any partial file was preserved; do not install it.") from error
        print('Fetched and verified '+artifact['name'])
    print('Install NeoForge 21.1.217 and the Rising Heroes JAR separately. KilaGraph is already nested in Photon.')
if __name__=='__main__':main()
