package dev.risingheroes.client;

import dev.risingheroes.entity.TrainingSentinel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class SentinelRenderer extends GeoEntityRenderer<TrainingSentinel> {
    public SentinelRenderer(EntityRendererProvider.Context context){super(context,new GeoModel<>() {
        @Override public ResourceLocation getModelResource(TrainingSentinel entity){return ResourceLocation.parse("mha:geo/training_sentinel.geo.json");}
        @Override public ResourceLocation getTextureResource(TrainingSentinel entity){return ResourceLocation.parse("mha:textures/entity/training_sentinel.png");}
        @Override public ResourceLocation getAnimationResource(TrainingSentinel entity){return ResourceLocation.parse("mha:animations/training_sentinel.animation.json");}
    });shadowRadius=.6f;}
}
