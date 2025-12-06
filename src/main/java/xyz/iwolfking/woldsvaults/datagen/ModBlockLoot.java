package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.init.ModItems;

import java.util.Map;
import java.util.stream.Collectors;

public class ModBlockLoot extends BlockLoot {
    @Override
    protected void addTables() {
//        dropSelf(ModBlocks.WUTODIE);
//        dropSelf(ModBlocks.VAULT_INFUSER_BLOCK);
//        dropSelf(ModBlocks.CHROMATIC_STEEL_INFUSER_BLOCK);
//        dropSelf(ModBlocks.DOLL_DISMANTLING_BLOCK);
//        dropSelf(ModBlocks.DECO_LODESTONE_BLOCK);
//        dropSelf(ModBlocks.SURVIVAL_MOB_BARRIER);
//        dropSelf(ModBlocks.DECO_MONOLITH_BLOCK);
//        dropSelf(ModBlocks.DECO_OBELISK_BLOCK);
//        dropSelf(ModBlocks.DECO_SCAVENGER_ALTAR_BLOCK);
//        dropSelf(ModBlocks.PRISMATIC_FIBER_BLOCK);
//        dropSelf(ModBlocks.WUTODIE_STAIRS);
//        dropSelf(ModBlocks.WUTODIE_SLAB);
//        dropSelf(ModBlocks.WUTODIE_WALL);
//        dropOther(ModBlocks.NULLITE_ORE, ModItems.NULLITE_FRAGMENT);

    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return ForgeRegistries.BLOCKS.getEntries().stream().filter(resourceKeyBlockEntry -> resourceKeyBlockEntry.getKey().getRegistryName().getNamespace().equals(WoldsVaults.MOD_ID) || resourceKeyBlockEntry.getKey().getRegistryName().getNamespace().equals(VaultMod.MOD_ID) ).map(Map.Entry::getValue).collect(Collectors.toList());
    }
}
