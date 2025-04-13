package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.skill.base.LearnableSkill;
import iskallia.vault.skill.base.TickingSkill;
import iskallia.vault.skill.expertise.type.AngelExpertise;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = AngelExpertise.class, remap = false)
public abstract class MixinAngelExpertise extends LearnableSkill implements TickingSkill {
    @Override
    public boolean isUnlocked() {
        return true;
    }
}
