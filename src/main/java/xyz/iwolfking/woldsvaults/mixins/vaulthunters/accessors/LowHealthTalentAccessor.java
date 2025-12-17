package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.skill.talent.type.health.LowHealthTalent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = LowHealthTalent.class, remap = false)
public interface LowHealthTalentAccessor {
    @Accessor("healthThreshold")
    float getHealthThreshold();
}
