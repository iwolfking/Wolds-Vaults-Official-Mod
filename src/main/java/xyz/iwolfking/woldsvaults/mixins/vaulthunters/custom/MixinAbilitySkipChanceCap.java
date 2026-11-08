package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.skill.ability.effect.spi.core.Ability;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.iwolfking.woldsvaults.config.SpellHasteConfig;
import xyz.iwolfking.woldsvaults.spellcraft.haste.SpellHaste;

@Mixin(value = Ability.class, remap = false)
public abstract class MixinAbilitySkipChanceCap {

    /** Caps the summed cooldown skip chance at the spell haste table's limit; uncapped, it reached 100%. */
    @ModifyVariable(method = "trySkipCooldown", at = @At("STORE"), ordinal = 0)
    private float woldsvaults$capSkipChance(float chance) {
        SpellHasteConfig config = SpellHaste.config();
        return config == null ? chance : Math.min(chance, config.skipChanceCap);
    }
}
