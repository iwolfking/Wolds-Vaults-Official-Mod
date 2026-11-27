package xyz.iwolfking.woldsvaults.spellcraft.ability;

import iskallia.vault.init.ModConfigs;
import iskallia.vault.skill.base.Skill;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * The abilities spells know, built from the spellcraft table's trait list and rebuilt whenever a new table is
 * installed. An ability missing from the table cannot appear in a spell or trigger one.
 */
public final class SpellAbilities {
    private static final Set<String> HEALS = Set.of("Heal_Base", "Heal_Group");
    private static SpellcraftConfig builtFrom;
    private static Map<String, SpellAbility> byId = Collections.emptyMap();

    private SpellAbilities() {
    }

    @Nullable
    public static SpellAbility get(SpellcraftConfig config, String id) {
        return all(config).get(id);
    }

    public static synchronized Map<String, SpellAbility> all(SpellcraftConfig config) {
        if (config != builtFrom) {
            Map<String, SpellAbility> built = new LinkedHashMap<>();
            for (Map.Entry<String, List<String>> entry : config.abilityTraits.entrySet()) {
                EnumSet<AbilityTrait> traits = EnumSet.noneOf(AbilityTrait.class);
                for (String trait : entry.getValue()) {
                    traits.add(AbilityTrait.valueOf(trait.toUpperCase(Locale.ROOT)));
                }
                if (!traits.contains(AbilityTrait.INSTANT) && !traits.contains(AbilityTrait.TOGGLE) && !traits.contains(AbilityTrait.HOLD)) {
                    WoldLogger.error("spellcraft.json: ability {} has no cast kind (INSTANT, TOGGLE or HOLD); spells can't use it", entry.getKey());
                    continue;
                }
                built.put(entry.getKey(), new SpellAbility(entry.getKey(), traits));
            }
            byId = Collections.unmodifiableMap(built);
            builtFrom = config;
        }
        return byId;
    }

    /**
     * Whether a step casting {@code spec} bills its heal mana factor (Intensify on a heal costs mana, not duration).
     */
    public static boolean isHeal(@Nullable String spec) {
        return spec != null && HEALS.contains(spec);
    }

    /** The name VH's skill screen shows for an ability, or its id when VH's config doesn't know it. */
    public static String displayName(String id) {
        return ModConfigs.ABILITIES == null ? id : ModConfigs.ABILITIES.getAbilityById(id).map(Skill::getName).orElse(id);
    }
}
