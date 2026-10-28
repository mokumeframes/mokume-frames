package dev.risingheroes.client.mixin;

import dev.risingheroes.client.SoftAvatar;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin {
    @Inject(method="<init>",at=@At("TAIL"))
    private void mha$softSurface(ModelPart root,boolean slim,CallbackInfo ci){SoftAvatar.install(root);}
}
