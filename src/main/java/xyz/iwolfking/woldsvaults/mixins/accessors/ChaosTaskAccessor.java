package xyz.iwolfking.woldsvaults.mixins.accessors;

import iskallia.vault.task.ChaosTask;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChaosTask.class)
public interface ChaosTaskAccessor {
    @Accessor
    int getRequiredCompletions();
}
