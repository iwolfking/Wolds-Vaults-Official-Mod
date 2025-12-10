package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.skill.talent.type.onhit.CastOnHitTalent;
import iskallia.vault.skill.talent.type.onkill.CastOnKillTalent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = CastOnKillTalent.class, remap = false)
public interface CastOnKillTalentAccessor {
    @Accessor("probability")
    float getProbability();

    @Accessor("ability")
    String getAbilityName();
}
