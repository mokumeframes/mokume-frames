package dev.risingheroes.core;

/** All gameplay numbers are server data, sent unchanged to notebook and HUD. Units are ticks/blocks. */
public record Move(String id, String name, String slot, String shape, int startup, int active,
                   int recovery, int stamina, int strain, int cooldown, double range, float damage,
                   int mastery, String trial, String description) {
    public void validate() {
        if(id==null||!id.matches("[a-z_]{3,40}")||name==null||name.length()>64||description==null||description.length()>600)
            throw new IllegalArgumentException("Invalid move identity/text: "+id);
        if(!java.util.Set.of("TECHNIQUE","UTILITY","CAPSTONE").contains(slot)||!java.util.Set.of("CONE","RAY","SPHERE","DASH","DODGE","AIR","VOLLEY","GUARD","STEP","CHARGE","GROUND","WALL","RAMP","CHAIN","STREAM","COOL","DECOY").contains(shape))
            throw new IllegalArgumentException("Invalid slot/shape: "+id);
        if(startup<1||startup>100||active<1||active>200||recovery<1||recovery>100||stamina<0||stamina>100||strain<0||strain>100||cooldown<1||cooldown>2400||range<0||range>32||!Double.isFinite(range)||damage<0||damage>40||!Float.isFinite(damage)||mastery<0||mastery>100)
            throw new IllegalArgumentException("Out-of-range move data: "+id);
    }
}
