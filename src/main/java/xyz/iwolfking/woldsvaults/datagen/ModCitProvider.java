package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ModCitProvider implements DataProvider {

    private final DataGenerator generator;
    private final List<Type> types;

    public ModCitProvider(DataGenerator generator) {
        this.generator = generator;
        this.types = new ArrayList<>();
        this.types.add(weaponTypeSetterSpec());
    }

    private static Type weaponTypeSetterSpec() {
        Map<String, String> variants = new LinkedHashMap<>();
        variants.put("Anchor", "anchor");
        variants.put("Axe", "axe");
        variants.put("Battlestaff", "battlestaff");
        variants.put("Claw", "claw");
        variants.put("Claymore", "claymore");
        variants.put("Cutlass", "cutlass");
        variants.put("Dagger", "dagger");
        variants.put("Double-Axe", "double_axe");
        variants.put("Fist", "fist");
        variants.put("Hammer", "hammer");
        variants.put("Katana", "katana");
        variants.put("Pickaxe", "pickaxe");
        variants.put("Rapier", "rapier");
        variants.put("Spear", "spear");
        variants.put("Staff", "staff");
        variants.put("Sword", "sword");
        variants.put("Trident", "trident");
        variants.put("Twin-Blade", "twin_blade");
        variants.put("Wand", "wand");
        variants.put("Halberd", "halberd");
        variants.put("None", "blank");

        return new Type(
                "woldsvaults:weapon_type_setter",
                "weapon_type_setter",
                "modifier",
                "weapon_type_setter",
                "woldsvaults:item/weapon_type_setter/weapon_type_setter_",
                "blank",
                variants,
                stem -> "weapon_type_setter_" + stem,
                stem -> stem
        );
    }

    @Override
    public String getName() {
        return "CIT Provider";
    }

    @Override
    public void run(HashCache cache) throws IOException {
        for (Type type : types) {
            runSpec(type, cache);
        }
    }

    private void runSpec(Type type, HashCache cache) throws IOException {
        Path output = generator.getOutputFolder();

        writeModel(output, type.modelFileNameResolver.apply(type.baseTextureStem), type.textureBase, type.baseTextureStem, cache);

        for (Map.Entry<String, String> entry : type.variants.entrySet()) {
            String nbtValue = entry.getKey();
            String stem = entry.getValue();

            String modelName = type.modelFileNameResolver.apply(stem);
            String citFileName = type.citFileNameResolver.apply(stem);

            writeModel(output, modelName, type.textureBase, stem, cache);

            Path citDir = output.resolve("assets/minecraft/optifine/cit/" + type.citFolder);
            Files.createDirectories(citDir);
            String properties = "type=item\n"
                    + "items=" + type.itemId + "\n"
                    + "model=item/" + modelName + "\n"
                    + "nbt." + type.nbtKey + "=" + nbtValue + "\n";
            Path citPath = citDir.resolve(citFileName + ".properties");
            Files.write(citPath, properties.getBytes(StandardCharsets.UTF_8));
            cache.keep(citPath);
        }
    }

    private void writeModel(Path output, String modelName, String textureBase, String textureStem, HashCache cache) throws IOException {
        Path modelDir = output.resolve("assets/minecraft/models/item");
        Files.createDirectories(modelDir);
        String json = "{\n"
                + "    \"parent\": \"item/generated\",\n"
                + "    \"textures\": {\n"
                + "        \"layer0\": \"" + textureBase + textureStem + "\"\n"
                + "    }\n"
                + "}";
        Path modelPath = modelDir.resolve(modelName + ".json");
        Files.write(modelPath, json.getBytes(StandardCharsets.UTF_8));
        cache.keep(modelPath);
    }

    public static class Type {
        public final String itemId;
        public final String citFolder;
        public final String nbtKey;
        public final String modelPrefix;
        public final String textureBase;
        public final String baseTextureStem;
        public final Map<String, String> variants;
        public final Function<String, String> modelFileNameResolver;
        public final Function<String, String> citFileNameResolver;

        public Type(String itemId, String citFolder, String nbtKey, String modelPrefix,
                    String textureBase, String baseTextureStem, Map<String, String> variants,
                    Function<String, String> modelFileNameResolver, Function<String, String> citFileNameResolver) {
            this.itemId = itemId;
            this.citFolder = citFolder;
            this.nbtKey = nbtKey;
            this.modelPrefix = modelPrefix;
            this.textureBase = textureBase;
            this.baseTextureStem = baseTextureStem;
            this.variants = new LinkedHashMap<>(variants);
            this.modelFileNameResolver = modelFileNameResolver;
            this.citFileNameResolver = citFileNameResolver;
        }
    }
}
