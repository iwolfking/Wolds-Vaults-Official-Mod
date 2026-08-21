package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearAttributeRegistry;
import iskallia.vault.gear.attribute.ability.AbilityLevelAttribute;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.gear.data.AttributeGearData;
import iskallia.vault.init.ModGearAttributes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.GodAlignmentData;
import xyz.iwolfking.woldsvaults.gods.GodNodeAttributeSource;
import xyz.iwolfking.woldsvaults.gods.GodTreeAttributeProviders;
import xyz.iwolfking.woldsvaults.items.gear.VaultLootSackItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import xyz.iwolfking.woldsvaults.gods.GodNodeValues;

/**
 * Turns the player's spent Tenos points into vault gear attributes. Two behaviour nodes are
 * expressible as attributes and live here rather than in a handler: Global Veins, whose "+8
 * effective levels" is exactly what the base mod's added-ability-level attribute already does, and
 * Sacked, which doubles a loot sack by contributing a second copy of what the sack rolls.
 */
public final class TenosAttributeProvider implements GodTreeAttributeProviders.Provider {
    public static final String VEIN_MINER_ABILITY = "Vein_Miner";
    public static int globalVeinsLevels() {
        return GodNodeValues.count(TenosNodes.GLOBAL_VEINS, "levels");
    }

    @Override
    public List<VaultGearAttributeInstance<?>> getGearAttributes(ServerPlayer player, GodNodeAttributeSource.Scope scope) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return List.of();
        }
        Map<String, Integer> ledger = GodAlignmentData.get(server).getSpentLedger(player.getUUID(), TenosNodes.GOD);
        List<VaultGearAttributeInstance<?>> result = new ArrayList<>();
        ledger.forEach((nodeId, points) -> {
            if (points <= 0) {
                return;
            }
            TenosNodes.StatEntry basic = TenosNodes.BASIC_STATS.get(nodeId);
            if (basic != null) {
                result.add(VaultGearAttributeInstance.cast(basic.attribute(), basic.perPoint() * points));
                return;
            }
            if (scope == GodNodeAttributeSource.Scope.ALL) {
                addNonBasic(player, nodeId, points, result);
            }
        });
        return result;
    }

    @Override
    public List<VaultGearAttributeInstance<?>> getMinorTransferAttributes(ServerPlayer player, Collection<String> nodeIds) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return List.of();
        }
        GodAlignmentData data = GodAlignmentData.get(server);
        List<VaultGearAttributeInstance<?>> result = new ArrayList<>();
        for (String nodeId : nodeIds) {
            if (!TenosNodes.owns(nodeId) || !TenosNodes.MINORS.contains(nodeId)) {
                continue;
            }
            int points = data.getPointsIn(player.getUUID(), TenosNodes.GOD, nodeId);
            if (points > 0) {
                addNonBasic(player, nodeId, points, result);
            }
        }
        return result;
    }

    private static void addNonBasic(ServerPlayer player, String nodeId, int points, List<VaultGearAttributeInstance<?>> result) {
        switch (nodeId) {
            case TenosNodes.GLOBAL_VEINS -> result.add(VaultGearAttributeInstance.cast(ModGearAttributes.ABILITY_LEVEL,
                    new AbilityLevelAttribute(VEIN_MINER_ABILITY, globalVeinsLevels() * points)));
            case TenosNodes.SACKED -> addSackCopy(player, result);
            default -> {
            }
        }
    }

    private static void addSackCopy(ServerPlayer player, List<VaultGearAttributeInstance<?>> result) {
        ItemStack offhand = player.getOffhandItem();
        if (offhand.isEmpty() || !(offhand.getItem() instanceof VaultLootSackItem)) {
            return;
        }
        copyAttributes(offhand, result);
    }

    /**
     * Emits a second copy of everything an offhand focus item rolls, which is what "doubles all
     * given stats" means once the gear pass has already contributed the first copy. Same iteration
     * the snapshot calculator performs for the vault charm multiplier.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static void copyAttributes(ItemStack stack, List<VaultGearAttributeInstance<?>> out) {
        AttributeGearData data = AttributeGearData.read(stack);
        for (VaultGearAttribute attribute : VaultGearAttributeRegistry.getRegistry()) {
            List<?> values = (List<?>) data.get(attribute, VaultGearAttributeTypeMerger.asList());
            for (Object value : values) {
                try {
                    out.add(VaultGearAttributeInstance.cast(attribute, value));
                } catch (ClassCastException e) {
                    WoldsVaults.LOGGER.error("Skipping focus attribute {} while doubling {}: value {} did not cast.",
                            attribute.getRegistryName(), stack.getItem().getRegistryName(), value, e);
                }
            }
        }
    }
}
