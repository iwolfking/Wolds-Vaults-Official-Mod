package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.skill.talent.type.mana.HighManaGearAttributeTalent;
import iskallia.vault.skill.talent.type.mana.LowManaHealingEfficiencyTalent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = LowManaHealingEfficiencyTalent.class, remap = false)
public interface LowManaHealingEfficiencyTalentAccessor {
    @Accessor("additionalHealingEfficiency")
    float getValue();
}
