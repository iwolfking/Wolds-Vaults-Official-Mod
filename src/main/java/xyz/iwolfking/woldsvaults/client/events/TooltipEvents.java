package xyz.iwolfking.woldsvaults.client.events;

import iskallia.vault.config.TrinketConfig;
import iskallia.vault.gear.trinket.TrinketEffect;
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
import xyz.iwolfking.woldsvaults.items.CombinedTrinketItem;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class TooltipEvents {
    private static boolean loggedInsertFallback = false;

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!SpeedLimitTrinketEffect.hasBootsEffect(stack) || !TrinketItem.isIdentified(stack)) {
            return;
        }
        int capPercent = SpeedLimitTrinketEffect.getCapPercent(stack);
        Component line = new TextComponent("Current speed limit: " + (capPercent <= 0 ? "Uncapped" : capPercent + "%")).withStyle(ChatFormatting.GRAY);
        List<Component> tooltip = event.getToolTip();
        int insertAt = findInsertIndex(stack, tooltip);
        if (insertAt >= 0) {
            tooltip.add(insertAt, line);
            return;
        }
        tooltip.add(line);
        if (!loggedInsertFallback) {
            loggedInsertFallback = true;
            WoldsVaults.LOGGER.warn("Weighted Boots: could not find the expected tooltip position, appended the speed limit line at the end instead.");
        }
    }

    private static int findInsertIndex(ItemStack stack, List<Component> tooltip) {
        if (stack.getItem() instanceof CombinedTrinketItem) {
            String bootsName = null;
            Set<String> otherNames = new HashSet<>();
            for (TrinketEffect<?> effect : CombinedTrinketItem.getTrinkets(stack)) {
                TrinketConfig.Trinket cfg = effect.getTrinketConfig();
                if (cfg == null) {
                    continue;
                }
                if (effect instanceof SpeedLimitTrinketEffect) {
                    bootsName = cfg.getName();
                } else {
                    otherNames.add(cfg.getName());
                }
            }
            if (bootsName != null) {
                for (int i = 1; i < tooltip.size(); i++) {
                    if (!tooltip.get(i).getString().equals(bootsName)) {
                        continue;
                    }
                    int insertAt = i + 1;
                    while (insertAt < tooltip.size() && !tooltip.get(insertAt).getString().isEmpty() && !otherNames.contains(tooltip.get(insertAt).getString())) {
                        insertAt++;
                    }
                    return insertAt;
                }
            }
        }
        for (int i = 1; i < tooltip.size(); i++) {
            if (tooltip.get(i).getString().isEmpty()) {
                return i;
            }
        }
        return -1;
    }
}
