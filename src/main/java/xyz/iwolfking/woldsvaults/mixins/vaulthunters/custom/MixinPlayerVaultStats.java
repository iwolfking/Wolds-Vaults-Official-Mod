package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;


import com.llamalad7.mixinextras.sugar.Local;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import iskallia.vault.config.VaultLevelsConfig;
import iskallia.vault.init.ModConfigs;
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
import xyz.iwolfking.woldsvaults.api.data.discovery.DiscoveredRecipesData;
import xyz.iwolfking.woldsvaults.integration.ftbquests.tasks.VaultLevelTask;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.iwolfking.woldsvaults.integration.ftbquests.tasks.api.WoldFTBQuestsHelper;

import java.util.List;
import java.util.UUID;

@Mixin(value = PlayerVaultStats.class, remap = false)
public abstract class MixinPlayerVaultStats {
    @Shadow public abstract int getVaultLevel();

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
}
