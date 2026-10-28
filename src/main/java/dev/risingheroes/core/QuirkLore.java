package dev.risingheroes.core;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public record QuirkLore(String id,String name,List<String> localizedNames,List<String> knownUsers,String sourceMedium,
    String sourceWork,String continuity,String firstCharacterAppearance,String firstDemonstration,String firstNamedExplanation,
    String lore,String adaptation,String role,String resource,String drawback,List<String> counters,List<String> nonCombatUtility,
    String equipment,String acquisition,String verification,List<Reference> references) {
    public record Reference(String title,String url,String supports,int season,String warning){}
    public static QuirkLore explosion() {
        return get("explosion");
    }
    public static QuirkLore get(String id) {
        Kits.get(id);
        try(var in=Objects.requireNonNull(QuirkLore.class.getResourceAsStream("/data/mha/mha/quirks/"+id+".json"))) {
            return Catalog.JSON.fromJson(new InputStreamReader(in,StandardCharsets.UTF_8),QuirkLore.class);
        }catch(IOException e){throw new UncheckedIOException(e);}
    }
}
