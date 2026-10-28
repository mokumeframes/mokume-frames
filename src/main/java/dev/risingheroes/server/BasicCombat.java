package dev.risingheroes.server;

import dev.risingheroes.*;
import dev.risingheroes.core.*;
import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

/** Authoritative unarmed timeline. A strike resolves once, after its windup. */
@EventBusSubscriber(modid="mha")
public final class BasicCombat {
    private record Chain(int stage,long end) {}
    private record Active(int stage,long start,long identity,ResourceKey<Level> dimension) {}
    private static final Map<UUID,Chain> CHAINS=new HashMap<>();
    private static final Map<UUID,Active> ACTIVE=new HashMap<>();
    public static boolean active(ServerPlayer p){return ACTIVE.containsKey(p.getUUID());}
    public static boolean eligible(ServerPlayer p){return p.isAlive()&&!p.isSpectator()&&p.getMainHandItem().isEmpty()&&p.getOffhandItem().isEmpty()&&!p.isUsingItem()&&!p.isPassenger()&&!p.isSwimming()&&!p.isFallFlying()&&!p.isSleeping()&&!p.isShiftKeyDown();}
    @SubscribeEvent public static void attack(AttackEntityEvent e){
        if(e.getEntity() instanceof ServerPlayer p&&eligible(p)){e.setCanceled(true);try{start(p);}catch(IllegalArgumentException ignored){}}
    }
    public static void start(ServerPlayer p){
        if(!eligible(p)||Gameplay.casting(p)||active(p))throw new IllegalArgumentException("Finish your current action before fighting");
        long now=Gameplay.now(p);var previous=CHAINS.get(p.getUUID());
        int stage=previous==null?0:MeleeCombo.next(previous.stage,previous.end,now);
        var strike=MeleeCombo.STRIKES[stage];var profile=Gameplay.profile(p);
        if(now<profile.exertionUntil||profile.stamina<strike.stamina())throw new IllegalArgumentException("Recover stamina before attacking");
        Gameplay.ledger(p).commit(p.getUUID(),v->{v.stamina-=strike.stamina();v.exertionUntil=Math.max(v.exertionUntil,now+strike.duration());});
        long identity=p.getRandom().nextLong();ACTIVE.put(p.getUUID(),new Active(stage,now,identity,p.level().dimension()));
        CHAINS.put(p.getUUID(),new Chain(stage,now+strike.duration()));
        Gameplay.visual(p,new Network.Visual(p.getId(),strike.id(),identity,0));
    }
    public static void tick(ServerPlayer p){
        var cast=ACTIVE.get(p.getUUID());if(cast==null)return;
        if(!eligible(p)||p.level().dimension()!=cast.dimension){cancel(p);return;}
        long age=Gameplay.now(p)-cast.start;var strike=MeleeCombo.STRIKES[cast.stage];
        if(age==strike.startup()){
            // RAY targeting selects one visible target and respects existing team/PvP/academy rules.
            int hits=DamageResolver.hit(p,Gameplay.effective(strike.move()));
            if(hits>0)Gameplay.ledger(p).commit(p.getUUID(),v->v.combatUntil=Gameplay.now(p)+100);
            p.serverLevel().playSound(null,p.blockPosition(),hits>0?SoundEvents.PLAYER_ATTACK_STRONG:SoundEvents.PLAYER_ATTACK_SWEEP,SoundSource.PLAYERS,.55f,cast.stage==2?.8f:1.15f);
            Gameplay.visual(p,new Network.Visual(p.getId(),strike.id(),cast.identity,1));
        }
        if(age>=strike.duration()){ACTIVE.remove(p.getUUID());Gameplay.visual(p,new Network.Visual(p.getId(),strike.id(),cast.identity,2));}
    }
    public static void catchUp(ServerPlayer p,ServerPlayer observer){var c=ACTIVE.get(p.getUUID());if(c!=null)net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(observer,new Network.Visual(p.getId(),MeleeCombo.STRIKES[c.stage].id(),c.identity,3,(int)(Gameplay.now(p)-c.start)));}
    public static void cancel(ServerPlayer p){var c=ACTIVE.remove(p.getUUID());CHAINS.remove(p.getUUID());if(c!=null)Gameplay.visual(p,new Network.Visual(p.getId(),MeleeCombo.STRIKES[c.stage].id(),c.identity,2));}
    public static void clear(){ACTIVE.clear();CHAINS.clear();}
}
