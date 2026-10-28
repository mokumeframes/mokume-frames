package dev.risingheroes.client.ui;

import dev.risingheroes.core.*;
import java.util.*;

/** Names and capacities come from the resource rules, not inferred abbreviations. */
public final class HudResources {
    public record Meter(String name,double value,int max,boolean signed,boolean warning) {
        public String text(){return name+" "+(int)Math.round(value)+"/"+max;}
    }
    public static List<Meter> of(HeroProfile p){
        var meters=new ArrayList<Meter>();meters.add(new Meter("Stamina",p.stamina,100,false,p.stamina<15));
        double v=QuirkResources.primary(p);
        meters.add(switch(p.quirk){
            case "half_cold_half_hot"->new Meter("Temperature",v,100,true,Math.abs(v)>=75);
            case "hardening"->new Meter("Fatigue",v,100,false,v>=75);
            case "electrification"->new Meter("Charge",v,100,false,v<15);
            case "blueflame"->new Meter("Heat",v,100,false,v>=75);
            case "umbrakinesis"->new Meter("Shade",v,100,false,v<15);
            case "kinetic_reservoir"->new Meter("Stored impact",v,60,false,v>=54);
            default->new Meter("Blast strain",v,100,false,v>=75);
        });
        if(p.quirk.equals("electrification"))meters.add(new Meter("Overload",QuirkResources.overload(p),100,false,QuirkResources.overload(p)>=75));
        if(p.quirk.equals("kinetic_reservoir")){double f=QuirkResources.value(p,"kinetic_fatigue",0);meters.add(new Meter("Guard fatigue",f,100,false,f>=75));}
        return List.copyOf(meters);
    }
}
