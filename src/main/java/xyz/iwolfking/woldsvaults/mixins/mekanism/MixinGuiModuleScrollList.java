package xyz.iwolfking.woldsvaults.mixins.mekanism;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.gear.item.VaultGearItem;
import mekanism.api.gear.ModuleData;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.scroll.GuiModuleScrollList;
import mekanism.client.gui.element.scroll.GuiScrollList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.integration.mekanism.init.ModModuleToVaultGearModifications;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

@Mixin(value = GuiModuleScrollList.class, remap = false)
public abstract class MixinGuiModuleScrollList extends GuiScrollList {
    @Shadow
    @Final
    private List<ModuleData<?>> currentList;

    @Shadow
    private ItemStack currentItem;

    @Shadow
    @Nullable
    public abstract ModuleData<?> getSelection();

    @Shadow
    protected abstract void setSelected(int index);

    @Shadow
    public abstract void clearSelection();


    @Shadow
    private int selectIndex;

    @Shadow
    @Final
    private Consumer<mekanism.common.content.gear.Module<?>> callback;

    protected MixinGuiModuleScrollList(IGuiWrapper gui, int x, int y, int width, int height, int elementHeight, ResourceLocation background, int backgroundSideSize) {
        super(gui, x, y, width, height, elementHeight, background, backgroundSideSize);
    }

    @Inject(method = "updateList", at = @At("HEAD"), cancellable = true)
    private void handleVaultGearModuleList(ItemStack currentItem, boolean forceReset, CallbackInfo ci) {
        ModuleData<?> prevSelect = getSelection();
        if(currentItem.getItem() instanceof VaultGearItem) {
            this.currentItem = currentItem;
            currentList.clear();
            currentList.addAll(ModModuleToVaultGearModifications.getModuleList(currentItem));
            boolean selected = false;
            if (!forceReset && prevSelect != null) {
                for (int i = 0, size = currentList.size(); i < size; i++) {
                    if (currentList.get(i) == prevSelect) {
                        setSelected(i);
                        selected = true;
                        break;
                    }
                }
            }
            if (!selected) {
                clearSelection();
            }
            ci.cancel();
        }
    }

    @Inject(method = "renderForeground", at = @At("TAIL"))
    private void renderVaultGearModuleNames(PoseStack matrix, int mouseX, int mouseY, CallbackInfo ci) {
        if (this.currentItem.getItem() instanceof VaultGearItem) {
            int elementHeight = 12;

            for (int i = 0; i < this.getFocusedElements(); ++i) {
                int index = this.getCurrentSelection() + i;
                if (index >= this.currentList.size()) break;

                ModuleData<?> module = this.currentList.get(index);
                int multipliedElement = elementHeight * i;

                int color = this.titleTextColor();
                this.drawScaledTextScaledBound(
                        matrix,
                        TextComponentUtil.build(module),
                        (float)(this.relativeX + 13),
                        (float)(this.relativeY + 3 + multipliedElement),
                        color,
                        86.0F,
                        0.7F
                );
            }
        }
    }

    @Inject(method = "setSelected", at = @At("HEAD"), cancellable = true)
    private void handleVaultGearSelection(int index, CallbackInfo ci) {
        if (this.currentItem.getItem() instanceof VaultGearItem) {
            if (index >= 0 && index < this.currentList.size()) {
                this.selectIndex = index;
                mekanism.common.content.gear.Module<?> dummyModule = new mekanism.common.content.gear.Module<>(this.currentList.get(index), this.currentItem);
                this.callback.accept(dummyModule);
            } else {
                this.clearSelection();
            }
            ci.cancel();
        }
    }


}
