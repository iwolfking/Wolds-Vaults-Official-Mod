package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.event.GreedAssassinSpawnHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.medallions.assassins.GreedAssassinKills;
import xyz.iwolfking.woldsvaults.medallions.assassins.GreedAssassinSpawner;

/** Cancels the assassin spawn roll and kill payout; both are re-implemented in the medallion package. */
@Mixin(value = GreedAssassinSpawnHandler.class, remap = false)
public class MixinGreedAssassinSpawnHandler {
    @Inject(method = "trySpawnGreedAssassin", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$medallionGatedSpawns(LivingEntity killed, LivingDeathEvent event, ServerLevel serverLevel, CallbackInfo ci) {
        ci.cancel();
        GreedAssassinSpawner.trySpawn(killed, event, serverLevel);
    }

    @Inject(method = "handleGreedAssassinKill", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$medallionKillRewards(LivingEntity killed, LivingDeathEvent event, ServerLevel serverLevel, CallbackInfo ci) {
        ci.cancel();
        GreedAssassinKills.handleKill(killed, event, serverLevel);
    }
}
