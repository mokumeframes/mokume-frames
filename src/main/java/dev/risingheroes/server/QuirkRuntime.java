package dev.risingheroes.server;

import dev.risingheroes.core.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.effect.*;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/** Stateful mechanics live on the server; only final poses/effect notifications go to clients. */
public final class QuirkRuntime {
    private static final ResourceLocation BRACE=ResourceLocation.parse("mha:hardening_brace");
    public static boolean activate(ServerPlayer p,Move m) {
        long now=Gameplay.now(p);
        if(UniqueQuirks.activate(p,m))return true;
        if(m.shape().equals("WALL")||m.shape().equals("RAMP")) {
            try{TemporaryConstructs.place(p,m);}catch(IllegalArgumentException error){Gameplay.error(p,error.getMessage());}return true;
        }
        if(m.shape().equals("GUARD")) {
            Gameplay.ledger(p).commit(p.getUUID(),v->{v.buffKind=m.id();v.buffUntil=now+m.active();});return true;
        }
        if(m.shape().equals("STEP")) {
            Vec3 look=Vec3.directionFromRotation(0,p.getYRot());double sign=m.id().equals("scorched_retreat")?-1:1;p.setDeltaMovement(look.scale(Math.min(1.0,m.range()*.28)*sign).add(0,m.id().equals("stored_vault")?.55:.02,0));p.hurtMarked=true;p.connection.send(new ClientboundSetEntityMotionPacket(p));
            if(m.id().equals("bodyguard_advance"))Gameplay.ledger(p).commit(p.getUUID(),v->{v.buffKind=m.id();v.buffUntil=now+m.recovery();});return true;
        }
        return m.shape().equals("CHARGE")||m.shape().equals("GROUND")||m.shape().equals("COOL");
    }
    public static void sustain(ServerPlayer p,Move m,long age) {
        if(age<m.startup()||age>=m.startup()+m.active())return;
        var data=Gameplay.profile(p);
        if(m.shape().equals("COOL")){data.resources.put("heat",Math.max(0,QuirkResources.primary(data)-35.0/m.active()));return;}
        if((m.shape().equals("STREAM")||m.id().equals("eclipse_well"))&&age>m.startup()&&(age-m.startup())%8==0)Gameplay.pulse(p,m);
        if(m.shape().equals("CHARGE")) {
            data.resources.put("charge",Math.min(100,QuirkResources.primary(data)+30.0/m.active()));
            double load=QuirkResources.overload(data)+10.0/m.active();data.resources.put("overload",Math.min(100,load));
            if(load>100){data.overloadUntil=Gameplay.now(p)+120;data.exertionUntil=Math.max(data.exertionUntil,data.overloadUntil);Gameplay.cancel(p);}
        }else if(m.shape().equals("GROUND")) {
            if(!p.onGround()){Gameplay.cancel(p);return;}data.resources.put("overload",Math.max(0,QuirkResources.overload(data)-25.0/m.active()));
        }
    }
    public static void tick(ServerPlayer p) {
        UniqueQuirks.tick(p);
        var data=Gameplay.profile(p);long now=Gameplay.now(p);
        boolean braced=p.isAlive()&&data.quirk.equals("hardening")&&now<data.buffUntil;
        double knock=braced&&data.buffKind.equals("stone_anchor")?.55:braced&&data.buffKind.equals("unbreakable")?.3:0;
        double speed=braced&&data.buffKind.equals("stone_anchor")?-.55:braced&&data.buffKind.equals("unbreakable")?-.25:0;
        modifier(p.getAttribute(Attributes.KNOCKBACK_RESISTANCE),knock,AttributeModifier.Operation.ADD_VALUE);
        modifier(p.getAttribute(Attributes.MOVEMENT_SPEED),speed,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        if(now<data.overloadUntil&&now%20==0){p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,25,1,false,true,true));Gameplay.error(p,"OVERLOAD / "+Math.max(1,(data.overloadUntil-now+19)/20)+"s recovery");}
    }
    private static void modifier(AttributeInstance attribute,double amount,AttributeModifier.Operation operation) {
        if(attribute==null)return;var old=attribute.getModifier(BRACE);
        if(old!=null&&(amount==0||old.amount()!=amount||old.operation()!=operation))attribute.removeModifier(BRACE);
        if(amount!=0&&attribute.getModifier(BRACE)==null)attribute.addTransientModifier(new AttributeModifier(BRACE,amount,operation));
    }
    public static boolean incoming(ServerPlayer p,LivingIncomingDamageEvent e) {
        var data=Gameplay.profile(p);long now=Gameplay.now(p);
        if(now<data.buffUntil&&data.quirk.equals("umbrakinesis")&&data.buffKind.equals("dusk_mantle")&&e.getSource().getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile){e.setAmount(e.getAmount()*.65f);return true;}
        if(now<data.buffUntil&&data.quirk.equals("kinetic_reservoir")) {
            var source=e.getSource();var attacker=source.getEntity();boolean physical=source.is(net.minecraft.world.damagesource.DamageTypes.MOB_ATTACK)||source.is(net.minecraft.world.damagesource.DamageTypes.MOB_ATTACK_NO_AGGRO)||source.is(net.minecraft.world.damagesource.DamageTypes.PLAYER_ATTACK)||source.is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE);
            if(!physical||!(attacker instanceof LivingEntity)||attacker==p||attacker.isAlliedTo(p)||attacker instanceof net.minecraft.world.entity.player.Player&&(!dev.risingheroes.MhaConfig.PVP.get()||!p.canHarmPlayer((net.minecraft.world.entity.player.Player)attacker)))return false;
            var blocked=ImpactStorage.block(data.buffKind,e.getAmount(),QuirkResources.primary(data),QuirkResources.value(data,"kinetic_fatigue",0));
            Gameplay.ledger(p).commit(p.getUUID(),v->{v.resources.put("impact",blocked.stored());v.resources.put("kinetic_fatigue",blocked.fatigue());if(blocked.broken()){v.buffUntil=0;v.buffKind="";}});e.setAmount(blocked.remaining());return !blocked.broken();
        }
        if(!data.quirk.equals("hardening")||now>=data.buffUntil)return false;
        float ratio=data.buffKind.equals("unbreakable")?.5f:.65f;float before=e.getAmount();
        boolean counter=data.buffKind.equals("counter_guard")&&e.getSource().getEntity() instanceof LivingEntity attacker&&attacker!=p&&!attacker.isAlliedTo(p);
        Gameplay.ledger(p).commit(p.getUUID(),v->{double fatigue=Math.min(100,QuirkResources.primary(v)+Math.min(12,before*(1-ratio)*2));v.resources.put("fatigue",fatigue);if(counter)v.counterUntil=now+80;if(fatigue>=100){v.buffUntil=0;v.buffKind="";}});
        e.setAmount(before*ratio);return Gameplay.profile(p).buffUntil>now;
    }
    public static Move attack(ServerPlayer p,Move m) {
        long now=Gameplay.now(p);var data=Gameplay.profile(p);
        if(m.id().equals("hardened_strike")) {
            boolean counter=now<data.counterUntil;Gameplay.ledger(p).commit(p.getUUID(),v->{v.buffKind=m.id();v.buffUntil=now+m.recovery();v.counterUntil=0;});
            if(counter)return new Move(m.id(),m.name(),m.slot(),m.shape(),m.startup(),m.active(),m.recovery(),m.stamina(),m.strain(),m.cooldown(),m.range(),m.damage()+2,m.mastery(),m.trial(),m.description());
        }
        return m;
    }
    public static void changed(ServerPlayer p){UniqueQuirks.clear(p);TemporaryConstructs.clearOwner(p);modifier(p.getAttribute(Attributes.KNOCKBACK_RESISTANCE),0,AttributeModifier.Operation.ADD_VALUE);modifier(p.getAttribute(Attributes.MOVEMENT_SPEED),0,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);}
    public static void endContext(ServerPlayer p){Gameplay.cancel(p);Gameplay.ledger(p).commit(p.getUUID(),v->{v.buffUntil=0;v.buffKind="";v.counterUntil=0;});changed(p);}
}
