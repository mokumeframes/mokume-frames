package dev.risingheroes.test;

import dev.risingheroes.*;
import java.util.*;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Permission-controlled M0 harness. Uses the same server-tick ownership as later combat. */
@EventBusSubscriber(modid="mha")
public final class IntegrationScene {
    private record Pending(UUID player, long tick, long identity) {}
    private static final List<Pending> attacks=new ArrayList<>();
    @SubscribeEvent public static void commands(RegisterCommandsEvent e) {
        e.getDispatcher().register(Commands.literal("mha")
            .then(Commands.literal("integration").requires(s->s.hasPermission(2))
                .then(Commands.literal("suit").executes(c->{
                    var p=c.getSource().getPlayerOrException();
                    p.setItemSlot(EquipmentSlot.HEAD,ModItems.HELMET.toStack());
                    p.setItemSlot(EquipmentSlot.CHEST,ModItems.CHEST.toStack());
                    p.setItemSlot(EquipmentSlot.LEGS,ModItems.LEGS.toStack());
                    p.setItemSlot(EquipmentSlot.FEET,ModItems.BOOTS.toStack());return 1;}))
                .then(Commands.literal("target").executes(c->{
                    var p=c.getSource().getPlayerOrException();
                    var v=p.position().add(p.getLookAngle().multiply(3,0,3));
                    ArmorStand target=new ArmorStand(p.serverLevel(),v.x,v.y,v.z);
                    target.setNoGravity(true);target.setCustomName(Component.literal("Integration target"));
                    target.setCustomNameVisible(true);p.serverLevel().addFreshEntity(target);return 1;}))
                .then(Commands.literal("attack").executes(c->{
                    ServerPlayer p=c.getSource().getPlayerOrException();
                    if(attacks.stream().anyMatch(a->a.player.equals(p.getUUID())))return 0;
                    long id=p.getRandom().nextLong();
                    attacks.add(new Pending(p.getUUID(),p.serverLevel().getGameTime()+4,id));
                    PacketDistributor.sendToPlayersTrackingEntityAndSelf(p,new Network.Visual(p.getId(),"palm_burst",id,0));return 1;}))));
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e) {
        var iterator=attacks.iterator();
        while(iterator.hasNext()) {
            var a=iterator.next();var p=e.getServer().getPlayerList().getPlayer(a.player);
            if(p==null){iterator.remove();continue;}
            if(p.serverLevel().getGameTime()<a.tick)continue;
            iterator.remove();
            if(!p.isAlive())continue;
            Vec3 look=p.getLookAngle();
            int hit=dev.risingheroes.server.DamageResolver.hit(p,dev.risingheroes.core.Catalog.move("palm_burst"));
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(p,new Network.Visual(p.getId(),"palm_burst",a.identity,1));
            p.displayClientMessage(Component.literal("Server-owned integration attack: "+hit+" target(s)."),true);
        }
    }
}
