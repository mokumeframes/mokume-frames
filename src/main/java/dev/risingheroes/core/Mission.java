package dev.risingheroes.core;

import java.util.*;

/** Objectives are facts witnessed by server handlers, never client-supplied progress counts. */
public record Mission(String id,String name,String path,String prerequisite,int licenceRequired,int credits,int mastery,int grantsLicence,int season,String source,List<Stage> stages) {
    public record Stage(String id,String type,String role,int count,int seconds,int x,int y,int z,String text) {}
    public void validate() {
        if(id==null||!id.matches("[a-z_]{3,48}")||name==null||name.length()>80||!Set.of("hero","villain","independent").contains(path)||prerequisite==null||source==null||stages==null||stages.isEmpty()||stages.size()>16)throw new IllegalArgumentException("Invalid mission identity: "+id);
        if(licenceRequired<0||licenceRequired>2||grantsLicence<0||grantsLicence>2||credits<0||credits>1000||mastery<0||mastery>10||season<0||season>8)throw new IllegalArgumentException("Invalid mission rewards: "+id);
        Set<String> ids=new HashSet<>();
        for(var s:stages)if(s==null||s.id()==null||!ids.add(s.id())||!s.id().matches("[a-z_]{2,40}")||!Set.of("MOVE","INTERACT","AID","ESCORT","DEFEND","COMBAT","JUDGMENT","BOSS").contains(s.type())||s.role()==null||s.role().isBlank()||s.count()<1||s.count()>8||s.seconds()<0||s.seconds()>120||s.x()<3||s.x()>44||s.y()<1||s.y()>10||s.z()<3||s.z()>44||s.text()==null||s.text().length()>500)throw new IllegalArgumentException("Invalid objective in "+id);
    }
}
