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
 * The Tenos nodes that reshape a vault, and the ones whose effect is recomputed on a cadence.
 *
 * <p>The three modifier nodes reach the vault through {@link GodNodeVaultStart}, which dispatches
 * once per runner per vault - the granularity these were already hand-rolling out of
 * {@code LISTENER_JOIN} plus a runner scan. Omega Vault and Master of Chests additionally ride the
 * shared ticker, because their reconcile is what re-applies their modifier to a vault that outlived
 * a server restart and what lets the node land mid-vault; Challenge Tackler does not, because it
 * only ever acted on join.
 *
 * <p>Everything else here is on the shared {@link GodNodeTicker} at its one-second cadence, which
 * is the cadence these nodes already ran at on their own tick listeners. Each of them reaches
 * outside the attribute snapshot - a global damage factor, a vanilla attribute modifier, a gold
 * debt - so each owns taking that back through {@link TickContributor#onDeactivated}, which is what
 * retires the "iterate every player and remove what they no longer hold" scans.
 */
public final class TenosVaultHandlers {
    private TenosVaultHandlers() {
    }

    /**
     * Omega Vault: doubled omega room weight, as {@code woldsvaults:omega_fortune_double} attached
     * once per vault. The whole of its number lives in {@code vault_modifiers.json}.
     */
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

    /**
     * Master of Chests: {@code woldsvaults:tenos_master_of_chests}, a group over the pack's
     * one-percent cascade rows, attached once per vault. Its stack count per row lives in
     * {@code vault_modifiers.json}.
     */
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

    /** Challenge Tackler: the sigil's own crate tiers, recomputed on join and part-granted again. */
    public record ChallengeTacklerHandler(GodEffect effect) implements VaultContributor {
        @Override
        public void onVaultStart(GodNodeContext context, Vault vault) {
            TenosChallengeTackler.boost(vault, this.effect.params(TenosNodeHandlers.ChallengeTacklerParams.class));
        }
    }

    /**
     * Sack of Mobs. The kill count moves only on a kill, but the damage factor derived from it is
     * pushed into the shared global damage registry once a second rather than being recomputed on
     * every hit.
     */
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

    /**
     * Unstoppable Greed. It rides the global damage registry rather than a dynamic gear attribute
     * on purpose: emitting {@code damage_increase} from a stat contributor would mean reading the
     * player's own item quantity while their snapshot is still being built. Item quantity and
     * rarity only move on gear changes, so a one second refresh is indistinguishable from live.
     *
     * <p>The registry is the whole of this node, not the attack-damage half of it. It scales every
     * hit whose true source is a player, ability damage included, so pairing it with a
     * {@code PLAYER_STAT} listener on {@code ABILITY_POWER_MULTIPLIER} squared the multiplier on
     * every ability.
     */
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

    /**
     * Gold Plating's settle pass. The reduction itself is a sub-stage of the shared final damage
     * stage, registered in {@link TenosGoldPlating}, because it is post-mitigation by design - it
     * must be ordered against Second Chance and the base mod's other reducers, which the
     * pre-mitigation pipeline cannot express. What is left for the ticker is billing the gold each
     * reduction books, batched so the inventory scan happens at most once a second. Settling once
     * more on deactivation is what stops a charm swap from stranding an unpaid debt.
     */
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

    /**
     * Deep Reserves is a transient vanilla attribute modifier rather than a gear attribute, because
     * the sheet asks for a multiplier and gear attributes contribute additively. The base mod
     * rewrites {@code MANA_MAX}'s <em>base</em> value every single tick, so a modifier is the only
     * safe place to put one, and the ticker is what keeps it correct across charm swaps and node
     * refunds without a scan of every online player.
     */
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
