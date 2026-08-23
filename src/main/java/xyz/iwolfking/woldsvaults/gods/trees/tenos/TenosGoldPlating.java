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
 * Gold Plating: a post-mitigation {@link FinalDamageStage} reduction billing
 * {@code cost_coefficient * log10(cost_offset + resisted)} gold. An unpayable debt is carried over.
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
