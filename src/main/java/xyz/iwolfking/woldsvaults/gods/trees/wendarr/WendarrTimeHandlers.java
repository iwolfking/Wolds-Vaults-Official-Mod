package xyz.iwolfking.woldsvaults.gods.trees.wendarr;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.core.vault.time.TickClock;
import iskallia.vault.core.vault.time.modifier.GreedExtension;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.combat.GlobalDamageMultiplierRegistry;
import xyz.iwolfking.woldsvaults.gods.node.CombatContributor;
import xyz.iwolfking.woldsvaults.gods.node.GodDamageContext;
import xyz.iwolfking.woldsvaults.gods.node.GodEffect;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeContext;
import xyz.iwolfking.woldsvaults.gods.node.GodNodePreviews;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeTicker;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeVaultStart;
import xyz.iwolfking.woldsvaults.gods.node.TickContributor;
import xyz.iwolfking.woldsvaults.gods.node.VaultContributor;

import javax.annotation.Nullable;

/**
 * The Wendarr nodes that act on the vault clock, and the two that trade vault time for damage.
 *
 * <p>Extender is this tree's only {@link VaultContributor}: it grants its time once per runner per
 * vault, which is exactly the granularity {@link GodNodeVaultStart} dispatches at, so the tree no
 * longer carries its own "already granted" bookkeeping.
 *
 * <p>Everything else here is on the shared {@link GodNodeTicker} at its one-second cadence, which
 * is the cadence these nodes already ran at inside the vault listener tick. Speed Demon and Quick
 * Search are party-wide and vault-sticky by design, so neither acts from its holder's tick at all:
 * the one vault reconcile in {@link WendarrClockNodes} runs as a tree pass over every vault with
 * an online runner, holder or not. That reconcile only ever applies, so the last holder losing the
 * node, leaving or logging out leaves the party's clock rate and aura exactly where they were, and
 * the vault carries them - and hands them to anyone who joins later - until it ends.
 *
 * <p>Both time-for-damage trades bill the vault clock. Vault time is shared by the whole party, so
 * the debt is accumulated per player in {@link WendarrVaultTime} and settled at most once a second
 * instead of writing the clock on every hit - the per-instance cost is unchanged, the clock writes
 * are not per-hit.
 */
public final class WendarrTimeHandlers {
    private WendarrTimeHandlers() {
    }

    /**
     * Extender grants its time per player and stacks across a party, exactly like the shipped
     * greed vault-time node, so it reuses the base mod's own {@link GreedExtension} rather than
     * writing {@code DISPLAY_TIME} by hand.
     */
    public record ExtenderHandler(GodEffect effect) implements VaultContributor {
        @Override
        public void onVaultStart(GodNodeContext context, Vault vault) {
            TickClock clock = vault.get(Vault.CLOCK);
            if (clock == null) {
                WoldsVaults.LOGGER.error("Extender found no clock on the vault {} joined; its time grant was lost.",
                        context.player().getGameProfile().getName());
                return;
            }
            clock.addModifier(new GreedExtension(context.player(),
                    this.effect.params(WendarrNodeHandlers.ExtenderParams.class).ticks()));
        }
    }

    /**
     * Speed Demon: a faster vault clock in exchange for a party-wide stat and armour aura. A second
     * player with the same node changes nothing, because the rate is a single fixed factor key on
     * the vault, and the aura is applied to a runner only while that runner does not already carry
     * it.
     *
     * <p>The per-holder tick is deliberately empty - the vault pass in {@link WendarrClockNodes}
     * owns marking and applying. Deactivation takes the aura off this player, whose damage factor
     * and armour modifier are transient state on the player rather than on the vault. It does not
     * touch the vault: the clock has already been running fast, and the party keeps both the rate
     * and the aura until the vault ends. A player who is still a runner therefore has the aura back
     * on the next pass.
     */
    public record SpeedDemonHandler(GodEffect effect) implements TickContributor {
        @Override
        public void tick(GodNodeContext context) {
        }

        @Override
        public void onDeactivated(ServerPlayer player, String effectId) {
            WendarrClockNodes.clearAura(player);
        }
    }

    /**
     * Quick Search: a faster vault clock plus the shipped 4x omega room weight modifier. Both are
     * vault state, and both are sticky, so there is nothing for a deactivation to take back; the
     * per-holder tick is empty for the same reason as Speed Demon's.
     */
    public record QuickSearchHandler(GodEffect effect) implements TickContributor {
        @Override
        public void tick(GodNodeContext context) {
        }
    }

