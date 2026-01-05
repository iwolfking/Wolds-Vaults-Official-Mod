package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.config.entry.LevelEntryList;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.task.BingoTask;
import iskallia.vault.task.ConfiguredTask;
import iskallia.vault.task.Task;
import iskallia.vault.task.TaskContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.util.WoldVaultUtils;
import xyz.iwolfking.woldsvaults.objectives.BallisticBingoObjective;

@Mixin(value = BingoTask.class, remap = false)
public abstract class MixinBingoTask extends ConfiguredTask<ConfiguredTask.Config> implements LevelEntryList.ILevelEntry {

    @Inject(method = "onComplete", at = @At("HEAD"))
    private void onComplete(Task task, TaskContext context, CallbackInfo ci) {
        Vault vault = context.getVault();
        if(vault == null)  {
            return;
        }

        Objective objective = WoldVaultUtils.getObjective(vault, BallisticBingoObjective.class);

        if(objective instanceof BallisticBingoObjective ballisticBingoObjective) {
            ballisticBingoObjective.addBingoTaskModifier(vault, "bingo_task_modifiers");
            ballisticBingoObjective.addBingoTaskModifier(vault, "bingo_task_modifiers_bad");
        }

    }
}
