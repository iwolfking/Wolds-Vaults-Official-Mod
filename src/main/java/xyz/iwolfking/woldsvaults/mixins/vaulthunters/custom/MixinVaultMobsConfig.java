package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.config.VaultMobsConfig;
import iskallia.vault.config.greed.GreedTrialsScreenConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionEffects;

@Mixin(value = VaultMobsConfig.class, remap = false)
public class MixinVaultMobsConfig {

    /**
     * Removes the vault owner's greed rank from mob health and damage. Base multiplies both by
     * {@code difficulty + rank * increment}; with a zero increment only the difficulty multiplier remains, and the
     * crystal's medallion below is the single greed axis.
     */
    @WrapOperation(
        method = "scale",
        at = @At(value = "INVOKE", target = "Liskallia/vault/config/greed/GreedTrialsScreenConfig;getGreedTierDifficultyIncrement()D")
    )
    private static double dropGreedRankScaling(GreedTrialsScreenConfig instance, Operation<Double> original) {
        return 0.0D;
    }

    /** Multiplies vault mob health and damage by the medallion bonus, on the attribute's base value. */
    @WrapOperation(
        method = "lambda$scale$0",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;setBaseValue(D)V"),
        remap = true
    )
    private static void scaleMobStatsForMedallion(AttributeInstance instance, double baseValue, Operation<Void> original, @Local(argsOnly = true) LivingEntity entity) {
        if (instance.getAttribute() == Attributes.MAX_HEALTH || instance.getAttribute() == Attributes.ATTACK_DAMAGE) {
            baseValue *= GreedMedallionEffects.mobStatMultiplier(entity);
        }
        original.call(instance, baseValue);
    }
}
