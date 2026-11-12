package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.VaultMod;
import iskallia.vault.config.UniqueGearConfig;
import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModGearAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * Resolves unique and ancient unique modifier rolls. A unique's tier table may hold a row scoped to that unique,
 * {@code <unique namespace>:<unique path>/<identifier path>}, which wins over the shared row for the same
 * identifier. Ancient items roll from {@link #ANCIENT_TABLE} and fall back to the regular unique table, with a
 * warning, for identifiers the ancient table lacks.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class UniqueRollTables {
    public static final ResourceLocation ANCIENT_TABLE = VaultMod.id("unique_ancient");

    private static final ThreadLocal<VaultGearData> ROLLING = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> RESOLVING = ThreadLocal.withInitial(() -> false);

    private UniqueRollTables() {
    }

    public static ResourceLocation scoped(ResourceLocation uniqueKey, ResourceLocation identifier) {
        return new ResourceLocation(uniqueKey.getNamespace(), uniqueKey.getPath() + "/" + identifier.getPath());
    }

    public static void beginUniqueRoll(VaultGearData data) {
        ROLLING.set(data);
    }

    public static void endUniqueRoll() {
        ROLLING.remove();
    }

    /** Whether {@code generateModifier} calls on this thread belong to a unique roll and should be resolved here. */
    public static boolean shouldResolve() {
        return ROLLING.get() != null && !RESOLVING.get();
    }

    /**
     * Rolls one identifier for the unique currently being rolled: the scoped row, then the shared row; an ancient
     * item then tries the regular unique table and logs that it did. Null when nothing defines the identifier,
     * which the caller skips exactly as the base mod does.
     */
    @Nullable
    public static VaultGearModifier<?> generate(VaultGearTierConfig config, ResourceLocation identifier, int level, Random random) {
        VaultGearData data = ROLLING.get();
        ResourceLocation uniqueKey = data == null ? null : data.getFirstValue(ModGearAttributes.UNIQUE_ITEM_KEY).orElse(null);
        RESOLVING.set(true);
        try {
            ResourceLocation found = resolve(config, uniqueKey, identifier);
            if (found != null) {
                return config.generateModifier(found, level, random);
            }
            if (data == null || !AncientUniqueHelper.isAncient(data)) {
                return null;
            }
            VaultGearTierConfig regular = ModConfigs.VAULT_GEAR_CONFIG.get(VaultGearTierConfig.UNIQUE_ITEM);
            if (regular == null || regular == config) {
                return null;
            }
            ResourceLocation regularFound = resolve(regular, uniqueKey, identifier);
            if (regularFound == null) {
                return null;
            }
            WoldLogger.error("Ancient {} has no ancient row for {}; rolled the regular unique value instead.", uniqueKey, identifier);
            return regular.generateModifier(regularFound, level, random);
        } finally {
            RESOLVING.set(false);
        }
    }

    @Nullable
    private static ResourceLocation resolve(VaultGearTierConfig config, @Nullable ResourceLocation uniqueKey, ResourceLocation identifier) {
        if (uniqueKey != null) {
            ResourceLocation scoped = scoped(uniqueKey, identifier);
            if (config.getTierGroup(scoped) != null) {
                return scoped;
            }
        }
        return config.getTierGroup(identifier) != null ? identifier : null;
    }

    /** Whether the table can roll at least one of the unique's identifiers. */
    public static boolean resolvesAny(VaultGearTierConfig config, ResourceLocation uniqueKey) {
        UniqueGearConfig.Entry entry = ModConfigs.UNIQUE_GEAR.getEntry(uniqueKey).orElse(null);
        if (entry == null) {
            return true;
        }
        return entry.getModifierIdentifiers().values().stream()
                .flatMap(List::stream)
                .anyMatch(identifier -> resolve(config, uniqueKey, identifier) != null);
    }

    /**
     * Logs every identifier a unique lists that neither its regular table nor, for ancient-capable uniques, the
     * ancient table defines; such a modifier never rolls.
     */
    @SubscribeEvent
    public static void auditTables(ServerStartedEvent event) {
        VaultGearTierConfig regular = ModConfigs.VAULT_GEAR_CONFIG.get(VaultGearTierConfig.UNIQUE_ITEM);
        VaultGearTierConfig ancient = ModConfigs.VAULT_GEAR_CONFIG.get(ANCIENT_TABLE);
        if (regular == null) {
            WoldLogger.error("The unique tier table is not loaded; skipping the unique roll audit.");
            return;
        }
        List<String> dead = new ArrayList<>();
        for (Map.Entry<ResourceLocation, UniqueGearConfig.Entry> unique : ModConfigs.UNIQUE_GEAR.getRegistry().entrySet()) {
            ResourceLocation uniqueKey = unique.getKey();
            boolean ancientCapable = ancient != null && AncientUniqueHelper.getAncientName(uniqueKey).isPresent();
            unique.getValue().getModifierIdentifiers().values().stream().flatMap(List::stream).forEach(identifier -> {
                boolean inRegular = resolve(regular, uniqueKey, identifier) != null;
                boolean inAncient = ancientCapable && resolve(ancient, uniqueKey, identifier) != null;
                if (!inRegular && !inAncient) {
                    dead.add(uniqueKey + " -> " + identifier);
                }
            });
        }
        if (!dead.isEmpty()) {
            WoldLogger.error("{} unique modifier identifiers resolve in no tier table and never roll: {}", dead.size(), dead);
        }
    }
}
