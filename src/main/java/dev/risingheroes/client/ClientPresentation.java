package dev.risingheroes.client;

import dev.risingheroes.Network;
import dev.risingheroes.ModItems;
import dev.risingheroes.equipment.EquipmentBridge;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import dev.risingheroes.client.ui.MenuEvents;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import com.lowdragmc.photon.client.fx.*;
import com.lowdragmc.photon.client.gameobject.emitter.particle.ParticleEmitter;
import com.lowdragmc.photon.client.gameobject.emitter.data.number.NumberFunction;
import com.lowdragmc.photon.client.gameobject.emitter.data.number.NumberFunction3;

@Mod(value = "mha", dist = Dist.CLIENT)
public final class ClientPresentation {
    public static final ResourceLocation LAYER = ResourceLocation.parse("mha:casting");
    public ClientPresentation(IEventBus bus) {
        EquipmentBridge.ARMOR=consumer->consumer.accept(new GeoRenderProvider(){
            private CadetRenderer renderer;
            @Override public <T extends LivingEntity> HumanoidModel<?> getGeoArmorRenderer(T entity,ItemStack stack,EquipmentSlot slot,HumanoidModel<T> original){
                if(renderer==null)renderer=new CadetRenderer();
                return renderer;
            }
        });
        bus.addListener(ClientControls::register);
        bus.addListener((net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers e)->e.registerEntityRenderer(dev.risingheroes.ModEntities.SENTINEL.get(),SentinelRenderer::new));
        bus.addListener((RegisterGuiLayersEvent e)->e.registerAboveAll(ResourceLocation.parse("mha:combat"),CombatHud::render));
        NeoForge.EVENT_BUS.addListener(ClientControls::tick);
        NeoForge.EVENT_BUS.addListener(SurvivalHotbar::before);
        NeoForge.EVENT_BUS.addListener(MenuEvents::opening);
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(LAYER, 1800, p -> new ModifierLayer<IAnimation>());
        Network.visualReceiver = ClientPresentation::visual;
        Network.snapshotReceiver = ClientState::receive;
        Network.catalogueReceiver = ClientState::catalogue;
    }
    @SuppressWarnings("unchecked")
    private static void visual(Network.Visual msg) {
        var mc=Minecraft.getInstance();
        if(mc.level==null || !(mc.level.getEntity(msg.entity()) instanceof AbstractClientPlayer player)) return;
        CastAudio.receive(msg,player);
        var layer=(ModifierLayer<IAnimation>)PlayerAnimationAccess.getPlayerAssociatedData(player).get(LAYER);
        if(layer==null) return;
        if(msg.phase()==2) {layer.setAnimation(null);return;}
        if(msg.phase()==0||msg.phase()==3) {
            var animation=PlayerAnimationRegistry.getAnimation(ResourceLocation.fromNamespaceAndPath("mha",msg.move()));
            if(animation instanceof dev.kosmx.playerAnim.core.data.KeyframeAnimation keyframes)layer.setAnimation(new dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer(keyframes,Math.max(0,Math.min(msg.age(),400))));else if(animation!=null)layer.setAnimation(animation.playAnimation());
        } else if(msg.phase()==1) {
            QuirkEffects.burst(player,msg.move());
        }
    }
}
