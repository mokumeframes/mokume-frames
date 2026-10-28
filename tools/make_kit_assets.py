"""Original 24-unit silhouettes, native SVG sources and timed M2 player animations.
Requires Pillow; the included exports are used by a normal Gradle build.
"""
from pathlib import Path
from PIL import Image,ImageDraw
import json
ROOT=Path(__file__).resolve().parents[1];A=ROOT/'src/main/resources/assets/mha';ART=ROOT/'art'
def write(path,obj):path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(obj,indent=2)+'\n')
SHAPES={
 'ice_shot':[[(1,10),(11,10),(11,3),(23,12),(11,21),(11,14),(1,14)]],
 'flame_jet':[[(2,22),(3,13),(9,15),(10,3),(16,9),(18,1),(23,14),(18,22)],[(8,22),(12,13),(16,22)]],
 'ice_wall':[[(2,6),(7,2),(7,23),(2,23)],[(9,3),(14,1),(14,23),(9,23)],[(16,6),(22,3),(22,23),(16,23)]],
 'ice_ramp':[[(1,20),(6,20),(6,15),(11,15),(11,10),(16,10),(16,5),(23,1),(23,23),(1,23)]],
 'flame_sweep':[[(1,20),(4,11),(8,17),(12,2),(16,14),(21,7),(23,20),(18,23),(5,23)]],
 'flashfreeze':[[(10,1),(14,1),(14,8),(20,4),(23,7),(16,12),(23,17),(20,20),(14,16),(14,23),(10,23),(10,16),(4,20),(1,17),(8,12),(1,7),(4,4),(10,8)]],
 'thermal_shock':[[(2,5),(10,1),(10,23),(2,18),(6,12)],[(14,1),(21,7),(18,12),(23,19),(14,23)]],
 'flashfreeze_heatwave':[[(1,6),(7,2),(11,11),(7,22),(1,17),(5,12)],[(13,1),(19,7),(18,12),(23,17),(21,23),(13,19)]],
 'harden':[[(3,5),(12,1),(21,5),(20,15),(12,23),(4,15)],[(8,8),(16,8),(15,13),(12,17),(9,13)]],
 'braced_step':[[(2,20),(3,14),(9,14),(9,4),(15,4),(15,17),(22,19),(22,23),(2,23)]],
 'hardened_strike':[[(2,9),(6,5),(10,7),(13,3),(17,5),(22,11),(20,18),(12,22),(5,18)]],
 'counter_guard':[[(2,4),(9,1),(10,6),(5,9),(10,13),(10,20),(2,14)],[(14,4),(22,10),(22,16),(17,20),(13,17),(18,13),(14,10)]],
 'stone_anchor':[[(9,1),(15,1),(15,15),(21,11),(23,15),(12,23),(1,15),(3,11),(9,15)]],
 'breakline':[[(1,3),(9,3),(7,10),(12,13),(8,21),(1,21)],[(14,3),(23,3),(23,21),(13,21),(17,14),(12,10)]],
 'bodyguard_advance':[[(2,6),(10,2),(16,6),(15,16),(10,22),(3,16)],[(17,4),(23,12),(17,20),(17,15),(11,15),(11,9),(17,9)]],
 'unbreakable':[[(1,5),(12,0),(23,5),(21,16),(12,24),(3,16)],[(6,8),(12,5),(18,8),(17,14),(12,19),(7,14)],[(10,9),(14,9),(14,14),(10,14)]],
 'spark_touch':[[(13,1),(5,12),(11,12),(8,23),(21,9),(14,9)]],
 'grounding_step':[[(2,17),(7,17),(7,10),(12,10),(12,18),(21,20),(21,23),(2,23)],[(18,1),(13,8),(18,8),(15,15),(23,5),(18,5)]],
 'directional_arc':[[(1,11),(8,11),(6,4),(19,9),(14,12),(23,17),(9,20),(11,14),(1,14)]],
 'static_net':[[(1,3),(4,1),(23,20),(20,23)],[(1,20),(20,1),(23,4),(4,23)],[(9,1),(13,1),(13,23),(9,23)]],
 'charge_capacitor':[[(3,4),(9,4),(9,1),(15,1),(15,4),(21,4),(21,23),(3,23)],[(13,6),(7,14),(12,14),(9,21),(18,11),(13,11)]],
 'chain_discharge':[[(1,3),(7,3),(7,9),(1,9)],[(9,9),(15,9),(15,15),(9,15)],[(17,17),(23,17),(23,23),(17,23)],[(5,7),(12,10),(10,14),(18,17),(17,20),(8,15),(10,12),(4,10)]],
 'grounding_stance':[[(10,1),(14,1),(14,14),(21,14),(21,17),(3,17),(3,14),(10,14)],[(6,19),(18,19),(18,21),(6,21)],[(9,23),(15,23),(15,24),(9,24)]],
 'maximum_discharge':[[(10,1),(14,1),(13,8),(20,4),(23,8),(16,12),(23,18),(19,22),(13,16),(14,23),(10,23),(10,16),(4,22),(1,18),(8,12),(1,8),(4,4),(10,8)]]}
