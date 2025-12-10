package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.skill.talent.type.luckyhit.HealthLeechLuckyHitTalent;
import iskallia.vault.skill.talent.type.luckyhit.ManaLeechLuckyHitTalent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = HealthLeechLuckyHitTalent.class, remap = false)
public interface HealthLeechLuckyHitTalentAccessor {
    @Accessor("maxHealthPercentage")
    float getMaxHealthPercentage();
}
