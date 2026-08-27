package xyz.iwolfking.woldsvaults.gods;

import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.init.ModAttributes;
import iskallia.vault.init.ModGearAttributes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

/**
 * Bridges the gear attributes the base mod only consumes from equipped gear onto vanilla attributes, as
 * transient modifiers reconciled on every snapshot rebuild. The bridge table mirrors every mapping
 * {@code VaultGearHelper#getModifiers} makes, target attribute and operation alike, so a god node stat
 * lands exactly where the same stat rolled on gear would; the percentile legs are {@code MULTIPLY_BASE}
 * and so stack additively with their gear counterparts. Also owns the shared modifier UUID scheme,
 * {@link #modifierId}.
 */
public final class GodVanillaAttributes {
    private record Bridge(VaultGearAttribute<?> source, Attribute target,
                          AttributeModifier.Operation operation, UUID id, String name) {
    }

    private static List<Bridge> bridges;
    private static final Set<String> WARNED_MISSING = ConcurrentHashMap.newKeySet();

    private GodVanillaAttributes() {
    }


    /**
     * The fixed modifier UUID for one {@code (effectId, attribute, operation)} triple, derived from its
     * name. Keying on the operation lets one effect hold two claims on the same attribute.
     */
    public static UUID modifierId(String effectId, Attribute target, AttributeModifier.Operation operation) {
        return UUID.nameUUIDFromBytes(("woldsvaults:god_node:" + effectId + ":" + target.getRegistryName()
                + ":" + operation.name()).getBytes(StandardCharsets.UTF_8));
    }


    private static List<Bridge> bridges() {
        if (bridges == null) {
            bridges = List.of(
                    new Bridge(ModGearAttributes.MANA_ADDITIVE, ModAttributes.MANA_MAX,
                            AttributeModifier.Operation.ADDITION,
                            UUID.fromString("5f0c9e5a-1a6c-4b0e-9d64-7c1e2b6f8a01"), "wolds_god_mana_flat"),
                    new Bridge(ModGearAttributes.MANA_ADDITIVE_PERCENTILE, ModAttributes.MANA_MAX,
                            AttributeModifier.Operation.MULTIPLY_BASE,
                            UUID.fromString("5f0c9e5a-1a6c-4b0e-9d64-7c1e2b6f8a02"), "wolds_god_mana_percent"),
                    new Bridge(ModGearAttributes.MANA_REGEN_ADDITIVE_PERCENTILE, ModAttributes.MANA_REGEN,
                            AttributeModifier.Operation.MULTIPLY_BASE,
                            UUID.fromString("5f0c9e5a-1a6c-4b0e-9d64-7c1e2b6f8a03"), "wolds_god_mana_regen"),
                    new Bridge(ModGearAttributes.ATTACK_SPEED_PERCENT, Attributes.ATTACK_SPEED,
                            AttributeModifier.Operation.MULTIPLY_BASE,
                            UUID.fromString("5f0c9e5a-1a6c-4b0e-9d64-7c1e2b6f8a04"), "wolds_god_attack_speed"),
                    new Bridge(ModGearAttributes.HEALTH, Attributes.MAX_HEALTH,
                            AttributeModifier.Operation.ADDITION,
                            UUID.fromString("5f0c9e5a-1a6c-4b0e-9d64-7c1e2b6f8a05"), "wolds_god_health_flat"),
                    new Bridge(ModGearAttributes.HEALTH_PERCENTILE, Attributes.MAX_HEALTH,
                            AttributeModifier.Operation.MULTIPLY_BASE,
                            UUID.fromString("5f0c9e5a-1a6c-4b0e-9d64-7c1e2b6f8a06"), "wolds_god_health_percent"),
                    new Bridge(ModGearAttributes.ARMOR, Attributes.ARMOR,
                            AttributeModifier.Operation.ADDITION,
                            UUID.fromString("5f0c9e5a-1a6c-4b0e-9d64-7c1e2b6f8a07"), "wolds_god_armor_flat"),
                    new Bridge(ModGearAttributes.ARMOR_PERCENTILE, Attributes.ARMOR,
                            AttributeModifier.Operation.MULTIPLY_BASE,
                            UUID.fromString("5f0c9e5a-1a6c-4b0e-9d64-7c1e2b6f8a08"), "wolds_god_armor_percent"),
                    new Bridge(ModGearAttributes.ARMOR_TOUGHNESS, Attributes.ARMOR_TOUGHNESS,
                            AttributeModifier.Operation.ADDITION,
                            UUID.fromString("5f0c9e5a-1a6c-4b0e-9d64-7c1e2b6f8a09"), "wolds_god_armor_toughness"),
                    new Bridge(ModGearAttributes.KNOCKBACK_RESISTANCE, Attributes.KNOCKBACK_RESISTANCE,
                            AttributeModifier.Operation.ADDITION,
                            UUID.fromString("5f0c9e5a-1a6c-4b0e-9d64-7c1e2b6f8a0a"), "wolds_god_knockback_resistance"),
                    new Bridge(ModGearAttributes.ATTACK_DAMAGE, Attributes.ATTACK_DAMAGE,
                            AttributeModifier.Operation.ADDITION,
                            UUID.fromString("5f0c9e5a-1a6c-4b0e-9d64-7c1e2b6f8a0b"), "wolds_god_attack_damage"),
                    new Bridge(ModGearAttributes.ATTACK_SPEED, Attributes.ATTACK_SPEED,
                            AttributeModifier.Operation.ADDITION,
                            UUID.fromString("5f0c9e5a-1a6c-4b0e-9d64-7c1e2b6f8a0c"), "wolds_god_attack_speed_flat"),
                    new Bridge(ModGearAttributes.REACH, ForgeMod.REACH_DISTANCE.get(),
                            AttributeModifier.Operation.ADDITION,
                            UUID.fromString("5f0c9e5a-1a6c-4b0e-9d64-7c1e2b6f8a0d"), "wolds_god_reach"),
                    new Bridge(ModGearAttributes.ATTACK_RANGE, ForgeMod.ATTACK_RANGE.get(),
                            AttributeModifier.Operation.ADDITION,
                            UUID.fromString("5f0c9e5a-1a6c-4b0e-9d64-7c1e2b6f8a0e"), "wolds_god_attack_range"),
                    new Bridge(ModGearAttributes.MOVEMENT_SPEED, Attributes.MOVEMENT_SPEED,
                            AttributeModifier.Operation.MULTIPLY_BASE,
                            UUID.fromString("5f0c9e5a-1a6c-4b0e-9d64-7c1e2b6f8a0f"), "wolds_god_movement_speed"));
        }
        return bridges;
    }

