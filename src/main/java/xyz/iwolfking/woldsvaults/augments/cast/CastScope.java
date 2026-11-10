package xyz.iwolfking.woldsvaults.augments.cast;

import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.spellcraft.bar.EffectiveBar;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * What is in effect while the spell caster runs one ability for one player: the step's glyph modifiers, the landing
 * point of a Bundled Spell (projectiles the cast spawns move there), the slot's billing, the ability spec being cast
 * (so its own hits can be told from procs) and the Shotgun volley member it is. VH computes area, mana,
 * cooldown and spawns projectiles synchronously inside the cast, so all of it only needs to apply for its duration.
 * Server thread only; nested casts restore the outer scope.
 */
public final class CastScope {
    private static Active current;

    private CastScope() {
    }

    /**
     * How mana and cooldown are charged inside a slot cast. The trigger step charges the whole slot (the mana of every
     * ability in the spell times the spell's mana factor, and the slot cooldown); every other step is free, since the
     * slot already paid. {@code suppressCastEvent} keeps a step from firing on-cast effects (Shotgun copies).
     */
    public record Billing(Mode mode, @Nullable String triggerSpec, float otherAbilitiesMana, float manaFactor,
                          @Nullable EffectiveBar.Slot slot, boolean suppressCastEvent) {
        public static Billing trigger(String triggerSpec, float otherAbilitiesMana, float manaFactor, EffectiveBar.Slot slot) {
            return new Billing(Mode.TRIGGER, triggerSpec, otherAbilitiesMana, manaFactor, slot, false);
        }

        public static Billing free(boolean suppressCastEvent) {
            return new Billing(Mode.FREE, null, 0.0F, 1.0F, null, suppressCastEvent);
        }
    }

    public enum Mode {
        TRIGGER,
        FREE
    }

    public static <T> T call(ServerPlayer player, CastModifiers modifiers, @Nullable CastOrigin origin, @Nullable Billing billing,
                             @Nullable String spec, @Nullable ShotgunHits.Shot shot, Supplier<T> action) {
        Active outer = current;
        current = new Active(player, modifiers, origin, billing, spec, shot);
        try {
            return action.get();
        } finally {
            current = outer;
        }
    }

    public static Optional<Active> current() {
        return Optional.ofNullable(current);
    }

    /** The billing of the scope open for this player, or null outside a slot cast. */
    @Nullable
    public static Billing billing(ServerPlayer player) {
        return current != null && current.player() == player ? current.billing() : null;
    }

    public record Active(ServerPlayer player, CastModifiers modifiers, @Nullable CastOrigin origin, @Nullable Billing billing,
                         @Nullable String spec, @Nullable ShotgunHits.Shot shot) {
    }
}
