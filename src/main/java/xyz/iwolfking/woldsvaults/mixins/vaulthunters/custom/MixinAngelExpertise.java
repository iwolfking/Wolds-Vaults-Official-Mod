package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.skill.base.LearnableSkill;
import iskallia.vault.skill.base.TickingSkill;
import iskallia.vault.skill.expertise.type.AngelExpertise;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.woldsvaults.init.ModGameRules;

import java.util.Optional;

@Mixin(value = AngelExpertise.class, remap = false)
public abstract class MixinAngelExpertise extends LearnableSkill implements TickingSkill {
    @Override
    public boolean isUnlocked() {
        return true;
    }

    @Redirect(method = "lambda$onTick$1", at = @At(value = "INVOKE", target = "Ljava/util/Optional;isPresent()Z"))
    private boolean checkGamerule(Optional<Vault> instance, @Local(argsOnly = true) Player player) {
        if(instance.isPresent() && player.getLevel().getGameRules().getBoolean(ModGameRules.ALLOW_FLIGHT_IN_VAULTS)) {
            return false;
        }

        return instance.isPresent();
    }
}
