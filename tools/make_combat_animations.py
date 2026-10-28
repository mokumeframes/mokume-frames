"""Original Player Animator v3 sources. No head/camera keys; walking legs stay free except kick/footwork."""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'src/main/resources/assets/mha/player_animations'
def pose(body=0,left=-35,right=-35,lb=45,rb=45,lean=0):
    return {'body':{'yaw':body,'pitch':lean},'leftArm':{'pitch':left,'yaw':-8,'roll':12,'bend':lb,'axis':0},'rightArm':{'pitch':right,'yaw':8,'roll':-12,'bend':rb,'axis':0}}
def frame(t,p,ease='easeInOutSine'):return {'tick':t,'easing':ease,**p}
def write(id,end,frames):
    parts={part for f in frames for part in f if part not in ('tick','easing')}
    neutral={part:{key:0 for key in next(f[part] for f in frames if part in f)} for part in parts}
    frames=[frame(0,neutral),*frames,frame(end,neutral)]
    obj={'version':3,'name':id,'author':'Rising Heroes project','description':'Original combat choreography: anticipation, release, recoil, recovery. Camera remains free.','emote':{'beginTick':0,'endTick':end,'stopTick':end+1,'isLoop':False,'degrees':True,'moves':frames}}
    (ASSETS/f'{id}.json').write_text(json.dumps(obj,indent=2)+'\n',encoding='utf-8')

# Jab: left shoulder leads, right hand guards; cross reverses hip/shoulder rotation.
write('m1_left',10,[frame(1,pose(-16,-52,-42,88,75)),frame(3,pose(22,-94,-46,4,80,5),'easeOutCubic'),frame(5,pose(12,-75,-45,32,76,3)),frame(8,pose(0,-38,-38,58,58))])
write('m1_right',12,[frame(2,pose(20,-42,-48,78,92,-4)),frame(4,pose(-28,-48,-98,80,4,7),'easeOutCubic'),frame(7,pose(-12,-45,-73,78,34,3)),frame(10,pose(0,-38,-38,58,58))])
def kick(p,rpitch,rbend,lpitch=0,lbend=10):
    return {**p,'rightLeg':{'pitch':rpitch,'bend':rbend,'axis':0},'leftLeg':{'pitch':lpitch,'bend':lbend,'axis':0}}
write('m1_kick',18,[frame(2,kick(pose(16,-45,-38,70,75,-5),-35,65,5)),frame(5,kick(pose(10,-52,-30,80,60,-12),-80,90,8)),frame(6,kick(pose(-14,-56,-22,70,35,-15),-95,2,10),'easeOutCubic'),frame(9,kick(pose(-8,-42,-35,65,65,-8),-45,70,4)),frame(14,kick(pose(0,-35,-35,55,55),0,0,0,0))])

kits=json.loads((ROOT/'src/main/resources/data/mha/mha/kits.json').read_text())
owner={id:k['id'] for k in kits for id in k['moves']}
for m in json.loads((ROOT/'src/main/resources/data/mha/mha/moves.json').read_text()):
    id=m['id'];kind=owner[id];start=m['startup'];end=start+m['active']+m['recovery'];shape=m['shape']
    wind=pose(18,-45,-55,70,95,-4);release=pose(-24,-65,-98,45,6,8);settle=pose(-9,-48,-72,55,38,3)
    if kind=='explosion':
        wind=pose(24,-54,-62,82,95,-8);release=pose(-27,-92,-100,8,4,12);settle=pose(-10,-60,-74,50,45,-4)
        if shape=='RAY':wind=pose(20,-62,-65,100,85,-4);release=pose(-20,-82,-100,45,0,4)
        if shape in ('DASH','DODGE','AIR'):wind=pose(0,-25,-25,45,45,10);release=pose(0,42,42,4,4,20);settle=pose(0,20,20,22,22,7)
        if id=='howitzer_impact':wind=pose(60,-78,-80,110,110,-12);release=pose(-65,-108,-108,0,0,18);settle=pose(-24,-70,-74,42,38,6)
    elif kind=='half_cold_half_hot':
        # Anatomical right = ice, left = fire; never mirrored by camera perspective.
        if id.startswith('flame_'):wind=pose(-20,-60,-30,90,50,-3);release=pose(25,-100,-40,4,65,6);settle=pose(10,-70,-30,35,50)
        elif id in ('thermal_shock','flashfreeze_heatwave'):wind=pose(0,-72,-72,95,95,-8);release=pose(0,-100,-100,5,5,9);settle=pose(0,-65,-65,45,45)
        else:wind=pose(20,-28,-55,48,90,-3);release=pose(-26,-35,-95,50,6,8);settle=pose(-10,-28,-65,50,35)
    elif kind=='hardening':
        wind=pose(10,-55,-58,95,95,6);release=pose(0,-62,-65,80,80,10);settle=pose(0,-54,-54,80,80,6)
        if shape!='GUARD':release=pose(-32,-56,-100,85,4,12)
    elif kind=='electrification':wind=pose(0,-38,-38,85,85,-6);release=pose(0,-72,-72,15,15,8);settle=pose(0,-48,-48,48,48,3)
    elif kind=='umbrakinesis':wind=pose(28,-28,-45,65,90,12);release=pose(-32,-70,-92,40,10,18);settle=pose(-10,-45,-55,55,48,8)
    elif kind=='kinetic_reservoir':wind=pose(16,-58,-60,95,95,12);release=pose(-30,-68,-100,65,4,16);settle=pose(-12,-50,-65,75,45,6)
    elif kind=='blueflame':wind=pose(22,-35,-55,60,100,-5);release=pose(-24,-48,-100,55,4,5);settle=pose(-10,-40,-78,58,25,8)
    if shape in ('COOL','GROUND','CHARGE'):release=pose(0,-35,-35,55,55,5);settle=pose(0,-30,-30,45,45,2)
    frames=[]
    if start>1:frames.append(frame(max(1,start-2),wind))
    frames.append(frame(start,release,'easeOutCubic'))
    if m['active']>1:frames.append(frame(start+m['active']-1,release))
    if end-3>start+m['active']-1:frames.append(frame(end-3,settle))
    write(id,end,frames)
print('Wrote 57 quirk timelines and three M1 combo animations.')
