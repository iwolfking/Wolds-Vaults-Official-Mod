package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.skill.talent.type.health.LowHealthDamageTalent;
import iskallia.vault.skill.talent.type.health.LowHealthTalent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = LowHealthDamageTalent.class, remap = false)
public interface LowHealthDamageTalentAccessor {
    @Accessor("damageIncrease")
    float getDamageIncrease();
}
