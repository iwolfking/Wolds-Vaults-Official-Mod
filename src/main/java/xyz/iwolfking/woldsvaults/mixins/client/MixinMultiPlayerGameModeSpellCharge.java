package xyz.iwolfking.woldsvaults.mixins.client;

import iskallia.vault.item.gear.FocusItem;
import iskallia.vault.item.gear.WandItem;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public abstract class MixinMultiPlayerGameModeSpellCharge {

    /**
     * A spell Charge is the server using the off-hand focus or wand, held by a spell slot key rather than the use key.
     * Vanilla releases any item use the moment the use key is up, which ended the charge one tick after it began; the
     * server ends it when the slot key is released instead.
     */
    @Inject(method = "releaseUsingItem", at = @At("HEAD"), cancellable = true)
    private void woldsvaults$keepSpellCharge(Player player, CallbackInfo ci) {
        if (player.isUsingItem() && player.getUsedItemHand() == InteractionHand.OFF_HAND) {
            Item item = player.getUseItem().getItem();
            if (item instanceof FocusItem || item instanceof WandItem) {
                ci.cancel();
            }
        }
    }
}
