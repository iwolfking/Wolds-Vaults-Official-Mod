package xyz.iwolfking.woldsvaults.events.client;

import com.mrcrayfish.configured.api.util.ConfigScreenHelper;
import iskallia.vault.client.gui.screen.accessibility.InventoryHudEditScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.compat.bettercombat.BetterCombatToggleHelper;
import xyz.iwolfking.woldsvaults.init.client.ModKeybinds;

@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class KeyInputEvents {
    public static boolean isFeatherFixEnabled = true;

    private static final ResourceLocation BACKGROUND = new ResourceLocation("minecraft", "textures/block/stone");

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        if (ModKeybinds.isFeatherFixed.consumeClick()) {
            isFeatherFixEnabled = !isFeatherFixEnabled;

            String state = isFeatherFixEnabled ? "ON" : "OFF";
            var player = Minecraft.getInstance().player;
            if (player != null){
                player.displayClientMessage(new TextComponent("Toggled Feather Fix: " + state).withStyle(ChatFormatting.YELLOW), true);
            }
        }

        if(ModKeybinds.openWoldsVaultsConfig.consumeClick()) {
            ModContainer woldsModContainer =  ModList.get().getModContainerById(WoldsVaults.MOD_ID).orElse(null);
            if(woldsModContainer != null) {
                Screen configScreen = ConfigScreenHelper.createForgeConfigSelectionScreen(new TextComponent("Wold's Vaults Configs"), ModList.get().getModContainerById(WoldsVaults.MOD_ID).orElse(null), BACKGROUND);
                Minecraft.getInstance().setScreen(configScreen);
            }
        }

        if(ModKeybinds.toggleBetterCombat.consumeClick()) {
            BetterCombatToggleHelper.toggleBetterCombat();
        }

        if(ModKeybinds.openInventoryHUD.consumeClick()) {
            Minecraft.getInstance().setScreen(new InventoryHudEditScreen(Minecraft.getInstance().screen));
        }
    }
}
