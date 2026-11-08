package xyz.iwolfking.woldsvaults.mixins.trashslot;

import iskallia.vault.research.StageManager;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.blay09.mods.balm.api.event.client.screen.ScreenKeyEvent;
import net.blay09.mods.trashslot.client.TrashSlotGuiHandler;
import net.blay09.mods.balm.api.event.client.screen.ContainerScreenDrawEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.data.WoldConstants;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "trashslot")
        }
)
@Mixin(value = TrashSlotGuiHandler.class, remap = false)
public class TrashSlotGuiHandlerMixin {

    @Inject(method = "onKeyPress", at = @At("HEAD"), cancellable = true)
    private static void cancelKeybindings(ScreenKeyEvent.Press.Post event, CallbackInfo ci) {
        if(!StageManager.RESEARCH_TREE.isResearched(WoldConstants.TRASH_SLOT_RESEARCH)) {
            ci.cancel();
        }
    }

    @Inject(method = "onBackgroundDrawn", at = @At("HEAD"), cancellable = true)
    private static void cancelBackgroundDraw(ContainerScreenDrawEvent.Background event, CallbackInfo ci) {
        if(!StageManager.RESEARCH_TREE.isResearched(WoldConstants.TRASH_SLOT_RESEARCH)) {
            ci.cancel();
        }
    }

    @Inject(method = "onScreenDrawn", at = @At("HEAD"), cancellable = true)
    private static void cancelScreenDraw(ContainerScreenDrawEvent.Background event, CallbackInfo ci) {
        if(!StageManager.RESEARCH_TREE.isResearched(WoldConstants.TRASH_SLOT_RESEARCH)) {
            ci.cancel();
        }
    }
}