package dev.risingheroes.server;

import dev.risingheroes.core.*;
import java.util.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.*;

/** Bounded transient shadow state. No player is hidden, teleported or forced to target anything. */
public final class UniqueQuirks {
    private static final class Shadow {final Map<UUID,Long> marks=new LinkedHashMap<>();ArmorStand decoy;long expiry;}
    private static final Map<UUID,Shadow> SHADOWS=new HashMap<>();
    public static void clear(ServerPlayer player){var state=SHADOWS.remove(player.getUUID());if(state!=null&&state.decoy!=null)state.decoy.discard();}
    public static void clearAll(){SHADOWS.values().forEach(s->{if(s.decoy!=null)s.decoy.discard();});SHADOWS.clear();}
    public static boolean marked(ServerPlayer p,LivingEntity target){var state=SHADOWS.get(p.getUUID());return state!=null&&state.marks.getOrDefault(target.getUUID(),0L)>Gameplay.now(p);}
    public static void preflight(ServerPlayer p,Move m) {
        if(Set.of("umbral_bind","shadow_reel").contains(m.id())&&DamageResolver.targets(p,m).stream().noneMatch(t->marked(p,t)))throw new IllegalArgumentException("First land Shadow Lash, then aim at your marked target with clear sight");
        if(m.shape().equals("DECOY"))decoyPosition(p,m);
    }
    private static Vec3 decoyPosition(ServerPlayer p,Move m) {
        Vec3 forward=Vec3.directionFromRotation(0,p.getYRot()),at=p.position().add(forward.scale(m.range()));
        var pos=net.minecraft.core.BlockPos.containing(at);var box=new AABB(at.x-.3,at.y,at.z-.3,at.x+.3,at.y+1.9,at.z+.3);var level=p.serverLevel();
        if(!level.hasChunkAt(pos)||!level.getWorldBorder().isWithinBounds(pos)||Campus.protectedAt(pos,level)||!level.getBlockState(pos.below()).isCollisionShapeFullBlock(level,pos.below())||!level.noCollision(box)||!level.getEntities(p,box,e->e.isAlive()).isEmpty()||level.clip(new net.minecraft.world.level.ClipContext(p.getEyePosition(),at.add(0,1,0),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,p)).getType()!=HitResult.Type.MISS)throw new IllegalArgumentException("A shadow decoy needs visible, clear, supported and unprotected space");
        return at;
    }
    public static boolean activate(ServerPlayer p,Move m) {
        if(!m.shape().equals("DECOY"))return false;
        Vec3 at;try{at=decoyPosition(p,m);}catch(IllegalArgumentException ex){Gameplay.error(p,ex.getMessage());return true;}
        var state=SHADOWS.computeIfAbsent(p.getUUID(),id->new Shadow());if(state.decoy!=null)state.decoy.discard();
        var decoy=new ArmorStand(p.serverLevel(),at.x,at.y,at.z);decoy.setNoGravity(true);decoy.setInvulnerable(true);decoy.setShowArms(true);decoy.setYRot(p.getYRot());decoy.setCustomName(net.minecraft.network.chat.Component.literal("Shadow Decoy / mod-original"));decoy.setCustomNameVisible(true);decoy.getPersistentData().putBoolean("mha_decoy",true);
        for(var entry:Map.of(EquipmentSlot.HEAD,Items.LEATHER_HELMET,EquipmentSlot.CHEST,Items.LEATHER_CHESTPLATE,EquipmentSlot.LEGS,Items.LEATHER_LEGGINGS,EquipmentSlot.FEET,Items.LEATHER_BOOTS).entrySet()){var stack=new ItemStack(entry.getValue());stack.set(net.minecraft.core.component.DataComponents.DYED_COLOR,new net.minecraft.world.item.component.DyedItemColor(0x332448,false));decoy.setItemSlot(entry.getKey(),stack);}
        p.serverLevel().addFreshEntity(decoy);state.decoy=decoy;state.expiry=Gameplay.now(p)+m.active();
        p.serverLevel().getEntitiesOfClass(Mob.class,decoy.getBoundingBox().inflate(12),mob->mob instanceof Enemy&&!mob.getPersistentData().getBoolean("mha_actor")&&mob.hasLineOfSight(decoy)&&p.hasLineOfSight(mob)).stream().sorted(Comparator.comparingDouble(decoy::distanceToSqr)).limit(8).forEach(mob->mob.setTarget(decoy));return true;
    }
    public static boolean allowHit(ServerPlayer p,LivingEntity target,Move m) {
        if(!Set.of("umbral_bind","shadow_reel").contains(m.id()))return true;
        if(!marked(p,target))return false;SHADOWS.get(p.getUUID()).marks.remove(target.getUUID());return true;
    }
    public static void landed(ServerPlayer p,LivingEntity target,Move m) {
        if(MeleeCombo.owns(m.id()))return;
        long now=Gameplay.now(p);
        if(m.id().equals("shadow_lash")){var state=SHADOWS.computeIfAbsent(p.getUUID(),id->new Shadow());state.marks.remove(target.getUUID());while(state.marks.size()>=4)state.marks.remove(state.marks.keySet().iterator().next());state.marks.put(target.getUUID(),now+120);}
        if(m.id().equals("shadow_reel")){var delta=p.position().subtract(target.position()).multiply(1,0,1);if(delta.lengthSqr()>2.25){target.setDeltaMovement(delta.normalize().scale(target.getMaxHealth()>40?.12:.45).add(0,.08,0));target.hurtMarked=true;}}
        if(Set.of("umbral_bind","eclipse_well").contains(m.id())&&now>=target.getPersistentData().getLong("mha_control_until")){target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,target.getMaxHealth()>40?10:30,1));target.getPersistentData().putLong("mha_control_until",now+80);}
        if(Set.of("impact_wave","singular_release").contains(m.id()))target.knockback(.6,p.getX()-target.getX(),p.getZ()-target.getZ());
        if(Kits.owner(m.id()).id().equals("blueflame"))target.igniteForSeconds(2);
    }
    public static void tick(ServerPlayer p) {
        long now=Gameplay.now(p);var data=Gameplay.profile(p);
        if(data.quirk.equals("umbrakinesis"))data.resources.put("ambient_light",(double)p.level().getMaxLocalRawBrightness(p.blockPosition()));
        if(data.quirk.equals("kinetic_reservoir")&&now>=data.combatUntil)data.resources.put("impact",Math.max(0,QuirkResources.primary(data)-.06));
        var state=SHADOWS.get(p.getUUID());if(state==null)return;
        state.marks.entrySet().removeIf(e->e.getValue()<=now);
        if(state.decoy!=null&&(now>=state.expiry||!p.isAlive()||state.decoy.level()!=p.level()||!data.quirk.equals("umbrakinesis"))){state.decoy.discard();state.decoy=null;}
        if(now%10==0)for(UUID id:state.marks.keySet()){var target=p.serverLevel().getEntity(id);if(target!=null&&p.hasLineOfSight(target))p.serverLevel().sendParticles(ParticleTypes.WITCH,target.getX(),target.getY()+.2,target.getZ(),2,.2,.05,.2,0);}
        if(state.marks.isEmpty()&&state.decoy==null)SHADOWS.remove(p.getUUID());
    }
}
