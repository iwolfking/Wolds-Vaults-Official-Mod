package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.core.vault.ScheduledModifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = ScheduledModifiers.class, remap = false)
public interface ScheduledModifiersAccessor {
    @Accessor
    List<ScheduledModifiers.Entry> getCache();
}
