package xyz.iwolfking.woldsvaults.mixins.lightmanscurrency;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ItemEditWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidgetWithChildren;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "lightmanscurrency")
        }
)
@Mixin(value = ItemEditWidget.class, remap = false)
public abstract class MixinItemEditWidget extends EasyWidgetWithChildren {

    @Unique private static int woldsVaults$loadedGroups;
    @Unique private static String woldsVaults$currGroup = "";
    @Shadow private static boolean rebuilding;
    @Shadow @Final private static List<ItemStack> allItems;

    protected MixinItemEditWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Inject(method = "renderWidget", at = @At("HEAD"))
    private void renderCount(EasyGuiGraphics gui, CallbackInfo ci) {
        if (rebuilding) {
            Minecraft.getInstance().font.draw(gui.getPose(), "Building list of all items", this.getX(), this.getY(), 0xFF000000);
            Minecraft.getInstance().font.draw(gui.getPose(), "Group "+ woldsVaults$loadedGroups +"/"+ CreativeModeTab.TABS.length, this.getX(), this.getY() + 9 + 5, 0xFF000000);
            Minecraft.getInstance().font.draw(gui.getPose(), "Current: "+ woldsVaults$currGroup, this.getX(), this.getY() + 9*2 + 5, 0xFF000000);
            Minecraft.getInstance().font.draw(gui.getPose(), allItems.size() + " items", this.getX(), this.getY() + 9*3 + 5, 0xFF000000);
        }
    }

    @Inject(method = "ConfirmItemListLoaded", at = @At("HEAD"), cancellable = true)
    private static void preventCME(CallbackInfo ci) {
        if (rebuilding) {
            ci.cancel();
        }
    }

    @Inject(method = "initItemList", at = @At(value = "INVOKE", target = "Lio/github/lightman314/lightmanscurrency/client/gui/widget/ItemEditWidget;IsCreativeTabAllowed(Lnet/minecraft/world/item/CreativeModeTab;)Z"))
    private static void increaseGroupsDone(CallbackInfo ci, @Local(name = "creativeTab") CreativeModeTab creativeTab) {
        woldsVaults$loadedGroups++;
        woldsVaults$currGroup = creativeTab.getDisplayName().getString();
    }

    @Inject(method = "initItemList", at = @At(value = "INVOKE", target = "Lio/github/lightman314/lightmanscurrency/LightmansCurrency;LogInfo(Ljava/lang/String;)V"))
    private static void resetGroupsDone(CallbackInfo ci) {
        woldsVaults$loadedGroups = 0;
    }

    @Inject(method = "initItemList", at = @At(value = "INVOKE", target = "Lio/github/lightman314/lightmanscurrency/client/gui/widget/ItemEditWidget;refreshSearch()V"))
    private static void fixRefresh(CallbackInfo ci) {
        rebuilding = false;
    }
}
