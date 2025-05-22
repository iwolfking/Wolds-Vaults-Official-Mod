package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.block.PylonBlock;
import iskallia.vault.config.TemporalShardConfig;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.tree.ExpertiseTree;
import iskallia.vault.world.data.PlayerExpertisesData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.woldsvaults.expertises.PylonPilfererExpertise;

@Mixin(value = PylonBlock.class, remap = false)
public class MixinPylonBlock {
    @Redirect(method = "playerDestroy", at = @At(value = "INVOKE", target = "Liskallia/vault/config/TemporalShardConfig;getDropChance()F", remap = false), remap = true)
    private float modifyChance(TemporalShardConfig instance, @Local(argsOnly = true) Player player, @Local(argsOnly = true) Level level) {
        float bonusChance = 0.0F;
        if(player instanceof ServerPlayer serverPlayer) {
            ExpertiseTree tree = PlayerExpertisesData.get(serverPlayer.getLevel()).getExpertises(player);
            if(tree == null) {
                return instance.getDropChance();
            }

            for(Skill skill : tree.skills) {
                if(skill instanceof PylonPilfererExpertise pylonPilfererExpertise) {
                    bonusChance += pylonPilfererExpertise.getChanceIncrease();
                }
            }


        }

        return instance.getDropChance() + bonusChance;
    }

    @Redirect(method = "playerDestroy", at = @At(value = "INVOKE", target = "Liskallia/vault/config/TemporalShardConfig;getUberChance()F", remap = false), remap = true)
    private float modifyUberChance(TemporalShardConfig instance, @Local(argsOnly = true) Player player, @Local(argsOnly = true) Level level) {
        float bonusChance = 0.0F;
        if(player instanceof ServerPlayer serverPlayer) {
            ExpertiseTree tree = PlayerExpertisesData.get(serverPlayer.getLevel()).getExpertises(player);
            if(tree == null) {
                return instance.getUberChance();
            }

            for(Skill skill : tree.skills) {
                if(skill instanceof PylonPilfererExpertise pylonPilfererExpertise) {
                    bonusChance += pylonPilfererExpertise.getChanceIncrease();
                }
            }


        }

        return instance.getUberChance() + bonusChance;
    }
}
