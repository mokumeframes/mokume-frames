package dev.risingheroes.client.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class Theme {
    public static final int INK=0xff111b27,PAPER=0xfff2ede2,TEAL=0xff55d3bc,ORANGE=0xffff974f,MUTED=0xff9eb0bf;
    public static final ResourceLocation ART=ResourceLocation.parse("mha:textures/gui/menu/academy_dawn.png");
    public static void background(GuiGraphics g,int w,int h) {
        float scale=Math.max(w/1672f,h/941f);
        int bw=Math.round(1672*scale),bh=Math.round(941*scale);
        g.blit(ART,(w-bw)/2,(h-bh)/2,bw,bh,0,0,1672,941,1672,941);
        // A quiet left-hand reading area; never bake words into the artwork.
        g.fillGradient(0,0,w/2,h,0xa4111b27,0xe8111b27);
        g.fillGradient(0,h-42,w,h,0x00111b27,0xe8111b27);
    }
    public static void brand(GuiGraphics g,int x,int y,float scale) {
        var font=Minecraft.getInstance().font;
        g.drawString(font,"MHA  /  RISING HEROES",x,y,TEAL,false);
        g.pose().pushPose();g.pose().translate(x,y+14,0);g.pose().scale(scale,scale,1);
        g.drawString(font,"RISING",0,0,PAPER,false);g.drawString(font,"HEROES",0,12,PAPER,false);
        g.pose().popPose();
    }
    public static void loading(GuiGraphics g,float progress,float alpha) {
        if(alpha<.02f)return;
        int w=g.guiWidth(),h=g.guiHeight();var font=Minecraft.getInstance().font;
        g.pose().pushPose();g.pose().translate(0,0,500);
        // At fade-out the game owns the transition. No synthetic timer or completion callback.
        g.setColor(1,1,1,alpha);background(g,w,h);g.setColor(1,1,1,1);
        int x=Math.max(20,w/20),barWidth=Math.min(300,w-2*x),y=h-54;
        brand(g,x,Math.max(20,h/3-20),h<300?1.4f:2.4f);
        g.drawString(font,"PREPARING YOUR NEXT CHAPTER",x,y-16,MUTED,false);
        g.fill(x,y,x+barWidth,y+3,0xff344654);
        g.fill(x,y,x+Math.round(barWidth*Mth.clamp(progress,0,1)),y+3,TEAL);
        g.drawString(font,Math.round(Mth.clamp(progress,0,1)*100)+"%  /  Loading resources",x,y+12,PAPER,false);
        g.pose().popPose();
    }
}
