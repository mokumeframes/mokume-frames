"""Editable rounded skin mesh + an original preview skin. Mirrors SoftAvatar's surface math.
This exporter is not a claim of an observed Minecraft rendering session.
"""
from pathlib import Path
from PIL import Image,ImageDraw
import json,math,uuid,base64,io
ROOT=Path(__file__).resolve().parents[1]
def write(p,obj):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(obj,separators=(',',':'))+'\n',encoding='utf-8')

def surface(origin,size,role):
    x,y,z=origin;w,h,d=size;X,Y,Z=x+w,y+h,z+d
    faces=[[[X,y,Z],[x,y,Z],[x,y,z],[X,y,z]],[[X,Y,z],[x,Y,z],[x,Y,Z],[X,Y,Z]],[[x,y,z],[x,y,Z],[x,Y,Z],[x,Y,z]],[[X,y,z],[x,y,z],[x,Y,z],[X,Y,z]],[[X,y,Z],[X,y,z],[X,Y,z],[X,Y,Z]],[[x,y,Z],[X,y,Z],[X,Y,Z],[x,Y,Z]]]
    radius=min(w/2,d/2,1.65 if role=='head' else 1.15)
    rows=[]
    for fi,c in enumerate(faces):
      for a in range(4):
       for b in range(4):
        points=[]
        for s,t in [(a/4,b/4),((a+1)/4,b/4),((a+1)/4,(b+1)/4),(a/4,(b+1)/4)]:
            p=[(1-t)*((1-s)*c[0][i]+s*c[1][i])+t*((1-s)*c[3][i]+s*c[2][i]) for i in range(3)]
            inner=[max(lo+radius,min(hi-radius,v)) for lo,hi,v in zip([x,y,z],[X,Y,Z],p)];delta=[p[i]-inner[i] for i in range(3)];length=math.sqrt(sum(q*q for q in delta))
            if length:p=[inner[i]+delta[i]/length*radius for i in range(3)]
            if role=='head':p=[(p[0]-(x+X)/2)*.9+(x+X)/2,Y+(p[1]-Y)*.9,(p[2]-(z+Z)/2)*.9+(z+Z)/2]
            elif role=='body':p[0]=(p[0]-(x+X)/2)*(1-.14*math.sin(math.pi*(p[1]-y)/h))+(x+X)/2
            points.append([round(v,5) for v in p])
        rows.append(dict(face=fi,s=a/4,t=b/4,points=points))
    return rows

def main():
    skin=Image.new('RGBA',(64,64),'#172635');draw=ImageDraw.Draw(skin)
    draw.rectangle((0,0,31,15),fill='#c9a78e');draw.rectangle((0,0,31,7),fill='#263642')
    for x in [0,8,16,24]:draw.rectangle((x,8,x+7,9),fill='#263642')
    draw.rectangle((9,11,10,12),fill='#f1f1e5');draw.rectangle((13,11,14,12),fill='#f1f1e5');draw.point((10,11),fill='#40bbee');draw.point((13,11),fill='#40bbee');draw.line((11,14,13,14),fill='#72594d')
    draw.rectangle((20,20,27,31),fill='#d8e0dc');draw.rectangle((20,20,21,31),fill='#263f4e');draw.rectangle((26,20,27,31),fill='#263f4e');draw.rectangle((23,21,24,31),fill='#4ab9d0')
    for x,y in [(44,20),(36,52)]:draw.rectangle((x,y,x+3,y+11),fill='#263f4e');draw.rectangle((x,y+9,x+3,y+11),fill='#c9a78e')
    draw.rectangle((4,20,7,31),fill='#23323e');draw.rectangle((20,52,23,63),fill='#23323e')
    skinpath=ROOT/'art/textures/original_preview_skin.png';skinpath.parent.mkdir(parents=True,exist_ok=True);skin.save(skinpath)
    bio=io.BytesIO();skin.save(bio,format='PNG');texture='data:image/png;base64,'+base64.b64encode(bio.getvalue()).decode()
    parts=[('head',[0,0,0],[-4,-8,-4],[8,8,8],[0,0],'head'),('body',[0,0,0],[-4,0,-2],[8,12,4],[16,16],'body'),('rightArm',[-5,2,0],[-3,-2,-2],[4,12,4],[40,16],'limb'),('leftArm',[5,2,0],[-1,-2,-2],[4,12,4],[32,48],'limb'),('rightLeg',[-1.9,12,0],[-2,0,-2],[4,12,4],[0,16],'limb'),('leftLeg',[1.9,12,0],[-2,0,-2],[4,12,4],[16,48],'limb')]
    exported=[];elements=[];outliner=[]
    for name,pivot,origin,size,uv,role in parts:
        faces=surface(origin,size,role);w,h,d=size;u,v=uv
        rects=[[u+d,v,u+d+w,v+d],[u+d+w,v+d,u+d+2*w,v],[u,v+d,u+d,v+d+h],[u+d,v+d,u+d+w,v+d+h],[u+d+w,v+d,u+2*d+w,v+d+h],[u+2*d+w,v+d,u+2*d+2*w,v+d+h]]
        vertices={};bbfaces={};index=0
        for row in faces:
            rect=rects[row['face']];ss,tt=row['s'],row['t'];coords=[]
            for s,t in [(ss,tt),(ss+.25,tt),(ss+.25,tt+.25),(ss,tt+.25)]:coords.append([rect[2]+(rect[0]-rect[2])*s,rect[1]+(rect[3]-rect[1])*t])
            row['uv']=coords;keys=[]
            for point in row['points']:
                key=f'v{index}';index+=1;keys.append(key);vertices[key]=[round(point[0]+pivot[0],5),round(24-point[1]-pivot[1],5),point[2]+pivot[2]]
            bbfaces[f'f{len(bbfaces)}']=dict(vertices=keys,uv={k:coord for k,coord in zip(keys,coords)},texture=0)
        uid=str(uuid.uuid5(uuid.NAMESPACE_URL,'mha:soft/'+name));group=str(uuid.uuid5(uuid.NAMESPACE_URL,'mha:soft/bone/'+name))
        elements.append(dict(type='mesh',name=name,origin=[pivot[0],24-pivot[1],pivot[2]],rotation=[0,0,0],vertices=vertices,faces=bbfaces,uuid=uid,export=True))
        outliner.append(dict(name=name,origin=[pivot[0],24-pivot[1],pivot[2]],rotation=[0,0,0],uuid=group,children=[uid],export=True))
        exported.append(dict(name=name,pivot=pivot,role=role,faces=faces))
    model=dict(version=1,description='Original rounded skin surface, six primary bones; preview uses standard arm width. Runtime also supports slim/outer layers and native Bendy deformation.',parts=exported,texture=texture)
    write(ROOT/'art/models/soft_player.mesh.json',model)
    write(ROOT/'art/models/soft_player.bbmodel',dict(meta=dict(format_version='4.10',model_format='free',box_uv=False),name='Rising Heroes - softer player surface',resolution=dict(width=64,height=64),elements=elements,outliner=outliner,textures=[dict(name='original_preview_skin.png',id='0',uuid=str(uuid.uuid5(uuid.NAMESPACE_URL,'mha:soft/skin')),source=texture)]))
    preview=ROOT/'design/blueflame';write(preview/'model.json',model)
    write(preview/'animation.json',json.loads((ROOT/'art/animations/bluefire_front.json').read_text()))
    print('Exported 576 rounded quads / six skin bones to native Blockbench, mesh data and preview inputs.')
if __name__=='__main__':main()
