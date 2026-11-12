package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.core.event.common.PlayerStatEvent;
import iskallia.vault.util.calc.PlayerStat;
import iskallia.vault.util.calc.ThornsHelper;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Fixes the base mod's double count of gear thorns damage: {@code getThornsDamageMultiplier} seeds
 * the player-stat event with the gear sum and then adds the result back onto that same sum.
 * Subtracting the seed makes the surrounding {@code +=} behave as an assignment.
 */
@Mixin(value = ThornsHelper.class, remap = false)
public abstract class MixinThornsHelper {
    @WrapOperation(method = "getThornsDamageMultiplier",
            at = @At(value = "INVOKE",
                    target = "Liskallia/vault/core/event/common/PlayerStatEvent;invoke(Liskallia/vault/util/calc/PlayerStat;Lnet/minecraft/world/entity/LivingEntity;F)Liskallia/vault/core/event/common/PlayerStatEvent$Data;"))
    private static PlayerStatEvent.Data woldsvaults$dropEchoedGearThorns(
            PlayerStatEvent instance, PlayerStat stat, LivingEntity entity, float seed,
            Operation<PlayerStatEvent.Data> original) {
        PlayerStatEvent.Data data = original.call(instance, stat, entity, seed);
        data.setValue(data.getValue() - seed);
        return data;
    }
}
