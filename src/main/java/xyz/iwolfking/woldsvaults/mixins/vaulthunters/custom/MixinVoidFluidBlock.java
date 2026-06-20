package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.fluid.block.VoidFluidBlock;
import iskallia.vault.world.data.DownedPlayerManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = VoidFluidBlock.class, remap = false)
public class MixinVoidFluidBlock {
    @WrapOperation(method = "affectPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z", ordinal = 0))
    private static boolean cancelTimeAccelerationIfDowned(ServerPlayer instance, MobEffectInstance mobEffectInstance, Operation<Boolean> original) {
        if(DownedPlayerManager.isPlayerDowned(instance)) {
            return false;
        }

        return original.call(instance, mobEffectInstance);
    }
}
