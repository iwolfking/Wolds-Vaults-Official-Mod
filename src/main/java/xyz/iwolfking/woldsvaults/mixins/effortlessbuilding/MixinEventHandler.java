package xyz.iwolfking.woldsvaults.mixins.effortlessbuilding;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.block.CrystalWorkbenchBlock;
import iskallia.vault.block.base.InventoryRetainerBlock;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraftforge.event.world.BlockEvent;
import nl.requios.effortlessbuilding.EventHandler;
import nl.requios.effortlessbuilding.buildmode.BuildModes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "effortlessbuilding")
        }
)
@Mixin(value = EventHandler.class, remap = false)
public abstract class MixinEventHandler {

    @Inject(method = "onBlockPlaced", at = @At(value = "INVOKE", target = "Lnl/requios/effortlessbuilding/buildmodifier/ModifierSettingsManager;getModifierSettings(Lnet/minecraft/world/entity/player/Player;)Lnl/requios/effortlessbuilding/buildmodifier/ModifierSettingsManager$ModifierSettings;"), cancellable = true)
    private static void preventPlacingRetainerBlocks(BlockEvent.EntityPlaceEvent event, CallbackInfo ci, @Local(name = "buildMode") BuildModes.BuildModeEnum buildMode) {
        if(buildMode != BuildModes.BuildModeEnum.NORMAL && event.getPlacedBlock().getBlock() instanceof InventoryRetainerBlock<?> || event.getPlacedBlock().getBlock() instanceof CrystalWorkbenchBlock) {
            ci.cancel();
        }
    }
}
