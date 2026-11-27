package xyz.iwolfking.woldsvaults.client.spellcraft.stats;

import iskallia.vault.skill.ability.component.AbilityLabelBindingRegistry;
import iskallia.vault.skill.ability.effect.spi.core.Ability;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

final class VhAbilityLabels {
    private static final String NO_BINDING = "NO BINDING";
    private static final Set<String> LOGGED = new HashSet<>();

    private VhAbilityLabels() {
    }

    static void missingLabel(Ability ability, String spec, String label) {
        String id = spec + "." + label;
        if (LOGGED.add(id)) {
            WoldLogger.error("Spellcraft stats: {} ({}) has no VH label '{}'; its main effect line is left out", spec,
                    ability.getClass().getSimpleName(), label);
        }
    }

    @Nullable
    static String binding(Ability ability, String key) {
        String value;
        try {
            value = AbilityLabelBindingRegistry.getBindingValue(ability, key);
        } catch (RuntimeException e) {
            String id = ability.getClass().getSimpleName() + "." + key;
            if (LOGGED.add(id)) {
                WoldLogger.error("Spellcraft stats: VH label {} threw while reading; the stat is left out of the tooltip", id, e);
            }
            return null;
        }
        return value == null || NO_BINDING.equals(value) ? null : value.trim();
    }

    @Nullable
    static Float percent(Ability ability, String key) {
        String value = binding(ability, key);
        if (value == null) {
            return null;
        }
        if (!value.endsWith("%")) {
            unreadable(ability, key, value);
            return null;
        }
        Float parsed = parse(ability, key, value.substring(0, value.length() - 1));
        return parsed == null ? null : parsed / 100.0F;
    }

    @Nullable
    static Float number(Ability ability, String key) {
        String value = binding(ability, key);
        return value == null ? null : parse(ability, key, value);
    }

    /**
     * A Vault Hunters time label such as "1.5s" in ticks, or null for any other form. Only {@code adjustedDuration}
     * also accepts a bare number of seconds.
     */
    @Nullable
    static Integer ticks(Ability ability, String key) {
        String value = binding(ability, key);
        boolean suffixed = value != null && value.endsWith("s");
        if (value == null || (!suffixed && !key.equals("adjustedDuration")) || value.endsWith("%")) {
            return null;
        }
        Float seconds = parse(ability, key, suffixed ? value.substring(0, value.length() - 1) : value);
        return seconds == null ? null : (int) (seconds * 20.0F);
    }

    @Nullable
    static Float parse(Ability ability, String key, String text) {
        try {
            return Float.parseFloat(text.trim());
        } catch (NumberFormatException e) {
            unreadable(ability, key, text);
            return null;
        }
    }

    private static void unreadable(Ability ability, String key, String value) {
        String id = ability.getClass().getSimpleName() + "." + key;
        if (LOGGED.add(id)) {
            WoldLogger.error("Spellcraft stats: VH label {} reads '{}', which is not a number; the stat is left out of the tooltip", id, value);
        }
    }
}
