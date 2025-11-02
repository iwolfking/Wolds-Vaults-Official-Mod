package xyz.iwolfking.woldsvaults.objectives.data.builtin.events;

import com.cursedcauldron.wildbackport.common.registry.WBMobEffects;
import xyz.iwolfking.woldsvaults.objectives.data.EnchantedEventsRegistry;
import xyz.iwolfking.woldsvaults.api.core.vault_events.impl.PotionEffectVaultEvent;

public class WildBackportEvents {
    public static final PotionEffectVaultEvent DARKNESS_EVENT = new PotionEffectVaultEvent("Lights Out", "Who turned out the lights!?", "#1a1a00", WBMobEffects.DARKNESS.get(), 600, 200);
    public static void init() {
        EnchantedEventsRegistry.register(DARKNESS_EVENT, 14.0, false, false);
    }
}
