package dev.risingheroes.server;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Original 48x48 modular exercise set. Each owner/mission reserves an immutable cell. */
public final class EncounterSet {
    public record Block(BlockPos pos,BlockState state) {}
    public static BlockPos origin(int arena){return new BlockPos(512+(arena%64)*64,0,512+(arena/64)*64);}
    public static List<Block> blocks(int arena) {
        var origin=origin(arena);Map<BlockPos,BlockState> blocks=new HashMap<>();
        for(int x=0;x<48;x++)for(int z=0;z<48;z++) {
            blocks.put(origin.offset(x,0,z),((x%8==0||z%8==0)?Blocks.SMOOTH_STONE:Blocks.LIGHT_GRAY_CONCRETE).defaultBlockState());
            if(x==0||x==47||z==0||z==47)for(int y=1;y<5;y++)blocks.put(origin.offset(x,y,z),Blocks.WHITE_CONCRETE.defaultBlockState());
        }
        // Flood lane with a raised rescue platform and two stairs to an accessible alternative route.
        for(int x=5;x<=16;x++)for(int z=24;z<=34;z++)blocks.put(origin.offset(x,1,z),Blocks.WATER.defaultBlockState());
        for(int x=13;x<=15;x++)for(int z=29;z<=31;z++)blocks.put(origin.offset(x,1,z),Blocks.CYAN_CONCRETE.defaultBlockState());
        for(int z=23;z<=35;z++)for(int x:new int[]{4,17})blocks.put(origin.offset(x,1,z),Blocks.SMOOTH_QUARTZ.defaultBlockState());
        for(int x=4;x<=17;x++)for(int z:new int[]{23,35})blocks.put(origin.offset(x,1,z),Blocks.SMOOTH_QUARTZ.defaultBlockState());
        for(int x=12;x<=16;x++){blocks.put(origin.offset(x,1,28),Blocks.SMOOTH_QUARTZ.defaultBlockState());blocks.put(origin.offset(x,1,32),Blocks.SMOOTH_QUARTZ.defaultBlockState());}
        // Hazard lane, rubble wall with walkable gaps, workshop and safe shelter marker.
        for(int x=30;x<=40;x++)for(int z=13;z<=20;z++)blocks.put(origin.offset(x,0,z),Blocks.RED_TERRACOTTA.defaultBlockState());
        for(int z=16;z<=29;z++)if(z!=21&&z!=22)for(int y=1;y<=3;y++)blocks.put(origin.offset(24,y,z),Blocks.STONE_BRICKS.defaultBlockState());
        for(int z:new int[]{12,25,38})for(int x=17;x<=20;x++)for(int y=1;y<=3;y++)blocks.put(origin.offset(x,y,z),Blocks.SMOOTH_QUARTZ.defaultBlockState());
        for(int x=33;x<=39;x++)for(int z=23;z<=29;z++)blocks.put(origin.offset(x,0,z),Blocks.CYAN_TERRACOTTA.defaultBlockState());
        blocks.put(origin.offset(40,1,8),Blocks.CRAFTING_TABLE.defaultBlockState());blocks.put(origin.offset(42,1,8),Blocks.SMITHING_TABLE.defaultBlockState());
        for(int x:new int[]{4,22,44})for(int z:new int[]{4,22,44}){blocks.put(origin.offset(x,1,z),Blocks.SMOOTH_QUARTZ.defaultBlockState());blocks.put(origin.offset(x,2,z),Blocks.SEA_LANTERN.defaultBlockState());}
        var result=new ArrayList<Block>();blocks.forEach((p,s)->result.add(new Block(p,s)));
        result.sort(Comparator.<Block>comparingInt(b->b.state().getFluidState().isEmpty()?0:1).thenComparingInt(b->b.pos().getX()>>4).thenComparingInt(b->b.pos().getZ()>>4).thenComparingInt(b->b.pos().getY()));return result;
    }
}
