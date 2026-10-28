"""Original editable exercise robot and GeckoLib 4 exports. Python 3 + Pillow.
Geometry, palette and motion are authored here; no downloaded model/texture is used.
"""
from pathlib import Path
from PIL import Image,ImageDraw
import base64,io,json,uuid
ROOT=Path(__file__).resolve().parents[1];A=ROOT/'src/main/resources/assets/mha'
def uid(label):return str(uuid.uuid5(uuid.NAMESPACE_URL,'mha:training_sentinel:'+label))
def write(path,obj):path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(obj,indent=2)+'\n')
COLORS={'navy':(0,'#203447'),'cream':(32,'#E8E5D9'),'teal':(64,'#55D3BC'),'orange':(96,'#FF974F')}
def cube(origin,size,material):
    x=COLORS[material][0]+4
    return {'origin':origin,'size':size,'uv':{face:{'uv':[x,4],'uv_size':[8,8]} for face in ['north','east','south','west','up','down']}}
def bone(name,pivot,cubes,parent='root'):
    result={'name':name,'pivot':pivot,'cubes':cubes}
    if parent:result['parent']=parent
    return result
def main():
    image=Image.new('RGBA',(128,64),'#111B27');d=ImageDraw.Draw(image)
    for x,color in COLORS.values():
        d.rectangle((x,0,x+31,31),fill=color);d.line((x,0,x+31,0),fill='#F2EDE2');d.rectangle((x+4,36,x+27,58),fill=color)
    texture=A/'textures/entity/training_sentinel.png';texture.parent.mkdir(parents=True,exist_ok=True);image.save(texture)
    bones=[bone('root',[0,0,0],[],None),bone('body',[0,20,0],[cube([-6,18,-4],[12,14,8],'navy'),cube([-7,25,-4.5],[14,6,9],'cream'),cube([-3.5,21,-5.2],[7,6,1],'navy'),cube([-2.3,22,-5.6],[4.6,4,1],'orange'),cube([-6.5,17,-4.5],[13,3,9],'cream')]),bone('head',[0,33,0],[cube([-4,32,-3.5],[8,8,7],'cream'),cube([-4.3,35,-4],[8.6,2,1],'teal'),cube([-2,40,-1.5],[4,1,3],'orange')],'body')]
    for side,sign in [('left',1),('right',-1)]:
        x=7 if sign==1 else -13;lx=1 if sign==1 else -6
        bones.extend([bone(side+'_arm',[9*sign,29,0],[cube([x,22,-3.5],[6,9,7],'navy'),cube([x-.5,27,-4],[7,4,8],'cream'),cube([x-.5,15,-4.5],[7,8,9],'orange')],'body'),bone(side+'_leg',[3.5*sign,18,0],[cube([lx,5,-3],[5,13,6],'navy'),cube([lx-.5,0,-4.5],[6,5,9],'cream'),cube([lx+.5,11,-3.7],[4,3,1],'teal')])])
    geo={'format_version':'1.12.0','minecraft:geometry':[{'description':{'identifier':'geometry.training_sentinel','texture_width':128,'texture_height':64,'visible_bounds_width':3,'visible_bounds_height':3.4,'visible_bounds_offset':[0,1.5,0]},'bones':bones}]}
    write(A/'geo/training_sentinel.geo.json',geo)
    anim={'format_version':'1.8.0','animations':{
      'idle':{'loop':True,'animation_length':2,'bones':{'head':{'rotation':[0,'math.sin(query.anim_time * 180) * 5',0]},'body':{'position':[0,'math.sin(query.anim_time * 180) * 0.25',0]}}},
      'walk':{'loop':True,'animation_length':1,'bones':{'left_leg':{'rotation':['math.sin(query.anim_time * 360) * 25',0,0]},'right_leg':{'rotation':['-math.sin(query.anim_time * 360) * 25',0,0]},'left_arm':{'rotation':['-math.sin(query.anim_time * 360) * 16',0,0]},'right_arm':{'rotation':['math.sin(query.anim_time * 360) * 16',0,0]}}},
      'windup':{'loop':False,'animation_length':1.15,'bones':{'left_arm':{'rotation':{'0.0':[0,0,0],'0.55':[-75,0,-12],'0.95':[-75,0,-12],'1.0':[15,0,4],'1.15':[0,0,0]}},'right_arm':{'rotation':{'0.0':[0,0,0],'0.55':[-75,0,12],'0.95':[-75,0,12],'1.0':[15,0,-4],'1.15':[0,0,0]}},'body':{'rotation':{'0.0':[0,0,0],'0.55':[-8,0,0],'0.95':[-8,0,0],'1.0':[8,0,0],'1.15':[0,0,0]}}}}}}
    write(A/'animations/training_sentinel.animation.json',anim);write(ROOT/'art/animations/training_sentinel.animation.json',anim)
    elements=[];groups={}
    for b in bones:
        children=[]
        for i,c in enumerate(b.get('cubes',[])):
            name=b['name']+'_'+str(i);element={'name':name,'uuid':uid(name),'from':c['origin'],'to':[v+s for v,s in zip(c['origin'],c['size'])],'origin':b['pivot'],'type':'cube','faces':{face:{'uv':v['uv']+[v['uv'][0]+8,v['uv'][1]+8],'texture':0} for face,v in c['uv'].items()}}
            elements.append(element);children.append(element['uuid'])
        groups[b['name']]={'name':b['name'],'uuid':uid(b['name']),'origin':b['pivot'],'children':children}
    outliner=[]
    for b in bones:
        if b.get('parent'):groups[b['parent']]['children'].append(groups[b['name']])
        else:outliner.append(groups[b['name']])
    buffer=io.BytesIO();image.save(buffer,format='PNG')
    project={'meta':{'format_version':'4.10','model_format':'bedrock','box_uv':False},'name':'Rising Heroes / original training sentinel','model_identifier':'training_sentinel','resolution':{'width':128,'height':64},'elements':elements,'outliner':outliner,'textures':[{'name':'training_sentinel.png','uuid':uid('texture'),'id':'0','width':128,'height':64,'source':'data:image/png;base64,'+base64.b64encode(buffer.getvalue()).decode()}],'animations':[]}
    write(ROOT/'art/models/training_sentinel.bbmodel',project)
    print(f'Authored original training sentinel: {len(elements)} cubes, {len(bones)} bones, idle/walk/windup animations, editable Blockbench geometry and embedded palette texture. Visual validation remains pending.')
if __name__=='__main__':main()
