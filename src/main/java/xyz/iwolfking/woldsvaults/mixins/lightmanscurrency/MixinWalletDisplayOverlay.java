package xyz.iwolfking.woldsvaults.mixins.lightmanscurrency;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.client.gui.overlay.WalletDisplayOverlay;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraftforge.client.gui.ForgeIngameGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "lightmanscurrency")
        }
)
@Mixin(value = WalletDisplayOverlay.class, remap = false)
public class MixinWalletDisplayOverlay {

    /**
     * @author radimous
     * @reason completely remove the overlay, replaced by {@link xyz.iwolfking.woldsvaults.client.invhud.LightmanWalletHudModule}
     */
    @Overwrite
    public void render(ForgeIngameGui fgui, PoseStack pose, float partialTick, int screenWidth, int screenHeight) {}
}
