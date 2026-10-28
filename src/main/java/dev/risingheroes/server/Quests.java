package dev.risingheroes.server;

import dev.risingheroes.*;
import dev.risingheroes.core.*;
import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid="mha")
public final class Quests {
    private record Aid(long finish,int stage,int target) {}
    private static final Map<UUID,Aid> AID=new HashMap<>();
    public static void clearRuntime(){AID.clear();}
    public static String objective(HeroProfile p) {
        if(!p.mission.id.isBlank())return MissionRules.objective(p);
        if(!p.activeTrial.isEmpty())return TrialRules.objective(p);
        return switch(p.schoolStage){
            case 0->"Enrol: visit Registrar Aki in U.A.'s central hall (39, 38).";
            case 1->"Control: hit three distinct training lanes (78, 20/30/40), or provide first aid to Mika at the city clinic (114, 76).";
            case 2->"Movement: reach the four teal checkpoints in order, starting at (74, 73). Walking, jumping and recoil are valid.";
            case 3->"Field rescue: visit Mika at the nearby city clinic (114, 76), then remain close for five seconds of first aid.";
            default->"Starter training complete. Visit Instructor Sato for technique trials or open the assignment board at the registrar/instructor to begin the connected school, villain or independent route.";
        };
    }
    public static void trainingHit(ServerPlayer player,LivingEntity target,Move move) {
        if(!Campus.isAcademy(player.level())||move==null)return;
        TechniqueTrials.target(player,target,move);
        var data=Gameplay.profile(player);
        if(data.schoolStage!=1)return;
        String lane=target.getPersistentData().getString("mha_lane");
        if(lane.isEmpty()||data.trials.contains(lane))return;
        Gameplay.ledger(player).commit(player.getUUID(),p->{
            p.trials.add(lane);
            if(p.trials.containsAll(List.of("lane_0","lane_1","lane_2"))){p.trials.add("control");QuirkResources.schoolTraining(p,"control");p.schoolStage=2;HeroRules.reward(p,"lesson:control",5,50);}
        });
        Gameplay.sync(player,"");
    }
    @SubscribeEvent public static void interact(PlayerInteractEvent.EntityInteract e) {
        if(!(e.getEntity() instanceof ServerPlayer p)||e.getHand()!=InteractionHand.MAIN_HAND||!Campus.isAcademy(p.level()))return;
        String contact=e.getTarget().getPersistentData().getString("mha_contact");if(contact.isEmpty())return;
        e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);
        if(p.distanceToSqr(e.getTarget())>25)return;
        var data=Gameplay.profile(p);
        if(contact.equals("registrar")&&data.schoolStage==0) {
            Gameplay.ledger(p).commit(p.getUUID(),v->{v.schoolStage=1;v.allegiance="hero";HeroRules.reward(v,"school:enrol",0,25);});
            p.getInventory().add(ModItems.CHEST.toStack());p.getInventory().add(ModItems.BOOTS.toStack());
            Gameplay.error(p,"Enrolled. Your licence is earned through school examinations. Your first training task is in the notebook.");
        }else if(contact.equals("mentor")&&TechniqueTrials.mentor(p)) {
            Gameplay.sync(p,"");
        }else if(contact.equals("civilian")&&(data.schoolStage==1||data.schoolStage==3||data.activeTrial.equals("capstone")&&data.trialStep==0)) {
            AID.putIfAbsent(p.getUUID(),new Aid(Gameplay.now(p)+100,data.schoolStage,e.getTarget().getId()));
            Gameplay.error(p,"First aid: remain within four blocks for five seconds. Leaving or taking damage interrupts treatment.");
        }else {p.displayClientMessage(Component.literal(objective(data)),false);Gameplay.sync(p,"notebook");}
        Gameplay.sync(p,"");
    }
    public static void tick(ServerPlayer p) {
        var data=Gameplay.profile(p);
        if(!Campus.isAcademy(p.level())||!p.isAlive()){AID.remove(p.getUUID());return;}
        TechniqueTrials.tick(p);
        if(data.schoolStage==2) {
            var course=Campus.blueprint(p.server).course();int checkpoint=data.courseCheckpoint;
            if(checkpoint<course.length) {
                var at=course[checkpoint];
                if(p.distanceToSqr(at[0]+.5,at[1],at[2]+.5)<4) {
                    Gameplay.ledger(p).commit(p.getUUID(),v->{v.courseCheckpoint++;if(v.courseCheckpoint>=course.length){v.schoolStage=3;v.trials.add("movement");QuirkResources.schoolTraining(v,"movement");HeroRules.reward(v,"lesson:movement",7,50);}});
                    Gameplay.error(p,"Course checkpoint "+(checkpoint+1)+" / "+course.length);
                }
            }
        }
        var aid=AID.get(p.getUUID());if(aid==null)return;
        var target=p.level().getEntity(aid.target);
        if(target==null||p.distanceToSqr(target)>16||!p.hasLineOfSight(target)||Gameplay.now(p)<data.combatUntil||data.schoolStage!=aid.stage){AID.remove(p.getUUID());return;}
        if(Gameplay.now(p)<aid.finish)return;
        AID.remove(p.getUUID());
        if(aid.stage==4){Gameplay.ledger(p).commit(p.getUUID(),v->TrialRules.aid(v,Gameplay.now(p)));Gameplay.error(p,TrialRules.objective(Gameplay.profile(p)));Gameplay.sync(p,"");return;}
        Gameplay.ledger(p).commit(p.getUUID(),v->{
            if(aid.stage==1){v.trials.add("control");QuirkResources.schoolTraining(v,"control");v.schoolStage=2;HeroRules.reward(v,"lesson:control",5,50);}
            else {v.trials.add("rescue");QuirkResources.schoolTraining(v,"rescue");v.schoolStage=4;v.tutorial=true;if(HeroRules.reward(v,"mission:first_rescue",8,100))HeroRules.grantToken(v);}
            HeroRules.unlockEarned(v);
        });
        if(aid.stage==3)p.getInventory().add(ModItems.boundReroll(p.getUUID()));
        Gameplay.error(p,aid.stage==1?"Control lesson passed through support work. AP Shot earned.":"Civilian treated. Introductory bound reroll token earned; school progress and mastery retained.");
        Gameplay.sync(p,"");
    }
}
