package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.vault_forge;

import iskallia.vault.client.gui.screen.block.forge.VaultForgeScreen;
import iskallia.vault.client.gui.screen.canvas.CanvasContainerScreen;
import iskallia.vault.client.gui.screen.canvas.CanvasStationFrame;
import iskallia.vault.container.VaultForgeContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = VaultForgeScreen.class, remap = false)
public abstract class MixinVaultForgeScreen extends CanvasContainerScreen<VaultForgeContainer> {

    @Unique
    private static final int WOLDSVAULTS$DELTA_Y = 24;

    @Unique
    private static final int WOLDSVAULTS$BASE_HEIGHT = 240;

    @Unique
    private static final int WOLDSVAULTS$NEW_HEIGHT = WOLDSVAULTS$BASE_HEIGHT + WOLDSVAULTS$DELTA_Y;

    protected MixinVaultForgeScreen(VaultForgeContainer container, Inventory inventory, Component title) {
        super(container, inventory, title, 176, WOLDSVAULTS$NEW_HEIGHT);
    }

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 240))
    private static int woldsVaults$initHeight(int constant) {
        return WOLDSVAULTS$NEW_HEIGHT;
    }

    @Redirect(
            method = "fitHeight",
            at = @At(value = "INVOKE", target = "Liskallia/vault/client/gui/screen/canvas/CanvasStationFrame;fitHeight(I)I")
    )
    private int woldsVaults$fitHeight(int height) {
        return CanvasStationFrame.fitHeight(WOLDSVAULTS$NEW_HEIGHT);
    }

    @Inject(method = "drawWindow", at = @At("HEAD"))
    private void woldsVaults$ensureCenteredTopPos(long now, CallbackInfo ci) {
        this.topPos = CanvasStationFrame.windowTop(this.height, WOLDSVAULTS$NEW_HEIGHT);
    }

    @ModifyConstant(method = "drawWindow", constant = @Constant(floatValue = 240.0F))
    private float woldsVaults$drawWindowHeight(float constant) {
        return (float) WOLDSVAULTS$NEW_HEIGHT;
    }

    @ModifyConstant(method = "drawPanel", constant = @Constant(floatValue = 240.0F))
    private float woldsVaults$drawPanelHeight(float constant) {
        return (float) WOLDSVAULTS$NEW_HEIGHT;
    }

    @ModifyConstant(method = "drawTrinketsTabBadge", constant = @Constant(floatValue = 240.0F))
    private float woldsVaults$trinketBadgeHeight(float constant) {
        return (float) WOLDSVAULTS$NEW_HEIGHT;
    }

    @ModifyArg(
            method = "drawWindow",
            at = @At(
                    value = "INVOKE",
                    target = "Liskallia/vault/client/gui/screen/options/ui/OptionsCanvas;text(Ljava/lang/String;FFIZ)V"
            ),
            index = 2
    )
    private float woldsVaults$shiftInventoryLabelY(float y) {
        return y == 147.0F ? y + WOLDSVAULTS$DELTA_Y : y;
    }
}