package dev.risingheroes.server;

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.CommandContext;
import dev.risingheroes.*;
import dev.risingheroes.core.*;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.commands.*;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid="mha")
public final class HeroCommands {
    private record Reset(UUID target,String nonce,long deadline){}
    private static final Map<UUID,Reset> RESETS=new HashMap<>();
    @SubscribeEvent public static void register(RegisterCommandsEvent e) {
        var root=Commands.literal("mha").executes(c->{Gameplay.sync(c.getSource().getPlayerOrException(),"notebook");return 1;});
        root.then(Commands.literal("notebook").executes(c->{var p=c.getSource().getPlayerOrException();p.getInventory().add(ModItems.NOTEBOOK.toStack());Gameplay.sync(p,"notebook");return 1;}));
        root.then(Commands.literal("missions").executes(c->{Gameplay.sync(c.getSource().getPlayerOrException(),"missions");return 1;}));
        root.then(Commands.literal("licence").executes(c->{var p=c.getSource().getPlayerOrException();if(Gameplay.profile(p).licence<1){Gameplay.error(p,"Complete the provisional examination to earn a licence. Items do not grant privileges.");return 0;}p.getInventory().add(ModItems.licence(p));Gameplay.sync(p,"missions");return 1;}));
        root.then(Commands.literal("travel").then(Commands.literal("academy").executes(c->safe(c,p->Campus.travel(p)))).then(Commands.literal("return").executes(c->safe(c,p->Campus.returnFrom(p)))));
        root.then(Commands.literal("inspect").requires(s->s.hasPermission(2)).then(Commands.argument("player",EntityArgument.player()).executes(c->{var p=EntityArgument.getPlayer(c,"player");c.getSource().sendSuccess(()->Component.literal(Catalog.JSON.toJson(Gameplay.profile(p))),false);return 1;})));
        root.then(Commands.literal("quirk").requires(source->source.hasPermission(2)).then(Commands.literal("assign").then(Commands.argument("player",EntityArgument.player()).then(Commands.argument("quirk",StringArgumentType.word()).suggests((c,b)->SharedSuggestionProvider.suggest(Kits.ids(),b)).executes(c->edit(c,v->{String id=StringArgumentType.getString(c,"quirk");Kits.get(id);HeroRules.assign(v,id);}))))));
        root.then(Commands.literal("reroll").requires(s->s.hasPermission(2)).then(Commands.literal("grant").then(Commands.argument("player",EntityArgument.player()).executes(c->{int result=edit(c,HeroRules::grantToken);if(result==1){var p=EntityArgument.getPlayer(c,"player");p.getInventory().add(ModItems.boundReroll(p.getUUID()));}return result;}))));
        root.then(Commands.literal("mastery").requires(s->s.hasPermission(2)).then(Commands.literal("set").then(Commands.argument("player",EntityArgument.player()).then(Commands.argument("value",IntegerArgumentType.integer(0,100)).executes(c->edit(c,v->{v.masteries.put(v.quirk,IntegerArgumentType.getInteger(c,"value"));HeroRules.unlockEarned(v);}))))));
        for(String verb:List.of("grant","revoke"))root.then(Commands.literal("move").requires(s->s.hasPermission(2)).then(Commands.literal(verb).then(Commands.argument("player",EntityArgument.player()).then(Commands.argument("move",StringArgumentType.word()).suggests((c,b)->SharedSuggestionProvider.suggest(Catalog.moves().keySet(),b)).executes(c->edit(c,v->{String id=StringArgumentType.getString(c,"move");Catalog.move(id);if(verb.equals("grant"))v.currentUnlocks().add(id);else {v.currentUnlocks().remove(id);Collections.replaceAll(v.slots,id,"");}}))))));
        root.then(Commands.literal("reputation").requires(s->s.hasPermission(2)).then(Commands.argument("player",EntityArgument.player()).then(Commands.argument("faction",StringArgumentType.word()).suggests((c,b)->SharedSuggestionProvider.suggest(List.of("hero","villain","independent"),b)).then(Commands.argument("value",IntegerArgumentType.integer(-100,100)).executes(c->edit(c,v->{String faction=StringArgumentType.getString(c,"faction");if(!v.reputation.containsKey(faction))throw new IllegalArgumentException("Unknown faction");v.reputation.put(faction,IntegerArgumentType.getInteger(c,"value"));}))))));
        root.then(Commands.literal("pools").requires(source->source.hasPermission(2)).then(Commands.literal("list").executes(c->{c.getSource().sendSuccess(()->Component.literal("Enabled experimental kits: "+Gameplay.pool()+". Available development IDs: "+Kits.ids()),false);return 1;})).then(Commands.argument("quirk",StringArgumentType.word()).suggests((c,b)->SharedSuggestionProvider.suggest(Kits.ids(),b)).then(Commands.argument("enabled",BoolArgumentType.bool()).executes(c->{String id=StringArgumentType.getString(c,"quirk");Kits.get(id);var pool=new ArrayList<String>(Gameplay.pool());pool.removeIf(id::equals);if(BoolArgumentType.getBool(c,"enabled"))pool.add(id);MhaConfig.STARTING_POOL.set(pool);MhaConfig.SERVER.save();return 1;}))));
        root.then(Commands.literal("test").requires(s->s.hasPermission(2))
            .then(Commands.literal("all_moves").then(Commands.argument("player",EntityArgument.player()).executes(c->edit(c,v->{HeroRules.testingLoadout(v);}))))
            .then(Commands.literal("resources").then(Commands.argument("player",EntityArgument.player()).executes(c->edit(c,v->{v.stamina=100;v.strain=0;v.resources.clear();v.overloadUntil=0;v.cooldowns.clear();v.exertionUntil=0;}))))
            .then(Commands.literal("licence_bypass").then(Commands.argument("player",EntityArgument.player()).executes(c->edit(c,v->v.licence=1)))));
        root.then(Commands.literal("reset").requires(s->s.hasPermission(2)).then(Commands.argument("player",EntityArgument.player())
            .then(Commands.literal("preview").executes(c->{var admin=c.getSource().getPlayerOrException();var target=EntityArgument.getPlayer(c,"player");String nonce=UUID.randomUUID().toString().substring(0,8);RESETS.put(admin.getUUID(),new Reset(target.getUUID(),nonce,Gameplay.now(admin)+1200));c.getSource().sendSuccess(()->Component.literal("DESTRUCTIVE RESET: quirk, all mastery, unlocks, credits, school progress, reputation, loadout and unspent tokens for "+target.getScoreboardName()+". Inventory/builds are retained. Confirm within 60s: /mha reset "+target.getScoreboardName()+" confirm "+nonce),false);return 1;}))
            .then(Commands.literal("confirm").then(Commands.argument("nonce",StringArgumentType.word()).executes(c->{var admin=c.getSource().getPlayerOrException();var target=EntityArgument.getPlayer(c,"player");var reset=RESETS.get(admin.getUUID());if(reset==null||!reset.target.equals(target.getUUID())||!reset.nonce.equals(StringArgumentType.getString(c,"nonce"))||Gameplay.now(admin)>reset.deadline)throw new IllegalArgumentException("No matching, unexpired reset preview");Gameplay.cancel(target);Gameplay.ledger(target).commit(target.getUUID(),v->{v.quirk="";v.masteries.clear();v.unlocks.clear();v.rewards.clear();v.trials.clear();v.offers.clear();v.offerIdentity="";v.liveTokens.clear();v.credits=0;v.rank=1;v.schoolStage=0;v.courseCheckpoint=0;v.licence=0;v.tutorial=false;v.revealSeen=false;v.reputation.replaceAll((k,x)->0);v.mission=new MissionRun();v.completedMissions.clear();v.missionCheckpoints.clear();v.missionGrades.clear();v.loadoutPresets.clear();v.resources.clear();v.overloadUntil=0;v.buffUntil=0;v.buffKind="";v.counterUntil=0;HeroRules.firstAssignment(v,Gameplay.pool(),new Random());});RESETS.remove(admin.getUUID());Gameplay.sync(target,"reveal");return 1;})))));
        e.getDispatcher().register(root);
    }
    private static int safe(CommandContext<CommandSourceStack> c,Consumer<ServerPlayer> action) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var p=c.getSource().getPlayerOrException();try{action.accept(p);return 1;}catch(IllegalArgumentException ex){Gameplay.error(p,ex.getMessage());return 0;}
    }
    private static int edit(CommandContext<CommandSourceStack> c,Consumer<HeroProfile> action) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var p=EntityArgument.getPlayer(c,"player");try{String before=Gameplay.profile(p).quirk;Gameplay.cancel(p);Gameplay.ledger(p).commit(p.getUUID(),action);if(!before.equals(Gameplay.profile(p).quirk))QuirkRuntime.changed(p);Gameplay.sync(p,"");c.getSource().sendSuccess(()->Component.literal("Updated "+p.getScoreboardName()+" through authoritative MHA state."),true);return 1;}catch(IllegalArgumentException ex){c.getSource().sendFailure(Component.literal(ex.getMessage()));return 0;}
    }
}
