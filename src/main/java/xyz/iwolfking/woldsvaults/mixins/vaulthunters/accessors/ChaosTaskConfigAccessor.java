package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.task.ChaosTask;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ChaosTask.Config.class, remap = false)
public interface ChaosTaskConfigAccessor {
    @Accessor("minTasks")
    int woldsvaults$getMinTasks();

    @Accessor("maxTasks")
    int woldsvaults$getMaxTasks();
}
