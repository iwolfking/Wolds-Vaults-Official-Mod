package xyz.iwolfking.woldsvaults.mixins.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.client.WorkstationScreenTabs;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;

import java.util.ArrayList;
import java.util.List;

@Mixin(AbstractContainerScreen.class)
public abstract class MixinAbstractContainerScreenTabs {
    @Unique
    private final List<Button> woldsvaults$tabButtons = new ArrayList<>();

    @Inject(method = "init", at = @At("TAIL"))
    private void woldsvaults$addWorkstationTabs(CallbackInfo ci) {
        if (!((Object) this instanceof AnvilScreen)) {
            return;
        }
        if (!WoldsVaultsConfig.CLIENT.showWorkstationTabs.get()) {
            return;
        }
        ((Screen) (Object) this).renderables.removeAll(woldsvaults$tabButtons);
        woldsvaults$tabButtons.clear();
        List<Button> created = WorkstationScreenTabs.createTabs((AbstractContainerScreen<?>) (Object) this);
        woldsvaults$tabButtons.addAll(created);
        WorkstationScreenTabs.registerButtons((AbstractContainerScreen<?>) (Object) this, created);
        for (Button btn : woldsvaults$tabButtons) {
            ((Screen) (Object) this).renderables.add(btn);
        }
    }

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
