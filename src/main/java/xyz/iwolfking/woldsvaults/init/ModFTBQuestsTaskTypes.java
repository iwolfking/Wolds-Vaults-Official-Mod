package xyz.iwolfking.woldsvaults.init;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.integration.ftbquests.tasks.VaultLevelTask;

public class ModFTBQuestsTaskTypes {
    public static TaskType VAULT_LEVEL;

    public static void init() {
        VAULT_LEVEL = TaskTypes.register(new ResourceLocation("woldsvaults", "vault_level"), VaultLevelTask::new, () -> Icon.getIcon("minecraft:item/gold_ingot"));
    }

}
