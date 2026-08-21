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

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

/**
 * Bridges god tree contributions onto real vanilla attributes for the gear attributes the base
 * mod only ever consumes from equipped gear. {@code VaultGearHelper} turns mana and attack-speed
 * gear attributes into vanilla {@code AttributeModifier}s on the item - nothing reads them from
 * the attribute snapshot - so god values folded only into the snapshot would silently do nothing
 * (Grand Archmage's mana, Weaponmaster's dual-wield attack speed). The vanilla-attribute talents
 * and prestige powers set the precedent this follows.
 *
 * <p>{@code GodCarryover} calls {@link #reconcile} with the exact post-scale instance list it
 * folded into the snapshot, every time the snapshot is rebuilt - so these modifiers appear,
 * update and disappear on the same cadence as every other god stat. Modifiers are transient
 * (never saved to NBT) under fixed UUIDs, applied with the same vanilla operation
 * {@code VaultGearHelper} uses for the matching gear attribute, and only touched when the value
 * actually changed.
 */
public final class GodVanillaAttributes {
    private record Bridge(VaultGearAttribute<?> source, Attribute target,
                          AttributeModifier.Operation operation, UUID id, String name) {
    }

    private static List<Bridge> bridges;
    private static final Set<String> WARNED_MISSING = ConcurrentHashMap.newKeySet();

    private GodVanillaAttributes() {
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

    /**
     * Applies the bridged share of {@code contributions} as vanilla attribute modifiers, removing
     * any bridge modifier whose contribution has gone. Runs on every snapshot rebuild, so a charm
     * swap or refund clears its modifiers on the next rebuild without any per-node bookkeeping.
     */
    public static void reconcile(ServerPlayer player, List<VaultGearAttributeInstance<?>> contributions) {
        for (Bridge bridge : bridges()) {
            double total = 0.0D;
            for (VaultGearAttributeInstance<?> instance : contributions) {
                if (instance.getAttribute() == bridge.source() && instance.getValue() instanceof Number number) {
                    total += number.doubleValue();
                }
            }
            apply(player, bridge, total);
        }
    }

    private static void apply(ServerPlayer player, Bridge bridge, double total) {
        AttributeInstance instance = player.getAttribute(bridge.target());
        if (instance == null) {
            String name = String.valueOf(bridge.target().getRegistryName());
            if (WARNED_MISSING.add(name)) {
                WoldsVaults.LOGGER.warn("Player has no attribute instance for {}; god tree bridge {} cannot apply.",
                        name, bridge.name());
            }
            return;
        }
        AttributeModifier existing = instance.getModifier(bridge.id());
        if (Math.abs(total) < 1.0E-6D) {
            if (existing != null) {
                instance.removeModifier(bridge.id());
            }
            return;
        }
        if (existing != null) {
            if (existing.getAmount() == total) {
                return;
            }
            instance.removeModifier(bridge.id());
        }
        instance.addTransientModifier(new AttributeModifier(bridge.id(), bridge.name(), total, bridge.operation()));
    }
}
