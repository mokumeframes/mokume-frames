package dev.risingheroes.client.ui;

import dev.risingheroes.*;
import dev.risingheroes.client.*;
import dev.risingheroes.core.*;
import dev.risingheroes.server.Gameplay;
import java.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class NotebookScreen extends Screen {
    private final Screen parent;
    private final boolean reveal;
    private final String browseKit;
    private Kits.Kit kit(){return browseKit.isBlank()?Kits.reference(ClientState.profile()):Kits.get(browseKit);}
    private int tab,left,top,pw,ph,scroll=0,equipSlot=0,pageScroll=0,preset=1;
    private String selected="palm_burst",query="";
    private EditBox search;
    private HeroButton equipButton;
    private String equipReason="";
    private final List<HeroButton> moveButtons=new ArrayList<>();
    public NotebookScreen(Screen parent,int tab){this(parent,tab,false);}
    public NotebookScreen(Screen parent,int tab,boolean reveal){this(parent,tab,reveal,"");}
    public NotebookScreen(Screen parent,int tab,boolean reveal,String browseKit){super(Component.translatable("mha.menu.notebook"));this.parent=parent;this.tab=tab;this.reveal=reveal;this.browseKit=browseKit;this.selected=kit().moves().getFirst();}
    @Override protected void init() {
        pw=Math.min(780,width-24);ph=Math.min(460,height-20);left=(width-pw)/2;top=(height-ph)/2;
        String[] names={"Character","Techniques","Journal","Atlas","Rerolls","Settings"};
        int tw=(pw-16)/6;
        for(int i=0;i<6;i++){final int target=i;addRenderableWidget(new HeroButton(left+8+i*tw,top+28,tw-2,18,Component.literal(names[i]),b->{query=search==null?query:search.getValue();tab=target;scroll=0;pageScroll=0;rebuildWidgets();},i==tab));}
        addRenderableWidget(new HeroButton(left+pw-51,top+6,43,16,Component.literal("Close"),b->onClose(),false));
        int x=left+12,y=top+56,bottom=top+ph-27;
        boolean online=minecraft.level!=null&&ClientState.view!=null;
        if(tab==0) {
            int bw=(pw-32)/3;
            var travel=addRenderableWidget(new HeroButton(x,bottom,bw,18,Component.literal("Travel to U.A."),b->ClientState.send(Gameplay.TRAVEL,0,"",""),true));travel.active=online;
            var back=addRenderableWidget(new HeroButton(x+bw+4,bottom,bw,18,Component.literal("Return to survival"),b->ClientState.send(Gameplay.RETURN,0,"",""),false));back.active=online;
            addRenderableWidget(new HeroButton(x+2*(bw+4),bottom,bw,18,Component.literal("Quirk sources"),b->minecraft.setScreen(new QuirkIndexScreen(this)),false));
        }else if(tab==1) {
            search=new EditBox(font,x,y,Math.min(142,pw/3),18,Component.literal("Search techniques"));search.setHint(Component.literal("Search techniques"));search.setValue(query);search.setResponder(value->{query=value;scroll=0;});addRenderableWidget(search);
            equipButton=addRenderableWidget(new HeroButton(left+pw-142,bottom,130,18,Component.literal("Equip selected move"),b->{var m=ClientState.move(selected);if(m!=null){int slot=m.slot().equals("UTILITY")?4:m.slot().equals("CAPSTONE")?5:equipSlot;ClientState.send(Gameplay.EQUIP,slot,selected,"");}},true));equipButton.active=online&&ClientState.profile().quirk.equals(kit().id());
            addRenderableWidget(new HeroButton(left+pw-270,bottom,121,18,Component.literal("Technique slot: "+(equipSlot+1)),b->{equipSlot=(equipSlot+1)%4;rebuildWidgets();},false));
            int presetW=(pw-32)/3;
            addRenderableWidget(new HeroButton(x,bottom-23,presetW,18,Component.literal("Preset "+preset),b->{preset=preset%3+1;rebuildWidgets();},false));
            var save=addRenderableWidget(new HeroButton(x+presetW+4,bottom-23,presetW,18,Component.literal("Save preset"),b->ClientState.send(Gameplay.PRESET_SAVE,0,Integer.toString(preset),""),false));save.active=online;
            var load=addRenderableWidget(new HeroButton(x+2*(presetW+4),bottom-23,presetW,18,Component.literal("Load preset"),b->ClientState.send(Gameplay.PRESET_LOAD,0,Integer.toString(preset),""),false));load.active=online;
            updateMoveButtons();
        }else if(tab==2) {
            addRenderableWidget(new HeroButton(x,bottom-23,pw-24,18,Component.literal("Campaign assignment board"),b->minecraft.setScreen(new MissionBoardScreen(this)),true));
            if(ClientState.profile().activeTrial.isEmpty()) {
                String[] ids={"precision","aerial","capstone"};String[] labels={"Precision Circuit","Rooftop Recovery","Capstone Circuit"};int bw=(pw-32)/3;
                for(int i=0;i<3;i++){String id=ids[i];var button=addRenderableWidget(new HeroButton(x+i*(bw+4),bottom,bw,18,Component.literal(labels[i]),b->ClientState.send(Gameplay.TRIAL_START,0,id,""),false));button.active=online&&ClientState.profile().schoolStage>=4&&ClientState.profile().quirk.equals("explosion");}
            }else addRenderableWidget(new HeroButton(x,bottom,160,18,Component.literal("Cancel current trial"),b->ClientState.send(Gameplay.TRIAL_CANCEL,0,"",""),false));
        }else if(tab==4) {
            var p=ClientState.profile();
            if(p.offers.isEmpty()) {
                var button=addRenderableWidget(new HeroButton(x,bottom,(pw-32)/2,18,Component.literal("Generate offers / 1 token"),b->ClientState.send(Gameplay.REROLL,0,"",""),true));button.active=online;
                var exchange=addRenderableWidget(new HeroButton(x+(pw-24)/2,bottom,(pw-32)/2,18,Component.literal("Exchange 250 credits"),b->ClientState.send(Gameplay.EXCHANGE,0,"",""),false));exchange.active=online;
            }else {
                int offerWidth=(pw-32)/3;for(int i=0;i<p.offers.size();i++){String q=p.offers.get(i);var choice=addRenderableWidget(new HeroButton(x+i*(offerWidth+4),bottom-24,offerWidth,18,Component.literal(Kits.get(q).name()),b->ClientState.send(Gameplay.CHOOSE,0,q,p.offerIdentity),true));choice.active=online;}
                addRenderableWidget(new HeroButton(x,bottom,180,18,Component.literal("Keep my current quirk"),b->ClientState.send(Gameplay.CHOOSE,0,"keep",p.offerIdentity),false));
            }
        }else if(tab==5) {
            addRenderableWidget(new HeroButton(x,y,Math.min(240,pw-24),20,Component.literal("Presentation and player model"),b->minecraft.setScreen(new PresentationSettingsScreen(this)),true));
            addRenderableWidget(new HeroButton(x,y+28,Math.min(240,pw-24),20,Component.translatable("mha.menu.hudEditor"),b->minecraft.setScreen(new HudEditorScreen(this)),false));
            addRenderableWidget(new HeroButton(x,bottom,Math.min(220,pw-24),18,Component.literal("Minecraft options and controls"),b->minecraft.setScreen(new OptionsScreen(this,minecraft.options)),true));
        }
    }
    private void updateMoveButtons() {
        for(var b:moveButtons)removeWidget(b);moveButtons.clear();
        var results=ClientState.moves().stream().filter(m->kit().moves().contains(m.id())).filter(m->m.name().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))).sorted(Comparator.comparing(Move::name)).toList();
        int count=Math.max(2,(ph-136)/23);scroll=Math.max(0,Math.min(scroll,Math.max(0,results.size()-count)));
        for(int i=scroll;i<Math.min(results.size(),scroll+count);i++) {
            var m=results.get(i);var b=new HeroButton(left+12,top+79+(i-scroll)*23,Math.min(142,pw/3),20,Component.literal(m.name()),button->{selected=m.id();},selected.equals(m.id()));
            moveButtons.add(b);addRenderableWidget(b);
        }
    }
    @Override public void tick(){if(tab==1){updateMoveButtons();var m=ClientState.move(selected);equipReason="Join a world to equip techniques";boolean ready=false;
        if(m!=null&&ClientState.view!=null){try{if(!ClientState.profile().quirk.equals(kit().id()))throw new IllegalArgumentException("Assign this quirk before equipping its techniques");if(!ClientPresentation.activeMove(minecraft.player.getId()).isBlank())throw new IllegalArgumentException("Finish your current action before changing equipment");int slot=m.slot().equals("UTILITY")?4:m.slot().equals("CAPSTONE")?5:equipSlot;HeroRules.equip(ClientState.profile().copy(),slot,m.id(),ClientState.now());equipReason="Ready to equip / slot "+(slot+1);ready=true;}catch(IllegalArgumentException unavailable){equipReason=unavailable.getMessage();}}
        if(equipButton!=null)equipButton.active=ready;
    }}
    @Override public void render(GuiGraphics g,int mx,int my,float dt) {
        Theme.background(g,width,height);g.fill(left,top,left+pw,top+ph,0xf1111b27);g.fill(left,top,left+pw,top+2,Theme.TEAL);
        g.drawString(font,font.plainSubstrByWidth(reveal?"QUIRK DISCOVERED / "+kit().name():"HERO ANALYSIS NOTEBOOK",pw-74),left+12,top+10,Theme.PAPER,false);
        int x=left+12,y=top+58,wrap=pw-24;var p=ClientState.profile();
        g.enableScissor(left+6,top+49,left+pw-6,top+ph-(tab==1||tab==2?55:30));
        g.pose().pushPose();g.pose().translate(0,-pageScroll,0);
        if(tab==0) {
            line(g,kit().name().toUpperCase(Locale.ROOT),x,y,kit().color());line(g,kit().role(),x,y+16,Theme.PAPER);
            line(g,ClientState.view==null?"Offline reference / default balance values":"Rank "+p.rank+"  /  Mastery "+p.mastery()+"  /  Credits "+p.credits,x,y+34,Theme.TEAL);
            text(g,QuirkLore.get(kit().id()).resource(),x,y+51,wrap);
            text(g,ClientState.view==null?"Join a world to receive your quirk, train and view personal progress.":ClientState.view.objective(),x,y+81,wrap);
            text(g,"Keys: "+ClientControls.MOVES[0].getTranslatedKeyMessage().getString()+" technique 1 / "+ClientControls.MOVES[4].getTranslatedKeyMessage().getString()+" utility / "+ClientControls.BOOK.getTranslatedKeyMessage().getString()+" notebook",x,y+116,wrap);
            text(g,String.join(" / ",QuirkLore.get(kit().id()).knownUsers()),x,y+139,wrap);text(g,"Mechanics and trials are mod adaptations. Source information and unresolved first-appearance/localisation details are in the quirk reference index.",x,y+170,wrap);
        }else if(tab==1) {
            var m=ClientState.move(selected);int dx=left+Math.min(165,pw/3+22),dw=left+pw-12-dx;
            if(m!=null) {
                g.blit(ResourceLocation.parse("mha:textures/gui/moves/"+m.id()+".png"),dx,y,20,20,0,0,24,24,24,24);
                line(g,font.plainSubstrByWidth(m.name(),Math.max(20,dw-27)),dx+27,y+6,Theme.ORANGE);
                text(g,m.description(),dx,y+28,dw);
                int statsY=y+(ph<270?73:84);
                line(g,"Cost "+m.stamina()+" stamina / "+m.strain()+" "+kit().resource(),dx,statsY,Theme.PAPER);
                line(g,"Cooldown "+ticks(m.cooldown())+"s / range "+m.range()+" blocks",dx,statsY+13,Theme.PAPER);
                line(g,"Damage "+m.damage()+" / "+m.startup()+"t windup",dx,statsY+26,Theme.PAPER);
                text(g,(p.currentUnlocks().contains(m.id())?"UNLOCKED":"LOCKED / mastery "+m.mastery()+" + "+(m.trial().isEmpty()?"starter":m.trial()+" trial")),dx,statsY+43,dw);
                text(g,equipReason,dx,statsY+67,dw);
            }
        }else if(tab==2) {
            line(g,"SCHOOL RECORD / "+(p.schoolStage==0?"APPLICANT":"ENROLLED"),x,y,Theme.TEAL);
            text(g,ClientState.view==null?"The first region supports enrolment, a control lesson with a support alternative, a short traversal course and a civilian first-aid mission.":ClientState.view.objective(),x,y+20,wrap);
            text(g,"Open the assignment board for the connected school route and experimental underground/independent contracts. Licences are awarded from the server's completed examination record. A copied item cannot grant privileges.",x,y+65,wrap);
            text(g,p.activeTrial.isEmpty()?(p.quirk.equals("explosion")?"After starter training, visit Instructor Sato at (75, 52) for the Precision, Rooftop and Capstone circuits. Equip AP Shot first.":"Train this quirk through assignment control, movement, rescue, defence and boss/shutdown objectives. Earned mastery and the matching field milestone unlock each technique. Repeating currency rewards never duplicates them."):"Active trial: "+dev.risingheroes.core.TrialRules.objective(p)+" Time remaining: "+Math.max(0,(p.trialDeadline-ClientState.now())/20)+"s",x,y+115,wrap);
        }else if(tab==3) {
            line(g,"REGIONAL ATLAS / U.A. STARTER REGION",x,y,Theme.TEAL);
            if(ClientState.view==null||p.returnDimension.isEmpty()){text(g,"Travel to the academy to discover this region. The atlas shows authored points of interest and your own position; it does not load world chunks or reveal hidden players.",x,y+22,wrap);}
            else {
                int mw=Math.min(300,wrap),mh=Math.min(106,ph-112),mapY=y+17;
                g.fill(x,mapY,x+mw,mapY+mh,0xff203442);
                rect(g,x,mapY,mw,mh,12,8,55,35,0xff8294a5);rect(g,x,mapY,mw,mh,10,47,52,10,0xff55a493);
                rect(g,x,mapY,mw,mh,10,70,21,16,0xff667b8b);rect(g,x,mapY,mw,mh,73,11,11,77,0xffff974f);
                rect(g,x,mapY,mw,mh,91,0,8,103,0xff3a4e5e);rect(g,x,mapY,mw,mh,105,69,17,14,0xff55d3bc);
                if(minecraft.player!=null&&minecraft.level.dimension().location().toString().equals("mha:academy")){int px=x+(int)(minecraft.player.getX()/144*mw),py=mapY+(int)(minecraft.player.getZ()/104*mh);g.fill(px-2,py-2,px+3,py+3,Theme.PAPER);}
                line(g,"Academy  /  orange: training  /  teal: clinic",x,mapY+mh+8,Theme.PAPER);
            }
        }else if(tab==4) {
            line(g,"QUIRK RESEARCH / "+p.liveTokens.size()+" bound tokens",x,y,Theme.ORANGE);
            text(g,"Generating up to three alternatives spends one token, even if you keep your current quirk. Saved offers remain the same when you reopen this page or reconnect.",x,y+20,wrap);
            text(g,"Keep rank, credits, school progress, cosmetics and archived mastery. Your selected quirk restores only its own earned mastery; old powers stop. Combat restrictions remain.",x,y+60,wrap);
            text(g,"Server-enabled pool: "+(ClientState.view==null?"join a world to inspect":String.join(", ",ClientState.view.pool()))+". An empty alternative pool never spends your token.",x,y+102,wrap);
        }
        g.pose().popPose();g.disableScissor();super.render(g,mx,my,dt);
    }
    private void line(GuiGraphics g,String text,int x,int y,int col){g.drawString(font,text,x,y,col,false);}
    private void text(GuiGraphics g,String text,int x,int y,int wrap){g.drawWordWrap(font,Component.literal(text),x,y,Math.max(40,wrap),Theme.MUTED);}
    private static String ticks(int t){return String.format(Locale.ROOT,"%.1f",t/20f);}
    private static void rect(GuiGraphics g,int x,int y,int w,int h,int bx,int bz,int bw,int bd,int col){g.fill(x+bx*w/144,y+bz*h/104,x+(bx+bw)*w/144,y+(bz+bd)*h/104,col);}
    @Override public boolean mouseScrolled(double x,double y,double sx,double sy){if(tab==1&&x<left+pw/3+20){scroll+=sy<0?1:-1;updateMoveButtons();return true;}if(tab!=5){pageScroll=Math.max(0,Math.min(180,pageScroll+(sy<0?14:-14)));return true;}return super.mouseScrolled(x,y,sx,sy);}
    @Override public void renderBackground(GuiGraphics g,int mx,int my,float dt){}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){if(reveal)ClientState.send(Gameplay.ACK_REVEAL,0,"","");minecraft.setScreen(parent);}
}
