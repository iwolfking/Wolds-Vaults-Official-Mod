package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.skill.talent.type.health.LowHealthTalent;
import iskallia.vault.skill.talent.type.mana.LowManaDamageTalent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = LowManaDamageTalent.class, remap = false)
public interface LowManaDamageTalentAccessor {
    @Accessor("damageIncrease")
    float getDamageIncrease();
}
