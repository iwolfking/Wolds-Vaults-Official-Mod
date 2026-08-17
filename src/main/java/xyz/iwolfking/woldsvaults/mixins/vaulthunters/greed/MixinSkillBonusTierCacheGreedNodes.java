package xyz.iwolfking.woldsvaults.mixins.vaulthunters.greed;

import iskallia.vault.skill.base.SkillBonusTierCache;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Design decision D32: the retired greed tree grants no ability tiers. In 3.21.6 the bonus tier a
 * skill receives is assembled once per player in {@code SkillBonusTierCache.PlayerCache}, where
 * {@code addGreedBonuses} folds in every unlocked {@code greed_ability_upgrade} node; cancelling it
 * leaves the greed contribution empty while gear, talent and prestige contributions are untouched.
 *
 * <p>The shipped pack config declares no {@code greed_ability_upgrade} nodes, so this is closing
 * the last path a re-enabled node could take rather than removing a live buff.
 */
@Mixin(value = SkillBonusTierCache.PlayerCache.class, remap = false)
public abstract class MixinSkillBonusTierCacheGreedNodes {
    @Inject(method = "addGreedBonuses(Lnet/minecraft/server/level/ServerPlayer;)V", at = @At("HEAD"), cancellable = true)
    private void woldsvaults$skipGreedAbilityTiers(ServerPlayer player, CallbackInfo ci) {
        ci.cancel();
    }
}
