package xyz.iwolfking.woldsvaults.gods.combat;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.time.TickClock;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Vault clock rate: how many game ticks one vault second lasts.
 *
 * <p>The base mod has no such concept — {@code TickTimer.tickTime()} decrements the displayed
 * time by exactly one every server tick, unconditionally. This primitive introduces the missing
 * rate: a vault second is {@value #BASE_TICKS_PER_VAULT_SECOND} game ticks by default, and named
 * factors multiply that length. A factor above 1 lengthens the vault second, so the clock runs
 * slower and the run lasts longer in real time; a factor below 1 speeds the clock up.
 *
 * <p>Applied by an accumulator on the clock itself, so fractional rates never round away: the
 * displayed time only advances once enough game ticks have accumulated. Pausing is handled for
 * free, because the base mod skips the tick entirely while the clock carries {@code PAUSED}, and
 * counting-up vaults ({@code TickStopwatch}) are covered too, because the gate sits on the shared
 * call site rather than on either subclass.
 *
 * <p>State is per vault and in memory only; it is not written to the vault snapshot, so factors
 * must be re-applied if a vault is reloaded from disk.
 */
public final class VaultClockRate {
    public static final int BASE_TICKS_PER_VAULT_SECOND = 20;
    private static final float MIN_FACTOR = 0.01F;
    private static final int MAX_TICKS_PER_PASS = 100;

    private VaultClockRate() {
    }

    /**
     * Sets a named rate factor on a vault's clock. Factors multiply with each other; the product
     * scales the length of a vault second.
     */
    public static boolean setRateFactor(Vault vault, ResourceLocation key, float factor) {
        State state = stateOf(vault);
        if (state == null) {
            return false;
        }
        if (factor < MIN_FACTOR) {
            WoldsVaults.LOGGER.error("Clock rate factor {} for key {} is below the {} floor; clamping.", factor, key, MIN_FACTOR);
            factor = MIN_FACTOR;
        }
        state.factors.put(key, factor);
        return true;
    }

    public static boolean removeRateFactor(Vault vault, ResourceLocation key) {
        State state = stateOf(vault);
        return state != null && state.factors.remove(key) != null;
    }

    public static void clear(Vault vault) {
        State state = stateOf(vault);
        if (state != null) {
            state.factors.clear();
            state.accumulator = 0.0F;
        }
    }

    /** The current length of a vault second, in game ticks. */
    public static float getRate(Vault vault) {
        return BASE_TICKS_PER_VAULT_SECOND * getRateFactor(vault);
    }

    /** The multiplicative product of every rate factor on this vault, 1.0 when unmodified. */
    public static float getRateFactor(Vault vault) {
        State state = stateOf(vault);
        return state == null ? 1.0F : state.product();
    }

    public static Map<ResourceLocation, Float> view(Vault vault) {
        State state = stateOf(vault);
        return state == null ? Collections.emptyMap() : Collections.unmodifiableMap(state.factors);
    }

    private static State stateOf(Vault vault) {
        TickClock clock = vault == null ? null : vault.get(Vault.CLOCK);
        if (clock == null) {
            return null;
        }
        return ((VaultClockRateHolder) clock).woldsvaults$getClockRate();
    }

    /** Per-clock rate factors plus the fractional accumulator that applies them. */
    public static final class State {
        public final Map<ResourceLocation, Float> factors = new LinkedHashMap<>();
        public float accumulator;

        public float product() {
            float product = 1.0F;
            for (float factor : this.factors.values()) {
                product *= factor;
            }
            return Math.max(product, MIN_FACTOR);
        }

        /**
         * Advances the accumulator by one game tick and returns how many clock ticks that
         * releases. Zero means this game tick is swallowed by a slowed clock.
         */
        public int consume() {
            this.accumulator += 1.0F / this.product();
            int ticks = 0;
            while (this.accumulator >= 1.0F && ticks < MAX_TICKS_PER_PASS) {
                this.accumulator -= 1.0F;
                ticks++;
            }
            if (this.accumulator >= 1.0F) {
                WoldsVaults.LOGGER.error("Vault clock rate released more than {} ticks in one pass; discarding the remainder.",
                        MAX_TICKS_PER_PASS);
                this.accumulator = 0.0F;
            }
            return ticks;
        }
    }
}
