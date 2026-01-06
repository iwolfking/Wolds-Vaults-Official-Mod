package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.block.entity.CrystalWorkbenchTileEntity;
import iskallia.vault.container.CrystalWorkbenchContainer;
import iskallia.vault.container.oversized.OverSizedSlotContainer;
import iskallia.vault.container.slot.TabSlot;
import iskallia.vault.item.InfusedCatalystItem;
import iskallia.vault.item.InscriptionItem;
import iskallia.vault.item.gear.CharmItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.client.init.ModSlotIcons;
import xyz.iwolfking.woldsvaults.items.LayoutModificationItem;

@Mixin(value = CrystalWorkbenchContainer.class, remap = false)
public abstract class MixinCrystalWorkbenchContainer  extends OverSizedSlotContainer {

    protected MixinCrystalWorkbenchContainer(MenuType<?> menuType, int id, Player player) {
        super(menuType, id, player);
    }

    @Inject(method = "getUniqueSlotBackground", at = @At("HEAD"), cancellable = true)
    private void addAdditionalSlotBackgrounds(int index, CallbackInfoReturnable<ResourceLocation> cir) {
        if(index == 4) {
            cir.setReturnValue(ModSlotIcons.LAYOUT_NO_ITEM);
        }
        else if(index == 5) {
            cir.setReturnValue(ModSlotIcons.PLACEHOLDER_NO_ITEM);
        }
    }
}
