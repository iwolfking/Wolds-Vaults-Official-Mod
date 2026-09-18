package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import iskallia.vault.init.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
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
           builder.add(ModItems.DECK_SOCKET.getRegistryName(), chanceStack(ModItems.SILVER_SCRAP, 0, 10, 1.0F), chanceStack(ModItems.BLACK_OPAL_GEM, 0, 4, 1.0F), chanceStack(ModItems.CARD_JUICE, 0, 6, 1.0F));
           builder.add(ModItems.MOD_BOX.getRegistryName(), chanceStack(ModItems.KNOWLEDGE_STAR_SHARD, 1, 4, 1.0F), chanceStack(Blocks.AIR, 0, 0, 0.0F), chanceStack(Blocks.AIR, 0, 0, 0.0F));
           builder.add(ModItems.CARD_DECK.getRegistryName(), chanceStack(ModItems.POG, 1, 1, 1.0F), chanceStack(ModItems.BLACK_OPAL_GEM, 4, 16, 1.0F), chanceStack(Blocks.AIR, 0, 0, 0.0F));
       });
    }

    public GsonChanceItemStack chanceStack(ItemLike item, int minCount, int maxCount, float chance) {
        return new GsonChanceItemStack(new ItemStack(item), minCount, maxCount, chance);
    }
}
