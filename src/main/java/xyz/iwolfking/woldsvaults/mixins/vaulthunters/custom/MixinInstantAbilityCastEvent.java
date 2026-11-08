package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.event.common.AbilityCastEvent;
import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.base.SkillContext;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.spellcraft.bar.SlotBilling;

@Mixin(value = AbilityCastEvent.class, remap = false)
public abstract class MixinInstantAbilityCastEvent {

    /** Shotgun copies don't fire on-cast effects; the slot's own cast and Echo still do. */
    @Inject(method = "invoke(Lnet/minecraft/world/entity/Entity;Liskallia/vault/skill/ability/effect/spi/core/Ability;Liskallia/vault/skill/base/SkillContext;)Liskallia/vault/core/event/common/AbilityCastEvent$Data;",
            at = @At("HEAD"), cancellable = true)
    private void woldsvaults$suppressCopyCastEvent(Entity entity, Ability ability, SkillContext context,
                                                   CallbackInfoReturnable<AbilityCastEvent.Data> cir) {
        if (SlotBilling.suppressesCastEvent(entity)) {
            SlotBilling.noteSuppressedCast(entity, ability);
            cir.setReturnValue(new AbilityCastEvent.Data(entity, ability, context));
        }
    }
}
