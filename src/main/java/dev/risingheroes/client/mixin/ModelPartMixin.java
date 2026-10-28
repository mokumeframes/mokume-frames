package dev.risingheroes.client.mixin;

import com.mojang.blaze3d.vertex.*;
import dev.risingheroes.client.SoftAvatar;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelPart.class)
public abstract class ModelPartMixin {
    @Inject(method="render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",at=@At("HEAD"))
    private void mha$activateSoftSurface(PoseStack pose,VertexConsumer vertices,int light,int overlay,int color,CallbackInfo ci){SoftAvatar.before((ModelPart)(Object)this);}
}
