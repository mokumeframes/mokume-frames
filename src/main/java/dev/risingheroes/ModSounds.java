package dev.risingheroes;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,"mha");
    public static final DeferredHolder<SoundEvent,SoundEvent> TELL=register("explosion.tell"),CHARGE=register("explosion.charge"),BURST=register("explosion.burst"),HEAVY=register("explosion.heavy"),RECOIL=register("explosion.recoil"),UI_SELECT=register("ui.select");
    private static DeferredHolder<SoundEvent,SoundEvent> register(String name){return SOUNDS.register(name,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("mha",name)));}
}
