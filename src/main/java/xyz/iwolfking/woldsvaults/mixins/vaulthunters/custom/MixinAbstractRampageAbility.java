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

    /** Applies Ultra Rampaging's Fury scaling to the Rampage damage bonus, for every specialization at once. */
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
