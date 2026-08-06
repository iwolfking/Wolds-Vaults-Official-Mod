package xyz.iwolfking.woldsvaults.client.events;

import iskallia.vault.item.gear.TrinketItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.effect.trinkets.SpeedLimitTrinketEffect;

import java.util.List;

@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class TooltipEvents {
    private static boolean loggedInsertFallback = false;

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof TrinketItem) || !TrinketItem.isIdentified(stack) || !(TrinketItem.getTrinket(stack).orElse(null) instanceof SpeedLimitTrinketEffect)) {
            return;
        }
        int capPercent = SpeedLimitTrinketEffect.getCapPercent(stack);
        Component line = new TextComponent("Current speed limit: " + (capPercent <= 0 ? "Uncapped" : capPercent + "%")).withStyle(ChatFormatting.GRAY);
        List<Component> tooltip = event.getToolTip();
        for (int i = 1; i < tooltip.size(); i++) {
            if (tooltip.get(i).getString().isEmpty()) {
                tooltip.add(i, line);
                return;
            }
        }
        tooltip.add(line);
        if (!loggedInsertFallback) {
            loggedInsertFallback = true;
            WoldsVaults.LOGGER.warn("Weighted Boots: could not find the expected blank tooltip line, appended the speed limit line at the end instead.");
        }
    }
}
