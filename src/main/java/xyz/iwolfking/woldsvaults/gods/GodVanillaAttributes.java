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
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

/**
 * Bridges the gear attributes the base mod only consumes from equipped gear - mana, attack speed - onto
 * vanilla attributes, as transient modifiers reconciled on every snapshot rebuild. Also owns the shared
 * modifier UUID scheme, {@link #modifierId}.
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
                            UUID.fromString("5f0c9e5a-1a6c-4b0e-9d64-7c1e2b6f8a04"), "wolds_god_attack_speed"));
        }
        return bridges;
    }

    /** Applies the bridged share of {@code contributions}, removing any bridge modifier that has gone. */
    public static void reconcile(ServerPlayer player, List<VaultGearAttributeInstance<?>> contributions) {
        for (Bridge bridge : bridges()) {
            double total = 0.0D;
            for (VaultGearAttributeInstance<?> instance : contributions) {
                if (instance.getAttribute() == bridge.source() && instance.getValue() instanceof Number number) {
                    total += number.doubleValue();
                }
            }
            apply(player, bridge.target(), bridge.operation(), bridge.id(), bridge.name(), total);
        }
    }

    private static void apply(ServerPlayer player, Attribute target, AttributeModifier.Operation operation,
                              UUID id, String name, double total) {
        AttributeInstance instance = player.getAttribute(target);
        if (instance == null) {
            String attributeName = String.valueOf(target.getRegistryName());
            if (WARNED_MISSING.add(attributeName)) {
                WoldsVaults.LOGGER.warn("Player has no attribute instance for {}; god tree bridge {} cannot apply.",
                        attributeName, name);
            }
            return;
        }
        AttributeModifier existing = instance.getModifier(id);
        if (Math.abs(total) < 1.0E-6D) {
            if (existing != null) {
                instance.removeModifier(id);
            }
            return;
        }
        if (existing != null) {
            if (existing.getAmount() == total) {
                return;
            }
            instance.removeModifier(id);
        }
        instance.addTransientModifier(new AttributeModifier(id, name, total, operation));
    }
}
