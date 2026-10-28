package dev.risingheroes.client;

import dev.risingheroes.ModSounds;
import dev.risingheroes.Network;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

/** Each startup tell belongs to one server cast. Cancellation never stops another player's cue. */
public final class CastAudio {
    private record Cue(long identity,Entity entity,EntityBoundSoundInstance sound,long expiry) {}
    private static final Map<Integer,Cue> CUES=new HashMap<>();
    public static void receive(Network.Visual msg,Entity entity) {
        var mc=Minecraft.getInstance();var old=CUES.get(msg.entity());
        if(msg.phase()!=0) {
            if(old!=null&&old.identity()==msg.identity()){mc.getSoundManager().stop(old.sound());CUES.remove(msg.entity());}
            return;
        }
        if(old!=null)mc.getSoundManager().stop(old.sound());
        if(CUES.size()>=64)return;
        var sound=new EntityBoundSoundInstance(msg.move().equals("howitzer_impact")?ModSounds.CHARGE.get():ModSounds.TELL.get(),SoundSource.PLAYERS,.55f,1,entity,msg.identity());
        CUES.put(msg.entity(),new Cue(msg.identity(),entity,sound,entity.level().getGameTime()+40));mc.getSoundManager().play(sound);
    }
    public static void tick() {
        var mc=Minecraft.getInstance();CUES.values().removeIf(cue->{
            boolean expired=mc.level==null||cue.entity().level()!=mc.level||cue.entity().isRemoved()||!cue.entity().isAlive()||mc.level.getGameTime()>=cue.expiry();
            if(expired)mc.getSoundManager().stop(cue.sound());return expired;
        });
    }
}
