package xyz.iwolfking.woldsvaults.api.gear.sets;

import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.snapshot.AttributeSnapshot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.GearSetConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Shared set-gear logic: which set an item belongs to, which pieces a player's attribute snapshot counts, and the
 * tier grants that the snapshot hook adds. Set membership comes only from {@code gear_sets.json}, keyed by the
 * item's unique id; rarity alone is not a test, because etched gear is UNIQUE rarity too.
 */
public final class GearSetHelper {
    private static final GearSetConfig INERT = GearSetConfig.inert();
    private static final AtomicBoolean WARNED = new AtomicBoolean();
    private static GearSetConfig resolvedFor;
    private static final Map<GearSetConfig.Tier, List<GearSetConfig.ResolvedGrant>> RESOLVED = new IdentityHashMap<>();

    private GearSetHelper() {
    }

    /** The installed set table, or an inert one with no sets until it is installed; never null. */
    public static GearSetConfig config() {
        GearSetConfig config = ModConfigs.GEAR_SETS;
        if (config != null) {
            return config;
        }
        if (WARNED.compareAndSet(false, true)) {
            WoldsVaults.LOGGER.error("Set gear was read before the gear/sets/gear_sets table was installed; nothing counts as a set piece until it is");
        }
        return INERT;
    }

    public static Optional<ResourceLocation> uniqueKey(ItemStack stack) {
        if (!(stack.getItem() instanceof VaultGearItem) || !VaultGearData.hasData(stack)) {
            return Optional.empty();
        }
        VaultGearData data = VaultGearData.read(stack);
        if (data.getState() != VaultGearState.IDENTIFIED) {
            return Optional.empty();
        }
        return data.getFirstValue(ModGearAttributes.UNIQUE_ITEM_KEY);
    }

    public static Optional<ResourceLocation> setOf(ItemStack stack) {
        GearSetConfig config = config();
        if (config == null) {
            return Optional.empty();
        }
        return uniqueKey(stack).flatMap(config::getSetOf);
    }

    public static boolean isSetPiece(ItemStack stack) {
        return setOf(stack).isPresent();
    }

    public static boolean isSetPiece(ResourceLocation uniqueKey) {
        GearSetConfig config = config();
        return config != null && config.getSetOf(uniqueKey).isPresent();
    }

    /** Distinct counted pieces per set, read from the unique ids the snapshot holds. */
    public static Map<ResourceLocation, Set<ResourceLocation>> wornPieces(AttributeSnapshot snapshot) {
        Map<ResourceLocation, Set<ResourceLocation>> worn = new LinkedHashMap<>();
        GearSetConfig config = config();
        if (config == null) {
            return worn;
        }
        for (ResourceLocation key : snapshot.getAttributeValueList(ModGearAttributes.UNIQUE_ITEM_KEY)) {
            config.getSetOf(key).ifPresent(set -> worn.computeIfAbsent(set, id -> new LinkedHashSet<>()).add(key));
        }
        return worn;
    }

    /** Adds the grants of every active tier to the snapshot; called once per snapshot rebuild on the server. */
    public static void addSetBonuses(AttributeSnapshot snapshot) {
        GearSetConfig config = config();
        if (config == null) {
            return;
        }
        wornPieces(snapshot).forEach((setId, pieces) -> config.getSet(setId).ifPresent(set -> {
            for (GearSetConfig.Tier tier : set.tiers) {
                if (pieces.size() >= tier.pieces) {
                    for (GearSetConfig.ResolvedGrant grant : grants(config, setId, tier)) {
                        add(snapshot, grant.attribute(), grant.value());
                    }
                }
            }
        }));
    }

    @SuppressWarnings("unchecked")
    private static <T> void add(AttributeSnapshot snapshot, VaultGearAttribute<T> attribute, Object value) {
        snapshot.addAttributeValue(attribute, (T) value);
    }

    private static synchronized List<GearSetConfig.ResolvedGrant> grants(GearSetConfig config, ResourceLocation setId, GearSetConfig.Tier tier) {
        if (resolvedFor != config) {
            RESOLVED.clear();
            resolvedFor = config;
        }
        return RESOLVED.computeIfAbsent(tier, t -> new ArrayList<>(GearSetConfig.resolve(setId, t)));
    }
}
