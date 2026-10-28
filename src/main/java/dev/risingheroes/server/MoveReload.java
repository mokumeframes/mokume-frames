package dev.risingheroes.server;

import dev.risingheroes.core.*;
import java.io.*;
import java.util.*;
import com.google.gson.reflect.TypeToken;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

@EventBusSubscriber(modid="mha")
public final class MoveReload extends SimplePreparableReloadListener<List<Move>> {
    @SubscribeEvent public static void register(AddReloadListenerEvent event){event.addListener(new MoveReload());}
    @Override protected List<Move> prepare(ResourceManager manager,ProfilerFiller profiler) {
        try(var reader=manager.getResourceOrThrow(ResourceLocation.parse("mha:mha/moves.json")).openAsReader()) {
            List<Move> moves=Catalog.JSON.fromJson(reader,new TypeToken<List<Move>>(){}.getType());
            Catalog.validate(moves);return moves;
        }catch(IOException e){throw new UncheckedIOException("Cannot reload MHA moves",e);}
    }
    @Override protected void apply(List<Move> input,ResourceManager manager,ProfilerFiller profiler){Catalog.replace(input);}
}
