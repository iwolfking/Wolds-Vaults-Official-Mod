package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.entity.boss.TheVesselEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;

public class VesselSlowCapHelper {

    public static final double MIN_SPEED_FRACTION = 0.5D;

    /**
     * Floors the Vessel's movement speed at {@link #MIN_SPEED_FRACTION} of its unslowed speed, so stacked slows
     * (Chilled, Slowness, ...) can never freeze it in place and leave it unable to reach the player.
     *
     * @param vessel the Vessel whose movement speed is being read
     * @param speed the attribute's value with every modifier applied
     * @param instance the Vessel's movement speed attribute instance
     * @return the speed the Vessel should move at
     */
    public static double capSlow(TheVesselEntity vessel, double speed, AttributeInstance instance) {
        double floor = getUnslowedSpeed(instance) * MIN_SPEED_FRACTION;
        if (speed >= floor) {
            return speed;
        }
        if (WoldsVaultsConfig.COMMON.enableDebugMode.get()) {
            WoldsVaults.LOGGER.debug("[WOLD'S VAULTS] Capped Vessel {} slow: movement speed {} raised to {}.", vessel.getId(), speed, floor);
        }
        return floor;
    }

    /**
     * Recomputes the attribute value the same way {@link AttributeInstance} does, skipping every modifier with a
     * negative amount.
     */
    private static double getUnslowedSpeed(AttributeInstance instance) {
        double base = instance.getBaseValue();
        for (AttributeModifier modifier : instance.getModifiers()) {
            if (modifier.getOperation() == AttributeModifier.Operation.ADDITION && modifier.getAmount() > 0.0D) {
                base += modifier.getAmount();
            }
        }
        double value = base;
        for (AttributeModifier modifier : instance.getModifiers()) {
            if (modifier.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE && modifier.getAmount() > 0.0D) {
                value += base * modifier.getAmount();
            }
        }
        for (AttributeModifier modifier : instance.getModifiers()) {
            if (modifier.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL && modifier.getAmount() > 0.0D) {
                value *= 1.0D + modifier.getAmount();
            }
        }
        return instance.getAttribute().sanitizeValue(value);
    }
}
