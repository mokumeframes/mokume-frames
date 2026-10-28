package dev.risingheroes.core;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class Catalog {
    public static final Gson JSON=new Gson();
    public static final Set<String> PLAYABLE=Kits.ids();
    private static Map<String,Move> moves=loadDefaults();
    private static Map<String,Move> loadDefaults() {
        try(var stream=Objects.requireNonNull(Catalog.class.getResourceAsStream("/data/mha/mha/moves.json"))) {
            return validate(JSON.fromJson(new InputStreamReader(stream,StandardCharsets.UTF_8),new TypeToken<List<Move>>(){}.getType()));
        } catch(IOException e) {throw new UncheckedIOException(e);}
    }
    public static Map<String,Move> validate(List<Move> input) {
        if(input==null||input.isEmpty()||input.size()>128)throw new IllegalArgumentException("Expected 1–128 move definitions");
        Map<String,Move> result=new LinkedHashMap<>();
        for(var move:input){move.validate();if(result.put(move.id(),move)!=null)throw new IllegalArgumentException("Duplicate move: "+move.id());}
        return Collections.unmodifiableMap(result);
    }
    public static void replace(List<Move> input) {
        var validated=validate(input);
        if(!validated.keySet().equals(loadDefaults().keySet()))throw new IllegalArgumentException("Move reload may tune existing IDs, not remove/add behavior implementations");
        moves=validated;
    }
    public static Map<String,Move> moves(){return moves;}
    public static Move move(String id){var m=moves.get(id);if(m==null)throw new IllegalArgumentException("Unknown move: "+id);return m;}
}
