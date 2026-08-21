package xyz.iwolfking.woldsvaults.gods.trees.wendarr;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.combat.FinalDamageStage;

/**
 * Temporal Shielding (r79), the one Wendarr node that must have the last word on a hit.
 *
 * <p>It is a reduction, so it sits in the shared post-mitigation {@link FinalDamageStage} rather
 * than on the pre-mitigation combat pipeline: that is what keeps it ordered against Second Chance
 * and the base mod's other reducers instead of racing them on event priority. Its handler is
 * therefore listener-bound - it claims no capability that would have it dispatched a second time -
 * except for the settle pass in {@link WendarrTimeHandlers}, which pays off the vault time each
 * reduction books here.
 */
public final class WendarrCombatNodes {
    private static final ResourceLocation TEMPORAL_SHIELDING_STAGE = WoldsVaults.id("wendarr_temporal_shielding");

    private WendarrCombatNodes() {
    }

    static void register() {
        FinalDamageStage.register(TEMPORAL_SHIELDING_STAGE, FinalDamageStage.ORDER_REDUCTION, (event, amount) -> {
            if (!(event.getEntityLiving() instanceof ServerPlayer player)) {
                return amount;
            }
            if (!WendarrNodes.isActive(player, WendarrNodes.TEMPORAL_SHIELDING)) {
                return amount;
            }
            WendarrVaultTime.queueDrain(player);
            return amount * WendarrNodeHandlers.params(WendarrNodes.TEMPORAL_SHIELDING,
                    WendarrNodeHandlers.TemporalShieldingParams.class).reduction();
        });
    }
}
