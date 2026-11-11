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
    BUNDLED_SPELL("Bundled Spell"),
    INTENSIFY("Intensify"),
    EXTEND("Extend"),
    CHAINING("Chaining"),
    RIME("Rime"),
    WARP_HASTE("Warp Haste"),
    WARP_VELOCITY("Warp Velocity"),
    STEEL_CORE("Steel Core"),
    TWINFIRE("Twinfire"),
    DEEP_CHILL("Deep Chill"),
    IMPLOSION("Implosion"),
    ENTROPIC_BIND("Entropic Bind"),
    WIDE_BLAST("Wide Blast"),
    NARROW_STORM("Narrow Storm"),
    SWIFT_STORM("Swift Storm"),
    DREAD("Dread"),
    QUICK_MEND("Quick Mend");

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
     * Whether this modifier fits a target with the given traits (lower case; an orb is {@link #BUNDLE_TRAIT}) and
     * specialization id (null for an orb): the target has every trait of at least one of the table's required sets,
     * none of its forbidden traits, and is one of its specializations when the glyph is spec-locked.
     */
    public boolean fits(FocusAugmentsConfig config, Set<String> traits, @Nullable String spec) {
        FocusAugmentsConfig.Glyph glyph = config.glyph(this);
        if (!glyph.specs.isEmpty() && (spec == null || !glyph.specs.contains(spec))) {
            return false;
        }
        for (String forbidden : glyph.forbids) {
            if (traits.contains(forbidden.toLowerCase(Locale.ROOT))) {
                return false;
            }
        }
        for (List<String> set : glyph.requires) {
            if (traits.containsAll(set)) {
                return true;
            }
        }
        return false;
    }
}
