package dev.risingheroes.equipment;

import java.util.function.Consumer;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;

/** GeckoLib's lazy renderer callback. Its implementation is installed only by the physical client. */
public final class EquipmentBridge {
    public static Consumer<Consumer<GeoRenderProvider>> ARMOR=ignored->{};
}
