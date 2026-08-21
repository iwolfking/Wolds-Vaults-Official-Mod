package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods;

import iskallia.vault.block.entity.GreedCauldronTileEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.gods.sacrifice.SacrificeAltarLogic;

@Mixin(targets = "iskallia.vault.block.entity.GreedCauldronTileEntity$SideInputHandler", remap = false)
public abstract class MixinGreedCauldronSideInputSacrifice {
    @Shadow
    @Final
    private GreedCauldronTileEntity tile;

    /**
     * @author PoorMansPhysicist
     * @reason the cauldron's piped-item intake now feeds the owner's selected god's sacrifice
     * gate instead of the retired demand system. The accepted portion is consumed outright; the
     * remainder returns to the inserting handler.
     */
    @Inject(method = "insertItem", at = @At("HEAD"), cancellable = true)
    private void woldsVaults$sacrificeInsert(int slot, ItemStack stack, boolean simulate,
                                             CallbackInfoReturnable<ItemStack> cir) {
        cir.setReturnValue(SacrificeAltarLogic.pipeInsert(this.tile, stack, simulate));
    }
}
