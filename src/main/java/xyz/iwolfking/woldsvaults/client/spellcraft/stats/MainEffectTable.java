package xyz.iwolfking.woldsvaults.client.spellcraft.stats;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class MainEffectTable {
    private MainEffectTable() {
    }

    enum Factor {
        NONE,
        INTENSITY,
        DURATION,
        UPSIDE,
        DRAIN
    }

    enum Format {
        PERCENT,
        NUMBER,
        INTEGER,
        BLOCKS,
        TICKS,
        PER_SECOND
    }

    record Main(String label, String key, Format format, Factor factor) {
    }

    /**
     * {@code percentDuration} marks a spec whose duration Vault Hunters binds as a percent label, such as Mana Shield.
     */
    record Effects(List<Main> mains, Factor hit, Factor duration, boolean percentDuration) {
    }

    /** Per spec, the Vault Hunters labels its buff glyphs reach and the factor each takes. */
    static Map<String, Effects> effects() {
        Map<String, Effects> table = new HashMap<>();
        Main speed = new Main("speed", "buff_speed", Format.PERCENT, Factor.INTENSITY);
        Main rampage = new Main("damage", "damage_bonus", Format.PERCENT, Factor.INTENSITY);
        Main heal = new Main("heal", "heal", Format.NUMBER, Factor.INTENSITY);
        Main hitCost = new Main("additionalManaPerHit", "mana_per_hit", Format.NUMBER, Factor.DRAIN);
        Main boltCost = new Main("additionalManaPerBolt", "mana_per_bolt", Format.NUMBER, Factor.DRAIN);
        Main thorns = new Main("additional_thorns_damage", "thorns", Format.PERCENT, Factor.INTENSITY);
        Main slowness = new Main("slowness", "slowness", Format.INTEGER, Factor.INTENSITY);
        Main reduction = new Main("damageReduction", "damage_reduction", Format.PERCENT, Factor.NONE);
        table.put("Empower_Base", effects(Factor.NONE, Factor.NONE, speed));
        table.put("Empower_Ice_Armor", effects(Factor.NONE, Factor.INTENSITY, new Main("chilled", "chilled", Format.INTEGER, Factor.INTENSITY), hitCost));
        table.put("Empower_Slowness_Aura", effects(Factor.NONE, Factor.NONE, slowness));
        table.put("Rampage_Base", effects(Factor.NONE, Factor.NONE, rampage));
        table.put("Rampage_Bloodlust", effects(Factor.NONE, Factor.UPSIDE, rampage,
                new Main("manaRefund", "mana_refund", Format.NUMBER, Factor.INTENSITY), new Main("speed", "stack_speed", Format.PERCENT, Factor.INTENSITY)));
        table.put("Rampage_Berserker", effects(Factor.NONE, Factor.NONE, rampage, new Main("manaRamp", "mana_ramp", Format.PER_SECOND, Factor.DRAIN)));
        table.put("Rampage_Instinct", effects(Factor.NONE, Factor.NONE, rampage,
                new Main("luckyHitChance", "lucky_hit", Format.PERCENT, Factor.INTENSITY)));
        table.put("Rampage_Leech", effects(Factor.NONE, Factor.NONE, new Main("leech", "leech", Format.PERCENT, Factor.INTENSITY)));
        table.put("Rampage_Chain", effects(Factor.NONE, Factor.NONE, new Main("chains", "chains", Format.INTEGER, Factor.INTENSITY)));
        table.put("Heal_Base", effects(Factor.NONE, Factor.NONE, heal));
        table.put("Heal_Group", effects(Factor.NONE, Factor.NONE, heal));
        table.put("Totem_Base", effects(Factor.NONE, Factor.DURATION, new Main("heal", "heal_per_second", Format.NUMBER, Factor.INTENSITY)));
        table.put("Totem_Mana_Regen", effects(Factor.NONE, Factor.DURATION, new Main("manaRegen", "mana_regen", Format.PERCENT, Factor.INTENSITY)));
        table.put("Totem_Player_Damage", effects(Factor.NONE, Factor.DURATION, new Main("damage", "damage_bonus", Format.PERCENT, Factor.INTENSITY)));
        table.put("Totem_Mob_Damage", effects(Factor.NONE, Factor.DURATION));
        table.put("Mana_Shield_Base", new Effects(List.of(new Main("barrierPoints", "barrierPoints", Format.INTEGER, Factor.INTENSITY)),
                Factor.NONE, Factor.DURATION, true));
        table.put("Mana_Barrier", effects(Factor.NONE, Factor.DURATION));
        table.put("Stonefall_Base", effects(Factor.NONE, Factor.DURATION, reduction, new Main("knockback", "knockback", Format.NUMBER, Factor.INTENSITY)));
        table.put("Stonefall_Snow", effects(Factor.NONE, Factor.DURATION,
                new Main("damageReduction", "damage_reduction", Format.PERCENT, Factor.INTENSITY)));
        table.put("Stonefall_Cold", effects(Factor.NONE, Factor.DURATION, reduction, new Main("freezeDuration", "freeze", Format.TICKS, Factor.INTENSITY)));
        table.put("Taunt_Base", effects(Factor.NONE, Factor.DURATION, new Main("vulnerable", "vulnerable", Format.INTEGER, Factor.INTENSITY)));
        table.put("Taunt_Repel", effects(Factor.NONE, Factor.DURATION, new Main("distance", "distance", Format.BLOCKS, Factor.INTENSITY)));
        table.put("Taunt_Charm", effects(Factor.NONE, Factor.DURATION, new Main("damage", "charm_damage", Format.PERCENT, Factor.INTENSITY)));
        table.put("Taunt_Decoy", effects(Factor.NONE, Factor.DURATION));
        table.put("Battle_Cry_Base", effects(Factor.NONE, Factor.DURATION,
                new Main("attackDamagePerStack", "attack_per_stack", Format.PERCENT, Factor.INTENSITY)));
        table.put("Battle_Cry_Spectral_Strike", effects(Factor.NONE, Factor.DURATION,
                new Main("abilityPowerPerStack", "power_per_stack", Format.PERCENT, Factor.INTENSITY)));
        table.put("Battle_Cry_Lucky_Strike", effects(Factor.NONE, Factor.DURATION,
                new Main("luckyHitPerStack", "lucky_per_stack", Format.PERCENT, Factor.INTENSITY)));
        table.put("Shell_Base", effects(Factor.NONE, Factor.NONE, new Main("stunChance", "stun_chance", Format.PERCENT, Factor.INTENSITY),
                new Main("stunDuration", "stun_duration", Format.TICKS, Factor.INTENSITY), hitCost));
        table.put("Shell_Porcupine", effects(Factor.NONE, Factor.NONE, thorns, hitCost));
        table.put("Shell_Quill", effects(Factor.NONE, Factor.NONE, thorns, hitCost));
        table.put("Smite_Base", effects(Factor.INTENSITY, Factor.NONE, boltCost));
        table.put("Smite_Archon", effects(Factor.INTENSITY, Factor.NONE, boltCost));
        table.put("Smite_Thunderstorm", effects(Factor.INTENSITY, Factor.NONE, boltCost));
        table.put("Smite_Blast_Wave", effects(Factor.INTENSITY, Factor.NONE));
        table.put("Hunter_Base", effects(Factor.NONE, Factor.DURATION));
        table.put("Ghost_Walk_Base", effects(Factor.NONE, Factor.DURATION));
        table.put("Ghost_Walk_Spirit", effects(Factor.NONE, Factor.DURATION));
        table.put("Summon_Eternal_Base", effects(Factor.NONE, Factor.DURATION));
        table.put("Nova_Slow", effects(Factor.NONE, Factor.DURATION, slowness));
        table.put("Colossus_Base", effects(Factor.NONE, Factor.DURATION,
                new Main("additionalResistance", "resistance", Format.PERCENT, Factor.INTENSITY), new Main("size", "size", Format.PERCENT, Factor.INTENSITY)));
        table.put("Sneaky_Getaway", effects(Factor.NONE, Factor.DURATION, speed));
        table.put("Concentrate_Base", effects(Factor.NONE, Factor.DURATION, new Main("baseAmplitude", "amplitude", Format.INTEGER, Factor.INTENSITY)));
        return Map.copyOf(table);
    }

    private static Effects effects(Factor hit, Factor duration, Main... mains) {
        return new Effects(List.of(mains), hit, duration, false);
    }
}
