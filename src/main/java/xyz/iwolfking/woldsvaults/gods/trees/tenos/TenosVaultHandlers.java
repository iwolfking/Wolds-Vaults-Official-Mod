package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.core.vault.Vault;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.gods.combat.GlobalDamageMultiplierRegistry;
import xyz.iwolfking.woldsvaults.gods.node.GodEffect;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeContext;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeTicker;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeVaultStart;
import xyz.iwolfking.woldsvaults.gods.node.TickContributor;
import xyz.iwolfking.woldsvaults.gods.node.VaultContributor;

/**
 * The Tenos nodes that reshape a vault, through {@link GodNodeVaultStart}, and those recomputed on
 * the shared {@link GodNodeTicker}, which undo themselves in {@link TickContributor#onDeactivated}.
 */
public final class TenosVaultHandlers {
    private TenosVaultHandlers() {
    }

    /** Omega Vault: attaches {@code woldsvaults:omega_fortune_double} once per vault. */
    public record OmegaVaultHandler(GodEffect effect) implements VaultContributor, TickContributor {
        @Override
        public void onVaultStart(GodNodeContext context, Vault vault) {
            TenosWorldNodes.reconcileOmegaRooms(vault);
        }

        @Override
        public void tick(GodNodeContext context) {
            TenosWorldNodes.reconcileOmegaRooms(TenosVaultUtil.vaultOf(context.player()));
        }
    }

    /** Master of Chests: attaches {@code woldsvaults:tenos_master_of_chests} once per vault. */
    public record MasterOfChestsHandler(GodEffect effect) implements VaultContributor, TickContributor {
        @Override
        public void onVaultStart(GodNodeContext context, Vault vault) {
            TenosWorldNodes.reconcileCascading(vault);
        }

        @Override
        public void tick(GodNodeContext context) {
            TenosWorldNodes.reconcileCascading(TenosVaultUtil.vaultOf(context.player()));
        }
    }

    public record ChallengeTacklerHandler(GodEffect effect) implements VaultContributor {
        @Override
        public void onVaultStart(GodNodeContext context, Vault vault) {
            TenosChallengeTackler.boost(vault, this.effect.params(TenosNodeHandlers.ChallengeTacklerParams.class));
        }
    }

    /** Sack of Mobs: republishes the damage factor derived from the kill count once a second. */
    public record SackOfMobsHandler(GodEffect effect) implements TickContributor {
        @Override
        public void tick(GodNodeContext context) {
            TenosSackOfMobs.updateMultiplier(context.player(),
                    this.effect.params(TenosNodeHandlers.SackOfMobsParams.class).log_base());
        }

        @Override
        public void onDeactivated(ServerPlayer player, String effectId) {
            GlobalDamageMultiplierRegistry.remove(player, TenosNodes.key(effectId));
        }
    }

    /** Unstoppable Greed: a global damage factor of {@code 1 + ratio * lootStatSum}, never below 1. */
    public record UnstoppableGreedHandler(GodEffect effect) implements TickContributor {
        @Override
        public void tick(GodNodeContext context) {
            ServerPlayer player = context.player();
            float factor = 1.0F + this.effect.params(TenosNodeHandlers.UnstoppableGreedParams.class).ratio()
                    * TenosLootStats.lootStatSum(player);
            GlobalDamageMultiplierRegistry.register(player, TenosNodes.key(context.effectId()),
                    Math.max(1.0F, factor));
        }

        @Override
        public void onDeactivated(ServerPlayer player, String effectId) {
            GlobalDamageMultiplierRegistry.remove(player, TenosNodes.key(effectId));
        }
    }

    /** Gold Plating's settle pass: bills the accrued debt, and once more on deactivation. */
    public record GoldPlatingHandler(GodEffect effect) implements TickContributor {
        @Override
        public void tick(GodNodeContext context) {
            TenosGoldPlating.settle(context.player());
        }

        @Override
        public void onDeactivated(ServerPlayer player, String effectId) {
            TenosGoldPlating.settle(player);
        }
    }

    /** Deep Reserves: keeps the transient {@code MANA_MAX} modifier applied while the node is live. */
    public record DeepReservesHandler(GodEffect effect) implements TickContributor {
        @Override
        public void tick(GodNodeContext context) {
            TenosMana.applyDeepReserves(context.player(),
                    this.effect.params(TenosNodeHandlers.DeepReservesParams.class).multiplier());
        }

        @Override
        public void onDeactivated(ServerPlayer player, String effectId) {
            TenosMana.removeDeepReserves(player);
        }
    }
}
