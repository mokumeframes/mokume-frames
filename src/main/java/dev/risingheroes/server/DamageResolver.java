package dev.risingheroes.server;

import dev.risingheroes.MhaConfig;
import dev.risingheroes.core.Move;
import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;

public final class DamageResolver {
    private static boolean eligible(ServerPlayer p,LivingEntity target) {
        if(target==p||!target.isAlive()||target.isSpectator()||target instanceof Villager||target.getPersistentData().getBoolean("mha_decoy"))return false;
        if(target.getPersistentData().getBoolean("mha_actor")&&!Missions.mayDamage(p,target))return false;
        if(target instanceof ArmorStand&&!target.getPersistentData().getBoolean("mha_target"))return false;
        if(target instanceof Player other&&(!MhaConfig.PVP.get()||!p.canHarmPlayer(other)))return false;
        if(!MhaConfig.FRIENDLY_FIRE.get()&&p.isAlliedTo(target))return false;
        return !Campus.protectedAt(p.blockPosition(),p.level())||target.getPersistentData().getBoolean("mha_target");
    }
    private static boolean clear(ServerPlayer p,Vec3 a,Vec3 b){return p.level().clip(new ClipContext(a,b,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p)).getType()==HitResult.Type.MISS;}
    public static List<LivingEntity> targets(ServerPlayer player,Move move) {
        Vec3 eye=player.getEyePosition(),look=player.getLookAngle(),end=eye.add(look.scale(move.range()));
        Vec3 block=player.level().clip(new ClipContext(eye,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player)).getLocation();
        double wallDistance=eye.distanceToSqr(block);boolean ray=Set.of("RAY","CHAIN").contains(move.shape());
        var candidates=player.serverLevel().getEntitiesOfClass(LivingEntity.class,player.getBoundingBox().inflate(move.range()),e->eligible(player,e)).stream().sorted(Comparator.comparingDouble(player::distanceToSqr)).limit(96).toList();
        var targets=new ArrayList<LivingEntity>();
        for(var target:candidates) {
            Vec3 delta=target.getBoundingBox().getCenter().subtract(eye);
            if(delta.lengthSqr()>move.range()*move.range()||!player.hasLineOfSight(target))continue;
            if(ray){var intersection=target.getBoundingBox().inflate(.22).clip(eye,end);if(intersection.isEmpty()||eye.distanceToSqr(intersection.get())>wallDistance)continue;}
            else if(!move.shape().equals("SPHERE")&&delta.normalize().dot(look)<.72)continue;
            targets.add(target);if(targets.size()>=(ray?1:32))break;
        }
        if(move.shape().equals("CHAIN")&&!targets.isEmpty()) {
            var nearby=player.serverLevel().getEntitiesOfClass(LivingEntity.class,player.getBoundingBox().inflate(move.range()+12),e->eligible(player,e)).stream().sorted(Comparator.comparingDouble(player::distanceToSqr)).limit(128).toList();
            while(targets.size()<3){var previous=targets.getLast();var next=nearby.stream().filter(t->!targets.contains(t)&&previous.distanceToSqr(t)<=36&&clear(player,previous.getBoundingBox().getCenter(),t.getBoundingBox().getCenter())).min(Comparator.comparingDouble(previous::distanceToSqr));if(next.isEmpty())break;targets.add(next.get());}
        }
        return targets;
    }
    public static int hit(ServerPlayer player,Move move) {
        int count=0;
        for(var target:targets(player,move)) {
            if(target.getPersistentData().getBoolean("mha_target")){if(!dev.risingheroes.core.MeleeCombo.owns(move.id()))Quests.trainingHit(player,target,move);count++;continue;}
            if(!UniqueQuirks.allowHit(player,target,move))continue;
            if(!target.hurt(player.damageSources().playerAttack(player),move.damage()))continue;count++;
            UniqueQuirks.landed(player,target,move);
            if(move.id().equals("m1_kick"))target.knockback(.35,player.getX()-target.getX(),player.getZ()-target.getZ());
            if(move.id().startsWith("flame_"))target.igniteForSeconds(3);
            if(move.id().equals("breakline"))target.knockback(.7,player.getX()-target.getX(),player.getZ()-target.getZ());
            if(Set.of("stun_grenade","flashfreeze","static_net").contains(move.id())) {
                long now=Gameplay.now(player);if(now<target.getPersistentData().getLong("mha_control_until"))continue;
                int duration=target.getMaxHealth()>40?10:move.id().equals("flashfreeze")?40:20;
                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,duration,move.id().equals("flashfreeze")?1:0));target.getPersistentData().putLong("mha_control_until",now+80);
            }
        }
        return count;
    }
}
