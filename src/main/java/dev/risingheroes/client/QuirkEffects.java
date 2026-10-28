package dev.risingheroes.client;

import dev.risingheroes.MhaConfig;
import dev.risingheroes.core.*;
import com.lowdragmc.photon.client.fx.*;
import com.lowdragmc.photon.client.gameobject.emitter.particle.ParticleEmitter;
import com.lowdragmc.photon.client.gameobject.emitter.data.number.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

/** Photon adapter with explicit world/lifetime/global caps. Decorative FX do not decide hits. */
public final class QuirkEffects {
    private record Live(Level level,long expires,AbstractClientPlayer player,int hand,long identity,EntityEffectExecutor effect) {}
    private static final List<Live> LIVE=new ArrayList<>();
    public static void tick() {
        var mc=Minecraft.getInstance();LIVE.removeIf(live->{boolean stop=mc.level!=live.level()||!live.player().isAlive()||live.player().isRemoved()||live.level().getGameTime()>=live.expires();if(stop&&live.effect().getRuntime()!=null)live.effect().getRuntime().destroy(true);else if(!stop)anchor(live.player(),live.hand(),live.effect());return stop;});
    }
    public static void cancel(int entity,long identity){LIVE.removeIf(l->{if(l.player().getId()!=entity||l.identity()!=identity)return false;if(l.effect().getRuntime()!=null)l.effect().getRuntime().destroy(true);return true;});}
    private static void anchor(AbstractClientPlayer player,int hand,EntityEffectExecutor effect){
        var look=player.getLookAngle();double yaw=Math.toRadians(player.getYRot());
        effect.setOffset(new Vector3f((float)(look.x*.55-Math.cos(yaw)*hand*.32),(float)(look.y*.55-.35),(float)(look.z*.55-Math.sin(yaw)*hand*.32)));
        effect.setRotation(new org.joml.Quaternionf().rotationTo(new Vector3f(0,1,0),look.toVector3f()));
    }
    public static void burst(AbstractClientPlayer player,String id,long identity) {
        var mc=Minecraft.getInstance();if(mc.level==null||mc.getCameraEntity()==null||player.distanceToSqr(mc.getCameraEntity())>4096||LIVE.size()>=16)return;
        var kit=Kits.owner(id);var move=ClientState.move(id);if(move==null)return;
        boolean reduced=MhaConfig.REDUCED_VFX.get()||player.distanceToSqr(mc.getCameraEntity())>1024;
        int hand=kit.id().equals("half_cold_half_hot")?(id.startsWith("flame_")?-1:1):1;
        boolean both=kit.id().equals("explosion")||kit.id().equals("half_cold_half_hot")&&(id.equals("thermal_shock")||id.equals("flashfreeze_heatwave"));
        emit(player,id,identity,hand,id.startsWith("flame_")?0xffff974f:kit.color(),move,reduced);
        if(both)emit(player,id,identity,-hand,kit.id().equals("half_cold_half_hot")?0xffff974f:kit.color(),move,reduced);
    }
    private static void emit(AbstractClientPlayer player,String id,long identity,int hand,int color,Move move,boolean reduced){
        if(LIVE.size()>=16)return;var mc=Minecraft.getInstance();
        FX fx=new FX();var emitter=new ParticleEmitter();emitter.setName(id+" / "+(hand>0?"right":"left"));
        boolean focused=move.shape().equals("RAY"),large=move.slot().equals("CAPSTONE");
        emitter.config.setDuration(large?5:3);emitter.config.setLooping(false);emitter.config.setMaxParticles(reduced?12:large?48:24);
        emitter.config.setStartLifetime(NumberFunction.constant(large?10:6));emitter.config.setStartSpeed(NumberFunction.constant(focused?.48:.24));
        double size=focused?.065:large?.14:.09;emitter.config.setStartSize(new NumberFunction3(size,size,size));
        emitter.config.setStartColor(NumberFunction.color(color));emitter.config.emission.setEmissionRate(NumberFunction.constant(reduced?2:large?8:5));
        var cone=new com.lowdragmc.photon.client.gameobject.emitter.data.shape.Cone();cone.setAngle(focused?3:large?35:18);cone.setRadius(.08f);emitter.config.shape.setShape(cone);
        fx.getFxData().objects().add(emitter);
        var effect=new EntityEffectExecutor(fx,mc.level,player,EntityEffectExecutor.AutoRotate.NONE);anchor(player,hand,effect);effect.setAllowMulti(true);effect.start();
        LIVE.add(new Live(mc.level,mc.level.getGameTime()+(large?16:10),player,hand,identity,effect));
    }
}
