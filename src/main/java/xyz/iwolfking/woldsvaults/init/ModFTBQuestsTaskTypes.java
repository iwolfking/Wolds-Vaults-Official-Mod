package xyz.iwolfking.woldsvaults.init;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.icon.ImageIcon;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.integration.ftbquests.tasks.CompleteBountyTask;
import xyz.iwolfking.woldsvaults.integration.ftbquests.tasks.EnterVaultTask;
import xyz.iwolfking.woldsvaults.integration.ftbquests.tasks.VaultLevelTask;

public class ModFTBQuestsTaskTypes {
    public static TaskType VAULT_LEVEL;
    public static TaskType ENTER_VAULT;
    //public static TaskType COMPLETE_VAULT;
    public static TaskType COMPLETE_BOUNTY;

    public static void init() {
        VAULT_LEVEL = TaskTypes.register(new ResourceLocation("woldsvaults", "vault_level"), VaultLevelTask::new, () -> Icon.getIcon("the_vault:item/spicy_hearty_burger"));
        ENTER_VAULT = TaskTypes.register(new ResourceLocation("woldsvaults", "enter_vault"), EnterVaultTask::new, () -> Icon.getIcon("the_vault:item/compass/vault_compass_00"));
        COMPLETE_BOUNTY = TaskTypes.register(new ResourceLocation("woldsvaults", "complete_bounty"), CompleteBountyTask::new, () -> Icon.getIcon("the_vault:item/lost_bounty"));
    }

}
