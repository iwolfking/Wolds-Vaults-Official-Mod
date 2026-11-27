package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.task.counter.TargetTaskCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = TargetTaskCounter.Config.class, remap = false)
public interface TargetTaskCounterConfigAccessor {
    @Accessor("target")
    void woldsvaults$setTarget(Object target);
}
