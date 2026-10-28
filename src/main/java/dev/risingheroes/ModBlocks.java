package dev.risingheroes;

import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.*;

/** No BlockItems or loot tables: temporary constructs cannot become a source of inventory blocks. */
public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks("mha");
    private static BlockBehaviour.Properties properties(){return BlockBehaviour.Properties.ofFullCopy(Blocks.PACKED_ICE).strength(.5f,1).noLootTable().pushReaction(PushReaction.BLOCK);}
    public static final DeferredBlock<Block> ICE=BLOCKS.register("temporary_ice",()->new Block(properties()));
    public static final DeferredBlock<StairBlock> STAIRS=BLOCKS.register("temporary_ice_stairs",()->new StairBlock(ICE.get().defaultBlockState(),properties()));
}
