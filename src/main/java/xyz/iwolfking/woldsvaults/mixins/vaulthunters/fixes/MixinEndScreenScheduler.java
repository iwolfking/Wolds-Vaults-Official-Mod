package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.client.gui.screen.summary.VaultEndScreen;
import iskallia.vault.core.event.client.ClientTickEvent;
import iskallia.vault.util.scheduler.EndScreenScheduler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.woldsvaults.WoldsVaults;

@Mixin(value = EndScreenScheduler.class, remap = false)
public abstract class MixinEndScreenScheduler {
    @Shadow
    public static EndScreenScheduler getInstance() {
        return null;
    }

    /**
     * @author iwolfking
     * @reason Test
     */
    @Redirect(method = "onClientTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"), remap = true)
    private static void onSetScreen(Minecraft instance, Screen pGuiScreen) {
        try {
            if(getInstance() != null) {
                if(Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().setScreen(new VaultEndScreen(getInstance().snapshot, new TextComponent("Vault Exit"), Minecraft.getInstance().player.getUUID()));
                    getInstance().snapshot = null;
                }
            }
        } catch (Exception e) {
            WoldsVaults.LOGGER.error("There was an issue displaying the end screen for the vault.");
            e.printStackTrace();
            if(getInstance() != null) {
                getInstance().snapshot = null;
            }
        }
    }
}
