package xyz.iwolfking.woldsvaults.spellcraft.scope;

import xyz.iwolfking.woldsvaults.spellcraft.bar.EffectiveBar;

import javax.annotation.Nullable;

/**
 * How mana and cooldown are charged inside a slot cast: the trigger step charges the whole slot and every other step is
 * free. {@code suppressCastEvent} keeps a step from firing on-cast effects. {@code fullyCharged}: the slot was cast
 * with Charge held to full.
 */
public record CastBilling(@Nullable String triggerSpec, float otherAbilitiesMana, float manaFactor,
                          @Nullable EffectiveBar.Slot slot, boolean suppressCastEvent, boolean fullyCharged) {
    public static CastBilling trigger(String triggerSpec, float otherAbilitiesMana, float manaFactor, EffectiveBar.Slot slot,
                                      boolean fullyCharged) {
        return new CastBilling(triggerSpec, otherAbilitiesMana, manaFactor, slot, false, fullyCharged);
    }

    public static CastBilling free(boolean suppressCastEvent) {
        return new CastBilling(null, 0.0F, 1.0F, null, suppressCastEvent, false);
    }

    public boolean isTrigger() {
        return this.triggerSpec != null;
    }
}
