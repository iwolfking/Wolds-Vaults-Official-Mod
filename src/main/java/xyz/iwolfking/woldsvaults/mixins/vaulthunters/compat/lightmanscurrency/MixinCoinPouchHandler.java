package xyz.iwolfking.woldsvaults.mixins.vaulthunters.compat.lightmanscurrency;

import iskallia.vault.item.CoinPouchItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CoinPouchItem.Handler.class, remap = false)
public abstract class MixinCoinPouchHandler extends ItemStackHandler {

    public MixinCoinPouchHandler(int size) {
        super(size);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void woldsvaults$initExtraSlots(ItemStack delegate, CallbackInfo ci) {
        ItemStack[] contained = CoinPouchItem.getContainedStacks(delegate);
        if (this.stacks.size() != contained.length) {
            this.setSize(contained.length);
        }
        for (int i = 8; i < contained.length; i++) {
            this.stacks.set(i, contained[i] == null ? ItemStack.EMPTY : contained[i].copy());
        }
    }
}