package dev.risingheroes.core;

/** Resource arithmetic is pure; the server owns the clock, physics and resulting effects. */
public final class QuirkResources {
    public static double value(HeroProfile p,String key,double fallback){return p.resources.getOrDefault(key,fallback);}
    public static double primary(HeroProfile p){return switch(p.quirk){case "half_cold_half_hot"->value(p,"temperature",0);case "hardening"->value(p,"fatigue",0);case "electrification"->value(p,"charge",100);case "blueflame"->value(p,"heat",0);case "umbrakinesis"->value(p,"shade",100);case "kinetic_reservoir"->value(p,"impact",0);default->p.strain;};}
    public static double cost(HeroProfile p,Move m){return m.strain()*(p.quirk.equals("umbrakinesis")&&value(p,"ambient_light",15)>=12?1.25:1);}
    public static double overload(HeroProfile p){return value(p,"overload",0);}
    public static double thermalDelta(Move m){if(m.id().equals("flashfreeze")||m.id().startsWith("ice_"))return -m.strain();if(m.id().startsWith("flame_"))return m.strain();return 0;}
    public static void check(HeroProfile p,Move m,long now,boolean airborne,boolean wet) {
        if(!Kits.owns(p,m.id()))throw new IllegalArgumentException("That move does not belong to your equipped quirk");
        if(now<p.overloadUntil)throw new IllegalArgumentException("Electrical overload: recover before using demanding abilities");
        if(m.shape().equals("AIR")&&!airborne)throw new IllegalArgumentException("Aerial Redirection requires being airborne");
        if((m.shape().equals("STEP")||m.shape().equals("GROUND"))&&airborne)throw new IllegalArgumentException("This is grounded footwork; land before using it");
        switch(p.quirk) {
            case "explosion" -> {if(wet)throw new IllegalArgumentException("Palm charge is unstable underwater; surface first");if(p.strain+m.strain()>100)throw new IllegalArgumentException("Blast strain is too high; recover first");}
            case "half_cold_half_hot" -> {double heat=primary(p);if(wet&&m.id().startsWith("flame_"))throw new IllegalArgumentException("Fire is unstable underwater; use ice or surface first");if(Math.abs(heat+thermalDelta(m))>100)throw new IllegalArgumentException("Thermal extreme: use the opposite side or recover");if((m.id().equals("thermal_shock")&&Math.abs(heat)>25)||(m.slot().equals("CAPSTONE")&&Math.abs(heat)>40))throw new IllegalArgumentException("Balance your temperature before mixing both sides");}
            case "hardening" -> {if(primary(p)+m.strain()>100)throw new IllegalArgumentException("Hardening fatigue is too high; release your stance and recover");}
            case "electrification" -> {if(wet&&!m.shape().equals("STEP")&&!m.shape().equals("GROUND"))throw new IllegalArgumentException("Surface before electrical discharge");if(primary(p)<m.strain())throw new IllegalArgumentException("Not enough electrical charge");}
            case "blueflame" -> {if(wet&&!m.shape().equals("STEP")&&!m.shape().equals("COOL"))throw new IllegalArgumentException("Surface before using blue flames");if(primary(p)+m.strain()>100)throw new IllegalArgumentException("Blueflame heat is too high; cool down before casting");}
            case "umbrakinesis" -> {if(primary(p)<cost(p,m))throw new IllegalArgumentException("Not enough shade; low light accelerates recovery");}
            case "kinetic_reservoir" -> {if(primary(p)<cost(p,m))throw new IllegalArgumentException("Not enough stored impact; guard a legitimate hostile attack first");if(m.shape().equals("GUARD")&&value(p,"kinetic_fatigue",0)>=92)throw new IllegalArgumentException("Guard fatigue is too high; recover before bracing");}
            default -> throw new IllegalArgumentException("Unknown active kit");
        }
    }
    public static void reserve(HeroProfile p,Move m,long now) {
        switch(p.quirk) {
            case "explosion" -> p.strain+=m.strain();
            case "half_cold_half_hot" -> p.resources.put("temperature",m.id().equals("thermal_shock")||m.slot().equals("CAPSTONE")?0:primary(p)+thermalDelta(m));
            case "hardening" -> p.resources.put("fatigue",primary(p)+m.strain());
            case "blueflame" -> p.resources.put("heat",primary(p)+m.strain());
            case "umbrakinesis" -> p.resources.put("shade",primary(p)-cost(p,m));
            case "kinetic_reservoir" -> {p.resources.put("impact",primary(p)-cost(p,m));if(m.shape().equals("GUARD"))p.resources.put("kinetic_fatigue",Math.min(100,value(p,"kinetic_fatigue",0)+8));}
            case "electrification" -> {
                p.resources.put("charge",primary(p)-m.strain());
                double load=overload(p)+(m.id().equals("maximum_discharge")?70:m.strain()*.65);
                if(m.id().equals("grounding_step"))load=Math.max(0,load-8);
                if(load>100){p.overloadUntil=now+120;p.exertionUntil=Math.max(p.exertionUntil,p.overloadUntil);load=100;}
                p.resources.put("overload",load);
            }
        }
    }
    public static void recover(HeroProfile p) {
        switch(p.quirk) {
            case "explosion" -> p.strain=Math.max(0,p.strain-.16);
            case "half_cold_half_hot" -> {double heat=primary(p);p.resources.put("temperature",Math.copySign(Math.max(0,Math.abs(heat)-.12),heat));}
            case "hardening" -> p.resources.put("fatigue",Math.max(0,primary(p)-.16));
            case "blueflame" -> p.resources.put("heat",Math.max(0,primary(p)-.18));
            case "umbrakinesis" -> p.resources.put("shade",Math.min(100,primary(p)+(value(p,"ambient_light",15)<=7?.24:.08)));
            case "kinetic_reservoir" -> p.resources.put("kinetic_fatigue",Math.max(0,value(p,"kinetic_fatigue",0)-.18));
            case "electrification" -> {p.resources.put("charge",Math.min(100,primary(p)+.18));p.resources.put("overload",Math.max(0,overload(p)-.14));}
        }
    }
    public static void fieldTraining(HeroProfile p,String objectiveType) {
        if(p.quirk.equals("explosion"))return;
        String milestone=switch(objectiveType){case "COMBAT","JUDGMENT"->"field_control";case "MOVE","ESCORT"->"field_movement";case "AID"->"field_rescue";case "DEFEND"->"field_defence";case "BOSS"->"field_capstone";default->"";};
        if(milestone.isBlank())return;
        if(p.trials.add(p.quirk+":"+milestone))HeroRules.reward(p,"training:"+p.quirk+":"+milestone,milestone.equals("field_defence")||milestone.equals("field_capstone")?7:5,0);
        if(objectiveType.equals("AID")&&p.trials.add(p.quirk+":field_control"))HeroRules.reward(p,"training:"+p.quirk+":field_control",5,0);
        HeroRules.unlockEarned(p);
    }
    public static void schoolTraining(HeroProfile p,String milestone){if(!p.quirk.equals("explosion"))p.trials.add(p.quirk+":field_"+milestone);}
    public static boolean hasTrial(HeroProfile p,Move m){return m.trial().isBlank()||p.trials.contains(m.trial().startsWith("field_")?p.quirk+":"+m.trial():m.trial());}
}
