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
    private record Live(Level level,long expires,EntityEffectExecutor effect) {}
    private static final List<Live> LIVE=new ArrayList<>();
    public static void tick() {
        var mc=Minecraft.getInstance();LIVE.removeIf(live->{boolean stop=mc.level!=live.level()||live.level().getGameTime()>=live.expires();if(stop&&live.effect().getRuntime()!=null)live.effect().getRuntime().destroy(true);return stop;});
    }
    public static void burst(AbstractClientPlayer player,String id) {
        var mc=Minecraft.getInstance();if(mc.level==null||mc.getCameraEntity()==null||player.distanceToSqr(mc.getCameraEntity())>4096||LIVE.size()>=16)return;
        var kit=Kits.owner(id);int color=id.startsWith("flame_")?0xffff974f:kit.color();boolean reduced=MhaConfig.REDUCED_VFX.get();
        FX fx=new FX();var emitter=new ParticleEmitter();emitter.setName(kit.name()+" / "+id);emitter.config.setDuration(3);emitter.config.setLooping(false);emitter.config.setMaxParticles(reduced?12:48);emitter.config.setStartLifetime(NumberFunction.constant(8));emitter.config.setStartSpeed(NumberFunction.constant(.24));emitter.config.setStartSize(new NumberFunction3(.10,.10,.10));emitter.config.setStartColor(NumberFunction.color(color));emitter.config.emission.setEmissionRate(NumberFunction.constant(reduced?3:10));fx.getFxData().objects().add(emitter);
        var effect=new EntityEffectExecutor(fx,mc.level,player,EntityEffectExecutor.AutoRotate.NONE);var look=player.getLookAngle();effect.setOffset(new Vector3f((float)(look.x*.65),(float)(look.y*.65-.25),(float)(look.z*.65)));effect.setAllowMulti(true);effect.start();LIVE.add(new Live(mc.level,mc.level.getGameTime()+14,effect));
    }
}
