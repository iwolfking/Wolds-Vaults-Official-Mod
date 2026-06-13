package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Modifiers;
import iskallia.vault.core.vault.modifier.VaultModifierStack;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.item.crystal.modifiers.CrystalModifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.lib.MixinModifierContextAccessor;
import xyz.iwolfking.woldsvaults.api.lib.VaultModifierStackInterface;
import xyz.iwolfking.woldsvaults.modifiers.vault.lib.SettableValueVaultModifier;

import java.util.Optional;

@Mixin(value = CrystalModifiers.class, remap = false)
public class MixinCrystalModifiers {
    @WrapOperation(method = "lambda$configure$0", at = @At(value = "INVOKE", target = "Liskallia/vault/core/vault/Modifiers;addModifier(Liskallia/vault/core/vault/modifier/spi/VaultModifier;IZLiskallia/vault/core/random/RandomSource;)Liskallia/vault/core/vault/Modifiers;"))
    private static Modifiers handleSettingValue(Modifiers instance, VaultModifier<?> modifier, int amount, boolean display, RandomSource random, Operation<Modifiers> original, @Local(argsOnly = true) VaultModifierStack stack) {
        if (stack.getModifier() instanceof SettableValueVaultModifier<?> settableValueVaultModifier) {
            WoldsVaults.LOGGER.info("Creating modifier with value context");
            return instance.addModifier(stack.getModifier(), stack.getSize(), true, random, context ->
                    ((MixinModifierContextAccessor) context).woldsVaults_Dev$setValue(settableValueVaultModifier.properties().getValue())
            );
        } else {
            return original.call(instance, modifier, amount, true, random);
        }
    }
}
