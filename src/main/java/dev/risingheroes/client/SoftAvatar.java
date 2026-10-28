package dev.risingheroes.client;

import com.mojang.blaze3d.vertex.*;
import dev.risingheroes.MhaConfig;
import io.github.kosmx.bendylib.*;
import io.github.kosmx.bendylib.impl.*;
import java.util.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

/** Optional skin-preserving rounded surfaces, using Bendy's native mutator and bend solver. */
public final class SoftAvatar {
    private static final Map<ModelPart,MutableCuboid> PARTS=new WeakHashMap<>();
    public static boolean enabled(){return MhaConfig.CLIENT.isLoaded()&&MhaConfig.SOFT_AVATAR.get();}
    public static void install(ModelPart root) {
        for(String name:List.of("head","hat","body","jacket","right_arm","right_sleeve","left_arm","left_sleeve","right_leg","right_pants","left_leg","left_pants")) {
            var part=root.getChild(name);String role=name.equals("head")||name.equals("hat")?"head":name.equals("body")||name.equals("jacket")?"body":"limb";
            ModelPartAccessor.optionalGetCuboid(part,0).ifPresent(mutable->{
                PARTS.put(part,mutable);
                var original=mutable.getCuboidBuilder("bend");
                if(original!=null){mutable.unregisterMutator("bend");mutable.registerMutator("bend",data->build(data,role,original));}
                mutable.registerMutator("mha_soft",data->build(data,role,null));
            });
        }
    }
    public static void before(ModelPart part){var mutable=PARTS.get(part);if(enabled()&&mutable!=null&&mutable.getActiveMutator()==null)mutable.getAndActivateMutator("mha_soft");}
    private static ICuboid build(ICuboidBuilder.Data data,String role,ICuboidBuilder<ICuboid> original) {
        Direction direction=role.equals("body")?Direction.DOWN:Direction.UP;
        var mesh=mesh(data,role);var fallback=original==null?null:original.build(data);
        return new BendableCuboid.Builder().setDirection(direction).build(data,(s,p,minX,minY,minZ,maxX,maxY,maxZ,fixX,fixY,fixZ,direction1,base,other,size)->new Rounded(mesh.sides,mesh.positions,minX,minY,minZ,maxX,maxY,maxZ,fixX,fixY,fixZ,direction1,base,other,size,fallback));
    }
    private record Mesh(BendableCuboid.Quad[] sides,RememberingPos[] positions){}
    private static Mesh mesh(ICuboidBuilder.Data d,String role) {
        float x=d.x-d.extraX,y=d.y-d.extraY,z=d.z-d.extraZ,X=d.x+d.sizeX+d.extraX,Y=d.y+d.sizeY+d.extraY,Z=d.z+d.sizeZ+d.extraZ;
        float[][][] faces={{{X,y,Z},{x,y,Z},{x,y,z},{X,y,z}},{{X,Y,z},{x,Y,z},{x,Y,Z},{X,Y,Z}},{{x,y,z},{x,y,Z},{x,Y,Z},{x,Y,z}},{{X,y,z},{x,y,z},{x,Y,z},{X,Y,z}},{{X,y,Z},{X,y,z},{X,Y,z},{X,Y,Z}},{{x,y,Z},{X,y,Z},{X,Y,Z},{x,Y,Z}}};
        float u=d.u,v=d.v,w=d.sizeX,h=d.sizeY,depth=d.sizeZ;
        float[][] uv={{u+depth,v,u+depth+w,v+depth},{u+depth+w,v+depth,u+depth+2*w,v},{u,v+depth,u+depth,v+depth+h},{u+depth,v+depth,u+depth+w,v+depth+h},{u+depth+w,v+depth,u+2*depth+w,v+depth+h},{u+2*depth+w,v+depth,u+2*depth+2*w,v+depth+h}};
        List<BendableCuboid.Quad> sides=new ArrayList<>();List<RememberingPos> positions=new ArrayList<>();
        int n=4;float radius=Math.min(Math.min((X-x)/2,(Z-z)/2),role.equals("head")?1.65f:1.15f);
        for(int face=0;face<6;face++)for(int a=0;a<n;a++)for(int b=0;b<n;b++) {
            float s0=a/(float)n,s1=(a+1)/(float)n,t0=b/(float)n,t1=(b+1)/(float)n;var corners=faces[face];
            RememberingPos[] points=new RememberingPos[4];float[][] st={{s0,t0},{s1,t0},{s1,t1},{s0,t1}};
            for(int j=0;j<4;j++) {
                float ss=st[j][0],tt=st[j][1];var point=new Vector3f();
                for(int axis=0;axis<3;axis++)point.setComponent(axis,(1-tt)*((1-ss)*corners[0][axis]+ss*corners[1][axis])+tt*((1-ss)*corners[3][axis]+ss*corners[2][axis]));
                var inner=new Vector3f(Math.max(x+radius,Math.min(X-radius,point.x)),Math.max(y+radius,Math.min(Y-radius,point.y)),Math.max(z+radius,Math.min(Z-radius,point.z)));
                var delta=new Vector3f(point).sub(inner);if(delta.lengthSquared()>0)point.set(inner).add(delta.normalize().mul(radius));
                if(role.equals("head")){point.x=(point.x-(x+X)/2)*.9f+(x+X)/2;point.z=(point.z-(z+Z)/2)*.9f+(z+Z)/2;point.y=Y+(point.y-Y)*.9f;}
                else if(role.equals("body")){float t=(point.y-y)/(Y-y);point.x=(point.x-(x+X)/2)*(1-.14f*(float)Math.sin(Math.PI*t))+(x+X)/2;}
                if(d.mirror)point.x=x+X-point.x;
                points[j]=new RememberingPos(point);positions.add(points[j]);
            }
            var rect=uv[face];float left=rect[2]+(rect[0]-rect[2])*s1,right=rect[2]+(rect[0]-rect[2])*s0,top=rect[1]+(rect[3]-rect[1])*t0,bottom=rect[1]+(rect[3]-rect[1])*t1;
            sides.add(new BendableCuboid.Quad(points,left,top,right,bottom,d.textureWidth,d.textureHeight,d.mirror));
        }
        return new Mesh(sides.toArray(BendableCuboid.Quad[]::new),positions.toArray(RememberingPos[]::new));
    }
    private static final class Rounded extends BendableCuboid {
        private final ICuboid fallback;
        Rounded(Quad[] sides,RememberingPos[] positions,float minX,float minY,float minZ,float maxX,float maxY,float maxZ,float fixX,float fixY,float fixZ,Direction direction,Plane base,Plane other,float size,ICuboid fallback){super(sides,positions,minX,minY,minZ,maxX,maxY,maxZ,fixX,fixY,fixZ,direction,base,other,size);this.fallback=fallback;}
        @Override public void render(PoseStack.Pose pose,VertexConsumer vertices,int light,int overlay,int color){if(!enabled()&&fallback!=null){if(fallback instanceof BendableCuboid b)b.applyBend(getBendAxis(),getBend());fallback.render(pose,vertices,light,overlay,color);}else super.render(pose,vertices,light,overlay,color);}
    }
}
