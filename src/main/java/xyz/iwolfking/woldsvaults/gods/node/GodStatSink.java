package xyz.iwolfking.woldsvaults.gods.node;

import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;

import java.util.List;

/**
 * Collector a {@link StatContributor} writes its gear attributes into. The collected instances
 * are folded into the player's attribute snapshot, which is both what the game reads and what
 * the stats screen displays - so a stat node is visible on the stats screen for free.
 */
public interface GodStatSink {
    <T> void add(VaultGearAttribute<T> attribute, T value);

    void add(VaultGearAttributeInstance<?> instance);

    /** A sink that appends to {@code target}, the shape {@code GodNodeAttributeSource} returns. */
    static GodStatSink collecting(List<VaultGearAttributeInstance<?>> target) {
        return new GodStatSink() {
            @Override
            public <T> void add(VaultGearAttribute<T> attribute, T value) {
                target.add(new VaultGearAttributeInstance<>(attribute, value));
            }

            @Override
            public void add(VaultGearAttributeInstance<?> instance) {
                target.add(instance);
            }
        };
    }
}
