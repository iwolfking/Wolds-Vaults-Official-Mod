package xyz.iwolfking.woldsvaults.gods.trees.velara;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.ArrayList;
import java.util.List;

/**
 * The effect list Velara's Immune nodes avoid, and the display key their avoidance instance is
 * grouped under.
 *
 * <p>It lives on its own because two unrelated consumers need exactly the same list: the Immune
 * node handler, and the mythic charm roll that grants the same avoidance as a charm affix. A
 * charm rolling a different set from the node would read as a bug to a player comparing them.
 */
public final class VelaraBadEffects {
    public static final String KEY = "the_vault.gear_attribute.effect_avoidance.avoidance.bad_effects";

    private static final String[] IDS = {
            "poison",
            "wither",
            "levitation",
            "slowness",
            "blindness",
            "hunger",
            "the_vault:bleed",
            "the_vault:chilled",
            "the_vault:corruption"
    };

    private static List<MobEffect> resolved;

    private VelaraBadEffects() {
    }

    /**
     * The registered effects of {@link #IDS}, resolved once. An id that no loaded mod provides is
     * logged and dropped rather than failing the list, so a pack that removes an effect loses that
     * one entry instead of the whole node.
     */
    public static synchronized List<MobEffect> resolve() {
        if (resolved != null) {
            return resolved;
        }
        List<MobEffect> effects = new ArrayList<>();
        for (String id : IDS) {
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(ResourceLocation.parse(id));
            if (effect == null) {
                WoldsVaults.LOGGER.error("Velara Immune could not resolve bad effect {}; it is dropped from the avoidance list.", id);
                continue;
            }
            effects.add(effect);
        }
        resolved = List.copyOf(effects);
        return resolved;
    }
}
