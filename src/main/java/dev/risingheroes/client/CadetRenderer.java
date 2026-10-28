package dev.risingheroes.client;

import dev.risingheroes.equipment.CadetArmor;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public final class CadetRenderer extends GeoArmorRenderer<CadetArmor> {
    public CadetRenderer() { super(new GeoModel<>() {
        @Override public ResourceLocation getModelResource(CadetArmor item) {return ResourceLocation.parse("mha:geo/cadet.geo.json");}
        @Override public ResourceLocation getTextureResource(CadetArmor item) {return ResourceLocation.parse("mha:textures/armor/cadet.png");}
        @Override public ResourceLocation getAnimationResource(CadetArmor item) {return ResourceLocation.parse("mha:animations/cadet.animation.json");}
    }); }
    @Override protected void applyBaseTransformations(HumanoidModel<?> base) {
        super.applyBaseTransformations(base);
        // GeoArmorRenderer copies shoulder/hip rotations once. Bendy is a different rig:
        // hinge the rigid forearm/shin children explicitly, using Player Animator's bend data.
        for (String side : new String[]{"right", "left"}) {
            boolean slim = currentEntity instanceof AbstractClientPlayer p && p.getSkin().model().id().equals("slim");
            model.getBone("armor" + (side.equals("right") ? "Right" : "Left") + "Arm").ifPresent(b -> b.setScaleX(slim ? .78f : 1f));
            transferBend(side + "Arm", side + "Forearm");
            transferBend(side + "Leg", side + "Shin");
            transferBend(side + "Leg", side + "Foot");
        }
    }
    private void transferBend(String part, String bone) {
        Vec3f bend = currentEntity instanceof AbstractClientPlayer p
            ? PlayerAnimationAccess.getPlayerAnimLayer(p).get3DTransform(part, TransformType.BEND, partialTick, new Vec3f(0,0,0))
            : new Vec3f(0,0,0);
        model.getBone(bone).ifPresent(b -> { b.setRotX(-bend.getY()); b.setRotY(0); b.setRotZ(0); });
    }
}
