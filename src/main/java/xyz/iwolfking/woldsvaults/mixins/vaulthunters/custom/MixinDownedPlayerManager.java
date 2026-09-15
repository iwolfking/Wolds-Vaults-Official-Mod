package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.world.data.DownedPlayerManager;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.data.WoldConstants;
import xyz.iwolfking.woldsvaults.api.util.HealthReductionHelper;

@Mixin(value = DownedPlayerManager.class, remap = false)
public class MixinDownedPlayerManager {
    @Inject(method = "completeRevive", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;setForcedPose(Lnet/minecraft/world/entity/Pose;)V"))
    private static void restoreMaxHealthFromSecondChanceOnRevive(ServerPlayer rescuer, ServerPlayer downedPlayer, CallbackInfo ci) {
        HealthReductionHelper.restoreReduction(downedPlayer, WoldConstants.SECOND_CHANCE_HEALTH_REDUCTION_UUID);
    }
}
