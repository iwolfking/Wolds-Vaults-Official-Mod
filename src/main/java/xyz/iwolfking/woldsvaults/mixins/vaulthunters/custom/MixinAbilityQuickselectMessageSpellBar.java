package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.network.message.AbilityQuickselectMessage;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(value = AbilityQuickselectMessage.class, remap = false)
public abstract class MixinAbilityQuickselectMessageSpellBar {

    /** VH's per-ability quick-fire keys are retired: abilities are cast from spell slots, so the packet does nothing. */
    @Inject(method = "handle", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$retireQuickfire(AbilityQuickselectMessage message, Supplier<NetworkEvent.Context> context, CallbackInfo ci) {
        context.get().setPacketHandled(true);
        ci.cancel();
    }
}
