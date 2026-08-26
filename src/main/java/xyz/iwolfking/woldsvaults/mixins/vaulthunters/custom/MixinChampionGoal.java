package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.entity.ai.ChampionGoal;
import iskallia.vault.world.VaultDifficulty;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.api.util.GameruleHelper;
import xyz.iwolfking.woldsvaults.init.ModGameRules;

@Mixin(value = ChampionGoal.class, remap = false)
public class MixinChampionGoal {
    @WrapOperation(method = "registerTeleportGoal", at = @At(value = "INVOKE", target = "Liskallia/vault/world/VaultDifficulty;shouldChampionRangeAttack()Z"))
    private static boolean controlByGamerule(VaultDifficulty instance, Operation<Boolean> original, @Local(argsOnly = true) Mob mob) {
        if(!GameruleHelper.isEnabled(ModGameRules.ENABLE_TELEPORTING_CHAMPIONS, mob.getLevel())) {
            return false;
        }

        return original.call(instance);
    }
}
