package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.snapshot.AttributeSnapshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = AttributeSnapshot.AttributeValue.class, remap = false)
public interface AttributeSnapshotValueAccessor<T, V> {
    @Accessor("cachedValues")
    List<T> getCachedValues();
}
