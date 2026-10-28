package dev.risingheroes.client;

import dev.risingheroes.Network;
import dev.risingheroes.core.*;
import dev.risingheroes.server.Gameplay.ClientView;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.*;

public final class ClientState {
    public static ClientView view;
    public static String pending="";
    private static long sequence=0,levelTick=0;
    private static List<Move> serverMoves=List.of();private static String revision="";
    public static void catalogue(Network.MoveCatalogue packet){List<Move> moves=Catalog.JSON.fromJson(packet.json(),new com.google.gson.reflect.TypeToken<List<Move>>(){}.getType());serverMoves=List.copyOf(Catalog.validate(moves).values());revision=packet.revision();}
    public static void receive(Network.Snapshot packet) {
        boolean firstSnapshot=view==null;
        view=Catalog.JSON.fromJson(packet.json(),ClientView.class);
        var level=Minecraft.getInstance().level;levelTick=level==null?0:level.getGameTime();
        if(!view.open().isEmpty())pending=view.open();
        else if(firstSnapshot&&!view.profile().revealSeen)pending="reveal";
    }
    public static HeroProfile profile(){return view==null?new HeroProfile():view.profile();}
    public static List<Move> moves(){return view==null?List.copyOf(Catalog.moves().values()):revision.equals(view.catalogue())?serverMoves:List.of();}
    public static Move move(String id){return moves().stream().filter(m->m.id().equals(id)).findFirst().orElse(null);}
    public static long now(){var level=Minecraft.getInstance().level;return view==null?0:view.tick()+Math.max(0,level==null?0:level.getGameTime()-levelTick);}
    public static void send(int op,int slot,String argument,String offer) {
        if(Minecraft.getInstance().getConnection()!=null)PacketDistributor.sendToServer(new Network.Action(++sequence,op,slot,argument,offer));
    }
    public static void clear(){view=null;pending="";sequence=0;levelTick=0;serverMoves=List.of();revision="";}
}
