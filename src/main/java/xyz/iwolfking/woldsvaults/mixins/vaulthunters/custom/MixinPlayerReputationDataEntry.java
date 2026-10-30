package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import xyz.iwolfking.woldsvaults.api.util.GodMasteryHelper;

/**
 * The per-player reputation entry clamps additions at a hardcoded 50 (an early-out guard plus
 * a Math.min). The entry has no idea which player it belongs to, so the effective cap
 * (50 + God's Mastery count) is published by the outer PlayerReputationData statics through
 * {@link GodMasteryHelper#currentCap()} before this code runs.
 */
@Mixin(targets = "iskallia.vault.world.data.PlayerReputationData$Entry", remap = false)
public class MixinPlayerReputationDataEntry {
    @ModifyConstant(method = "addReputation", constant = @Constant(intValue = 50), require = 2)
    private int woldsVaults$entryCap(int baseCap) {
        return GodMasteryHelper.currentCap();
    }
}
