package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;


import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.event.event.VaultLevelUpEvent;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.skill.PlayerVaultStats;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.expertise.type.ExperiencedExpertise;
import iskallia.vault.skill.tree.ExpertiseTree;
import iskallia.vault.util.NetcodeUtils;
import iskallia.vault.world.data.PlayerExpertisesData;
import iskallia.vault.world.data.PlayerGreedData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.loading.FMLEnvironment;
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

    @Shadow private int unspentPrestigePoints;

    /**
     * @author iwolfking
     * @reason
     */
    @Overwrite
    public void addVaultExp(MinecraftServer server, int exp) {
        this.exp = Math.max(this.exp, 0);
        this.exp += (int) ((float) exp * ModConfigs.LEVELS_META.getExpMultiplier());
        ServerPlayer player = server.getPlayerList().getPlayer(this.uuid);
        if(player == null) {
            return;
        }
        ExpertiseTree expertises = PlayerExpertisesData.get(player.getLevel()).getExpertises(player);
        float increase = 0.0F;
        for (ExperiencedExpertise expertise1 : expertises.getAll(ExperiencedExpertise.class, Skill::isUnlocked)) {
            increase += expertise1.getIncreasedExpPercentage();
        }
        this.exp = (int) (this.exp + (this.exp * increase));
        int maxLevel = ModConfigs.LEVELS_META.getMaxLevel();
        if (this.getVaultLevel() >= 100 || this.getVaultLevel() < maxLevel) {
            int initialLevel = this.vaultLevel;
            if (initialLevel >= 100 && !PlayerGreedData.get(server).get(this.uuid).hasCompletedHerald() && FMLEnvironment.production) {
                this.exp = 0;
                this.vaultLevel = 100;
            } else {
                int neededExp;
                for (; this.exp >= (neededExp = this.getExpNeededToNextLevel()); this.exp -= neededExp) {
                    ++this.vaultLevel;
                    if (this.vaultLevel > 100) {
                        ++this.unspentPrestigePoints;
                    } else {
                        ++this.unspentSkillPoints;
                        if (this.vaultLevel % 5 == 0) {
                            ++this.unspentExpertisePoints;
                        }
                    }
                }

                if (this.vaultLevel > initialLevel) {
                    NetcodeUtils.runIfPresent(server, this.uuid, this::fancyLevelUpEffects);
                    player.refreshTabListName();

                    CommonEvents.VAULT_LEVEL_UP.invoke(player, exp, initialLevel, this.vaultLevel);
                }

                this.sync(server);
            }
        }
    }
}
