package xyz.iwolfking.woldsvaults.spellcraft.source;

import net.minecraft.util.Mth;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;

import javax.annotation.Nullable;

public final class GlyphQuality {
    private GlyphQuality() {
    }

    public static float tierQuality(SpellcraftConfig.GlyphDefinition glyph, float potency) {
        SpellcraftConfig.PotencyTier top = null;
        for (SpellcraftConfig.PotencyTier tier : glyph.potency.tiers) {
            if (top == null || tier.minLevel > top.minLevel) {
                top = tier;
            }
        }
        if (top == null || top.max <= top.min) {
            return 0.0F;
        }
        return Mth.clamp((potency - top.min) / (top.max - top.min), 0.0F, 1.0F);
    }

    public static float linkedQuality(@Nullable SpellcraftConfig.Linked linked, float value) {
        return linked == null || linked.max <= linked.min ? 0.0F : Mth.clamp((value - linked.min) / (linked.max - linked.min), 0.0F, 1.0F);
    }

    public static float linkedValue(SpellcraftConfig.Linked linked, float quality) {
        return linked.min + (linked.max - linked.min) * quality;
    }
}
