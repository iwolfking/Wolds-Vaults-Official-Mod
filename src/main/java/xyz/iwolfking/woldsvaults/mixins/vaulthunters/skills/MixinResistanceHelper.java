package xyz.iwolfking.woldsvaults.mixins.vaulthunters.skills;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.core.event.common.PlayerStatEvent;
import iskallia.vault.util.calc.PlayerStat;
import iskallia.vault.util.calc.ResistanceHelper;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.abilities.ColossusAbility;
import xyz.iwolfking.woldsvaults.init.ModEffects;

import java.util.function.Consumer;

@Mixin(value = ResistanceHelper.class,remap = false)
public class MixinResistanceHelper {

    @WrapOperation(method = "getResistance", at = @At(value = "INVOKE", target = "Liskallia/vault/util/calc/AttributeLimitHelper;getResistanceLimit(Lnet/minecraft/world/entity/LivingEntity;)F"))
    private static float colossusTitanEtchingResistanceIncrease(LivingEntity entity, Operation<Float> original) {
        if(entity.hasEffect(ModEffects.COLOSSUS)) {
            return original.call(entity) + ColossusAbility.ColossusEffect.getColossusResistanceCapIncrease(entity);
        }

        return original.call(entity);
    }

    @WrapOperation(method = "getResistanceUnlimited",
    at = @At(value = "INVOKE", target = "Liskallia/vault/core/event/common/PlayerStatEvent;invoke(Liskallia/vault/util/calc/PlayerStat;Lnet/minecraft/world/entity/LivingEntity;FLjava/util/function/Consumer;)Liskallia/vault/core/event/common/PlayerStatEvent$Data;"))
    private static PlayerStatEvent.Data addColossusResistance(PlayerStatEvent instance, PlayerStat stat, LivingEntity entity, float value, Consumer<PlayerStatEvent.Data> config, Operation<PlayerStatEvent.Data> original) {
        return original.call(instance,stat,entity,value + ColossusAbility.ColossusEffect.getColossusResistance(entity),config);
    }
}
