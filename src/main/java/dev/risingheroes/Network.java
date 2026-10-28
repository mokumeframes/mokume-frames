package dev.risingheroes;

import java.util.function.Consumer;
import dev.risingheroes.server.Gameplay;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class Network {
    /** Installed by the physical client only. Dedicated servers never link a rendering class. */
    public static Consumer<Visual> visualReceiver = ignored -> {};
    public static Consumer<Snapshot> snapshotReceiver = ignored -> {};
    public static Consumer<MoveCatalogue> catalogueReceiver = ignored -> {};
    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar=event.registrar("6");
        registrar.playToClient(Visual.TYPE, Visual.CODEC, (data, context) -> visualReceiver.accept(data));
        registrar.playToClient(Snapshot.TYPE, Snapshot.CODEC, (data, context) -> snapshotReceiver.accept(data));
        registrar.playToClient(MoveCatalogue.TYPE, MoveCatalogue.CODEC, (data, context) -> catalogueReceiver.accept(data));
        registrar.playToServer(Action.TYPE,Action.CODEC,(data,context)->{
            if(context.player() instanceof net.minecraft.server.level.ServerPlayer player)Gameplay.action(player,data);
        });
    }
    public record Visual(int entity, String move, long identity, int phase,int age) implements CustomPacketPayload {
        public Visual(int entity,String move,long identity,int phase){this(entity,move,identity,phase,0);}
        public static final Type<Visual> TYPE = new Type<>(ResourceLocation.parse("mha:visual"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Visual> CODEC = StreamCodec.of(
            (buf, p) -> {buf.writeVarInt(p.entity);buf.writeUtf(p.move,64);buf.writeLong(p.identity);buf.writeVarInt(p.phase);buf.writeVarInt(p.age);},
            buf -> new Visual(buf.readVarInt(),buf.readUtf(64),buf.readLong(),buf.readVarInt(),buf.readVarInt()));
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
    public record Snapshot(String json) implements CustomPacketPayload {
        public static final Type<Snapshot> TYPE=new Type<>(ResourceLocation.parse("mha:snapshot"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Snapshot> CODEC=StreamCodec.of((b,p)->b.writeUtf(p.json,131072),b->new Snapshot(b.readUtf(131072)));
        @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }
    public record MoveCatalogue(String revision,String json) implements CustomPacketPayload {
        public static final Type<MoveCatalogue> TYPE=new Type<>(ResourceLocation.parse("mha:move_catalogue"));
        public static final StreamCodec<RegistryFriendlyByteBuf,MoveCatalogue> CODEC=StreamCodec.of((b,p)->{b.writeUtf(p.revision,64);b.writeUtf(p.json,262144);},b->new MoveCatalogue(b.readUtf(64),b.readUtf(262144)));
        @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }
    public record Action(long sequence,int operation,int slot,String argument,String offer) implements CustomPacketPayload {
        public static final Type<Action> TYPE=new Type<>(ResourceLocation.parse("mha:action"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Action> CODEC=StreamCodec.of((b,p)->{b.writeLong(p.sequence);b.writeVarInt(p.operation);b.writeVarInt(p.slot);b.writeUtf(p.argument,64);b.writeUtf(p.offer,64);},b->new Action(b.readLong(),b.readVarInt(),b.readVarInt(),b.readUtf(64),b.readUtf(64)));
        @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }
}
