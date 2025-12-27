package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.modification.VaultSealerCost;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = VaultSealerCost.class, remap = false)
public class MixinVorpalSealCost {
    @Inject(method = "getVorpalCost", at = @At("HEAD"), cancellable = true)
    private static void handleMythicRarity(VaultGearRarity rarity, CallbackInfoReturnable<Integer> cir) {
        if(rarity.equals(VaultGearRarity.valueOf("MYTHIC"))) {
            cir.setReturnValue(20);
        }
    }
}
