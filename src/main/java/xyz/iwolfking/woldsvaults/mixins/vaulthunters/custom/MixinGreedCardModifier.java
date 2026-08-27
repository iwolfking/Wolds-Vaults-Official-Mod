package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.card.modifier.card.GreedCardModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = GreedCardModifier.class, remap = false)
public class MixinGreedCardModifier {

    /**
     * Renders a greed card as the additive efficiency bonus it actually grants ("+80% efficiency")
     * rather than as a standalone multiplier ("x1.8"), which reads as if greeds compounded.
     */
    @ModifyArg(method = "addText",
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/network/chat/TextComponent;<init>(Ljava/lang/String;)V",
                        ordinal = 0),
               index = 0)
    private String showGreedAsEfficiencyPercent(String original, @Local(name = "multiplier") float multiplier) {
        return String.format("%+.0f%% efficiency", (multiplier - 1.0F) * 100.0F);
    }
}
