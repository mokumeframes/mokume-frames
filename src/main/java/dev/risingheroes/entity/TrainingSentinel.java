package dev.risingheroes.entity;

import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

/** Original exercise robot. Missions owns its bounded movement/attacks; it has no autonomous grief AI. */
public final class TrainingSentinel extends PathfinderMob implements GeoEntity {
    private static final EntityDataAccessor<Boolean> CHARGING=SynchedEntityData.defineId(TrainingSentinel.class,EntityDataSerializers.BOOLEAN);
    private static final RawAnimation IDLE=RawAnimation.begin().thenLoop("idle"),WALK=RawAnimation.begin().thenLoop("walk"),WINDUP=RawAnimation.begin().thenPlay("windup");
    private final AnimatableInstanceCache cache=GeckoLibUtil.createInstanceCache(this);
    public TrainingSentinel(EntityType<? extends PathfinderMob> type,Level level){super(type,level);}
    public static AttributeSupplier.Builder attributes(){return Mob.createMobAttributes().add(Attributes.MAX_HEALTH,20).add(Attributes.MOVEMENT_SPEED,.20).add(Attributes.ATTACK_DAMAGE,2).add(Attributes.ARMOR,4).add(Attributes.KNOCKBACK_RESISTANCE,.3).add(Attributes.FOLLOW_RANGE,16);}
    @Override protected void registerGoals(){}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(CHARGING,false);}
    public void charging(boolean value){entityData.set(CHARGING,value);}
    @Override public AnimatableInstanceCache getAnimatableInstanceCache(){return cache;}
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers){controllers.add(new AnimationController<TrainingSentinel>(this,"motion",2,state->state.setAndContinue(entityData.get(CHARGING)?WINDUP:state.isMoving()?WALK:IDLE)));}
}
