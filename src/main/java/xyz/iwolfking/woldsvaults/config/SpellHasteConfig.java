package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;

import javax.annotation.Nullable;

/**
 * Spell haste, generated as {@code vault_configs/spell_haste/spell_haste.json}. VH's cooldown reduction attribute and
 * player stat now carry haste: a cooldown is divided by {@code 1 + haste} (negative haste multiplies it by
 * {@code 1 + |haste|}), and positive haste is capped at {@code baseCap} plus the player's cooldown reduction cap
 * attribute. Cooldown skip chance is capped at {@code skipChanceCap}.
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
