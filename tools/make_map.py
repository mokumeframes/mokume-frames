"""Original, compressed playable academy starter and nearby city block. Requires nbtlib.
The layout is a game adaptation, not a reproduction of exact canonical geography.
Produces the runtime blueprint and an editable standard structure NBT from the same source.
"""
from pathlib import Path
import json
import nbtlib as n
ROOT=Path(__file__).resolve().parents[1]
DATA=ROOT/'src/main/resources/data/mha'
blocks={}
def setb(x,y,z,state):blocks[x,y,z]='minecraft:'+state
def fill(x1,y1,z1,x2,y2,z2,state):
    for x in range(x1,x2+1):
      for y in range(y1,y2+1):
        for z in range(z1,z2+1):setb(x,y,z,state)
def room(x,z,w,d,height=6,roof=True):
    fill(x,0,z,x+w,0,z+d,'smooth_quartz')
    fill(x,1,z,x+w,height,z,'white_concrete');fill(x,1,z+d,x+w,height,z+d,'white_concrete')
    fill(x,1,z,x,height,z+d,'white_concrete');fill(x+w,1,z,x+w,height,z+d,'white_concrete')
    for xx in range(x+2,x+w-1,4):
        fill(xx,2,z,xx+1,4,z,'light_blue_stained_glass');fill(xx,2,z+d,xx+1,4,z+d,'light_blue_stained_glass')
    if roof:fill(x, height+1,z,x+w,height+1,z+d,'smooth_quartz')
    fill(x+w//2-1,1,z+d,x+w//2+1,3,z+d,'air')
    for xx in (x+2,x+w-2):setb(xx,height,z+2,'sea_lantern');setb(xx,height,z+d-2,'sea_lantern')
def desk(x,z):
    setb(x,1,z,'oak_fence');setb(x,2,z,'oak_pressure_plate');setb(x,1,z+1,'oak_stairs[facing=north]')

# Entire authored region is bounded. It is constructed only in its own new dimension.
fill(0,0,0,143,0,103,'grass_block')
fill(8,0,59,135,0,65,'smooth_stone')
fill(65,0,6,71,0,99,'smooth_stone')
fill(91,0,0,99,0,103,'gray_concrete')
for z in range(3,103,8):fill(95,0,z,95,0,z+3,'white_concrete')
# Campus boundary, open at the entry.
fill(3,1,3,87,1,3,'polished_andesite');fill(3,1,3,3,1,90,'polished_andesite')
fill(3,1,90,87,1,90,'polished_andesite');fill(87,1,3,87,1,59,'polished_andesite');fill(87,1,66,87,1,90,'polished_andesite')
for z in range(8,91,12):
    for x in (5,85):fill(x,1,z,x,4,z,'polished_andesite');setb(x,5,z,'sea_lantern')
# Main academic building: two wings, a central glazed connector, and accessible interiors.
room(12,8,24,35,8);room(43,8,24,35,8);room(36,16,7,19,10)
fill(35,1,25,44,3,28,'air')
fill(36,1,16,43,9,16,'light_blue_stained_glass')
fill(36,1,35,43,9,35,'light_blue_stained_glass');fill(38,1,35,41,3,35,'air')
fill(38,0,35,41,0,62,'smooth_quartz')
# Class 1-A and 1-B teaching wings, blackboards, desk clusters.
for x in (12,43):
    fill(x+2,2,9,x+21,4,9,'green_terracotta')
    for xx in (x+5,x+11,x+17):
        for zz in (15,21,27):desk(xx,zz)
    fill(x+1,1,32,x+23,5,32,'white_concrete');fill(x+10,1,32,x+13,3,32,'air')
    setb(x+4,1,37,'lectern');fill(x+18,1,36,x+21,4,36,'bookshelf')
# Support workshop and infirmary, each usable in survival.
room(10,47,18,10,5)
for x,state in ((12,'anvil'),(15,'smithing_table'),(18,'crafting_table'),(22,'furnace'),(25,'grindstone')):setb(x,1,50,state)
room(46,47,16,10,5)
for x in (49,53,57):
    setb(x,1,49,'white_bed[facing=south,part=foot]');setb(x,1,50,'white_bed[facing=south,part=head]')
setb(60,1,54,'barrel')
# Dorm module and cafeteria garden.
room(10,70,21,16,5)
for x in (13,20,27):
    setb(x,1,73,'cyan_bed[facing=south,part=foot]');setb(x,1,74,'cyan_bed[facing=south,part=head]');setb(x,1,79,'barrel')
setb(14,1,83,'crafting_table')
for x in range(37,60,5):desk(x,77)
# Training annex and a walking/jumping route. Colour-coded endpoints remain readable without VFX.
fill(73,0,11,84,0,49,'light_gray_concrete')
for z in (20,30,40):fill(76,0,z,80,0,z+2,'orange_concrete')
course=[[74,1,73],[79,1,77],[74,1,82],[80,1,86]]
for x,y,z in course:fill(x-1,y-1,z-1,x+1,y-1,z+1,'cyan_concrete')
for i in range(4):fill(70+i,0+i,70,70+i,0+i,72,'smooth_quartz')
# City block and small clinic; roof access is deliberately traversable.
room(105,69,17,14,5)
fill(106,1,70,121,3,70,'light_blue_stained_glass');fill(111,1,83,116,3,83,'air')
for z in (9,32):
    room(105,z,24,16,8)
    for x in range(108,126,5):setb(x,1,z+4,'barrel')
for i in range(10):fill(133, i,35+i,135,i,35+i,'smooth_quartz')
fill(129,9,43,136,9,45,'smooth_quartz');fill(133,9,43,135,9,45,'cyan_concrete')
# Trees and planter details use bounded original geometry.
for x,z in ((8,65),(32,65),(61,65),(84,65),(103,60),(132,63),(36,84)):
    fill(x-2,0,z-2,x+2,0,z+2,'moss_block');fill(x,1,z,x,4,z,'oak_log');fill(x-2,4,z-2,x+2,6,z+2,'oak_leaves[persistent=true]')

palette=sorted(set(blocks.values()));lookup={b:i for i,b in enumerate(palette)}
entries=[[x,y,z,lookup[s]] for (x,y,z),s in sorted(blocks.items(),key=lambda p:(p[0][1],p[0][0],p[0][2])) if s!='minecraft:air']
blueprint={'version':1,'size':[144,16,104],'adaptation':True,'entry':[39.5,2,64.5],'course':course,'palette':palette,'blocks':entries,
 'contacts':[{'id':'registrar','name':'Registrar Aki / mod-original','pos':[39,1,38]}, {'id':'mentor','name':'Instructor Sato / mod-original','pos':[75,1,52]}, {'id':'civilian','name':'Mika / mod-original civilian','pos':[114,1,76]}],
 'targets':[[78,1,20],[78,1,30],[78,1,40]]}
(DATA/'mha/maps').mkdir(parents=True,exist_ok=True)
(DATA/'mha/maps/academy_start.json').write_text(json.dumps(blueprint,separators=(',',':')),encoding='utf-8')
def state(s):
    name,_,props=s.partition('[');obj=n.Compound({'Name':n.String(name)})
    if props:obj['Properties']=n.Compound({k:n.String(v) for k,v in (p.split('=') for p in props[:-1].split(','))})
    return obj
nbt=n.File({'DataVersion':n.Int(3955),'size':n.List[n.Int]([144,16,104]),'palette':n.List[n.Compound]([state(s) for s in palette]),'blocks':n.List[n.Compound]([n.Compound({'pos':n.List[n.Int](e[:3]),'state':n.Int(e[3])}) for e in entries]),'entities':n.List[n.Compound]([])},gzipped=True)
(DATA/'structure').mkdir(parents=True,exist_ok=True);nbt.save(DATA/'structure/academy_start.nbt')
print(f'Authored academy/city starter: {len(entries)} blocks, {len(palette)} palette states, 3 contacts, 3 training targets, 4 course checkpoints.')
