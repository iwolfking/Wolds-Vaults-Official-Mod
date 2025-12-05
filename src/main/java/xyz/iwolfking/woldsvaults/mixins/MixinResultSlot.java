package xyz.iwolfking.woldsvaults.mixins;

import iskallia.vault.block.SkillAltarBlock;
import iskallia.vault.item.KnowledgeBrewItem;
import iskallia.vault.item.MentorsBrewItem;
import iskallia.vault.item.VaultDollItem;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import xyz.iwolfking.woldsvaults.api.util.GameruleHelper;
import xyz.iwolfking.woldsvaults.blocks.DollDismantlingBlock;
import xyz.iwolfking.woldsvaults.init.ModGameRules;

@Mixin(value = ResultSlot.class, remap = false)
public abstract class MixinResultSlot extends Slot {

    public MixinResultSlot(Container pContainer, int pSlot, int pX, int pY) {
        super(pContainer, pSlot, pX, pY);
    }


    //TODO: Refactor this into something more dynamic
    @Override
    public boolean mayPickup(Player pPlayer) {
        ItemStack pStack = this.getItem();
        if(pStack.getItem() instanceof VaultDollItem && !GameruleHelper.isEnabled(ModGameRules.ENABLE_VAULT_DOLLS, pPlayer.getLevel())) {
            return false;
        }
        else if(pStack.getItem() instanceof KnowledgeBrewItem && !GameruleHelper.isEnabled(iskallia.vault.init.ModGameRules.ALLOW_KNOWLEDGE_BREW, pPlayer.getLevel())) {
            return false;
        }
        else if(pStack.getItem() instanceof MentorsBrewItem && !GameruleHelper.isEnabled(iskallia.vault.init.ModGameRules.ALLOW_MENTOR_BREW, pPlayer.getLevel())) {
            return false;
        }
        else if(pStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof DollDismantlingBlock && !GameruleHelper.isEnabled(ModGameRules.ENABLE_VAULT_DOLLS, pPlayer.getLevel())) {
            return false;
        }
        else if(pStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof SkillAltarBlock && !GameruleHelper.isEnabled(ModGameRules.ENABLE_SKILL_ALTARS, pPlayer.getLevel())) {
            return false;
        }

        return true;
    }
}
