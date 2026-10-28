package dev.risingheroes.test;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.level.GameType;

/** The vanilla mock overrides isCreative() to true. This connected test player uses real survival rules. */
public final class TestPlayers {
    public static ServerPlayer survival(GameTestHelper helper) {
        var server=helper.getLevel().getServer();
        if(!(server instanceof net.minecraft.gametest.framework.GameTestServer))throw new IllegalStateException("Test players may only be created in the dedicated GameTest engine");
        var cookie=CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(),"hero-test"),false);
        var player=new ServerPlayer(server,helper.getLevel(),cookie.gameProfile(),cookie.clientInformation());
        var connection=new Connection(PacketFlow.SERVERBOUND);new EmbeddedChannel(connection);server.getPlayerList().placeNewPlayer(connection,player,cookie);
        player.setGameMode(GameType.SURVIVAL);return player;
    }
}
