package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.skill.talent.type.health.HighHealthGearAttributeTalent;
import iskallia.vault.skill.talent.type.mana.HighManaGearAttributeTalent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = HighManaGearAttributeTalent.class, remap = false)
public interface HighManaGearAttributeTalentAccessor {
    @Accessor("value")
    double getValue();
}
