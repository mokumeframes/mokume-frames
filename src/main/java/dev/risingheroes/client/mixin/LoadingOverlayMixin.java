package dev.risingheroes.client.mixin;

import dev.risingheroes.MhaConfig;
import dev.risingheroes.client.ui.Theme;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LoadingOverlay.class)
public abstract class LoadingOverlayMixin {
    @Shadow @Final private ReloadInstance reload;
    @Shadow private long fadeOutStart;
    @Inject(method="render",at=@At("TAIL"))
    private void mha$drawLoading(GuiGraphics graphics,int x,int y,float dt,CallbackInfo ci) {
        if(MhaConfig.themed()&&(fadeOutStart<0||Util.getMillis()-fadeOutStart<950))Theme.loading(graphics,reload.getActualProgress(),1);
    }
}
