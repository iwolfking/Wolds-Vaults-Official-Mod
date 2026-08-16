package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.client.gui.screen.player.legacy.widget.PrestigePowerWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.iwolfking.woldsvaults.api.util.PrestigeRankDisplay;

@Mixin(value = PrestigePowerWidget.class, remap = false)
public class MixinPrestigePowerWidget {

    /**
     * Renames the node tooltip's gate line from "Requires Greed Tier 8" to "Requires Rank: Hunter 2".
     * Every tooltip line in this method funnels through the same factory call, so the rewrite is
     * applied to all of them and filters by prefix rather than by injection ordinal.
     */
    @ModifyArg(
        method = "renderHover",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/FormattedCharSequence;forward(Ljava/lang/String;Lnet/minecraft/network/chat/Style;)Lnet/minecraft/util/FormattedCharSequence;",
            remap = true
        ),
        index = 0
    )
    private String useRankNameInRequirement(String line) {
        return PrestigeRankDisplay.rewriteWidgetRequirement(line);
    }
}
