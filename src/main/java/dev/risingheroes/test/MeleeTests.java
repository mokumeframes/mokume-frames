package dev.risingheroes.test;

import dev.risingheroes.core.*;
import dev.risingheroes.server.*;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("mha") @PrefixGameTestTemplate(false)
public final class MeleeTests {
    private static final TicketType<UUID> TICKET=TicketType.create("mha_melee",UUID::compareTo);
    private record Fixture(ServerPlayer player,LivingEntity target,int x){}
    private static Fixture fixture(GameTestHelper h,int x){
        var p=TestPlayers.survival(h);Gameplay.ledger(p).commit(p.getUUID(),v->HeroRules.assign(v,"explosion"));
        // Onboarding gives the notebook into the selected hotbar slot; M1 requires empty hands.
        p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,net.minecraft.world.item.ItemStack.EMPTY);
        p.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,net.minecraft.world.item.ItemStack.EMPTY);
        var level=h.getLevel();level.getChunkSource().addRegionTicket(TICKET,new ChunkPos(new BlockPos(x+8,0,8008)),3,p.getUUID());
        for(int xx=x;xx<x+16;xx++)for(int z=8000;z<8016;z++)level.setBlock(new BlockPos(xx,0,z),Blocks.SMOOTH_STONE.defaultBlockState(),2);
        p.setPos(x+2.5,1,8000.5);p.setNoGravity(true);p.setYRot(0);p.setXRot(0);p.setOnGround(true);
        var mob=EntityType.HUSK.create(level);mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);mob.getAttribute(Attributes.ARMOR).setBaseValue(0);mob.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);mob.setHealth(100);mob.setNoAi(true);mob.setNoGravity(true);mob.setPos(x+2.5,1,8002.5);level.addFreshEntity(mob);return new Fixture(p,mob,x);
    }
    private static void ready(GameTestHelper h,Fixture f){h.assertTrue(h.getLevel().getEntity(f.target.getUUID())==f.target,"Waiting for target storage");}
    private static void finish(GameTestHelper h,Fixture f){BasicCombat.cancel(f.player);f.target.discard();h.getLevel().getChunkSource().removeRegionTicket(TICKET,new ChunkPos(new BlockPos(f.x+8,0,8008)),3,f.player.getUUID());}
    @GameTest(template="empty",timeoutTicks=220)
    public static void threeClicksResolveOneHitPerStrikeAfterWindup(GameTestHelper h){
        Fixture[] f={null};double[] reserved={0};
        h.startSequence().thenExecute(()->f[0]=fixture(h,8900)).thenWaitUntil(()->ready(h,f[0])).thenExecute(()->{
            BasicCombat.start(f[0].player);reserved[0]=Gameplay.profile(f[0].player).stamina;boolean denied=false;try{BasicCombat.start(f[0].player);}catch(IllegalArgumentException e){denied=true;}h.assertTrue(denied,"Spam replaced an active strike");h.assertTrue(Gameplay.profile(f[0].player).stamina==reserved[0],"Rejected input charged stamina");
        }).thenIdle(2).thenExecute(()->h.assertTrue(f[0].target.getHealth()==100,"Jab hit before windup"))
        .thenIdle(3).thenExecute(()->h.assertTrue(Math.abs(f[0].target.getHealth()-97)<.01,"Jab did not hit once"))
        .thenIdle(6).thenExecute(()->BasicCombat.start(f[0].player)).thenIdle(6).thenExecute(()->h.assertTrue(Math.abs(f[0].target.getHealth()-93)<.01,"Cross damage or sequence incorrect"))
        .thenIdle(7).thenExecute(()->BasicCombat.start(f[0].player)).thenIdle(8).thenExecute(()->h.assertTrue(Math.abs(f[0].target.getHealth()-88)<.01,"Kick damage or sequence incorrect"))
        .thenIdle(12).thenExecute(()->{h.assertTrue(Math.abs(f[0].target.getHealth()-88)<.01,"One strike damaged repeatedly");h.assertTrue(Gameplay.profile(f[0].player).mastery()==0,"Unarmed clicks farmed quirk mastery");finish(h,f[0]);}).thenSucceed();
    }
    @GameTest(template="empty",timeoutTicks=150)
    public static void wallsAndContextCancellationPreventMeleeDamage(GameTestHelper h){
        Fixture[] f={null};h.startSequence().thenExecute(()->f[0]=fixture(h,9000)).thenWaitUntil(()->ready(h,f[0])).thenExecute(()->{
            for(int y=1;y<=3;y++)h.getLevel().setBlock(new BlockPos(9002,y,8001),Blocks.STONE.defaultBlockState(),2);BasicCombat.start(f[0].player);
        }).thenIdle(12).thenExecute(()->{h.assertTrue(f[0].target.getHealth()==100,"Jab crossed solid cover");for(int y=1;y<=3;y++)h.getLevel().setBlock(new BlockPos(9002,y,8001),Blocks.AIR.defaultBlockState(),2);BasicCombat.start(f[0].player);QuirkRuntime.endContext(f[0].player);})
        .thenIdle(14).thenExecute(()->{h.assertTrue(f[0].target.getHealth()==100&&!BasicCombat.active(f[0].player),"Cancelled attack still hit");finish(h,f[0]);}).thenSucceed();
    }
    @GameTest(template="empty",timeoutTicks=150)
    public static void separatePlayersCannotAdvanceEachOthersCombo(GameTestHelper h){
        Fixture[] a={null},b={null};h.startSequence().thenExecute(()->{a[0]=fixture(h,9100);b[0]=fixture(h,9200);}).thenWaitUntil(()->{ready(h,a[0]);ready(h,b[0]);})
        .thenExecute(()->BasicCombat.start(a[0].player)).thenIdle(12).thenExecute(()->{BasicCombat.start(a[0].player);BasicCombat.start(b[0].player);})
        .thenIdle(6).thenExecute(()->{h.assertTrue(Math.abs(a[0].target.getHealth()-93)<.01&&Math.abs(b[0].target.getHealth()-97)<.01,"Combo state leaked between players");finish(h,a[0]);finish(h,b[0]);}).thenSucceed();
    }
}
