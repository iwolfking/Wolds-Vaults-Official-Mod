package xyz.iwolfking.woldsvaults.gods.trees.wendarr;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.ItemVaultFruit;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

import java.util.ArrayList;
import java.util.List;

/**
 * Gardener: each orange from a Woldian Garden may become a starfruit, then every fruit stack may double,
 * on the POST phase.
 */
public final class WendarrGardener {
    private static final Object OWNER = new Object();

    private WendarrGardener() {
    }

    static void register() {
        CommonEvents.LOOTABLE_BLOCK_GENERATION_EVENT.post().register(OWNER, data -> {
            ServerPlayer player = data.getPlayer();
            if (player == null || !data.getState().is(ModBlocks.ISKALLIAN_LEAVES_BLOCK)) {
                return;
            }
            if (!WendarrNodes.isActive(player, WendarrNodes.GARDENER)) {
                return;
            }
            enrich(player, data.getLoot());
        });
    }

    private static void enrich(ServerPlayer player, List<ItemStack> loot) {
        WendarrNodeHandlers.GardenerParams params = WendarrNodeHandlers.params(WendarrNodes.GARDENER,
                WendarrNodeHandlers.GardenerParams.class);
        List<ItemStack> starFruit = new ArrayList<>();
        for (ItemStack stack : loot) {
            if (stack.getItem() != ModItems.SOUR_ORANGE) {
                continue;
            }
            int upgraded = 0;
            for (int i = 0; i < stack.getCount(); i++) {
                if (player.getRandom().nextFloat() < params.starfruit_upgrade_chance()) {
                    upgraded++;
                }
            }
            if (upgraded > 0) {
                stack.shrink(upgraded);
                starFruit.add(new ItemStack(ModItems.STAR_FRUIT, upgraded));
            }
        }
        loot.removeIf(ItemStack::isEmpty);
        loot.addAll(starFruit);
        List<ItemStack> doubled = new ArrayList<>();
        for (ItemStack stack : loot) {
            if (stack.getItem() instanceof ItemVaultFruit && player.getRandom().nextFloat() < params.extra_fruit_chance()) {
                doubled.add(stack.copy());
            }
        }
        loot.addAll(doubled);
    }
}
