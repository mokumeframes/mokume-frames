package dev.risingheroes.client;

import dev.risingheroes.*;
import dev.risingheroes.client.ui.NotebookScreen;
import dev.risingheroes.client.ui.MissionBoardScreen;
import dev.risingheroes.server.Gameplay;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.lwjgl.glfw.GLFW;

public final class ClientControls {
    public static void attack(net.neoforged.neoforge.client.event.InputEvent.InteractionKeyMappingTriggered e){
        var mc=Minecraft.getInstance();var p=mc.player;
        if(!e.isAttack()||mc.screen!=null||p==null||ClientState.view==null||p.isSpectator()||!p.getMainHandItem().isEmpty()||!p.getOffhandItem().isEmpty()||p.isUsingItem()||p.isPassenger()||p.isSwimming()||p.isFallFlying()||p.isShiftKeyDown())return;
        if(mc.hitResult!=null&&mc.hitResult.getType()==net.minecraft.world.phys.HitResult.Type.BLOCK)return;
        if(mc.hitResult instanceof net.minecraft.world.phys.EntityHitResult hit&&!(hit.getEntity() instanceof net.minecraft.world.entity.LivingEntity))return;
        e.setCanceled(true);e.setSwingHand(false);ClientState.send(Gameplay.MELEE,0,"","");
    }
    public static final KeyMapping BOOK=new KeyMapping("key.mha.notebook",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_J,"key.categories.mha");
    public static final KeyMapping MAP=new KeyMapping("key.mha.atlas",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_M,"key.categories.mha");
    public static final KeyMapping[] MOVES=new KeyMapping[6];
    static {int[] defaults={GLFW.GLFW_KEY_Z,GLFW.GLFW_KEY_X,GLFW.GLFW_KEY_C,GLFW.GLFW_KEY_V,GLFW.GLFW_KEY_R,GLFW.GLFW_KEY_G};for(int i=0;i<6;i++)MOVES[i]=new KeyMapping("key.mha.slot"+(i+1),InputConstants.Type.KEYSYM,defaults[i],"key.categories.mha");}
    public static void register(RegisterKeyMappingsEvent e){e.register(BOOK);e.register(MAP);for(var key:MOVES)e.register(key);}
    public static void tick(ClientTickEvent.Post event) {
        var mc=Minecraft.getInstance();
        CastAudio.tick();QuirkEffects.tick();ClientPresentation.tick();
        if(mc.level==null){if(ClientState.view!=null)ClientState.clear();return;}
        if(mc.screen==null&&!ClientState.pending.isEmpty()) {
            String pending=ClientState.pending;ClientState.pending="";mc.setScreen(pending.equals("missions")?new MissionBoardScreen(null):new NotebookScreen(null,pending.equals("reroll")?4:0,pending.equals("reveal")));return;
        }
        if(mc.screen!=null)return;
        if(BOOK.consumeClick()){mc.setScreen(new NotebookScreen(null,0));return;}
        if(MAP.consumeClick()){mc.setScreen(new NotebookScreen(null,3));return;}
        for(int i=0;i<MOVES.length;i++)if(MOVES[i].consumeClick())ClientState.send(Gameplay.CAST,i,"","");
    }
    public static boolean conflict(KeyMapping key){for(var other:Minecraft.getInstance().options.keyMappings)if(other!=key&&key.same(other))return true;return false;}
}
