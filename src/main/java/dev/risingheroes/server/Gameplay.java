package dev.risingheroes.server;

import dev.risingheroes.*;
import dev.risingheroes.core.*;
import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid="mha")
public final class Gameplay {
    public static final int CAST=0,EQUIP=1,REROLL=2,CHOOSE=3,ACK_REVEAL=4,TRAVEL=5,RETURN=6,BOOKMARK=7,EXCHANGE=8,TRIAL_START=9,TRIAL_CANCEL=10,PRESET_SAVE=11,PRESET_LOAD=12;
    public static final int MISSION_START=13,MISSION_LEAVE=14,MISSION_DECISION=15;
    public record ClientView(HeroProfile profile,long tick,String open,String objective,List<String> pool,String catalogue) {}
    private record Cast(Move move,long start,long identity,ResourceKey<Level> dimension) {}
    private static final class Session {long sequence=-1,lastAction=-100,lastError=-100;String catalogue="";}
    private static Map<String,Move> cachedSource;private static double cachedScale=Double.NaN;
    private static Network.MoveCatalogue cachedCatalogue;
    private static Network.MoveCatalogue catalogue() {
        double scale=MhaConfig.DAMAGE_SCALE.get();if(cachedSource==Catalog.moves()&&cachedScale==scale&&cachedCatalogue!=null)return cachedCatalogue;
        String json=Catalog.JSON.toJson(Catalog.moves().values().stream().map(Gameplay::effective).toList());
        cachedSource=Catalog.moves();cachedScale=scale;cachedCatalogue=new Network.MoveCatalogue(UUID.nameUUIDFromBytes(json.getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString(),json);return cachedCatalogue;
    }
    private static final Map<UUID,Cast> CASTS=new HashMap<>();
    private static final Map<UUID,Session> SESSIONS=new HashMap<>();
    private static final Map<UUID,Set<UUID>> OBSERVERS=new HashMap<>();
    private static void visual(ServerPlayer actor,Network.Visual packet) {
        Set<UUID> recipients=new HashSet<>(OBSERVERS.getOrDefault(actor.getUUID(),Set.of()));recipients.add(actor.getUUID());
        for(UUID id:recipients){var player=actor.server.getPlayerList().getPlayer(id);if(player!=null&&player.level()==actor.level()&&player.connection.hasChannel(Network.Visual.TYPE))PacketDistributor.sendToPlayer(player,packet);}
    }
    @SubscribeEvent public static void tracking(PlayerEvent.StartTracking event) {
        if(!(event.getEntity() instanceof ServerPlayer observer)||!(event.getTarget() instanceof ServerPlayer actor)||!observer.connection.hasChannel(Network.Visual.TYPE))return;
        OBSERVERS.computeIfAbsent(actor.getUUID(),id->new HashSet<>()).add(observer.getUUID());var cast=CASTS.get(actor.getUUID());
        if(cast!=null){long age=now(actor)-cast.start;if(age>=0&&age<cast.move.startup()+cast.move.active()+cast.move.recovery())PacketDistributor.sendToPlayer(observer,new Network.Visual(actor.getId(),cast.move.id(),cast.identity,3,(int)age));}
    }
    @SubscribeEvent public static void untracking(PlayerEvent.StopTracking event){if(event.getTarget() instanceof ServerPlayer actor){var set=OBSERVERS.get(actor.getUUID());if(set!=null)set.remove(event.getEntity().getUUID());}}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){CASTS.clear();SESSIONS.clear();OBSERVERS.clear();UniqueQuirks.clearAll();Campus.clearRuntime();Quests.clearRuntime();Missions.clearRuntime();}
    public static HeroLedger ledger(ServerPlayer p){return HeroLedger.get(p.server);}
    public static HeroProfile profile(ServerPlayer p){return ledger(p).profile(p.getUUID());}
    public static long now(ServerPlayer p){return p.server.overworld().getGameTime();}
    public static List<String> pool(){return MhaConfig.STARTING_POOL.get().stream().filter(Catalog.PLAYABLE::contains).map(String::toString).toList();}
    public static Move effective(Move m) {return new Move(m.id(),m.name(),m.slot(),m.shape(),m.startup(),m.active(),m.recovery(),m.stamina(),m.strain(),m.cooldown(),m.range(),(float)(m.damage()*MhaConfig.DAMAGE_SCALE.get()),m.mastery(),m.trial(),m.description());}
    public static void sync(ServerPlayer p,String open) {
        // GameTest/FakePlayer connections have no negotiated mod channels. Never emit an illegal packet.
        if(!p.connection.hasChannel(Network.Snapshot.TYPE))return;
        var catalogue=catalogue();var session=SESSIONS.computeIfAbsent(p.getUUID(),id->new Session());
        if(!session.catalogue.equals(catalogue.revision())){if(!p.connection.hasChannel(Network.MoveCatalogue.TYPE))return;PacketDistributor.sendToPlayer(p,catalogue);session.catalogue=catalogue.revision();}
        var view=new ClientView(profile(p),now(p),open,Quests.objective(profile(p)),pool(),catalogue.revision());
        PacketDistributor.sendToPlayer(p,new Network.Snapshot(Catalog.JSON.toJson(view)));
    }
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e) {
        if(!(e.getEntity() instanceof ServerPlayer p))return;
        SESSIONS.put(p.getUUID(),new Session());
        try {
            boolean fresh=profile(p).quirk.isEmpty();
            if(fresh)ledger(p).commit(p.getUUID(),data->HeroRules.firstAssignment(data,pool(),new Random(p.getRandom().nextLong())));
            if(fresh){p.getInventory().add(ModItems.NOTEBOOK.toStack());p.displayClientMessage(Component.literal("Your quirk is "+Kits.get(profile(p).quirk).name()+". Open the Hero Analysis Notebook to begin."),false);}
            sync(p,profile(p).revealSeen?"":"reveal");
        }catch(RuntimeException error){RisingHeroes.LOG.error("Player onboarding failed safely",error);error(p,error.getMessage());}
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e) {
        if(e.getEntity() instanceof ServerPlayer p){QuirkRuntime.endContext(p);ledger(p).flush();SESSIONS.remove(p.getUUID());OBSERVERS.remove(p.getUUID());OBSERVERS.values().forEach(set->set.remove(p.getUUID()));}
    }
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){if(e.getEntity() instanceof ServerPlayer p){QuirkRuntime.endContext(p);sync(p,"");}}
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p){QuirkRuntime.endContext(p);sync(p,"");}}
    @SubscribeEvent public static void death(net.neoforged.neoforge.event.entity.living.LivingDeathEvent e){if(e.getEntity() instanceof ServerPlayer p)QuirkRuntime.endContext(p);}
    @SubscribeEvent public static void incoming(LivingIncomingDamageEvent e) {
        if(e.getEntity() instanceof ServerPlayer p&&e.getAmount()>0) {
            Missions.damaged(p);
            boolean guarded=QuirkRuntime.incoming(p,e);ledger(p).commit(p.getUUID(),s->s.combatUntil=now(p)+120);if(!guarded)cancel(p);
        }
    }
    public static void cancel(ServerPlayer p) {
        var cast=CASTS.remove(p.getUUID());
        if(cast!=null)visual(p,new Network.Visual(p.getId(),cast.move.id(),cast.identity,2));
    }
    public static void action(ServerPlayer p,Network.Action action) {
        var s=SESSIONS.computeIfAbsent(p.getUUID(),k->new Session());long now=now(p);
        if(action.sequence()<=s.sequence||now-s.lastAction<2)return;
        s.sequence=action.sequence();s.lastAction=now;
        try {
            if(!p.isAlive()||p.isSpectator())throw new IllegalArgumentException("This action requires a living player");
            switch(action.operation()) {
                case CAST -> cast(p,action.slot());
                case EQUIP -> {if(CASTS.containsKey(p.getUUID()))throw new IllegalArgumentException("Cannot equip during a cast");ledger(p).commit(p.getUUID(),v->HeroRules.equip(v,action.slot(),action.argument(),now));}
                case REROLL -> {
                    int balance=profile(p).liveTokens.size();
                    ledger(p).commit(p.getUUID(),v->HeroRules.beginReroll(v,pool(),now,CASTS.containsKey(p.getUUID())||!v.activeTrial.isEmpty()||MissionRules.stableLoadout(v),new Random(p.getRandom().nextLong())));
                    if(profile(p).liveTokens.size()<balance)for(var stack:p.getInventory().items){if(stack.is(ModItems.REROLL.get())){stack.shrink(1);break;}}
                }
                case CHOOSE -> {if(CASTS.containsKey(p.getUUID()))throw new IllegalArgumentException("Cannot select during a cast");String before=profile(p).quirk;ledger(p).commit(p.getUUID(),v->HeroRules.chooseReroll(v,action.offer(),action.argument(),now));cancel(p);if(!before.equals(profile(p).quirk))QuirkRuntime.changed(p);}
                case ACK_REVEAL -> ledger(p).commit(p.getUUID(),v->v.revealSeen=true);
                case TRAVEL -> Campus.travel(p);
                case RETURN -> Campus.returnFrom(p);
                case BOOKMARK -> ledger(p).commit(p.getUUID(),v->{if(!Catalog.PLAYABLE.contains(action.argument()))throw new IllegalArgumentException("Unknown entry");if(!v.bookmarks.add(action.argument()))v.bookmarks.remove(action.argument());});
                case EXCHANGE -> {if(!Campus.atRegistrar(p))throw new IllegalArgumentException("Visit the academy registrar to exchange credits");if(pool().stream().noneMatch(q->!q.equals(profile(p).quirk)))throw new IllegalArgumentException("No alternative quirk is enabled. Your credits were not spent");ledger(p).commit(p.getUUID(),v->{if(v.credits<250)throw new IllegalArgumentException("A reroll token costs 250 mission credits");v.credits-=250;HeroRules.grantToken(v);});p.getInventory().add(ModItems.boundReroll(p.getUUID()));}
                case TRIAL_START -> TechniqueTrials.start(p,action.argument());
                case TRIAL_CANCEL -> TechniqueTrials.cancel(p);
                case PRESET_SAVE -> ledger(p).commit(p.getUUID(),v->HeroRules.savePreset(v,action.argument(),now));
                case PRESET_LOAD -> ledger(p).commit(p.getUUID(),v->HeroRules.loadPreset(v,action.argument(),now));
                case MISSION_START -> Missions.start(p,action.argument());
                case MISSION_LEAVE -> Missions.leave(p);
                case MISSION_DECISION -> Missions.decision(p,action.argument());
                default -> throw new IllegalArgumentException("Unknown MHA request");
            }
            sync(p,"");
        }catch(IllegalArgumentException ex){error(p,ex.getMessage());sync(p,"");}
        catch(RuntimeException ex){RisingHeroes.LOG.error("MHA action failed before outcome",ex);error(p,"Could not save this action. No result was awarded; check server logs.");}
    }
    public static void error(ServerPlayer p,String message){p.displayClientMessage(Component.literal(message==null?"Action unavailable":message).withStyle(net.minecraft.ChatFormatting.GOLD),true);}
    public static void cast(ServerPlayer p,int slot) {
        var holder=new Move[1];long time=now(p);
        if(slot<0||slot>=6)throw new IllegalArgumentException("Invalid slot");String id=profile(p).slots.get(slot);if(!id.isBlank())UniqueQuirks.preflight(p,Catalog.move(id));
        ledger(p).commit(p.getUUID(),data->holder[0]=HeroRules.reserveCast(data,slot,time,CASTS.containsKey(p.getUUID()),!p.onGround(),p.isUnderWater()));
        Move move=effective(holder[0]);long identity=p.getRandom().nextLong();
        CASTS.put(p.getUUID(),new Cast(move,time,identity,p.level().dimension()));
        visual(p,new Network.Visual(p.getId(),move.id(),identity,0));
    }
    public static void pulse(ServerPlayer p,Move move){if(DamageResolver.hit(p,move)>0)ledger(p).commit(p.getUUID(),v->v.combatUntil=now(p)+100);}
    @SubscribeEvent public static void tick(ServerTickEvent.Post event) {
        long time=event.getServer().overworld().getGameTime();
        Campus.tick(event.getServer());
        Missions.tick(event.getServer());
        TemporaryConstructs.tick(event.getServer());
        for(var p:event.getServer().getPlayerList().getPlayers()) {
            QuirkRuntime.tick(p);
            var data=profile(p);var cast=CASTS.get(p.getUUID());
            if(cast!=null) {
                if(!p.isAlive()||p.isSpectator()||p.level().dimension()!=cast.dimension||p.isPassenger()||p.isSleeping()){cancel(p);continue;}
                long age=time-cast.start;var m=cast.move;
                if(age==m.startup()||m.shape().equals("VOLLEY")&&(age==m.startup()+4||age==m.startup()+8))activate(p,cast);
                QuirkRuntime.sustain(p,m,age);
                if(age>=m.startup()+m.active()+m.recovery())cancel(p);
            }else if(time>=data.exertionUntil&&p.isAlive()) {
                data.stamina=Math.min(100,data.stamina+.32);QuirkResources.recover(data);
            }
            if(time%5==0)Quests.tick(p);
            if(time%10==0)sync(p,"");
        }
        if(time%100==0)HeroLedger.get(event.getServer()).flush();
    }
    private static void activate(ServerPlayer p,Cast cast) {
        var m=cast.move;
        if(QuirkRuntime.activate(p,m)) {
            // Dedicated mechanics have already applied their authoritative outcome.
        }else if(Set.of("DASH","DODGE","AIR").contains(m.shape())) {
            Vec3 look=p.getLookAngle();double sign=m.shape().equals("DODGE")?-1:1;
            Vec3 motion=new Vec3(look.x*sign*1.1,Math.max(-.25,Math.min(.5,look.y*sign*.65+.12)),look.z*sign*1.1);
            p.setDeltaMovement(motion);p.hurtMarked=true;p.connection.send(new ClientboundSetEntityMotionPacket(p));
            TechniqueTrials.movement(p,m);
        }else {
            if(DamageResolver.hit(p,QuirkRuntime.attack(p,m))>0)ledger(p).commit(p.getUUID(),v->v.combatUntil=now(p)+100);
        }
        visual(p,new Network.Visual(p.getId(),m.id(),cast.identity,1));
        var sound=Set.of("DASH","DODGE","AIR").contains(m.shape())?ModSounds.RECOIL.get():m.slot().equals("CAPSTONE")?ModSounds.HEAVY.get():ModSounds.BURST.get();
        p.serverLevel().playSound(null,p.blockPosition(),sound,net.minecraft.sounds.SoundSource.PLAYERS,.65f,m.shape().equals("VOLLEY")?1.15f:1);
    }
}
