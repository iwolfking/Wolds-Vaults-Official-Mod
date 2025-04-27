package xyz.iwolfking.woldsvaults.config.fake;

import iskallia.vault.config.VaultRecyclerConfig;
import iskallia.vault.config.entry.ChanceItemStackEntry;
import iskallia.vault.config.entry.ConditionalChanceItemStackEntry;
import iskallia.vault.init.ModItems;
import net.minecraft.world.item.Items;

public class CatalystVaultRecylerConfig {
    public static VaultRecyclerConfig.RecyclerOutput CATALYST_VAULT_RECYCLER_OUTPUT = new VaultRecyclerConfig.RecyclerOutput(
            new ChanceItemStackEntry(ModItems.VAULT_CATALYST_FRAGMENT.getDefaultInstance(), 0, 9, 1.0F),
            new ConditionalChanceItemStackEntry(Items.AIR.getDefaultInstance(), 1, 1, 0),
            new ConditionalChanceItemStackEntry(Items.AIR.getDefaultInstance(), 1, 1, 0));
}
