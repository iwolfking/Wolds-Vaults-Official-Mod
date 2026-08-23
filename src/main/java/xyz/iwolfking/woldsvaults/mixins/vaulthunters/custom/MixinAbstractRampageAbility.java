package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.skill.ability.effect.spi.AbstractRampageAbility;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.gods.combat.UltraRampaging;

@Mixin(value = AbstractRampageAbility.class, remap = false)
public class MixinAbstractRampageAbility {

    /**
     * Applies Ultra Rampaging's Fury scaling to the Rampage damage bonus.
     *
     * <p>{@code getDamageIncrease(Player)} is the single seam that reaches every Rampage
     * specialization at once - base, Bloodlust, Berserker, Instinct, Vampiric and Chaining all
     * inherit it - and it is what {@code RampageAbility#applyActiveRampageDamage} multiplies the
     * hit by. Injecting here therefore inherits every gate Rampage already has: no thorns, no
     * area-flagged damage, direct melee only, no critical hits, full-charge swings only. Nothing
     * about where the bonus lands changes; only its size does.
     *
     * <p>The skill screen is deliberately untouched. Its label binding reads
     * {@code getUnmodifiedDamageIncrease()}, so the tooltip keeps showing the ability's own tier
     * value rather than a number that changes twice a second.
     *
     * <p>{@code @Inject} rather than {@code @Overwrite}: if a future the_vault release reshapes
     * this method the node quietly stops working, which is a far better failure than a crash on
     * world load.
     */
    @Inject(method = "getDamageIncrease", at = @At("RETURN"), cancellable = true)
    private void woldsvaults$applyUltraRampaging(Player player, CallbackInfoReturnable<Float> cir) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        float base = cir.getReturnValueF();
        float boosted = UltraRampaging.applyCdm(serverPlayer, base);
        if (boosted != base) {
            cir.setReturnValue(boosted);
        }
    }
}
