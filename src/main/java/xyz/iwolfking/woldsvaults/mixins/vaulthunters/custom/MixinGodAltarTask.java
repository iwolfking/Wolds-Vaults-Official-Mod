package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.task.GodAltarTask;
import iskallia.vault.task.Task;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = GodAltarTask.class, remap = false)
public abstract class MixinGodAltarTask extends Task {

}
