package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.util.calc.ItemQuantityHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Cash Hunter (r117): item quantity applies to coin piles at 25% efficiency.
 *
 * <p>The coin pile builds its generator with item quantity hardcoded to zero, so the bonus cannot
 * be fed in through the generator; it is applied to the generated stacks on the POST phase
 * instead, with the fractional part resolved stochastically so small bonuses are not rounded away.
 *
 * <p><b>The item rarity half is not implemented.</b> Coin piles use {@code LootTableGenerator},
 * which has no rarity field and no sub-pool reweighting of any kind - rarity is structurally
 * absent, not merely disabled. Making it work means swapping coin piles onto
 * {@code TieredLootTableGenerator}, which changes coin loot for every player, not just node
 * holders. That is a design decision, so only the quantity half ships.
 */
public final class TenosCoinPiles {
    private static final Object OWNER = new Object();

    private TenosCoinPiles() {
    }

    static void register() {
        CommonEvents.COIN_STACK_LOOT_GENERATION.post().register(OWNER, data -> {
            ServerPlayer player = data.getPlayer();
            if (player == null || !TenosNodes.isActive(player, TenosNodes.CASH_HUNTER)) {
                return;
            }
            float bonus = ItemQuantityHelper.getItemQuantity(player) * TenosNodeHandlers.params(
                    TenosNodes.CASH_HUNTER, TenosNodeHandlers.CashHunterParams.class).efficiency();
            if (bonus <= 0.0F) {
                return;
            }
            List<ItemStack> extra = new ArrayList<>();
            for (ItemStack stack : data.getLoot()) {
                if (stack.isEmpty()) {
                    continue;
                }
                float remaining = bonus;
                while (remaining > 0.0F && player.getRandom().nextFloat() < remaining) {
                    extra.add(stack.copy());
                    remaining -= 1.0F;
                }
            }
            data.getLoot().addAll(extra);
        });
    }
}
