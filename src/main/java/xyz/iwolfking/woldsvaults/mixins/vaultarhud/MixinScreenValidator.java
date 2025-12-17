package xyz.iwolfking.woldsvaults.mixins.vaultarhud;

import io.iridium.vaultarhud.VaultarHUDOverlay;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.woldsvaults.integration.vaultarhud.WoldScreenValidator;
@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "vaultarhud")
        }
)
@Mixin(value = VaultarHUDOverlay.class, remap = false)
public abstract class MixinScreenValidator {

    /**
     * @return
     * @author iwolfking
     * @reason Add vaultarhud compat hack
     */
    @Redirect(method = "onGUIScreenDraw", at = @At(value = "INVOKE", target = "Lio/iridium/vaultarhud/util/ScreenValidator;isValidScreen(Lnet/minecraft/client/gui/screens/Screen;)Z"))
    private static boolean replaceWithWoldValidator(Screen screen) {
        return WoldScreenValidator.isValidScreen(screen);
    }
}
