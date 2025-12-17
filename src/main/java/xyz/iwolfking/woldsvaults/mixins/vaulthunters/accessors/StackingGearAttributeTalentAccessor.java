package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.skill.talent.type.StackingGearAttributeTalent;
import iskallia.vault.skill.talent.type.onhit.DamageOnHitTalent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = StackingGearAttributeTalent.class, remap = false)
public interface StackingGearAttributeTalentAccessor {
    @Accessor("maxStacks")
    int getMaxStacks();
}
