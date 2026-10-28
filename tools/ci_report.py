"""Report fresh CI evidence without treating compilation or an empty suite as readiness."""
import json
import os
from pathlib import Path
import re
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[1]

def annotation(level, message):
    message = message.replace('%', '%25').replace('\r', '%0D').replace('\n', '%0A')
    print(f'::{level}::{message}')

counts = dict(tests=0, failures=0, errors=0, skipped=0)
for path in (root / 'build/test-results/test').glob('TEST-*.xml'):
    suite = ET.parse(path).getroot()
    for key in counts:
        counts[key] += int(suite.attrib.get(key, 0))
engine_path = root / 'build/mha-gametests.log'
engine_log = engine_path.read_text(errors='replace') if engine_path.exists() else ''
match = re.search(r'All (\d+) required tests passed', engine_log)
engine_passed = int(match.group(1)) if match else 0
state = {
    'commit': os.environ.get('GITHUB_SHA'),
    'build': os.environ.get('COMPILE_OUTCOME', 'unknown'),
    'junit': counts,
    'engine': {'outcome': os.environ.get('ENGINE_OUTCOME', 'unknown'), 'required_passed': engine_passed},
    'client_visuals': 'unobserved in Minecraft',
    'authenticated_multiplayer': 'unverified',
    'complete_mod': False,
}
output = root / 'build/checkpoint-verification.json'
output.parent.mkdir(exist_ok=True)
output.write_text(json.dumps(state, indent=2) + '\n')
annotation('notice', json.dumps(state))
for name, outcome in [('mha-build.log', state['build']), ('mha-gametests.log', state['engine']['outcome'])]:
    path = root / 'build' / name
    if outcome == 'failure' and path.exists():
        text = path.read_text(errors='replace')
        # The final Gradle error includes dependency resolution and checksum diagnostics.
        start = text.rfind('FAILURE:')
        excerpt = text[start:] if start >= 0 else '\n'.join(text.splitlines()[-70:])
        annotation('error', name + '\n' + excerpt[-18000:])
        failures = [line for line in text.splitlines() if 'failed at' in line or 'required tests failed' in line]
        if failures:
            annotation('error', '\n'.join(failures))
if state['build'] == 'success' and (counts['tests'] < 87 or counts['failures'] or counts['errors'] or counts['skipped']):
    raise SystemExit('Expected at least 87 passing JUnit cases with no failures, errors or skips')
if state['engine']['outcome'] == 'success' and engine_passed < 18:
    raise SystemExit('Expected at least 18 explicitly passing required engine cases')
