package xyz.iwolfking.woldsvaults.integration.ftbquests.tasks.api;

import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Unique;
import xyz.iwolfking.woldsvaults.integration.ftbquests.tasks.VaultLevelTask;

import java.util.List;

public class WoldFTBQuestsHelper {
    private static List<VaultLevelTask> woldsVaults$levelTasks = null;


    @Unique
    public static void progressVaultLevelTasks(Player player, int newLevel) {
        if (woldsVaults$levelTasks == null) {
            woldsVaults$levelTasks = ServerQuestFile.INSTANCE.collect(VaultLevelTask.class);
        }

        if (woldsVaults$levelTasks.isEmpty()) {
            return;
        }

        TeamData data = ServerQuestFile.INSTANCE.getData(player);

        for (VaultLevelTask task : woldsVaults$levelTasks) {
            if (data.getProgress(task) < task.getMaxProgress() && data.canStartTasks(task.quest)) {
                data.setProgress(task, newLevel);
            }
        }
    }
}
