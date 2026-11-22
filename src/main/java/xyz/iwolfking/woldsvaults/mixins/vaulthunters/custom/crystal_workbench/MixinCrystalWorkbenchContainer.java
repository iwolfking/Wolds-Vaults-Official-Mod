package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.crystal_workbench;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import iskallia.vault.block.entity.CrystalWorkbenchWorkspace;
import iskallia.vault.container.CrystalWorkbenchContainer;
import iskallia.vault.init.ModSlotIcons;
import iskallia.vault.item.tool.LeanJewels;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(value = CrystalWorkbenchContainer.class, remap = false)
public class MixinCrystalWorkbenchContainer {

    @ModifyReturnValue(method = "socketPositions", at = @At("RETURN"))
    private static Map<CrystalWorkbenchWorkspace.Socket, int[]> woldsVaults$addMiscSocketPosition(
            Map<CrystalWorkbenchWorkspace.Socket, int[]> positions
    ) {
        positions.putIfAbsent(CrystalWorkbenchWorkspace.Socket.valueOf("MISC"), new int[]{8, 33});
        return positions;
    }

    //TODO: Use more fitting icon
    @Inject(method = "socketIcon", at = @At("HEAD"), cancellable = true)
    private static void woldsVaults$miscSocketIcon(
            CrystalWorkbenchWorkspace.Socket socket,
            CallbackInfoReturnable<ResourceLocation> cir
    ) {
        if ("MISC".equals(socket.name())) {
            cir.setReturnValue(ModSlotIcons.CARD_UPGRADE_NO_ITEM);
        }
    }
}