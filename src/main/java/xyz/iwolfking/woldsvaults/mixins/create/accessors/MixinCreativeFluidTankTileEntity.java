package xyz.iwolfking.woldsvaults.mixins.create.accessors;

import com.simibubi.create.content.fluids.tank.CreativeFluidTankBlockEntity;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.init.ModTags;

import java.util.function.Consumer;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "create")
        }
)
@Mixin(value = CreativeFluidTankBlockEntity.CreativeSmartFluidTank.class, remap = false)
public abstract class MixinCreativeFluidTankTileEntity extends SmartFluidTank {


    public MixinCreativeFluidTankTileEntity(int capacity, Consumer<FluidStack> updateCallback) {
        super(capacity, updateCallback);
    }

    @Inject(method = "setContainedFluid", at = @At("HEAD"), cancellable = true)
    private void blacklistSetContainedFluid(FluidStack fluidStack, CallbackInfo ci) {
        if(ModTags.isCreativeTankBlacklisted(fluidStack)) {
            ci.cancel();
        }
    }

    @Inject(method = "getFluidAmount", at = @At("HEAD"), cancellable = true)
    private void blacklistFluidsAmount(CallbackInfoReturnable<Integer> cir) {
        if(ModTags.isCreativeTankBlacklisted(this.fluid)) {
            cir.setReturnValue(super.getFluidAmount());
        }
    }

    @Inject(method = "drain(ILnet/minecraftforge/fluids/capability/IFluidHandler$FluidAction;)Lnet/minecraftforge/fluids/FluidStack;", at = @At("HEAD"), cancellable = true)
    private void blacklistFluidsDrain1(int maxDrain, FluidAction action, CallbackInfoReturnable<FluidStack> cir) {
        if(ModTags.isCreativeTankBlacklisted(this.fluid)) {
            cir.setReturnValue(super.drain(maxDrain, action));
        }
    }

    @Inject(method = "drain(Lnet/minecraftforge/fluids/FluidStack;Lnet/minecraftforge/fluids/capability/IFluidHandler$FluidAction;)Lnet/minecraftforge/fluids/FluidStack;", at = @At("HEAD"), cancellable = true)
    private void blacklistFluidsDrain1(FluidStack resource, FluidAction action, CallbackInfoReturnable<FluidStack> cir) {
        if(ModTags.isCreativeTankBlacklisted(this.fluid)) {
            cir.setReturnValue(super.drain(resource, action));
        }
    }

}
