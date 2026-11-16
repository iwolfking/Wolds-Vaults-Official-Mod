package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.WoldsVaults;

/** Builds the ids of greed stage content: {@code woldsvaults:greed/stage_<n>/<path>}. */
public final class GreedStageIds {
    private GreedStageIds() {
    }

    public static ResourceLocation id(int stage, String path) {
        return WoldsVaults.id("greed/stage_" + stage + "/" + path);
    }

    /** The same id as a string, for references written inside palette and loot entries. */
    public static String ref(int stage, String path) {
        return id(stage, path).toString();
    }
}
