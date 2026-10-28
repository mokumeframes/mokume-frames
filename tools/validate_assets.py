"""Static asset contracts complement GameTests; this does not claim visual rig verification."""
from pathlib import Path
import json
from PIL import Image
import nbtlib
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources';A=RES/'assets/mha'
checks=0
def check(condition,message):
    global checks
    if not condition:raise AssertionError(message)
    checks+=1
for f in RES.rglob('*.json'):json.loads(f.read_text(encoding='utf-8'))
moves=json.loads((RES/'data/mha/mha/moves.json').read_text())
for m in moves:
    for folder in ('gui/moves','item'):
        if folder=='item':continue
        im=Image.open(A/f'textures/{folder}/{m["id"]}.png');check(im.size==(24,24),f'Icon grid: {m["id"]}')
    anim=json.loads((A/f'player_animations/{m["id"]}.json').read_text())
    check(anim['emote']['endTick']==m['startup']+m['active']+m['recovery'],f'Animation timing: {m["id"]}')
    check(anim['emote']['isLoop'] is False,f'One-shot cleanup: {m["id"]}')
geo=json.loads((A/'geo/cadet.geo.json').read_text())['minecraft:geometry'][0]
bones=geo['bones'];names={b['name'] for b in bones}
check(len(names)==len(bones),'Unique rig bone names')
for b in bones:
    check('parent' not in b or b['parent'] in names,'Missing bone parent '+b['name'])
    seen=set();cur=b
    while 'parent' in cur:
        check(cur['name'] not in seen,'Cyclic rig hierarchy');seen.add(cur['name']);cur=next(x for x in bones if x['name']==cur['parent'])
check({'armorHead','armorBody','armorRightArm','armorLeftArm','armorRightLeg','armorLeftLeg','armorRightBoot','armorLeftBoot'}<=names,'GeckoLib 4 armour rig names')
source=json.loads((ROOT/'art/models/cadet.bbmodel').read_text())
check(len(source['elements'])==sum(len(b.get('cubes',[])) for b in bones),'Editable/runtime geometry agreement')
mapdata=json.loads((RES/'data/mha/mha/maps/academy_start.json').read_text())
check(len(mapdata['blocks'])<=40000,'Map block budget')
positions=set()
palette=mapdata['palette'];placed={}
for x,y,z,s in mapdata['blocks']:
    check(0<=x<144 and 0<=y<16 and 0<=z<104 and 0<=s<len(palette),'Out-of-bounds map data')
    check((x,y,z) not in positions,'Duplicate map coordinate');positions.add((x,y,z));placed[x,y,z]=palette[s]
for x,y,z in mapdata['course']:
    check((x,y-1,z) in positions and (x,y,z) not in positions and (x,y+1,z) not in positions,'Walkable checkpoint')
for c in mapdata['contacts']:
    x,y,z=map(int,c['pos']);check((x,y-1,z) in positions and (x,y,z) not in positions and (x,y+1,z) not in positions,'Contact spawn clearance')
for pos,state in placed.items():
    if '_bed[' in state and 'part=foot' in state:
        x,y,z=pos;check(placed.get((x,y,z+1))==state.replace('part=foot','part=head'),'Bed head/foot pairing')
structure=nbtlib.load(RES/'data/mha/structure/academy_start.nbt')
check(len(structure['blocks'])==len(mapdata['blocks']),'Structure/blueprint block agreement')
for model in (A/'models/item').glob('*.json'):
    obj=json.loads(model.read_text());texture=obj['textures']['layer0'].replace('mha:','')
    check((A/f'textures/{texture}.png').exists(),f'Missing item texture: {model.name}')
check((A/'textures/gui/menu/academy_dawn.png').read_bytes()==(ROOT/'art/menu/academy_dawn.original.png').read_bytes(),'Original menu artwork preserved')
sounds=json.loads((A/'sounds.json').read_text());lang=json.loads((A/'lang/en_us.json').read_text())
sentinel=json.loads((A/'geo/training_sentinel.geo.json').read_text())['minecraft:geometry'][0]
source_sentinel=json.loads((ROOT/'art/models/training_sentinel.bbmodel').read_text())
check(sum(len(b.get('cubes',[])) for b in sentinel['bones'])==len(source_sentinel['elements']),'Sentinel source/runtime geometry agreement')
check(Image.open(A/'textures/entity/training_sentinel.png').size==(128,64),'Sentinel texture budget')
check(set(json.loads((A/'animations/training_sentinel.animation.json').read_text())['animations'])=={'idle','walk','windup'},'Sentinel animation states')
missions=json.loads((RES/'data/mha/mha/missions.json').read_text());check(len(missions)==28,'Campaign definition count')
check(sum(len(m['stages']) for m in missions)==108,'Campaign objective count')
check(len(sounds)==6,'Six original sound events')
for name,event in sounds.items():
    check(event['subtitle'] in lang,'Missing subtitle: '+name)
    for sound in event['sounds']:
        path=A/'sounds'/(sound['name'].replace('mha:','')+'.ogg')
        check(path.read_bytes().startswith(b'OggS'),'Invalid or missing Vorbis export: '+name)
print(f'PASS: {checks} static asset assertions; eight timed player animations, armour hierarchy, map bounds/clearance, bed pairs, textures, sound references/subtitles and sources.')
