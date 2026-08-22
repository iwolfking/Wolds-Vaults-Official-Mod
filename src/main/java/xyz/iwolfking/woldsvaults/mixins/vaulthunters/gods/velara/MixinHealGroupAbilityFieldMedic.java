package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods.velara;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import iskallia.vault.skill.ability.effect.HealGroupAbility;
import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.base.SkillContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import xyz.iwolfking.woldsvaults.gods.trees.velara.VelaraFieldMedic;

/**
 * Attribution for Field Medic. {@code LivingHealEvent} carries no healer, so the caster is stamped
 * on a thread local for the duration of the group heal and read back by the Field Medic listener.
 *
 * <p>The stamp wraps the call rather than bracketing it with HEAD and RETURN injections. RETURN
 * does not run when the target throws, so a bracket would leave the attribution set on the server
 * thread permanently and credit every later heal on that thread, anyone's, to that caster.
 */
@Mixin(value = HealGroupAbility.class, remap = false)
public class MixinHealGroupAbilityFieldMedic {

    @WrapMethod(method = "doAction")
    private Ability.ActionResult velaraAttributeHeal(SkillContext context, Operation<Ability.ActionResult> original) {
        ServerPlayer healer = context.getSource().as(ServerPlayer.class).orElse(null);
        if (healer == null) {
            return original.call(context);
        }
        LivingEntity previous = VelaraFieldMedic.pushHealer(healer);
        try {
            return original.call(context);
        } finally {
            VelaraFieldMedic.popHealer(previous);
        }
    }
}
