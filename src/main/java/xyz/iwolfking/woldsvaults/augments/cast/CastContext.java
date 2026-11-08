package xyz.iwolfking.woldsvaults.augments.cast;

import xyz.iwolfking.woldsvaults.augments.AugmentAbility;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * How one run of a recipe timeline casts. {@code origin} is null to cast from the player, otherwise a Bundled
 * Spell's landing point. {@code landings} is null when every cast is native, otherwise it is shared by every landing
 * of one throw. {@code damageMultiplier} is an outside damage factor, such as the Shotgun split factor of a bundle.
 */
public record CastContext(@Nullable CastOrigin origin, @Nullable Landings landings, float damageMultiplier) {
    public static final CastContext DIRECT = new CastContext(null, null, 1.0F);

    public CastContext withOrigin(CastOrigin newOrigin) {
        return new CastContext(newOrigin, this.landings, this.damageMultiplier);
    }

    public Claim claim(AugmentAbility ability) {
        return this.landings == null ? Claim.NATIVE : this.landings.claim(ability);
    }

    public void record(AugmentAbility ability, boolean went) {
        if (this.landings != null) {
            this.landings.record(ability, went);
        }
    }

    public enum Claim {
        NATIVE,
        EXTRA,
        SKIP
    }

    /**
     * Tracks one throw's landings. The first landing of each ability is cast natively (mana and cooldown); later
     * landings are extra casts only if that native cast went through. If it did not (cooldown, no mana, blocked),
     * every later landing of that ability is skipped, so a throw never gets around a cooldown. Server thread only.
     */
    public static final class Landings {
        private final Map<AugmentAbility, Boolean> nativeWent = new HashMap<>();

        public static Landings firstNative() {
            return new Landings();
        }

        Claim claim(AugmentAbility ability) {
            Boolean went = this.nativeWent.get(ability);
            if (went == null) {
                this.nativeWent.put(ability, Boolean.FALSE);
                return Claim.NATIVE;
            }
            return went ? Claim.EXTRA : Claim.SKIP;
        }

        void record(AugmentAbility ability, boolean went) {
            this.nativeWent.put(ability, went);
        }
    }
}
