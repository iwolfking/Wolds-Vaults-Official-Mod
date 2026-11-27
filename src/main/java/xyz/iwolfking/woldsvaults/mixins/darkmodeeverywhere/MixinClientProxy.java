package xyz.iwolfking.woldsvaults.mixins.darkmodeeverywhere;

import com.buuz135.darkmodeeverywhere.ClientProxy;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraftforge.client.event.ScreenEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "darkmodeeverywhere"),
    }
)
@Mixin(value = ClientProxy.class, remap = false)
public class MixinClientProxy {

    /** Keeps the Dark Mode button off the vault player screens, where it covers the spell bar. */
    @Inject(method = "openGui", at = @At("HEAD"), cancellable = true)
    private void woldsvaults$skipVaultPlayerScreens(ScreenEvent.InitScreenEvent event, CallbackInfo ci) {
        if (event.getScreen().getClass().getName().startsWith("iskallia.vault.client.gui.screen.player.")) {
            ci.cancel();
        }
    }
}
