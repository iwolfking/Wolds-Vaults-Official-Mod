package xyz.iwolfking.woldsvaults.mixins.vaulthunters;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.core.util.WeightedTree;
import iskallia.vault.core.world.loot.LootPool;
import iskallia.vault.core.world.loot.entry.LootEntry;
import iskallia.vault.core.world.loot.generator.TieredLootTableGenerator;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.api.util.ducks.DuckMapTier;
import xyz.iwolfking.woldsvaults.gods.trees.tenos.TenosChestRolls;
import xyz.iwolfking.woldsvaults.loot.MythicLootScaling;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionEffects;
import xyz.iwolfking.woldsvaults.loot.StrongboxTierScaling;
import xyz.iwolfking.woldsvaults.loot.TieredCdfApprox;

import java.util.Map;
import java.util.function.Function;

@Mixin(value = TieredLootTableGenerator.class, remap = false)
public class MixinTieredLootTableGenerator implements DuckMapTier {
    @Shadow
    private double[] key;
    @Shadow
    public float itemRarity;

    /** Map tier (0-5) of the mapped strongbox this rolls for, set before {@code generate()}; -1 for none. */
    @Unique
    private int woldsvaults$mapTier = -1;

    @Override
    public int getMapTier() {
        return this.woldsvaults$mapTier;
    }

    @Override
    public void setMapTier(int tier) {
        this.woldsvaults$mapTier = tier;
    }

    @ModifyExpressionValue(
            method = "generate",
            at = @At(value = "FIELD", target = "Liskallia/vault/core/world/loot/generator/TieredLootTableGenerator;itemQuantity:F", opcode = Opcodes.GETFIELD)
    )
    private float alterItemQuantityScaling(float originalValue) {
        return 1.1f * (float) Math.log(originalValue + 1.0f);
    }

    /** Adds strongbox per-tier and Tenos base rolls, then applies the medallion's "+X% Chest Rolls" last. */
    @ModifyExpressionValue(
            method = "generate",
            at = @At(value = "INVOKE", target = "Liskallia/vault/core/world/roll/IntRoll;get(Liskallia/vault/core/random/RandomSource;)I")
    )
    private int addStrongboxBaseRolls(int roll) {
        int adjusted = this.woldsvaults$mapTier >= 0 ? roll + StrongboxTierScaling.baseRollBonus(this.woldsvaults$mapTier) : roll;
        Entity source = ((TieredLootTableGenerator) (Object) this).getSource();
        if (source instanceof Player player) {
            adjusted += TenosChestRolls.bonusRolls(player);
            adjusted = (int) Math.round(adjusted * GreedMedallionEffects.chestRollMultiplier(player));
        }
        return adjusted;
    }

    /** Mapped strongboxes cap at 72-162 rolls by tier instead of 54; Tenos chest nodes lift the cap too. */
    @ModifyExpressionValue(
            method = "generate",
            at = @At(value = "FIELD", target = "Liskallia/vault/core/world/loot/generator/TieredLootTableGenerator;maxRolls:I", opcode = Opcodes.GETFIELD)
    )
    private int raiseStrongboxMaxRolls(int maxRolls) {
        if (this.woldsvaults$mapTier >= 0) {
            return StrongboxTierScaling.maxRolls(this.woldsvaults$mapTier);
        }
        Entity source = ((TieredLootTableGenerator) (Object) this).getSource();
        return source instanceof Player player ? maxRolls + TenosChestRolls.bonusRolls(player) : maxRolls;
    }

    /**
     * Replaces the uniform {@code weight * (1 + itemRarity)} sub-pool scaling with per-pool curves on
     * 5-pool tables - strongbox curves for a mapped strongbox, mythic otherwise. 4-pool tables keep theirs.
     */
    @WrapOperation(
            method = "generateEntry",
            at = @At(value = "INVOKE", target = "Liskallia/vault/core/world/loot/LootPool;addTree(Liskallia/vault/core/util/WeightedTree;D)Liskallia/vault/core/util/WeightedTree;")
    )
    private WeightedTree<LootEntry> alterItemRarityScaling(LootPool adjustedPool, WeightedTree<LootEntry> childPool, double weight, Operation<WeightedTree<LootEntry>> original) {
        if (this.key != null && this.key.length - 1 == MythicLootScaling.MAPPED_POOL_COUNT) {
            int index = adjustedPool.getChildren().size();
            double scale = this.woldsvaults$mapTier >= 0
                    ? StrongboxTierScaling.poolScale(index, this.itemRarity, this.woldsvaults$mapTier)
                    : MythicLootScaling.poolScale(index, this.itemRarity);
            weight = this.key[index + 1] * scale;
        }
        return original.call(adjustedPool, childPool, weight);
    }

    /**
     * Routes the rarity-percentile CDF three ways: strongboxes skip it, 5-pool tables above
     * {@code EXACT_ROLL_LIMIT} rolls approximate, everything else enumerates exactly. Null skips
     * {@code CDF::new}, and {@link #routeCdfValue} substitutes the value.
     */
    @WrapOperation(
            method = "generate",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;computeIfAbsent(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;")
    )
    private Object routeCdfBuild(Map<Object, Object> cache, Object cdfKey, Function<Object, Object> factory, Operation<Object> original) {
        if (this.woldsvaults$mapTier >= 0) {
            return null;
        }
        if (this.key.length - 1 == MythicLootScaling.MAPPED_POOL_COUNT && this.key[0] > TieredCdfApprox.EXACT_ROLL_LIMIT) {
            return null;
        }
        return original.call(cache, cdfKey, factory);
    }

    /** Pairs with {@link #routeCdfBuild}: a skipped build resolves 1.0, or the Gaussian approximation. */
    @WrapOperation(
            method = "generate",
            at = @At(value = "INVOKE", target = "Liskallia/vault/core/world/loot/generator/TieredLootTableGenerator$CDF;get([I)D")
    )
    private double routeCdfValue(TieredLootTableGenerator.CDF cdf, int[] frequencies, Operation<Double> original) {
        if (cdf != null) {
            return original.call(cdf, frequencies);
        }
        return this.woldsvaults$mapTier >= 0 ? 1.0 : TieredCdfApprox.cdf(this.key, frequencies);
    }
}
