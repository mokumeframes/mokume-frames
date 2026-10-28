package dev.risingheroes.equipment;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import java.util.function.Consumer;

/** Common item contains no client/render types. Renderer is registered on the client event bus. */
public final class CadetArmor extends ArmorItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public CadetArmor(Type type) {
        super(ArmorMaterials.IRON, type, new Item.Properties().durability(type.getDurability(15)));
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
    @Override public void createGeoRenderer(Consumer<GeoRenderProvider> consumer){EquipmentBridge.ARMOR.accept(consumer);}
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Armour follows the humanoid pose; articulated joints are transferred by CadetRenderer.
    }
}
