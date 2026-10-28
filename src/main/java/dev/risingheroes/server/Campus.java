package dev.risingheroes.server;

import dev.risingheroes.MhaConfig;
import dev.risingheroes.core.*;
import java.io.*;
import java.util.*;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid="mha")
public final class Campus {
    public static final ResourceKey<Level> KEY=ResourceKey.create(Registries.DIMENSION,ResourceLocation.parse("mha:academy"));
    public record Contact(String id,String name,double[] pos) {}
    public record Blueprint(int version,int[] size,double[] entry,int[][] course,String[] palette,int[][] blocks,Contact[] contacts,double[][] targets) {}
    private static Blueprint blueprint;
    private static List<BlockState> palette;
    private static final TicketType<UUID> BUILD_TICKET=TicketType.create("mha_academy_build",UUID::compareTo,40);
    private static final class Build {int cursor=0;boolean preflight=true;ChunkPos chunk;final UUID identity=UUID.randomUUID();}
    private static Build build;
    private static ServerLevel constructionTestLevel;
    public static final class State extends SavedData {
        int phase=0;
        static State load(CompoundTag tag,HolderLookup.Provider provider){State s=new State();s.phase=tag.getInt("phase");return s;}
        @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider provider){tag.putInt("phase",phase);return tag;}
    }
    public static State state(MinecraftServer server){return server.overworld().getDataStorage().computeIfAbsent(new SavedData.Factory<>(State::new,State::load),"mha_academy");}
    public static boolean ready(MinecraftServer server){return state(server).phase==2;}
    public static boolean isAcademy(Level level){return level.dimension()==KEY||constructionTestLevel==level&&level.getServer() instanceof net.minecraft.gametest.framework.GameTestServer;}
    public static String buildStatus(MinecraftServer server){return "phase="+state(server).phase+", cursor="+(build==null?-1:build.cursor)+", preflight="+(build!=null&&build.preflight);}
    public static void clearRuntime(){blueprint=null;palette=null;build=null;constructionTestLevel=null;}
    /** Minecraft's GameTestServer discards datapack dimensions when constructing its flat preset.
     * This explicit test hook exercises the same bounded builder in that disposable test world only. */
    public static void prepareConstructionTest(ServerLevel level) {
        if(!(level.getServer() instanceof net.minecraft.gametest.framework.GameTestServer))throw new IllegalStateException("Construction test hook is forbidden in a normal server");
        constructionTestLevel=level;var s=state(level.getServer());s.phase=1;s.setDirty();build=new Build();blueprint(level.getServer());
    }
    public static Blueprint blueprint(MinecraftServer server) {
        if(blueprint==null) {
            try(var reader=server.getResourceManager().getResourceOrThrow(ResourceLocation.parse("mha:mha/maps/academy_start.json")).openAsReader()){
                blueprint=Catalog.JSON.fromJson(reader,Blueprint.class);
                if(blueprint.version!=1||blueprint.blocks.length>40000)throw new IllegalArgumentException("Unsupported or oversized academy blueprint");
                palette=new ArrayList<>();
                for(String entry:blueprint.palette)palette.add(BlockStateParser.parseForBlock(server.registryAccess().lookupOrThrow(Registries.BLOCK),entry,false).blockState());
                Arrays.sort(blueprint.blocks,Comparator.<int[]>comparingInt(v->v[0]>>4).thenComparingInt(v->v[2]>>4).thenComparingInt(v->v[1]).thenComparingInt(v->v[0]).thenComparingInt(v->v[2]));
            }catch(Exception ex){throw new IllegalStateException("Cannot read authored academy blueprint",ex);}
        }
        return blueprint;
    }
    public static boolean protectedAt(BlockPos pos,Level level){return MhaConfig.PROTECT_CAMPUS.get()&&level.dimension()==KEY&&pos.getX()<90&&pos.getX()>=0&&pos.getZ()>=0&&pos.getZ()<104;}
    @SubscribeEvent public static void breaking(BlockEvent.BreakEvent e){if(e.getLevel() instanceof Level level&&protectedAt(e.getPos(),level)&&!e.getPlayer().isCreative())e.setCanceled(true);}
    @SubscribeEvent public static void placing(BlockEvent.EntityPlaceEvent e){if(e.getLevel() instanceof Level level&&protectedAt(e.getPos(),level)&&!(e.getEntity() instanceof net.minecraft.world.entity.player.Player p&&p.isCreative()))e.setCanceled(true);}
    public static void travel(ServerPlayer p) {
        if(!Gameplay.profile(p).mission.id.isBlank())throw new IllegalArgumentException("Leave the assignment through its mission board before travelling");
        if(Gameplay.now(p)<Gameplay.profile(p).combatUntil||Gameplay.now(p)<Gameplay.profile(p).exertionUntil)throw new IllegalArgumentException("Travel is unavailable during combat/recovery");
        if(p.level().dimension()==KEY)return;
        var s=state(p.server);
        if(s.phase==3)throw new IllegalArgumentException("Academy construction found a conflicting block and stopped safely. Ask an administrator to inspect it");
        if(s.phase!=2){blueprint(p.server);s.phase=1;s.setDirty();if(build==null)build=new Build();Gameplay.error(p,"Preparing the academy in its own dimension. Retry travel in a few seconds; your world is untouched.");return;}
        var level=p.server.getLevel(KEY);if(level==null)throw new IllegalArgumentException("Academy dimension is unavailable; reload the world with the mod's data pack");
        Gameplay.ledger(p).commit(p.getUUID(),v->{v.returnDimension=p.level().dimension().location().toString();v.returnX=p.getX();v.returnY=p.getY();v.returnZ=p.getZ();});
        var b=blueprint(p.server);p.teleportTo(level,b.entry[0],b.entry[1],b.entry[2],180,0);
        Gameplay.error(p,"U.A. starter campus / original compressed layout. Speak to Registrar Aki in the central hall.");
    }
    public static void returnFrom(ServerPlayer p) {
        if(!Gameplay.profile(p).mission.id.isBlank())throw new IllegalArgumentException("Leave the assignment through its mission board before travelling");
        if(p.level().dimension()!=KEY)return;
        var data=Gameplay.profile(p);
        if(!data.activeTrial.isEmpty())throw new IllegalArgumentException("Finish or cancel your trial before leaving the academy");
        if(Gameplay.now(p)<data.combatUntil||Gameplay.now(p)<data.exertionUntil)throw new IllegalArgumentException("Leave combat/recovery before travelling");
        var level=data.returnDimension.isEmpty()?p.server.overworld():p.server.getLevel(ResourceKey.create(Registries.DIMENSION,ResourceLocation.parse(data.returnDimension)));
        if(level==null)level=p.server.overworld();
        double x=data.returnX,y=data.returnY,z=data.returnZ;
        if(!Double.isFinite(x+y+z)||!level.getWorldBorder().isWithinBounds(BlockPos.containing(x,y,z))||data.returnDimension.isEmpty()) {var pos=level.getSharedSpawnPos();x=pos.getX()+.5;y=pos.getY()+1;z=pos.getZ()+.5;}
        level.getChunkAt(BlockPos.containing(x,y,z));
        for(int attempt=0;attempt<16&&!level.noCollision(p,p.getBoundingBox().move(x-p.getX(),y-p.getY(),z-p.getZ()));attempt++)y++;
        if(!level.noCollision(p,p.getBoundingBox().move(x-p.getX(),y-p.getY(),z-p.getZ())))throw new IllegalArgumentException("Return position is obstructed. Use a safe operator teleport; no unsafe arrival was attempted");
        p.teleportTo(level,x,y,z,p.getYRot(),p.getXRot());
    }
    public static boolean atRegistrar(ServerPlayer p){return isAcademy(p.level())&&p.distanceToSqr(39,1,38)<36;}
    public static boolean atMentor(ServerPlayer p){return isAcademy(p.level())&&p.distanceToSqr(75.5,1,52.5)<36;}
    public static void tick(MinecraftServer server) {
        var s=state(server);if(s.phase!=1)return;
        var level=constructionTestLevel!=null?constructionTestLevel:server.getLevel(KEY);if(level==null)return;
        var b=blueprint(server);if(build==null)build=new Build();
        for(int budget=0;budget<256&&build.cursor<b.blocks.length;budget++) {
            var entry=b.blocks[build.cursor];var pos=new BlockPos(entry[0],entry[1],entry[2]);var wanted=palette.get(entry[3]);
            ChunkPos chunk=new ChunkPos(pos);
            if(!chunk.equals(build.chunk)) {
                releaseChunk(level);
                build.chunk=chunk;level.getChunkSource().addRegionTicket(BUILD_TICKET,chunk,0,build.identity);
                level.getChunkAt(pos);return; // at most one short-lived, full-chunk ticket at a time
            }
            var current=level.getBlockState(pos);
            if(!current.isAir()&&!current.equals(wanted)){dev.risingheroes.RisingHeroes.LOG.error("Academy placement stopped at {}: found {}, expected {}",pos,current,wanted);releaseChunk(level);s.phase=3;s.setDirty();build=null;return;}
            if(!build.preflight&&current.isAir())level.setBlock(pos,wanted,2);
            build.cursor++;
        }
        if(build.cursor>=b.blocks.length) {
            releaseChunk(level);
            if(build.preflight){build.cursor=0;build.preflight=false;}
            else {spawnContacts(level,b);s.phase=2;s.setDirty();build=null;}
        }
    }
    private static void releaseChunk(ServerLevel level){if(build!=null&&build.chunk!=null){level.getChunkSource().removeRegionTicket(BUILD_TICKET,build.chunk,0,build.identity);build.chunk=null;}}
    private static void spawnContacts(ServerLevel level,Blueprint b) {
        for(var contact:b.contacts) {
            if(!level.getEntitiesOfClass(Villager.class,new net.minecraft.world.phys.AABB(contact.pos[0]-3,0,contact.pos[2]-3,contact.pos[0]+3,8,contact.pos[2]+3),v->v.getPersistentData().getString("mha_contact").equals(contact.id)).isEmpty())continue;
            Villager npc=EntityType.VILLAGER.create(level);if(npc==null)throw new IllegalStateException("Could not create academy contact");
            npc.setUUID(UUID.nameUUIDFromBytes(("mha:contact:"+level.dimension().location()+":"+contact.id).getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            npc.setPos(contact.pos[0]+.5,contact.pos[1],contact.pos[2]+.5);npc.setNoAi(true);npc.setInvulnerable(true);npc.setPersistenceRequired();
            npc.setCustomName(Component.literal(contact.name));npc.setCustomNameVisible(true);npc.getPersistentData().putString("mha_contact",contact.id);level.addFreshEntity(npc);
        }
        int i=0;for(var pos:b.targets) {
            ArmorStand target=new ArmorStand(level,pos[0]+.5,pos[1],pos[2]+.5);target.setInvulnerable(true);target.setNoGravity(true);
            target.setUUID(UUID.nameUUIDFromBytes(("mha:target:"+level.dimension().location()+":"+i).getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            target.getPersistentData().putBoolean("mha_target",true);target.getPersistentData().putString("mha_lane","lane_"+i++);target.setCustomName(Component.literal("Control target / aim a technique"));target.setCustomNameVisible(true);level.addFreshEntity(target);
        }
    }
}
