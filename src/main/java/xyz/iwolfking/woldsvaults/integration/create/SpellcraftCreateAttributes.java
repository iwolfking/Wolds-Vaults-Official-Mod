package xyz.iwolfking.woldsvaults.integration.create;

import com.simibubi.create.content.logistics.filter.ItemAttribute;
import xyz.iwolfking.woldsvaults.augments.AugmentType;

public final class SpellcraftCreateAttributes {
    private SpellcraftCreateAttributes() {
    }

    /**
     * Registers the spellcraft glyph, depth and arcana attributes with Create's attribute filter. Touches Create
     * classes, so call it only when Create is loaded.
     */
    public static void register() {
        ItemAttribute.register(new SpellcraftGlyphAttribute(AugmentType.AMPLIFY_AREA));
        ItemAttribute.register(new SpellcraftDepthAttribute(1));
        ItemAttribute.register(new SpellcraftArcanaAttribute(SpellcraftArcanaAttribute.STEP));
    }
}
