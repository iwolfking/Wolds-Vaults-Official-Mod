package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.config.entry.LevelEntryList;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.task.BingoTask;
import iskallia.vault.task.ConfiguredTask;
import iskallia.vault.task.Task;
import iskallia.vault.task.TaskContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.objectives.BallisticBingoObjective;

import java.util.Iterator;

@Mixin(value = BingoTask.class, remap = false)
public abstract class MixinBingoTask extends ConfiguredTask<ConfiguredTask.Config> implements LevelEntryList.ILevelEntry {

    @Inject(method = "onComplete", at = @At("HEAD"))
    private void onComplete(Task task, TaskContext context, CallbackInfo ci) {
        Vault vault = context.getVault();
        if(vault == null)  {
            return;
        }

        BallisticBingoObjective obj = null;
        for(Listener listener:  vault.get(Vault.LISTENERS).getAll()) {
            Iterator<Objective> objIterator = listener.getObjectives(vault);
            while(objIterator.hasNext()) {
                if(objIterator.next() instanceof BallisticBingoObjective ballisticBingoObjective) {
                    obj = ballisticBingoObjective;
                    break;
                }
            }
            if(obj != null) {
                break;
            }
        }
        if(obj == null) {
            return;
        }

        obj.addBingoTaskModifier(vault, "bingo_task_modifiers");
        obj.addBingoTaskModifier(vault, "bingo_task_modifiers_bad");
    }
}
