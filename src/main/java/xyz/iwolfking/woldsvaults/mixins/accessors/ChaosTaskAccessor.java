package xyz.iwolfking.woldsvaults.mixins.accessors;

import iskallia.vault.task.ChaosTask;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ChaosTask.class, remap = false)
public interface ChaosTaskAccessor {
    @Accessor
    int getRequiredCompletions();
}
