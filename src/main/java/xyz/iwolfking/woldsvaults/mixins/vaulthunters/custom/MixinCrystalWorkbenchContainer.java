package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.container.CrystalWorkbenchContainer;
import iskallia.vault.container.oversized.OverSizedSlotContainer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.client.init.ModSlotIcons;
import xyz.iwolfking.woldsvaults.init.ModTags;

@Mixin(value = CrystalWorkbenchContainer.class, remap = false)
public abstract class MixinCrystalWorkbenchContainer  extends OverSizedSlotContainer {

    @Unique
    private static final int WOLDSVAULTS$MEDALLION_SLOT = 6;
    @Unique
    private static final int WOLDSVAULTS$MEDALLION_SLOT_X = 223;
    @Unique
    private static final int WOLDSVAULTS$MEDALLION_SLOT_Y = 19;

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
        else if(index == WOLDSVAULTS$MEDALLION_SLOT) {
            cir.setReturnValue(ModSlotIcons.PLACEHOLDER_NO_ITEM);
        }
    }

    /**
     * Rewrites the seal slot so stackable seals may be inserted in stacks, and appends the greed
     * medallion slot right after the last of the base mod's six unique slots. The medallion slot has
     * to land before the output slot is added, because {@code quickMoveStack} takes the last slot in
     * the container to be the output.
     */
    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Liskallia/vault/container/CrystalWorkbenchContainer;addSlot(Lnet/minecraft/world/inventory/Slot;)Lnet/minecraft/world/inventory/Slot;"), remap = true,
    slice = @Slice(from = @At(value = "INVOKE", target = "Liskallia/vault/block/entity/CrystalWorkbenchTileEntity;getUniqueIngredients()Liskallia/vault/container/oversized/OverSizedInventory;", ordinal = 0),
                     to = @At(value = "INVOKE", target = "Liskallia/vault/block/entity/CrystalWorkbenchTileEntity;getOutput()Liskallia/vault/container/oversized/OverSizedInventory;")))
    private Slot stackableSeals(CrystalWorkbenchContainer instance, Slot slot, Operation<Slot> original){
        var bg = slot.getNoItemIcon();
        var typedSlot = slot instanceof CrystalWorkbenchContainer.CrystalWorkbenchSlot sl ? sl : null;
        Slot added;
        if (typedSlot != null && bg != null && bg.getSecond().equals(iskallia.vault.init.ModSlotIcons.SEAL_NO_ITEM)) {
            added = original.call(instance, (new CrystalWorkbenchContainer.CrystalWorkbenchSlot(typedSlot.container, typedSlot.index, typedSlot.x, typedSlot.y) {
                public boolean mayPlace(ItemStack stack) {
                    return instance.getEntity().getUniqueIngredients().canPlaceItem(typedSlot.index, stack);
                }

                @Override
                public int getMaxStackSize(ItemStack pStack) {
                    if (pStack.is(ModTags.STACKABLE_SEALS)) {
                        return 64;
                    }
                    return super.getMaxStackSize(pStack);
                }
            }).setBackground(bg.getSecond()));
        } else {
            added = original.call(instance, slot);
        }
        if (typedSlot != null && typedSlot.index == WOLDSVAULTS$MEDALLION_SLOT - 1) {
            this.addSlot(woldsVaults$createMedallionSlot(instance));
        }
        return added;
    }

    @Unique
    private static Slot woldsVaults$createMedallionSlot(CrystalWorkbenchContainer instance) {
        return new CrystalWorkbenchContainer.CrystalWorkbenchSlot(instance.getEntity().getUniqueIngredients(),
                WOLDSVAULTS$MEDALLION_SLOT, WOLDSVAULTS$MEDALLION_SLOT_X, WOLDSVAULTS$MEDALLION_SLOT_Y) {
            public boolean mayPlace(ItemStack stack) {
                return instance.getEntity().getUniqueIngredients().canPlaceItem(WOLDSVAULTS$MEDALLION_SLOT, stack);
            }
        }.setBackground(ModSlotIcons.PLACEHOLDER_NO_ITEM);
    }
}
