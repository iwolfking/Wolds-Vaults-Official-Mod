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
 * Extender runs from {@link GodNodeVaultStart}; the rest from the shared {@link GodNodeTicker}.
 */
public final class WendarrTimeHandlers {
    private WendarrTimeHandlers() {
    }

    /** Extender: adds vault time as a {@link GreedExtension}, per player and stacking across a party. */
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

    /** Speed Demon: a faster vault clock for a party-wide aura. Deactivation drops the aura only. */
    public record SpeedDemonHandler(GodEffect effect) implements TickContributor {
        @Override
        public void tick(GodNodeContext context) {
        }

        @Override
        public void onDeactivated(ServerPlayer player, String effectId) {
            WendarrClockNodes.clearAura(player);
        }
    }

    /** Quick Search: a faster vault clock plus an omega weight modifier, both vault state. */
    public record QuickSearchHandler(GodEffect effect) implements TickContributor {
        @Override
        public void tick(GodNodeContext context) {
        }
    }

    /** Paced Strikes: a damage factor of {@code sqrt((reference + minutesLeft) / reference)}. */
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

    /** Edge of Time: a flat damage multiplier paid for in vault time, queued per hit. */
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

    /** Temporal Shielding's settle pass: pays off the vault time its reductions book. */
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
