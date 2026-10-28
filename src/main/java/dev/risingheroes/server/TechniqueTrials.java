package dev.risingheroes.server;

import dev.risingheroes.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public final class TechniqueTrials {
    public static void start(ServerPlayer p,String id){if(!Campus.atMentor(p))throw new IllegalArgumentException("Speak to Instructor Sato at the training annex (75, 52)");Gameplay.ledger(p).commit(p.getUUID(),v->TrialRules.start(v,id,Gameplay.now(p)));Gameplay.error(p,TrialRules.objective(Gameplay.profile(p)));}
    public static void cancel(ServerPlayer p){Gameplay.ledger(p).commit(p.getUUID(),TrialRules::cancel);Gameplay.error(p,"Trial cancelled. Completed training and earned techniques are retained.");}
    public static void target(ServerPlayer p,LivingEntity target,Move move) {
        var data=Gameplay.profile(p);if(data.activeTrial.isEmpty())return;
        String lane=target.getPersistentData().getString("mha_lane");double distance=p.distanceTo(target);
        var next=data.copy();
        if(!TrialRules.target(next,lane,move.id(),distance,Gameplay.now(p)))return;
        Gameplay.ledger(p).commit(p.getUUID(),v->TrialRules.target(v,lane,move.id(),distance,Gameplay.now(p)));
        Gameplay.error(p,Gameplay.profile(p).activeTrial.isEmpty()?"Precision Circuit passed. Burst Volley earned.":TrialRules.objective(Gameplay.profile(p)));
    }
    public static void movement(ServerPlayer p,Move move){
        var data=Gameplay.profile(p);
        if(!data.activeTrial.isEmpty()&&Campus.isAcademy(p.level())&&p.getX()>100&&p.getZ()>25&&!p.onGround()&&(move.id().equals("blast_dash")||move.id().equals("recoil_dodge")))Gameplay.ledger(p).commit(p.getUUID(),v->v.rooftopImpulse=true);
    }
    public static boolean mentor(ServerPlayer p) {
        var data=Gameplay.profile(p);if(!data.activeTrial.equals("capstone")||data.trialStep!=5)return false;
        Gameplay.ledger(p).commit(p.getUUID(),v->TrialRules.finish(v,Gameplay.now(p)));Gameplay.error(p,"Capstone circuit passed. Howitzer Impact earned; school licensing remains a separate qualification.");return true;
    }
    public static void tick(ServerPlayer p) {
        var data=Gameplay.profile(p);if(data.activeTrial.isEmpty())return;
        if(!p.isAlive()||!Campus.isAcademy(p.level())||Gameplay.now(p)>data.trialDeadline){cancel(p);return;}
        if(p.onGround()&&p.distanceToSqr(134.5,10,44.5)<6&&data.rooftopImpulse) {
            var next=data.copy();if(TrialRules.rooftop(next,Gameplay.now(p))){Gameplay.ledger(p).commit(p.getUUID(),v->TrialRules.rooftop(v,Gameplay.now(p)));Gameplay.error(p,Gameplay.profile(p).activeTrial.isEmpty()?"Rooftop Recovery passed. Aerial Redirection earned.":TrialRules.objective(Gameplay.profile(p)));}
        }
    }
}
