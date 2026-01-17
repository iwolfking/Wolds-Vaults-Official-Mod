package xyz.iwolfking.woldsvaults.objectives.survival;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.world.storage.VirtualWorld;
import xyz.iwolfking.woldsvaults.config.SurvivalObjectiveConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.objectives.SurvivalObjective;
import xyz.iwolfking.woldsvaults.objectives.lib.ObjectiveManager;
import xyz.iwolfking.woldsvaults.objectives.survival.lib.TickTimer;

public class SurvivalChallengeManager extends ObjectiveManager<SurvivalObjective> {
    private static final int TICKS_PER_ACTION = 1200;

    public final TickTimer TIMER = new TickTimer(TICKS_PER_ACTION);

    public SurvivalChallengeManager(Vault vault, VirtualWorld world, SurvivalObjective objective) {
        super(vault, world, objective);
    }


    public void tick() {
        if(TIMER.ready()) {
            ModConfigs.SURVIVAL_OBJECTIVE.SURVIVAL_CHALLENGES.getForLevel(level).flatMap(SurvivalObjectiveConfig.SurvivalChallengeEntry::getRandomChallengeEvent).ifPresent(vaultEvent -> {
                vaultEvent.triggerEvent(vault);
            });
        }
    }
}
