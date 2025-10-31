package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.skill.prestige.TreasureHunterPrestigePower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = TreasureHunterPrestigePower.class, remap = false)
public class MixinTreasureHunterEffect {
    @ModifyConstant(method = "updateIndicatorEffect", constant = @Constant(intValue = 40))
    private static int updateEffectEarlier(int constant) {
        return 100;
    }
}
