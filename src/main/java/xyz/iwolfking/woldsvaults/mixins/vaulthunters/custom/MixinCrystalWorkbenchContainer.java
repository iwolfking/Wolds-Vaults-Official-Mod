package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.container.CrystalWorkbenchContainer;
import iskallia.vault.container.oversized.OverSizedSlotContainer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.client.init.ModSlotIcons;

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
