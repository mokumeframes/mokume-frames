"""Deterministic original art and rig sources. Python 3 + Pillow; no downloaded MHA assets."""
from pathlib import Path
from PIL import Image, ImageDraw
import json, uuid, base64, io

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/mha'
SOURCE = ROOT / 'art'

def write(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + '\n', encoding='utf-8')

def save(im, path):
    path.parent.mkdir(parents=True, exist_ok=True)
    im.save(path)

# Shared 24px icon grid. Shapes are original silhouettes; all lettering is live GUI text.
shapes = {
 'palm_burst': [[(12,1),(15,7),(22,5),(18,11),(23,15),(16,16),(14,23),(10,17),(3,20),(6,13),(0,9),(8,8)]],
 'blast_dash': [[(2,6),(14,6),(11,2),(23,12),(11,22),(14,17),(2,17),(8,12)]],
 'ap_shot': [[(1,10),(15,10),(15,5),(23,12),(15,19),(15,14),(1,14)]],
 'recoil_dodge': [[(1,12),(10,3),(10,8),(22,8),(22,16),(10,16),(10,21)]],
 'stun_grenade': [[(12,1),(14,8),(22,4),(17,11),(23,14),(16,16),(19,23),(12,18),(6,23),(8,16),(1,14),(7,10),(3,4),(10,8)]],
 'burst_volley': [[(2,4),(7,1),(11,4),(7,7)],[(12,10),(17,7),(22,10),(17,13)],[(3,19),(8,15),(13,19),(8,23)]],
 'aerial_redirection': [[(2,21),(7,9),(11,12),(17,3),(14,1),(23,1),(23,10),(20,7),(12,19),(8,16)]],
 'howitzer_impact': [[(12,1),(21,6),(23,15),(16,23),(6,22),(1,14),(4,5)],[(12,6),(17,9),(18,14),(14,18),(9,17),(6,12),(8,8)]],
}
for name, polygons in shapes.items():
    im = Image.new('RGBA', (24,24))
    d = ImageDraw.Draw(im)
    for i, points in enumerate(polygons):
        d.polygon(points, fill='#FF974F' if i == 0 or name == 'burst_volley' else '#111B27')
    save(im, ASSETS/f'textures/gui/moves/{name}.png')
    svg = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">' + ''.join(
        '<polygon fill="%s" points="%s"/>' % ('#FF974F' if i==0 or name=='burst_volley' else '#111B27', ' '.join(f'{x},{y}' for x,y in p))
        for i,p in enumerate(polygons)) + '</svg>\n'
    (SOURCE/'interface').mkdir(parents=True, exist_ok=True)
    (SOURCE/f'interface/{name}.svg').write_text(svg, encoding='utf-8')