POSES={
 'ice_shot':(-90,0,12,0),'flame_jet':(0,-85,0,-18),'ice_wall':(-105,-55,12,-15),'ice_ramp':(35,0,-18,0),'flame_sweep':(-20,-90,20,-45),'flashfreeze':(-95,-45,45,-30),'thermal_shock':(-80,-80,18,-18),'flashfreeze_heatwave':(-70,-70,50,-50),
 'harden':(-55,-55,45,-45),'braced_step':(-20,-20,-20,20),'hardened_strike':(-100,-30,-8,15),'counter_guard':(-35,-75,40,-25),'stone_anchor':(-30,-30,55,-55),'breakline':(30,30,-25,25),'bodyguard_advance':(-55,-55,-30,30),'unbreakable':(-70,-70,50,-50),
 'spark_touch':(-65,-15,-5,10),'grounding_step':(30,-20,-20,20),'directional_arc':(-90,-20,0,10),'static_net':(-70,-70,55,-55),'charge_capacitor':(-35,-35,25,-25),'chain_discharge':(-80,-60,20,-30),'grounding_stance':(20,20,35,-35),'maximum_discharge':(-145,-145,20,-20)}
def arm(pitch,roll,bend):return dict(pitch=pitch,roll=roll,bend=bend,axis=0)
def main():
    from extra_kit_art import SHAPES as more_shapes,POSES as more_poses
    SHAPES.update(more_shapes);POSES.update(more_poses)
    kits=json.loads((ROOT/'src/main/resources/data/mha/mha/kits.json').read_text());moves=json.loads((ROOT/'src/main/resources/data/mha/mha/moves.json').read_text());ownership={m:k for k in kits for m in k['moves']}
    for m in moves:
        name=m['id']
        if name not in SHAPES:continue
        kit=ownership[name];color=f"#{kit['color']&0xffffff:06x}";polygons=SHAPES[name];im=Image.new('RGBA',(24,24));d=ImageDraw.Draw(im)
        fills=[color if not (i==1 and name in ['harden','unbreakable','charge_capacitor','flame_jet']) else '#111B27' for i in range(len(polygons))]
        for points,fill in zip(polygons,fills):d.polygon(points,fill=fill)
        output=A/f'textures/gui/moves/{name}.png';output.parent.mkdir(parents=True,exist_ok=True);im.save(output)
        svg='<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">'+''.join('<polygon fill="'+fill+'" points="'+' '.join(f'{x},{y}' for x,y in points)+'"/>' for points,fill in zip(polygons,fills))+'</svg>\n'
        source=ART/f'interface/{name}.svg';source.parent.mkdir(parents=True,exist_ok=True);source.write_text(svg)
        r,l,rr,lr=POSES[name];duration=m['startup']+m['active']+m['recovery'];hold=m['shape'] in ['GUARD','CHARGE','GROUND','COOL','STREAM','DECOY'] or name=='eclipse_well';bend=8 if m['shape']=='STREAM' else 65 if hold else 20
        rest={'rightArm':arm(0,0,0),'leftArm':arm(0,0,0),'body':{'pitch':0,'yaw':0}}
        ready={'rightArm':arm(r*.55,rr*.7,55),'leftArm':arm(l*.55,lr*.7,55),'body':{'pitch':-8 if m['slot']=='CAPSTONE' else 0,'yaw':-10 if r!=l else 0}}
        action={'rightArm':arm(r,rr,bend),'leftArm':arm(l,lr,bend),'body':{'pitch':12 if name in ['breakline','bodyguard_advance','stone_anchor'] else 0,'yaw':10 if r!=l else 0}}
        frames=[dict(tick=0,**rest),dict(tick=m['startup'],easing='easeOutQuad',**ready),dict(tick=m['startup']+1,**action)]
        if hold:frames.append(dict(tick=m['startup']+m['active'],**action))
        frames.append(dict(tick=duration,easing='easeInOutSine',**rest))
        anim={'version':3,'name':name,'author':'Rising Heroes project','description':'Original '+kit['name']+' animation; game adaptation.','emote':{'beginTick':0,'endTick':duration,'stopTick':duration+1,'isLoop':False,'degrees':True,'moves':frames}}
        write(A/f'player_animations/{name}.json',anim);write(ART/f'animations/{name}.json',anim)
    for kit in kits:
        if kit['id']=='explosion':continue
        color=f"#{kit['color']&0xffffff:06x}";im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im);d.polygon([(16,1),(28,8),(28,24),(16,31),(4,24),(4,8)],fill='#203447',outline='#F2EDE2',width=2)
        icon=Image.open(A/f"textures/gui/moves/{kit['moves'][0]}.png");im.alpha_composite(icon,(4,4));path=A/f"textures/item/{kit['id']}_token.png";im.save(path)
        write(A/f"models/item/{kit['id']}_token.json",{'parent':'minecraft:item/generated','textures':{'layer0':f"mha:item/{kit['id']}_token"}})
    write(A/'models/block/temporary_ice.json',{'parent':'minecraft:block/cube_all','textures':{'all':'minecraft:block/packed_ice'}})
    write(A/'blockstates/temporary_ice.json',{'variants':{'':{'model':'mha:block/temporary_ice'}}})
    for shape,parent in [('','stairs'),('_inner','inner_stairs'),('_outer','outer_stairs')]:write(A/f'models/block/temporary_ice_stairs{shape}.json',{'parent':'minecraft:block/'+parent,'textures':{k:'minecraft:block/packed_ice' for k in ['bottom','top','side']}})
    variants={}
    for facing,angle in [('east',0),('south',90),('west',180),('north',270)]:
        for half in ['bottom','top']:
            for shape in ['straight','inner_left','inner_right','outer_left','outer_right']:
                suffix='_inner' if shape.startswith('inner') else '_outer' if shape.startswith('outer') else ''
                y=(angle+(270 if shape.endswith('left') and half=='bottom' or shape.endswith('right') and half=='top' else 0))%360
                variants[f'facing={facing},half={half},shape={shape}']={'model':'mha:block/temporary_ice_stairs'+suffix,'x':180 if half=='top' else 0,'y':y,'uvlock':True}
    write(A/'blockstates/temporary_ice_stairs.json',{'variants':variants})
    print(f'Authored {len(SHAPES)} distinct move silhouettes/SVGs and timed animations, {len(kits)-1} additional quirk tokens and temporary-ice models. In-game visual checks remain pending.')
if __name__=='__main__':main()
