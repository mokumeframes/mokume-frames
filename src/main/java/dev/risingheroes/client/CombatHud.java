package dev.risingheroes.client;

import dev.risingheroes.MhaConfig;
import dev.risingheroes.core.*;
import dev.risingheroes.client.ui.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.DeltaTracker;
import net.minecraft.resources.ResourceLocation;

public final class CombatHud {
    public static void render(GuiGraphics g,DeltaTracker dt){
        var mc=Minecraft.getInstance();
        if(mc.player==null||mc.player.isSpectator()||mc.options.hideGui||ClientState.view==null||!MhaConfig.HUD.get()||ClientState.profile().quirk.isEmpty())return;
        int exclusion=Math.max(56,Math.max(mc.gui.leftHeight,mc.gui.rightHeight)+12);
        drawPanel(g,HudLayout.at(g.guiWidth(),g.guiHeight(),MhaConfig.HUD_SCALE.get(),MhaConfig.HUD_ANCHOR_X.get(),MhaConfig.HUD_ANCHOR_Y.get(),MhaConfig.HUD_COMPACT.get(),MhaConfig.HUD_NUMBERS.get(),exclusion),MhaConfig.HUD_OPACITY.get(),false);
        if(MhaConfig.QUEST_TRACKER.get()&&g.guiWidth()>=450){
            int x=g.guiWidth()-172;g.fill(x-7,9,g.guiWidth()-8,47,0xad111b27);g.drawString(mc.font,"TRAINING RECORD",x,14,Theme.TEAL,false);
            String label=ClientState.view.objective();g.drawString(mc.font,mc.font.plainSubstrByWidth(label,156),x,27,Theme.PAPER,false);
        }
    }
    public static void drawPanel(GuiGraphics g,HudLayout l,double opacity,boolean preview){
        var mc=Minecraft.getInstance();var p=ClientState.profile();var kit=Kits.reference(p);
        int cell=l.cell(),width=3*cell+16,after=22+2*(cell+4),row=l.numbers()?16:7;
        g.pose().pushPose();g.pose().translate(l.x(),l.y(),0);g.pose().scale(l.scale(),l.scale(),1);
        g.fill(0,0,width,after+13+3*row+5,((int)(opacity*255)<<24)|0x101923);g.fill(0,0,2,18,kit.color());
        String title=kit.id().equals("blueflame")?"Blue Flame":kit.name();g.drawString(mc.font,mc.font.plainSubstrByWidth(title,width-12),6,3,Theme.PAPER,false);
        boolean warning=HudResources.of(p).stream().anyMatch(HudResources.Meter::warning)||ClientState.now()<p.overloadUntil;
        g.drawString(mc.font,warning?"! RESOURCE WARNING":"EQUIPPED TECHNIQUES",6,13,warning?Theme.ORANGE:Theme.MUTED,false);
        String active=mc.player==null?"":ClientPresentation.activeMove(mc.player.getId());
        String[] defaults={"palm_burst","ap_shot","stun_grenade","burst_volley","blast_dash","howitzer_impact"};
        for(int i=0;i<6;i++){
            int x=4+(i%3)*(cell+4),y=22+(i/3)*(cell+4);String id=preview?defaults[i]:p.slots.get(i);var m=ClientState.move(id);
            long left=preview&&i==1?35:p.cooldowns.getOrDefault(id,0L)-ClientState.now();
            boolean locked=m!=null&&!preview&&!p.currentUnlocks().contains(id),exhausted=false,running=!id.isBlank()&&id.equals(active);
            if(!preview&&m!=null&&!locked){try{QuirkResources.check(p,m,ClientState.now(),mc.player!=null&&!mc.player.onGround(),mc.player!=null&&mc.player.isUnderWater());exhausted=p.stamina<m.stamina()||ClientState.now()<p.exertionUntil;}catch(IllegalArgumentException e){exhausted=true;}}
            g.fill(x,y,x+cell,y+cell,0xff1e2c39);g.fill(x,y+cell-1,x+cell,y+cell,running?Theme.ORANGE:left>0||exhausted||locked?Theme.MUTED:kit.color());
            if(m!=null)g.blit(ResourceLocation.fromNamespaceAndPath("mha","textures/gui/moves/"+id+".png"),x+(cell-24)/2,y+2,24,24,0,0,24,24,24,24);
            if(m==null){g.drawCenteredString(mc.font,"-",x+cell/2,y+3,Theme.MUTED);g.drawCenteredString(mc.font,"EMPTY",x+cell/2,y+14,Theme.MUTED);}
            else if(locked){g.fill(x,y,x+cell,y+cell-10,0xc0101923);g.renderOutline(x+cell/2-3,y+3,6,6,Theme.MUTED);g.fill(x+cell/2-5,y+7,x+cell/2+5,y+13,Theme.MUTED);g.drawCenteredString(mc.font,"LOCK",x+cell/2,y+15,Theme.PAPER);}
            else if(left>0&&!running){int cover=(int)((cell-10)*Math.min(1,left/(double)Math.max(1,m.cooldown())));g.fill(x,y+cell-10-cover,x+cell,y+cell-10,0xc0101923);g.drawCenteredString(mc.font,String.format(java.util.Locale.ROOT,"%.1fs",left/20f),x+cell/2,y+10,Theme.PAPER);}
            else if(exhausted&&!running){g.fill(x,y,x+cell,y+cell-10,0xba101923);g.drawCenteredString(mc.font,"! COST",x+cell/2,y+10,Theme.ORANGE);}
            if(running)g.renderOutline(x,y,cell,cell,Theme.ORANGE);
            var key=ClientControls.MOVES[i];String hint=key.isUnbound()?"NO KEY":key.getTranslatedKeyMessage().getString();
            g.fill(x+1,y+cell-10,x+cell-1,y+cell-1,0xe6101923);g.drawCenteredString(mc.font,mc.font.plainSubstrByWidth(hint,cell-4),x+cell/2,y+cell-9,key.isUnbound()||ClientControls.conflict(key)?Theme.ORANGE:Theme.PAPER);
        }
        var move=ClientState.move(active);String caption=MeleeCombo.owns(active)?switch(active){case "m1_left"->"Left jab / 1 of 3";case "m1_right"->"Right cross / 2 of 3";default->"Front kick / 3 of 3";}:move!=null?move.name():"M1: jab > cross > kick";
        g.drawString(mc.font,mc.font.plainSubstrByWidth(caption,width-8),4,after+1,Theme.PAPER,false);
        int y=after+13;
        for(var meter:HudResources.of(p)){
            int col=meter.warning()?Theme.ORANGE:meter.name().equals("Stamina")?Theme.TEAL:kit.color();
            if(l.numbers())g.drawString(mc.font,meter.text(),4,y,Theme.PAPER,false);
            int by=y+(l.numbers()?10:0),w=width-8;g.fill(4,by,width-4,by+3,0xff344654);
            double fraction=Math.max(meter.signed()?-1:0,Math.min(1,meter.value()/meter.max()));
            if(meter.signed()){int center=4+w/2,end=center+(int)(w*fraction/2);g.fill(Math.min(center,end),by,Math.max(center,end),by+3,fraction<0?kit.color():Theme.ORANGE);g.fill(center,by-1,center+1,by+4,Theme.PAPER);}
            else{g.fill(4,by,4+(int)(w*fraction),by+3,col);double mark=java.util.Set.of("Stamina","Charge","Shade").contains(meter.name())?.15:meter.name().equals("Stored impact")?.9:.75;int threshold=4+(int)(w*mark);g.fill(threshold,by,threshold+1,by+3,0xffd7aa66);}
            y+=row;
        }
        g.pose().popPose();
    }
}
