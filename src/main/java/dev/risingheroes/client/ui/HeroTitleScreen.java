package dev.risingheroes.client.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.SafetyScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.gui.ModListScreen;

public final class HeroTitleScreen extends Screen {
    private MenuLayout layout;
    public HeroTitleScreen(){super(Component.translatable("mha.menu.title"));}
    @Override protected void init() {
        layout=MenuLayout.at(width,height);int x=layout.left(),w=layout.buttonWidth(),h=layout.buttonHeight();
        addRenderableWidget(new HeroButton(x,layout.row(0),w,h,Component.translatable("mha.menu.begin"),b->minecraft.setScreen(new SelectWorldScreen(this)),true));
        addRenderableWidget(new HeroButton(x,layout.row(1),w,h,Component.translatable("menu.multiplayer"),b->minecraft.setScreen(minecraft.options.skipMultiplayerWarning?new JoinMultiplayerScreen(this):new SafetyScreen(this)),false));
        addRenderableWidget(new HeroButton(x,layout.row(2),w,h,Component.translatable("mha.menu.notebook"),b->minecraft.setScreen(new NotebookScreen(this,0)),false));
        addRenderableWidget(new HeroButton(x,layout.row(3),(w-4)/2,h,Component.translatable("menu.options"),b->minecraft.setScreen(new OptionsScreen(this,minecraft.options)),false));
        addRenderableWidget(new HeroButton(x+(w+4)/2,layout.row(3),(w-4)/2,h,Component.translatable("fml.menu.mods"),b->minecraft.setScreen(new ModListScreen(this)),false));
        addRenderableWidget(new HeroButton(x,layout.row(4),w,h,Component.translatable("menu.quit"),b->minecraft.stop(),false));
    }
    @Override public void render(GuiGraphics g,int mx,int my,float dt) {
        Theme.background(g,width,height);
        Theme.brand(g,layout.left(),layout.titleTop(),height<300?1.2f:2.4f);
        g.drawString(font,"01  /  A NEW GENERATION",Math.max(layout.left(),width-180),18,Theme.PAPER,false);
        g.drawString(font,"1.21.1  /  NEOFORGE  /  DEVELOPMENT BUILD",layout.left(),height-12,Theme.MUTED,false);
        super.render(g,mx,my,dt);
    }
    @Override public void renderBackground(GuiGraphics g,int mx,int my,float dt) {}
    @Override public boolean shouldCloseOnEsc(){return false;}
}
