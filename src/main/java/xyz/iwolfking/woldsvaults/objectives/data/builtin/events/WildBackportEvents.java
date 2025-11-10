package xyz.iwolfking.woldsvaults.objectives.data.builtin.events;

import com.cursedcauldron.wildbackport.common.registry.WBMobEffects;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.vault_events.VaultEvent;
import xyz.iwolfking.woldsvaults.api.core.vault_events.impl.tasks.PlayerMobEffectTask;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.EventTag;
import xyz.iwolfking.woldsvaults.objectives.data.EnchantedEventsRegistry;

public class WildBackportEvents {
    public static void init() {
        EnchantedEventsRegistry.register(WoldsVaults.id("lights_out"), new VaultEvent.Builder()
                .tag(EventTag.NEGATIVE)
                .color(TextColor.parseColor("#1a1a00"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new PlayerMobEffectTask.Builder()
                        .effect(WBMobEffects.DARKNESS.get(), 200, 600)
                        .build()
                )
                .build("Lights Out", new TextComponent("Who turned out the lights!?")), 6.0);
    }
}
