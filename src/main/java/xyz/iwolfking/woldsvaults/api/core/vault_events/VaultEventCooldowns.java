package xyz.iwolfking.woldsvaults.api.core.vault_events;

import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.EventTag;

public class VaultEventCooldowns {
    public static int getCooldown(VaultEvent event) {
        if(event.getEventTags().contains(EventTag.INFINITE_COOLDOWN)) {
            return Integer.MAX_VALUE;
        }
        else if (event.getEventTags().contains(EventTag.SUPER_LONG_COOLDOWN)) {
            return 20 * 600;
        }
        else if (event.getEventTags().contains(EventTag.EXTRA_LONG_COOLDOWN)) {
            return 20 * 300;
        }
        else if (event.getEventTags().contains(EventTag.LONG_COOLDOWN)) {
            return 20 * 180;
        }
        else if (event.getEventTags().contains(EventTag.SHORT_COOLDOWN)) {
            return 20 * 60;
        }

        return 20 * 100;
    }
}
