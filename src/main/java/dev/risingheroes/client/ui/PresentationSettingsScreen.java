package dev.risingheroes.client.ui;

import dev.risingheroes.MhaConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class PresentationSettingsScreen extends Screen {
    private final Screen parent;
    public PresentationSettingsScreen(Screen parent){super(Component.literal("Presentation options"));this.parent=parent;}
    @Override protected void init(){
        int w=Math.min(240,width-32),x=(width-w)/2,y=Math.max(32,(height-184)/2);
        toggle(x,y,w,"Themed menus",MhaConfig.THEMED_MENUS);toggle(x,y+23,w,"Combat HUD",MhaConfig.HUD);
        toggle(x,y+46,w,"Custom item hotbar",MhaConfig.CUSTOM_HOTBAR);toggle(x,y+69,w,"Softer player mesh",MhaConfig.SOFT_AVATAR);
        toggle(x,y+92,w,"Reduced particles",MhaConfig.REDUCED_VFX);
        addRenderableWidget(new HeroButton(x,y+119,w,18,Component.translatable("mha.menu.hudEditor"),b->minecraft.setScreen(new HudEditorScreen(this)),true));
        addRenderableWidget(new HeroButton(x,y+145,w,18,Component.literal("Back"),b->onClose(),false));
    }
    private void toggle(int x,int y,int w,String name,ModConfigSpec.BooleanValue value){addRenderableWidget(new HeroButton(x,y,w,18,Component.literal(name+": "+(value.get()?"on":"off")),b->{value.set(!value.get());MhaConfig.CLIENT.save();rebuildWidgets();},false));}
    @Override public void render(GuiGraphics g,int x,int y,float dt){Theme.background(g,width,height);g.drawCenteredString(font,"PRESENTATION",width/2,14,Theme.PAPER);super.render(g,x,y,dt);}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float dt){}
    @Override public void onClose(){minecraft.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
