package xyz.iwolfking.woldsvaults.spellcraft.haste;

import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import iskallia.vault.util.calc.CooldownHelper;
import net.minecraft.world.entity.LivingEntity;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.SpellHasteConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

import javax.annotation.Nullable;

/**
 * Spell haste. VH's cooldown reduction attribute, its Cufflinks percentile and the cooldown reduction player stat now
 * carry haste. A cooldown is divided by the haste factor: {@code 1 + haste} for positive haste, {@code 1 / (1 + |haste|)}
 * for negative haste, times any spell multipliers (Quicken, Discount), and never above {@code 1 + cap}. The cap is the
 * table's base cap plus the player's cooldown reduction cap attribute.
 */
public final class SpellHaste {
    private static boolean missingLogged;

    private SpellHaste() {
    }

    /** The spell haste table, or null (logged once) when it failed to install; VH's own cooldown reduction then applies. */
    @Nullable
    public static SpellHasteConfig config() {
        SpellHasteConfig config = ModConfigs.SPELL_HASTE;
        if (config == null && !missingLogged) {
            missingLogged = true;
            WoldsVaults.LOGGER.error("Spell haste table is not installed; cooldowns fall back to Vault Hunters' cooldown reduction");
        }
        return config;
    }

    /** Uncapped haste: gear, talents and every other source, Cufflinks, and the player stat event. */
    public static float raw(LivingEntity entity) {
        return CooldownHelper.getCooldownMultiplierUnlimited(entity);
    }

    /** The cap on positive haste: the table's base cap plus the cooldown reduction cap attribute, never below 0. */
    public static float cap(LivingEntity entity, SpellHasteConfig config) {
        float extra = AttributeSnapshotHelper.getInstance().getSnapshot(entity)
                .getAttributeValue(ModGearAttributes.COOLDOWN_REDUCTION_CAP, VaultGearAttributeTypeMerger.floatSum()).floatValue();
        return Math.max(0.0F, config.baseCap + extra);
    }

    /** The haste factor for raw haste, a spell multiplier and a cap. */
    public static float factor(float raw, float multiplier, float cap) {
        float base = raw >= 0.0F ? 1.0F + raw : 1.0F / (1.0F - raw);
        return Math.min(base * Math.max(0.01F, multiplier), 1.0F + cap);
    }

    /** The player's haste factor with a spell multiplier (1 for a plain spell), or null when the table is missing. */
    @Nullable
    public static Float factor(LivingEntity entity, float multiplier) {
        SpellHasteConfig config = config();
        if (config == null) {
            return null;
        }
        return factor(raw(entity), multiplier, cap(entity, config));
    }

    /** The share of a cooldown the factor removes, the form VH's cooldown code subtracts; negative for slowed casting. */
    public static float reduction(float factor) {
        return 1.0F - 1.0F / factor;
    }
}
