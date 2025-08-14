package xyz.iwolfking.woldsvaults.events;

import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import iskallia.vault.VaultMod;
import iskallia.vault.core.data.key.ThemeKey;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultRegistry;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.core.vault.WorldManager;
import iskallia.vault.core.vault.player.Completion;
import iskallia.vault.core.vault.stat.StatCollector;
import iskallia.vault.core.vault.stat.StatsCollector;
import iskallia.vault.event.event.BountyCompleteEvent;
import iskallia.vault.event.event.VaultJoinEvent;
import iskallia.vault.event.event.VaultLeaveEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.spongepowered.asm.mixin.Unique;
import virtuoel.pehkui.api.ScaleTypes;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.data.discovery.DiscoveredRecipesData;
import xyz.iwolfking.woldsvaults.data.discovery.DiscoveredThemesData;
import xyz.iwolfking.woldsvaults.integration.ftbquests.tasks.CompleteBountyTask;
import xyz.iwolfking.woldsvaults.integration.ftbquests.tasks.EnterVaultTask;
import xyz.iwolfking.woldsvaults.integration.ftbquests.tasks.VaultLevelTask;
import xyz.iwolfking.woldsvaults.objectives.CorruptedObjective;

import java.util.List;

@Mod.EventBusSubscriber(
        modid = WoldsVaults.MOD_ID
)
public class VaultEvents {
    @SubscribeEvent
    public static void onVaultJoin(VaultJoinEvent event) {

        ResourceLocation theme = event.getVault().get(Vault.WORLD).get(WorldManager.THEME);
        ThemeKey themeKey = VaultRegistry.THEME.getKey(theme);

        if(!event.getPlayers().isEmpty()) {
            for(ServerPlayer joinedPlayer : event.getPlayers()) {
                ScaleTypes.BASE.getScaleData(joinedPlayer).setScale(1.0F);
                if(!DiscoveredThemesData.get(joinedPlayer.server).hasDiscovered(joinedPlayer, theme) && themeKey != null) {
                    DiscoveredThemesData.get(joinedPlayer.server).discoverThemeAndBroadcast(themeKey, joinedPlayer);
                }
                progressVaultEnterTasks(joinedPlayer);
            }
        }

    }

    @SubscribeEvent
    public static void onVaultComplete(VaultLeaveEvent event) {
        if(event.getPlayer() != null && event.getVault() != null) {
            Vault vault = event.getVault();
            ServerPlayer player = event.getPlayer();
            if(VaultUtils.hasObjective(vault, CorruptedObjective.class)) {
                if(vault.has(Vault.STATS)) {
                    StatsCollector statsCollector = vault.get(Vault.STATS);
                    StatCollector statCollector = statsCollector.get(player.getUUID());
                    if(statCollector.getCompletion().equals(Completion.COMPLETED)) {
                        if(!DiscoveredRecipesData.get(player.getLevel()).hasDiscovered(player, WoldsVaults.id("shattering_jewel"))) {
                            DiscoveredRecipesData.get(player.getLevel()).discoverRecipeAndBroadcast(WoldsVaults.id("shattering_jewel"), player);
                        }
                    }
                }
            }
        }
    }

    private static List<CompleteBountyTask> completeBountyTasks = null;


    @SubscribeEvent
    public static void onBountyComplete(BountyCompleteEvent event) {
        Player player = event.getPlayer();
        if (completeBountyTasks == null) {
            completeBountyTasks = ServerQuestFile.INSTANCE.collect(CompleteBountyTask.class);
        }

        if (completeBountyTasks.isEmpty()) {
            return;
        }

        TeamData data = ServerQuestFile.INSTANCE.getData(player);

        for (CompleteBountyTask task : completeBountyTasks) {
            if (data.getProgress(task) < task.getMaxProgress() && data.canStartTasks(task.quest)) {
                if(task.getTaskType().equals(VaultMod.id("any")) || task.getTaskType() == null) {
                    data.setProgress(task, 1L);
                }
                else if(task.getTaskType().equals(event.getTask().getTaskType())) {
                    data.setProgress(task, 1L);
                }
            }
        }
    }

    private static List<EnterVaultTask> enterVaultTasks = null;


    @Unique
    public static void progressVaultEnterTasks(Player player) {
        if (enterVaultTasks == null) {
            enterVaultTasks = ServerQuestFile.INSTANCE.collect(EnterVaultTask.class);
        }

        if (enterVaultTasks.isEmpty()) {
            return;
        }

        TeamData data = ServerQuestFile.INSTANCE.getData(player);

        for (EnterVaultTask task : enterVaultTasks) {
            if (data.getProgress(task) < task.getMaxProgress() && data.canStartTasks(task.quest)) {
                data.setProgress(task, 1L);
            }
        }
    }
}
