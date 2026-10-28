package dev.risingheroes.client.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;

/** Keep Minecraft's original disconnect/reporting/LAN/options actions and pause semantics. */
public final class HeroPauseScreen extends PauseScreen {
    public HeroPauseScreen(){super(true);}
    @Override protected void init() {
        super.init();int w=Math.min(150,(width-28)/2),x=(width-2*w-4)/2;
        addRenderableWidget(new HeroButton(x,10,w,18,Component.translatable("mha.menu.notebook"),b->minecraft.setScreen(new NotebookScreen(this,0)),true));
        addRenderableWidget(new HeroButton(x+w+4,10,w,18,Component.translatable("mha.menu.hudEditor"),b->minecraft.setScreen(new HudEditorScreen(this)),false));
    }
    @Override public void renderBackground(GuiGraphics g,int mx,int my,float dt) {
        Theme.background(g,width,height);
        g.fill(width/2-112,34,width/2+112,height-22,0xb5111b27);
        if(width>=640)Theme.brand(g,20,height/2-25,1.4f);
        String state=minecraft.isPaused()?"PAUSED / YOUR NEXT CHAPTER AWAITS":"CONNECTED / THE WORLD CONTINUES";
        g.drawCenteredString(font,state,width/2,height-12,Theme.MUTED);
    }
}
