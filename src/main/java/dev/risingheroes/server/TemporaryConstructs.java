package dev.risingheroes.server;

import dev.risingheroes.*;
import dev.risingheroes.core.*;
import java.util.*;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.*;

public final class TemporaryConstructs {
    private static int cursor=0;
    public static int place(ServerPlayer p,Move move) {
        var level=p.serverLevel();var forward=p.getDirection();var right=forward.getClockWise();
        var base=p.blockPosition().relative(forward,move.shape().equals("WALL")?3:2);Map<BlockPos,BlockState> wanted=new LinkedHashMap<>();
        if(move.shape().equals("WALL")){for(int x=-1;x<=1;x++)for(int y=0;y<3;y++)wanted.put(base.relative(right,x).above(y),ModBlocks.ICE.get().defaultBlockState());}
        else {for(int step=0;step<4;step++)for(int side=0;side<2;side++)wanted.put(base.relative(forward,step).relative(right,side).above(step),ModBlocks.STAIRS.get().defaultBlockState().setValue(StairBlock.FACING,forward).setValue(StairBlock.HALF,Half.BOTTOM).setValue(StairBlock.SHAPE,StairsShape.STRAIGHT));}
        for(var entry:wanted.entrySet()) {
            var at=entry.getKey();if(!level.hasChunkAt(at)||!level.getWorldBorder().isWithinBounds(at)||at.getY()<level.getMinBuildHeight()||at.getY()>=level.getMaxBuildHeight()||!level.getBlockState(at).isAir()||Campus.protectedAt(at,level)||level.getBlockEntity(at)!=null){Gameplay.error(p,"Ice needs clear, loaded and unprotected space. No terrain was replaced.");return 0;}
            if(level.dimension()==Missions.KEY){var o=Missions.origin(p);if(Gameplay.profile(p).mission.id.isBlank()||at.getX()<o.getX()+2||at.getX()>o.getX()+45||at.getZ()<o.getZ()+2||at.getZ()>o.getZ()+45)return 0;}
            var shape=entry.getValue().getCollisionShape(level,at).move(at.getX(),at.getY(),at.getZ());
            if(level.getEntities((net.minecraft.world.entity.Entity)null,new AABB(at),e->e.isAlive()&&e.getBoundingBox().intersects(shape.bounds())).size()>0){Gameplay.error(p,"An entity occupies the intended ice. No collision block was placed.");return 0;}
        }
        List<ConstructPlan> plans=new ArrayList<>();wanted.forEach((at,state)->plans.add(new ConstructPlan(level.dimension().location().toString(),at.getX(),at.getY(),at.getZ(),BlockStateParser.serialize(state),Gameplay.now(p)+move.cooldown()*2L,p.getUUID())));
        Gameplay.ledger(p).planConstructs(plans);wanted.forEach((at,state)->level.setBlock(at,state,2));return wanted.size();
    }
    public static void tick(MinecraftServer server) {
        var ledger=HeroLedger.get(server);var plans=ledger.constructs();if(plans.isEmpty()){cursor=0;return;}
        List<String> done=new ArrayList<>();long now=server.overworld().getGameTime();
        for(int i=0;i<Math.min(16,plans.size());i++) {
            var plan=plans.get(Math.floorMod(cursor++,plans.size()));if(now<plan.expiry())continue;
            var l=server.getLevel(ResourceKey.create(Registries.DIMENSION,ResourceLocation.parse(plan.dimension())));var pos=new BlockPos(plan.x(),plan.y(),plan.z());if(l==null||!l.hasChunkAt(pos))continue;
            String current=BlockStateParser.serialize(l.getBlockState(pos));if(current.equals(plan.expected()))l.setBlock(pos,Blocks.AIR.defaultBlockState(),3);done.add(plan.key());
        }
        if(!done.isEmpty())ledger.forgetConstructs(done);
    }
    public static void clearOwner(ServerPlayer p) {
        var ledger=Gameplay.ledger(p);ledger.expireConstructs(p.getUUID(),Gameplay.now(p));List<String> done=new ArrayList<>();
        for(var plan:ledger.constructs())if(plan.owner().equals(p.getUUID())) {
            var l=p.server.getLevel(ResourceKey.create(Registries.DIMENSION,ResourceLocation.parse(plan.dimension())));var pos=new BlockPos(plan.x(),plan.y(),plan.z());
            if(l!=null&&l.hasChunkAt(pos)){if(BlockStateParser.serialize(l.getBlockState(pos)).equals(plan.expected()))l.setBlock(pos,Blocks.AIR.defaultBlockState(),3);done.add(plan.key());}
            // Unloaded cells are already durably expired. They are cleaned on load without forcing chunks.
        }
        if(!done.isEmpty())ledger.forgetConstructs(done);
    }
}
