package xyz.iwolfking.woldsvaults.mixins.vaulthunters.greed;

import iskallia.vault.skill.base.SkillBonusTierCache;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Cancels {@code addGreedBonuses}, so greed nodes contribute no ability bonus tiers. */
@Mixin(value = SkillBonusTierCache.PlayerCache.class, remap = false)
public abstract class MixinSkillBonusTierCacheGreedNodes {
    @Inject(method = "addGreedBonuses(Lnet/minecraft/server/level/ServerPlayer;)V", at = @At("HEAD"), cancellable = true)
    private void woldsvaults$skipGreedAbilityTiers(ServerPlayer player, CallbackInfo ci) {
        ci.cancel();
    }
}
