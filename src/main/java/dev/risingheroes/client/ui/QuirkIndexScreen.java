package dev.risingheroes.client.ui;

import dev.risingheroes.client.ClientState;
import dev.risingheroes.core.*;
import java.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Installed lore and moves remain searchable without a world or internet connection. */
public final class QuirkIndexScreen extends Screen {
    private final Screen parent;private String selected="explosion",query="";private int filter=0,owned=0,left,top,pw,ph,scroll=0;private boolean bookmarks=false;
    private final List<HeroButton> entries=new ArrayList<>();
    public QuirkIndexScreen(Screen parent){super(Component.literal("Quirk reference index"));this.parent=parent;selected=Kits.reference(ClientState.profile()).id();}
    @Override protected void init() {
        pw=Math.min(720,width-24);ph=Math.min(420,height-20);left=(width-pw)/2;top=(height-ph)/2;
        int x=left+12,y=top+34,w=(pw-32)/3;
        var search=new EditBox(font,x,y,pw-24,18,Component.literal("Search quirks, users and source works"));search.setHint(Component.literal("Search names, users, roles or works"));search.setValue(query);search.setResponder(value->{query=value;scroll=0;updateEntries();});addRenderableWidget(search);
        String[] roles={"All roles","Pressure","Area control","Protection","Disruption"};
        addRenderableWidget(new HeroButton(x,y+23,w,18,Component.literal(roles[filter]),b->{filter=(filter+1)%5;rebuildWidgets();},false));
        addRenderableWidget(new HeroButton(x+w+4,y+23,w,18,Component.literal(owned==0?"All mastery":owned==1?"Acquired kits":"Trained kits"),b->{owned=(owned+1)%3;rebuildWidgets();},false));
        addRenderableWidget(new HeroButton(x+2*(w+4),y+23,w,18,Component.literal(bookmarks?"Bookmarked":"All bookmarks"),b->{bookmarks=!bookmarks;rebuildWidgets();},false));
        int bottom=top+ph-27;
        addRenderableWidget(new HeroButton(x,bottom,(pw-28)/2,18,Component.literal("Read source entry"),b->minecraft.setScreen(new SourceScreen(this,selected)),true));
        addRenderableWidget(new HeroButton(x+(pw-20)/2,bottom,(pw-28)/2,18,Component.literal("Technique reference"),b->minecraft.setScreen(new NotebookScreen(this,1,false,selected)),false));
        addRenderableWidget(new HeroButton(left+pw-61,top+7,49,16,Component.literal("Back"),b->onClose(),false));updateEntries();
    }
    private void updateEntries() {
        for(var b:entries)removeWidget(b);entries.clear();var p=ClientState.profile();String needle=query.toLowerCase(Locale.ROOT);int i=0;
        for(var kit:Kits.all().stream().sorted(Comparator.comparing(Kits.Kit::name)).toList()) {
            var lore=QuirkLore.get(kit.id());String content=kit.name()+" "+kit.role()+" "+lore.sourceWork()+" "+lore.sourceMedium()+" "+String.join(" ",lore.knownUsers());
            int role=Set.of("explosion","blueflame").contains(kit.id())?1:kit.id().equals("half_cold_half_hot")?2:Set.of("hardening","kinetic_reservoir").contains(kit.id())?3:4;
            if(!content.toLowerCase(Locale.ROOT).contains(needle)||filter!=0&&role!=filter||owned==1&&!p.masteries.containsKey(kit.id())||owned==2&&p.masteries.getOrDefault(kit.id(),0)==0||bookmarks&&!p.bookmarks.contains(kit.id()))continue;
            if(i++<scroll)continue;if(entries.size()>=Math.max(1,(ph-140)/23))continue;
            String label=kit.name()+(lore.sourceMedium().startsWith("Mod-original")?" [Original]":"")+" / mastery "+p.masteries.getOrDefault(kit.id(),0);var button=new HeroButton(left+12,top+82+entries.size()*23,pw-24,20,Component.literal(label),b->{selected=kit.id();updateEntries();},selected.equals(kit.id()));entries.add(button);addRenderableWidget(button);
        }
        if(entries.isEmpty()&&scroll>0){scroll=0;updateEntries();}
    }
    @Override public void render(GuiGraphics g,int mx,int my,float dt){Theme.background(g,width,height);g.fill(left,top,left+pw,top+ph,0xf3111b27);g.fill(left,top,left+pw,top+2,Theme.TEAL);g.drawString(font,font.plainSubstrByWidth("QUIRK REFERENCE / EXPERIMENTAL",pw-72),left+12,top+12,Theme.PAPER,false);if(ph>=300)g.drawString(font,"Scroll to browse / Original content is explicitly labelled",left+12,top+ph-49,Theme.MUTED,false);super.render(g,mx,my,dt);}
    @Override public boolean mouseScrolled(double x,double y,double sx,double sy){scroll=Math.max(0,Math.min(Kits.ids().size()-1,scroll+(sy<0?1:-1)));updateEntries();return true;}
    @Override public void renderBackground(GuiGraphics g,int mx,int my,float dt){}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){minecraft.setScreen(parent);}
}
