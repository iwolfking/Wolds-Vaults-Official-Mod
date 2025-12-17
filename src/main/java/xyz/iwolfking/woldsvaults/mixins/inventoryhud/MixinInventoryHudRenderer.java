package xyz.iwolfking.woldsvaults.mixins.inventoryhud;

import dlovin.inventoryhud.InventoryHUD;
import dlovin.inventoryhud.gui.InventoryGui;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "inventoryhud")
    }
)
@Mixin(value = InventoryGui.class, remap = false)
public class MixinInventoryHudRenderer {
}
