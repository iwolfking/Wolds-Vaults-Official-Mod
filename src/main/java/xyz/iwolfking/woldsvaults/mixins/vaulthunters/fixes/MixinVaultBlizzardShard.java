package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.entity.entity.VaultBlizzardShard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = VaultBlizzardShard.class, remap = false)
public class MixinVaultBlizzardShard {
    /**
     * Drops damage increase from the baked shard damage. The hurt event applies damage increase to the shard
     * anyway, so baking it in as well counted it twice.
     */
    @ModifyVariable(method = "getShardBlizzardDamage", at = @At("STORE"), ordinal = 1)
    private float woldsVaults$skipBakedDamageIncrease(float attackDamagePercentageIncrease) {
        return 0.0F;
    }

    /**
     * Lifts the level 8 clamp on the Blizzard Effect etching's Vulnerable.
     */
    @ModifyConstant(method = "lambda$destroy$5", constant = @Constant(intValue = 8))
    private int woldsVaults$uncapBlizzardVulnerable(int constant) {
        return Integer.MAX_VALUE;
    }
}
