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
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.WoldVaultUtils;
import xyz.iwolfking.woldsvaults.objectives.BallisticBingoObjective;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultObjective;

@Mixin(value = BingoTask.class, remap = false)
public abstract class MixinBingoTask extends ConfiguredTask<ConfiguredTask.Config> implements LevelEntryList.ILevelEntry {

    /**
     * Hyper vaults: a completed bingo line must stay reward-free. onBingo attaches a reward
     * task rolled from the bingo config pools — the first-line pool gives the Bingo! loot
     * buffs, later lines can roll crate tiers.
     */
    @Inject(method = "onBingo", at = @At("HEAD"), cancellable = true)
    private void woldsVaults$skipLineRewardsInHyperVaults(TaskContext context, CallbackInfo ci) {
        Vault vault = context.getVault();
        if (vault != null && !vault.get(Vault.OBJECTIVES).getAll(HyperVaultObjective.class).isEmpty()) {
            WoldsVaults.LOGGER.info("Suppressed a bingo-line reward — Hyper lines award nothing.");
            ci.cancel();
        }
    }

    @Inject(method = "onComplete", at = @At("HEAD"))
    private void onComplete(Task task, TaskContext context, CallbackInfo ci) {
        Vault vault = context.getVault();
        if(vault == null)  {
            return;
        }

        BallisticBingoObjective objective = WoldVaultUtils.getObjective(vault, BallisticBingoObjective.class);

        if(objective != null) {
            objective.addBingoTaskModifier(vault, "bingo_task_modifiers");
            objective.addBingoTaskModifier(vault, "bingo_task_modifiers_bad");
        }

    }
}
