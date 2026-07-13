package xyz.iwolfking.woldsvaults.datagen;

import com.github.klikli_dev.occultism.registry.OccultismBlocks;
import com.github.klikli_dev.occultism.registry.OccultismTags;
import com.google.gson.*;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ModPentacleProvider implements DataProvider {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
    private static final Logger LOGGER = LogManager.getLogger();
    private final Map<String, JsonElement> toSerialize = new HashMap();
    private final DataGenerator generator;

    public ModPentacleProvider(DataGenerator generator) {
        this.generator = generator;
    }

    public void run(HashCache cache) throws IOException {
        Path folder = this.generator.getOutputFolder();
        this.start();
        this.toSerialize.forEach((name, json) -> {
            Path path = folder.resolve("data/occultism/modonomicon/multiblocks/" + name + ".json");

            try {
                DataProvider.save(GSON, cache, json, path);
            } catch (IOException e) {
                LOGGER.error("Couldn't save pentacle {}", path, e);
            }

        });
    }

    private void start() {
        this.addPentacle("velara",
                this.createPattern(
                        "    WSW    ",
                        "   W V W   ",
                        "  WFFWFFW  ",
                        " W WFWFW W ",
                        "W   WWW   W",
                        "S PPP0GGG S",
                        "W    R    W",
                        " W   R   W ",
                        "  W  R  W  ",
                        "   W   W   ",
                        "    WSW    "), (new ModPentacleProvider.MappingBuilder()).bowl().redChalk().goldChalk().purpleChalk().whiteChalk().flower().crystal().velaraAltar().build());

        this.addPentacle("idona",
                this.createPattern(
                        "    WSW    ",
                        "   WNINW   ",
                        "  WZRRRZW  ",
                        " W RRRRR W ",
                        "W    R    W",
                        "S GGG0PPP S",
                        "W   WWW   W",
                        " W   W   W ",
                        "  W  W  W  ",
                        "   W   W   ",
                        "    WSW    "
                        ), (new MappingBuilder()).bowl().redChalk().goldChalk().purpleChalk().whiteChalk().skeleton().wither().crystal().idonaALtar().build());

        this.addPentacle("wendarr",
                this.createPattern(
                        "    WSW    ",
                        "   WX^XW   ",
                        "  WG G GW  ",
                        " W WGGGR W ",
                        "W    G    W",
                        "S WWW0RRR S",
                        "W    P    W",
                        " W   P   W ",
                        "  W  P  W  ",
                        "   W   W   ",
                        "    WSW    "
                ), (new MappingBuilder()).bowl().redChalk().goldChalk().purpleChalk().whiteChalk().clock().crystal().wendarrAltar().build());

        this.addPentacle("tenos",
                this.createPattern(
                        "    WSW    ",
                        "   WP&@W   ",
                        "  W@ P PW  ",
                        " W RPPPW W ",
                        "W    P    W",
                        "S RRR0WWW S",
                        "W    G    W",
                        " W   G   W ",
                        "  W  G  W  ",
                        "   W   W   ",
                        "    WSW    "
                ), (new ModPentacleProvider.MappingBuilder()).bowl().redChalk().goldChalk().purpleChalk().whiteChalk().crystal().bookshelf().tenosAltar().build());


        this.addPentacle("god_alignment",
                this.createPattern(
                        "    WSW    ",
                        "   W V W   ",
                        "  WFFWFFW  ",
                        " WPWFWFWGW ",
                        "W@ PWWWG XW",
                        "S&PPP0GGG^S",
                        "WP P R G XW",
                        " W@RRRRRGW ",
                        "  WZRRRZW  ",
                        "   WNINW   ",
                        "    WSW    "), (new ModPentacleProvider.MappingBuilder()).bowl().redChalk().goldChalk().purpleChalk().whiteChalk().clock().crystal().flower().skeleton().wither().bookshelf().velaraAltar().idonaALtar().wendarrAltar().tenosAltar().build());
    }

    private List<String> createPattern(String... rows) {
        List<String> pattern = new ArrayList<>();

        for(String row : rows) {
            pattern.add(row.replace(" ", "_"));
        }

        return pattern;
    }

    private void addPentacle(String name, List<String> pattern, Map<Character, JsonElement> mappings) {
        this.addPentacle(ResourceLocation.fromNamespaceAndPath(WoldsVaults.MOD_ID, name), pattern, mappings);
    }

    private void addPentacle(ResourceLocation rl, List<String> pattern, Map<Character, JsonElement> mappings) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "modonomicon:dense");
        JsonArray outerPattern = new JsonArray();
        JsonArray innerPattern = new JsonArray();

        for(String row : pattern) {
            innerPattern.add(row);
        }

        outerPattern.add(innerPattern);
        JsonArray ground = new JsonArray();

        for(int i = 0; i < pattern.size(); ++i) {
            String row = "";

            for(int j = 0; j < pattern.get(i).length(); ++j) {
                if ((i + j) % 2 == 0) {
                    row = row + "*";
                } else {
                    row = row + "+";
                }
            }

            ground.add(row);
        }

        outerPattern.add(ground);
        json.add("pattern", outerPattern);
        JsonObject jsonMapping = new JsonObject();

        for(Map.Entry<Character, JsonElement> entry : mappings.entrySet()) {
            jsonMapping.add(String.valueOf(entry.getKey()), entry.getValue());
        }

        json.add("mapping", jsonMapping);
        this.toSerialize.put(rl.getPath(), json);
    }

    public String getName() {
        return "Pentacles: Wold's Vaults";
    }

    private static class MappingBuilder {
        private final Map<Character, JsonElement> mappings = new HashMap();

        public MappingBuilder() {
            this.ground();
        }

        private ModPentacleProvider.MappingBuilder element(char c, JsonElement e) {
            this.mappings.put(c, e);
            return this;
        }

        private Map<Character, JsonElement> build() {
            return this.mappings;
        }

        private ModPentacleProvider.MappingBuilder block(char c, Supplier<? extends Block> b) {
            JsonObject json = new JsonObject();
            json.addProperty("type", "modonomicon:block");
            json.addProperty("block", ForgeRegistries.BLOCKS.getKey((Block) b.get()).toString());
            return this.element(c, json);
        }

        private ModPentacleProvider.MappingBuilder blockDisplay(char c, Supplier<? extends Block> b, Supplier<? extends Block> display) {
            JsonObject json = new JsonObject();
            json.addProperty("type", "modonomicon:block");
            json.addProperty("block", ForgeRegistries.BLOCKS.getKey((Block) b.get()).toString());
            json.addProperty("display", ForgeRegistries.BLOCKS.getKey((Block) display.get()).toString());
            return this.element(c, json);
        }

        private ModPentacleProvider.MappingBuilder display(char c, Supplier<? extends Block> display) {
            JsonObject json = new JsonObject();
            json.addProperty("type", "modonomicon:display");
            json.addProperty("display", ForgeRegistries.BLOCKS.getKey((Block) display.get()).toString());
            return this.element(c, json);
        }

        private ModPentacleProvider.MappingBuilder tag(char c, TagKey<Block> tag) {
            JsonObject json = new JsonObject();
            json.addProperty("type", "modonomicon:tag");
            json.addProperty("tag", "#" + tag.location());
            return this.element(c, json);
        }

        private ModPentacleProvider.MappingBuilder bowl() {
            return this.block('0', OccultismBlocks.GOLDEN_SACRIFICIAL_BOWL);
        }

        private ModPentacleProvider.MappingBuilder candle() {
            return this.tag('C', OccultismTags.CANDLES);
        }

        private ModPentacleProvider.MappingBuilder flower() {
            return this.tag('F', BlockTags.SMALL_FLOWERS);
        }

        private ModPentacleProvider.MappingBuilder bookPile() {
            return this.block('B', ModRegistry.BOOK_PILE);
        }

        private ModPentacleProvider.MappingBuilder clock() {
            return this.block('X', ModRegistry.CLOCK_BLOCK);
        }

        private ModPentacleProvider.MappingBuilder whiteChalk() {
            return this.block('W', OccultismBlocks.CHALK_GLYPH_WHITE);
        }

        private ModPentacleProvider.MappingBuilder goldChalk() {
            return this.block('G', OccultismBlocks.CHALK_GLYPH_GOLD);
        }

        private ModPentacleProvider.MappingBuilder purpleChalk() {
            return this.block('P', OccultismBlocks.CHALK_GLYPH_PURPLE);
        }

        private ModPentacleProvider.MappingBuilder redChalk() {
            return this.block('R', OccultismBlocks.CHALK_GLYPH_RED);
        }

        private ModPentacleProvider.MappingBuilder crystal() {
            return this.block('S', OccultismBlocks.SPIRIT_ATTUNED_CRYSTAL);
        }

        private ModPentacleProvider.MappingBuilder skeleton() {
            return this.blockDisplay('Z', () -> Blocks.SKELETON_SKULL, OccultismBlocks.SKELETON_SKULL_DUMMY);
        }

        private ModPentacleProvider.MappingBuilder ground() {
            return this.display('*', OccultismBlocks.OTHERSTONE).display('+', () -> Blocks.STONE);
        }

        private ModPentacleProvider.MappingBuilder wither() {
            return this.blockDisplay('N', () -> Blocks.WITHER_SKELETON_SKULL, OccultismBlocks.WITHER_SKELETON_SKULL_DUMMY);
        }

        private ModPentacleProvider.MappingBuilder velaraAltar() {
            return this.block('V', () -> ModBlocks.DECO_VELARA_ALTAR_BLOCK);
        }

        private ModPentacleProvider.MappingBuilder idonaALtar() {
            return this.block('I', () -> ModBlocks.DECO_VELARA_ALTAR_BLOCK);
        }

        private ModPentacleProvider.MappingBuilder wendarrAltar() {
            return this.block('^', () -> ModBlocks.DECO_WENDARR_ALTAR_BLOCK);
        }

        private ModPentacleProvider.MappingBuilder tenosAltar() {
            return this.block('&', () -> ModBlocks.DECO_TENOS_ALTAR_BLOCK);
        }

        private ModPentacleProvider.MappingBuilder bookshelf() {
            return this.block('@', () -> Blocks.BOOKSHELF);
        }

    }
}
