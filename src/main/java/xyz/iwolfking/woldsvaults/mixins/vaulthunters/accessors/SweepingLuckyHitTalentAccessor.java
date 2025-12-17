package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.skill.talent.type.luckyhit.SweepingLuckyHitTalent;
import iskallia.vault.skill.talent.type.onhit.CastOnHitTalent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = SweepingLuckyHitTalent.class, remap = false)
public interface SweepingLuckyHitTalentAccessor {
    @Accessor("damageRange")
    float getRange();

    @Accessor("damagePercentage")
    float getDamage();
}
