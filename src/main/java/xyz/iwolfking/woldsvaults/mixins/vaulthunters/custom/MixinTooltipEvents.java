package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.event.TooltipEvents;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.util.GameruleHelper;
import xyz.iwolfking.woldsvaults.init.ModGameRules;

@Mixin(value = TooltipEvents.class, remap = false)
public class MixinTooltipEvents {
    @Inject(method = "addBlacklistOnTooltip", at = @At("HEAD"), cancellable = true)
    private static void hideTooltipIfGameruleEnabled(ItemTooltipEvent event, CallbackInfo ci) {
        if(event.getPlayer() == null) {
            return;
        }

        if(GameruleHelper.isEnabled(ModGameRules.ENABLE_ALL_ITEMS_IN_VAULTS, event.getPlayer().getLevel())) {
            ci.cancel();
        }
    }
}
