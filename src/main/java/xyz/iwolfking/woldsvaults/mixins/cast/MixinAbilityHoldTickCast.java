package xyz.iwolfking.woldsvaults.mixins.cast;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.ability.effect.spi.core.HoldManaAbility;
import iskallia.vault.skill.base.SkillContext;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.api.cast.CastContext;
import xyz.iwolfking.woldsvaults.api.cast.CastTag;

/** Gives every active tick of a held mana channel its own cast context, covering its payment and its hits. */
@Mixin(value = Ability.class, remap = false)
public class MixinAbilityHoldTickCast {

    @WrapOperation(method = "onTick", at = @At(value = "INVOKE",
            target = "Liskallia/vault/skill/ability/effect/spi/core/Ability;doActiveTick(Liskallia/vault/skill/base/SkillContext;)Liskallia/vault/skill/ability/effect/spi/core/Ability$TickResult;"))
    private Ability.TickResult woldsvaults$openHoldTickContext(Ability ability, SkillContext context, Operation<Ability.TickResult> original) {
        if (!(ability instanceof HoldManaAbility)) {
            return original.call(ability, context);
        }
        ServerPlayer player = context.getSource().as(ServerPlayer.class).orElse(null);
        if (player == null) {
            return original.call(ability, context);
        }
        CastTag tag = new CastTag(player, ability);
        return CastContext.run(tag, () -> original.call(ability, context));
    }
}
