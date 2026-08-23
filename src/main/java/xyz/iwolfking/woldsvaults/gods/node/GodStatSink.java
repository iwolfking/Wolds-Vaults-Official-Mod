package xyz.iwolfking.woldsvaults.gods.node;

import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;

import java.util.List;

/** Collector a {@link StatContributor} writes into; the instances are folded into the attribute snapshot. */
public interface GodStatSink {
    <T> void add(VaultGearAttribute<T> attribute, T value);

    void add(VaultGearAttributeInstance<?> instance);

    /** A sink that appends to {@code target}. */
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
