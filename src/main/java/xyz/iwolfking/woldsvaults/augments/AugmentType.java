package xyz.iwolfking.woldsvaults.augments;

import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * The glyphs a focus or wand can roll. Modifiers change the nearest ability, or Bundled Spell orb, before them in a
 * spell; Delay pauses the spell; Bundled Spell throws the rest of the spell as an orb. What each one does per point of
 * potency, and what it fits, is in the focus augments table.
 */
public enum AugmentType {
    AMPLIFY_AREA("Amplify Area"),
    QUICKEN("Quicken"),
    DISCOUNT("Discount"),
    ECHO("Echo"),
    STRENGTHEN("Strengthen"),
    ACCELERATE("Accelerate"),
    HOMING("Homing"),
    ORBIT("Orbit"),
    DELAY("Delay"),
    SIZE_UP("Size Up"),
    SIZE_DOWN("Size Down"),
    ETHEREAL("Ethereal"),
    SHOTGUN("Shotgun"),
    LOW_GRAVITY("Low Gravity"),
    CHARGE("Charge"),
    BUNDLED_SPELL("Bundled Spell");

    public static final String BUNDLE_TRAIT = "bundle";

    private final String displayName;

    AugmentType(String displayName) {
        this.displayName = displayName;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String displayName() {
        return this.displayName;
    }

    @Nullable
    public static AugmentType byId(String id) {
        for (AugmentType type : values()) {
            if (type.id().equalsIgnoreCase(id)) {
                return type;
            }
        }
        return null;
    }

    public boolean isModifier(FocusAugmentsConfig config) {
        return "modifier".equals(config.glyph(this).kind);
    }

    /**
     * Whether this modifier fits a target with the given traits (lower case; an orb is {@link #BUNDLE_TRAIT}): the
     * target has every trait of at least one of the table's required sets.
     */
    public boolean fits(FocusAugmentsConfig config, Set<String> traits) {
        for (List<String> set : config.glyph(this).requires) {
            if (traits.containsAll(set)) {
                return true;
            }
        }
        return false;
    }
}
