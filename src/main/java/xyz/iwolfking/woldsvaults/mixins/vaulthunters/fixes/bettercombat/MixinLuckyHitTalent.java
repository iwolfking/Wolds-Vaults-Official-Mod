package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes.bettercombat;

import iskallia.vault.skill.talent.type.luckyhit.LuckyHitTalent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = LuckyHitTalent.class, remap = false)
public class MixinLuckyHitTalent {
    @ModifyConstant(method = "doLuckyHit", constant = @Constant(floatValue = 1.0F, ordinal = 0))
    private static float adjustAttackScaleRequirementLuckyHit(float constant) {
        return 0.5F;
    }
}
