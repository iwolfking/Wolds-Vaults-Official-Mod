package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.core.util.WeightedList;
import iskallia.vault.task.BingoTask;
import iskallia.vault.task.Task;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = BingoTask.Config.class, remap = false)
public interface BingoTaskConfigAccessor {
    /** The tile pools a board draws its tiles from when it populates, keyed by minimum level. */
    @Accessor("tasks")
    Map<Integer, WeightedList<Task>> woldsvaults$getTasks();
}
