package dev.risingheroes.test;

import dev.risingheroes.*;
import dev.risingheroes.core.*;
import dev.risingheroes.server.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("mha") @PrefixGameTestTemplate(false)
public final class KitMechanicsTests {
    private static final TicketType<UUID> FIXTURE=TicketType.create("mha_kit_fixture",UUID::compareTo);
    private static ServerPlayer fixture(GameTestHelper h,String kit,int x) {
        var p=TestPlayers.survival(h);Gameplay.ledger(p).commit(p.getUUID(),v->HeroRules.assign(v,kit));
        var level=h.getLevel();var center=new ChunkPos(new BlockPos(x+8,0,8008));level.getChunkSource().addRegionTicket(FIXTURE,center,3,p.getUUID());
        for(int xx=x;xx<x+24;xx++)for(int z=8000;z<8016;z++)level.setBlock(new BlockPos(xx,0,z),Blocks.SMOOTH_STONE.defaultBlockState(),2);
        p.setPos(x+2.5,1,8000.5);p.setNoGravity(true);p.setOnGround(true);p.setYRot(0);p.setXRot(0);return p;
    }
    private static void finish(GameTestHelper h,ServerPlayer p,int x){h.getLevel().getChunkSource().removeRegionTicket(FIXTURE,new ChunkPos(new BlockPos(x+8,0,8008)),3,p.getUUID());}
    @GameTest(template="empty",timeoutTicks=200)
    public static void hardeningReducesRealDamageOnlyDuringItsFiniteWindow(GameTestHelper h) {
        ServerPlayer[] p={null};LivingEntity[] enemy={null};
        h.startSequence().thenExecute(()->p[0]=fixture(h,"hardening",8200)).thenIdle(80).thenExecute(()->{
            enemy[0]=EntityType.HUSK.create(h.getLevel());enemy[0].setPos(8205,1,8005);((Mob)enemy[0]).setNoAi(true);h.getLevel().addFreshEntity(enemy[0]);Gameplay.cast(p[0],0);
        }).thenIdle(6).thenExecute(()->{
            var player=p[0];float before=player.getHealth();player.hurt(player.damageSources().mobAttack(enemy[0]),8);
            h.assertTrue(Math.abs((before-player.getHealth())-5.2)<.02,"Harden did not reduce actual damage by 35%");
            h.assertTrue(QuirkResources.primary(Gameplay.profile(player))>8,"A legitimate impact did not add fatigue");
            h.assertTrue(Gameplay.profile(player).buffUntil>Gameplay.now(player),"The guard was incorrectly interrupted by its first impact");
        }).thenIdle(50).thenExecute(()->{
            var player=p[0];float before=player.getHealth();player.hurt(player.damageSources().mobAttack(enemy[0]),8);
            h.assertTrue(Math.abs((before-player.getHealth())-8)<.02,"Expired hardening still reduced damage");finish(h,player,8200);
        }).thenSucceed();
    }
    @GameTest(template="empty",timeoutTicks=500)
    public static void temporaryIceRefusesOccupiedSpaceAndPreservesLaterBuildingChanges(GameTestHelper h) {
        ServerPlayer[] p={null};LivingEntity[] occupant={null};var center=new BlockPos(8002,1,8003);
        h.startSequence().thenExecute(()->p[0]=fixture(h,"half_cold_half_hot",8000)).thenIdle(10).thenExecute(()->{
            var player=p[0];var move=Catalog.move("ice_wall");h.getLevel().setBlock(center,Blocks.CHEST.defaultBlockState(),2);
            h.assertTrue(TemporaryConstructs.place(player,move)==0&&h.getLevel().getBlockState(center).is(Blocks.CHEST),"Ice replaced a foreign/container block");h.getLevel().setBlock(center,Blocks.AIR.defaultBlockState(),2);
            var cow=EntityType.COW.create(h.getLevel());cow.setPos(8002.5,1,8003.5);cow.setNoAi(true);occupant[0]=cow;h.getLevel().addFreshEntity(cow);
        }).thenWaitUntil(()->h.assertTrue(h.getLevel().getEntity(occupant[0].getUUID())==occupant[0],"Waiting for fixture entity storage to become accessible")).thenExecute(()->{
            var player=p[0];var move=Catalog.move("ice_wall");
            h.assertTrue(TemporaryConstructs.place(player,move)==0,"Ice was placed inside an entity");occupant[0].discard();
            h.assertTrue(TemporaryConstructs.place(player,move)==9,"Clear wall did not place nine bounded blocks");
            h.assertTrue(Gameplay.ledger(player).constructs().stream().filter(c->c.owner().equals(player.getUUID())).count()==9,"Construct placement lacked durable leases");
            h.getLevel().setBlock(center,Blocks.GOLD_BLOCK.defaultBlockState(),2);TemporaryConstructs.clearOwner(player);
            h.assertTrue(h.getLevel().getBlockState(center).is(Blocks.GOLD_BLOCK),"Cleanup overwrote a later building change");h.getLevel().setBlock(center,Blocks.AIR.defaultBlockState(),2);
            h.assertTrue(TemporaryConstructs.place(player,move)==9,"The cleared wall could not be cast again");
        }).thenIdle(285).thenExecute(()->{
            h.assertTrue(h.getLevel().getBlockState(center).isAir(),"Expired ice remained in a loaded chunk");
            h.assertTrue(Gameplay.ledger(p[0]).constructs().stream().noneMatch(c->c.owner().equals(p[0].getUUID())),"Expired construct leases were retained");finish(h,p[0],8000);
        }).thenSucceed();
    }
    @GameTest(template="empty",timeoutTicks=300)
    public static void electricalChainCapsHopsAndCannotCrossSolidCover(GameTestHelper h) {
        ServerPlayer[] p={null};List<LivingEntity> targets=new ArrayList<>();
        h.startSequence().thenExecute(()->p[0]=fixture(h,"electrification",8100)).thenIdle(10).thenExecute(()->{
            for(int i=0;i<4;i++){var mob=EntityType.HUSK.create(h.getLevel());mob.setPos(8102.5+i*4,1,8004.5);mob.setNoAi(true);h.getLevel().addFreshEntity(mob);targets.add(mob);}
        }).thenWaitUntil(()->h.assertTrue(targets.stream().allMatch(e->h.getLevel().getEntity(e.getUUID())==e),"Waiting for fixture target chunks to expose their entities")).thenExecute(()->{
            var found=DamageResolver.targets(p[0],Catalog.move("chain_discharge"));h.assertTrue(found.size()==3&&found.containsAll(targets.subList(0,3))&&!found.contains(targets.get(3)),"Electrical chain fixture: player="+p[0].position()+" aim="+p[0].getLookAngle()+" found="+found.stream().map(e->e.getId()+"@"+e.position()).toList()+" expected="+targets.stream().map(e->e.getId()+"@"+e.position()).toList());
            for(int y=1;y<=3;y++)h.getLevel().setBlock(new BlockPos(8104,y,8004),Blocks.STONE.defaultBlockState(),2);
            found=DamageResolver.targets(p[0],Catalog.move("chain_discharge"));h.assertTrue(found.equals(List.of(targets.getFirst())),"Electrical chain crossed a solid wall");finish(h,p[0],8100);
        }).thenSucceed();
    }
    @GameTest(template="empty",timeoutTicks=220)
    public static void chargingAndGroundingUseTheServerActiveTimeline(GameTestHelper h) {
        ServerPlayer[] p={null};double[] beforeGround={0};
        h.startSequence().thenExecute(()->{
            p[0]=fixture(h,"electrification",8300);Gameplay.ledger(p[0]).commit(p[0].getUUID(),v->{HeroRules.testingLoadout(v);v.slots.set(0,"charge_capacitor");v.slots.set(4,"grounding_stance");v.resources.put("charge",0.0);v.resources.put("overload",50.0);});
        }).thenIdle(10).thenExecute(()->Gameplay.cast(p[0],0)).thenIdle(8).thenExecute(()->h.assertTrue(QuirkResources.primary(Gameplay.profile(p[0]))<3,"Charge was awarded before the active window"))
        .thenIdle(45).thenExecute(()->{var data=Gameplay.profile(p[0]);h.assertTrue(Math.abs(QuirkResources.primary(data)-31.8)<2,"Charge stance did not deliver its bounded active-window gain");})
        .thenIdle(15).thenExecute(()->{p[0].setOnGround(true);beforeGround[0]=QuirkResources.overload(Gameplay.profile(p[0]));Gameplay.cast(p[0],4);})
        .thenIdle(47).thenExecute(()->{h.assertTrue(QuirkResources.overload(Gameplay.profile(p[0]))<=beforeGround[0]-24.5,"Grounding did not remove its bounded overload amount");finish(h,p[0],8300);}).thenSucceed();
        h.onEachTick(()->{if(p[0]!=null)p[0].setOnGround(true);});
    }
    @GameTest(template="empty",timeoutTicks=220)
    public static void kineticStorageRequiresARealGuardedHostileImpact(GameTestHelper h) {
        ServerPlayer[] p={null};LivingEntity[] enemy={null};double[] stored={0};
        h.startSequence().thenExecute(()->{p[0]=fixture(h,"kinetic_reservoir",8400);enemy[0]=EntityType.HUSK.create(h.getLevel());})
        .thenIdle(80).thenExecute(()->Gameplay.cast(p[0],0)).thenIdle(6).thenExecute(()->{
            var player=p[0];float before=player.getHealth();player.hurt(player.damageSources().mobAttack(enemy[0]),8);
            h.assertTrue(Math.abs((before-player.getHealth())-6.4)<.02,"Absorb Brace did not reduce a hostile physical impact by 20%");stored[0]=QuirkResources.primary(Gameplay.profile(player));h.assertTrue(Math.abs(stored[0]-3.2)<.02,"Legitimate prevented damage did not create bounded storage");
        }).thenIdle(12).thenExecute(()->{
            var player=p[0];player.setHealth(20);player.hurt(player.damageSources().generic(),8);h.assertTrue(Math.abs(player.getHealth()-12)<.02,"Environmental damage was incorrectly absorbed");h.assertTrue(Math.abs(QuirkResources.primary(Gameplay.profile(player))-stored[0])<.02,"Environmental damage charged storage");
        }).thenIdle(12).thenExecute(()->{
            var player=p[0];player.hurt(player.damageSources().playerAttack(player),1);h.assertTrue(Math.abs(QuirkResources.primary(Gameplay.profile(player))-stored[0])<.02,"Self damage charged storage");QuirkRuntime.endContext(player);h.assertTrue(Gameplay.profile(player).buffUntil==0,"Leaving world context retained a guard");finish(h,player,8400);
        }).thenSucceed();
    }
    @GameTest(template="empty",timeoutTicks=220)
    public static void shadowMarksAreOwnerBoundConsumedAndRespectCover(GameTestHelper h) {
        ServerPlayer[] p={null},other={null};LivingEntity[] target={null};
        h.startSequence().thenExecute(()->{p[0]=fixture(h,"umbrakinesis",8500);other[0]=fixture(h,"umbrakinesis",8500);var enemy=EntityType.HUSK.create(h.getLevel());enemy.setPos(8502.5,1,8004.5);enemy.setNoAi(true);h.getLevel().addFreshEntity(enemy);target[0]=enemy;})
        .thenWaitUntil(()->h.assertTrue(h.getLevel().getEntity(target[0].getUUID())==target[0],"Waiting for the shadow target chunk"))
        .thenExecute(()->{h.assertTrue(DamageResolver.hit(p[0],Catalog.move("shadow_lash"))==1,"Shadow Lash did not land");h.assertTrue(UniqueQuirks.marked(p[0],target[0]),"A legitimate hit did not mark the target");h.assertTrue(!UniqueQuirks.marked(other[0],target[0]),"A shadow mark leaked to another owner");boolean denied=false;try{UniqueQuirks.preflight(other[0],Catalog.move("umbral_bind"));}catch(IllegalArgumentException expected){denied=true;}h.assertTrue(denied,"Another player consumed a foreign mark");})
        .thenIdle(12).thenExecute(()->{for(int y=1;y<=3;y++)h.getLevel().setBlock(new BlockPos(8502,y,8002),Blocks.STONE.defaultBlockState(),2);h.assertTrue(DamageResolver.hit(p[0],Catalog.move("umbral_bind"))==0,"A bind crossed solid cover");h.assertTrue(UniqueQuirks.marked(p[0],target[0]),"An obstructed hit consumed its mark");for(int y=1;y<=3;y++)h.getLevel().setBlock(new BlockPos(8502,y,8002),Blocks.AIR.defaultBlockState(),2);h.assertTrue(DamageResolver.hit(p[0],Catalog.move("umbral_bind"))==1,"A visible owner-bound bind failed");h.assertTrue(!UniqueQuirks.marked(p[0],target[0]),"The bind did not consume its mark");h.assertTrue(target[0].hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN),"The bind had no bounded control effect");finish(h,p[0],8500);finish(h,other[0],8500);}).thenSucceed();
    }
    @GameTest(template="empty",timeoutTicks=250)
    public static void shadowDecoyIsFiniteAndRemovedOnContextChange(GameTestHelper h) {
        ServerPlayer[] p={null};net.minecraft.world.entity.decoration.ArmorStand[] decoy={null};
        h.startSequence().thenExecute(()->p[0]=fixture(h,"umbrakinesis",8600)).thenIdle(10).thenExecute(()->UniqueQuirks.activate(p[0],Catalog.move("silhouette_decoy")))
        .thenWaitUntil(()->{var list=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.decoration.ArmorStand.class,p[0].getBoundingBox().inflate(8),e->e.getPersistentData().getBoolean("mha_decoy"));h.assertTrue(list.size()==1,"Waiting for one bounded shadow decoy");decoy[0]=list.getFirst();})
        .thenIdle(85).thenExecute(()->{h.assertTrue(decoy[0].isRemoved(),"The shadow decoy outlived its finite duration");UniqueQuirks.activate(p[0],Catalog.move("silhouette_decoy"));})
        .thenIdle(3).thenExecute(()->{var list=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.decoration.ArmorStand.class,p[0].getBoundingBox().inflate(8),e->e.getPersistentData().getBoolean("mha_decoy"));h.assertTrue(list.size()==1,"Decoy replacement created duplicate entities");QuirkRuntime.endContext(p[0]);h.assertTrue(list.getFirst().isRemoved(),"World-context cleanup retained a shadow decoy");finish(h,p[0],8600);}).thenSucceed();
    }
    @GameTest(template="empty",timeoutTicks=250)
    public static void blueflameWaitsForWindupAndCoolingUsesTheActiveWindow(GameTestHelper h) {
        ServerPlayer[] p={null};LivingEntity[] target={null};
        h.startSequence().thenExecute(()->{p[0]=fixture(h,"blueflame",8700);Gameplay.ledger(p[0]).commit(p[0].getUUID(),v->{HeroRules.testingLoadout(v);v.slots.set(0,"cobalt_stream");v.slots.set(1,"controlled_cooling");});var enemy=EntityType.HUSK.create(h.getLevel());enemy.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(100);enemy.setHealth(100);/* Isolate cast timing from the husk\'s innate armour mitigation. */enemy.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR).setBaseValue(0);enemy.setNoAi(true);enemy.setPos(8702.5,1,8004.5);h.getLevel().addFreshEntity(enemy);target[0]=enemy;})
        .thenWaitUntil(()->h.assertTrue(h.getLevel().getEntity(target[0].getUUID())==target[0],"Waiting for the blueflame target chunk"))
        .thenExecute(()->Gameplay.cast(p[0],0)).thenIdle(6).thenExecute(()->h.assertTrue(target[0].getHealth()==100,"Blueflame damaged a target before windup completed"))
        .thenIdle(8).thenExecute(()->h.assertTrue(target[0].getHealth()<=97,"The active stream did not apply its server-owned hit; health="+target[0].getHealth()+", armor="+target[0].getArmorValue()+", fire="+target[0].isOnFire()+", position="+target[0].position()+", player="+p[0].position()+", aim="+p[0].getLookAngle()+", candidates="+DamageResolver.targets(p[0],Catalog.move("cobalt_stream")).stream().map(e->e.getUUID().toString()).toList()))
        .thenIdle(40).thenExecute(()->{Gameplay.ledger(p[0]).commit(p[0].getUUID(),v->v.resources.put("heat",80.0));Gameplay.cast(p[0],1);})
        .thenIdle(8).thenExecute(()->h.assertTrue(Math.abs(QuirkResources.primary(Gameplay.profile(p[0]))-80)<.02,"Cooling happened before its active window"))
        .thenIdle(44).thenExecute(()->{h.assertTrue(Math.abs(QuirkResources.primary(Gameplay.profile(p[0]))-45)<.9,"Cooling did not remove its bounded 35 heat");finish(h,p[0],8700);}).thenSucceed();
    }
}
