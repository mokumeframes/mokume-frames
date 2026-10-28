package dev.risingheroes.test;

import dev.risingheroes.core.*;
import dev.risingheroes.server.*;
import java.util.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("mha") @PrefixGameTestTemplate(false)
public final class CampaignTests {
    private static final net.minecraft.server.level.TicketType<UUID> FIXTURE=net.minecraft.server.level.TicketType.create("mha_campaign_fixture",UUID::compareTo);
    private static void interact(ServerPlayer p,LivingEntity npc){p.setPos(npc.getX()+1,npc.getY(),npc.getZ());Missions.interact(new PlayerInteractEvent.EntityInteract(p,InteractionHand.MAIN_HAND,npc));}
    @GameTest(template="empty",timeoutTicks=48000)
    public static void allThreeCampaignRoutesUseRealEncounterHandlersAndPersistCredentials(GameTestHelper helper) {
        Missions.bindTestLevel(helper.getLevel());ServerPlayer[] player={null};int[] index={0},escortPoint={0},lastStep={-1};String[] lastMission={""};long[] lastProgress={0};
        var route=List.copyOf(MissionCatalog.all());
        helper.onEachTick(()->{
            if(!Campus.ready(helper.getLevel().getServer()))return;
            if(player[0]==null){var p=TestPlayers.survival(helper);player[0]=p;Gameplay.ledger(p).commit(p.getUUID(),v->v.schoolStage=4);lastProgress[0]=Gameplay.now(p);}
            var p=player[0];var data=Gameplay.profile(p);
            helper.assertTrue(p.isAlive(),"Campaign fixture died at "+data.mission.id+" step "+data.mission.step);
            if(index[0]>=route.size()) {
                helper.assertTrue(data.completedMissions.size()==28,"A route failed to record all 28 completions");
                helper.assertTrue(data.licence==2&&data.missionGrades.containsKey("school_provisional_exam"),"School chain did not award durable credentials");
                helper.assertTrue(data.liveTokens.size()==9,"Three-completion research rewards duplicated or went missing");
                var restored=HeroProfile.fromJson(Catalog.JSON.toJson(data));helper.assertTrue(restored.completedMissions.equals(data.completedMissions)&&restored.licence==2,"Campaign state failed JSON round trip");helper.succeed();return;
            }
            var mission=route.get(index[0]);
            if(data.mission.id.isBlank()) {helper.assertTrue(!lastMission[0].equals(mission.id()),"Encounter was unexpectedly cancelled during "+mission.id()+"; inspect the server's placement diagnostic");p.setPos(75.5,1,53.5);Missions.start(p,mission.id());var chunk=new net.minecraft.world.level.ChunkPos(Missions.origin(p).offset(24,0,24));helper.getLevel().getChunkSource().addRegionTicket(FIXTURE,chunk,3,p.getUUID());lastProgress[0]=Gameplay.now(p);return;}
            if(!Missions.prepared(p)){if(Gameplay.now(p)-lastProgress[0]>2000)helper.fail("Encounter construction stalled at "+Missions.buildCursor(p));return;}
            if(data.mission.completed) {
                helper.assertTrue(data.completedMissions.contains(mission.id()),"Completion reward was not journaled");
                if(mission.id().equals("school_internship"))helper.assertTrue(data.licence==0,"Licence granted before the examination");
                if(mission.id().equals("school_provisional_exam"))helper.assertTrue(data.licence==1,"Provisional examination failed to grant level one");
                if(Gameplay.now(p)<data.combatUntil||Gameplay.now(p)<data.exertionUntil)return;
                var chunk=new net.minecraft.world.level.ChunkPos(Missions.origin(p).offset(24,0,24));helper.getLevel().getChunkSource().removeRegionTicket(FIXTURE,chunk,3,p.getUUID());Missions.leave(p);index[0]++;lastProgress[0]=Gameplay.now(p);return;
            }
            var stage=MissionRules.stage(data);if(!lastMission[0].equals(mission.id())||lastStep[0]!=data.mission.step){lastMission[0]=mission.id();lastStep[0]=data.mission.step;escortPoint[0]=0;lastProgress[0]=Gameplay.now(p);dev.risingheroes.RisingHeroes.LOG.info("Campaign verification checkpoint: {} / {}",mission.id(),stage.id());}
            if(Gameplay.now(p)-lastProgress[0]>2800)helper.fail("Objective stalled: "+mission.id()+" / "+stage.id()+" waypoint="+escortPoint[0]+" actors="+Missions.actors(p).stream().map(a->a.getPersistentData().getString("mha_role")+"@"+a.position()).toList());
            var o=Missions.origin(p);var actors=Missions.actors(p);var npc=actors.stream().filter(a->a.getPersistentData().getString("mha_role").equals(stage.role())).findFirst().orElse(null);
            switch(stage.type()) {
                case "MOVE" -> p.setPos(o.getX()+stage.x()+.5,stage.y(),o.getZ()+stage.z()+.5);
                case "INTERACT","AID","COMBAT","BOSS" -> {if(npc!=null)interact(p,npc);}
                case "JUDGMENT" -> {if(npc!=null){p.setPos(npc.getX()+1,npc.getY(),npc.getZ());Missions.decision(p,"rescue_first");}}
                case "DEFEND" -> {double angle=Gameplay.now(p)*.12;p.setPos(o.getX()+stage.x()+.5+2.6*Math.cos(angle),stage.y(),o.getZ()+stage.z()+.5+2.6*Math.sin(angle));}
                case "ESCORT" -> {
                    if(npc==null)return;
                    double[][] points={{12.5,18.5},{22.5,21.5},{27.5,21.5},{30.5,40.5},{39.5,40.5}};int point=Math.min(escortPoint[0],points.length-1);var goal=points[point];
                    Vec3 target=new Vec3(o.getX()+goal[0],1,o.getZ()+goal[1]);Vec3 delta=target.subtract(npc.position());
                    if(delta.horizontalDistance()<2.2&&escortPoint[0]<points.length-1)escortPoint[0]++;
                    Vec3 lead=delta.horizontalDistance()>4?delta.normalize().scale(4):delta;p.setPos(npc.getX()+lead.x,1,npc.getZ()+lead.z);
                }
                default -> helper.fail("Uncovered objective type "+stage.type());
            }
        });
    }
}
