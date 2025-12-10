package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.skill.talent.type.VanillaAttributeTalent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = VanillaAttributeTalent.class, remap = false)
public interface VanillaAttributeTalentAccessor {
    @Accessor("amount")
    double getAmount();
}
