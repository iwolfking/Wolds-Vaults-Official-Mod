package xyz.iwolfking.woldsvaults.mixins.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.client.WorkstationScreenTabs;

import java.util.List;

@Mixin(AbstractContainerScreen.class)
public abstract class MixinAbstractContainerScreenTabs {
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void woldsvaults$handleTabClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        List<Button> buttons = WorkstationScreenTabs.getButtons((Screen) (Object) this);
        if (buttons == null) return;
        for (Button btn : buttons) {
            if (btn.isMouseOver(mouseX, mouseY)) {
                btn.onPress();
                cir.setReturnValue(true);
                return;
            }
        }
    }
}
