package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.config.greed.GreedTraderConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Mixin(value = GreedTraderConfig.class, remap = false)
public class MixinGreedTraderConfig {
    @Shadow
    private Map<Integer, List<GreedTraderConfig.TradeEntry>> tierPools;

    /**
     * Turns the shop's tier pools from cumulative into replace semantics: a rank sees exactly the
     * block declared for the highest rank at or below it, never the union of every block beneath it.
     *
     * <p>Base concatenates every {@code tierPools} entry whose key is at or below the rank, so a
     * weight declared once at Scavenger 1 is still in the pool at Legend, at its original price
     * band. That makes three things the greed rework's shop tables require impossible: a weight
     * that stays constant across ranks (declaring 200 sixteen times would total 3200), a weight
     * that falls with rank, and a price floor that rises with rank. It is also the source of the
     * dead-slot bug - an entry whose pool has aged out of its {@code maxGreedTier} keeps its
     * accumulated weight and silently drops the slot, which cost ~12-15% of every slot from greed
     * tier 6 upward. Under replace semantics a rank can only draw entries it declared itself, so
     * both problems go away by construction.</p>
     *
     * <p>{@code Math.max(1, greedTier)} is preserved from base: greed tier 0 reads the tier 1
     * block, which is what makes "anything unlocked at greed tier 0 folds into Scavenger 1" true.</p>
     */
    @Inject(method = "getAvailableEntries", at = @At("HEAD"), cancellable = true)
    private void useOnlyHighestDeclaredTier(int greedTier, CallbackInfoReturnable<List<GreedTraderConfig.TradeEntry>> cir) {
        int effectiveTier = Math.max(1, greedTier);
        List<GreedTraderConfig.TradeEntry> best = null;
        int bestTier = Integer.MIN_VALUE;
        for (Map.Entry<Integer, List<GreedTraderConfig.TradeEntry>> entry : this.tierPools.entrySet()) {
            int declaredTier = entry.getKey();
            if (declaredTier > effectiveTier || declaredTier <= bestTier) {
                continue;
            }
            bestTier = declaredTier;
            best = entry.getValue();
        }
        cir.setReturnValue(best == null ? Collections.emptyList() : new ArrayList<>(best));
    }
}
