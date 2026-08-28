package xyz.iwolfking.woldsvaults.events;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.milestones.trials.TrialMastery;

/**
 * Applies the trial mastery bonus as one multiplicative layer on everything a rank-up trial's runner
 * deals — melee, ranged and abilities alike — at LOW priority, so the hyperboss instrumentation
 * sampling at LOWEST still sees it.
 *
 * <p>Attacks that a previous hit spawned are skipped. Reaving, Echoing and the Fanged Strike proc all
 * derive their damage from a hit this handler has already multiplied, so multiplying them again would
 * compound the bonus once per link in the chain rather than applying it once per real hit.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class TrialMasteryDamage {
    private TrialMasteryDamage() {
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void applyTrialMastery(LivingHurtEvent event) {
        if (isDerivedProc()) {
            return;
        }
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        Vault vault = ServerVaults.get(player.level).orElse(null);
        if (vault == null) {
            return;
        }
        double multiplier = TrialMastery.getMultiplier(vault);
        if (multiplier <= 1.0D) {
            return;
        }
        event.setAmount((float) (event.getAmount() * multiplier));
    }

    /** True while the hit being resolved was spawned by an earlier hit rather than by the player. */
    private static boolean isDerivedProc() {
        return WoldActiveFlags.IS_REAVING_ATTACKING.isSet()
                || WoldActiveFlags.IS_ECHOING_ATTACKING.isSet()
                || WoldActiveFlags.IS_PROC_FANG_ATTACKING.isSet();
    }
}
