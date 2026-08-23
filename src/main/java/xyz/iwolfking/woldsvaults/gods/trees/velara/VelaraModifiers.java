package xyz.iwolfking.woldsvaults.gods.trees.velara;

import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import xyz.iwolfking.woldsvaults.gods.ActiveGodResolver;
import xyz.iwolfking.woldsvaults.gods.GodVanillaAttributes;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * The Velara nodes that multiply vanilla health, armour or movement speed. Every leg folds into one
 * fixed-UUID modifier per attribute, rewritten only when its value changes.
 */
public final class VelaraModifiers {
    private static final UUID ARMOR_MULTIPLIER_ID = GodVanillaAttributes.modifierId(
            "velara_defense", Attributes.ARMOR, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private static final UUID SPEED_MULTIPLIER_ID = GodVanillaAttributes.modifierId(
            VelaraNodes.THE_STONEWALL, Attributes.MOVEMENT_SPEED, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private static final UUID ARMOR_FLAT_ID = GodVanillaAttributes.modifierId(
            "velara_defense", Attributes.ARMOR, AttributeModifier.Operation.ADDITION);
    private static final UUID HEALTH_MULTIPLIER_ID = GodVanillaAttributes.modifierId(
            "velara_defense", Attributes.MAX_HEALTH, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private static final UUID HEALTH_FLAT_ID = GodVanillaAttributes.modifierId(
            "velara_defense", Attributes.MAX_HEALTH, AttributeModifier.Operation.ADDITION);

    private VelaraModifiers() {
    }

    static void update(ServerPlayer player, boolean inVault) {
        int charmCount = inVault ? countPartyCharmGods(player) : 0;

        double armorMultiplier = 1.0D;
        double healthMultiplier = 1.0D;
        double speedMultiplier = 1.0D;
        double armorFlat = 0.0D;
        double healthFlat = 0.0D;

        if (VelaraNodes.isActive(player, VelaraNodes.THE_STONEWALL)) {
            armorMultiplier *= VelaraValues.stonewallArmorMultiplier();
            speedMultiplier *= VelaraValues.stonewallSpeedMultiplier();
        }
        if (VelaraNodes.isActive(player, VelaraNodes.CACTUS)) {
            armorMultiplier *= VelaraValues.cactusArmorMultiplier();
        }
        if (VelaraNodes.isActive(player, VelaraNodes.IMMORTAL)) {
            armorMultiplier *= VelaraValues.immortalArmorMultiplier();
            healthMultiplier *= VelaraValues.immortalHealthMultiplier();
            armorFlat += VelaraValues.immortalFlatArmor();
            healthFlat += VelaraValues.immortalFlatHealth();
        }
        if (VelaraNodes.isActive(player, VelaraNodes.STEADFAST)) {
            armorMultiplier *= 1.0D + steadfastArmorBonus(player);
        }
        if (charmCount > 0 && VelaraNodes.isActive(player, VelaraNodes.DEFENDER_OF_THE_FAITH)) {
            double defender = 1.0D + VelaraValues.defenderPerCharm() * charmCount;
            armorMultiplier *= defender;
            healthMultiplier *= defender;
        }

        applyModifier(player, Attributes.ARMOR, ARMOR_MULTIPLIER_ID, "Velara armor multiplier",
                armorMultiplier - 1.0D, AttributeModifier.Operation.MULTIPLY_TOTAL);
        applyModifier(player, Attributes.ARMOR, ARMOR_FLAT_ID, "Velara armor",
                armorFlat, AttributeModifier.Operation.ADDITION);
        applyModifier(player, Attributes.MAX_HEALTH, HEALTH_MULTIPLIER_ID, "Velara health multiplier",
                healthMultiplier - 1.0D, AttributeModifier.Operation.MULTIPLY_TOTAL);
        applyModifier(player, Attributes.MAX_HEALTH, HEALTH_FLAT_ID, "Velara health",
                healthFlat, AttributeModifier.Operation.ADDITION);
        applyModifier(player, Attributes.MOVEMENT_SPEED, SPEED_MULTIPLIER_ID, "Velara speed multiplier",
                speedMultiplier - 1.0D, AttributeModifier.Operation.MULTIPLY_TOTAL);

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    /** Steadfast's armour bonus, from the unclamped knockback-resistance sum on the snapshot. */
    private static double steadfastArmorBonus(ServerPlayer player) {
        AttributeSnapshot snapshot = AttributeSnapshotHelper.getInstance().getSnapshot(player);
        float knockbackResistance = snapshot.getAttributeValue(ModGearAttributes.KNOCKBACK_RESISTANCE,
                VaultGearAttributeTypeMerger.floatSum());
        float excess = knockbackResistance - VelaraValues.steadfastKnockbackFloor();
        return excess <= 0.0F ? 0.0D : excess * VelaraValues.steadfastArmorPerExcess();
    }

    private static int countPartyCharmGods(ServerPlayer player) {
        Set<VaultGod> gods = EnumSet.noneOf(VaultGod.class);
        ActiveGodResolver.getActiveGod(player).ifPresent(gods::add);
        List<ServerPlayer> allies = VelaraParty.alliesNear(player, 0.0F);
        for (ServerPlayer ally : allies) {
            ActiveGodResolver.getActiveGod(ally).ifPresent(gods::add);
        }
        return gods.size();
    }

    private static void applyModifier(ServerPlayer player, Attribute attribute, UUID id, String name,
                                      double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        AttributeModifier existing = instance.getModifier(id);
        if (existing != null) {
            if (existing.getAmount() == amount) {
                return;
            }
            instance.removeModifier(id);
        }
        if (amount != 0.0D) {
            instance.addTransientModifier(new AttributeModifier(id, name, amount, operation));
        }
    }

    static void clear(ServerPlayer player) {
        removeModifier(player, Attributes.ARMOR, ARMOR_MULTIPLIER_ID);
        removeModifier(player, Attributes.ARMOR, ARMOR_FLAT_ID);
        removeModifier(player, Attributes.MAX_HEALTH, HEALTH_MULTIPLIER_ID);
        removeModifier(player, Attributes.MAX_HEALTH, HEALTH_FLAT_ID);
        removeModifier(player, Attributes.MOVEMENT_SPEED, SPEED_MULTIPLIER_ID);
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void removeModifier(ServerPlayer player, Attribute attribute, UUID id) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null && instance.getModifier(id) != null) {
            instance.removeModifier(id);
        }
    }
}
