package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.util.RegionPos;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.WorldManager;
import iskallia.vault.core.vault.ClassicPortalLogic;
import iskallia.vault.core.world.generator.GridGenerator;
import iskallia.vault.core.world.generator.VaultGenerator;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.gear.data.GearDataCache;
import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import iskallia.vault.util.calc.PlayerStat;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * Every Tenos node that reshapes a live player stat, on one listener per stat.
 *
 * <p>{@code PLAYER_STAT} is invoked at each read site rather than cached in the attribute
 * snapshot, which is what these need: chest rate, distance from the portal and trap disarm all
 * change constantly, so they cannot ride the snapshot the way plain stat nodes do.
 *
 * <p>Anything that needs to read a player's own item quantity, item rarity or trap disarm reads
 * the attribute snapshot directly instead of calling the helper for that stat, because the helper
 * fires the very event these listeners are attached to.
 */
public final class TenosLootStats {
    public static final float LOOTING_ENGINE_REFERENCE = 1200.0F;
    public static final float INDIANA_JONES_REFERENCE = 1000.0F;
    public static final float MASSIVE_CHESTS_RARITY_MULTIPLIER = 0.5F;
    public static final float WEALTHY_PATRON_PER_UNIQUE = 0.03F;
    public static final float UNSTOPPABLE_GREED_RATIO = 0.10F;
    public static final float DOMAIN_EXPANSION_PER_CELL = 0.025F;
    public static final float DOMAIN_EXPANSION_CAP = 1.5F;

    private static final Object OWNER = new Object();

    private TenosLootStats() {
    }

    static void register() {
        CommonEvents.PLAYER_STAT.of(PlayerStat.ITEM_QUANTITY).register(OWNER, data -> {
            if (data.getEntity() instanceof ServerPlayer player) {
                data.setValue(data.getValue() * lootMultiplier(player, false));
            }
        });
        CommonEvents.PLAYER_STAT.of(PlayerStat.ITEM_RARITY).register(OWNER, data -> {
            if (data.getEntity() instanceof ServerPlayer player) {
                data.setValue(data.getValue() * lootMultiplier(player, true));
            }
        });
        CommonEvents.PLAYER_STAT.of(PlayerStat.TRAP_DISARM_CHANCE).register(OWNER, data -> {
            if (data.getEntity() instanceof ServerPlayer player && TenosNodes.hasMinor(player, TenosNodes.INDIANA_JONES)) {
                data.setValue(0.0F);
            }
        });
        CommonEvents.PLAYER_STAT.of(PlayerStat.AREA_OF_EFFECT).register(OWNER, data -> {
            if (data.getEntity() instanceof ServerPlayer player) {
                data.setValue(data.getValue() * domainExpansion(player));
            }
        });
        CommonEvents.PLAYER_STAT.of(PlayerStat.ABILITY_POWER_MULTIPLIER).register(OWNER, data -> {
            if (data.getEntity() instanceof ServerPlayer player) {
                data.setValue(data.getValue() * unstoppableGreed(player));
            }
        });
    }

    /** The product of every Tenos multiplier that applies to item quantity or item rarity. */
    private static float lootMultiplier(ServerPlayer player, boolean rarity) {
        float multiplier = 1.0F;
        multiplier *= lootingEngine(player);
        multiplier *= indianaJones(player);
        multiplier *= wealthyPatron(player);
        if (rarity && TenosNodes.hasMajor(player, TenosNodes.MASSIVE_CHESTS)) {
            multiplier *= MASSIVE_CHESTS_RARITY_MULTIPLIER;
        }
        return multiplier;
    }

    /**
     * Looting Engine (r99): {@code ((1200 + c) / 1200) ^ (1/3)} with {@code c} the five minute
     * sliding average of chests per minute, exactly as the sheet writes it. Note that at realistic
     * rates the cube root leaves this worth about 1.02x - the constant, not the implementation,
     * is what makes it small.
     */
    private static float lootingEngine(ServerPlayer player) {
        if (!TenosNodes.hasMajor(player, TenosNodes.LOOTING_ENGINE)) {
            return 1.0F;
        }
        float chestsPerMinute = ChestRateTracker.getChestsPerMinute(player);
        return (float) Math.cbrt((LOOTING_ENGINE_REFERENCE + chestsPerMinute) / LOOTING_ENGINE_REFERENCE);
    }

