package xyz.iwolfking.woldsvaults.objectives.data.builtin.events;

import com.github.alexthe668.cloudstorage.entity.CSEntityRegistry;
import iskallia.vault.core.util.WeightedList;
import net.minecraft.world.entity.EntityType;
import xyz.iwolfking.woldsvaults.objectives.data.EnchantedEventsRegistry;
import xyz.iwolfking.woldsvaults.api.core.vault_events.impl.SpawnEntityVaultEvent;

public class CloudStorageEvents {
    public static final SpawnEntityVaultEvent CLOUDSTORAGE_EVENT = new SpawnEntityVaultEvent("Happy Clood Become Angry Clood", "You will understand my pain", "#e6ffff",  new WeightedList<EntityType<?>>().add(CSEntityRegistry.BLOVIATOR.get(), 6.0), new WeightedList<Integer>().add(1, 10).add(2, 10));
    public static void init() {
        EnchantedEventsRegistry.register(CLOUDSTORAGE_EVENT, 8.0, false, false);
    }
}
