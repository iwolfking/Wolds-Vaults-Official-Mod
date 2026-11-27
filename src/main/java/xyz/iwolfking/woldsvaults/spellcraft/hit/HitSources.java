package xyz.iwolfking.woldsvaults.spellcraft.hit;

import iskallia.vault.entity.entity.EffectCloudEntity;
import iskallia.vault.entity.entity.EternalEntity;
import iskallia.vault.entity.entity.IceBoltEntity;
import iskallia.vault.event.ActiveFlags;
import net.minecraft.world.entity.Entity;
import xyz.iwolfking.woldsvaults.api.cast.CastCarrier;
import xyz.iwolfking.woldsvaults.events.WoldActiveFlags;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class HitSources {
    private static final String PLAYER_ATTACK = "player";
    private static final String EXPLOSION = "explosion.player";
    private static final String ARROW = "arrow";
    private static final String INDIRECT_MAGIC = "indirectMagic";
    private static final String MAGIC = "magic";
    private static final String MOB = "mob";
    private static final String THORNS = "thorns";
    private static final String LIFE_BOND = "woldsvaults.life_bond";

    private static final Map<String, List<Source>> TABLE = build();

    private HitSources() {
    }

    /** DIRECT: one target the ability or its projectile struck; AREA: every target in an area or chain; DOT: a tick. */
    public enum Kind {
        DIRECT,
        AREA,
        DOT
    }

    /**
     * One way an ability deals its own damage. {@code msgId} is the damage source id (null: any), {@code direct} the
     * class of the source's direct entity (null: the attacking player), {@code flags} the exact VH flag set, {@code
     * wvFlags} WV flags that must be set, {@code carrier} what carries it, {@code nested} whether VH deals it inside
     * another entity's hurt call.
     */
    public record Source(Kind kind, CastCarrier carrier, @Nullable String msgId, @Nullable Class<? extends Entity> direct,
                         Set<ActiveFlags> flags, Set<WoldActiveFlags> wvFlags, String via, boolean nested) {
    }

    /** The hit sources of a spec; empty for specs that deal no damage of their own, null for specs the table lacks. */
    @Nullable
    public static List<Source> sources(String spec) {
        return TABLE.get(spec);
    }

    static boolean has(String spec) {
        return TABLE.containsKey(spec);
    }

    private static Map<String, List<Source>> build() {
        Map<String, List<Source>> table = new LinkedHashMap<>();
        Set<ActiveFlags> apAoe = EnumSet.of(ActiveFlags.IS_AP_ATTACKING, ActiveFlags.IS_AOE_ATTACKING);
        Set<ActiveFlags> aoe = EnumSet.of(ActiveFlags.IS_AOE_ATTACKING);

        for (String javelin : List.of("Javelin_Base", "Javelin_Piercing", "Javelin_Scatter", "Javelin_Sight")) {
            put(table, javelin, direct(CastCarrier.PROJECTILE, PLAYER_ATTACK, null, EnumSet.of(ActiveFlags.IS_JAVELIN_ATTACKING), "VaultThrownJavelin hit"));
        }
        put(table, "Fireball_Base", area(CastCarrier.PROJECTILE, PLAYER_ATTACK, null, apAoe, "VaultFireball explosion"));
        put(table, "Fireball_Volley", area(CastCarrier.PROJECTILE, PLAYER_ATTACK, null, apAoe, "VaultFireball explosion and 25% contact hit"));
        put(table, "Fireball_Fireshot", direct(CastCarrier.PROJECTILE, PLAYER_ATTACK, null,
                EnumSet.of(ActiveFlags.IS_AP_ATTACKING, ActiveFlags.IS_FIRESHOT_ATTACKING), "VaultFireball contact hit"));
        put(table, "Fireball_Fireshot", new Source(Kind.DOT, CastCarrier.DOT, PLAYER_ATTACK, null, EnumSet.of(ActiveFlags.IS_AP_ATTACKING),
                EnumSet.of(WoldActiveFlags.IS_NO_KNOCKBACK_DAMAGE), "PercentBurnEffect tick", false));
        for (String bolt : List.of("Ice_Bolt_Base", "Ice_Bolt_Blast")) {
            put(table, bolt, direct(CastCarrier.PROJECTILE, ARROW, IceBoltEntity.class, aoe, "IceBoltEntity hit and splinters"));
            put(table, bolt, direct(CastCarrier.PROJECTILE, ARROW, IceBoltEntity.class, EnumSet.noneOf(ActiveFlags.class), "IceBoltEntity hit with the lucky-hit etching"));
        }
        put(table, "Storm_Arrow_Base", direct(CastCarrier.ENTITY, PLAYER_ATTACK, null,
                EnumSet.of(ActiveFlags.IS_AP_ATTACKING, ActiveFlags.IS_SMITE_ATTACKING), "VaultStormEntity bolt"));
        put(table, "Shard_Blizzard", direct(CastCarrier.ENTITY, PLAYER_ATTACK, null, aoe, "VaultBlizzardShard destroy"));
        put(table, "Storm_Arrow_Blizzard", new Source(Kind.DOT, CastCarrier.ENTITY, PLAYER_ATTACK, null, EnumSet.of(ActiveFlags.IS_EFFECT_ATTACKING),
                EnumSet.noneOf(WoldActiveFlags.class), "hypothermia tick from its shards", false));
        put(table, "Shard_Blizzard", new Source(Kind.DOT, CastCarrier.ENTITY, PLAYER_ATTACK, null, EnumSet.of(ActiveFlags.IS_EFFECT_ATTACKING),
                EnumSet.noneOf(WoldActiveFlags.class), "hypothermia tick from its shards", false));
        put(table, "Nova_Base", area(CastCarrier.CAST, PLAYER_ATTACK, null, apAoe, "NovaAbility"));
        put(table, "Nova_Slow", new Source(Kind.DOT, CastCarrier.ENTITY, PLAYER_ATTACK, null, EnumSet.of(ActiveFlags.IS_EFFECT_ATTACKING),
                EnumSet.noneOf(WoldActiveFlags.class), "hypothermia tick on the target's update", false));
        for (String dot : List.of("Nova_Dot", "Toxic_Grenade")) {
            put(table, dot, new Source(Kind.DOT, CastCarrier.DOT, PLAYER_ATTACK, null,
                    EnumSet.of(ActiveFlags.IS_DOT_ATTACKING, ActiveFlags.IS_POISON_NOVA_ATTACKING), EnumSet.noneOf(WoldActiveFlags.class),
                    "DamageOverTimeHelper tick", false));
        }
        for (String grenade : List.of("Grenade_Base", "Grenade_Sticky")) {
            put(table, grenade, area(CastCarrier.PROJECTILE, EXPLOSION, null, apAoe, "VaultGrenade explosion"));
        }
        put(table, "Smite_Base", direct(CastCarrier.TOGGLE, PLAYER_ATTACK, null,
                EnumSet.of(ActiveFlags.IS_AP_ATTACKING, ActiveFlags.IS_SMITE_BASE_ATTACKING), "smite bolt"));
        for (String smite : List.of("Smite_Archon", "Smite_Thunderstorm")) {
            put(table, smite, direct(CastCarrier.TOGGLE, PLAYER_ATTACK, null,
                    EnumSet.of(ActiveFlags.IS_AP_ATTACKING, ActiveFlags.IS_SMITE_ATTACKING), "smite bolt"));
        }
        put(table, "Smite_Blast_Wave", area(CastCarrier.TOGGLE, PLAYER_ATTACK, null,
                EnumSet.of(ActiveFlags.IS_AP_ATTACKING, ActiveFlags.IS_AOE_ATTACKING, ActiveFlags.IS_SMITE_ATTACKING), "blast wave"));
        for (String quake : List.of("Earthquake_Base", "Earthquake_Singularity", "Earthquake_Tremor")) {
            put(table, quake, area(CastCarrier.CAST, PLAYER_ATTACK, null, apAoe, "first shock"));
            put(table, quake, area(CastCarrier.SCHEDULED, PLAYER_ATTACK, null, apAoe, "later shocks"));
        }
        put(table, "Earthquake_Landmine", area(CastCarrier.SCHEDULED, PLAYER_ATTACK, null, apAoe, "LandmineBlockEntity trigger"));
        put(table, "Chain_Lightning_Base", direct(CastCarrier.PROJECTILE, PLAYER_ATTACK, null, apAoe, "ChainLightningProjectile hit and chain"));
        put(table, "Chain_Lightning_Charged_Bolts", direct(CastCarrier.CAST, PLAYER_ATTACK, null, apAoe, "spell bolt path hit (VH's own path is scheduled)"));
        put(table, "Chain_Lightning_Orbs", area(CastCarrier.PROJECTILE, PLAYER_ATTACK, null,
                EnumSet.of(ActiveFlags.IS_AP_ATTACKING, ActiveFlags.IS_LIGHTNING_ORB_ATTACKING, ActiveFlags.IS_AOE_ATTACKING), "LightningOrbEntity tick"));
        put(table, "Arcane_Rail", direct(CastCarrier.CAST, PLAYER_ATTACK, null,
                EnumSet.of(ActiveFlags.IS_AP_ATTACKING, ActiveFlags.IS_ARCANE_RAIL_ATTACKING), "rail"));
        put(table, "Arcane_Prism", area(CastCarrier.SCHEDULED, PLAYER_ATTACK, null,
                EnumSet.of(ActiveFlags.IS_AP_ATTACKING, ActiveFlags.IS_EFFECT_ATTACKING, ActiveFlags.IS_AOE_ATTACKING), "prism pulses"));
        put(table, "Arcane_Base", direct(CastCarrier.TOGGLE, PLAYER_ATTACK, null,
                EnumSet.of(ActiveFlags.IS_AP_ATTACKING, ActiveFlags.IS_EFFECT_ATTACKING), "held beam tick"));
        Set<ActiveFlags> minion = EnumSet.of(ActiveFlags.IS_AP_ATTACKING, ActiveFlags.IS_NECROMANCY_MINION_ATTACKING);
        put(table, "Necromancy_Base", direct(CastCarrier.MINION, PLAYER_ATTACK, null, minion, "minion melee and splash"));
        put(table, "Necromancy_Golem", direct(CastCarrier.MINION, PLAYER_ATTACK, null, minion, "minion melee and splash"));
        put(table, "Necromancy_Archer", new Source(Kind.DIRECT, CastCarrier.MINION, PLAYER_ATTACK, null, minion, EnumSet.noneOf(WoldActiveFlags.class),
                "minion arrow, dealt inside the cancelled arrow hurt", true));
        for (String bash : List.of("Shield_Bash", "Shield_Bash_Earthshatter", "Shield_Bash_Battering_Ram")) {
            put(table, bash, area(CastCarrier.CAST, PLAYER_ATTACK, null, aoe, "ShieldBashAbility cone"));
        }
        put(table, "Mana_Shield_Retribution", area(CastCarrier.EVENT, PLAYER_ATTACK, null, aoe, "retribution when the charge timer ends"));
        put(table, "Mana_Shield_Implode", area(CastCarrier.CAST, PLAYER_ATTACK, null, aoe, "ImplodeAbility"));
        put(table, "Implode_Life_Tap", area(CastCarrier.CAST, PLAYER_ATTACK, null, aoe, "LifeTapAbility"));
        put(table, "Mana_Barrier", area(CastCarrier.CAST, PLAYER_ATTACK, null, apAoe, "ability-power etching burst"));
        put(table, "Stonefall_Snow", area(CastCarrier.EVENT, PLAYER_ATTACK, null, apAoe, "landing hit on the player's fall event"));
        put(table, "Taunt_Charm", new Source(Kind.DIRECT, CastCarrier.MINION, PLAYER_ATTACK, null, EnumSet.of(ActiveFlags.IS_CHARMED_ATTACKING),
                EnumSet.noneOf(WoldActiveFlags.class), "charmed mob hit, dealt inside the cancelled mob hurt", true));
        put(table, "Totem_Mob_Damage", area(CastCarrier.BLOCK_ENTITY, PLAYER_ATTACK, null,
                EnumSet.of(ActiveFlags.IS_AP_ATTACKING, ActiveFlags.IS_TOTEM_ATTACKING), "TotemMobDamageTileEntity tick"));
        put(table, "Totem_Mob_Damage", area(CastCarrier.BLOCK_ENTITY, PLAYER_ATTACK, null,
                EnumSet.of(ActiveFlags.IS_TOTEM_ATTACKING), "TotemMobDamageTileEntity tick with the attack-damage etching"));
        put(table, "Dash_Damage", area(CastCarrier.EVENT, PLAYER_ATTACK, null, aoe, "skill tick after the dash"));
        put(table, "Rampage_Chain", new Source(Kind.AREA, CastCarrier.EVENT, null, null, EnumSet.of(ActiveFlags.IS_CHAINING_ATTACKING),
                EnumSet.noneOf(WoldActiveFlags.class), "chain inside the melee hurt", true));
        put(table, "Shell_Porcupine", new Source(Kind.DIRECT, CastCarrier.EVENT, THORNS, null, EnumSet.of(ActiveFlags.IS_THORNS_REFLECTING),
                EnumSet.noneOf(WoldActiveFlags.class), "thorns inside the attacker's hit on the player", true));
        put(table, "Shell_Quill", new Source(Kind.AREA, CastCarrier.EVENT, THORNS, null,
                EnumSet.of(ActiveFlags.IS_AOE_ATTACKING, ActiveFlags.IS_THORNS_REFLECTING), EnumSet.noneOf(WoldActiveFlags.class),
                "quills inside the attacker's hit on the player", true));
        for (String fangs : List.of("Fangs_Base", "Fangs_Maw")) {
            put(table, fangs, new Source(Kind.DIRECT, CastCarrier.ENTITY, PLAYER_ATTACK, null, EnumSet.noneOf(ActiveFlags.class),
                    EnumSet.of(WoldActiveFlags.IS_FANG_ATTACKING), "CustomFangEntity tick", false));
        }
        put(table, "Expunge_Base", area(CastCarrier.ENTITY, INDIRECT_MAGIC, EffectCloudEntity.class, aoe, "EffectCloudEntity instant damage"));
        put(table, "Expunge_Base", new Source(Kind.DOT, CastCarrier.DOT, PLAYER_ATTACK, null,
                EnumSet.of(ActiveFlags.IS_AP_ATTACKING, ActiveFlags.IS_EFFECT_ATTACKING), EnumSet.noneOf(WoldActiveFlags.class),
                "poison from its clouds", false));
        put(table, "Concentrate_Base", new Source(Kind.DIRECT, CastCarrier.CAST, MAGIC, null, EnumSet.noneOf(ActiveFlags.class),
                EnumSet.noneOf(WoldActiveFlags.class), "drain etching (no attacker)", false));
        put(table, "UltimateShield_Base", new Source(Kind.AREA, CastCarrier.EVENT, PLAYER_ATTACK, null, aoe, EnumSet.noneOf(WoldActiveFlags.class),
                "Imploding Barrier, inside the player's damage event", true));
        put(table, "Cope_De_Grace", area(CastCarrier.EVENT, PLAYER_ATTACK, null, aoe, "server-tick dash sweep"));
        put(table, "Life_Block_Base", new Source(Kind.DIRECT, CastCarrier.EVENT, LIFE_BOND, null, EnumSet.of(ActiveFlags.IS_THORNS_REFLECTING),
                EnumSet.noneOf(WoldActiveFlags.class), "life bond, inside the attacker's hit on the player", true));
        put(table, "Summon_Eternal_Base", new Source(Kind.DIRECT, CastCarrier.MINION, MOB, EternalEntity.class, EnumSet.noneOf(ActiveFlags.class),
                EnumSet.noneOf(WoldActiveFlags.class), "eternal melee (attacker is the eternal)", false));

        for (String none : List.of("Execute_Base", "Battle_Cry_Base", "Battle_Cry_Spectral_Strike", "Battle_Cry_Lucky_Strike",
                "Rampage_Base", "Rampage_Bloodlust", "Rampage_Berserker", "Rampage_Instinct", "Rampage_Leech",
                "Empower_Base", "Empower_Ice_Armor", "Empower_Slowness_Aura", "Heal_Base", "Heal_Group", "Heal_Cleanse",
                "Colossus_Base", "Sneaky_Getaway", "Ghost_Walk_Base", "Ghost_Walk_Spirit", "Mega_Jump_Base", "Mega_Jump_Break_Up",
                "Mega_Jump_Break_Down", "Hunter_Base", "Taunt_Base", "Taunt_Repel", "Taunt_Decoy", "Stonefall_Base", "Stonefall_Cold",
                "Totem_Base", "Totem_Player_Damage", "Totem_Mana_Regen", "Mana_Shield_Base", "Shell_Base", "Farmer_Base",
                "Farmer_Melon", "Farmer_Cactus", "Farmer_Animal", "Vein_Miner_Base", "Vein_Miner_Fortune", "Vein_Miner_Durability",
                "Vein_Miner_Void", "Vein_Miner_Chain", "Levitate", "Dash_Base", "Dash_Warp", "Stirrings_Of_Power",
                "Stirrings_Of_Power_Dormant", "Savior", "Eyes_Of_God", "Bullet_Time", "Mana_Block")) {
            table.putIfAbsent(none, new ArrayList<>());
        }
        Map<String, List<Source>> frozen = new LinkedHashMap<>();
        table.forEach((spec, list) -> frozen.put(spec, List.copyOf(list)));
        return Collections.unmodifiableMap(frozen);
    }

    private static void put(Map<String, List<Source>> table, String spec, Source source) {
        table.computeIfAbsent(spec, key -> new ArrayList<>()).add(source);
    }

    private static Source direct(CastCarrier carrier, String msgId, @Nullable Class<? extends Entity> direct, Set<ActiveFlags> flags, String via) {
        return new Source(Kind.DIRECT, carrier, msgId, direct, flags, EnumSet.noneOf(WoldActiveFlags.class), via, false);
    }

    private static Source area(CastCarrier carrier, String msgId, @Nullable Class<? extends Entity> direct, Set<ActiveFlags> flags, String via) {
        return new Source(Kind.AREA, carrier, msgId, direct, flags, EnumSet.noneOf(WoldActiveFlags.class), via, false);
    }
}
