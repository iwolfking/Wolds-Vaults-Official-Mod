package xyz.iwolfking.woldsvaults.mixins.client;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftRoller;

@Mixin(MultiPlayerGameMode.class)
public abstract class MixinMultiPlayerGameModeSpellCharge {

    /**
     * A spell Charge is the server using the spell's focus or wand in its hand, held by a spell slot key rather than the
     * use key.
     * Vanilla releases any item use the moment the use key is up, which ended the charge one tick after it began; the
     * server ends it when the slot key is released instead.
     */
    @Inject(method = "releaseUsingItem", at = @At("HEAD"), cancellable = true)
    private void woldsvaults$keepSpellCharge(Player player, CallbackInfo ci) {
        if (player.isUsingItem() && SpellcraftRoller.canCarry(player.getUseItem())) {
            ci.cancel();
        }
    }
}
