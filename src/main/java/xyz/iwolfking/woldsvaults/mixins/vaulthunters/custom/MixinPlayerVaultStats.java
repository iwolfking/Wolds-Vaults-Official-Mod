package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;


import iskallia.vault.event.event.VaultLevelUpEvent;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.skill.PlayerVaultStats;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.expertise.type.ExperiencedExpertise;
import iskallia.vault.skill.tree.ExpertiseTree;
import iskallia.vault.util.NetcodeUtils;
import iskallia.vault.world.data.PlayerExpertisesData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;

@Mixin(value = PlayerVaultStats.class, remap = false)
public abstract class MixinPlayerVaultStats {
    @Shadow public abstract int getVaultLevel();

    @Shadow private int exp;
    @Shadow private int vaultLevel;

    @Shadow public abstract int getExpNeededToNextLevel();

    @Shadow private int unspentSkillPoints;
    @Shadow private int unspentExpertisePoints;
    @Shadow @Final private UUID uuid;

    @Shadow protected abstract void fancyLevelUpEffects(ServerPlayer player);

    @Shadow public abstract void sync(MinecraftServer server);

    /**
     * @author iwolfking
     * @reason Modify how expertise points are calculated after level 100, Experienced Expertise boosts XP
     */
    @Overwrite
    public void addVaultExp(MinecraftServer server, int exp) {
        int maxLevel = ModConfigs.LEVELS_META.getMaxLevel();
        if (this.getVaultLevel() < maxLevel) {
            ServerPlayer player = server.getPlayerList().getPlayer(this.uuid);
            if(player == null) {
                return;
            }
            this.exp = Math.max(this.exp, 0);
            ExpertiseTree expertises = PlayerExpertisesData.get(player.getLevel()).getExpertises(player);
            float increase = 0.0F;
            for (ExperiencedExpertise expertise1 : expertises.getAll(ExperiencedExpertise.class, Skill::isUnlocked)) {
                increase += expertise1.getIncreasedExpPercentage();
            }
            float multipler = ModConfigs.LEVELS_META.getExpMultiplier();
            this.exp += (int) (exp * (1.0F + increase) * multipler);
            int initialLevel = this.vaultLevel;

            int neededExp;
            while (this.exp >= (neededExp = this.getExpNeededToNextLevel())) {
                ++this.vaultLevel;
                ++this.unspentSkillPoints;
                if (this.vaultLevel % 5 == 0) {
                    ++this.unspentExpertisePoints;
                }

                this.exp -= neededExp;
                if (this.getVaultLevel() >= maxLevel) {
                    this.vaultLevel = maxLevel;
                    this.exp = 0;
                    break;
                }
            }

            if (this.vaultLevel > initialLevel) {
                NetcodeUtils.runIfPresent(server, this.uuid, this::fancyLevelUpEffects);
                player.refreshTabListName();

                MinecraftForge.EVENT_BUS.post(new VaultLevelUpEvent(player, exp, initialLevel, this.vaultLevel));
            }

            this.sync(server);
        }
    }
}
