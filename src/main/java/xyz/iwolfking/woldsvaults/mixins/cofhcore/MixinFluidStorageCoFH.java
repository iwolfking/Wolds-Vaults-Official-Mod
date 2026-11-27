package xyz.iwolfking.woldsvaults.mixins.cofhcore;

import cofh.lib.fluid.FluidStorageCoFH;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.init.ModTags;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "cofh_core")
        }
)
@Mixin(value = FluidStorageCoFH.class, remap = false)
public abstract class MixinFluidStorageCoFH {
    @Shadow
    protected FluidStack fluid;

    @Inject(method = "isCreative", at = @At("HEAD"), cancellable = true)
    private void blacklistCreativeFluids(CallbackInfoReturnable<Boolean> cir) {
        if (ModTags.isCreativeTankBlacklisted(this.fluid)) {
            cir.setReturnValue(false);
        }
    }
}
