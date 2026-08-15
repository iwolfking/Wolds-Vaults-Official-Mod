package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods.velara;

import iskallia.vault.skill.ability.effect.HealGroupAbility;
import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.base.SkillContext;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.gods.trees.velara.FieldMedic;

/**
 * Attribution for Field Medic. {@code LivingHealEvent} carries no healer, so the caster is stamped
 * on a thread local for the duration of the group heal and read back by the Field Medic listener.
 */
@Mixin(value = HealGroupAbility.class, remap = false)
public class MixinHealGroupAbilityFieldMedic {

    @Inject(method = "doAction", at = @At("HEAD"))
    private void velaraPushHealer(SkillContext context, CallbackInfoReturnable<Ability.ActionResult> cir) {
        context.getSource().as(ServerPlayer.class).ifPresent(FieldMedic::pushHealer);
    }

    @Inject(method = "doAction", at = @At("RETURN"))
    private void velaraPopHealer(SkillContext context, CallbackInfoReturnable<Ability.ActionResult> cir) {
        FieldMedic.popHealer();
    }
}
