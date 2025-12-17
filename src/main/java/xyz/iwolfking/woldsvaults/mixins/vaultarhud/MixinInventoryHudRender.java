package xyz.iwolfking.woldsvaults.mixins.vaultarhud;

import io.iridium.vaultarhud.renderers.HUDInventoryRenderer;
import io.iridium.vaultarhud.util.Point;
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
@Mixin(value = HUDInventoryRenderer.class, remap = false)
public abstract class MixinInventoryHudRender {

    /**
     * @return
     * @author iwolfking
     * @reason Replace ScreenValidator with WoldScreenValidator
     */
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lio/iridium/vaultarhud/util/ScreenValidator;getScreenHUDCoordinates(Lnet/minecraft/client/gui/screens/Screen;Lio/iridium/vaultarhud/util/Point;)Lio/iridium/vaultarhud/util/Point;"))
    private static Point render(Screen screen, Point point) {
        return WoldScreenValidator.getScreenHUDCoordinates(screen, point);
    }



}
