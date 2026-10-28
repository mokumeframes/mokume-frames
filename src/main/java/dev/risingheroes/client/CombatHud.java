package dev.risingheroes.client;

import dev.risingheroes.MhaConfig;
import dev.risingheroes.core.*;
import dev.risingheroes.client.ui.Theme;
import dev.risingheroes.client.ui.HudLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.DeltaTracker;
import net.minecraft.resources.ResourceLocation;

public final class CombatHud {
    public static void render(GuiGraphics g,DeltaTracker dt) {
        var mc=Minecraft.getInstance();
        if(mc.player==null||mc.player.isSpectator()||mc.options.hideGui||ClientState.view==null||!MhaConfig.HUD.get())return;
        if(ClientState.profile().quirk.isEmpty())return;
        int exclusion=Math.max(56,Math.max(mc.gui.leftHeight,mc.gui.rightHeight)+12);
        var layout=HudLayout.at(g.guiWidth(),g.guiHeight(),MhaConfig.HUD_SCALE.get(),MhaConfig.HUD_ANCHOR_X.get(),MhaConfig.HUD_ANCHOR_Y.get(),MhaConfig.HUD_COMPACT.get(),MhaConfig.HUD_NUMBERS.get(),exclusion);
        drawPanel(g,layout,MhaConfig.HUD_OPACITY.get(),false);
        // Vanilla retains health, food, armour, air, experience, offhand and mount information.
        if(MhaConfig.QUEST_TRACKER.get()&&g.guiWidth()>=450) {
            int x=g.guiWidth()-172;var p=ClientState.profile();
            g.fill(x-7,9,g.guiWidth()-8,47,0xad111b27);
            g.drawString(mc.font,"TRAINING RECORD",x,14,Theme.TEAL,false);
            String label=!p.mission.id.isBlank()?"Assignment "+Math.min(p.mission.step+1,MissionCatalog.get(p.mission.id).stages().size())+" / "+MissionCatalog.get(p.mission.id).stages().size():!p.activeTrial.isEmpty()?"Trial: "+p.activeTrial:p.schoolStage>=4?"Starter objectives complete":"School objective "+(p.schoolStage+1)+" / 4";
            g.drawString(mc.font,mc.font.plainSubstrByWidth(label,156),x,27,Theme.PAPER,false);
        }
    }
    public static void drawPanel(GuiGraphics g,HudLayout l,double opacity,boolean preview) {
        var mc=Minecraft.getInstance();var p=ClientState.profile();var kit=Kits.reference(p);
        int cell=l.cell(),width=3*cell+6;
        g.pose().pushPose();g.pose().translate(l.x(),l.y(),0);g.pose().scale(l.scale(),l.scale(),1);
        int bg=((int)(opacity*255)<<24)|0x111b27;
        g.drawString(mc.font,mc.font.plainSubstrByWidth(kit.name().toUpperCase(java.util.Locale.ROOT),width),0,0,Theme.PAPER,false);
        String[] defaults={"palm_burst","ap_shot","stun_grenade","burst_volley","blast_dash","howitzer_impact"};
        for(int i=0;i<6;i++) {
            int x=(i%3)*(cell+3),y=11+(i/3)*(cell+3);
            String id=preview?defaults[i]:p.slots.get(i);var move=ClientState.move(id);
            long left=preview&&i==1?35:p.cooldowns.getOrDefault(id,0L)-ClientState.now();
            boolean exhausted=false;if(!preview&&move!=null){try{QuirkResources.check(p,move,ClientState.now(),mc.player!=null&&!mc.player.onGround(),mc.player!=null&&mc.player.isUnderWater());exhausted=p.stamina<move.stamina()||ClientState.now()<p.exertionUntil;}catch(IllegalArgumentException unavailable){exhausted=true;}}
            g.fill(x,y,x+cell,y+cell,bg);g.fill(x,y+cell-1,x+cell,y+cell,left>0||exhausted?0xff8294a5:kit.color());
            if(move!=null)g.blit(ResourceLocation.fromNamespaceAndPath("mha","textures/gui/moves/"+id+".png"),x+5,y+3,cell-10,cell-10,0,0,24,24,24,24);
            else g.drawString(mc.font,"+",x+cell/2-3,y+4,Theme.MUTED,false);
            String hint=ClientControls.MOVES[i].getTranslatedKeyMessage().getString();
            if(left>0){g.fill(x,y,x+cell,y+cell,0xb9111b27);g.drawCenteredString(mc.font,String.format(java.util.Locale.ROOT,"%.1f",left/20f),x+cell/2,y+cell/2-4,Theme.PAPER);}
            else if(exhausted){g.fill(x,y,x+cell,y+cell,0x99111b27);g.drawCenteredString(mc.font,"!",x+cell/2,y+cell/2-4,Theme.PAPER);}
            g.drawString(mc.font,mc.font.plainSubstrByWidth(hint,cell-4),x+2,y+cell-9,ClientControls.conflict(ClientControls.MOVES[i])?0xffffcc66:Theme.PAPER,false);
        }
        int y=11+2*(cell+3)+1;double stamina=preview?74:p.stamina,resource=preview?31:QuirkResources.primary(p);
        g.fill(0,y,width,y+3,0xff344654);g.fill(0,y,(int)(width*stamina/100),y+3,Theme.TEAL);
        g.fill(0,y+5,width,y+8,0xff344654);
        if(p.quirk.equals("half_cold_half_hot")){int center=width/2,end=center+(int)(width*resource/200);g.fill(Math.min(center,end),y+5,Math.max(center,end),y+8,resource<0?kit.color():Theme.ORANGE);g.fill(center,y+4,center+1,y+9,Theme.PAPER);}
        else g.fill(0,y+5,(int)(width*resource/(p.quirk.equals("kinetic_reservoir")?60:100)),y+8,kit.color());
        String symbol=switch(p.quirk){case "half_cold_half_hot"->"T";case "hardening"->"F";case "electrification"->"C";case "blueflame"->"H";case "umbrakinesis"->"U";case "kinetic_reservoir"->"K";default->"B";};
        String values="S"+(int)stamina+" "+symbol+(int)resource;
        if(p.quirk.equals("electrification")){g.fill(0,y+10,width,y+12,0xff344654);g.fill(0,y+10,(int)(width*QuirkResources.overload(p)/100),y+12,Theme.ORANGE);values+=" O"+(int)QuirkResources.overload(p);}
        if(p.quirk.equals("kinetic_reservoir")){g.fill(0,y+10,width,y+12,0xff344654);g.fill(0,y+10,(int)(width*QuirkResources.value(p,"kinetic_fatigue",0)/100),y+12,Theme.ORANGE);values+=" F"+(int)QuirkResources.value(p,"kinetic_fatigue",0);}
        if(l.numbers())g.drawString(mc.font,values,0,y+15,Theme.PAPER,false);
        g.pose().popPose();
    }
}
