package dev.risingheroes.core;

/** Called only after the server establishes a real hostile physical/projectile impact. */
public final class ImpactStorage {
    public record Blocked(float remaining,double stored,double fatigue,boolean broken) {}
    public static Blocked block(String guard,float incoming,double stored,double fatigue) {
        if(!Float.isFinite(incoming)||incoming<=0||!Double.isFinite(stored)||!Double.isFinite(fatigue)||stored<0||stored>60||fatigue<0||fatigue>100)throw new IllegalArgumentException("Invalid guarded impact");
        double reduction=switch(guard){case "absorb_brace"->.2;case "damping_guard"->.25;case "kinetic_bulwark"->.4;default->throw new IllegalArgumentException("Unknown kinetic guard");};
        double blocked=incoming*reduction;
        double nextFatigue=Math.min(100,fatigue+Math.min(20,blocked*2+2));
        return new Blocked((float)(incoming-blocked),Math.min(60,stored+Math.min(10,blocked*2)),nextFatigue,nextFatigue>=100);
    }
}
