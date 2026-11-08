package xyz.iwolfking.woldsvaults.augments;

import javax.annotation.Nullable;
import java.util.Locale;

/**
 * The augments a wand or focus can roll. Most modify the closest ability (or Bundled Spell) before them in a
 * recipe; Delay pauses the recipe and Bundled Spell throws the rest of it as a projectile.
 * <p>
 * Table strength per glyph: Amplify Area adds to the area-of-effect stat; Quicken adds to the cooldown reduction
 * stat; Discount refunds a share of the mana spent; Strengthen adds to the damage multiplier; Accelerate adds to
 * projectile speed; Size Up multiplies scale by {@code 1 + s}, Size Down by {@code 1 - s}; Low Gravity removes a
 * share of gravity; Shotgun multiplies each copy's damage by {@code s} per split; Delay pauses {@code s} seconds;
 * Charge adds {@code s} to Rail's damage multiplier. Echo, Homing, Orbit, Ethereal and Bundled Spell read their
 * tuning instead.
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

    /** Whether this augment can modify the given ability. Delay and Bundled Spell are recipe steps, not modifiers. */
    public boolean canModify(AugmentAbility ability) {
        boolean instant = ability.has(AbilityTrait.INSTANT);
        boolean projectile = ability.has(AbilityTrait.PROJECTILE);
        return switch (this) {
            case AMPLIFY_AREA -> instant && (ability.has(AbilityTrait.AREA) || projectile);
            case QUICKEN -> instant || ability.has(AbilityTrait.TOGGLE);
            case DISCOUNT -> ability.has(AbilityTrait.MANA);
            case ECHO, STRENGTHEN -> instant;
            case SIZE_UP, SHOTGUN -> instant && (projectile || ability.has(AbilityTrait.BOLT));
            case ETHEREAL, ACCELERATE, HOMING, ORBIT, LOW_GRAVITY, SIZE_DOWN -> instant && projectile;
            case CHARGE -> instant && ability.has(AbilityTrait.BOLT);
            case DELAY, BUNDLED_SPELL -> false;
        };
    }

    /** Whether this augment can modify a Bundled Spell projectile. */
    public boolean canModifyBundle() {
        return switch (this) {
            case ACCELERATE, HOMING, ORBIT, SIZE_UP, SIZE_DOWN, ETHEREAL, SHOTGUN -> true;
            default -> false;
        };
    }
}
