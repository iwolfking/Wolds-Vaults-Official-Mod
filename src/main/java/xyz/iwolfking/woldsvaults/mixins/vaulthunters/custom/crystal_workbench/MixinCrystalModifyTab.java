package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.crystal_workbench;

import iskallia.vault.block.entity.CrystalWorkbenchWorkspace;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.world.theme.ThemeSeries;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(targets = "iskallia.vault.client.gui.screen.block.crystal.CrystalModifyTab", remap = false)
public class MixinCrystalModifyTab {
    @Shadow
    private static int nameColour(ItemStack item) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    /**
     * @author iwolfking
     * @reason
     */
    @Overwrite
    private static int socketColour(CrystalWorkbenchWorkspace.Socket socket, ItemStack item, @Nullable CrystalData shown) {
        int named = nameColour(item);
        int socketColour;
        if(socket.equals(CrystalWorkbenchWorkspace.Socket.SEAL)) {
            socketColour = shown == null ? 16755200 : shown.getObjective().getColor(0.0F).orElse(16755200);
        }
        else if(socket.equals(CrystalWorkbenchWorkspace.Socket.AUGMENT)) {
            socketColour = shown == null ? named : ThemeSeries.ofCrystal(shown).map(ThemeSeries::color).orElse(named);
        }
        else if(socket.equals(CrystalWorkbenchWorkspace.Socket.OBJECTIVE_MODIFIER)) {
            socketColour = 11114186;
        }
        else if(socket.equals(CrystalWorkbenchWorkspace.Socket.CAPSTONE)) {
            socketColour= named == 16777215 ? 16759296 : named;
        }
        else if(socket.equals(CrystalWorkbenchWorkspace.Socket.SIGIL)) {
            socketColour = named;
        }
        else if(socket.equals(CrystalWorkbenchWorkspace.Socket.valueOf("MISC"))) {
            socketColour = named;
        }
        else {
            socketColour = named;
        }

        return socketColour;
    }

    @Inject(method = "drawGodSocket", at = @At("HEAD"), cancellable = true)
    private void howAboutNotDrawingUnfinishedStuffHuh(CallbackInfo ci) {
        ci.cancel();
    }


}
