package xyz.iwolfking.woldsvaults.datagen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import iskallia.vault.init.ModBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.world.level.block.Block;
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

