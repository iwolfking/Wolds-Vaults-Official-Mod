package xyz.iwolfking.woldsvaults.api.core.vault_events.impl.tasks;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.Objective;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.VaultEventTask;
import xyz.iwolfking.woldsvaults.api.util.WoldVaultUtils;
import xyz.iwolfking.woldsvaults.objectives.SurvivalObjective;

import java.util.function.Supplier;

public class SurvivalWaveAdjustmentTask implements VaultEventTask {

    private final int waveAdjustment;

    public SurvivalWaveAdjustmentTask(int waveAdjustment) {
        this.waveAdjustment = waveAdjustment;
    }

    @Override
    public void performTask(Supplier<BlockPos> pos, ServerPlayer player, Vault vault) {
        Objective objective = WoldVaultUtils.getObjective(vault, SurvivalObjective.class);
        if(objective == null) {
            return;
        }

        if(objective instanceof SurvivalObjective survivalObjective) {
            survivalObjective.adjustWave(vault, waveAdjustment);
        }
    }
}
