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

    /**
     * Map tier (0-5) of the mapped strongbox this generator rolls for, set by the chest tile
     * entity mixin before generate() runs. -1 (everything that is not a mapped strongbox) keeps
     * every path below identical to the chest behavior.
     */
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

    /**
     * Mapped strongboxes add a few base rolls per tier, lifting the whole quantity curve. The
     * Tenos chest nodes (Massive Chests, Expert Looter) add their extra rolls at the same point,
     * read off the generator's source entity - the chest tile entity sets it to the opening player.
     * A greed medallion on the vault scales the result last, so its "+X% Chest Rolls" multiplies
     * every additive source rather than joining them.
     */
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

    /**
     * Mapped strongboxes cap at 72-162 rolls by tier instead of the constructor's 54. The Tenos
     * chest nodes lift the cap by the same amount they add to the base roll, so Massive Chests
     * reaches 74 rather than piling rolls onto a cap that would throw them away again.
     */
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
     * Replaces the uniform weight * (1 + itemRarity) sub-pool scaling with per-pool curves on
     * 5-pool (mapped vault) tables; 4-pool tables keep the incoming vanilla weight. generate()
     * fills key = [roll, w0..wn-1] before generateEntry, and the adjusted pool being built here
     * holds one child per already-scaled sub-pool, giving the base weight and pool index directly.
     * Mapped strongboxes (mapTier >= 0) use the tier-steepened strongbox curves; mapped chests
     * keep the plain mythic curve.
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
     * Routes the rarity-percentile CDF three ways. Strongboxes skip it entirely - their rarity
     * comes from the map tier, and building the exact CDF at a T6 strongbox's 162 rolls would
     * enumerate ~30M compositions (multi-second freeze, GBs of cache). 5-pool mapped-chest
     * tables above EXACT_ROLL_LIMIT rolls use the O(pools) Gaussian approximation instead of
     * growing the exact cache into the hundreds of MB lategame. Everything else (4-pool tables,
     * low-roll 5-pool tables) keeps the exact enumeration. Returning null skips CDF::new; the
     * paired wrap below turns the null into the substitute value.
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

    /**
     * Pairs with {@link #routeCdfBuild}: when the CDF build was skipped, strongboxes resolve
     * 1.0 (the value is never read - rarity comes from the map tier) and mapped chests resolve
     * the Gaussian approximation.
     */
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
