package xyz.iwolfking.woldsvaults.mixins.vaulthunters.skills;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.core.event.common.PlayerStatEvent;
import iskallia.vault.util.calc.AreaOfEffectHelper;
import iskallia.vault.util.calc.PlayerStat;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.abilities.ColossusAbility;

@Mixin(value = AreaOfEffectHelper.class, remap = false)
public class MixinAreaOfEffectHelper {
    @WrapOperation(method = "getAreaOfEffectUnlimited",
            at = @At(value = "INVOKE", target = "Liskallia/vault/core/event/common/PlayerStatEvent;invoke(Liskallia/vault/util/calc/PlayerStat;Lnet/minecraft/world/entity/LivingEntity;F)Liskallia/vault/core/event/common/PlayerStatEvent$Data;"))
    private static PlayerStatEvent.Data addColossusAOE(PlayerStatEvent instance, PlayerStat stat, LivingEntity entity, float value, Operation<PlayerStatEvent.Data> original) {
        return original.call(instance,stat,entity,value + ColossusAbility.ColossusEffect.getColossusAOE(entity));
    }
}
