package dev.risingheroes.core;

import java.util.*;

/** Durable state only. No client identity, entity IDs or transient summons/poses are saved. */
public final class HeroProfile {
    public int version=4;
    public Map<String,Double> resources=new HashMap<>();
    public long buffUntil=0,counterUntil=0,overloadUntil=0;
    public String buffKind="";
    public MissionRun mission=new MissionRun();
    public Set<String> completedMissions=new HashSet<>();
    public Map<String,Integer> missionCheckpoints=new HashMap<>(),missionGrades=new HashMap<>();
    public String quirk="";
    public Map<String,Integer> masteries=new HashMap<>();
    public Map<String,Set<String>> unlocks=new HashMap<>();
    public Set<String> trials=new HashSet<>();
    public Set<String> rewards=new HashSet<>();
    public Set<String> bookmarks=new HashSet<>();
    public Set<String> liveTokens=new HashSet<>();
    public List<String> offers=new ArrayList<>();
    public String offerIdentity="";
    public List<String> slots=new ArrayList<>(List.of("palm_burst","","","","blast_dash",""));
    public Map<String,Long> cooldowns=new HashMap<>();
    public Map<String,List<String>> loadoutPresets=new HashMap<>();
    public Map<String,Integer> reputation=new HashMap<>(Map.of("hero",0,"villain",0,"independent",0));
    public int rank=1, credits=0, schoolStage=0, courseCheckpoint=0;
    public int licence=0;
    public String allegiance="independent";
    public boolean tutorial=false, revealSeen=false;
    public String activeTrial="";
    public int trialStep=0;
    public long trialDeadline=0;
    public boolean rooftopImpulse=false;
    public double stamina=100, strain=0;
    public long exertionUntil=0, combatUntil=0;
    public String returnDimension="";
    public double returnX=0,returnY=0,returnZ=0;
    public int mastery(){return masteries.getOrDefault(quirk,0);}
    public Set<String> currentUnlocks(){return unlocks.computeIfAbsent(quirk,k->new HashSet<>());}
    public static HeroProfile fromJson(String json){var p=Catalog.JSON.fromJson(json,HeroProfile.class);if(p==null)throw new IllegalArgumentException("Missing player profile");p.migrate();p.validate();return p;}
    public HeroProfile copy(){return fromJson(Catalog.JSON.toJson(this));}
    public void migrate(){if(version==1){activeTrial="";trialStep=0;trialDeadline=0;rooftopImpulse=false;version=2;}if(version==2){mission=new MissionRun();completedMissions=new HashSet<>();missionCheckpoints=new HashMap<>();missionGrades=new HashMap<>();version=3;}if(version==3){resources=new HashMap<>();buffUntil=0;counterUntil=0;overloadUntil=0;buffKind="";version=4;}}
    public void validate() {
        if(version!=4)throw new IllegalArgumentException("Unsupported player-data version "+version+"; preserve file and migrate explicitly");
        if(resources==null||resources.size()>32||buffKind==null||resources.values().stream().anyMatch(v->v==null||!Double.isFinite(v)||v< -100||v>100))throw new IllegalArgumentException("Invalid quirk resources");
        if(mission==null||completedMissions==null||missionCheckpoints==null||missionGrades==null)throw new IllegalArgumentException("Missing campaign data");
        mission.validate();
        for(var entry:missionCheckpoints.entrySet())if(entry.getValue()<0||entry.getValue()>MissionCatalog.get(entry.getKey()).stages().size())throw new IllegalArgumentException("Invalid campaign checkpoint");
        if(activeTrial==null||!Set.of("","precision","aerial","capstone").contains(activeTrial)||trialStep<0||trialStep>12)throw new IllegalArgumentException("Invalid trial checkpoint");
        if(loadoutPresets==null||loadoutPresets.size()>192||loadoutPresets.values().stream().anyMatch(v->v==null||v.size()!=6))throw new IllegalArgumentException("Invalid saved loadout");
        if(quirk==null||slots==null||slots.size()!=6||masteries==null||unlocks==null||trials==null||rewards==null||liveTokens==null||offers==null||offerIdentity==null||cooldowns==null||reputation==null)
            throw new IllegalArgumentException("Incomplete player save");
        if(!Double.isFinite(stamina)||!Double.isFinite(strain)||stamina<0||stamina>100||strain<0||strain>100||rank<1||rank>50||credits<0||schoolStage<0||schoolStage>10||licence<0||licence>2||offers.size()>3||new HashSet<>(offers).size()!=offers.size())
            throw new IllegalArgumentException("Invalid player state");
        for(int value:masteries.values())if(value<0||value>100)throw new IllegalArgumentException("Invalid archived mastery");
    }
}
