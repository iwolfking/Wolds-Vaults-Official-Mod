package xyz.iwolfking.woldsvaults.mixins.ae2;

import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.me.common.MEStorageMenu;
import iskallia.vault.research.StageManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.client.gui.ae2.DepositBackpackButton;
import xyz.iwolfking.woldsvaults.mixins.ae2.accessors.AccessorAEBaseScreen;

@Mixin(value = MEStorageScreen.class, remap = false)
public abstract class MixinMEStorageScreen {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void vault$addDepositBackpackButton(MEStorageMenu menu, Inventory playerInventory, Component title, ScreenStyle style, CallbackInfo ci) {
        if(!StageManager.RESEARCH_TREE.isResearched("Altar Automation")) {
            return;
        }

        ((AccessorAEBaseScreen) this).invokeAddToLeftToolbar(new DepositBackpackButton());
    }
}