    /**
     * Paced Strikes. Damage grows with the time left on the vault clock, so it is recomputed once
     * a second as a named global factor rather than per hit.
     */
    public record PacedStrikesHandler(GodEffect effect) implements TickContributor {
        @Override
        public void tick(GodNodeContext context) {
            ServerPlayer player = context.player();
            ResourceLocation key = WendarrNodes.key(context.effectId());
            Vault vault = vaultOf(player);
            if (vault == null) {
                GlobalDamageMultiplierRegistry.remove(player, key);
                return;
            }
            float reference = this.effect.params(WendarrNodeHandlers.PacedStrikesParams.class).reference_minutes();
            GlobalDamageMultiplierRegistry.register(player, key, pacedStrikesFactor(reference, minutesLeft(vault)));
        }

        @Override
        public void onDeactivated(ServerPlayer player, String effectId) {
            GlobalDamageMultiplierRegistry.remove(player, WendarrNodes.key(effectId));
        }
    }

    private static float minutesLeft(Vault vault) {
        TickClock clock = vault.get(Vault.CLOCK);
        return clock == null ? 0.0F : Math.max(0, clock.get(TickClock.DISPLAY_TIME)) / 20.0F / 60.0F;
    }

    private static float pacedStrikesFactor(float reference, float minutesLeft) {
        return (float) Math.sqrt((reference + minutesLeft) / reference);
    }

    /** The gods tab preview of Paced Strikes: the multiplier the time left on the player's vault gives. */
    static GodNodePreviews.Preview previewPacedStrikes(ServerPlayer player) {
        float reference = WendarrNodeHandlers.params(WendarrNodes.PACED_STRIKES,
                WendarrNodeHandlers.PacedStrikesParams.class).reference_minutes();
        Vault vault = vaultOf(player);
        float minutes = vault == null ? 0.0F : minutesLeft(vault);
        float factor = pacedStrikesFactor(reference, minutes);
        String referenceText = GodNodePreviews.number(reference);
        return new GodNodePreviews.Working(VaultGod.WENDARR)
                .formula("Damage multiplier", "sqrt((" + referenceText + " + t) / " + referenceText + ")")
                .input("t", "the time left on your vault, in minutes",
                        vault == null ? "0 (not in a vault)" : GodNodePreviews.number(minutes))
                .result("sqrt(" + GodNodePreviews.number(reference + minutes) + " / " + referenceText + ")", factor)
                .inactive(!WendarrNodes.isActive(player, WendarrNodes.PACED_STRIKES))
                .build(factor);
    }

    /**
     * Edge of Time: a flat damage multiplier paid for in vault time, billed per hit.
     *
     * <p>The billing leg runs pre-mitigation on the shared combat pipeline. It reads the running
     * amount only to tell a real hit from a cancelled one and never changes it, so the seam is
     * free to be the earlier of the two - what matters is that it is the one ordered pipeline
     * rather than a private {@code LivingHurtEvent} listener racing it.
     */
    public record EdgeOfTimeHandler(GodEffect effect) implements TickContributor, CombatContributor {
        @Override
        public void tick(GodNodeContext context) {
            ServerPlayer player = context.player();
            ResourceLocation key = WendarrNodes.key(context.effectId());
            Vault vault = vaultOf(player);
            if (vault == null) {
                GlobalDamageMultiplierRegistry.remove(player, key);
                return;
            }
            GlobalDamageMultiplierRegistry.register(player, key,
                    this.effect.params(WendarrNodeHandlers.EdgeOfTimeParams.class).multiplier());
            WendarrVaultTime.settleDrain(player, vault);
        }

        @Override
        public void onOutgoing(GodNodeContext context, GodDamageContext damage) {
            if (damage.getAmount() <= 0.0F) {
                return;
            }
            WendarrVaultTime.queueDrain(context.player());
        }

        @Override
        public void onDeactivated(ServerPlayer player, String effectId) {
            GlobalDamageMultiplierRegistry.remove(player, WendarrNodes.key(effectId));
        }
    }

    /**
     * Temporal Shielding's settle pass. The reduction itself is a sub-stage of the shared final
     * damage stage, registered in {@link WendarrCombatNodes}, because it is post-mitigation by
     * design - it must be ordered against Second Chance and the base mod's other reducers, which
     * the pre-mitigation pipeline cannot express. What is left for the ticker is paying off the
     * vault time each reduction books, which the node owes whether or not it also holds Edge of
     * Time.
     */
    public record TemporalShieldingHandler(GodEffect effect) implements TickContributor {
        @Override
        public void tick(GodNodeContext context) {
            ServerPlayer player = context.player();
            WendarrVaultTime.settleDrain(player, vaultOf(player));
        }
    }

    @Nullable
    private static Vault vaultOf(ServerPlayer player) {
        return ServerVaults.get(player.level).orElse(null);
    }
}
