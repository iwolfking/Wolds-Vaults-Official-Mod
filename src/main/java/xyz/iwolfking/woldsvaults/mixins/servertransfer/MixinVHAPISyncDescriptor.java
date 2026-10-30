package xyz.iwolfking.woldsvaults.mixins.servertransfer;

import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.vhapi.networking.VHAPISyncDescriptor;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.client.servertransfer.ServerTransferState;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "vhapi")
    }
)
@Mixin(value = VHAPISyncDescriptor.class, remap = false)
public class MixinVHAPISyncDescriptor {
    @Inject(method = "handle", at = @At("HEAD"), cancellable = true)
    private void disableVHAPISync(NetworkEvent.Context context, CallbackInfo ci){
        if (ServerTransferState.disableVHAPITemplateSync) {
            WoldLogger.debug("VHAPI template sync disabled, skipping handle");
            context.setPacketHandled(true);
            ServerTransferState.disableVHAPITemplateSync = false;
            ci.cancel();
        }
    }
}
