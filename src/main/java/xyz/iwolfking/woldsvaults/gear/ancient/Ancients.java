package xyz.iwolfking.woldsvaults.gear.ancient;

import iskallia.vault.gear.data.GearDataCache;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModConfigs;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.config.AncientsConfig;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The installed ancients table: whether gear is ancient, which uniques can be, what they are called, and the numbers
 * of the ancient-only effects. Until the table is installed nothing is ancient-capable and the effects are off.
 */
public final class Ancients {
    private static volatile AncientsConfig table;
    private static volatile Map<ResourceLocation, String> names = Collections.emptyMap();

    private Ancients() {
    }

    /** Installs a validated table, or clears the installed one when given null; called by the table installer. */
    public static void load(@Nullable AncientsConfig config) {
        if (config == null) {
            names = Collections.emptyMap();
            table = null;
            return;
        }
        Map<ResourceLocation, String> parsed = new LinkedHashMap<>();
        config.names.forEach((key, name) -> parsed.put(new ResourceLocation(key), name));
        names = Collections.unmodifiableMap(parsed);
        table = config;
    }

    /** The installed table, or null (logged once) when it is missing or was rejected. */
    @Nullable
    public static AncientsConfig installed() {
        AncientsConfig config = table;
        if (config == null) {
            WoldLogger.errorOnce("ancients-table-missing",
                    "The gear/ancients/ancients table is not installed; no unique can become ancient and ancient effects do nothing");
        }
        return config;
    }

    /** The ancient-only effect numbers, or null (logged once) when the table is not installed. */
    @Nullable
    public static AncientsConfig.Effects effects() {
        AncientsConfig config = installed();
        return config == null ? null : config.effects;
    }

    /** Whether gear data carries the ancient flag. */
    public static boolean isAncient(VaultGearData data) {
        return data.getFirstValue(ModGearAttributes.ANCIENT_UNIQUE).orElse(false);
    }

    /** Whether a client gear cache carries the ancient flag; the flag is only ever written true. */
    public static boolean isAncient(GearDataCache cache) {
        return cache.hasAttribute(ModGearAttributes.ANCIENT_UNIQUE);
    }

    public static boolean isAncientCapable(ResourceLocation uniqueKey) {
        return names.containsKey(uniqueKey);
    }

    public static Optional<String> getAncientName(ResourceLocation uniqueKey) {
        return Optional.ofNullable(names.get(uniqueKey));
    }

    /**
     * Every registered unique that carries an ancient name. Drawing uniformly from this list weights each gear type
     * by how many ancients it holds, so every ancient is equally likely regardless of type. A named unique the
     * registry does not know is skipped and logged once.
     */
    public static List<ResourceLocation> getAncientCapableUniques() {
        if (installed() == null) {
            return new ArrayList<>();
        }
        List<ResourceLocation> candidates = new ArrayList<>();
        for (ResourceLocation uniqueKey : names.keySet()) {
            if (ModConfigs.UNIQUE_GEAR.getEntry(uniqueKey).isPresent()) {
                candidates.add(uniqueKey);
            } else {
                WoldLogger.errorOnce("ancient-unknown-unique-" + uniqueKey,
                        "The ancients table names unique {}, which the unique registry does not define; it is skipped", uniqueKey);
            }
        }
        return candidates;
    }
}
