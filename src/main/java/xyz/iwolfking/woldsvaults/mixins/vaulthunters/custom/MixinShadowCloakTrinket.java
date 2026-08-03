package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.gear.trinket.effects.ShadowCloakTrinket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.events.HyperVaultEvents;

/**
 * Radar coverage for the Shadow Cloak trinket: its untargetability flows entirely through the
 * two static isInvisible checks (the Targeting override and the damage-side query), so forcing
 * both to false while the player's vault carries the hyper Radar modifier disables the cloak
 * for exactly that vault and self-reverts on exit — unlike the trinket's own
 * forceDisableCloak, whose static cooldown map would leak the disable past the vault.
 */
@Mixin(value = ShadowCloakTrinket.class, remap = false)
public class MixinShadowCloakTrinket {

    @Inject(method = "isInvisible(Lnet/minecraft/world/entity/player/Player;)Z", at = @At("HEAD"), cancellable = true)
    private static void woldsVaults$radarRevealsPlayer(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (player instanceof ServerPlayer serverPlayer && HyperVaultEvents.hasRadar(serverPlayer)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isInvisible(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;)Z", at = @At("HEAD"), cancellable = true)
    private static void woldsVaults$radarRevealsEntity(LivingEntity entity, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof ServerPlayer serverPlayer && HyperVaultEvents.hasRadar(serverPlayer)) {
            cir.setReturnValue(false);
        }
    }
}
