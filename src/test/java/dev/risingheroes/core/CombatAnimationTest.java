package dev.risingheroes.core;
import com.google.gson.*;
import dev.kosmx.playerAnim.core.data.gson.AnimationJson;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class CombatAnimationTest {
    private JsonObject asset(String id)throws IOException{try(var in=Objects.requireNonNull(getClass().getResourceAsStream("/assets/mha/player_animations/"+id+".json"))){return JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();}}
    @Test void everyAnimationLoadsThroughThePinnedPlayerAnimatorDecoder()throws IOException{
        var durations=new LinkedHashMap<String,Integer>();for(var m:Catalog.moves().values())durations.put(m.id(),m.startup()+m.active()+m.recovery());for(var s:MeleeCombo.STRIKES)durations.put(s.id(),s.duration());
        for(var entry:durations.entrySet()){
            var json=asset(entry.getKey());var animation=new AnimationJson().deserialize(json,null,null).getFirst();
            assertEquals(entry.getValue().intValue(),animation.endTick,entry.getKey());assertFalse(animation.isInfinite);
            var frames=json.getAsJsonObject("emote").getAsJsonArray("moves");
            assertTrue(frames.size()>=4);for(var f:frames)assertFalse(f.getAsJsonObject().has("head"),"Combat must not rotate the camera");
        }
    }
    @Test void punchesLeadWithDifferentArmsAndKickUsesLegs()throws IOException{
        assertTrue(asset("m1_kick").toString().contains("rightLeg"));
        var left=asset("m1_left").getAsJsonObject("emote").getAsJsonArray("moves").get(2).getAsJsonObject();
        assertTrue(left.getAsJsonObject("leftArm").get("pitch").getAsDouble()<left.getAsJsonObject("rightArm").get("pitch").getAsDouble());
        var right=asset("m1_right").getAsJsonObject("emote").getAsJsonArray("moves").get(2).getAsJsonObject();
        assertTrue(right.getAsJsonObject("rightArm").get("pitch").getAsDouble()<right.getAsJsonObject("leftArm").get("pitch").getAsDouble());
    }
}
