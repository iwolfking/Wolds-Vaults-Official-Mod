package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;


import com.llamalad7.mixinextras.sugar.Local;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import iskallia.vault.config.VaultLevelsConfig;
import iskallia.vault.skill.PlayerVaultStats;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.expertise.type.ExperiencedExpertise;
import iskallia.vault.skill.tree.ExpertiseTree;
import iskallia.vault.world.data.PlayerExpertisesData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.data.discovery.DiscoveredRecipesData;
import xyz.iwolfking.woldsvaults.integration.ftbquests.tasks.VaultLevelTask;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.UUID;

@Mixin(value = PlayerVaultStats.class, remap = false)
public abstract class MixinPlayerVaultStats {
    @Shadow public abstract int getVaultLevel();

    @Shadow private int exp;
    @Shadow private int vaultLevel;
    @Shadow @Final private UUID uuid;

    @Redirect(method = "addVaultExp", at = @At(value = "INVOKE", target = "Liskallia/vault/config/VaultLevelsConfig;getExpMultiplier()F"))
    private float addExperiencedExpertiseMultiplier(VaultLevelsConfig instance, @Local MinecraftServer server) {
        ServerPlayer player = server.getPlayerList().getPlayer(this.uuid);
        if(player == null) {
            return instance.getExpMultiplier();
        }

        ExpertiseTree expertises = PlayerExpertisesData.get(player.getLevel()).getExpertises(player);
        float increase = 0.0F;
        for (ExperiencedExpertise expertise1 : expertises.getAll(ExperiencedExpertise.class, Skill::isUnlocked)) {
            increase += expertise1.getIncreasedExpPercentage();
        }

        return instance.getExpMultiplier() + increase;
    }

    @Inject(method = "addVaultExp", at = @At(value = "INVOKE", target = "Liskallia/vault/core/event/common/VaultLevelUpEvent;invoke(Lnet/minecraft/server/level/ServerPlayer;III)Liskallia/vault/core/event/common/VaultLevelUpEvent$Data;"))
    private void progressFTBQuestsTasks(MinecraftServer server, int exp, CallbackInfo ci, @Local ServerPlayer player) {
        if(player.getServer() != null && this.vaultLevel >= 75 && !DiscoveredRecipesData.get(player.getServer()).hasDiscovered(player, WoldsVaults.id("standard_trinket_pouch"))) {
            DiscoveredRecipesData.get(player.getServer()).discoverRecipeAndBroadcast(WoldsVaults.id("standard_trinket_pouch"), player);
        }

        woldsVaults$vaultLevelTaskProgress(player, this.vaultLevel);
    }


    @Unique
    private List<VaultLevelTask> woldsVaults$levelTasks = null;


    @Unique
    public void woldsVaults$vaultLevelTaskProgress(Player player, int newLevel) {
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
