package xyz.iwolfking.woldsvaults.mixins.vaulthunters.skills.buffs;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.ability.effect.spi.core.ToggleAbility;
import iskallia.vault.skill.base.SkillContext;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import xyz.iwolfking.woldsvaults.augments.effect.BuffGlyphs;
import xyz.iwolfking.woldsvaults.spellcraft.bar.ToggleGuard;

/**
 * A toggle switching on starts its Intensify/Extend activation (none outside a spell step) and records its bar slot so
 * it switches off when that spell changes; switching off ends both.
 */
@Mixin(value = ToggleAbility.class, remap = false)
public class MixinToggleAbilityBuffGlyphs {

    @WrapMethod(method = "onAction")
    private Ability.ActionResult woldsvaults$trackActivation(SkillContext context, Operation<Ability.ActionResult> original) {
        Ability.ActionResult result = original.call(context);
        ToggleAbility self = (ToggleAbility) (Object) this;
        context.getSource().as(ServerPlayer.class).ifPresent(player -> {
            if (!self.isActive() || result.isSuccess()) {
                BuffGlyphs.onToggle(player, self);
                ToggleGuard.onToggle(player, self);
            }
        });
        return result;
    }
}
