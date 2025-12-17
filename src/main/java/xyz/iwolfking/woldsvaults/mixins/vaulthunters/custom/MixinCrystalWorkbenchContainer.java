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
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.woldsvaults.items.LayoutModificationItem;

@Mixin(value = CrystalWorkbenchContainer.class, remap = false)
public abstract class MixinCrystalWorkbenchContainer  extends OverSizedSlotContainer {

    @Shadow @Final private CrystalWorkbenchTileEntity entity;

    protected MixinCrystalWorkbenchContainer(MenuType<?> menuType, int id, Player player) {
        super(menuType, id, player);
    }

    @Redirect(method = "<init>", at = @At(value = "TAIL"))
    private Slot addManipulator(CrystalWorkbenchContainer instance, Slot slot, @Local(ordinal = 1) int slotIndex, @Local(ordinal = 0, argsOnly = true) int finalSlotIndex) {
        return this.addSlot((new CrystalWorkbenchContainer.CrystalWorkbenchSlot(this.entity.getUniqueIngredients(), slotIndex + 1, 250, 250) {
            public boolean mayPlace(ItemStack stack) {
                return entity.getUniqueIngredients().canPlaceItem(slotIndex + 1, stack);
            }
        }).setBackground(this.getUniqueSlotBackground(42)));
    }


    @Shadow
    protected abstract ResourceLocation getUniqueSlotBackground(int index);
}
