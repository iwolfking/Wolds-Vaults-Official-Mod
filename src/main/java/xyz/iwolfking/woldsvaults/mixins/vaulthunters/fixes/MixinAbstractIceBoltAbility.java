package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.skill.ability.effect.spi.AbstractIceBoltAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = AbstractIceBoltAbility.class, remap = false)
public class MixinAbstractIceBoltAbility {
    /**
     * Drops damage increase from the baked bolt damage. The hurt event applies damage increase to the bolt
     * anyway, so baking it in as well counted it twice.
     */
    @ModifyVariable(method = "getTotalDamage(Lnet/minecraft/world/entity/player/Player;Z)F", at = @At("STORE"), ordinal = 1)
    private float woldsVaults$skipBakedDamageIncrease(float attackDamagePercentageIncrease) {
        return 0.0F;
    }
}
