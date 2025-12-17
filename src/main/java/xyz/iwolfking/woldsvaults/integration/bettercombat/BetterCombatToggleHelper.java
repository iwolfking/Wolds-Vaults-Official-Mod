package xyz.iwolfking.woldsvaults.integration.bettercombat;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;

@OnlyIn(Dist.CLIENT)
public class BetterCombatToggleHelper {
    public static void toggleBetterCombat() {
        boolean currentValue = WoldsVaultsConfig.CLIENT.weaponsShouldntBeBetter.get();
        WoldsVaultsConfig.CLIENT.weaponsShouldntBeBetter.set(!currentValue);
        WoldsVaultsConfig.CLIENT.weaponsShouldntBeBetter.save();
        Minecraft mc = Minecraft.getInstance();
        if(mc.player != null) {
            mc.player.displayClientMessage(new TextComponent("Better Combat toggled ").withStyle(ChatFormatting.AQUA).append(!currentValue ? new TextComponent("OFF!").withStyle(ChatFormatting.RED) : new TextComponent("ON!").withStyle(ChatFormatting.GREEN)), true);
        }
    }
}
