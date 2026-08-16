package xyz.iwolfking.woldsvaults.mixins.mekanism;

import iskallia.vault.gear.item.VaultGearItem;
import mekanism.api.gear.ModuleData;
import mekanism.client.gui.element.scroll.GuiModuleScrollList;
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

@Mixin(value = GuiModuleScrollList.class, remap = false)
public abstract class MixinGuiModificationStation {
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
}
