package xyz.iwolfking.woldsvaults.mixins.client;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.spellcraft.item.SpellcraftItemData;

@Mixin(MultiPlayerGameMode.class)
public abstract class MixinMultiPlayerGameModeSpellCharge {

    /**
     * Keeps vanilla from releasing a spell Charge when the use key is up, since the charge is held by a spell slot key
     * and the server ends it when that key is released.
     */
    @Inject(method = "releaseUsingItem", at = @At("HEAD"), cancellable = true)
    private void woldsvaults$keepSpellCharge(Player player, CallbackInfo ci) {
        if (player.isUsingItem() && SpellcraftItemData.canCarry(player.getUseItem())) {
            ci.cancel();
        }
    }
}
