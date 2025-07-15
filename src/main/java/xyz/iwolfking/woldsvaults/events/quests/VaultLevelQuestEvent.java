package xyz.iwolfking.woldsvaults.events.quests;

import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import iskallia.vault.event.event.VaultLevelUpEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.integration.ftbquests.tasks.VaultLevelTask;

import java.util.List;

@Mod.EventBusSubscriber(
        modid = WoldsVaults.MOD_ID
)
public class VaultLevelQuestEvent {
    private List<VaultLevelTask> levelTasks = null;


    @SubscribeEvent
    public void vaultLevel(VaultLevelUpEvent event) {
        ServerPlayer player = event.getPlayer();
            if (levelTasks == null) {
                levelTasks = ServerQuestFile.INSTANCE.collect(VaultLevelTask.class);
            }

            if (levelTasks.isEmpty()) {
                return;
            }

            TeamData data = ServerQuestFile.INSTANCE.getData(player);

            for (VaultLevelTask task : levelTasks) {
                if (data.getProgress(task) < task.getMaxProgress() && data.canStartTasks(task.quest)) {
                    task.setValue(event.getNewLevel());
                }
            }
        }

}
