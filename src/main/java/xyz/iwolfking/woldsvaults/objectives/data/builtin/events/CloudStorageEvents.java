package xyz.iwolfking.woldsvaults.objectives.data.builtin.events;

import com.github.alexthe668.cloudstorage.entity.CSEntityRegistry;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.vault_events.VaultEvent;
import xyz.iwolfking.woldsvaults.api.core.vault_events.impl.tasks.SpawnMobTask;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.EventTag;
import xyz.iwolfking.woldsvaults.objectives.data.EnchantedEventsRegistry;

public class CloudStorageEvents {

    public static void init() {
        EnchantedEventsRegistry.register(WoldsVaults.id("cloudstorage_event"), new VaultEvent.Builder()
                .tag(EventTag.NEGATIVE)
                .tag(EventTag.SPAWN_MOB)
                .color(TextColor.parseColor("#e6ffff"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new SpawnMobTask.Builder()
                        .entity(CSEntityRegistry.BLOVIATOR.get(), 1.0)
                        .amount(1, 10)
                        .amount(2, 5)
                        .amount(4, 1)
                        .build()
                )
                .build("Forecast - Cloudy", new TextComponent("Spawns angry cloud(s) to assault you!")), 9.0);
    }
}
