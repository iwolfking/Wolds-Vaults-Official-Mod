package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import xyz.iwolfking.vhapi.api.datagen.gen.AbstractLootTableProvider.LootBuilder;
import xyz.iwolfking.vhapi.api.datagen.gen.AbstractLootTableProvider.PoolBuilder;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * The theme drop payload loot tables under {@code woldsvaults:theme_drops/}, registered into
 * {@link ModVaultLootTablesProvider}. Each has one entry whose pool is grafted into a chest tier by
 * {@link ModThemeDropsProvider}'s rules. The Tidebreaker payload holds every piece of the set at equal weight, each
 * as its base gear item rolling that piece's unique.
 */
public final class ModThemeDropPayloadsProvider {
    public static final ResourceLocation TIDEBREAKER = WoldsVaults.id("theme_drops/tidebreaker");

    private static final String UNIQUE_POOL = "the_vault:gear_unique_pool";
    private static final String ROLL_TYPE = "the_vault:gear_roll_type";
    private static final String UNIQUE_ROLL_TYPE = "Unique";

    private ModThemeDropPayloadsProvider() {
    }

    /** The ids of every payload table this class writes. */
    public static Set<ResourceLocation> ids() {
        return Set.of(TIDEBREAKER);
    }

    public static void register(BiConsumer<ResourceLocation, Consumer<LootBuilder>> add, Map<ResourceLocation, Item> uniqueBaseItems) {
        add.accept(TIDEBREAKER, loot -> loot.entry(entry -> entry
                .rolls(1, 1)
                .pool(1, pool -> uniques(pool, ModGearSetsProvider.tidebreakerPieces(), uniqueBaseItems))));
    }

    private static void uniques(PoolBuilder pool, List<String> uniqueIds, Map<ResourceLocation, Item> uniqueBaseItems) {
        for (String uniqueId : uniqueIds) {
            Item base = uniqueBaseItems.get(ResourceLocation.parse(uniqueId));
            if (base == null) {
                throw new IllegalStateException("Theme drop payload names unique " + uniqueId + ", which the unique gear provider does not write");
            }
            pool.itemNbt(1, base.getRegistryName().toString(), 1, 1, nbt -> {
                nbt.put(UNIQUE_POOL, uniqueId);
                nbt.put(ROLL_TYPE, UNIQUE_ROLL_TYPE);
            });
        }
    }
}
