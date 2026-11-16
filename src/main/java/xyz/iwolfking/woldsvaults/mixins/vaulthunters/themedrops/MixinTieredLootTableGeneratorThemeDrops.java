package xyz.iwolfking.woldsvaults.mixins.vaulthunters.themedrops;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.core.util.WeightedTree;
import iskallia.vault.core.world.loot.LootPool;
import iskallia.vault.core.world.loot.entry.LootEntry;
import iskallia.vault.core.world.loot.generator.TieredLootTableGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.api.util.ducks.DuckThemeDrops;
import xyz.iwolfking.woldsvaults.loot.themedrops.ThemeDropInjections;

import javax.annotation.Nullable;
import java.util.Map;

@Mixin(value = TieredLootTableGenerator.class, remap = false)
public class MixinTieredLootTableGeneratorThemeDrops implements DuckThemeDrops {
    @Shadow
    @Final
    protected Map<LootPool, Integer> poolToIndex;

    @Unique
    @Nullable
    private ThemeDropInjections woldsvaults$themeDrops;

    @Override
    @Nullable
    public ThemeDropInjections getThemeDrops() {
        return this.woldsvaults$themeDrops;
    }

    @Override
    public void setThemeDrops(@Nullable ThemeDropInjections themeDrops) {
        this.woldsvaults$themeDrops = themeDrops;
    }

    /**
     * Swaps a tier for its theme drop wrapper as the adjusted pool is built. The weight passes through unchanged and
     * the wrapper takes the tier's frequency slot, so the chest's rarity is counted as without the drop.
     */
    @WrapOperation(
            method = "generateEntry",
            at = @At(value = "INVOKE", target = "Liskallia/vault/core/world/loot/LootPool;addTree(Liskallia/vault/core/util/WeightedTree;D)Liskallia/vault/core/util/WeightedTree;")
    )
    private WeightedTree<LootEntry> graftThemeDrops(LootPool adjustedPool, WeightedTree<LootEntry> tier, double weight, Operation<WeightedTree<LootEntry>> original) {
        if (this.woldsvaults$themeDrops == null) {
            return original.call(adjustedPool, tier, weight);
        }
        int tierIndex = adjustedPool.getChildren().size();
        LootPool wrapper = this.woldsvaults$themeDrops.wrap(tierIndex, tier);
        if (wrapper == null) {
            return original.call(adjustedPool, tier, weight);
        }
        Integer slot = this.poolToIndex.get(tier);
        if (slot == null) {
            WoldLogger.errorOnce("theme-drops-no-slot-" + tierIndex,
                    "Theme drops skip tier {} because the generator has no frequency slot for it", tierIndex);
            return original.call(adjustedPool, tier, weight);
        }
        this.poolToIndex.put(wrapper, slot);
        return original.call(adjustedPool, wrapper, weight);
    }
}
