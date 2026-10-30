package xyz.iwolfking.woldsvaults.mixins.refinedstorage;

import com.refinedmods.refinedstorage.screen.grid.GridScreen;
import com.refinedmods.refinedstorage.screen.widget.sidebutton.SideButton;
import iskallia.vault.research.StageManager;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.util.BackpackUtils;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.network.packets.ServerboundDepositBackpackToRSGridPacket;

@Mixin(GridScreen.class)
public abstract class MixinRSGridScreen {
    @Inject(method = "onPostInit", at = @At("TAIL"), remap = false)
    private void woldsvaults$addDepositBackpackButton(CallbackInfo ci) {
        if (!StageManager.RESEARCH_TREE.isResearched("Altar Automation")) {
            return;
        }

        GridScreen screen = (GridScreen) (Object) this;
        screen.addSideButton(new SideButton(screen) {
            @Override
            protected void renderButtonIcon(PoseStack stack, int x, int y) {
                ItemStack backpack = BackpackUtils.findBackpack(Minecraft.getInstance().player);
                if (!backpack.isEmpty()) {
                    ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
                    renderer.renderAndDecorateItem(backpack, x, y);
                }
            }

            @Override
            protected String getTooltip() {
                ItemStack backpack = BackpackUtils.findBackpack(Minecraft.getInstance().player);
                if (backpack.isEmpty()) {
                    return "Deposit Backpack\nNo backpack found";
                }
                return "Deposit Backpack\n" + backpack.getHoverName().getString();
            }

            @Override
            public void onPress() {
                ModNetwork.sendToServer(new ServerboundDepositBackpackToRSGridPacket());
            }
        });
    }
}
