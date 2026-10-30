package xyz.iwolfking.woldsvaults.datagen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import iskallia.auxiliaryblocks.AuxiliaryBlocks;
import iskallia.vault.init.ModBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class RechiseledDataProvider implements DataProvider {

    private final DataGenerator generator;
    private final String modid;
    private final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    Map<String, ChiselingFileBuilder> builderMap = new HashMap<>();

    public RechiseledDataProvider(DataGenerator generator) {
        this.generator = generator;
        this.modid = WoldsVaults.MOD_ID;
    }

    @Override
    public void run(HashCache output) throws IOException {

        add(ModBlocks.VELVET_BLOCK, chiselingFileBuilder -> {
            chiselingFileBuilder.add(ModBlocks.VELVET_BLOCK);
            chiselingFileBuilder.add(ModBlocks.VELVET_BLOCK_CHISELED);
            chiselingFileBuilder.add(ModBlocks.VELVET_BLOCK_STRIPS);
        });


        add(ModBlocks.ORNATE_BLOCK, chiselingFileBuilder -> {
            chiselingFileBuilder.add(ModBlocks.ORNATE_BLOCK);
            chiselingFileBuilder.add(ModBlocks.ORNATE_BLOCK_CHISELED);
            chiselingFileBuilder.add(ModBlocks.ORNATE_BLOCK_PILLAR);
            chiselingFileBuilder.add(ModBlocks.ORNATE_BLOCK_TILED);
            chiselingFileBuilder.add(ModBlocks.ORNATE_BRICKS);
            chiselingFileBuilder.add(ModBlocks.ORNATE_BRICKS_CHIPPED);
            chiselingFileBuilder.add(ModBlocks.ORNATE_BRICKS_CRACKED);
            chiselingFileBuilder.add(ModBlocks.ORNATE_BRICKS_RUSTY);
        });

        add(ModBlocks.ORNATE_BLOCK_VELVET, chiselingFileBuilder -> {
            chiselingFileBuilder.add(ModBlocks.ORNATE_BLOCK_VELVET);
            chiselingFileBuilder.add(ModBlocks.ORNATE_BLOCK_VELVET_CHISELED);
            chiselingFileBuilder.add(ModBlocks.ORNATE_BLOCK_VELVET_PILLAR);
        });

        add(ModBlocks.GILDED_BLOCK, chiselingFileBuilder -> {
            chiselingFileBuilder.add(ModBlocks.GILDED_BLOCK);
            chiselingFileBuilder.add(ModBlocks.GILDED_BLOCK_BUMBO);
            chiselingFileBuilder.add(ModBlocks.GILDED_BLOCK_CHISELED);
            chiselingFileBuilder.add(ModBlocks.GILDED_BLOCK_PILLAR);
            chiselingFileBuilder.add(ModBlocks.GILDED_BRICKS);
            chiselingFileBuilder.add(ModBlocks.GILDED_BRICKS_DULL);
            chiselingFileBuilder.add(ModBlocks.GILDED_BRICKS_CRACKED);
            chiselingFileBuilder.add(ModBlocks.GILDED_BRICKS_CRACKED_DULL);
            chiselingFileBuilder.add(ModBlocks.GILDED_COBBLE);
        });

        add(ModBlocks.VAULT_STONE, chiselingFileBuilder -> {
            chiselingFileBuilder.add(ModBlocks.VAULT_STONE);
            chiselingFileBuilder.add(ModBlocks.VAULT_COBBLESTONE);
            chiselingFileBuilder.add(ModBlocks.CHISELED_VAULT_STONE);
            chiselingFileBuilder.add(ModBlocks.POLISHED_VAULT_STONE);
            chiselingFileBuilder.add(ModBlocks.VAULT_STONE_BRICKS);
            chiselingFileBuilder.add(ModBlocks.VAULT_STONE_BRICKS_CRACKED);
            chiselingFileBuilder.add(ModBlocks.VAULT_STONE_PILLAR);
            chiselingFileBuilder.add(ModBlocks.BUMBO_POLISHED_VAULT_STONE);
        });

        add(ModBlocks.ANCIENT_COPPER_BLOCK, chiselingFileBuilder -> {
            chiselingFileBuilder.add(ModBlocks.ANCIENT_COPPER_BLOCK);
            chiselingFileBuilder.add(ModBlocks.ANCIENT_COPPER_BRICKS);
            chiselingFileBuilder.add(ModBlocks.ANCIENT_COPPER_SMALL_BRICKS);
            chiselingFileBuilder.add(ModBlocks.ANCIENT_COPPER_BLOCK_PILLAR);
        });


        add(ModBlocks.ANCIENT_COPPER_BLOCK_EXPOSED, chiselingFileBuilder -> {
            chiselingFileBuilder.add(ModBlocks.ANCIENT_COPPER_BLOCK_EXPOSED);
            chiselingFileBuilder.add(ModBlocks.ANCIENT_COPPER_BRICKS_EXPOSED);
            chiselingFileBuilder.add(ModBlocks.ANCIENT_COPPER_SMALL_BRICKS_EXPOSED);
            chiselingFileBuilder.add(ModBlocks.ANCIENT_COPPER_BLOCK_PILLAR_EXPOSED);
        });

        add(ModBlocks.ANCIENT_COPPER_BLOCK_WEATHERED, chiselingFileBuilder -> {
            chiselingFileBuilder.add(ModBlocks.ANCIENT_COPPER_BLOCK_WEATHERED);
            chiselingFileBuilder.add(ModBlocks.ANCIENT_COPPER_BRICKS_WEATHERED);
            chiselingFileBuilder.add(ModBlocks.ANCIENT_COPPER_SMALL_BRICKS_WEATHERED);
            chiselingFileBuilder.add(ModBlocks.ANCIENT_COPPER_BLOCK_PILLAR_WEATHERED);
        });

        add(ModBlocks.ANCIENT_COPPER_BLOCK_OXIDIZED, chiselingFileBuilder -> {
            chiselingFileBuilder.add(ModBlocks.ANCIENT_COPPER_BLOCK_OXIDIZED);
            chiselingFileBuilder.add(ModBlocks.ANCIENT_COPPER_BRICKS_OXIDIZED);
            chiselingFileBuilder.add(ModBlocks.ANCIENT_COPPER_SMALL_BRICKS_OXIDIZED);
            chiselingFileBuilder.add(ModBlocks.ANCIENT_COPPER_BLOCK_PILLAR_OXIDIZED);
        });

        add(ModBlocks.IDONA_BRICK, chiselingFileBuilder -> {
            chiselingFileBuilder.add(ModBlocks.IDONA_BRICK);
            chiselingFileBuilder.add(ModBlocks.IDONA_CHISELED_BRICK);
            chiselingFileBuilder.add(ModBlocks.IDONA_DARK_SMOOTH_BLOCK);
            chiselingFileBuilder.add(ModBlocks.IDONA_LIGHT_SMOOTH_BLOCK);
            chiselingFileBuilder.add(ModBlocks.IDONA_GEM_BLOCK);
        });

        add(ModBlocks.TENOS_BRICK, chiselingFileBuilder -> {
            chiselingFileBuilder.add(ModBlocks.TENOS_BRICK);
            chiselingFileBuilder.add(ModBlocks.TENOS_CHISELED_BRICK);
            chiselingFileBuilder.add(ModBlocks.TENOS_DARK_SMOOTH_BLOCK);
            chiselingFileBuilder.add(ModBlocks.TENOS_LIGHT_SMOOTH_BLOCK);
            chiselingFileBuilder.add(ModBlocks.TENOS_GEM_BLOCK);
        });

        add(ModBlocks.VELARA_BRICK, chiselingFileBuilder -> {
            chiselingFileBuilder.add(ModBlocks.VELARA_BRICK);
            chiselingFileBuilder.add(ModBlocks.VELARA_CHISELED_BRICK);
            chiselingFileBuilder.add(ModBlocks.VELARA_DARK_SMOOTH_BLOCK);
            chiselingFileBuilder.add(ModBlocks.VELARA_LIGHT_SMOOTH_BLOCK);
            chiselingFileBuilder.add(ModBlocks.VELARA_GEM_BLOCK);
        });

        add(ModBlocks.WENDARR_BRICK, chiselingFileBuilder -> {
            chiselingFileBuilder.add(ModBlocks.WENDARR_BRICK);
            chiselingFileBuilder.add(ModBlocks.WENDARR_CHISELED_BRICK);
            chiselingFileBuilder.add(ModBlocks.WENDARR_BRICK);
            chiselingFileBuilder.add(ModBlocks.WENDARR_LIGHT_SMOOTH_BLOCK);
            chiselingFileBuilder.add(ModBlocks.WENDARR_GEM_BLOCK);
        });

        add(ModBlocks.LIVING_ROCK_BLOCK_COBBLE, chiselingFileBuilder -> {
            chiselingFileBuilder.add(ModBlocks.LIVING_ROCK_BLOCK_COBBLE);
            chiselingFileBuilder.add(ModBlocks.LIVING_ROCK_BLOCK_POLISHED);
            chiselingFileBuilder.add(ModBlocks.LIVING_ROCK_BLOCK_STACKED);
            chiselingFileBuilder.add(ModBlocks.LIVING_ROCK_BRICKS);
            chiselingFileBuilder.add(ModBlocks.MOSSY_LIVING_ROCK_BLOCK_COBBLE);
            chiselingFileBuilder.add(ModBlocks.MOSSY_LIVING_ROCK_BLOCK_POLISHED);
            chiselingFileBuilder.add(ModBlocks.MOSSY_LIVING_ROCK_BLOCK_STACKED);
            chiselingFileBuilder.add(ModBlocks.MOSSY_LIVING_ROCK_BRICKS);
        });

        add(ModBlocks.SANDY_BLOCK, chiselingFileBuilder -> {
            chiselingFileBuilder.add(ModBlocks.SANDY_BLOCK);
            chiselingFileBuilder.add(ModBlocks.SANDY_BLOCK_BUMBO);
            chiselingFileBuilder.add(ModBlocks.SANDY_BLOCK_CHISELED);
            chiselingFileBuilder.add(ModBlocks.SANDY_BLOCK_POLISHED);
            chiselingFileBuilder.add(ModBlocks.SANDY_BRICKS);
            chiselingFileBuilder.add(ModBlocks.SANDY_BRICKS_CRACKED);
            chiselingFileBuilder.add(ModBlocks.SANDY_SMALL_BRICKS);
            chiselingFileBuilder.add(ModBlocks.SANDY_SMALL_BRICKS_CRACKED);
        });

        add(iskallia.auxiliaryblocks.init.ModBlocks.COBBLED_SANDSTONE.get(), chiselingFileBuilder -> {
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.ROCKY_SAND.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.MOSSY_STONE.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.ROCKY_DIRT.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.ROCKY_STONE.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.DIRTY_SAND.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.MOSSY_DIRT.get());
            chiselingFileBuilder.add(Blocks.SANDSTONE);
        });

        add(iskallia.auxiliaryblocks.init.ModBlocks.COBBLED_SANDSTONE.get(), chiselingFileBuilder -> {
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.ROCKY_SAND.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.MOSSY_STONE.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.ROCKY_DIRT.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.ROCKY_STONE.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.DIRTY_SAND.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.MOSSY_DIRT.get());
            chiselingFileBuilder.add(Blocks.SANDSTONE);
        });

        add(Blocks.DIORITE, chiselingFileBuilder -> {
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.DARK_PURPLE_DIORITE.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.PURPLE_DIORITE.get());
        });

        add(iskallia.auxiliaryblocks.init.ModBlocks.LAB_LIGHT_GRAY.get(), chiselingFileBuilder -> {
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.LAB_BLUE.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.LAB_CHROME.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.LAB_CYAN.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.LAB_GRAY.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.LAB_DARK_GRAY.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.LAB_LIGHT_METALLIC.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.LAB_METALLIC.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.LAB_RED.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.LAB_YELLOW.get());
        });

        add(iskallia.auxiliaryblocks.init.ModBlocks.CONCRETE.get(), chiselingFileBuilder -> {
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.CONCRETE_VENT.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.NAILED_REINFORCED_CONCRETE.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.PLATED_CONCRETE.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.REINFORCED_CONCRETE.get());
            chiselingFileBuilder.add(Blocks.GRAY_CONCRETE);
        });

        add(iskallia.auxiliaryblocks.init.ModBlocks.GRAY_PASTEL.get(), chiselingFileBuilder -> {
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.BLUE_PASTEL.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.GREEN_PASTEL.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.YELLOW_PASTEL.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.PINK_PASTEL.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.ORANGE_PASTEL.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.PURPLE_PASTEL.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.SMOOTH_BLUE_PASTEL.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.SMOOTH_GRAY_PASTEL.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.SMOOTH_GREEN_PASTEL.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.SMOOTH_ORANGE_PASTEL.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.SMOOTH_PINK_PASTEL.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.SMOOTH_PURPLE_PASTEL.get());
        });

        add(iskallia.auxiliaryblocks.init.ModBlocks.PLASTIC.get(), chiselingFileBuilder -> {
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.CYAN_PLASTIC.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.DARK_GRAY_PLASTIC.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.GREEN_PLASTIC.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.RED_PLASTIC.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.ORANGE_PLASTIC.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.PURPLE_PLASTIC.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.BLUE_PLASTIC.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.LIGHT_GRAY_PLASTIC.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.DARK_GRAY_PLASTIC.get());
        });

        add(iskallia.auxiliaryblocks.init.ModBlocks.ASPHALT.get(), chiselingFileBuilder -> {
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.ASPHALT_CAUTION.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.ASPHALT_LINE_CENTER.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.ASPHALT_LINE_EDGE.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.DARK_ASPHALT.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.DARK_ASPHALT_CAUTION.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.DARK_ASPHALT_LINE_CENTER.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.DARK_ASPHALT_LINE_EDGE.get());
        });

        add(iskallia.auxiliaryblocks.init.ModBlocks.ORANGE_GELATIN.get(), chiselingFileBuilder -> {
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.CYAN_GELATIN.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.LIME_GELATIN.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.RED_GELATIN.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.BLUE_GELATIN.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.MAGENTA_GELATIN.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.PURPLE_GELATIN.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.YELLOW_GELATIN.get());
        });

        add(iskallia.auxiliaryblocks.init.ModBlocks.BLACK_CRYSTAL.get(), chiselingFileBuilder -> {
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.BLUE_CRYSTAL.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.GREEN_CRYSTAL.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.INDIGO_CRYSTAL.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.ORANGE_CRYSTAL.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.RED_CRYSTAL.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.VIOLET_CRYSTAL.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.WHITE_CRYSTAL.get());
            chiselingFileBuilder.add(iskallia.auxiliaryblocks.init.ModBlocks.YELLOW_CRYSTAL.get());
        });


        builderMap.forEach((s, chiselingFileBuilder) -> {

            Path path = this.generator.getOutputFolder().resolve(
                    "data/rechiseled/chiseling_recipes/" + s + ".json"
            );

            try {
                DataProvider.save(GSON, output, chiselingFileBuilder.build(), path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

    }

    private void add(Block baseBlock, Consumer<ChiselingFileBuilder> builderConsumer) {
        ChiselingFileBuilder builder = new ChiselingFileBuilder(baseBlock.getRegistryName().getPath());
        builderConsumer.accept(builder);
        builderMap.put(baseBlock.getRegistryName().getPath(), builder);
    }

    @Override
    public String getName() {
        return modid + " Chiseling Entries";
    }


    public static class ChiselingFileBuilder {

        private final String name;
        private final List<String> items = new ArrayList<>();
        private boolean overwrite = false;

        public ChiselingFileBuilder(String name) {
            this.name = name;
        }

        public ChiselingFileBuilder add(String itemId) {
            this.items.add(itemId);
            return this;
        }

        public ChiselingFileBuilder add(Block block) {
            this.items.add(block.getRegistryName().toString());
            return this;
        }

        public ChiselingFileBuilder overwrite(boolean overwrite) {
            this.overwrite = overwrite;
            return this;
        }

        public String getName() {
            return this.name;
        }

        public JsonObject build() {
            JsonObject root = new JsonObject();
            root.addProperty("type", "rechiseled:chiseling");

            JsonArray entries = new JsonArray();
            for (String id : items) {
                JsonObject entry = new JsonObject();
                entry.addProperty("item", id);
                entries.add(entry);
            }

            root.add("entries", entries);
            root.addProperty("overwrite", overwrite);

            return root;
        }
    }
}

