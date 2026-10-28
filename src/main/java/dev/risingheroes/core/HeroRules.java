package dev.risingheroes.core;

import java.util.*;
import java.util.random.RandomGenerator;

/** Pure server rules. Caller commits a copy before sending outcomes or changing the world. */
public final class HeroRules {
    public static boolean firstAssignment(HeroProfile p,List<String> pool,RandomGenerator rng) {
        if(!p.quirk.isEmpty())return false;
        if(pool.isEmpty())throw new IllegalArgumentException("No playable starting quirks enabled; enable Explosion");
        assign(p,pool.get(rng.nextInt(pool.size())));return true;
    }
    public static void assign(HeroProfile p,String quirk) {
        if(MissionRules.stableLoadout(p))throw new IllegalArgumentException("Leave the active assignment before changing quirk");
        if(quirk==null||!quirk.matches("[a-z_]{2,40}"))throw new IllegalArgumentException("Invalid quirk");
        p.quirk=quirk;p.masteries.putIfAbsent(quirk,0);
        var kit=Kits.reference(p);p.currentUnlocks().addAll(kit.starters());
        // Assignment never replenishes resources, cooldowns, rank, credits or school state.
        p.slots=new ArrayList<>(List.of("","","","","",""));int technique=0;
        for(String id:kit.starters()){var move=Catalog.move(id);p.slots.set(move.slot().equals("UTILITY")?4:move.slot().equals("CAPSTONE")?5:technique++,id);}
        p.buffUntil=0;p.buffKind="";p.counterUntil=0;
        p.activeTrial="";p.trialStep=0;p.trialDeadline=0;p.rooftopImpulse=false;
    }
    public static void equip(HeroProfile p,int slot,String move,long now) {
        if(MissionRules.stableLoadout(p))throw new IllegalArgumentException("Leave the active assignment before changing loadout");
        if(!p.activeTrial.isEmpty())throw new IllegalArgumentException("Finish or cancel your current trial before changing loadout");
        if(now<p.combatUntil||now<p.exertionUntil)throw new IllegalArgumentException("Change loadout only out of combat and after recovery");
        if(slot<0||slot>=6)throw new IllegalArgumentException("Invalid loadout slot");
        var m=Catalog.move(move);
        if(!Kits.reference(p).moves().contains(move))throw new IllegalArgumentException("That move belongs to a different quirk");
        if(!p.currentUnlocks().contains(move))throw new IllegalArgumentException("Technique is locked");
        String required=slot==4?"UTILITY":slot==5?"CAPSTONE":"TECHNIQUE";
        if(!m.slot().equals(required))throw new IllegalArgumentException("That technique belongs in a "+m.slot().toLowerCase()+" slot");
        if(p.slots.contains(move)&&!p.slots.get(slot).equals(move))throw new IllegalArgumentException("Technique already equipped");
        p.slots.set(slot,move);
    }
    public static Move reserveCast(HeroProfile p,int slot,long now,boolean active,boolean airborne,boolean wet) {
        if(!Kits.ids().contains(p.quirk))throw new IllegalArgumentException("No implemented quirk is equipped");
        if(active||now<p.exertionUntil)throw new IllegalArgumentException("Finish recovery before another cast");
        if(slot<0||slot>=6)throw new IllegalArgumentException("Invalid slot");
        String id=p.slots.get(slot);
        if(id.isBlank())throw new IllegalArgumentException("That slot is empty. Equip a technique in the notebook");
        var m=Catalog.move(id);
        if(!p.currentUnlocks().contains(id))throw new IllegalArgumentException("Technique is locked");
        if(now<p.cooldowns.getOrDefault(id,0L))throw new IllegalArgumentException("Technique is cooling down");
        QuirkResources.check(p,m,now,airborne,wet);
        if(p.stamina<m.stamina())throw new IllegalArgumentException("Not enough stamina");
        p.stamina-=m.stamina();QuirkResources.reserve(p,m,now);
        p.cooldowns.put(id,now+m.cooldown());p.exertionUntil=Math.max(p.exertionUntil,now+m.startup()+m.active()+m.recovery());
        return m;
    }
    public static void unlockEarned(HeroProfile p) {
        for(String id:Kits.reference(p).moves()){var m=Catalog.move(id);if(p.mastery()>=m.mastery()&&QuirkResources.hasTrial(p,m))p.currentUnlocks().add(m.id());}
    }
    public static void savePreset(HeroProfile p,String number,long now) {
        if(MissionRules.stableLoadout(p))throw new IllegalArgumentException("Leave the active assignment before editing presets");
        if(!Set.of("1","2","3").contains(number))throw new IllegalArgumentException("Choose preset 1, 2 or 3");
        if(now<p.combatUntil||now<p.exertionUntil||!p.activeTrial.isEmpty())throw new IllegalArgumentException("Save a loadout after combat/recovery and outside a trial");
        p.loadoutPresets.put(p.quirk+"#"+number,new ArrayList<>(p.slots));
    }
    public static void loadPreset(HeroProfile p,String number,long now) {
        if(MissionRules.stableLoadout(p))throw new IllegalArgumentException("Leave the active assignment before editing presets");
        if(!Set.of("1","2","3").contains(number))throw new IllegalArgumentException("Choose preset 1, 2 or 3");
        if(now<p.combatUntil||now<p.exertionUntil||!p.activeTrial.isEmpty())throw new IllegalArgumentException("Load a preset after combat/recovery and outside a trial");
        var saved=p.loadoutPresets.get(p.quirk+"#"+number);if(saved==null)throw new IllegalArgumentException("No preset saved in this slot for the current quirk");
        Set<String> used=new HashSet<>();
        for(int i=0;i<6;i++) {
            String id=saved.get(i);if(id.isBlank())continue;var move=Catalog.move(id);
            String expected=i==4?"UTILITY":i==5?"CAPSTONE":"TECHNIQUE";
            if(!p.currentUnlocks().contains(id)||!Kits.reference(p).moves().contains(id)||!move.slot().equals(expected)||!used.add(id))throw new IllegalArgumentException("Preset contains a locked, foreign, duplicate or incorrectly slotted technique; update it first");
        }
        p.slots=new ArrayList<>(saved); // cooldowns and resources are deliberately untouched
    }
    public static boolean reward(HeroProfile p,String identity,int mastery,int credits) {
        if(!p.rewards.add(identity))return false;
        p.masteries.put(p.quirk,Math.min(100,p.mastery()+Math.max(0,mastery)));
        p.credits=Math.addExact(p.credits,Math.max(0,credits));
        p.rank=Math.min(50,1+p.rewards.size()/2);unlockEarned(p);return true;
    }
    public static String grantToken(HeroProfile p) {String id=UUID.randomUUID().toString();p.liveTokens.add(id);return id;}
    public static List<String> beginReroll(HeroProfile p,List<String> pool,long now,boolean busy,Random rng) {
        if(!p.offers.isEmpty())return List.copyOf(p.offers); // reopening can never refresh or charge again
        if(busy||MissionRules.stableLoadout(p)||now<p.combatUntil||now<p.exertionUntil)throw new IllegalArgumentException("Rerolls are unavailable during combat, recovery or a stable-loadout mission");
        var eligible=new ArrayList<>(new LinkedHashSet<>(pool));eligible.remove(p.quirk);
        if(eligible.isEmpty())throw new IllegalArgumentException("No eligible alternative is enabled by this server. Your token was not spent");
        if(p.liveTokens.isEmpty())throw new IllegalArgumentException("Earn a bound Quirk Reroll Token first");
        Collections.shuffle(eligible,rng);
        p.liveTokens.remove(p.liveTokens.iterator().next());
        p.offers=new ArrayList<>(eligible.subList(0,Math.min(3,eligible.size())));p.offerIdentity=UUID.randomUUID().toString();
        return List.copyOf(p.offers);
    }
    public static void chooseReroll(HeroProfile p,String offerId,String choice,long now) {
        if(MissionRules.stableLoadout(p))throw new IllegalArgumentException("Leave the active assignment before changing quirk");
        if(!p.activeTrial.isEmpty())throw new IllegalArgumentException("Finish or cancel the current trial before changing your quirk");
        if(p.offers.isEmpty()||!p.offerIdentity.equals(offerId))throw new IllegalArgumentException("That reroll offer is no longer pending");
        if(now<p.combatUntil||now<p.exertionUntil)throw new IllegalArgumentException("Leave combat before selecting a quirk");
        if(!choice.equals("keep")&&!p.offers.contains(choice))throw new IllegalArgumentException("Choice was not offered");
        if(!choice.equals("keep"))assign(p,choice);
        p.offers.clear();p.offerIdentity="";
    }
    public static boolean mayUseCreativeToken(boolean creative,int permission) {return creative||permission>=2;}
    public static void testingLoadout(HeroProfile p) {
        p.currentUnlocks().addAll(Kits.get(p.quirk).moves());p.masteries.put(p.quirk,100);p.slots=new ArrayList<>(List.of("","","","","",""));int technique=0;
        for(String id:Kits.get(p.quirk).moves()){var m=Catalog.move(id);if(m.slot().equals("UTILITY"))p.slots.set(4,id);else if(m.slot().equals("CAPSTONE"))p.slots.set(5,id);else if(technique<4)p.slots.set(technique++,id);}
    }
}
