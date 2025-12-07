package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.loot.BlockLoot;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import java.util.stream.Collectors;

//TODO: Add all of the blocks
public class ModBlockLootTables extends BlockLoot {
    @Override
    protected void addTables() {
        dropSelf(ModBlocks.CARBON_BLOCK);
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return ForgeRegistries.BLOCKS.getValues().stream()
                .filter(b -> b.getRegistryName() != null)
                .filter(b -> b.getRegistryName().equals(WoldsVaults.id("carbon_block")))
                .filter(b -> b.getRegistryName().getNamespace().equals(WoldsVaults.MOD_ID))
                .collect(Collectors.toList());
    }
}
