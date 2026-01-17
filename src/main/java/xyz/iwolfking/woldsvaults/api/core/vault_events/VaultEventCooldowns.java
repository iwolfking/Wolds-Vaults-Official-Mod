package xyz.iwolfking.woldsvaults.api.core.vault_events;

import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.EventTag;

public class VaultEventCooldowns {
    public static int getCooldown(VaultEvent event) {
        if (event.getEventTags().contains(EventTag.LONG_COOLDOWN)) {
            return 20 * 120;
        }
        if (event.getEventTags().contains(EventTag.SHORT_COOLDOWN)) {
            return 20 * 30;
        }
        return 20 * 60;
    }
}
