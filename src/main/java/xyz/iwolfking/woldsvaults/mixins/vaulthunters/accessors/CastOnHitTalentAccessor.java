package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.skill.talent.type.health.LowHealthTalent;
import iskallia.vault.skill.talent.type.onhit.CastOnHitTalent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = CastOnHitTalent.class, remap = false)
public interface CastOnHitTalentAccessor {
    @Accessor("probability")
    float getProbability();

    @Accessor("ability")
    String getAbilityName();
}
