package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.UUID;

@Mixin(value = AttributeSnapshotHelper.class, remap = false)
public interface AttributeSnapshotHelperAccessor {
    @Accessor("playerSnapshots")
    Map<UUID, AttributeSnapshot> getPlayerSnapshots();
}
