package dev.risingheroes;

import dev.risingheroes.entity.TrainingSentinel;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.*;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> TYPES=DeferredRegister.create(Registries.ENTITY_TYPE,"mha");
    public static final DeferredHolder<EntityType<?>,EntityType<TrainingSentinel>> SENTINEL=TYPES.register("training_sentinel",()->EntityType.Builder.of(TrainingSentinel::new,MobCategory.CREATURE).sized(1.0f,2.6f).clientTrackingRange(8).updateInterval(2).build("mha:training_sentinel"));
    public static void attributes(EntityAttributeCreationEvent event){event.put(SENTINEL.get(),TrainingSentinel.attributes().build());}
}
