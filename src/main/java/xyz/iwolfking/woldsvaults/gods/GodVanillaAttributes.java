package xyz.iwolfking.woldsvaults.gods;

import iskallia.vault.core.vault.influence.VaultGod;
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
import xyz.iwolfking.woldsvaults.gods.node.GodNodeContext;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

/**
 * The single choke point between god tree nodes and real vanilla attributes, and the only place
 * in the god core allowed to create an {@code AttributeModifier}.
 *
 * <p>Two things arrive here. First, the gear attributes the base mod only ever consumes from
 * equipped gear: {@code VaultGearHelper} turns mana and attack-speed gear attributes into vanilla
 * modifiers on the item - nothing reads them from the attribute snapshot - so god values folded
 * only into the snapshot would silently do nothing (Grand Archmage's mana, Weaponmaster's
 * dual-wield attack speed). Second, nodes that must move a vanilla attribute outright (movement
 * speed, armor), which {@link #declare} registers.
 *
 * <p>Vanilla modifiers persist on the entity until explicitly removed, and this codebase has
 * already shipped that leak once, so every modifier written here uses a UUID derived
 * deterministically from its {@code (effectId, attribute)} pair - never a random one.
 * {@link #reconcile(ServerPlayer)} walks every declaration, applies the value each one currently
 * wants and removes the modifier of any declaration that wants nothing, which is what lets a
 * respec, a charm swap or a save left dirty by a crash come back to baseline without a relog.
 *
 * <p>{@code GodCarryover} calls {@link #reconcile(ServerPlayer, List)} with the exact post-scale
 * instance list it folded into the snapshot, every time the snapshot is rebuilt - so these
 * modifiers appear, update and disappear on the same cadence as every other god stat. Modifiers
 * are transient (never saved to NBT), applied with the same vanilla operation
 * {@code VaultGearHelper} uses for the matching gear attribute, and only touched when the value
 * actually changed.
 */
public final class GodVanillaAttributes {
    private record Bridge(VaultGearAttribute<?> source, Attribute target,
                          AttributeModifier.Operation operation, UUID id, String name) {
    }

    /**
     * A node effect's claim on one vanilla attribute. {@code amount} is asked for the value the
     * effect wants right now and must return zero when it wants none.
     */
    @FunctionalInterface
    public interface VanillaAmount {
        double get(GodNodeContext context);
    }

    private record Declaration(String effectId, VaultGod god, Attribute target,
                               AttributeModifier.Operation operation, UUID id, String name, VanillaAmount amount) {
    }

    private static List<Bridge> bridges;
    private static final Map<String, Declaration> DECLARATIONS = new ConcurrentHashMap<>();
    private static final Set<String> WARNED_MISSING = ConcurrentHashMap.newKeySet();

    private GodVanillaAttributes() {
    }

    /**
     * Registers that {@code effectId} moves a vanilla attribute. Declarations are static - one
     * per effect, attribute and operation, made during setup - which is what lets
     * {@link #reconcile} find and remove a stray modifier from a previous session without any
     * saved bookkeeping.
     */
    public static void declare(String effectId, VaultGod god, Attribute target,
                               AttributeModifier.Operation operation, VanillaAmount amount) {
        UUID id = modifierId(effectId, target, operation);
        Declaration declaration = new Declaration(effectId, god, target, operation, id,
                "wolds_god_node/" + effectId, amount);
        Declaration previous = DECLARATIONS.put(id.toString(), declaration);
        if (previous != null) {
            WoldsVaults.LOGGER.error("God node effect {} declared vanilla attribute {} as {} twice; the later "
                    + "declaration wins.", effectId, target.getRegistryName(), operation);
        }
    }

    /**
     * The fixed modifier UUID for one {@code (effectId, attribute, operation)} triple. Derived from
     * the triple's name, so it is identical across sessions and across a rebuild of the config.
     *
     * <p>The operation is part of the key because a vanilla {@code AttributeInstance} indexes its
     * modifiers by UUID: without it one effect could not hold both a {@code MULTIPLY_TOTAL} and an
     * {@code ADDITION} claim on the same attribute, which is exactly the shape the shipped Velara
     * armour and health nodes need.
     */
    public static UUID modifierId(String effectId, Attribute target, AttributeModifier.Operation operation) {
        return UUID.nameUUIDFromBytes(("woldsvaults:god_node:" + effectId + ":" + target.getRegistryName()
                + ":" + operation.name()).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Diffs every declared vanilla attribute against what is applied to {@code player} and removes
     * the strays. Runs on node purchase, refund, charm change, dimension change and login - the
     * login pass is what repairs a save left dirty by a crash.
     */
    public static void reconcile(ServerPlayer player) {
        for (Declaration declaration : DECLARATIONS.values()) {
            double total = GodNodeGate.context(player, declaration.god(), declaration.effectId())
                    .map(declaration.amount()::get)
                    .orElse(0.0D);
            apply(player, declaration.target(), declaration.operation(), declaration.id(), declaration.name(), total);
        }
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
     * any bridge modifier whose contribution has gone, then reconciles the declared vanilla
     * attributes. Runs on every snapshot rebuild, so a charm swap or refund clears its modifiers
     * on the next rebuild without any per-node bookkeeping.
     */
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
        reconcile(player);
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
