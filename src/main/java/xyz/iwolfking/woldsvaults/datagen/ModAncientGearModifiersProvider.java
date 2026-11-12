package xyz.iwolfking.woldsvaults.datagen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import org.jetbrains.annotations.NotNull;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.datagen.lib.AncientGearModifiers;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/** Writes the per-unique ancient gear modifier configs from {@link AncientGearModifiers} through Gson. */
public class ModAncientGearModifiersProvider implements DataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final DataGenerator generator;

    public ModAncientGearModifiersProvider(DataGenerator generator) {
        this.generator = generator;
    }

    @Override
    public void run(@NotNull HashCache cache) throws IOException {
        Path root = this.generator.getOutputFolder()
                .resolve("data").resolve(WoldsVaults.MOD_ID)
                .resolve("vault_configs").resolve("gear").resolve("gear_modifiers");
        for (Map.Entry<String, JsonObject> entry : AncientGearModifiers.build().entrySet()) {
            DataProvider.save(GSON, cache, entry.getValue(), root.resolve(entry.getKey() + ".json"));
        }
    }

    @Override
    public @NotNull String getName() {
        return "Wold's Vaults Ancient Gear Modifiers";
    }
}
