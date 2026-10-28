package dev.risingheroes.client.ui;

import dev.risingheroes.MhaConfig;
import dev.risingheroes.client.ClientState;
import dev.risingheroes.core.QuirkLore;
import dev.risingheroes.server.Gameplay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;
import java.util.*;

public final class SourceScreen extends Screen {
    private final Screen parent;
    private final QuirkLore lore;
    private int left,top,pw,ph,scroll;
    private boolean bookmarked;
    public SourceScreen(Screen parent){this(parent,dev.risingheroes.core.Kits.reference(ClientState.profile()).id());}
    public SourceScreen(Screen parent,String id){super(Component.literal(dev.risingheroes.core.Kits.get(id).name()+" / source notebook"));this.parent=parent;this.lore=QuirkLore.get(id);}
    @Override protected void init() {
        pw=Math.min(700,width-24);ph=Math.min(450,height-20);left=(width-pw)/2;top=(height-ph)/2;
        addRenderableWidget(new HeroButton(left+pw-64,top+8,54,18,Component.literal("Back"),b->onClose(),false));
        bookmarked=ClientState.profile().bookmarks.contains(lore.id());
        var star=addRenderableWidget(new HeroButton(left+12,top+ph-27,120,18,Component.literal(bookmarked?"Bookmarked":"Bookmark entry"),b->ClientState.send(Gameplay.BOOKMARK,0,lore.id(),""),true));star.active=minecraft.level!=null&&ClientState.view!=null;
        addRenderableWidget(new HeroButton(left+140,top+ph-27,Math.min(190,pw-152),18,Component.literal("Spoilers through season "+MhaConfig.SPOILER_SEASON.get()),b->{MhaConfig.SPOILER_SEASON.set((MhaConfig.SPOILER_SEASON.get()+1)%9);MhaConfig.CLIENT.save();rebuildWidgets();},false));
    }
    @Override public void tick(){if(bookmarked!=ClientState.profile().bookmarks.contains(lore.id()))rebuildWidgets();}
    @Override public void render(GuiGraphics g,int mx,int my,float dt) {
        Theme.background(g,width,height);g.fill(left,top,left+pw,top+ph,0xf4111b27);g.fill(left,top,left+pw,top+2,Theme.ORANGE);
        g.drawString(font,font.plainSubstrByWidth(lore.name().toUpperCase(Locale.ROOT)+" / SOURCE NOTEBOOK",pw-82),left+12,top+13,Theme.PAPER,false);
        g.enableScissor(left+10,top+38,left+pw-10,top+ph-34);
        int x=left+14,y=top+42-scroll,w=pw-28;
        y=paragraph(g,"IDENTITY",String.join(" / ",lore.localizedNames())+"\n"+String.join("\n",lore.knownUsers()),x,y,w);
        y=paragraph(g,"SOURCE AND CONTINUITY",lore.sourceMedium()+"\n"+lore.continuity(),x,y,w);
        y=paragraph(g,"APPEARANCE VERIFICATION",lore.firstCharacterAppearance()+"\n"+lore.firstDemonstration()+"\n"+lore.firstNamedExplanation(),x,y,w);
        y=paragraph(g,"LORE / MOD ADAPTATION",lore.lore()+"\n"+lore.adaptation(),x,y,w);
        y=paragraph(g,"ROLE, RESOURCE AND DRAWBACK",lore.role()+"\n"+lore.resource()+"\n"+lore.drawback(),x,y,w);
        y=paragraph(g,"COUNTERS AND NON-COMBAT USES",String.join("\n",lore.counters())+"\n"+String.join("\n",lore.nonCombatUtility()),x,y,w);
        y=paragraph(g,"EQUIPMENT AND ACQUISITION",lore.equipment()+"\n"+lore.acquisition(),x,y,w);
        y=paragraph(g,"VERIFICATION NOTES",lore.verification(),x,y,w);
        for(var ref:lore.references()) {
            if(ref.season()>MhaConfig.SPOILER_SEASON.get()){y=paragraph(g,"HIDDEN SOURCE","A season "+ref.season()+" source is hidden by your spoiler setting.",x,y,w);continue;}
            y=paragraph(g,ref.title().toUpperCase(Locale.ROOT),ref.supports()+"\n"+ref.warning()+"\n"+ref.url(),x,y,w);
        }
        g.disableScissor();super.render(g,mx,my,dt);
    }
    private int paragraph(GuiGraphics g,String title,String text,int x,int y,int w){g.drawString(font,title,x,y,Theme.TEAL,false);g.drawWordWrap(font,Component.literal(text),x,y+15,w,Theme.MUTED);return y+22+font.wordWrapHeight(text,w);}
    @Override public boolean mouseScrolled(double x,double y,double sx,double sy){scroll=Math.max(0,Math.min(1800,scroll+(sy<0?28:-28)));return true;}
    @Override public boolean keyPressed(int key,int scan,int modifiers){if(key==org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_DOWN){scroll=Math.min(1800,scroll+ph/2);return true;}if(key==org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_UP){scroll=Math.max(0,scroll-ph/2);return true;}return super.keyPressed(key,scan,modifiers);}
    @Override public void onClose(){minecraft.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void renderBackground(GuiGraphics g,int mx,int my,float dt){}
}
