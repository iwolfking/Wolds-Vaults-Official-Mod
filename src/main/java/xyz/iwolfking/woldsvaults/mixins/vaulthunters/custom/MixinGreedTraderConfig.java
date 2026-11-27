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
     * Turns the shop's tier pools from cumulative into replace semantics: a rank sees only the block
     * declared for the highest rank at or below it. Greed tier 0 reads the tier 1 block.
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
