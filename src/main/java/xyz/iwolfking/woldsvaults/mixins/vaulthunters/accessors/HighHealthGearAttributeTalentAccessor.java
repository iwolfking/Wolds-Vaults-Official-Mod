package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.skill.talent.type.health.HighHealthGearAttributeTalent;
import iskallia.vault.skill.talent.type.health.LowHealthDamageTalent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = HighHealthGearAttributeTalent.class, remap = false)
public interface HighHealthGearAttributeTalentAccessor {
    @Accessor("value")
    double getValue();
}
