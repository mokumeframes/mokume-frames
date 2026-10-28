package dev.risingheroes.core;

import java.util.UUID;

/** Durable checkpoints and single-claim rewards. Runtime owns proximity, combat and timers. */
public final class MissionRules {
    public static String unavailable(HeroProfile p,Mission m,long now) {
        if(p.schoolStage<4)return "Complete the academy starter lessons first";
        if(!p.mission.id.isBlank())return "Finish or leave the current encounter first";
        if(!p.activeTrial.isEmpty()||now<p.combatUntil||now<p.exertionUntil)return "Finish your trial and recover before starting an encounter";
        if(!m.prerequisite().isBlank()&&!p.completedMissions.contains(m.prerequisite()))return "Complete "+MissionCatalog.get(m.prerequisite()).name()+" first";
        if(p.licence<m.licenceRequired())return "This assignment requires licence level "+m.licenceRequired();
        return "";
    }
    public static void start(HeroProfile p,String id,long now) {
        var m=MissionCatalog.get(id);String blocked=unavailable(p,m,now);if(!blocked.isBlank())throw new IllegalArgumentException(blocked);
        p.mission=new MissionRun();p.mission.id=id;p.mission.session=UUID.randomUUID().toString();p.mission.step=p.completedMissions.contains(id)?0:Math.min(p.missionCheckpoints.getOrDefault(id,0),m.stages().size()-1);
    }
    public static Mission.Stage stage(HeroProfile p){if(p.mission.id.isBlank()||p.mission.completed)return null;return MissionCatalog.get(p.mission.id).stages().get(p.mission.step);}
    public static boolean witness(HeroProfile p,String session,String type,String role,String evidence) {
        var s=stage(p);
        if(s==null||!p.mission.session.equals(session)||!s.type().equals(type)||!s.role().equals(role)||evidence==null||evidence.isBlank()||evidence.length()>100)return false;
        if(!p.mission.evidence.add(evidence))return false;
        if(p.mission.evidence.size()<s.count())return true;
        QuirkResources.fieldTraining(p,s.type());
        p.mission.evidence.clear();p.mission.step++;p.missionCheckpoints.put(p.mission.id,p.mission.step);
        var m=MissionCatalog.get(p.mission.id);
        if(p.mission.step==m.stages().size()) {
            p.mission.completed=true;
            HeroRules.reward(p,"mission_training:"+m.id()+":"+p.quirk,m.mastery(),0);
            if(p.completedMissions.add(m.id())) {
                p.missionGrades.put(m.id(),Math.max(50,100-p.mission.penalties*10));HeroRules.reward(p,"mission:"+m.id(),0,m.credits());
                p.reputation.compute(m.path(),(key,value)->Math.min(100,(value==null?0:value)+5));
                if(m.path().equals("hero")&&m.grantsLicence()>0)p.licence=Math.max(p.licence,m.grantsLicence());
                if(p.completedMissions.size()%3==0)HeroRules.grantToken(p);
            }
        }
        return true;
    }
    public static void penalize(HeroProfile p){if(!p.mission.id.isBlank()&&!p.mission.completed)p.mission.penalties=Math.min(20,p.mission.penalties+1);}
    public static void leave(HeroProfile p){p.mission=new MissionRun();}
    public static String objective(HeroProfile p) {
        if(p.mission.id.isBlank())return "Choose a campaign assignment at the academy mission board";
        var m=MissionCatalog.get(p.mission.id);var s=stage(p);
        return s==null?m.name()+" complete. Leave the encounter to return safely.":m.name()+" / "+(p.mission.step+1)+" of "+m.stages().size()+": "+s.text();
    }
    public static boolean stableLoadout(HeroProfile p){return !p.mission.id.isBlank();}
}
