package dev.risingheroes.client.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import dev.risingheroes.ModSounds;
import net.minecraft.network.chat.Component;

public final class HeroButton extends Button {
    private final boolean primary;
    public HeroButton(int x,int y,int w,int h,Component label,OnPress action,boolean primary) {
        super(x,y,w,h,label,action,DEFAULT_NARRATION);this.primary=primary;
    }
    @Override public void playDownSound(SoundManager manager){manager.play(SimpleSoundInstance.forUI(ModSounds.UI_SELECT.get(),1,.55f));}
    @Override protected void renderWidget(GuiGraphics g,int mx,int my,float dt) {
        boolean over=isHoveredOrFocused();
        int accent=primary?Theme.TEAL:Theme.PAPER;
        g.fill(getX(),getY(),getX()+width,getY()+height,over?0xef28404b:0xd912202d);
        g.fill(getX(),getY(),getX()+2,getY()+height,active?accent:0xff5d6973);
        g.fill(getX()+2,getY()+height-1,getX()+width,getY()+height,over?0x9955d3bc:0x403b5566);
        if(isFocused())g.renderOutline(getX()-2,getY()-2,width+4,height+4,Theme.TEAL);
        var font=Minecraft.getInstance().font;
        g.drawString(font,font.plainSubstrByWidth(getMessage().getString(),width-22),getX()+10,getY()+(height-8)/2,active?Theme.PAPER:Theme.MUTED,false);
        if(over&&active)g.drawString(Minecraft.getInstance().font,">",getX()+width-12,getY()+(height-8)/2,accent,false);
    }
}
