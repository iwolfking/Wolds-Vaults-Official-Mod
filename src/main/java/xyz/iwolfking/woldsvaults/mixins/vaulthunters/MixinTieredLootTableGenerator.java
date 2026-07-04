package xyz.iwolfking.woldsvaults.mixins.vaulthunters;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.core.util.WeightedTree;
import iskallia.vault.core.world.loot.LootPool;
import iskallia.vault.core.world.loot.entry.LootEntry;
import iskallia.vault.core.world.loot.generator.TieredLootTableGenerator;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.loot.MythicLootScaling;

@Mixin(value = TieredLootTableGenerator.class, remap = false)
public class MixinTieredLootTableGenerator {
    @Shadow
    private double[] key;
    @Shadow
    public float itemRarity;

    @ModifyExpressionValue(
            method = "generate",
            at = @At(value = "FIELD", target = "Liskallia/vault/core/world/loot/generator/TieredLootTableGenerator;itemQuantity:F", opcode = Opcodes.GETFIELD)
    )
    private float alterItemQuantityScaling(float originalValue) {
        return 1.1f * (float) Math.log(originalValue + 1.0f);
    }

    /**
     * Replaces the uniform weight * (1 + itemRarity) sub-pool scaling with per-pool curves on
     * 5-pool (mapped vault) tables; 4-pool tables keep the incoming vanilla weight. generate()
     * fills key = [roll, w0..wn-1] before generateEntry, and the adjusted pool being built here
     * holds one child per already-scaled sub-pool, giving the base weight and pool index directly.
     */
    @WrapOperation(
            method = "generateEntry",
            at = @At(value = "INVOKE", target = "Liskallia/vault/core/world/loot/LootPool;addTree(Liskallia/vault/core/util/WeightedTree;D)Liskallia/vault/core/util/WeightedTree;")
    )
    private WeightedTree<LootEntry> alterItemRarityScaling(LootPool adjustedPool, WeightedTree<LootEntry> childPool, double weight, Operation<WeightedTree<LootEntry>> original) {
        if (this.key != null && this.key.length - 1 == MythicLootScaling.MAPPED_POOL_COUNT) {
            int index = adjustedPool.getChildren().size();
            weight = this.key[index + 1] * MythicLootScaling.poolScale(index, this.itemRarity);
        }
        return original.call(adjustedPool, childPool, weight);
    }

}