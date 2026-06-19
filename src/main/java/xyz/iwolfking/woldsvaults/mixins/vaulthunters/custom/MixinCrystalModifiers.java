package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.item.crystal.modifiers.CrystalModifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(value = CrystalModifiers.class, remap = false)
public class MixinCrystalModifiers {
    @WrapWithCondition(method = "configure", at = @At(value = "INVOKE", target = "Ljava/util/Optional;ifPresent(Ljava/util/function/Consumer;)V", ordinal = 0))
    private boolean cancelSigilIfBrazierVault(Optional<?> instance, Consumer<?> action, @Local(argsOnly = true) Vault vault) {
        if(VaultUtils.isBrazierVault(vault)) {
            return false;
        }

        return true;
    }
}
