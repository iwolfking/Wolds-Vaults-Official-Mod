package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;

import javax.annotation.Nullable;

/**
 * Spell haste, generated as {@code vault_configs/spell_haste/spell_haste.json}. {@code baseCap} caps positive haste
 * before the player's cooldown reduction cap attribute is added, in haste (1.0 is +100%). {@code skipChanceCap} caps
 * the summed cooldown skip chance, from 0 to 1.
 */
public class SpellHasteConfig extends Config implements WoldsTable {
    @Expose public float baseCap;
    @Expose public float skipChanceCap;

    @Override
    public String getName() {
        return "spell_haste";
    }

    @Override
    protected void reset() {
    }

    @Nullable
    @Override
    public String validate() {
        if (this.baseCap < 0.0F) {
            return "the base haste cap is negative";
        }
        if (this.skipChanceCap < 0.0F || this.skipChanceCap > 1.0F) {
            return "the skip chance cap is outside 0-1";
        }
        return null;
    }
}
