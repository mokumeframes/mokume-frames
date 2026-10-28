package dev.risingheroes.core;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.saveddata.SavedData;

/** SavedData mirror plus synchronously committed journal for critical profile transactions.
 * The journal owns token balances/rewards, so item/player.dat saves cannot duplicate their value. */
public final class HeroLedger extends SavedData {
    private static final class Document {int version=1;Map<UUID,HeroProfile> players=new HashMap<>();Map<String,Integer> arenas=new HashMap<>();Map<String,ConstructPlan> constructs=new LinkedHashMap<>();}
    private Document document=new Document();
    private Path journal;
    public static HeroLedger openJournal(Path path){var ledger=new HeroLedger();ledger.attach(path);return ledger;}
    public static HeroLedger get(MinecraftServer server) {
        var ledger=server.overworld().getDataStorage().computeIfAbsent(new Factory<>(HeroLedger::new,HeroLedger::load),"mha_heroes");
        if(ledger.journal==null)ledger.attach(server.getWorldPath(LevelResource.ROOT).resolve("data/mha-transactions.json"));
        return ledger;
    }
    private static HeroLedger load(CompoundTag nbt,HolderLookup.Provider registries) {
        HeroLedger ledger=new HeroLedger();
        if(nbt.contains("profiles"))ledger.document=decode(nbt.getString("profiles"));
        return ledger;
    }
    private static Document decode(String json) {
        var d=Catalog.JSON.fromJson(json,Document.class);
        if(d==null||d.version!=1||d.players==null)throw new IllegalArgumentException("Unknown/corrupt MHA save; restore or migrate, do not reset automatically");
        if(d.arenas==null||d.arenas.size()>4096||new HashSet<>(d.arenas.values()).size()!=d.arenas.size()||d.arenas.values().stream().anyMatch(v->v==null||v<0||v>=4096))throw new IllegalArgumentException("Invalid reserved encounter slots");
        if(d.constructs==null||d.constructs.size()>2048)throw new IllegalArgumentException("Invalid construct journal");
        d.constructs.forEach((key,plan)->{plan.validate();if(!key.equals(plan.key()))throw new IllegalArgumentException("Construct lease identity mismatch");});
        d.players.values().forEach(p->{p.migrate();p.validate();});return d;
    }
    private void attach(Path path) {
        journal=path;
        if(Files.exists(path))try {document=decode(Files.readString(path));}catch(IOException e){throw new UncheckedIOException("Cannot read MHA journal; refusing a free reset",e);}
    }
    public HeroProfile profile(UUID id){return document.players.computeIfAbsent(id,k->new HeroProfile());}
    public List<ConstructPlan> constructs(){return List.copyOf(document.constructs.values());}
    public void planConstructs(List<ConstructPlan> plans) {
        if(plans.isEmpty()||plans.size()>32)throw new IllegalArgumentException("Invalid construct batch");
        var next=new LinkedHashMap<>(document.constructs);UUID owner=plans.getFirst().owner();
        if(next.values().stream().filter(p->p.owner().equals(owner)).count()+plans.size()>128||next.size()+plans.size()>2048)throw new IllegalArgumentException("Temporary construct capacity reached; wait for existing ice to expire");
        for(var plan:plans){plan.validate();if(!plan.owner().equals(owner)||next.putIfAbsent(plan.key(),plan)!=null)throw new IllegalArgumentException("A construct already occupies that position");}
        var old=document.constructs;document.constructs=next;try{flush();setDirty();}catch(RuntimeException error){document.constructs=old;throw error;}
    }
    public void forgetConstructs(Collection<String> keys) {
        var old=document.constructs;var next=new LinkedHashMap<>(old);keys.forEach(next::remove);document.constructs=next;
        try{flush();setDirty();}catch(RuntimeException error){document.constructs=old;throw error;}
    }
    public void expireConstructs(UUID owner,long now) {
        var old=document.constructs;var next=new LinkedHashMap<>(old);boolean changed=false;
        for(var plan:old.values())if(plan.owner().equals(owner)&&plan.expiry()>now){next.put(plan.key(),new ConstructPlan(plan.dimension(),plan.x(),plan.y(),plan.z(),plan.expected(),now,owner));changed=true;}
        if(!changed)return;document.constructs=next;try{flush();setDirty();}catch(RuntimeException error){document.constructs=old;throw error;}
    }
    public int reserveArena(UUID owner,String mission) {
        MissionCatalog.get(mission);String key=owner+":"+mission;Integer existing=document.arenas.get(key);if(existing!=null)return existing;
        int slot=0;while(document.arenas.containsValue(slot)&&slot<4096)slot++;
        if(slot>=4096)throw new IllegalArgumentException("Encounter capacity reached; ask an administrator to archive old sessions");
        document.arenas.put(key,slot);try{flush();setDirty();return slot;}catch(RuntimeException error){document.arenas.remove(key);throw error;}
    }
    public HeroProfile commit(UUID id,Consumer<HeroProfile> change) {
        HeroProfile next=profile(id).copy();change.accept(next);next.validate();
        var old=document.players.put(id,next);
        try {flush();setDirty();return next;}
        catch(RuntimeException e){if(old==null)document.players.remove(id);else document.players.put(id,old);throw e;}
    }
    public void flush() {
        if(journal==null)throw new IllegalStateException("Journal not attached");
        try {
            Files.createDirectories(journal.getParent());
            Path temp=journal.resolveSibling(journal.getFileName()+".tmp");
            byte[] data=Catalog.JSON.toJson(document).getBytes(StandardCharsets.UTF_8);
            try(var channel=FileChannel.open(temp,StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING,StandardOpenOption.WRITE)) {
                ByteBuffer buffer=ByteBuffer.wrap(data);while(buffer.hasRemaining())channel.write(buffer);channel.force(true);
            }
            // Do not silently fall back to a non-atomic replace. The old save remains authoritative on failure.
            replaceAtomically(temp,journal);
        }catch(IOException e){throw new UncheckedIOException("MHA transaction failed; no outcome should be awarded",e);}
    }
    private static void replaceAtomically(Path temp,Path journal) throws IOException {
        // Windows scanners/readers can briefly deny a rename. Keep atomicity and a strict retry bound.
        for(int attempt=0;;attempt++)try {Files.move(temp,journal,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);return;}
        catch(AccessDeniedException locked){if(attempt>=4)throw locked;java.util.concurrent.locks.LockSupport.parkNanos((attempt+1)*5_000_000L);}
    }
    @Override public CompoundTag save(CompoundTag nbt,HolderLookup.Provider registries) {
        nbt.putInt("version",1);nbt.putString("profiles",Catalog.JSON.toJson(document));return nbt;
    }
}
