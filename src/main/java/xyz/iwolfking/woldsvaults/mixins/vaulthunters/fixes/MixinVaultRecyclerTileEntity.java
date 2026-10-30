package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.block.entity.VaultRecyclerTileEntity;
import iskallia.vault.core.card.CardDeck;
import iskallia.vault.item.CardDeckItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = VaultRecyclerTileEntity.class, remap = false, priority = 500)
public class MixinVaultRecyclerTileEntity {
    @Inject(method = "isValidInput", at = @At("HEAD"), cancellable = true)
    private void preventRecyclingDeck(ItemStack input, CallbackInfoReturnable<Boolean> cir) {
        if(input.getItem() instanceof CardDeckItem) {
            CardDeck deck = CardDeckItem.getCardDeck(input).orElse(null);
            if(deck != null && !deck.getSnapshotAttributes().isEmpty()) {
                cir.setReturnValue(false);
            }
        }
    }
}
