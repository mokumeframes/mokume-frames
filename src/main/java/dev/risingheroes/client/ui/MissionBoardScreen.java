package dev.risingheroes.client.ui;

import dev.risingheroes.client.ClientState;
import dev.risingheroes.core.*;
import dev.risingheroes.server.Gameplay;
import java.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class MissionBoardScreen extends Screen {
    private final Screen parent;
    private String path="hero",selected="school_assessment",lastState="";
    private int left,top,pw,ph,listScroll=0,bodyScroll=0;
    public MissionBoardScreen(Screen parent){super(Component.literal("Campaign assignments"));this.parent=parent;}
    private String stateKey(){var p=ClientState.profile();return p.mission.session+":"+p.mission.step+":"+p.completedMissions.size()+":"+p.licence;}
    @Override protected void init() {
        pw=Math.min(780,width-24);ph=Math.min(460,height-20);left=(width-pw)/2;top=(height-ph)/2;lastState=stateKey();
        int x=left+12,y=top+32,w=(pw-32)/3;
        String[] paths={"hero","villain","independent"};String[] labels={"School / hero","Underground","Independent"};
        for(int i=0;i<3;i++){String target=paths[i];addRenderableWidget(new HeroButton(x+i*(w+4),y,w,18,Component.literal(labels[i]),b->{path=target;listScroll=0;bodyScroll=0;selected=MissionCatalog.all().stream().filter(m->m.path().equals(path)).findFirst().orElseThrow().id();rebuildWidgets();},path.equals(target)));}
        addRenderableWidget(new HeroButton(left+pw-61,top+7,49,16,Component.literal("Back"),b->onClose(),false));
        var p=ClientState.profile();var rows=MissionCatalog.all().stream().filter(m->m.path().equals(path)).toList();int count=Math.max(2,(ph-104)/23);listScroll=Math.max(0,Math.min(listScroll,Math.max(0,rows.size()-count)));
        int listWidth=Math.min(180,(pw-36)/3);
        for(int i=listScroll;i<Math.min(rows.size(),listScroll+count);i++){var m=rows.get(i);String label=(p.completedMissions.contains(m.id())?"+ ":"")+m.name();addRenderableWidget(new HeroButton(x,top+59+(i-listScroll)*23,listWidth,20,Component.literal(label),b->{selected=m.id();bodyScroll=0;rebuildWidgets();},selected.equals(m.id())));}
        int bottom=top+ph-27;boolean online=minecraft.level!=null&&ClientState.view!=null;
        if(p.mission.id.isBlank()) {
            var start=addRenderableWidget(new HeroButton(x,bottom,pw-24,18,Component.literal(p.completedMissions.contains(selected)?"Replay assignment / rewards already claimed":"Start at academy registrar or instructor"),b->ClientState.send(Gameplay.MISSION_START,0,selected,""),true));start.active=online&&MissionRules.unavailable(p,MissionCatalog.get(selected),ClientState.now()).isBlank();
        }else {
            var leave=addRenderableWidget(new HeroButton(x,bottom,pw-24,18,Component.literal(p.mission.completed?"Leave completed encounter":"Leave encounter / checkpoint retained"),b->ClientState.send(Gameplay.MISSION_LEAVE,0,"",""),true));leave.active=online;
            var active=MissionRules.stage(p);if(active!=null&&active.type().equals("JUDGMENT")) {
                addRenderableWidget(new HeroButton(x,bottom-23,(pw-28)/2,18,Component.literal("Rescue first / call support"),b->ClientState.send(Gameplay.MISSION_DECISION,0,"rescue_first",""),true));
                addRenderableWidget(new HeroButton(x+(pw-20)/2,bottom-23,(pw-28)/2,18,Component.literal("Pursue the withdrawing threat"),b->ClientState.send(Gameplay.MISSION_DECISION,0,"pursue",""),false));
            }
        }
    }
    @Override public void tick(){if(!lastState.equals(stateKey()))rebuildWidgets();}
    @Override public void render(GuiGraphics g,int mx,int my,float dt) {
        Theme.background(g,width,height);g.fill(left,top,left+pw,top+ph,0xf3111b27);g.fill(left,top,left+pw,top+2,Theme.TEAL);
        g.drawString(font,"CAMPAIGN ASSIGNMENTS",left+12,top+11,Theme.PAPER,false);
        int dx=left+Math.min(180,(pw-36)/3)+22,dw=left+pw-12-dx,y=top+61-bodyScroll;var p=ClientState.profile();var m=MissionCatalog.get(selected);
        g.enableScissor(dx,top+56,left+pw-8,top+ph-55);
        List<String> lines=new ArrayList<>();lines.add(m.name());
        if(!p.mission.id.isBlank()) {
            lines.add("ACTIVE: "+MissionRules.objective(p));var s=MissionRules.stage(p);
            if(s!=null){int ox=512+(p.mission.arena%64)*64,oz=512+(p.mission.arena/64)*64;lines.add("Current marker: "+(ox+s.x())+", "+s.y()+", "+(oz+s.z())+" in the reserved exercise set.");}
        }
        lines.add(p.completedMissions.contains(m.id())?"COMPLETED / grade "+p.missionGrades.getOrDefault(m.id(),0)+". Replay grants no further reward.":"Reward: "+m.credits()+" credits / "+m.mastery()+" mastery. First completion only.");
        String blocked=MissionRules.unavailable(p,m,ClientState.now());if(!blocked.isBlank())lines.add("Requirement: "+blocked);
        if(m.grantsLicence()>0)lines.add("Successful first completion awards "+(m.grantsLicence()==1?"provisional hero licence":"the mod's professional qualification")+" through your server record.");
        lines.add("Objectives:");for(int i=0;i<m.stages().size();i++)lines.add((i+1)+". "+m.stages().get(i).text());
        lines.add(m.source());lines.add("Solo exercise with NPC teamwork. Real party/session play and final presentation checks remain pending.");
        for(int i=0;i<lines.size();i++){String line=lines.get(i);g.drawWordWrap(font,Component.literal(line),dx,y,dw,i==0?Theme.ORANGE:Theme.MUTED);y+=font.wordWrapHeight(line,dw)+9;}
        g.disableScissor();super.render(g,mx,my,dt);
    }
    @Override public boolean mouseScrolled(double x,double y,double sx,double sy){if(x<left+Math.min(180,(pw-36)/3)+22){listScroll+=sy<0?1:-1;rebuildWidgets();}else bodyScroll=Math.max(0,Math.min(2000,bodyScroll+(sy<0?18:-18)));return true;}
    @Override public void renderBackground(GuiGraphics g,int mx,int my,float dt){}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){minecraft.setScreen(parent);}
}
