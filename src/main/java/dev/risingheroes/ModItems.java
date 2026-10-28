package dev.risingheroes;

import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import dev.risingheroes.equipment.CadetArmor;
import dev.risingheroes.equipment.GuideItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("mha");
    public static final DeferredItem<GuideItem> NOTEBOOK=ITEMS.register("hero_notebook",()->new GuideItem(GuideItem.Kind.NOTEBOOK));
    public static final DeferredItem<GuideItem> EXPLOSION=ITEMS.register("explosion_token",()->new GuideItem(GuideItem.Kind.CREATIVE_ASSIGNMENT));
    public static final DeferredItem<GuideItem> THERMAL=ITEMS.register("half_cold_half_hot_token",()->new GuideItem(GuideItem.Kind.CREATIVE_ASSIGNMENT,"half_cold_half_hot"));
    public static final DeferredItem<GuideItem> HARDENING=ITEMS.register("hardening_token",()->new GuideItem(GuideItem.Kind.CREATIVE_ASSIGNMENT,"hardening"));
    public static final DeferredItem<GuideItem> ELECTRIC=ITEMS.register("electrification_token",()->new GuideItem(GuideItem.Kind.CREATIVE_ASSIGNMENT,"electrification"));
    public static final DeferredItem<GuideItem> BLUEFLAME=ITEMS.register("blueflame_token",()->new GuideItem(GuideItem.Kind.CREATIVE_ASSIGNMENT,"blueflame"));
    public static final DeferredItem<GuideItem> UMBRA=ITEMS.register("umbrakinesis_token",()->new GuideItem(GuideItem.Kind.CREATIVE_ASSIGNMENT,"umbrakinesis"));
    public static final DeferredItem<GuideItem> KINETIC=ITEMS.register("kinetic_reservoir_token",()->new GuideItem(GuideItem.Kind.CREATIVE_ASSIGNMENT,"kinetic_reservoir"));
    public static final DeferredItem<GuideItem> REROLL=ITEMS.register("reroll_token",()->new GuideItem(GuideItem.Kind.REROLL));
    public static final DeferredItem<GuideItem> LICENCE=ITEMS.register("licence_credential",()->new GuideItem(GuideItem.Kind.LICENCE));
    public static final DeferredRegister<CreativeModeTab> TABS=DeferredRegister.create(Registries.CREATIVE_MODE_TAB,"mha");
    public static final DeferredItem<CadetArmor> HELMET = ITEMS.register("cadet_helmet", () -> new CadetArmor(ArmorItem.Type.HELMET));
    public static final DeferredItem<CadetArmor> CHEST = ITEMS.register("cadet_chestplate", () -> new CadetArmor(ArmorItem.Type.CHESTPLATE));
    public static final DeferredItem<CadetArmor> LEGS = ITEMS.register("cadet_leggings", () -> new CadetArmor(ArmorItem.Type.LEGGINGS));
    public static final DeferredItem<CadetArmor> BOOTS = ITEMS.register("cadet_boots", () -> new CadetArmor(ArmorItem.Type.BOOTS));
    public static ItemStack boundReroll(java.util.UUID owner){
        var stack=REROLL.toStack();var data=new net.minecraft.nbt.CompoundTag();data.putUUID("mha_owner",owner);
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(data));return stack;
    }
    public static ItemStack licence(net.minecraft.server.level.ServerPlayer player){var stack=LICENCE.toStack();var tag=new net.minecraft.nbt.CompoundTag();tag.putUUID("mha_owner",player.getUUID());tag.putString("mha_name",player.getGameProfile().getName());tag.putInt("mha_licence",dev.risingheroes.server.Gameplay.profile(player).licence);stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(tag));return stack;}
    static {TABS.register("rising_heroes",()->CreativeModeTab.builder().title(Component.literal("MHA: Rising Heroes / Development")).icon(NOTEBOOK::toStack).displayItems((params,out)->{out.accept(NOTEBOOK);out.accept(EXPLOSION);out.accept(THERMAL);out.accept(HARDENING);out.accept(ELECTRIC);out.accept(BLUEFLAME);out.accept(UMBRA);out.accept(KINETIC);out.accept(LICENCE);out.accept(REROLL);out.accept(HELMET);out.accept(CHEST);out.accept(LEGS);out.accept(BOOTS);}).build());}
}
