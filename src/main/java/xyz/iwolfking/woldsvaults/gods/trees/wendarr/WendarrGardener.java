package xyz.iwolfking.woldsvaults.gods.trees.wendarr;

import iskallia.vault.VaultMod;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.item.ItemVaultFruit;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

import java.util.ArrayList;
import java.util.List;

/** Gardener: Woldian Gardens drop extra fruit and upgrade fruit to starfruit, on the POST phase. */
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
        Item starFruit = ForgeRegistries.ITEMS.getValue(VaultMod.id("star_fruit"));
        if (starFruit == null) {
            WoldsVaults.LOGGER.error("Gardener could not resolve the_vault:star_fruit; only the extra-fruit half will apply.");
        }
        List<ItemStack> extra = new ArrayList<>();
        for (ItemStack stack : loot) {
            if (!(stack.getItem() instanceof ItemVaultFruit)) {
                continue;
            }
            if (starFruit != null && stack.getItem() != starFruit
                    && player.getRandom().nextFloat() < params.starfruit_upgrade_chance()) {
                ItemStack upgraded = new ItemStack(starFruit, stack.getCount());
                stack.setCount(0);
                extra.add(upgraded);
                continue;
            }
            if (player.getRandom().nextFloat() < params.extra_fruit_chance()) {
                extra.add(stack.copy());
            }
        }
        loot.removeIf(ItemStack::isEmpty);
        loot.addAll(extra);
    }
}
