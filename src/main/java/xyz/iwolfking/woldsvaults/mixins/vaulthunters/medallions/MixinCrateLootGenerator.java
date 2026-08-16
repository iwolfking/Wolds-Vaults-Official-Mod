package xyz.iwolfking.woldsvaults.mixins.vaulthunters.medallions;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.vault.CrateLootGenerator;
import iskallia.vault.core.vault.Vault;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionEffects;

@Mixin(value = CrateLootGenerator.class, remap = false)
public class MixinCrateLootGenerator {

    /**
     * Greed medallions multiply completion crate loot. The crate's roll count is
     * {@code roll * (1 + itemQuantity)}, so scaling the item quantity to
     * {@code (1 + itemQuantity) * (1 + bonus) - 1} multiplies the expected item count by exactly
     * {@code 1 + bonus} - which is the multiplicative stacking the design asks for, and cannot be
     * had by scaling the quantity stat directly. Only {@code createLoot} is touched, so the
     * /give-loot command path keeps its unmodified numbers.
     */
    @ModifyExpressionValue(
            method = "createLoot",
            at = @At(value = "FIELD", target = "Liskallia/vault/core/vault/CrateLootGenerator;itemQuantity:F", opcode = Opcodes.GETFIELD)
    )
    private float scaleCrateLootForMedallion(float itemQuantity, @Local(argsOnly = true) Vault vault) {
        double multiplier = GreedMedallionEffects.crateLootMultiplier(vault);
        if (multiplier == 1.0D) {
            return itemQuantity;
        }
        return (float) ((1.0D + itemQuantity) * multiplier - 1.0D);
    }
}
