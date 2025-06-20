package xyz.iwolfking.woldsvaults.mixins.vaulthunters.performance;

import iskallia.vault.skill.talent.type.health.HighHealthGearAttributeTalent;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = HighHealthGearAttributeTalent.class, remap = false)
public class MixinHighHealthGearAttributeTalent {
}
