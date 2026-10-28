package dev.risingheroes.client.ui;

import dev.risingheroes.MhaConfig;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.neoforged.neoforge.client.event.ScreenEvent;

public final class MenuEvents {
    public static void opening(ScreenEvent.Opening event) {
        if(!MhaConfig.themed())return;
        if(event.getNewScreen() instanceof TitleScreen)event.setNewScreen(new HeroTitleScreen());
        else if(event.getNewScreen()!=null&&event.getNewScreen().getClass()==PauseScreen.class&&((PauseScreen)event.getNewScreen()).showsPauseMenu())event.setNewScreen(new HeroPauseScreen());
    }
}
