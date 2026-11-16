package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.skill.ability.effect.ArcaneAbility;
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
 * Arcane drawn as a laser: Vault Hunters' own particle trail is turned off, and after each ray the beam is sent to
 * nearby clients as a laser ({@link ArcaneLasers}).
 */
@Mixin(value = ArcaneAbility.class, remap = false)
public abstract class MixinArcaneAbility {
    @Redirect(method = "fireRay", at = @At(value = "INVOKE", remap = true,
            target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"))
    private int woldsvaults$noParticleTrail(ServerLevel level, ParticleOptions particle, double x, double y, double z, int count,
                                             double dx, double dy, double dz, double speed) {
        return 0;
    }

    @Inject(method = "fireRay", at = @At("RETURN"))
    private void woldsvaults$laser(ServerPlayer player, CallbackInfo ci) {
        ArcaneAbility self = (ArcaneAbility) (Object) this;
        ArcaneLasers.arcaneFired(player, self.getRange(), self.getPercentAbilityPowerDealt());
    }
}
