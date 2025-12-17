package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.skill.talent.type.health.HighHealthGearAttributeTalent;
import iskallia.vault.skill.talent.type.health.LowHealthGearAttributeTalent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = LowHealthGearAttributeTalent.class, remap = false)
public interface LowHealthGearAttributeTalentAccessor {
    @Accessor("value")
    double getValue();
}
