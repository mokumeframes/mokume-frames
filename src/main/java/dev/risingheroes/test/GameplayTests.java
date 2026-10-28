package dev.risingheroes.test;

import dev.risingheroes.core.*;
import dev.risingheroes.server.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.gametest.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@GameTestHolder("mha") @PrefixGameTestTemplate(false)
public final class GameplayTests {
    private static final net.minecraft.server.level.TicketType<UUID> CONTACT_TEST=net.minecraft.server.level.TicketType.create("mha_contact_test",UUID::compareTo);
    private static Villager contact(GameTestHelper helper,String id){return helper.getLevel().getEntitiesOfClass(Villager.class,new net.minecraft.world.phys.AABB(0,0,0,144,16,104),v->v.getPersistentData().getString("mha_contact").equals(id)).stream().findFirst().orElseThrow(()->new GameTestAssertException("Missing contact "+id));}
    private static void talk(ServerPlayer p,Villager npc){p.setPos(npc.getX(),npc.getY(),npc.getZ()+1);Quests.interact(new PlayerInteractEvent.EntityInteract(p,InteractionHand.MAIN_HAND,npc));}
    private static void precisionTargets(GameTestHelper helper,ServerPlayer p){
        var targets=helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.decoration.ArmorStand.class,new net.minecraft.world.phys.AABB(70,0,10,84,8,48),v->v.getPersistentData().getBoolean("mha_target"));
        for(int i=0;i<3;i++){final String lane="lane_"+i;var target=targets.stream().filter(t->t.getPersistentData().getString("mha_lane").equals(lane)).findFirst().orElseThrow();p.setPos(target.getX()+12,target.getY(),target.getZ()+2);p.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,target.getEyePosition());helper.assertTrue(DamageResolver.targets(p,Catalog.move("ap_shot")).contains(target),"Precision line was obstructed for "+lane);DamageResolver.hit(p,Catalog.move("ap_shot"));}
    }
    private static void rooftop(ServerPlayer p){p.setPos(123.5,5,55.5);p.setOnGround(false);TechniqueTrials.movement(p,Catalog.move("blast_dash"));p.setPos(134.5,10,44.5);p.setOnGround(true);TechniqueTrials.tick(p);}
    @GameTest(template="empty",timeoutTicks=3000)
    public static void schoolStarterAndAllExplosionTrialsAreEarnableWithoutAdminGrants(GameTestHelper helper) {
        var server=helper.getLevel().getServer();ServerPlayer[] player=new ServerPlayer[1];UUID fixture=UUID.randomUUID();
        var chunks=Arrays.stream(Campus.blueprint(server).contacts()).map(c->new net.minecraft.world.level.ChunkPos(BlockPos.containing(c.pos()[0],c.pos()[1],c.pos()[2]))).toList();
        for(var chunk:chunks)helper.getLevel().getChunkSource().addRegionTicket(CONTACT_TEST,chunk,2,fixture);
        helper.startSequence()
            .thenWaitUntil(()->helper.assertTrue(Campus.ready(server),"Waiting for authored academy fixture"))
            .thenExecute(()->{
                var p=TestPlayers.survival(helper);player[0]=p;
                talk(p,contact(helper,"registrar"));talk(p,contact(helper,"registrar"));
                helper.assertTrue(Gameplay.profile(p).schoolStage==1&&Gameplay.profile(p).credits==25,"Enrolment duplicated or failed");
                for(int z:new int[]{20,30,40}){p.setPos(78.5,1,z-2.5);p.setYRot(0);p.setXRot(0);DamageResolver.hit(p,Catalog.move("palm_burst"));}
                helper.assertTrue(Gameplay.profile(p).mastery()==5&&Gameplay.profile(p).currentUnlocks().contains("ap_shot"),"Control targets did not earn AP Shot");
                for(var point:Campus.blueprint(server).course()){p.setPos(point[0]+.5,point[1],point[2]+.5);Quests.tick(p);}
                helper.assertTrue(Gameplay.profile(p).schoolStage==3&&Gameplay.profile(p).mastery()==12,"Course checkpoint progression failed");
                talk(p,contact(helper,"civilian"));
            }).thenIdle(115)
            .thenExecute(()->{
                var p=player[0];helper.assertTrue(Gameplay.profile(p).schoolStage==4&&Gameplay.profile(p).mastery()==20&&Gameplay.profile(p).liveTokens.size()==1,"First aid did not grant exactly one introductory token");
                Gameplay.ledger(p).commit(p.getUUID(),v->HeroRules.equip(v,1,"ap_shot",Gameplay.now(p)));
                p.setPos(75.5,1,53.5);TechniqueTrials.start(p,"precision");precisionTargets(helper,p);
                helper.assertTrue(Gameplay.profile(p).mastery()==35&&Gameplay.profile(p).currentUnlocks().contains("burst_volley"),"Precision Circuit failed");
                p.setPos(75.5,1,53.5);TechniqueTrials.start(p,"aerial");rooftop(p);
                helper.assertTrue(Gameplay.profile(p).currentUnlocks().contains("aerial_redirection"),"Rooftop Recovery failed");
                p.setPos(75.5,1,53.5);TechniqueTrials.start(p,"capstone");talk(p,contact(helper,"civilian"));
            }).thenIdle(115)
            .thenExecute(()->{
                var p=player[0];helper.assertTrue(Gameplay.profile(p).trialStep==1,"Capstone first aid checkpoint failed");
                rooftop(p);precisionTargets(helper,p);talk(p,contact(helper,"mentor"));talk(p,contact(helper,"mentor"));
                var data=Gameplay.profile(p);
                helper.assertTrue(data.mastery()==50&&data.currentUnlocks().containsAll(Kits.get("explosion").moves()),"All eight Explosion actions were not earned");
                helper.assertTrue(data.credits==650&&data.liveTokens.size()==1,"Repeated contact duplicated rewards");
                helper.assertTrue(data.licence==0,"Quirk trials incorrectly bypassed school licensing");
                for(var chunk:chunks)helper.getLevel().getChunkSource().removeRegionTicket(CONTACT_TEST,chunk,2,fixture);
            }).thenSucceed();
    }
    @GameTest(template="empty")
    public static void academyDimensionUsesTheActualMinecraftCodec(GameTestHelper helper) throws Exception {
        try(var reader=helper.getLevel().getServer().getResourceManager().getResourceOrThrow(net.minecraft.resources.ResourceLocation.parse("mha:dimension/academy.json")).openAsReader()){
            var ops=net.minecraft.resources.RegistryOps.create(com.mojang.serialization.JsonOps.INSTANCE,helper.getLevel().registryAccess());
            var stem=net.minecraft.world.level.dimension.LevelStem.CODEC.parse(ops,com.google.gson.JsonParser.parseReader(reader)).getOrThrow();
            helper.assertTrue(stem.generator() instanceof net.minecraft.world.level.levelgen.FlatLevelSource,"Academy generator was not decoded as a flat source");
        }
        helper.succeed();
    }
    @GameTest(template="empty")
    public static void serverAttackUsesRangeAndLineOfSight(GameTestHelper helper) {
        var p=TestPlayers.survival(helper);var at=helper.absolutePos(new BlockPos(2,1,1));p.setPos(at.getX()+.5,at.getY(),at.getZ()+.5);p.setYRot(0);p.setXRot(0);
        var target=helper.spawn(EntityType.HUSK,2,1,3);target.setNoAi(true);
        var move=Catalog.move("palm_burst");
        helper.assertTrue(DamageResolver.targets(p,move).contains(target),"Visible in-range target was not acquired");
        float before=target.getHealth();DamageResolver.hit(p,move);
        helper.assertTrue(target.getHealth()<before,"Server attack did not apply damage");
        helper.setBlock(2,1,2,Blocks.STONE);helper.setBlock(2,2,2,Blocks.STONE);helper.setBlock(2,3,2,Blocks.STONE);
        helper.assertTrue(!DamageResolver.targets(p,move).contains(target),"Attack acquired target through a solid wall");
        helper.succeed();
    }
    @GameTest(template="empty")
    public static void criticalJournalRestoresProgressAndPendingOffers(GameTestHelper helper) throws Exception {
        Path dir=helper.getLevel().getServer().getWorldPath(LevelResource.ROOT).resolve("mha-journal-tests");Files.createDirectories(dir);
        Path file=dir.resolve(UUID.randomUUID()+".json");UUID id=UUID.randomUUID();
        var ledger=HeroLedger.openJournal(file);
        ledger.commit(id,p->{HeroRules.firstAssignment(p,List.of("explosion"),new Random(1));p.masteries.put("explosion",20);p.schoolStage=3;HeroRules.grantToken(p);HeroRules.beginReroll(p,List.of("explosion","synthetic_test_quirk"),100,false,new Random(2));});
        var reloaded=HeroLedger.openJournal(file);var saved=reloaded.profile(id);
        helper.assertTrue(saved.mastery()==20&&saved.schoolStage==3,"Durable mastery or story was lost");
        helper.assertTrue(saved.offers.equals(List.of("synthetic_test_quirk"))&&saved.liveTokens.isEmpty(),"Offers or charged token balance changed on reload");
        boolean rejected=false;try {reloaded.commit(id,p->{p.credits=-1;});}catch(IllegalArgumentException e){rejected=true;}
        helper.assertTrue(rejected&&reloaded.profile(id).credits==0,"Invalid transaction mutated committed state");
        Path blocked=dir.resolve(UUID.randomUUID()+"-blocked");Files.writeString(blocked,"A file cannot be a journal directory");
        var broken=HeroLedger.openJournal(blocked.resolve("profiles.json"));boolean ioRejected=false;
        try {broken.commit(id,p->HeroRules.assign(p,"explosion"));}catch(java.io.UncheckedIOException e){ioRejected=true;}
        helper.assertTrue(ioRejected&&broken.profile(id).quirk.isEmpty(),"Failed disk write awarded an assignment");
        helper.succeed();
    }
    @GameTest(template="empty",timeoutTicks=1600)
    public static void authoredAcademyConstructsAndContainsUsableContacts(GameTestHelper helper) {
        var server=helper.getLevel().getServer();Campus.prepareConstructionTest(helper.getLevel());
        UUID fixture=UUID.randomUUID();
        var chunks=Arrays.stream(Campus.blueprint(server).contacts()).map(c->new net.minecraft.world.level.ChunkPos(BlockPos.containing(c.pos()[0],c.pos()[1],c.pos()[2]))).toList();
        for(var chunk:chunks)helper.getLevel().getChunkSource().addRegionTicket(CONTACT_TEST,chunk,2,fixture);
        helper.succeedWhen(()->{
            helper.assertTrue(Campus.ready(server),"Academy construction status: "+Campus.buildStatus(server));
            var level=helper.getLevel();
            helper.assertTrue(level.getBlockState(new BlockPos(12,2,8)).is(Blocks.WHITE_CONCRETE),"Authored academy wall missing");
            helper.assertTrue(level.getBlockState(new BlockPos(39,1,35)).isAir(),"Central entry is blocked");
            var contacts=level.getEntitiesOfClass(net.minecraft.world.entity.npc.Villager.class,new net.minecraft.world.phys.AABB(0,0,0,144,16,104),v->!v.getPersistentData().getString("mha_contact").isEmpty());
            helper.assertTrue(contacts.size()==3,"Expected three functional contacts, got "+contacts.size());
            for(var chunk:chunks)level.getChunkSource().removeRegionTicket(CONTACT_TEST,chunk,2,fixture);
        });
    }
}
