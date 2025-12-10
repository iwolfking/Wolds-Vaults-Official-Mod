package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.skill.talent.GearAttributeSkill;
import iskallia.vault.skill.talent.type.onhit.CastOnHitTalent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = GearAttributeSkill.class, remap = false)
public interface GearAttributeSkillAccessor {

}
