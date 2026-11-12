package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.VaultMod;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.item.crystal.modifiers.CrystalModifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.util.ObjectiveDifficulty;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(value = CrystalModifiers.class, remap = false)
public class MixinCrystalModifiers {
    @WrapWithCondition(method = "configure", at = @At(value = "INVOKE", target = "Ljava/util/Optional;ifPresent(Ljava/util/function/Consumer;)V", ordinal = 0))
    private boolean cancelSigilIfBrazierVault(Optional<?> instance, Consumer<?> action, @Local(argsOnly = true) Vault vault) {
        return !VaultUtils.isBrazierVault(vault);
    }

    /** The sigil's crate tiers come from its difficulty (objective_difficulty.json), not from a per-sigil file. */
    @Inject(method = "configure", at = @At("TAIL"))
    private void woldsvaults$addSigilCrateTiers(Vault vault, RandomSource random, String sigil, CallbackInfo ci) {
        if (sigil == null || VaultUtils.isBrazierVault(vault)) {
            return;
        }
        int crateTiers = ObjectiveDifficulty.crateTiers(sigil);
        if (crateTiers <= 0) {
            return;
        }
        VaultModifierRegistry.getOpt(VaultMod.id("crate_tier")).ifPresent(modifier ->
                vault.ifPresent(Vault.MODIFIERS, modifiers -> modifiers.addModifier(modifier, crateTiers, false, random)));
    }
}
