package xyz.iwolfking.woldsvaults.mixins;

import iskallia.vault.block.SkillAltarBlock;
import iskallia.vault.item.KnowledgeBrewItem;
import iskallia.vault.item.MentorsBrewItem;
import iskallia.vault.item.VaultDollItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.api.util.GameruleHelper;
import xyz.iwolfking.woldsvaults.blocks.DollDismantlingBlock;
import xyz.iwolfking.woldsvaults.init.ModGameRules;

@Mixin(value = Slot.class)
public abstract class MixinResultSlot{


    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    public void mayPickup(Player player, CallbackInfoReturnable<Boolean> cir) {
        Slot thisSlot = (Slot)(Object)this;

        ItemStack pStack = this.getItem();
        if(pStack.getItem() instanceof VaultDollItem && !GameruleHelper.isEnabled(ModGameRules.ENABLE_VAULT_DOLLS, player.getLevel())) {
            cir.setReturnValue(false);
        }
        else if(pStack.getItem() instanceof KnowledgeBrewItem && !GameruleHelper.isEnabled(iskallia.vault.init.ModGameRules.ALLOW_KNOWLEDGE_BREW, player.getLevel())) {
            cir.setReturnValue(false);
        }
        else if(pStack.getItem() instanceof MentorsBrewItem && !GameruleHelper.isEnabled(iskallia.vault.init.ModGameRules.ALLOW_MENTOR_BREW, player.getLevel())) {
            cir.setReturnValue(false);
        }
        else if(pStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof DollDismantlingBlock && !GameruleHelper.isEnabled(ModGameRules.ENABLE_VAULT_DOLLS, player.getLevel())) {
            cir.setReturnValue(false);
        }
        else if(pStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof SkillAltarBlock && !GameruleHelper.isEnabled(ModGameRules.ENABLE_SKILL_ALTARS, player.getLevel())) {
            cir.setReturnValue(false);
        }
    }

    @Shadow
    public abstract ItemStack getItem();
}
