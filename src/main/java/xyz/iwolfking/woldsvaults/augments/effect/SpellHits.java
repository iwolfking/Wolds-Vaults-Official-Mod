package xyz.iwolfking.woldsvaults.augments.effect;

import iskallia.vault.entity.entity.EffectCloudEntity;
import iskallia.vault.entity.entity.EternalEntity;
import iskallia.vault.entity.entity.IceBoltEntity;
import iskallia.vault.event.ActiveFlags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.events.WoldActiveFlags;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Which hurt events are an ability's own hits, so damage glyphs multiply those and nothing else (no on-hit procs, no
 * lucky-hit extras, no etching side hits). Each ability spec lists its hit sources as VH deals them in 3.21.6: the
 * damage source id, the direct entity, the exact set of VH {@link ActiveFlags} set around the hurt call, the WV flags
 * it needs, what carries it (the cast itself, a projectile's tick, or something deferred) and whether VH deals it from
 * inside another hurt call. A hit matches when all of that agrees; a nested hurt matches only a source marked nested.
 */
public final class SpellHits {
    private static final String PLAYER_ATTACK = "player";
    private static final String EXPLOSION = "explosion.player";
    private static final String ARROW = "arrow";
    private static final String INDIRECT_MAGIC = "indirectMagic";
    private static final String MAGIC = "magic";
    private static final String MOB = "mob";
    private static final String THORNS = "thorns";
    private static final String LIFE_BOND = "woldsvaults.life_bond";

    private static final Set<ActiveFlags> LEAKY_FLAGS = EnumSet.of(ActiveFlags.IS_THORNS_REFLECTING);
    private static final Set<WoldActiveFlags> PROC_WV_FLAGS = EnumSet.of(WoldActiveFlags.IS_REAVING_ATTACKING,
            WoldActiveFlags.IS_ECHOING_ATTACKING, WoldActiveFlags.IS_PROC_FANG_ATTACKING, WoldActiveFlags.IS_DERIVED_ATTACKING,
            WoldActiveFlags.IS_LIGHTNING_FIEND_ATTACKING, WoldActiveFlags.IS_SCEPTER_CAST_ATTACKING, WoldActiveFlags.IS_TRUE_DAMAGE);

    private static final Map<String, List<Source>> TABLE = build();
    private static final Set<String> UNKNOWN_WARNED = ConcurrentHashMap.newKeySet();
    private static final Set<String> SKIP_NOTED = ConcurrentHashMap.newKeySet();
    private static final ThreadLocal<int[]> HURT_DEPTH = ThreadLocal.withInitial(() -> new int[1]);

    private SpellHits() {
    }

    /**
     * What delivers a hit relative to the cast. CAST and PROJECTILE are reached by the cast scope and projectile tags;
     * the others by the cast's deferred glyphs, each only through its own carrier (a scheduled task reaches SCHEDULED
     * sources, an entity's tick ENTITY and MINION sources, a block entity's tick BLOCK_ENTITY sources, a damage-over-time
     * tick DOT sources, an ability's own later window EVENT sources, a minion's hit MINION sources).
     */
    public enum Carrier {
        CAST,
        PROJECTILE,
        ENTITY,
        BLOCK_ENTITY,
        SCHEDULED,
        DOT,
        MINION,
        TOGGLE,
        EVENT;

        /** Whether the hit happens after the cast's own call and outside a tagged projectile's tick. */
        public boolean deferred() {
            return this != CAST && this != PROJECTILE;
        }

        /** The hit sources a deferred carrier delivers, or null for the cast itself (any source). */
        @Nullable
        public Set<Carrier> reaches() {
            return switch (this) {
                case CAST, PROJECTILE -> null;
                case ENTITY -> EnumSet.of(ENTITY, MINION);
                default -> EnumSet.of(this);
            };
        }
    }

    /** DIRECT: one target the ability or its projectile struck; AREA: every target in an area or chain; DOT: a tick. */
    public enum Kind {
        DIRECT,
        AREA,
        DOT
    }

    /**
     * One way an ability deals its own damage. {@code msgId} is the damage source id (null: any), {@code direct} the
     * class of the source's direct entity (null: the attacking player), {@code flags} the exact VH flag set,
     * {@code wvFlags} WV flags that must be set, {@code via} what carries it, {@code nested} whether VH deals it inside
     * another entity's hurt call.
     */
    public record Source(Kind kind, Carrier carrier, @Nullable String msgId, @Nullable Class<? extends Entity> direct,
                         Set<ActiveFlags> flags, Set<WoldActiveFlags> wvFlags, String via, boolean nested) {
    }

    /** The hit sources of a spec; empty for specs that deal no damage of their own, null for specs the table lacks. */
    @Nullable
    public static List<Source> sources(String spec) {
        return TABLE.get(spec);
    }

    public static Map<String, List<Source>> table() {
        return TABLE;
    }

    /**
     * Whether this hurt is one of {@code spec}'s own hits. A null spec (a Bundled Spell orb's step) has none. A spec
     * the table lacks logs an error once and gets no glyph damage. The first hit of each other kind seen inside a
     * spec's cast is logged once, so a missing source shows up in the log.
     */
    public static boolean isOwnHit(@Nullable String spec, DamageSource source) {
        if (matchesOwnHit(spec, source)) {
            return true;
        }
        if (spec != null && TABLE.containsKey(spec)) {
            String key = spec + "|" + signature(source);
            if (SKIP_NOTED.add(key)) {
                WoldsVaults.LOGGER.info("Spell hits: a {} hit inside {}'s cast is not one of its own hits; damage glyphs skip it.", signature(source), spec);
            }
        }
        return false;
    }

    /**
     * Whether this hurt, dealt later through a deferred carrier, is one of {@code spec}'s own hits: like {@link #isOwnHit}
     * but only sources {@code carriers} delivers count. Procs that share an own hit's signature but run through another
     * carrier (Chain Lightning's delayed strike is scheduled, its own hit is a projectile's) stay out.
     */
    public static boolean isOwnDeferredHit(@Nullable String spec, DamageSource source, Set<Carrier> carriers) {
        return matchesOwnHit(spec, source, carriers);
    }

    /** Whether {@code spec} has any hit source one of {@code carriers} delivers (so area reads there are its own). */
    public static boolean hasSourceVia(@Nullable String spec, Set<Carrier> carriers) {
        List<Source> sources = spec == null ? null : TABLE.get(spec);
        if (sources == null) {
            return false;
        }
        for (Source candidate : sources) {
            if (carriers.contains(candidate.carrier())) {
                return true;
            }
        }
        return false;
    }

    /** {@link #isOwnHit} without the log of skipped hits. */
    public static boolean matchesOwnHit(@Nullable String spec, DamageSource source) {
        return matchesOwnHit(spec, source, null);
    }

    private static boolean matchesOwnHit(@Nullable String spec, DamageSource source, @Nullable Set<Carrier> carriers) {
        if (spec == null) {
            return false;
        }
        List<Source> sources = TABLE.get(spec);
        if (sources == null) {
            if (UNKNOWN_WARNED.add(spec)) {
                WoldsVaults.LOGGER.error("Spell hits: ability {} has no hit-source entry; damage glyphs skip its hits until it is added to SpellHits.", spec);
            }
            return false;
        }
        for (Source candidate : sources) {
            if ((carriers == null || carriers.contains(candidate.carrier())) && matches(candidate, source)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matches(Source candidate, DamageSource source) {
        if (!candidate.nested() && HURT_DEPTH.get()[0] > 1) {
            return false;
        }
        if (candidate.msgId() != null && !candidate.msgId().equals(source.getMsgId())) {
            return false;
        }
        Entity direct = source.getDirectEntity();
        if (candidate.direct() == null ? direct != source.getEntity() : !candidate.direct().isInstance(direct)) {
            return false;
        }
        for (ActiveFlags flag : ActiveFlags.values()) {
            boolean wanted = candidate.flags().contains(flag);
            if (flag.isSet() != wanted && !(flag.isSet() && LEAKY_FLAGS.contains(flag))) {
                return false;
            }
        }
        for (WoldActiveFlags flag : WoldActiveFlags.values()) {
            if (candidate.wvFlags().contains(flag) ? !flag.isSet() : flag.isSet() && PROC_WV_FLAGS.contains(flag)) {
                return false;
            }
        }
        return true;
    }

    private static String signature(DamageSource source) {
        StringBuilder flags = new StringBuilder();
        for (ActiveFlags flag : ActiveFlags.values()) {
            if (flag.isSet()) {
                flags.append(flags.length() == 0 ? "" : ",").append(flag.name());
            }
        }
        for (WoldActiveFlags flag : WoldActiveFlags.values()) {
            if (flag.isSet()) {
                flags.append(flags.length() == 0 ? "" : ",").append(flag.name());
            }
        }
        Entity direct = source.getDirectEntity();
        return source.getMsgId() + "/" + (direct == null ? "none" : direct.getClass().getSimpleName()) + "/{" + flags + "}/depth " + HURT_DEPTH.get()[0];
    }

    /** Called around every {@code LivingEntity.hurt}: hurts nested inside another are procs unless a source says otherwise. */
    public static void enterHurt() {
        HURT_DEPTH.get()[0]++;
    }

    public static void exitHurt() {
        int[] depth = HURT_DEPTH.get();
        depth[0] = Math.max(0, depth[0] - 1);
    }

    private static Map<String, List<Source>> build() {
        Map<String, List<Source>> table = new LinkedHashMap<>();
        Set<ActiveFlags> apAoe = EnumSet.of(ActiveFlags.IS_AP_ATTACKING, ActiveFlags.IS_AOE_ATTACKING);
        Set<ActiveFlags> aoe = EnumSet.of(ActiveFlags.IS_AOE_ATTACKING);

        for (String javelin : List.of("Javelin_Base", "Javelin_Piercing", "Javelin_Scatter", "Javelin_Sight")) {
            put(table, javelin, direct(Carrier.PROJECTILE, PLAYER_ATTACK, null, EnumSet.of(ActiveFlags.IS_JAVELIN_ATTACKING), "VaultThrownJavelin hit"));
        }
        put(table, "Fireball_Base", area(Carrier.PROJECTILE, PLAYER_ATTACK, null, apAoe, "VaultFireball explosion"));
        put(table, "Fireball_Volley", area(Carrier.PROJECTILE, PLAYER_ATTACK, null, apAoe, "VaultFireball explosion and 25% contact hit"));
        put(table, "Fireball_Fireshot", direct(Carrier.PROJECTILE, PLAYER_ATTACK, null,
                EnumSet.of(ActiveFlags.IS_AP_ATTACKING, ActiveFlags.IS_FIRESHOT_ATTACKING), "VaultFireball contact hit"));
        put(table, "Fireball_Fireshot", new Source(Kind.DOT, Carrier.DOT, PLAYER_ATTACK, null, EnumSet.of(ActiveFlags.IS_AP_ATTACKING),
                EnumSet.of(WoldActiveFlags.IS_NO_KNOCKBACK_DAMAGE), "PercentBurnEffect tick", false));
        for (String bolt : List.of("Ice_Bolt_Base", "Ice_Bolt_Blast")) {
            put(table, bolt, direct(Carrier.PROJECTILE, ARROW, IceBoltEntity.class, aoe, "IceBoltEntity hit and splinters"));
            put(table, bolt, direct(Carrier.PROJECTILE, ARROW, IceBoltEntity.class, EnumSet.noneOf(ActiveFlags.class), "IceBoltEntity hit with the lucky-hit etching"));
        }
        put(table, "Storm_Arrow_Base", direct(Carrier.ENTITY, PLAYER_ATTACK, null,
                EnumSet.of(ActiveFlags.IS_AP_ATTACKING, ActiveFlags.IS_SMITE_ATTACKING), "VaultStormEntity bolt"));
        put(table, "Shard_Blizzard", direct(Carrier.ENTITY, PLAYER_ATTACK, null, aoe, "VaultBlizzardShard destroy"));
        put(table, "Storm_Arrow_Blizzard", new Source(Kind.DOT, Carrier.ENTITY, PLAYER_ATTACK, null, EnumSet.of(ActiveFlags.IS_EFFECT_ATTACKING),
                EnumSet.noneOf(WoldActiveFlags.class), "hypothermia tick from its shards", false));
        put(table, "Shard_Blizzard", new Source(Kind.DOT, Carrier.ENTITY, PLAYER_ATTACK, null, EnumSet.of(ActiveFlags.IS_EFFECT_ATTACKING),
                EnumSet.noneOf(WoldActiveFlags.class), "hypothermia tick from its shards", false));
        put(table, "Nova_Base", area(Carrier.CAST, PLAYER_ATTACK, null, apAoe, "NovaAbility"));
        put(table, "Nova_Slow", new Source(Kind.DOT, Carrier.ENTITY, PLAYER_ATTACK, null, EnumSet.of(ActiveFlags.IS_EFFECT_ATTACKING),
                EnumSet.noneOf(WoldActiveFlags.class), "hypothermia tick on the target's update", false));
        for (String dot : List.of("Nova_Dot", "Toxic_Grenade")) {
            put(table, dot, new Source(Kind.DOT, Carrier.DOT, PLAYER_ATTACK, null,
                    EnumSet.of(ActiveFlags.IS_DOT_ATTACKING, ActiveFlags.IS_POISON_NOVA_ATTACKING), EnumSet.noneOf(WoldActiveFlags.class),
                    "DamageOverTimeHelper tick", false));
        }
        for (String grenade : List.of("Grenade_Base", "Grenade_Sticky")) {
            put(table, grenade, area(Carrier.PROJECTILE, EXPLOSION, null, apAoe, "VaultGrenade explosion"));
        }
        put(table, "Smite_Base", direct(Carrier.TOGGLE, PLAYER_ATTACK, null,
                EnumSet.of(ActiveFlags.IS_AP_ATTACKING, ActiveFlags.IS_SMITE_BASE_ATTACKING), "smite bolt"));
        for (String smite : List.of("Smite_Archon", "Smite_Thunderstorm")) {
            put(table, smite, direct(Carrier.TOGGLE, PLAYER_ATTACK, null,
                    EnumSet.of(ActiveFlags.IS_AP_ATTACKING, ActiveFlags.IS_SMITE_ATTACKING), "smite bolt"));
        }
        put(table, "Smite_Blast_Wave", area(Carrier.TOGGLE, PLAYER_ATTACK, null,
                EnumSet.of(ActiveFlags.IS_AP_ATTACKING, ActiveFlags.IS_AOE_ATTACKING, ActiveFlags.IS_SMITE_ATTACKING), "blast wave"));
        for (String quake : List.of("Earthquake_Base", "Earthquake_Singularity", "Earthquake_Tremor")) {
            put(table, quake, area(Carrier.CAST, PLAYER_ATTACK, null, apAoe, "first shock"));
            put(table, quake, area(Carrier.SCHEDULED, PLAYER_ATTACK, null, apAoe, "later shocks"));
        }
        put(table, "Earthquake_Landmine", area(Carrier.SCHEDULED, PLAYER_ATTACK, null, apAoe, "LandmineBlockEntity trigger"));
        put(table, "Chain_Lightning_Base", direct(Carrier.PROJECTILE, PLAYER_ATTACK, null, apAoe, "ChainLightningProjectile hit and chain"));
        put(table, "Chain_Lightning_Charged_Bolts", direct(Carrier.CAST, PLAYER_ATTACK, null, apAoe, "spell bolt path hit (VH's own path is scheduled)"));
        put(table, "Chain_Lightning_Orbs", area(Carrier.PROJECTILE, PLAYER_ATTACK, null,
                EnumSet.of(ActiveFlags.IS_AP_ATTACKING, ActiveFlags.IS_LIGHTNING_ORB_ATTACKING, ActiveFlags.IS_AOE_ATTACKING), "LightningOrbEntity tick"));
        put(table, "Arcane_Rail", direct(Carrier.CAST, PLAYER_ATTACK, null,
                EnumSet.of(ActiveFlags.IS_AP_ATTACKING, ActiveFlags.IS_ARCANE_RAIL_ATTACKING), "rail"));
        put(table, "Arcane_Prism", area(Carrier.SCHEDULED, PLAYER_ATTACK, null,
                EnumSet.of(ActiveFlags.IS_AP_ATTACKING, ActiveFlags.IS_EFFECT_ATTACKING, ActiveFlags.IS_AOE_ATTACKING), "prism pulses"));
        put(table, "Arcane_Base", direct(Carrier.TOGGLE, PLAYER_ATTACK, null,
                EnumSet.of(ActiveFlags.IS_AP_ATTACKING, ActiveFlags.IS_EFFECT_ATTACKING), "held beam tick"));
        Set<ActiveFlags> minion = EnumSet.of(ActiveFlags.IS_AP_ATTACKING, ActiveFlags.IS_NECROMANCY_MINION_ATTACKING);
        put(table, "Necromancy_Base", direct(Carrier.MINION, PLAYER_ATTACK, null, minion, "minion melee and splash"));
        put(table, "Necromancy_Golem", direct(Carrier.MINION, PLAYER_ATTACK, null, minion, "minion melee and splash"));
        put(table, "Necromancy_Archer", new Source(Kind.DIRECT, Carrier.MINION, PLAYER_ATTACK, null, minion, EnumSet.noneOf(WoldActiveFlags.class),
                "minion arrow, dealt inside the cancelled arrow hurt", true));
        for (String bash : List.of("Shield_Bash", "Shield_Bash_Earthshatter", "Shield_Bash_Battering_Ram")) {
            put(table, bash, area(Carrier.CAST, PLAYER_ATTACK, null, aoe, "ShieldBashAbility cone"));
        }
        put(table, "Mana_Shield_Retribution", area(Carrier.EVENT, PLAYER_ATTACK, null, aoe, "retribution when the charge timer ends"));
        put(table, "Mana_Shield_Implode", area(Carrier.CAST, PLAYER_ATTACK, null, aoe, "ImplodeAbility"));
        put(table, "Implode_Life_Tap", area(Carrier.CAST, PLAYER_ATTACK, null, aoe, "LifeTapAbility"));
        put(table, "Mana_Barrier", area(Carrier.CAST, PLAYER_ATTACK, null, apAoe, "ability-power etching burst"));
        put(table, "Stonefall_Snow", area(Carrier.EVENT, PLAYER_ATTACK, null, apAoe, "landing hit on the player's fall event"));
        put(table, "Taunt_Charm", new Source(Kind.DIRECT, Carrier.MINION, PLAYER_ATTACK, null, EnumSet.of(ActiveFlags.IS_CHARMED_ATTACKING),
                EnumSet.noneOf(WoldActiveFlags.class), "charmed mob hit, dealt inside the cancelled mob hurt", true));
        put(table, "Totem_Mob_Damage", area(Carrier.BLOCK_ENTITY, PLAYER_ATTACK, null,
                EnumSet.of(ActiveFlags.IS_AP_ATTACKING, ActiveFlags.IS_TOTEM_ATTACKING), "TotemMobDamageTileEntity tick"));
        put(table, "Totem_Mob_Damage", area(Carrier.BLOCK_ENTITY, PLAYER_ATTACK, null,
                EnumSet.of(ActiveFlags.IS_TOTEM_ATTACKING), "TotemMobDamageTileEntity tick with the attack-damage etching"));
        put(table, "Dash_Damage", area(Carrier.EVENT, PLAYER_ATTACK, null, aoe, "skill tick after the dash"));
        put(table, "Rampage_Chain", new Source(Kind.AREA, Carrier.EVENT, null, null, EnumSet.of(ActiveFlags.IS_CHAINING_ATTACKING),
                EnumSet.noneOf(WoldActiveFlags.class), "chain inside the melee hurt", true));
        put(table, "Shell_Porcupine", new Source(Kind.DIRECT, Carrier.EVENT, THORNS, null, EnumSet.of(ActiveFlags.IS_THORNS_REFLECTING),
                EnumSet.noneOf(WoldActiveFlags.class), "thorns inside the attacker's hit on the player", true));
        put(table, "Shell_Quill", new Source(Kind.AREA, Carrier.EVENT, THORNS, null,
                EnumSet.of(ActiveFlags.IS_AOE_ATTACKING, ActiveFlags.IS_THORNS_REFLECTING), EnumSet.noneOf(WoldActiveFlags.class),
                "quills inside the attacker's hit on the player", true));
        for (String fangs : List.of("Fangs_Base", "Fangs_Maw")) {
            put(table, fangs, new Source(Kind.DIRECT, Carrier.ENTITY, PLAYER_ATTACK, null, EnumSet.noneOf(ActiveFlags.class),
                    EnumSet.of(WoldActiveFlags.IS_FANG_ATTACKING), "CustomFangEntity tick", false));
        }
        put(table, "Expunge_Base", area(Carrier.ENTITY, INDIRECT_MAGIC, EffectCloudEntity.class, aoe, "EffectCloudEntity instant damage"));
        put(table, "Expunge_Base", new Source(Kind.DOT, Carrier.DOT, PLAYER_ATTACK, null,
                EnumSet.of(ActiveFlags.IS_AP_ATTACKING, ActiveFlags.IS_EFFECT_ATTACKING), EnumSet.noneOf(WoldActiveFlags.class),
                "poison from its clouds", false));
        put(table, "Concentrate_Base", new Source(Kind.DIRECT, Carrier.CAST, MAGIC, null, EnumSet.noneOf(ActiveFlags.class),
                EnumSet.noneOf(WoldActiveFlags.class), "drain etching (no attacker)", false));
        put(table, "UltimateShield_Base", new Source(Kind.AREA, Carrier.EVENT, PLAYER_ATTACK, null, aoe, EnumSet.noneOf(WoldActiveFlags.class),
                "Imploding Barrier, inside the player's damage event", true));
        put(table, "Cope_De_Grace", area(Carrier.EVENT, PLAYER_ATTACK, null, aoe, "server-tick dash sweep"));
        put(table, "Life_Block_Base", new Source(Kind.DIRECT, Carrier.EVENT, LIFE_BOND, null, EnumSet.of(ActiveFlags.IS_THORNS_REFLECTING),
                EnumSet.noneOf(WoldActiveFlags.class), "life bond, inside the attacker's hit on the player", true));
        put(table, "Summon_Eternal_Base", new Source(Kind.DIRECT, Carrier.MINION, MOB, EternalEntity.class, EnumSet.noneOf(ActiveFlags.class),
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

    private static Source direct(Carrier carrier, String msgId, @Nullable Class<? extends Entity> direct, Set<ActiveFlags> flags, String via) {
        return new Source(Kind.DIRECT, carrier, msgId, direct, flags, EnumSet.noneOf(WoldActiveFlags.class), via, false);
    }

    private static Source area(Carrier carrier, String msgId, @Nullable Class<? extends Entity> direct, Set<ActiveFlags> flags, String via) {
        return new Source(Kind.AREA, carrier, msgId, direct, flags, EnumSet.noneOf(WoldActiveFlags.class), via, false);
    }
}
