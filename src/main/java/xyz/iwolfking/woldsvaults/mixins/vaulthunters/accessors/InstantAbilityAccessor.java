package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.ability.effect.spi.core.InstantAbility;
import iskallia.vault.skill.base.SkillContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = InstantAbility.class, remap = false)
public interface InstantAbilityAccessor {
    @Invoker("doAction")
    Ability.ActionResult callDoAction(SkillContext context);

    @Invoker("doActionPost")
    void callDoActionPost(SkillContext context);

    @Invoker("doParticles")
    void callDoParticles(SkillContext context);

    @Invoker("doSound")
    void callDoSound(SkillContext context);
}