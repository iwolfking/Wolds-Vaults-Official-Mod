package xyz.iwolfking.woldsvaults.integration.ftbquests.tasks;

import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.BooleanTask;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.init.ModFTBQuestsTaskTypes;

public class EnterVaultTask extends BooleanTask {
    public EnterVaultTask(Quest q) {
        super(q);
    }

    @Override
    public boolean canSubmit(TeamData teamData, ServerPlayer serverPlayer) {
        return !serverPlayer.isSpectator() && ServerVaults.get(serverPlayer.getLevel()).isPresent();
    }

    @Override
    public TaskType getType() {
        return ModFTBQuestsTaskTypes.ENTER_VAULT;
    }
}
