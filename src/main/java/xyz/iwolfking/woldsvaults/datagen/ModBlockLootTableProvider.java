package xyz.iwolfking.woldsvaults.datagen;

import com.mojang.datafixers.util.Pair;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import xyz.iwolfking.woldsvaults.init.ModCompressibleBlocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;


public class ModBlockLootTableProvider extends LootTableProvider {

    public ModBlockLootTableProvider(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables() {
        return List.of(
                Pair.of(ModBlockLootTables::new, LootContextParamSets.BLOCK),
                Pair.of(CompressiumBlockLoot::new, LootContextParamSets.BLOCK)
        );
    }

    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext context) {
        map.forEach((id, table) -> {
            if (id.getNamespace().equals("woldsvaults")) {
                table.validate(context);
            }
        });
    }

    private static class CompressiumBlockLoot extends BlockLoot {

        @Override
        protected void addTables() {
            ModCompressibleBlocks.getRegisteredBlocks().forEach((k, v) -> v.forEach(e -> this.dropSelf(e.get())));
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            List<Block> blocks = new ArrayList<>();
            ModCompressibleBlocks.getRegisteredBlocks().forEach((k, v) -> v.forEach(e -> blocks.add(e.get())));
            return blocks;
        }

    }
}
