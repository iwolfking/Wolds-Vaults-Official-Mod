package xyz.iwolfking.woldsvaults.api.gear.sets;

import iskallia.vault.entity.entity.EternalEntity;
import iskallia.vault.entity.entity.PetEntity;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.util.SidedHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

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
            WoldLogger.error("Set gear was read before the gear/sets/gear_sets table was installed; nothing counts as a set piece until it is");
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
            config.getSetOf(key).ifPresent(set -> worn.computeIfAbsent(set, id -> new LinkedHashSet<>()).add(config.basePiece(key)));
        }
        return worn;
    }

    /**
     * Returns the snapshot's set pieces plus the set-piece weapons held in either hand. A held weapon counts only when
     * it is identified, not broken and not above the player's level.
     */
    public static Map<ResourceLocation, Set<ResourceLocation>> wornPieces(AttributeSnapshot snapshot, Player player) {
        Map<ResourceLocation, Set<ResourceLocation>> worn = wornPieces(snapshot);
        GearSetConfig config = config();
        if (config == null) {
            return worn;
        }
        for (ItemStack stack : List.of(player.getMainHandItem(), player.getOffhandItem())) {
            if (!(stack.getItem() instanceof VaultGearItem gear) || !gear.isIntendedForSlot(stack, EquipmentSlot.MAINHAND) || gear.isBroken(stack)
                    || VaultGearData.read(stack).getItemLevel() > SidedHelper.getVaultLevel(player)) {
                continue;
            }
            uniqueKey(stack).ifPresent(key -> config.getSetOf(key)
                    .ifPresent(set -> worn.computeIfAbsent(set, id -> new LinkedHashSet<>()).add(config.basePiece(key))));
        }
        return worn;
    }

    /** Adds the grants of every active tier to the snapshot; called once per snapshot rebuild on the server. */
    public static void addSetBonuses(AttributeSnapshot snapshot, Player player) {
        wornPieces(snapshot, player).forEach((setId, pieces) -> {
            for (GearSetConfig.ResolvedGrant grant : activeGrants(setId, pieces.size())) {
                add(snapshot, grant.attribute(), grant.value());
            }
        });
    }

    /** Returns the total that a set's active tiers grant of a number attribute for this many pieces. */
    public static float tierGrant(ResourceLocation setId, int pieces, VaultGearAttribute<? extends Number> attribute) {
        float total = 0.0F;
        for (GearSetConfig.ResolvedGrant grant : activeGrants(setId, pieces)) {
            if (grant.attribute() == attribute) {
                total += ((Number) grant.value()).floatValue();
            }
        }
        return total;
    }

    public static boolean isEnemy(Mob mob) {
        return mob.isAlive() && !(mob instanceof EternalEntity) && !(mob instanceof PetEntity);
    }

    public static List<Mob> enemiesNear(ServerLevel level, Vec3 at, float radius) {
        return level.getEntitiesOfClass(Mob.class, new AABB(at, at).inflate(radius),
                mob -> isEnemy(mob) && mob.getBoundingBox().getCenter().distanceToSqr(at) <= radius * radius);
    }

    private static List<GearSetConfig.ResolvedGrant> activeGrants(ResourceLocation setId, int pieces) {
        GearSetConfig config = config();
        GearSetConfig.GearSet set = config == null ? null : config.getSet(setId).orElse(null);
        if (set == null) {
            return List.of();
        }
        List<GearSetConfig.ResolvedGrant> active = new ArrayList<>();
        for (GearSetConfig.Tier tier : set.tiers) {
            if (pieces >= tier.pieces) {
                active.addAll(grants(config, setId, tier));
            }
        }
        return active;
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
