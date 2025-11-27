package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.skill.expertise.type.InfuserExpertise;
import iskallia.vault.skill.expertise.type.MysticExpertise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = InfuserExpertise.class, remap = false)
public interface InfuserExpertiseAccessor {
    @Accessor("negativeModifierRemovalChance")
    void setChance(float chance);
}
