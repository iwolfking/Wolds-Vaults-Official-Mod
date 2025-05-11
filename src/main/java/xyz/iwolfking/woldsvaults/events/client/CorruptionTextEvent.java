package xyz.iwolfking.woldsvaults.events.client;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.util.ComponentUtils;
import xyz.iwolfking.woldsvaults.util.CorruptedVaultHelper;

import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = {Dist.CLIENT})
public class CorruptionTextEvent {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void corruptTooltips(ItemTooltipEvent event) {
        if(!CorruptedVaultHelper.isVaultCorrupted) return;

        List<Component> toolTip = event.getToolTip();

        for (int i = 0; i < toolTip.size(); i++) {
            if(toolTip.get(i) instanceof MutableComponent cmp) {
                toolTip.set(i, ComponentUtils.corruptComponent(cmp));
            }
        }
    }

}
