package xyz.iwolfking.woldsvaults.gods.trees.velara;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.ArrayList;
import java.util.List;

/** The effect list Velara's Immune nodes avoid, shared with the mythic charm roll, and its key. */
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

    /** The registered effects of {@link #IDS}, resolved once. An unresolvable id is dropped. */
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
