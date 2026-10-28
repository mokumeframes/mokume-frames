package dev.risingheroes.core;

import java.util.UUID;

/** A durable lease is committed before placement. Only air may be replaced by these constructs. */
public record ConstructPlan(String dimension,int x,int y,int z,String expected,long expiry,UUID owner) {
    public String key(){return dimension+"@"+x+","+y+","+z;}
    public void validate(){if(dimension==null||!dimension.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")||expected==null||!expected.startsWith("mha:temporary_ice")||owner==null||Math.abs((long)x)>30000000||Math.abs((long)z)>30000000||y< -2048||y>2048)throw new IllegalArgumentException("Invalid temporary construct lease");}
}
