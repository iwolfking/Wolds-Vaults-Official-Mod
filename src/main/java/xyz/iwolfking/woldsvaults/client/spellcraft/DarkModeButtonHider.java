package xyz.iwolfking.woldsvaults.client.spellcraft;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.ArrayList;
import java.util.List;

/**
 * Dark Mode Everywhere adds its Dark Mode / Light Mode button to every container screen at a fixed spot near the
 * bottom-left corner, where it covers the spell bar on the vault player screens (abilities, talents, expertise).
 * This removes the button from those screens only; it stays everywhere else.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, value = Dist.CLIENT)
public final class DarkModeButtonHider {
    private static final String SCREEN_PACKAGE = "iskallia.vault.client.gui.screen.player.";
    private static final List<String> BUTTON_KEYS = List.of("gui.darkmodeeverywhere.dark_mode", "gui.darkmodeeverywhere.light_mode");

    private DarkModeButtonHider() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onInit(ScreenEvent.InitScreenEvent.Post event) {
        if (!event.getScreen().getClass().getName().startsWith(SCREEN_PACKAGE)) {
            return;
        }
        for (GuiEventListener listener : new ArrayList<>(event.getListenersList())) {
            if (listener instanceof AbstractWidget widget && widget.getMessage() instanceof TranslatableComponent text
                    && BUTTON_KEYS.contains(text.getKey())) {
                event.removeListener(listener);
            }
        }
    }
}