    /**
     * Indiana Jones (r106) trades trap disarm for loot. The disarm value is read off the snapshot
     * rather than through {@code TrapDisarmChanceHelper}, which would re-enter the listener that
     * zeroes it. Negative disarm (greed medallions roll -350%) is clamped to zero so the trade can
     * only ever be neutral, never a loot penalty.
     */
    private static float indianaJones(ServerPlayer player) {
        if (!TenosNodes.hasMinor(player, TenosNodes.INDIANA_JONES)) {
            return 1.0F;
        }
        float disarmPercent = Math.max(0.0F, rawStat(player, ModGearAttributes.TRAP_DISARMING)) * 100.0F;
        return (float) Math.cbrt((INDIANA_JONES_REFERENCE + disarmPercent) / INDIANA_JONES_REFERENCE);
    }

    private static float wealthyPatron(ServerPlayer player) {
        if (!TenosNodes.hasMinor(player, TenosNodes.WEALTHY_PATRON)) {
            return 1.0F;
        }
        int uniques = countUniqueArmour(player);
        return uniques <= 0 ? 1.0F : (float) Math.pow(1.0F + WEALTHY_PATRON_PER_UNIQUE, uniques);
    }

    private static float unstoppableGreed(ServerPlayer player) {
        if (!TenosNodes.hasMinor(player, TenosNodes.UNSTOPPABLE_GREED)) {
            return 1.0F;
        }
        return 1.0F + UNSTOPPABLE_GREED_RATIO * lootStatSum(player);
    }

    /** The player's raw item quantity plus item rarity, straight off the snapshot. */
    public static float lootStatSum(ServerPlayer player) {
        return rawStat(player, ModGearAttributes.ITEM_QUANTITY) + rawStat(player, ModGearAttributes.ITEM_RARITY);
    }

    private static float rawStat(ServerPlayer player, VaultGearAttribute<Float> attribute) {
        AttributeSnapshot snapshot = AttributeSnapshotHelper.getInstance().getSnapshot(player);
        return snapshot.getAttributeValue(attribute, VaultGearAttributeTypeMerger.floatSum());
    }

    private static int countUniqueArmour(ServerPlayer player) {
        int count = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) {
                continue;
            }
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (GearDataCache.of(stack).getRarity() == VaultGearRarity.UNIQUE) {
                count++;
            }
        }
        return count;
    }

    /**
     * Domain Expansion (r104): area of effect grows by 0.025x per cell of taxicab distance from
     * the vault entrance, capped at 1.5x. The entrance is the real spawn point rather than the
     * grid origin, so the distance means what a player would say it means.
     */
    private static float domainExpansion(ServerPlayer player) {
        if (!TenosNodes.hasMinor(player, TenosNodes.DOMAIN_EXPANSION)) {
            return 1.0F;
        }
        int cells = cellsFromEntrance(player);
        if (cells <= 0) {
            return 1.0F;
        }
        return Math.min(DOMAIN_EXPANSION_CAP, 1.0F + DOMAIN_EXPANSION_PER_CELL * cells);
    }

    private static int cellsFromEntrance(ServerPlayer player) {
        Vault vault = TenosVaultUtil.vaultOf(player);
        if (vault == null) {
            return 0;
        }
        WorldManager world = vault.get(Vault.WORLD);
        if (world == null || !(world.get(WorldManager.PORTAL_LOGIC) instanceof ClassicPortalLogic logic)) {
            return 0;
        }
        VaultGenerator generator = world.get(WorldManager.GENERATOR);
        if (!(generator instanceof GridGenerator grid)) {
            return 0;
        }
        Optional<BlockPos> start = logic.getPlayerStartPos(vault);
        if (start.isEmpty()) {
            return 0;
        }
        int cellX = grid.get(GridGenerator.CELL_X);
        int cellZ = grid.get(GridGenerator.CELL_Z);
        RegionPos from = RegionPos.ofBlockPos(start.get(), cellX, cellZ);
        RegionPos to = RegionPos.ofBlockPos(player.blockPosition(), cellX, cellZ);
        return Math.abs(to.getX() - from.getX()) + Math.abs(to.getZ() - from.getZ());
    }
}
