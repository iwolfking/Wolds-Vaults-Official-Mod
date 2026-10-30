package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.core.card.CardDeck;
import iskallia.vault.item.CardDeckItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.sophisticatedvaultupgrades.upgrades.recycler.RecyclerUpgradeWrapper;

@Mixin(value = RecyclerUpgradeWrapper.class, remap = false)
public class MixinRecyclerUpgradeHelper {
    @Inject(method = "stackMatchesFilter", at = @At("HEAD"), cancellable = true)
    private void preventRecyclingDeck(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if(stack.getItem() instanceof CardDeckItem) {
            CardDeck deck = CardDeckItem.getCardDeck(stack).orElse(null);
            if(deck != null && !deck.getSnapshotAttributes().isEmpty()) {
                cir.setReturnValue(false);
            }
        }
    }
}