    /**
     * Applies the bridged share of {@code contributions}, removing any bridge modifier that has gone.
     * Health is clamped only when a max-health bridge actually moved this pass, so a shrinking pool
     * cannot leave the player above its own maximum.
     */
    public static void reconcile(ServerPlayer player, List<VaultGearAttributeInstance<?>> contributions) {
        boolean maxHealthChanged = false;
        for (Bridge bridge : bridges()) {
            double total = 0.0D;
            for (VaultGearAttributeInstance<?> instance : contributions) {
                if (instance.getAttribute() == bridge.source() && instance.getValue() instanceof Number number) {
                    total += number.doubleValue();
                }
            }
            boolean changed = apply(player, bridge.target(), bridge.operation(), bridge.id(), bridge.name(), total);
            maxHealthChanged |= changed && bridge.target() == Attributes.MAX_HEALTH;
        }
        if (maxHealthChanged && player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static boolean apply(ServerPlayer player, Attribute target, AttributeModifier.Operation operation,
                                 UUID id, String name, double total) {
        AttributeInstance instance = player.getAttribute(target);
        if (instance == null) {
            String attributeName = String.valueOf(target.getRegistryName());
            if (WARNED_MISSING.add(attributeName)) {
                WoldsVaults.LOGGER.warn("Player has no attribute instance for {}; god tree bridge {} cannot apply.",
                        attributeName, name);
            }
            return false;
        }
        AttributeModifier existing = instance.getModifier(id);
        if (Math.abs(total) < 1.0E-6D) {
            if (existing == null) {
                return false;
            }
            instance.removeModifier(id);
            return true;
        }
        if (existing != null) {
            if (existing.getAmount() == total) {
                return false;
            }
            instance.removeModifier(id);
        }
        instance.addTransientModifier(new AttributeModifier(id, name, total, operation));
        return true;
    }
}
