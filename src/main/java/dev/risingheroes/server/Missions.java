package dev.risingheroes.server;

import dev.risingheroes.*;
import dev.risingheroes.core.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.BlockEvent;

/** Private, bounded exercise cells. The mission journal owns progress; world actors only witness it. */
@EventBusSubscriber(modid="mha")
public final class Missions {
    public static final ResourceKey<Level> KEY=ResourceKey.create(Registries.DIMENSION,ResourceLocation.parse("mha:operations"));
    private static final TicketType<UUID> BUILD_TICKET=TicketType.create("mha_encounter_build",UUID::compareTo,40);
    private record Threat(Vec3 center,double radius,long release,LivingEntity source,float damage) {}
    private static final class Runtime {
        String session;int cursor=0,spawnedStep=-1;boolean preflight=true,ready=false,entered=false;
        ChunkPos chunk;List<EncounterSet.Block> blocks;UUID interaction;long held=-1;int holdTicks=0;
        long retryAfter=0,nextPath=0;List<Vec3> escortPath=List.of();int escortNode=0;final Map<UUID,Threat> threats=new HashMap<>();
        Runtime(MissionRun run){session=run.session;blocks=EncounterSet.blocks(run.arena);}
    }
    private static final Map<UUID,Runtime> RUNS=new HashMap<>();
    private static ServerLevel testLevel;
    public static void clearRuntime(){RUNS.clear();testLevel=null;}
    public static void bindTestLevel(ServerLevel level){if(!(level.getServer() instanceof net.minecraft.gametest.framework.GameTestServer))throw new IllegalStateException("Encounter test binding is forbidden on normal servers");testLevel=level;}
    public static ServerLevel level(MinecraftServer server){return testLevel!=null?testLevel:server.getLevel(KEY);}
    public static boolean isOperations(Level level){return level.dimension()==KEY||testLevel==level&&level.getServer() instanceof net.minecraft.gametest.framework.GameTestServer;}
    public static BlockPos origin(ServerPlayer p){return EncounterSet.origin(Gameplay.profile(p).mission.arena);}
    public static boolean prepared(ServerPlayer p){var r=RUNS.get(p.getUUID());return r!=null&&r.ready;}
    public static int buildCursor(ServerPlayer p){var r=RUNS.get(p.getUUID());return r==null?-1:r.cursor;}
    public static void start(ServerPlayer p,String id) {
        if(!Campus.atMentor(p)&&!Campus.atRegistrar(p))throw new IllegalArgumentException("Start an assignment at the academy registrar or instructor");
        var mission=MissionCatalog.get(id);String blocked=MissionRules.unavailable(Gameplay.profile(p),mission,Gameplay.now(p));if(!blocked.isBlank())throw new IllegalArgumentException(blocked);
        if(level(p.server)==null)throw new IllegalArgumentException("The operations dimension is unavailable; reload the world with this mod's data pack");
        int arena=Gameplay.ledger(p).reserveArena(p.getUUID(),id);
        Gameplay.ledger(p).commit(p.getUUID(),v->{MissionRules.start(v,id,Gameplay.now(p));v.mission.arena=arena;v.mission.returnDimension=p.level().dimension().location().toString();v.mission.returnX=p.getX();v.mission.returnY=p.getY();v.mission.returnZ=p.getZ();});
        RUNS.put(p.getUUID(),new Runtime(Gameplay.profile(p).mission));Gameplay.error(p,"Preparing your reserved encounter. No survival builds are used or reset.");Gameplay.sync(p,"");
    }
    public static void leave(ServerPlayer p) {
        var data=Gameplay.profile(p);if(data.mission.id.isBlank())return;
        if(p.isAlive()&&(Gameplay.now(p)<data.combatUntil||Gameplay.now(p)<data.exertionUntil))throw new IllegalArgumentException("Recover before leaving the encounter");
        var run=data.mission;var back=p.server.getLevel(ResourceKey.create(Registries.DIMENSION,ResourceLocation.parse(run.returnDimension)));
        if(back==null)back=p.server.overworld();double x=run.returnX,y=run.returnY,z=run.returnZ;
        if(!back.getWorldBorder().isWithinBounds(BlockPos.containing(x,y,z)))throw new IllegalArgumentException("Saved return position is outside the border");
        back.getChunkAt(BlockPos.containing(x,y,z));
        for(int n=0;n<16&&!back.noCollision(p,p.getBoundingBox().move(x-p.getX(),y-p.getY(),z-p.getZ()));n++)y++;
        if(!back.noCollision(p,p.getBoundingBox().move(x-p.getX(),y-p.getY(),z-p.getZ())))throw new IllegalArgumentException("Return position is blocked; encounter progress is retained");
        cleanup(p);Gameplay.cancel(p);p.teleportTo(back,x,y,z,p.getYRot(),p.getXRot());Gameplay.ledger(p).commit(p.getUUID(),MissionRules::leave);Gameplay.sync(p,"");
    }
    private static void release(ServerLevel level,ServerPlayer p,Runtime r){if(r.chunk!=null){level.getChunkSource().removeRegionTicket(BUILD_TICKET,r.chunk,0,p.getUUID());r.chunk=null;}}
    private static void cleanup(ServerPlayer p) {
        var r=RUNS.remove(p.getUUID());var level=level(p.server);if(level==null)return;
        if(r!=null)release(level,p,r);
        for(var actor:allOwned(p))actor.discard();
    }
    public static List<LivingEntity> actors(ServerPlayer p) {
        var run=Gameplay.profile(p).mission;if(run.arena<0||level(p.server)==null)return List.of();var o=EncounterSet.origin(run.arena);
        return level(p.server).getEntitiesOfClass(LivingEntity.class,new AABB(o.getX(),0,o.getZ(),o.getX()+48,16,o.getZ()+48),e->owned(p,e));
    }
    private static List<LivingEntity> allOwned(ServerPlayer p){var run=Gameplay.profile(p).mission;if(run.arena<0||level(p.server)==null)return List.of();var o=origin(p);return level(p.server).getEntitiesOfClass(LivingEntity.class,new AABB(o.getX(),0,o.getZ(),o.getX()+48,16,o.getZ()+48),e->e.getPersistentData().hasUUID("mha_owner")&&e.getPersistentData().getUUID("mha_owner").equals(p.getUUID()));}
    public static boolean mayDamage(ServerPlayer p,Entity e){return validCurrent(p,e)&&e.getPersistentData().getString("mha_role").startsWith("hostile");}
    private static boolean owned(ServerPlayer p,Entity e){var tag=e.getPersistentData();var run=Gameplay.profile(p).mission;return tag.hasUUID("mha_owner")&&tag.getUUID("mha_owner").equals(p.getUUID())&&tag.getString("mha_session").equals(run.session);}
    private static boolean validCurrent(ServerPlayer p,Entity e){return owned(p,e)&&e.getPersistentData().getInt("mha_stage")==Gameplay.profile(p).mission.step;}
    private static void tag(ServerPlayer p,LivingEntity e,String role) {
        var run=Gameplay.profile(p).mission;var tag=e.getPersistentData();tag.putUUID("mha_owner",p.getUUID());tag.putString("mha_session",run.session);tag.putInt("mha_stage",run.step);tag.putString("mha_role",role);tag.putBoolean("mha_actor",true);
        e.setUUID(UUID.nameUUIDFromBytes((p.getUUID()+":"+run.session+":"+run.step+":"+role).getBytes(java.nio.charset.StandardCharsets.UTF_8)));e.setCustomNameVisible(true);
        if(e instanceof Mob mob){mob.setPersistenceRequired();mob.setNoAi(true);mob.setCanPickUpLoot(false);}
    }
    private static void spawn(ServerPlayer p,Mission.Stage s) {
        for(var e:allOwned(p))e.discard();var l=level(p.server);var o=origin(p);
        if(s.type().equals("MOVE")||s.type().equals("DEFEND")) {
            ArmorStand marker=new ArmorStand(l,o.getX()+s.x()+.5,s.y(),o.getZ()+s.z()+.5);tag(p,marker,s.role());marker.setNoGravity(true);marker.setInvisible(true);marker.setInvulnerable(true);marker.setCustomName(Component.literal(s.type().equals("MOVE")?"Objective checkpoint":"Shelter / remain within four blocks"));l.addFreshEntity(marker);
        }
        if(Set.of("COMBAT","BOSS","DEFEND").contains(s.type())) {
            int count=s.type().equals("DEFEND")?2:s.count();
            for(int i=0;i<count;i++) {
                var actor=ModEntities.SENTINEL.get().create(l);if(actor==null)throw new IllegalStateException("Could not create exercise sentinel");
                tag(p,actor,"hostile_"+i);actor.getAttribute(Attributes.MAX_HEALTH).setBaseValue(s.type().equals("BOSS")?80:20);actor.setHealth(actor.getMaxHealth());
                actor.setPos(o.getX()+35.5+i*3,1,o.getZ()+35.5);actor.setCustomName(Component.literal(s.type().equals("BOSS")?"Bulwark / mod-original sentinel":"Exercise sentinel / shutdown relay available"));l.addFreshEntity(actor);
            }
        }
        if(!s.type().equals("MOVE")&&!s.type().equals("DEFEND")) {
            Villager actor=EntityType.VILLAGER.create(l);if(actor==null)throw new IllegalStateException("Could not create exercise contact");
            tag(p,actor,s.role());actor.setInvulnerable(true);
            double x=s.type().equals("ESCORT")?8.5:Set.of("COMBAT","BOSS").contains(s.type())?40.5:s.x()+.5;
            double z=s.type().equals("ESCORT")?8.5:Set.of("COMBAT","BOSS").contains(s.type())?8.5:s.z()+.5;
            actor.setPos(o.getX()+x,s.type().equals("ESCORT")?1:s.y(),o.getZ()+z);
            actor.setCustomName(Component.literal(switch(s.type()){case "AID"->"Injured trainee / hold first aid";case "ESCORT"->"Evacuee / stay close and lead";case "COMBAT","BOSS"->"Emergency shutdown / hold eight seconds";case "JUDGMENT"->"Decision console / open mission board";default->s.role().replace('_',' ')+" / interact";}));l.addFreshEntity(actor);
        }
    }
    private static boolean build(ServerPlayer p,Runtime r,ServerLevel level) {
        for(int budget=0;budget<192&&r.cursor<r.blocks.size();budget++) {
            var b=r.blocks.get(r.cursor);var chunk=new ChunkPos(b.pos());
            if(!chunk.equals(r.chunk)){release(level,p,r);r.chunk=chunk;level.getChunkSource().addRegionTicket(BUILD_TICKET,chunk,0,p.getUUID());level.getChunkAt(b.pos());return false;}
            var current=level.getBlockState(b.pos());
            boolean ownWaterFlow=!r.preflight&&current.is(net.minecraft.world.level.block.Blocks.WATER)&&b.state().is(net.minecraft.world.level.block.Blocks.WATER);
            if(!current.isAir()&&!current.equals(b.state())&&!ownWaterFlow){release(level,p,r);throw new IllegalArgumentException("Reserved encounter found a conflicting block at "+b.pos()+": "+current+" / expected "+b.state()+". Construction stopped; no foreign block was overwritten");}
            if(!r.preflight&&!current.equals(b.state()))level.setBlock(b.pos(),b.state(),2);r.cursor++;
        }
        if(r.cursor==r.blocks.size()){release(level,p,r);if(r.preflight){r.preflight=false;r.cursor=0;}else r.ready=true;}
        return r.ready;
    }
    public static void tick(MinecraftServer server) {
        for(var p:server.getPlayerList().getPlayers()) {
            var data=Gameplay.profile(p);if(data.mission.id.isBlank()){if(RUNS.containsKey(p.getUUID()))cleanup(p);continue;}
            var l=level(server);if(l==null)continue;
            var r=RUNS.computeIfAbsent(p.getUUID(),k->new Runtime(data.mission));
            if(Gameplay.now(p)<r.retryAfter)continue;
            try {
                if(!r.ready){if(!build(p,r,l))continue;}
                if(!r.entered){var o=origin(p);l.getChunkAt(o.offset(8,1,8));p.teleportTo(l,o.getX()+8.5,1,o.getZ()+8.5,0,0);r.entered=true;}
                if(!p.isAlive())continue;
                if(p.level()!=l){cleanup(p);Gameplay.ledger(p).commit(p.getUUID(),MissionRules::leave);continue;}
                var o=origin(p);if(p.getX()<o.getX()+1||p.getX()>o.getX()+47||p.getZ()<o.getZ()+1||p.getZ()>o.getZ()+47||p.getY()< -5){p.teleportTo(l,o.getX()+8.5,1,o.getZ()+8.5,0,0);damaged(p);}
                var s=MissionRules.stage(data);if(s==null){if(r.spawnedStep!=data.mission.step){for(var a:actors(p))a.discard();r.spawnedStep=data.mission.step;Gameplay.error(p,"Assignment complete. Your record and reward are saved. Leave through the mission board.");}continue;}
                if(r.spawnedStep!=data.mission.step){spawn(p,s);r.spawnedStep=data.mission.step;r.interaction=null;r.held=-1;r.holdTicks=0;r.threats.clear();r.escortPath=List.of();r.nextPath=0;Gameplay.error(p,s.text());Gameplay.sync(p,"");}
                if(s.type().equals("MOVE")&&p.distanceToSqr(o.getX()+s.x()+.5,s.y(),o.getZ()+s.z()+.5)<5)witness(p,s,"arrival");
                if(s.type().equals("ESCORT"))escort(p,r,s);
                if(s.type().equals("DEFEND")){boolean inside=p.distanceToSqr(o.getX()+s.x()+.5,s.y(),o.getZ()+s.z()+.5)<16;if(inside&&++r.holdTicks>=s.seconds()*20)witness(p,s,"shelter_held");else if(Gameplay.now(p)%20==0)Gameplay.error(p,"Shelter defence: "+r.holdTicks/20+" / "+s.seconds()+" seconds"+(inside?"":" / return to the marked shelter"));}
                if(r.interaction!=null)hold(p,r,s);
                pressure(p,r,s);
                if(!data.mission.evidence.contains("fire_valve")&&s.id().equals("fire_valve")&&p.getX()>o.getX()+30&&p.getX()<o.getX()+41&&p.getZ()>o.getZ()+13&&p.getZ()<o.getZ()+21&&Gameplay.now(p)%40==0)p.hurt(p.damageSources().onFire(),1);
            }catch(java.io.UncheckedIOException ex){r.retryAfter=Gameplay.now(p)+100;RisingHeroes.LOG.error("Encounter persistence failed; paused this player's encounter without granting its pending outcome",ex);Gameplay.error(p,"Saving failed. Your encounter is paused and will retry; no pending objective reward was awarded.");}
            catch(IllegalArgumentException ex){RisingHeroes.LOG.warn("Encounter stopped before outcome: {}",ex.getMessage());cleanup(p);Gameplay.ledger(p).commit(p.getUUID(),MissionRules::leave);Gameplay.error(p,ex.getMessage());}
        }
        RUNS.entrySet().removeIf(entry->{if(server.getPlayerList().getPlayer(entry.getKey())!=null)return false;var l=level(server);var r=entry.getValue();if(l!=null&&r.chunk!=null)l.getChunkSource().removeRegionTicket(BUILD_TICKET,r.chunk,0,entry.getKey());return true;});
    }
    private static void escort(ServerPlayer p,Runtime r,Mission.Stage s) {
        var npc=actors(p).stream().filter(a->a.getPersistentData().getString("mha_role").equals(s.role())).findFirst().orElse(null);if(npc==null)return;
        double distance=npc.distanceTo(p);
        if(distance>2&&distance<8) {
            if(Gameplay.now(p)>=r.nextPath){r.escortPath=escortPath(p,npc);r.escortNode=0;r.nextPath=Gameplay.now(p)+20;}
            if(r.escortNode<r.escortPath.size()) {
                Vec3 direction=r.escortPath.get(r.escortNode).subtract(npc.position());
                if(direction.horizontalDistance()<.15)r.escortNode++;
                else npc.move(MoverType.SELF,new Vec3(direction.x,0,direction.z).normalize().scale(Math.min(.10,direction.horizontalDistance())));
            }
        }
        var o=origin(p);if(npc.distanceToSqr(o.getX()+s.x()+.5,s.y(),o.getZ()+s.z()+.5)<5&&distance<6)witness(p,s,npc.getUUID().toString());
    }
    private static List<Vec3> escortPath(ServerPlayer p,LivingEntity npc) {
        var l=level(p.server);var o=origin(p);var start=BlockPos.containing(npc.getX(),1,npc.getZ());var goal=BlockPos.containing(p.getX(),1,p.getZ());
        ArrayDeque<BlockPos> queue=new ArrayDeque<>();Map<BlockPos,BlockPos> previous=new HashMap<>();queue.add(start);previous.put(start,start);
        while(!queue.isEmpty()&&previous.size()<=2304) {
            var at=queue.removeFirst();if(at.equals(goal)){List<Vec3> path=new ArrayList<>();for(var cursor=goal;!cursor.equals(start);cursor=previous.get(cursor))path.add(Vec3.atBottomCenterOf(cursor));Collections.reverse(path);return path;}
            for(var direction:new Direction[]{Direction.NORTH,Direction.EAST,Direction.SOUTH,Direction.WEST}) {
                var next=at.relative(direction);if(next.getX()<o.getX()+2||next.getX()>o.getX()+45||next.getZ()<o.getZ()+2||next.getZ()>o.getZ()+45||previous.containsKey(next)||!l.hasChunkAt(next))continue;
                if(!l.getBlockState(next).isAir()||!l.getBlockState(next.above()).isAir()||l.getBlockState(next.below()).isAir())continue;
                previous.put(next,at);queue.addLast(next);
            }
        }
        return List.of();
    }
    private static void hold(ServerPlayer p,Runtime r,Mission.Stage s) {
        var npc=level(p.server).getEntity(r.interaction);
        if(npc==null||!validCurrent(p,npc)||p.distanceToSqr(npc)>16||!p.hasLineOfSight(npc)){r.interaction=null;r.held=-1;return;}
        int seconds=Set.of("COMBAT","BOSS").contains(s.type())?8:s.seconds();
        if(Gameplay.now(p)-r.held>=seconds*20) {
            r.interaction=null;
            if(Set.of("COMBAT","BOSS").contains(s.type())){for(int i=0;i<s.count();i++)witness(p,s,"shutdown_"+i);}
            else witness(p,s,npc.getUUID().toString());
        }
    }
    private static void pressure(ServerPlayer p,Runtime r,Mission.Stage s) {
        if(!Set.of("COMBAT","BOSS","DEFEND").contains(s.type()))return;
        long now=Gameplay.now(p);var l=level(p.server);
        for(var actor:actors(p))if(actor.getPersistentData().getString("mha_role").startsWith("hostile")&&actor.isAlive()) {
            var threat=r.threats.get(actor.getUUID());
            if(threat!=null) {
                if(now>=threat.release()){if(p.distanceToSqr(threat.center())<=threat.radius()*threat.radius()&&actor.hasLineOfSight(p))p.hurt(p.damageSources().mobAttack(actor),threat.damage());if(actor instanceof dev.risingheroes.entity.TrainingSentinel sentinel)sentinel.charging(false);r.threats.remove(actor.getUUID());}
                continue;
            }
            if(actor.distanceTo(p)>4){Vec3 v=p.position().subtract(actor.position());actor.move(MoverType.SELF,new Vec3(v.x,0,v.z).normalize().scale(s.type().equals("BOSS")?.07:.09));}
            if(now%60!=0||actor.distanceTo(p)>12||!actor.hasLineOfSight(p))continue;
            boolean radial=s.type().equals("BOSS")&&actor.getHealth()<actor.getMaxHealth()*.6;
            Vec3 center=radial?actor.position():p.position();double radius=radial?5:1.75;
            r.threats.put(actor.getUUID(),new Threat(center,radius,now+20,actor,radial?5:2));
            if(actor instanceof dev.risingheroes.entity.TrainingSentinel sentinel)sentinel.charging(true);
            for(int i=0;i<24;i++){double a=i*Math.PI/12;l.sendParticles(ParticleTypes.CRIT,center.x+Math.cos(a)*radius,center.y+.15,center.z+Math.sin(a)*radius,1,0,0,0,0);}
            Gameplay.error(p,radial?"BULWARK: radial blast in one second / move beyond five blocks!":"INCOMING: leave the marked spot or use solid cover / one second!");
        }
    }
    private static void witness(ServerPlayer p,Mission.Stage s,String evidence){var before=Gameplay.profile(p);String session=before.mission.session;int licence=before.licence,tokens=before.liveTokens.size();Gameplay.ledger(p).commit(p.getUUID(),v->MissionRules.witness(v,session,s.type(),s.role(),evidence));var after=Gameplay.profile(p);if(after.licence>licence)p.getInventory().add(ModItems.licence(p));if(after.liveTokens.size()>tokens)p.getInventory().add(ModItems.boundReroll(p.getUUID()));Gameplay.sync(p,"");}
    public static void damaged(ServerPlayer p){var r=RUNS.get(p.getUUID());if(r!=null){r.interaction=null;r.held=-1;}if(!Gameplay.profile(p).mission.id.isBlank())Gameplay.ledger(p).commit(p.getUUID(),MissionRules::penalize);}
    public static void decision(ServerPlayer p,String choice) {
        var s=MissionRules.stage(Gameplay.profile(p));if(s==null||!s.type().equals("JUDGMENT")||!prepared(p)||p.level()!=level(p.server))throw new IllegalArgumentException("No decision station is active");
        var o=origin(p);if(p.distanceToSqr(o.getX()+s.x()+.5,s.y(),o.getZ()+s.z()+.5)>25)throw new IllegalArgumentException("Visit the decision console before answering");
        if(!choice.equals("rescue_first")){Gameplay.ledger(p).commit(p.getUUID(),MissionRules::penalize);throw new IllegalArgumentException("Prioritise immediate safety, call support and stabilise the casualty before pursuing a withdrawing threat. Try again");}
        witness(p,s,"judgment");
    }
    @SubscribeEvent public static void interact(PlayerInteractEvent.EntityInteract e) {
        if(!(e.getEntity() instanceof ServerPlayer p)||e.getHand()!=InteractionHand.MAIN_HAND||!e.getTarget().getPersistentData().getBoolean("mha_actor"))return;
        e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);
        if(!validCurrent(p,e.getTarget())||p.distanceToSqr(e.getTarget())>25||!p.hasLineOfSight(e.getTarget()))return;
        var s=MissionRules.stage(Gameplay.profile(p));var r=RUNS.get(p.getUUID());if(s==null||r==null||!e.getTarget().getPersistentData().getString("mha_role").equals(s.role()))return;
        if(s.type().equals("INTERACT"))witness(p,s,e.getTarget().getUUID().toString());
        else if(Set.of("AID","COMBAT","BOSS").contains(s.type())){if(r.interaction==null){r.interaction=e.getTarget().getUUID();r.held=Gameplay.now(p);}Gameplay.error(p,"Remain within four blocks and avoid damage until treatment/shutdown completes.");}
        else if(s.type().equals("JUDGMENT"))Gameplay.sync(p,"missions");
    }
    @SubscribeEvent public static void death(LivingDeathEvent e){if(e.getSource().getEntity() instanceof ServerPlayer p&&validCurrent(p,e.getEntity())){var s=MissionRules.stage(Gameplay.profile(p));if(s!=null&&Set.of("COMBAT","BOSS").contains(s.type())&&e.getEntity().getPersistentData().getString("mha_role").startsWith("hostile"))witness(p,s,e.getEntity().getUUID().toString());}}
    @SubscribeEvent public static void drops(LivingDropsEvent e){if(e.getEntity().getPersistentData().getBoolean("mha_actor"))e.getDrops().clear();}
    @SubscribeEvent public static void experience(LivingExperienceDropEvent e){if(e.getEntity().getPersistentData().getBoolean("mha_actor"))e.setDroppedExperience(0);}
    @SubscribeEvent public static void actorDamage(LivingIncomingDamageEvent e){if(e.getEntity().getPersistentData().getBoolean("mha_actor")&&(!(e.getSource().getEntity() instanceof ServerPlayer p)||!mayDamage(p,e.getEntity())))e.setCanceled(true);}
    @SubscribeEvent public static void joining(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent e){if(e.getLevel() instanceof ServerLevel l&&e.getEntity().getPersistentData().getBoolean("mha_actor")){var tag=e.getEntity().getPersistentData();if(!tag.hasUUID("mha_owner")){e.setCanceled(true);return;}var data=HeroLedger.get(l.getServer()).profile(tag.getUUID("mha_owner"));if(data.mission.id.isBlank()||!data.mission.session.equals(tag.getString("mha_session"))||data.mission.step!=tag.getInt("mha_stage"))e.setCanceled(true);}}
    @SubscribeEvent public static void attack(AttackEntityEvent e){if(e.getTarget().getPersistentData().getBoolean("mha_actor")&&e.getEntity() instanceof ServerPlayer p){if(!validCurrent(p,e.getTarget())||!e.getTarget().getPersistentData().getString("mha_role").startsWith("hostile")){e.setCanceled(true);if(validCurrent(p,e.getTarget()))damaged(p);}}}
    @SubscribeEvent public static void breakBlock(BlockEvent.BreakEvent e){if(e.getLevel() instanceof Level l&&l.dimension()==KEY&&!e.getPlayer().isCreative())e.setCanceled(true);}
    @SubscribeEvent public static void placeBlock(BlockEvent.EntityPlaceEvent e){if(e.getLevel() instanceof Level l&&l.dimension()==KEY&&!(e.getEntity() instanceof net.minecraft.world.entity.player.Player p&&p.isCreative()))e.setCanceled(true);}
}
