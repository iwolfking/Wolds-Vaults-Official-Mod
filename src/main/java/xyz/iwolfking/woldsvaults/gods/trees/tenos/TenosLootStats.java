package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.util.RegionPos;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.WorldManager;
import iskallia.vault.core.vault.ClassicPortalLogic;
import iskallia.vault.core.vault.influence.VaultGod;
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
import xyz.iwolfking.woldsvaults.gods.node.GodNodePreviews;

import java.util.Optional;

/**
 * Every Tenos node that reshapes a live player stat, one {@code PLAYER_STAT} listener per stat.
 * Reads of the player's own item quantity, rarity or trap disarm must use {@link #rawStat}.
 */
public final class TenosLootStats {
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
            if (data.getEntity() instanceof ServerPlayer player
                    && TenosNodes.isActive(player, TenosNodes.INDIANA_JONES)) {
                data.setValue(0.0F);
            }
        });
        CommonEvents.PLAYER_STAT.of(PlayerStat.AREA_OF_EFFECT).register(OWNER, data -> {
            if (data.getEntity() instanceof ServerPlayer player) {
                data.setValue(data.getValue() * domainExpansion(player));
            }
        });
    }

    private static float lootMultiplier(ServerPlayer player, boolean rarity) {
        float multiplier = 1.0F;
        multiplier *= lootingEngine(player);
        multiplier *= indianaJones(player);
        multiplier *= wealthyPatron(player);
        if (rarity && TenosNodes.isActive(player, TenosNodes.MASSIVE_CHESTS)) {
            multiplier *= TenosNodeHandlers.params(TenosNodes.MASSIVE_CHESTS,
                    TenosNodeHandlers.MassiveChestsParams.class).rarity_multiplier();
        }
        return multiplier;
    }

    /** Looting Engine: {@code cbrt((reference + c) / reference)}, {@code c} being chests per minute. */
    private static float lootingEngine(ServerPlayer player) {
        if (!TenosNodes.isActive(player, TenosNodes.LOOTING_ENGINE)) {
            return 1.0F;
        }
        return lootingEngineFactor(lootingEngineReference(), ChestRateTracker.getChestsPerMinute(player));
    }

    private static float lootingEngineReference() {
        return TenosNodeHandlers.params(TenosNodes.LOOTING_ENGINE,
                TenosNodeHandlers.LootingEngineParams.class).reference();
    }

    private static float lootingEngineFactor(float reference, float chestsPerMinute) {
        return (float) Math.cbrt((reference + chestsPerMinute) / reference);
    }

    static GodNodePreviews.Preview previewLootingEngine(ServerPlayer player) {
        float reference = lootingEngineReference();
        float rate = ChestRateTracker.getChestsPerMinute(player);
        float factor = lootingEngineFactor(reference, rate);
        String referenceText = GodNodePreviews.number(reference);
        return new GodNodePreviews.Working(VaultGod.TENOS)
                .formula("Item Quantity and Rarity multiplier", "cubeRoot((" + referenceText + " + c) / " + referenceText + ")")
                .input("c", "your chests per minute, averaged over the last five minutes", GodNodePreviews.number(rate))
                .result("cubeRoot(" + GodNodePreviews.number(reference + rate) + " / " + referenceText + ")", factor)
                .inactive(!TenosNodes.isActive(player, TenosNodes.LOOTING_ENGINE))
                .build(factor);
    }

    /** Indiana Jones: {@code cbrt((reference + disarm%) / reference)}; negative disarm counts as 0. */
    private static float indianaJones(ServerPlayer player) {
        if (!TenosNodes.isActive(player, TenosNodes.INDIANA_JONES)) {
            return 1.0F;
        }
        return indianaJonesFactor(indianaJonesReference(), indianaJonesDisarmPercent(player));
    }

    private static float indianaJonesReference() {
        return TenosNodeHandlers.params(TenosNodes.INDIANA_JONES,
                TenosNodeHandlers.IndianaJonesParams.class).reference();
    }

    private static float indianaJonesDisarmPercent(ServerPlayer player) {
        return Math.max(0.0F, rawStat(player, ModGearAttributes.TRAP_DISARMING)) * 100.0F;
    }

    private static float indianaJonesFactor(float reference, float disarmPercent) {
        return (float) Math.cbrt((reference + disarmPercent) / reference);
    }

    static GodNodePreviews.Preview previewIndianaJones(ServerPlayer player) {
        float reference = indianaJonesReference();
        float disarm = indianaJonesDisarmPercent(player);
        float factor = indianaJonesFactor(reference, disarm);
        String referenceText = GodNodePreviews.number(reference);
        return new GodNodePreviews.Working(VaultGod.TENOS)
                .formula("Item Quantity and Rarity multiplier", "cubeRoot((" + referenceText + " + trap disarm) / " + referenceText + ")")
                .input("trap disarm", "your Trap Disarming from gear, in percent (negative counts as 0)", GodNodePreviews.number(disarm))
                .result("cubeRoot(" + GodNodePreviews.number(reference + disarm) + " / " + referenceText + ")", factor)
                .inactive(!TenosNodes.isActive(player, TenosNodes.INDIANA_JONES))
                .build(factor);
    }

    private static float wealthyPatron(ServerPlayer player) {
        if (!TenosNodes.isActive(player, TenosNodes.WEALTHY_PATRON)) {
            return 1.0F;
        }
        int uniques = countUniqueArmour(player);
        if (uniques <= 0) {
            return 1.0F;
        }
        return (float) Math.pow(1.0F + TenosNodeHandlers.params(TenosNodes.WEALTHY_PATRON,
                TenosNodeHandlers.WealthyPatronParams.class).per_unique(), uniques);
    }

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

    /** Domain Expansion: area of effect grows per cell of distance from the entrance, up to a cap. */
    private static float domainExpansion(ServerPlayer player) {
        if (!TenosNodes.isActive(player, TenosNodes.DOMAIN_EXPANSION)) {
            return 1.0F;
        }
        int cells = cellsFromEntrance(player);
        if (cells <= 0) {
            return 1.0F;
        }
        TenosNodeHandlers.DomainExpansionParams params = TenosNodeHandlers.params(TenosNodes.DOMAIN_EXPANSION,
                TenosNodeHandlers.DomainExpansionParams.class);
        return Math.min(params.cap(), 1.0F + params.per_cell() * cells);
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
