package dev.risingheroes.core;

import java.util.*;

public final class MissionRun {
    public String id="",session="",returnDimension="";
    public int step=0,penalties=0,arena=-1;
    public double returnX=0,returnY=0,returnZ=0;
    public Set<String> evidence=new HashSet<>();
    public boolean completed=false;
    public void validate() {
        if(id==null||session==null||returnDimension==null||evidence==null||step<0||step>16||penalties<0||penalties>20||arena< -1||arena>=4096||!Double.isFinite(returnX+returnY+returnZ))throw new IllegalArgumentException("Invalid mission session");
        if(!id.isBlank()) {var m=MissionCatalog.get(id);if(step>m.stages().size()||session.isBlank()||completed!=(step==m.stages().size()))throw new IllegalArgumentException("Inconsistent mission checkpoint");}
    }
}
