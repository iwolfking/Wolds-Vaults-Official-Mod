package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.bar;

import iskallia.vault.network.message.AbilityQuickselectMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.AbilityQuickselectMessageAccessor;
import xyz.iwolfking.woldsvaults.spellcraft.play.SpellBarInput;

import java.util.function.Supplier;

@Mixin(value = AbilityQuickselectMessage.class, remap = false)
public abstract class MixinAbilityQuickselectMessageSpellBar {

    /**
     * Routes VH's per-ability quick-fire keys through the spell bar: in classic mode the key casts the slot that casts
     * its ability, and in slot mode it does nothing. VH's own handling never runs.
     */
    @Inject(method = "handle", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$quickfireThroughBar(AbilityQuickselectMessage message, Supplier<NetworkEvent.Context> context, CallbackInfo ci) {
        NetworkEvent.Context ctx = context.get();
        AbilityQuickselectMessageAccessor packet = (AbilityQuickselectMessageAccessor) message;
        String group = packet.woldsvaults$getAbilityName();
        int action = packet.woldsvaults$getAction();
        if (action == AbilityQuickselectMessage.PRESSED || action == AbilityQuickselectMessage.RELEASED) {
            ctx.enqueueWork(() -> {
                ServerPlayer sender = ctx.getSender();
                if (sender != null) {
                    SpellBarInput.onQuickfire(sender, group, action == AbilityQuickselectMessage.PRESSED);
                }
            });
        }
        ctx.setPacketHandled(true);
        ci.cancel();
    }
}
