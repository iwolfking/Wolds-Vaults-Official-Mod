package xyz.iwolfking.woldsvaults.augments;

import iskallia.vault.init.ModConfigs;
import iskallia.vault.skill.base.Skill;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * The abilities recipes know, built from the focus augments table's trait list and rebuilt whenever a new table is
 * installed. An ability missing from the table cannot appear in a recipe or trigger one.
 */
public final class AugmentAbilities {
    private static FocusAugmentsConfig builtFrom;
    private static Map<String, AugmentAbility> byId = Collections.emptyMap();

    private AugmentAbilities() {
    }

    @Nullable
    public static AugmentAbility get(FocusAugmentsConfig config, String id) {
        return all(config).get(id);
    }

    public static synchronized Map<String, AugmentAbility> all(FocusAugmentsConfig config) {
        if (config != builtFrom) {
            Map<String, AugmentAbility> built = new LinkedHashMap<>();
            for (Map.Entry<String, List<String>> entry : config.abilityTraits.entrySet()) {
                EnumSet<AbilityTrait> traits = EnumSet.noneOf(AbilityTrait.class);
                for (String trait : entry.getValue()) {
                    traits.add(AbilityTrait.valueOf(trait.toUpperCase(Locale.ROOT)));
                }
                if (!traits.contains(AbilityTrait.INSTANT) && !traits.contains(AbilityTrait.TOGGLE) && !traits.contains(AbilityTrait.HOLD)) {
                    WoldLogger.error("focus_augments.json: ability {} has no cast kind (INSTANT, TOGGLE or HOLD); recipes can't use it", entry.getKey());
                    continue;
                }
                built.put(entry.getKey(), new AugmentAbility(entry.getKey(), traits));
            }
            byId = Collections.unmodifiableMap(built);
            builtFrom = config;
        }
        return byId;
    }

    /** The name VH's skill screen shows for an ability, or its id when VH's config doesn't know it. */
    public static String displayName(String id) {
        return ModConfigs.ABILITIES == null ? id : ModConfigs.ABILITIES.getAbilityById(id).map(Skill::getName).orElse(id);
    }
}
