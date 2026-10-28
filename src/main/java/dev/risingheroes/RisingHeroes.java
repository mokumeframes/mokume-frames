package dev.risingheroes;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(RisingHeroes.ID)
public final class RisingHeroes {
    public static final String ID = "mha";
    public static final Logger LOG = LogUtils.getLogger();
    public RisingHeroes(IEventBus bus, ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER,MhaConfig.SERVER);
        container.registerConfig(ModConfig.Type.CLIENT,MhaConfig.CLIENT);
        ModItems.ITEMS.register(bus);
        ModItems.TABS.register(bus);
        ModSounds.SOUNDS.register(bus);
        ModEntities.TYPES.register(bus);
        ModBlocks.BLOCKS.register(bus);
        bus.addListener(ModEntities::attributes);
        bus.addListener(Network::register);
        LOG.info("MHA: Rising Heroes foundation loaded; gameplay is experimental.");
    }
}
