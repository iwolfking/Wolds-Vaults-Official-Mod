package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.skill.ability.effect.ArcaneRailAbility;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.modifiers.arcane.ArcaneLasers;

/**
 * Arcane Rail drawn as a laser: Vault Hunters' particle trail is turned off, and after each bolt a thick lingering laser
 * is sent to nearby clients ({@link ArcaneLasers}).
 */
@Mixin(value = ArcaneRailAbility.class, remap = false)
public abstract class MixinArcaneRailAbility {
    @Redirect(method = "fireBolt", at = @At(value = "INVOKE", remap = true,
            target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"))
    private int woldsvaults$noParticleTrail(ServerLevel level, ParticleOptions particle, double x, double y, double z, int count,
                                             double dx, double dy, double dz, double speed) {
        return 0;
    }

    @Inject(method = "fireBolt", at = @At("RETURN"))
    private void woldsvaults$laser(ServerPlayer player, CallbackInfo ci) {
        ArcaneRailAbility self = (ArcaneRailAbility) (Object) this;
        ArcaneLasers.railFired(player, self.getPercentAbilityPowerDealtMin(), self.getPercentAbilityPowerDealtMax());
    }
}
