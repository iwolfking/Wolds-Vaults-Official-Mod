package xyz.iwolfking.woldsvaults.gods.trees.wendarr;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.combat.FinalDamageStage;

/** Temporal Shielding: a post-mitigation {@link FinalDamageStage} reduction, paid in vault time. */
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
