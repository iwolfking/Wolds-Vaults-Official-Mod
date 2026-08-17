package xyz.iwolfking.woldsvaults.milestones.trials;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.vault.objective.RebirthObjective;
import iskallia.vault.core.vault.player.Completion;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.player.Runner;
import iskallia.vault.core.vault.stat.StatCollector;
import iskallia.vault.core.vault.time.TickClock;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * The vessel half of the rank-up trials, driven from {@code MixinRebirthObjective}.
 *
 * <p>The base mod's greed trial is an endless endurance fight: the vessel cannot be killed (its
 * health is clamped at a floor of one and both {@code die} and {@code kill} are neutered), phases
 * double their damage goal forever, and the only way the run ends is the player dying. A rank-up
 * trial keeps the arena and the vessel but replaces that shape with a single phase whose damage
 * goal is the sheet's health pool, a five-minute clock, and a win the moment the pool is emptied -
 * the vessel is never required to die, which is just as well because it cannot.</p>
 *
 * <p>The sheet's damage and speed numbers land as flat attribute multipliers on top of the
 * vessel's own built-in ramp (1.2x damage per minute for the first five, +5% speed per minute),
 * which is the "ramps up over the fight" the design doc asks for.</p>
 */
public final class GreedTrialVessel {
    private static final UUID DAMAGE_UUID =
            UUID.nameUUIDFromBytes("woldsvaults:greed_trial_vessel_damage".getBytes(StandardCharsets.UTF_8));
    private static final UUID SPEED_UUID =
            UUID.nameUUIDFromBytes("woldsvaults:greed_trial_vessel_speed".getBytes(StandardCharsets.UTF_8));

    private GreedTrialVessel() {
    }

    /** The trial row this rebirth vault is running, or null when it is an ordinary greed trial. */
    public static GreedTrial trial(Vault vault) {
        GreedTrial trial = GreedTrials.trial(vault);
        return trial != null && trial.getKind() == GreedTrial.Kind.VESSEL ? trial : null;
    }

    public static boolean isTrial(Vault vault) {
        return trial(vault) != null;
    }

    /**
     * Runs one tick of the trial layer: scales the vessel, keeps the displayed damage goal honest,
     * and ends the vault the moment the health pool is emptied or the clock runs out. Called from
     * the tail of {@code RebirthObjective#tickServer}, so it sees the same state the base tick just
     * finished writing.
     */
    public static void tick(Objective objective, VirtualWorld world, Vault vault) {
        GreedTrial trial = trial(vault);
        if (trial == null || objective.getOr(RebirthObjective.TRIAL_ENDED, false)) {
            return;
        }
        applyVesselScaling(objective, world, trial);
        if (!objective.getOr(RebirthObjective.FIGHT_STARTED, false)) {
            return;
        }
        objective.set(RebirthObjective.PHASE_DAMAGE_GOAL, (int) Math.min(Integer.MAX_VALUE, trial.getVesselHealth()));
        float dealt = objective.getOr(RebirthObjective.PHASE_DAMAGE_DEALT, 0.0F);
        if (dealt >= (float) trial.getVesselHealth()) {
            finish(objective, world, vault, true);
            return;
        }
        if (vault.get(Vault.CLOCK).getOr(TickClock.DISPLAY_TIME, 1) <= 0) {
            finish(objective, world, vault, false);
        }
    }

    /**
     * Applies the row's damage and speed multipliers to the live vessel. Idempotent through the two
     * fixed modifier ids, so it can run every tick and simply no-ops once the vessel has them; the
     * vessel spawns several ticks after the objective starts, which is why this is not a one-shot.
     */
    private static void applyVesselScaling(Objective objective, VirtualWorld world, GreedTrial trial) {
        UUID bossId = objective.getOr(RebirthObjective.BOSS_ID, null);
        if (bossId == null) {
            return;
        }
        Entity entity = world.getEntity(bossId);
        if (!(entity instanceof LivingEntity boss)) {
            return;
        }
        addMultiplier(boss.getAttribute(Attributes.ATTACK_DAMAGE), DAMAGE_UUID,
                "greed_trial_vessel_damage", trial.getVesselDamageScale() - 1.0D);
        addMultiplier(boss.getAttribute(Attributes.MOVEMENT_SPEED), SPEED_UUID,
                "greed_trial_vessel_speed", trial.getVesselSpeedBonus());
    }

    private static void addMultiplier(AttributeInstance attribute, UUID id, String name, double amount) {
        if (attribute == null || amount <= 0.0D || attribute.getModifier(id) != null) {
            return;
        }
        attribute.addPermanentModifier(new AttributeModifier(id, name, amount, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    /**
     * Ends a vessel trial, win or lose. Both outcomes hand the outro back to the base objective by
     * setting {@code TRIAL_END_TELEPORT_DELAY}, whose own tick teleports every runner home on their
     * stored join state and marks the vault finished; a win uses the standard fifteen seconds.
     */
    public static void finish(Objective objective, VirtualWorld world, Vault vault, boolean won) {
        objective.set(RebirthObjective.TRIAL_ENDED, true);
        objective.set(RebirthObjective.TRIAL_END_TELEPORT_DELAY,
                won ? GreedTrials.VICTORY_TICKS : GreedTrials.FAILURE_TICKS);
        discardVessel(objective, world);
        for (Listener listener : vault.get(Vault.LISTENERS).getAll()) {
            if (!(listener instanceof Runner)) {
                continue;
            }
            vault.ifPresent(Vault.STATS, stats -> stats.get(listener.getId())
                    .set(StatCollector.COMPLETION, won ? Completion.COMPLETED : Completion.FAILED));
            ServerPlayer player = listener.getPlayer().orElse(null);
            if (player == null) {
                WoldsVaults.LOGGER.warn("Vessel trial ended for an offline runner — no payout was made.");
                continue;
            }
            if (won) {
                GreedTrials.award(vault, player);
            } else {
                GreedTrials.forfeit(vault, player);
            }
            player.displayClientMessage(new net.minecraft.network.chat.TextComponent(
                            won ? "The Vessel yields. Extracting..." : "Out of time.")
                    .withStyle(won ? ChatFormatting.GOLD : ChatFormatting.RED), false);
        }
    }

    /**
     * The base mod's own phase damage goal, saturating at {@code Integer.MAX_VALUE} exactly as its
     * private version does. Used only to recognise an untouched goal.
     */
    public static int baseGoal(int phase) {
        int shift = Math.min(Math.max(phase, 0), 30);
        long doubled = 50_000L << shift;
        return doubled > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) doubled;
    }

    /**
     * The goal the greed trial HUD should draw. The synced {@code PHASE_DAMAGE_GOAL} is trusted
     * whenever it differs from the base curve, which is exactly when a rank-up trial has overwritten
     * it; an ordinary greed trial keeps the float curve, which is what the client mixin exists for
     * (the base field saturates at {@code Integer.MAX_VALUE} from phase 16 on).
     *
     * <p>Client-safe: pure arithmetic on a value the server already synced, with no world lookup.</p>
     */
    public static float displayGoal(int phase, int syncedGoal) {
        if (syncedGoal > 0 && syncedGoal != baseGoal(phase)) {
            return (float) syncedGoal;
        }
        return (float) (50000.0D * Math.pow(2, phase));
    }

    private static void discardVessel(Objective objective, VirtualWorld world) {
        UUID bossId = objective.getOr(RebirthObjective.BOSS_ID, null);
        if (bossId == null) {
            return;
        }
        Entity boss = world.getEntity(bossId);
        if (boss != null) {
            boss.discard();
        }
    }
}