icons={}
token=Image.new('RGBA',(32,32)); d=ImageDraw.Draw(token)
d.polygon([(16,1),(28,8),(28,24),(16,31),(4,24),(4,8)],fill='#273B4A',outline='#F2EDE2',width=2)
d.polygon([(16,5),(20,12),(27,13),(22,19),(23,26),(16,23),(9,26),(10,19),(5,13),(12,12)],fill='#FF974F')
icons['explosion_token']=token
im=Image.new('RGBA',(32,32)); d=ImageDraw.Draw(im)
d.rounded_rectangle((5,1,27,31),radius=3,fill='#55D3BC',outline='#F2EDE2',width=2)
d.arc((9,8,23,23),30,300,fill='#111B27',width=3); d.polygon([(22,6),(25,15),(17,13)],fill='#111B27')
icons['reroll_token']=im
im=Image.new('RGBA',(32,32)); d=ImageDraw.Draw(im)
d.rectangle((4,2,27,30),fill='#F2EDE2');d.rectangle((4,2,9,30),fill='#27464B')
d.rectangle((12,7,23,9),fill='#55D3BC'); d.line((12,15,23,15),fill='#273B4A',width=2); d.line((12,21,20,21),fill='#273B4A',width=2)
icons['hero_notebook']=im
im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im)
d.rounded_rectangle((2,5,29,26),radius=2,fill='#F2EDE2',outline='#273B4A',width=2)
d.rectangle((3,6,28,10),fill='#55D3BC');d.rectangle((6,14,13,22),fill='#273B4A')
d.line((17,15,25,15),fill='#273B4A',width=2);d.line((17,20,23,20),fill='#FF974F',width=2)
icons['licence_credential']=im
for name,im in icons.items():
    save(im, ASSETS/f'textures/item/{name}.png')
    write(ASSETS/f'models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'mha:item/{name}'}})

# Flat palette tiles keep the suit crisp. Box UVs stay inside 32x32 tiles.
tex=Image.new('RGBA',(128,128),'#142433');d=ImageDraw.Draw(tex)
for (x,y),col in [((0,0),'#203447'),((32,0),'#E8E5D9'),((64,0),'#52C4B0'),((96,0),'#EE8445')]:
    d.rectangle((x,y,x+31,y+31),fill=col)
    d.line((x,y,x+31,y),fill='#FFFFFF',width=1)
save(tex,ASSETS/'textures/armor/cadet.png')

UV={'navy':[0,0],'cream':[32,0],'teal':[64,0],'orange':[96,0]}
def cube(o,s,material='navy',inflate=0):
    return dict(origin=o,size=s,uv=UV[material],inflate=inflate)
def bone(n,p,c=None,parent=None):
    b={'name':n,'pivot':p}
    if c:b['cubes']=c
    if parent:b['parent']=parent
    return b
bones=[
 bone('armorHead',[0,24,0],[cube([-4.5,29,-4.5],[9,3,9],'navy'),cube([-4.7,25,-4.7],[1,5,9.4],'cream'),cube([3.7,25,-4.7],[1,5,9.4],'cream'),cube([-4,28,-4.8],[8,1.5,1],'teal')]),
 bone('armorBody',[0,24,0],[cube([-4.5,12,-2.5],[9,12,5]),cube([-4.8,17,-3],[9.6,5,1],'cream'),cube([-1,14,-3.2],[2,9,1],'teal'),cube([-4.7,12,-2.9],[9.4,2,5.8],'cream'),cube([-1.5,12,-3.2],[3,2,1],'orange')]),
]
for side,sgn in [('Right',-1),('Left',1)]:
    x=-8.4 if sgn==-1 else 4
    piv=[5*sgn,22,0]
    bones += [bone(f'armor{side}Arm',piv,[cube([x,17,-2.3],[4.4,6,4.6]),cube([x-.3,20,-2.7],[5,2.6,5.4],'cream')]),
      bone(f'{side.lower()}Forearm',[5*sgn,16,0],[cube([x-.2,10,-2.6],[4.8,5,5.2],'orange'),cube([x-.3,10,-2.9],[5,1,5.8],'cream'),cube([x+1,11,-3],[2,3,1],'teal')],f'armor{side}Arm')]
    lx=-4.15 if sgn==-1 else .05
    bones += [bone(f'armor{side}Leg',[2*sgn,12,0],[cube([lx,6,-2.15],[4.1,6,4.3]),cube([lx-.1,6,-2.6],[4.3,2,1],'cream')]),
      bone(f'{side.lower()}Shin',[2*sgn,6,0],[cube([lx,0,-2.2],[4.1,5,4.4]),cube([lx+1.5,1,-2.5],[1,4,1],'teal')],f'armor{side}Leg'),
      bone(f'armor{side}Boot',[2*sgn,12,0]),
      bone(f'{side.lower()}Foot',[2*sgn,6,0],[cube([lx-.2,-.2,-3],[4.5,2,5.5],'cream'),cube([lx-.2,-.2,-3],[4.5,.7,5.5],'orange')],f'armor{side}Boot')]
geo={'format_version':'1.12.0','minecraft:geometry':[{'description':{'identifier':'geometry.mha_cadet','texture_width':128,'texture_height':128,'visible_bounds_width':3,'visible_bounds_height':3.5,'visible_bounds_offset':[0,1.5,0]},'bones':bones}]}
write(ASSETS/'geo/cadet.geo.json',geo)
write(ASSETS/'animations/cadet.animation.json',{'format_version':'1.8.0','animations':{'animation.cadet.idle':{'loop':True,'animation_length':2,'bones':{}}}})

# Valid native Blockbench project retaining the same geometry and hierarchy.
bio=io.BytesIO();tex.save(bio,format='PNG')
elements=[];groups={};out=[]
for b in bones:
    uid=str(uuid.uuid5(uuid.NAMESPACE_URL,'mha:bone/'+b['name']))
    g={'name':b['name'],'origin':b['pivot'],'rotation':[0,0,0],'uuid':uid,'export':True,'isOpen':True,'children':[]}
    for i,c in enumerate(b.get('cubes',[])):
        cuid=str(uuid.uuid5(uuid.NAMESPACE_URL,f"mha:{b['name']}/{i}"))
        o=c['origin'];s=c['size'];u,v=c['uv']
        elements.append({'name':b['name']+f'_{i}','type':'cube','from':o,'to':[o[j]+s[j] for j in range(3)],'origin':b['pivot'],'uuid':cuid,'uv_offset':[u,v],'box_uv':True,'faces':{face:{'uv':[u,v,u+min(12,s[0]),v+min(12,s[1])],'texture':0} for face in ['north','south','east','west','up','down']}})
        g['children'].append(cuid)
    groups[b['name']]=g
    if b.get('parent'):groups[b['parent']]['children'].append(g)
    else:out.append(g)
write(SOURCE/'models/cadet.bbmodel',{'meta':{'format_version':'4.10','model_format':'free','box_uv':True},'name':'Rising Heroes - original Cadet suit','model_identifier':'mha_cadet','visible_box':[3,3.5,1.5],'resolution':{'width':128,'height':128},'elements':elements,'outliner':out,'textures':[{'name':'cadet.png','id':'0','uuid':str(uuid.uuid5(uuid.NAMESPACE_URL,'mha:cadet-texture')),'source':'data:image/png;base64,'+base64.b64encode(bio.getvalue()).decode()}]})

for part,col in [('helmet','#E8E5D9'),('chestplate','#55D3BC'),('leggings','#203447'),('boots','#EE8445')]:
    im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im)
    if part=='helmet':d.polygon([(5,5),(27,5),(29,23),(23,23),(22,12),(10,12),(9,23),(3,23)],fill=col)
    elif part=='chestplate':d.polygon([(9,3),(3,6),(1,16),(8,18),(9,29),(23,29),(24,18),(31,16),(29,6),(23,3),(20,8),(12,8)],fill=col)
    elif part=='leggings':d.polygon([(6,3),(26,3),(26,30),(18,30),(16,15),(14,30),(6,30)],fill=col)
    else:d.rectangle((3,10,13,29),fill=col);d.rectangle((19,10,29,29),fill=col)
    save(im,ASSETS/f'textures/item/cadet_{part}.png')
    write(ASSETS/f'models/item/cadet_{part}.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'mha:item/cadet_{part}'}})

# Upper body animation leaves ordinary locomotion in control of the legs.
for name,duration,windup in [('palm_burst',13,4),('blast_dash',11,2),('ap_shot',22,9),('recoil_dodge',8,1),('stun_grenade',28,12),('burst_volley',32,8),('aerial_redirection',8,1),('howitzer_impact',61,30)]:
    frames=[{'tick':0,'rightArm':{'pitch':0,'roll':0,'bend':0,'axis':0},'leftArm':{'pitch':0,'roll':0,'bend':0,'axis':0}},
            {'tick':windup,'easing':'easeOutQuad','rightArm':{'pitch':-65,'roll':-15,'bend':55,'axis':0},'leftArm':{'pitch':-35,'roll':20,'bend':35,'axis':0}},
            {'tick':windup+1,'rightArm':{'pitch':-90,'roll':-8,'bend':8,'axis':0},'leftArm':{'pitch':-75,'roll':8,'bend':15,'axis':0}},
            {'tick':duration,'easing':'easeInOutSine','rightArm':{'pitch':0,'roll':0,'bend':0,'axis':0},'leftArm':{'pitch':0,'roll':0,'bend':0,'axis':0}}]
    anim={'version':3,'name':name,'author':'Rising Heroes project','description':'Original palm-cast animation; mod adaptation.','emote':{'beginTick':0,'endTick':duration,'stopTick':duration+1,'isLoop':False,'degrees':True,'moves':frames}}
    write(ASSETS/f'player_animations/{name}.json',anim)
    write(SOURCE/f'animations/{name}.json',anim)

print(f'Generated {len(elements)} suit cubes, 8 move icons, 8 player animations and editable sources.')
