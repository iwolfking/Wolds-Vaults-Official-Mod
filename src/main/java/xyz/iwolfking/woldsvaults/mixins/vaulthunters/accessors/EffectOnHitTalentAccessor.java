package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.skill.talent.type.onhit.CastOnHitTalent;
import iskallia.vault.skill.talent.type.onhit.EffectOnHitTalent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = EffectOnHitTalent.class, remap = false)
public interface EffectOnHitTalentAccessor {
    @Accessor("probability")
    float getProbability();

    @Accessor("amplifier")
    int getAmplifier();
}
