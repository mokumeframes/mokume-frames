package dev.risingheroes.core;

import com.google.gson.reflect.TypeToken;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class MissionCatalog {
    private static final Map<String,Mission> ALL=load();
    private static Map<String,Mission> load() {
        try(var in=Objects.requireNonNull(MissionCatalog.class.getResourceAsStream("/data/mha/mha/missions.json"))) {
            List<Mission> rows=Catalog.JSON.fromJson(new InputStreamReader(in,StandardCharsets.UTF_8),new TypeToken<List<Mission>>(){}.getType());
            var result=new LinkedHashMap<String,Mission>();
            for(var m:rows){m.validate();if(result.put(m.id(),m)!=null)throw new IllegalArgumentException("Duplicate mission "+m.id());}
            for(var m:rows){Set<String> seen=new HashSet<>();Mission next=m;while(!next.prerequisite().isBlank()){if(!seen.add(next.id())||!result.containsKey(next.prerequisite()))throw new IllegalArgumentException("Missing/cyclic mission prerequisite "+m.id());next=result.get(next.prerequisite());}}
            return Collections.unmodifiableMap(result);
        }catch(java.io.IOException e){throw new java.io.UncheckedIOException(e);}
    }
    public static Collection<Mission> all(){return ALL.values();}
    public static Mission get(String id){var mission=ALL.get(id);if(mission==null)throw new IllegalArgumentException("Unknown mission "+id);return mission;}
}
