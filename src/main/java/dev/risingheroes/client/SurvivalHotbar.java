package dev.risingheroes.client;

import dev.risingheroes.MhaConfig;
import dev.risingheroes.client.ui.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/** Vanilla item renderers retain stack counts, damage bars, glint and item cooldown overlays. */
public final class SurvivalHotbar {
    public static void before(RenderGuiLayerEvent.Pre event) {
        var mc=Minecraft.getInstance();
        if(!event.getName().equals(VanillaGuiLayers.HOTBAR)||mc.player==null||mc.player.isSpectator()||mc.options.hideGui||!MhaConfig.HUD.get()||!MhaConfig.CUSTOM_HOTBAR.get()||!(mc.getCameraEntity() instanceof Player player))return;
        var g=event.getGuiGraphics();int x=g.guiWidth()/2-104,y=g.guiHeight()-24;
        int bg=((int)(MhaConfig.HUD_OPACITY.get()*255)<<24)|0x111b27;
        for(int i=0;i<9;i++) {
            int left=x+i*23;boolean selected=i==player.getInventory().selected;
            g.fill(left,y,left+22,y+23,bg);
            if(selected){g.renderOutline(left,y,22,23,Theme.PAPER);g.fill(left+1,y+21,left+21,y+23,Theme.TEAL);}
            var stack=player.getInventory().items.get(i);
            g.renderItem(player,stack,left+3,y+3,i+1);g.renderItemDecorations(mc.font,stack,left+3,y+3);
        }
        boolean offLeft=player.getMainArm().getOpposite()==HumanoidArm.LEFT;
        if(!player.getOffhandItem().isEmpty()) {
            int ox=offLeft?x-27:x+211;g.fill(ox,y,ox+23,y+23,bg);g.renderOutline(ox,y,23,23,Theme.MUTED);
            g.renderItem(player,player.getOffhandItem(),ox+3,y+3,10);g.renderItemDecorations(mc.font,player.getOffhandItem(),ox+3,y+3);
        }
        float attack=mc.player.getAttackStrengthScale(0);
        if(mc.options.attackIndicator().get()==AttackIndicatorStatus.HOTBAR&&attack<1) {
            int ax=offLeft?x+213:x-8;g.fill(ax,y+2,ax+4,y+21,0xff344654);int filled=(int)(19*Math.max(0,attack));g.fill(ax,y+21-filled,ax+4,y+21,Theme.PAPER);
        }
        event.setCanceled(true);
    }
}
