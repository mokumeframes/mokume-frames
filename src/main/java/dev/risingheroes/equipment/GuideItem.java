package dev.risingheroes.equipment;

import dev.risingheroes.core.HeroRules;
import dev.risingheroes.server.Gameplay;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import java.util.List;

public final class GuideItem extends Item {
    public enum Kind {NOTEBOOK,CREATIVE_ASSIGNMENT,REROLL,LICENCE}
    private final Kind kind;
    private final String quirk;
    public GuideItem(Kind kind){this(kind,"explosion");}
    public GuideItem(Kind kind,String quirk){super(new Properties().stacksTo(1));this.kind=kind;this.quirk=quirk;}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand) {
        if(player instanceof ServerPlayer p) {
            if(kind==Kind.REROLL){
                var custom=player.getItemInHand(hand).get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
                if(custom!=null){var tag=custom.copyTag();if(tag.hasUUID("mha_owner")&&!tag.getUUID("mha_owner").equals(p.getUUID())){Gameplay.error(p,"This reroll credential is bound to another player");return InteractionResultHolder.fail(player.getItemInHand(hand));}}
            }
            if(kind==Kind.CREATIVE_ASSIGNMENT) {
                if(!HeroRules.mayUseCreativeToken(p.isCreative(),p.hasPermissions(2)?2:0)){Gameplay.error(p,"Creative mode or operator permission is required for this token");return InteractionResultHolder.fail(player.getItemInHand(hand));}
                if(dev.risingheroes.core.MissionRules.stableLoadout(Gameplay.profile(p))){Gameplay.error(p,"Leave the active assignment before changing quirk");return InteractionResultHolder.fail(player.getItemInHand(hand));}
                dev.risingheroes.core.Kits.get(quirk);Gameplay.cancel(p);Gameplay.ledger(p).commit(p.getUUID(),v->HeroRules.assign(v,quirk));dev.risingheroes.server.QuirkRuntime.changed(p);Gameplay.sync(p,"reveal");
            }else Gameplay.sync(p,kind==Kind.LICENCE?"missions":kind==Kind.NOTEBOOK?"notebook":"reroll");
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand),level.isClientSide());
    }
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> lines,TooltipFlag flag) {
        lines.add(Component.translatable("item.mha."+(kind==Kind.NOTEBOOK?"hero_notebook":kind==Kind.REROLL?"reroll_token":kind==Kind.LICENCE?"licence_credential":quirk+"_token")+".tooltip"));
        if(kind==Kind.LICENCE){var data=stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);if(data!=null){var tag=data.copyTag();lines.add(Component.literal("Issued to "+tag.getString("mha_name")+" / level "+tag.getInt("mha_licence")));}}
    }
}
