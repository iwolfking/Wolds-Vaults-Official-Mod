package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.skill.expertise.type.MysticExpertise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = MysticExpertise.class, remap = false)
public interface MysticExpertiseAccessor {
    @Accessor("additionalCrystalVolume")
    void setAdditionalVolume(int volume);
}
