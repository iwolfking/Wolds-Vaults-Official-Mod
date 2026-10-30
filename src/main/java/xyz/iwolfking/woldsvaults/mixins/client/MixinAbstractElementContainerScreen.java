package xyz.iwolfking.woldsvaults.mixins.client;

import iskallia.vault.client.gui.framework.screen.AbstractElementContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.client.WorkstationScreenTabs;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;

import java.util.ArrayList;
import java.util.List;

@Mixin(AbstractElementContainerScreen.class)
public abstract class MixinAbstractElementContainerScreen<C extends AbstractContainerMenu> {
    @Unique
    private final List<Button> woldsvaults$tabButtons = new ArrayList<>();

    @Inject(method = "init", at = @At("TAIL"))
    private void woldsvaults$addWorkstationTabs(CallbackInfo ci) {
        if(WoldsVaultsConfig.CLIENT.showWorkstationTabs.get()) {
            woldsvaults$tabButtons.clear();
            List<Button> created = WorkstationScreenTabs.createTabs((AbstractContainerScreen<?>) (Object) this);
            woldsvaults$tabButtons.addAll(created);
            WorkstationScreenTabs.registerButtons((AbstractContainerScreen<?>) (Object) this, created);
            for (Button btn : woldsvaults$tabButtons) {
                ((Screen) (Object) this).renderables.add(btn);
            }
        }

    }

}
