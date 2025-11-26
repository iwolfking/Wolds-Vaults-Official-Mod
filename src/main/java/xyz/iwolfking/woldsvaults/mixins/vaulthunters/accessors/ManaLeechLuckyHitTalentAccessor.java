package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.skill.talent.type.luckyhit.ManaLeechLuckyHitTalent;
import iskallia.vault.skill.talent.type.mana.LowManaDamageTalent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ManaLeechLuckyHitTalent.class, remap = false)
public interface ManaLeechLuckyHitTalentAccessor {
    @Accessor("maxManaPercentage")
    float getMaxManaPercentage();
}
