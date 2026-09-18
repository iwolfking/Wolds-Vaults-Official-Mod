package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.modifier.TickingModifierAdderModifier;
import iskallia.vault.core.vault.time.TickClock;
import net.minecraftforge.event.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TickingModifierAdderModifier.class, remap = false)
public class MixinTickingModifierAdderModifier {
    @Inject(method = "lambda$initServer$1", at = @At(value = "INVOKE", target = "Liskallia/vault/core/vault/time/TickClock;get(Liskallia/vault/core/data/key/GenericFieldKey;)Ljava/lang/Object;"), cancellable = true)
    private void dontTickWhenVaultIsPaused(Vault vault, TickEvent.ServerTickEvent data, CallbackInfo ci) {
        if(vault.get(Vault.CLOCK).has(TickClock.PAUSED)) {
            ci.cancel();
        }
    }
}
