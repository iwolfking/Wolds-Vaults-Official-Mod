package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import iskallia.vault.init.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import xyz.iwolfking.vhapi.api.datagen.AbstractVaultRecyclerProvider;
import xyz.iwolfking.vhapi.api.loaders.workstation.lib.GsonChanceItemStack;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ModVaultRecyclerProvider extends AbstractVaultRecyclerProvider {
    protected ModVaultRecyclerProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
       add("new_recycleables", builder -> {
           builder.add(VaultMod.id("deck_socket"), chanceStack(ModItems.SILVER_SCRAP, 6, 12, 0.8F), chanceStack(ModItems.BLACK_OPAL_GEM, 1, 1, 0.2F), chanceStack(ModItems.CARD_JUICE, 1, 1, 0.2F));
       });
    }

    public GsonChanceItemStack chanceStack(ItemLike item, int minCount, int maxCount, float chance) {
        return new GsonChanceItemStack(new ItemStack(item), minCount, maxCount, chance);
    }
}
