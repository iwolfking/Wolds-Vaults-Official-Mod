package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import xyz.iwolfking.woldsvaults.augments.AbilityTrait;
import xyz.iwolfking.woldsvaults.augments.AugmentType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Focus augments, generated as {@code vault_configs/focus_augments/focus_augments.json}: what each augment does per glyph,
 * how many fit on one cast, how they roll onto wands and focuses, the recipe limits, and which abilities take
 * which augments. Ability mana costs and cooldowns are never set here; they stay VH's own.
 */
public class FocusAugmentsConfig extends Config implements WoldsTable {
    @Expose public boolean worksOutsideVaults;
    @Expose public Limits limits;
    @Expose public Tuning tuning;
    @Expose public Map<String, AugmentEntry> augments;
    @Expose public List<SocketTier> sockets;
    @Expose public Map<String, List<String>> abilityTraits;
    @Expose public List<String> disabledAbilities;

    public static class Limits {
        @Expose public int maxRecipeLength;
        @Expose public int maxCastsPerTrigger;
        @Expose public int maxRecipesPerItem;
        @Expose public int maxDelaysPerRecipe;
        @Expose public int maxEchoesPerRecipe;
        @Expose public int maxBundleDepth;
    }

    public static class Tuning {
        @Expose public float echoDelaySeconds;
        @Expose public boolean echoKeepsCharge;
        @Expose public float shotgunSpreadDegrees;
        @Expose public float shotgunCopyManaShare;
        @Expose public float homingRange;
        @Expose public float homingTurnRate;
        @Expose public int homingRetargetTicks;
        @Expose public float orbitRadius;
        @Expose public int orbitMaxSeconds;
        @Expose public int etherealMaxSeconds;
        @Expose public int bundleMaxSeconds;
        @Expose public float bundleBaseSpeed;
        @Expose public float chargeSecondsPerGlyph;
        @Expose public float railRange;
        @Expose public float sizeUpMaxScale;
        @Expose public float sizeDownMinScale;
    }

    public static class AugmentEntry {
        @Expose public float strength;
        @Expose public int maxPerCast;
        @Expose public int rollWeight;
        @Expose public int minLevel;
        @Expose public int maxTokensPerRoll;
    }

    public static class SocketTier {
        @Expose public int minLevel;
        @Expose public int minSockets;
        @Expose public int maxSockets;
    }

    @Override
    public String getName() {
        return "focus_augments";
    }

    @Override
    protected void reset() {
    }

    /** The table entry for an augment; present on every installed table ({@link #validate()} checks it). */
    public AugmentEntry entry(AugmentType type) {
        return this.augments.get(type.id());
    }

    /** Strength per glyph of an augment; what it means is documented on each {@link AugmentType}. */
    public float strength(AugmentType type) {
        return entry(type).strength;
    }

    /** Most glyphs of this augment one cast (or one recipe, for Echo and Delay) may carry. */
    public int maxPerCast(AugmentType type) {
        return entry(type).maxPerCast;
    }

    /** The socket tier for an item level: the highest tier whose minimum level the item reaches. */
    @Nullable
    public SocketTier socketTier(int itemLevel) {
        SocketTier best = null;
        for (SocketTier tier : this.sockets) {
            if (tier.minLevel <= itemLevel && (best == null || tier.minLevel > best.minLevel)) {
                best = tier;
            }
        }
        return best;
    }

    public boolean isDisabled(String abilityId) {
        return this.disabledAbilities.contains(abilityId);
    }

    @Nullable
    @Override
    public String validate() {
        if (this.limits == null || this.tuning == null || this.augments == null || this.sockets == null
                || this.abilityTraits == null || this.disabledAbilities == null) {
            return "a section is missing";
        }
        for (AugmentType type : AugmentType.values()) {
            AugmentEntry entry = this.augments.get(type.id());
            if (entry == null) {
                return "augment " + type.id() + " has no entry";
            }
            if (entry.strength < 0 || entry.maxPerCast < 0 || entry.rollWeight < 0 || entry.maxTokensPerRoll < 1) {
                return "augment " + type.id() + " has a negative value or rolls no tokens";
            }
        }
        if (this.augments.size() != AugmentType.values().length) {
            return "it has entries for augments that do not exist";
        }
        if (this.limits.maxRecipeLength < 1 || this.limits.maxCastsPerTrigger < 1 || this.limits.maxRecipesPerItem < 1) {
            return "a recipe limit is below 1";
        }
        if (this.tuning.sizeUpMaxScale < 1 || this.tuning.sizeDownMinScale <= 0 || this.tuning.sizeDownMinScale > 1) {
            return "the size limits are out of range";
        }
        if (this.tuning.shotgunCopyManaShare < 0 || this.tuning.shotgunCopyManaShare > 1) {
            return "the Shotgun copy mana share is outside 0-1";
        }
        if (this.sockets.isEmpty() || socketTier(0) == null) {
            return "no socket tier starts at level 0";
        }
        for (SocketTier tier : this.sockets) {
            if (tier.minSockets < 0 || tier.maxSockets < tier.minSockets) {
                return "socket tier at level " + tier.minLevel + " has an invalid range";
            }
        }
        for (Map.Entry<String, List<String>> ability : this.abilityTraits.entrySet()) {
            for (String trait : ability.getValue()) {
                try {
                    AbilityTrait.valueOf(trait.toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException e) {
                    return "ability " + ability.getKey() + " has an unknown trait " + trait;
                }
            }
        }
        return null;
    }
}
