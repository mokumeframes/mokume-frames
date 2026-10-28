package dev.risingheroes;

import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class MhaConfig {
    public static final ModConfigSpec SERVER,CLIENT;
    public static final ModConfigSpec.BooleanValue PVP,FRIENDLY_FIRE,PROTECT_CAMPUS,THEMED_MENUS,HUD,REDUCED_VFX,HUD_COMPACT,HUD_NUMBERS,QUEST_TRACKER,CUSTOM_HOTBAR,SOFT_AVATAR;
    public static final ModConfigSpec.DoubleValue DAMAGE_SCALE,HUD_SCALE,HUD_OPACITY,HUD_ANCHOR_X,HUD_ANCHOR_Y;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> STARTING_POOL;
    public static final ModConfigSpec.IntValue SPOILER_SEASON;
    static {
        var s=new ModConfigSpec.Builder();
        s.comment("Server authority. Changes require world/server restart in this development build.");
        PVP=s.define("combat.pvp",false);FRIENDLY_FIRE=s.define("combat.friendlyFire",false);
        DAMAGE_SCALE=s.defineInRange("combat.damageScale",1.0,.1,3);
        PROTECT_CAMPUS=s.define("world.protectAcademy",true);
        STARTING_POOL=s.defineListAllowEmpty("quirks.startingPool",List.of("explosion"),()->"explosion",v->v instanceof String id&&dev.risingheroes.core.Kits.ids().contains(id));
        SERVER=s.build();
        var c=new ModConfigSpec.Builder();
        c.comment("Client presentation. These settings can be changed through the notebook.");
        THEMED_MENUS=c.define("menus.themed",true);HUD=c.define("hud.enabled",true);
        HUD_SCALE=c.defineInRange("hud.scale",1.0,.6,1.5);HUD_OPACITY=c.defineInRange("hud.opacity",.9,.3,1);
        HUD_ANCHOR_X=c.defineInRange("hud.anchorX",0.0,0,1);HUD_ANCHOR_Y=c.defineInRange("hud.anchorY",1.0,0,1);
        HUD_COMPACT=c.define("hud.compact",false);HUD_NUMBERS=c.define("hud.numbers",true);QUEST_TRACKER=c.define("hud.questTracker",true);
        CUSTOM_HOTBAR=c.define("hud.customHotbar",true);SOFT_AVATAR=c.define("models.softPlayerMesh",true);
        REDUCED_VFX=c.define("effects.reduced",false);
        SPOILER_SEASON=c.defineInRange("guide.spoilerSeason",1,0,8);CLIENT=c.build();
    }
    public static boolean themed(){return !CLIENT.isLoaded()||THEMED_MENUS.get();}
}
