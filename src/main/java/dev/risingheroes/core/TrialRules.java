package dev.risingheroes.core;

import java.util.Set;

/** Checkpoint and reward logic is shared by real mission handlers and headless tests. */
public final class TrialRules {
    public static void start(HeroProfile p,String id,long now) {
        if(MissionRules.stableLoadout(p))throw new IllegalArgumentException("Leave the active assignment before starting a technique trial");
        if(!Set.of("precision","aerial","capstone").contains(id))throw new IllegalArgumentException("Unknown trial");
        if(!p.activeTrial.isEmpty())throw new IllegalArgumentException("A trial is already active; finish or cancel it first");
        if(p.schoolStage<4||!p.quirk.equals("explosion"))throw new IllegalArgumentException("Complete starter training with Explosion before these trials");
        if(now<p.combatUntil||now<p.exertionUntil)throw new IllegalArgumentException("Recover before starting a trial");
        if(p.trials.contains(id.equals("precision")?"advanced_control":id.equals("capstone")?"ultimate":"aerial"))throw new IllegalArgumentException("This trial is already complete");
        if(!id.equals("precision")&&!p.trials.contains("advanced_control"))throw new IllegalArgumentException("Complete the Precision Circuit first");
        if(id.equals("capstone")&&!p.trials.contains("aerial"))throw new IllegalArgumentException("Complete Rooftop Recovery first");
        if(!p.currentUnlocks().contains("ap_shot")||!p.slots.contains("ap_shot"))throw new IllegalArgumentException("Equip AP Shot before starting this trial");
        if(!id.equals("precision")&&!p.slots.contains("blast_dash")&&!p.slots.contains("recoil_dodge"))throw new IllegalArgumentException("Equip Blast Dash or Recoil Dodge for the rooftop section");
        p.activeTrial=id;p.trialStep=0;p.rooftopImpulse=false;p.trialDeadline=now+(id.equals("precision")?900:id.equals("aerial")?1800:3000);
    }
    public static void cancel(HeroProfile p){p.activeTrial="";p.trialStep=0;p.trialDeadline=0;p.rooftopImpulse=false;}
    public static boolean target(HeroProfile p,String lane,String move,double distance,long now) {
        if(p.activeTrial.isEmpty()||now>p.trialDeadline||!move.equals("ap_shot")||distance<8||distance>16)return false;
        boolean precision=p.activeTrial.equals("precision");
        if(!precision&&!(p.activeTrial.equals("capstone")&&p.trialStep>=2&&p.trialStep<=4))return false;
        int wanted=precision?p.trialStep:p.trialStep-2;
        if(!lane.equals("lane_"+wanted))return false;
        p.trialStep++;
        if(precision&&p.trialStep==3){p.trials.add("advanced_control");HeroRules.reward(p,"trial:precision",15,125);cancel(p);}
        return true;
    }
    public static boolean rooftop(HeroProfile p,long now) {
        if(now>p.trialDeadline||!p.rooftopImpulse)return false;
        if(p.activeTrial.equals("aerial")){p.trials.add("aerial");HeroRules.reward(p,"trial:aerial",0,100);cancel(p);return true;}
        if(p.activeTrial.equals("capstone")&&p.trialStep==1){p.trialStep=2;return true;}
        return false;
    }
    public static boolean aid(HeroProfile p,long now) {
        if(!p.activeTrial.equals("capstone")||p.trialStep!=0||now>p.trialDeadline)return false;
        p.trialStep=1;return true;
    }
    public static boolean finish(HeroProfile p,long now) {
        if(!p.activeTrial.equals("capstone")||p.trialStep!=5||now>p.trialDeadline)return false;
        p.trials.add("ultimate");HeroRules.reward(p,"trial:capstone",15,200);cancel(p);return true;
    }
    public static String objective(HeroProfile p) {
        return switch(p.activeTrial){
            case "precision"->"Precision Circuit: hit lane "+(p.trialStep+1)+" with AP Shot from 8–16 blocks. Three lanes in order; 45 seconds.";
            case "aerial"->"Rooftop Recovery: use Blast Dash or Recoil Dodge while airborne in the city, then land on the marked roof pad (134, 10, 44).";
            case "capstone"->switch(p.trialStep){case 0->"Capstone circuit: provide first aid to Mika at the clinic.";case 1->"Capstone circuit: use airborne recoil in the city, then land on the roof pad (134, 10, 44).";case 2,3,4->"Capstone circuit: AP Shot lane "+(p.trialStep-1)+" from 8–16 blocks.";default->"Capstone circuit: report back to Instructor Sato.";};
            default->"No active technique trial.";
        };
    }
}
