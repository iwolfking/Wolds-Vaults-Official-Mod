package xyz.iwolfking.woldsvaults.objectives.survival;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.world.storage.VirtualWorld;
import xyz.iwolfking.woldsvaults.config.SurvivalObjectiveConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.objectives.SurvivalObjective;
import xyz.iwolfking.woldsvaults.objectives.lib.ObjectiveManager;

public class SurvivalBonusManager extends ObjectiveManager<SurvivalObjective> {
    public static final int TICKS_PER_ACTION = 3000;

    public SurvivalBonusManager(Vault vault, VirtualWorld world, SurvivalObjective objective) {
        super(vault, world, objective);
    }


    public void tick() {
        if(objective.get(SurvivalObjective.TIME_SURVIVED) >= TICKS_PER_ACTION) {
            ModConfigs.SURVIVAL_OBJECTIVE.SURVIVAL_REWARDS.getForLevel(level).flatMap(SurvivalObjectiveConfig.SurvivalRewardEntry::getRandomRewardEvent).ifPresent(vaultEvent -> {
                vaultEvent.triggerEvent(vault);
                objective.set(SurvivalObjective.TIME_SURVIVED, 0);
            });
        }
    }
}
