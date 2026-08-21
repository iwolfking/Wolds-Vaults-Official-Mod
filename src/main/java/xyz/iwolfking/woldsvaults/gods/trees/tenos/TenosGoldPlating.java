package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.init.ModBlocks;
import iskallia.vault.util.CoinDefinition;
import iskallia.vault.util.InventoryUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.GodNodeState;
import xyz.iwolfking.woldsvaults.gods.combat.FinalDamageStage;

import java.util.List;
import java.util.concurrent.atomic.DoubleAdder;

/**
 * Gold Plating (r116): 75% of incoming damage is blocked, paid for with
 * {@code 2 * log10(10 + resisted)} gold per hit.
 *
 * <p>It is a damage reduction, so it sits in the shared post-mitigation {@link FinalDamageStage}
 * rather than on the pre-mitigation combat pipeline: that is what keeps it ordered against Second
 * Chance and the base mod's other reducers instead of racing them on event priority.
 *
 * <p>The charge is accumulated and settled at most once a second by
 * {@link TenosVaultHandlers.GoldPlatingHandler}, never per hit.
 * {@code CoinDefinition.extractCurrency} walks the player's whole inventory including coin pouches
 * and keyrings, so billing on every hit would be twenty full inventory scans a second per player.
 * Batching keeps the total cost exactly as specified while collapsing the scans into one.
 *
 * <p>A player who cannot pay keeps the debt: the resistance stays on and the outstanding gold is
 * taken as soon as they have it again, rather than the node flickering off mid-fight. The ledger
 * lives in the shared {@link GodNodeState} player scratch, so logout, vault-listener leave and
 * respec all clear it through the god core's own teardown.
 */
public final class TenosGoldPlating {
    private TenosGoldPlating() {
    }

    static void register() {
        FinalDamageStage.register(TenosNodes.key(TenosNodes.GOLD_PLATING), FinalDamageStage.ORDER_REDUCTION,
                (event, amount) -> {
                    if (!(event.getEntityLiving() instanceof ServerPlayer player)) {
                        return amount;
                    }
                    if (!TenosNodes.isActive(player, TenosNodes.GOLD_PLATING)) {
                        return amount;
                    }
                    TenosNodeHandlers.GoldPlatingParams params = TenosNodeHandlers.params(TenosNodes.GOLD_PLATING,
                            TenosNodeHandlers.GoldPlatingParams.class);
                    float reduced = amount * params.damage_multiplier();
                    float resisted = amount - reduced;
                    if (resisted > 0.0F) {
                        debt(player).add(params.cost_coefficient()
                                * (float) Math.log10(params.cost_offset() + resisted));
                    }
                    return reduced;
                });
    }

    /** Takes whatever whole gold the player owes and can pay, carrying the rest over. */
    static void settle(ServerPlayer player) {
        DoubleAdder owed = GodNodeState.<DoubleAdder>peek(player.getUUID(), TenosNodes.GOLD_PLATING).orElse(null);
        if (owed == null || owed.sum() < 1.0D) {
            return;
        }
        int gold = (int) Math.floor(owed.sum());
        List<InventoryUtil.ItemAccess> items = InventoryUtil.findAllItems(player);
        ItemStack price = new ItemStack(ModBlocks.VAULT_GOLD, gold);
        if (!CoinDefinition.hasEnoughCurrency(items, price)) {
            WoldsVaults.LOGGER.debug("Gold Plating could not charge {} {} gold; the debt is carried over.",
                    player.getGameProfile().getName(), gold);
            return;
        }
        CoinDefinition.extractCurrency(player, items, price);
        owed.add(-gold);
    }

    private static DoubleAdder debt(ServerPlayer player) {
        return GodNodeState.get(player.getUUID(), TenosNodes.GOLD_PLATING, DoubleAdder::new);
    }
}
