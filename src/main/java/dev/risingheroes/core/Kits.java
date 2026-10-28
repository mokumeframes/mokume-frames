package dev.risingheroes.core;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class Kits {
    public record Kit(String id,String name,String role,String resource,int color,List<String> moves,List<String> starters) {}
    private static final Map<String,Kit> ALL=load();
    private static Map<String,Kit> load() {
        try(var in=Objects.requireNonNull(Kits.class.getResourceAsStream("/data/mha/mha/kits.json"))) {
            List<Kit> list=new Gson().fromJson(new InputStreamReader(in,StandardCharsets.UTF_8),new TypeToken<List<Kit>>(){}.getType());
            Map<String,Kit> result=new LinkedHashMap<>();Set<String> moves=new HashSet<>();
            for(var kit:list){if(!kit.id().matches("[a-z_]{2,40}")||kit.name().length()>64||kit.moves().size()<6||kit.moves().size()>10||!kit.moves().containsAll(kit.starters())||kit.starters().size()<2||result.put(kit.id(),kit)!=null)throw new IllegalArgumentException("Invalid kit "+kit.id());for(var id:kit.moves())if(!moves.add(id))throw new IllegalArgumentException("Move owned by multiple kits: "+id);}
            return Collections.unmodifiableMap(result);
        }catch(IOException e){throw new UncheckedIOException(e);}
    }
    public static Collection<Kit> all(){return ALL.values();}
    public static Set<String> ids(){return ALL.keySet();}
    public static Kit get(String id){var kit=ALL.get(id);if(kit==null)throw new IllegalArgumentException("Unknown playable kit: "+id);return kit;}
    public static Kit owner(String move){return ALL.values().stream().filter(k->k.moves().contains(move)).findFirst().orElseThrow(()->new IllegalArgumentException("Move has no kit: "+move));}
    public static Kit reference(HeroProfile p){return ALL.getOrDefault(p.quirk,ALL.get("explosion"));}
    public static boolean owns(HeroProfile p,String move){return ids().contains(p.quirk)&&get(p.quirk).moves().contains(move);}
}
