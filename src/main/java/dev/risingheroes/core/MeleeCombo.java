package dev.risingheroes.core;

/** Short, bounded unarmed chain. Input never supplies damage, stage, or a target. */
public final class MeleeCombo {
    public static final int RESET_TICKS=26;
    public record Strike(String id,String name,int startup,int recovery,int stamina,float damage,double range) {
        public int duration(){return startup+1+recovery;}
        public Move move(){return new Move(id,name,"BASIC","RAY",startup,1,recovery,stamina,0,duration(),range,damage,0,"","Original unarmed combat technique.");}
    }
    public static final Strike[] STRIKES={
        new Strike("m1_left","Left jab",3,6,3,3,2.8),
        new Strike("m1_right","Right cross",4,7,4,4,2.8),
        new Strike("m1_kick","Front kick",6,11,6,5,3.0)
    };
    public static int next(int previous,long previousEnd,long now){return now<previousEnd||now-previousEnd>RESET_TICKS?0:(previous+1)%STRIKES.length;}
    public static boolean owns(String id){return id.equals("m1_left")||id.equals("m1_right")||id.equals("m1_kick");}
}
