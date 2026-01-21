package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.world.generator.layout.ClassicInfiniteLayout;
import iskallia.vault.core.world.generator.layout.VaultGridLayout;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.objectives.HauntedBraziersObjective;

@Mixin(value = VaultUtils.class, remap = false)
public abstract class MixinVaultUtils {

    @Shadow
    public static boolean hasObjective(Vault vault, Class<? extends Objective> objectiveClass) {
        return false;
    }

    @Inject(method = "isBrazierVault", at = @At("HEAD"), cancellable = true)
    private static void isHauntedBraziersVault(Vault vault, CallbackInfoReturnable<Boolean> cir) {
        if(hasObjective(vault, HauntedBraziersObjective.class)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getVaultRadius", at = @At("HEAD"), cancellable = true)
    private static void fixInfiniteRadius(VaultGridLayout layout, Vault vault, CallbackInfoReturnable<Integer> cir){
       if (layout instanceof ClassicInfiniteLayout) {
           cir.setReturnValue(100);
       }
    }
}
