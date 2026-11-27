package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.util.calc.ItemQuantityHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Cash Hunter: applies the player's item quantity to coin pile loot at a reduced efficiency, by
 * duplicating generated stacks. The fractional part is resolved stochastically.
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
