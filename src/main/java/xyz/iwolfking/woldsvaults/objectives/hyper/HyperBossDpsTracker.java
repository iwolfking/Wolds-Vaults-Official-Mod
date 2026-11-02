package xyz.iwolfking.woldsvaults.objectives.hyper;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.DamageNumberFormat;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;
import xyz.iwolfking.woldsvaults.events.HyperVaultEvents;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.mixins.LivingEntityAccessor;
import xyz.iwolfking.woldsvaults.network.message.BossDpsSnapshotMessage;
import xyz.iwolfking.woldsvaults.network.message.BossDpsSnapshotMessage.Snapshot;
import xyz.iwolfking.woldsvaults.network.message.BossDpsSnapshotMessage.Source;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Measures the damage each player actually lands on a hyperboss and feeds the boss DPS HUD module.
 *
 * <p>Every hit is followed from {@link LivingAttackEvent} (the amount the attacker swung for) through
 * {@link LivingHurtEvent} (after the hurt-band multipliers) and {@link LivingDamageEvent} (after armor and
 * absorption) to the health it removed, capped at the boss's remaining health. Attacks that never reach
 * the damage event are counted as blocked; the vanilla invulnerability window is predicted at attack time
 * from {@code invulnerableTime} and {@code lastHurt}, so a hit swallowed by it ({@code amount <= lastHurt})
 * or shrunk by it (lands as {@code amount - lastHurt}) is reported separately from hits cancelled elsewhere.
 *
 * <p>Damage whose source has no player (or player-owned pet) attached is credited to every player
 * currently fighting that boss under an {@code unattributed:} label, so in multiplayer those hits count
 * once per fighter. With {@code logHyperbossDamage} on, every hit, every blocked attack and a periodic
 * per-source summary go to the log.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class HyperBossDpsTracker {
    private static final int WINDOW_TICKS = 100;
    private static final int SYNC_INTERVAL_TICKS = 5;
    private static final int LIVE_TICKS = 200;
    private static final int SUMMARY_INTERVAL_TICKS = 100;
    private static final int HUD_SOURCES = 4;
    private static final int LABEL_LIMIT = 64;

    private static final class Attempt {
        private final LivingEntity boss;
        private final DamageSource source;
        private final float attempted;
        private final boolean iframe;
        private final float lastHurt;
        private float preArmor = Float.NaN;

        private Attempt(LivingEntity boss, DamageSource source, float attempted, boolean iframe, float lastHurt) {
            this.boss = boss;
            this.source = source;
            this.attempted = attempted;
            this.iframe = iframe;
            this.lastHurt = lastHurt;
        }
    }

    private record WindowHit(long tick, double dealt) {
    }

    private static final class SourceStats {
        private double dealt;
        private int hits;
    }

    private static final class Fight {
        private final UUID bossId;
        private final ResourceKey<Level> dimension;
        private final ArrayDeque<WindowHit> window = new ArrayDeque<>();
        private final Map<String, SourceStats> sources = new LinkedHashMap<>();
        private double windowSum;
        private long firstHitTick = -1L;
        private long lastHitTick;
        private long lastSyncTick = Long.MIN_VALUE / 2;
        private long lastSummaryTick;
        private int hitsAtLastSummary;
        private double total;
        private double maxHit;
        private double lastHit;
        private int hits;
        private int iframeBlocked;
        private int iframeReduced;
        private int otherBlocked;
        private float bossHealth;
        private float bossMaxHealth;
        private boolean ended;
        private boolean dirty;

        private Fight(LivingEntity boss) {
            this.bossId = boss.getUUID();
            this.dimension = boss.level.dimension();
        }

        private void prune(long now) {
            while (!this.window.isEmpty() && now - this.window.peekFirst().tick() >= WINDOW_TICKS) {
                this.windowSum -= this.window.pollFirst().dealt();
            }
            if (this.window.isEmpty()) {
                this.windowSum = 0.0;
            }
        }

        private int windowTicks(long now) {
            return (int) Math.max(20L, Math.min(WINDOW_TICKS, now - this.firstHitTick + 1L));
        }

        private double windowDps(long now) {
            return this.firstHitTick < 0L ? 0.0 : this.windowSum * 20.0 / windowTicks(now);
        }

        private int durationTicks() {
            return this.firstHitTick < 0L ? 0 : (int) (this.lastHitTick - this.firstHitTick + 1L);
        }

        private double fightDps() {
            return this.firstHitTick < 0L ? 0.0 : this.total * 20.0 / Math.max(20, durationTicks());
        }

        private List<Map.Entry<String, SourceStats>> sortedSources() {
            List<Map.Entry<String, SourceStats>> sorted = new ArrayList<>(this.sources.entrySet());
            sorted.sort((a, b) -> Double.compare(b.getValue().dealt, a.getValue().dealt));
            return sorted;
        }

        private Snapshot snapshot(long now) {
            List<Source> top = new ArrayList<>();
            for (Map.Entry<String, SourceStats> entry : sortedSources()) {
                if (top.size() >= HUD_SOURCES) {
                    break;
                }
                top.add(new Source(entry.getKey(), entry.getValue().dealt, entry.getValue().hits));
            }
            return new Snapshot(true, this.ended, this.total, fightDps(), windowDps(now), windowTicks(now),
                    durationTicks(), this.hits, this.iframeBlocked, this.iframeReduced, this.otherBlocked,
                    this.maxHit, this.lastHit, this.bossHealth, this.bossMaxHealth, top);
        }
    }

    private static final ThreadLocal<Deque<Attempt>> PENDING = ThreadLocal.withInitial(ArrayDeque::new);
    private static final Map<UUID, Fight> FIGHTS = new ConcurrentHashMap<>();

    private HyperBossDpsTracker() {
    }

    private static boolean logging() {
        return WoldsVaultsConfig.COMMON.logHyperbossDamage.get();
    }

    /** Drops the player's fight statistics and clears their HUD. */
    public static void reset(ServerPlayer player) {
        Fight removed = FIGHTS.remove(player.getUUID());
        if (removed != null && removed.hits > 0 && logging()) {
            logSummary(player.getGameProfile().getName(), removed, player.level.getGameTime(), "reset");
        }
        ModNetwork.sendToClient(new BossDpsSnapshotMessage(Snapshot.INACTIVE), player);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAttack(LivingAttackEvent event) {
        LivingEntity boss = event.getEntityLiving();
        if (boss.level.isClientSide() || !HyperVaultEvents.isHyperBoss(boss)) {
            return;
        }
        boolean iframe = boss.invulnerableTime > 10.0F;
        float lastHurt = ((LivingEntityAccessor) boss).getLastHurt();
        PENDING.get().push(new Attempt(boss, event.getSource(), event.getAmount(), iframe, lastHurt));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onHurt(LivingHurtEvent event) {
        Deque<Attempt> pending = PENDING.get();
        if (pending.isEmpty()) {
            return;
        }
        for (Attempt attempt : pending) {
            if (attempt.boss == event.getEntityLiving() && attempt.source == event.getSource()) {
                attempt.preArmor = event.getAmount();
                return;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDamage(LivingDamageEvent event) {
        LivingEntity boss = event.getEntityLiving();
        if (boss.level.isClientSide()) {
            return;
        }
        Attempt attempt = claim(boss, event.getSource());
        if (attempt == null && !HyperVaultEvents.isHyperBoss(boss)) {
            return;
        }
        recordHit(boss, event.getSource(), attempt, event.getAmount());
    }

    /**
     * Pops the newest pending attempt matching this boss and source. Attempts pushed after it that
     * never reached their damage event have finished without landing and are recorded as blocked.
     */
    private static Attempt claim(LivingEntity boss, DamageSource source) {
        Deque<Attempt> pending = PENDING.get();
        if (pending.isEmpty()) {
            return null;
        }
        boolean found = false;
        for (Attempt attempt : pending) {
            if (attempt.boss == boss && attempt.source == source) {
                found = true;
                break;
            }
        }
        if (!found) {
            return null;
        }
        while (!pending.isEmpty()) {
            Attempt top = pending.pop();
            if (top.boss == boss && top.source == source) {
                return top;
            }
            recordBlocked(top);
        }
        return null;
    }

    private static void drainPending() {
        Deque<Attempt> pending = PENDING.get();
        while (!pending.isEmpty()) {
            recordBlocked(pending.pollLast());
        }
    }

    private static ServerPlayer attacker(DamageSource source) {
        Entity entity = source.getEntity();
        if (entity instanceof ServerPlayer player) {
            return player;
        }
        if (entity instanceof OwnableEntity ownable && ownable.getOwner() instanceof ServerPlayer owner) {
            return owner;
        }
        return null;
    }

    private static String typeName(Entity entity) {
        return entity.getType().getRegistryName() == null ? "?" : entity.getType().getRegistryName().getPath();
    }

    private static String label(DamageSource source, ServerPlayer player) {
        Entity owner = source.getEntity();
        Entity direct = source.getDirectEntity();
        String label;
        if (owner == null) {
            label = "unattributed:" + source.getMsgId();
        } else if (owner != player) {
            label = "pet:" + typeName(owner) + ":" + source.getMsgId();
        } else if (direct != null && direct != owner) {
            label = source.getMsgId() + ":" + typeName(direct);
        } else {
            label = source.getMsgId();
        }
        return label.length() > LABEL_LIMIT ? label.substring(0, LABEL_LIMIT) : label;
    }

    private record Credit(String playerName, Fight fight, String label) {
    }

    /**
     * The fights a hit on this boss counts towards: the attacker's (opened, or restarted when they
     * switch bosses), or for unattributed damage every open fight on this boss.
     */
    private static List<Credit> credits(LivingEntity boss, DamageSource source) {
        List<Credit> credits = new ArrayList<>();
        ServerPlayer player = attacker(source);
        if (player != null) {
            Fight fight = FIGHTS.get(player.getUUID());
            if (fight == null || !fight.bossId.equals(boss.getUUID())) {
                if (fight != null && fight.hits > 0 && logging()) {
                    logSummary(player.getGameProfile().getName(), fight, boss.level.getGameTime(), "new boss");
                }
                fight = new Fight(boss);
                FIGHTS.put(player.getUUID(), fight);
            }
            credits.add(new Credit(player.getGameProfile().getName(), fight, label(source, player)));
            return credits;
        }
        if (boss.getServer() == null) {
            return credits;
        }
        for (Map.Entry<UUID, Fight> entry : FIGHTS.entrySet()) {
            if (entry.getValue().bossId.equals(boss.getUUID()) && !entry.getValue().ended) {
                ServerPlayer fighter = boss.getServer().getPlayerList().getPlayer(entry.getKey());
                String name = fighter == null ? entry.getKey().toString().substring(0, 8) : fighter.getGameProfile().getName();
                credits.add(new Credit(name, entry.getValue(), label(source, null)));
            }
        }
        return credits;
    }

    private static void recordHit(LivingEntity boss, DamageSource source, Attempt attempt, float finalAmount) {
        long now = boss.level.getGameTime();
        float healthBefore = boss.getHealth();
        double dealt = Math.max(0.0, Math.min(finalAmount, healthBefore));
        double overkill = Math.max(0.0, finalAmount - dealt);
        boolean reduced = attempt != null && attempt.iframe;
        List<Credit> credits = credits(boss, source);
        for (Credit credit : credits) {
            Fight fight = credit.fight();
            if (fight.firstHitTick < 0L) {
                fight.firstHitTick = now;
                fight.lastSummaryTick = now;
            }
            fight.lastHitTick = now;
            fight.total += dealt;
            fight.hits++;
            fight.lastHit = dealt;
            fight.maxHit = Math.max(fight.maxHit, dealt);
            if (reduced) {
                fight.iframeReduced++;
            }
            fight.window.addLast(new WindowHit(now, dealt));
            fight.windowSum += dealt;
            fight.prune(now);
            SourceStats stats = fight.sources.computeIfAbsent(credit.label(), key -> new SourceStats());
            stats.dealt += dealt;
            stats.hits++;
            fight.bossHealth = (float) Math.max(0.0, healthBefore - dealt);
            fight.bossMaxHealth = boss.getMaxHealth();
            fight.dirty = true;
        }
        if (!logging()) {
            return;
        }
        String attempted = attempt == null ? "? (no attack event)" : DamageNumberFormat.format(attempt.attempted);
        String iframeNote = reduced
                ? String.format(Locale.ROOT, " [i-frames: lastHurt %s, landed as %s, lost %s]",
                DamageNumberFormat.format(attempt.lastHurt),
                DamageNumberFormat.format(attempt.attempted - attempt.lastHurt),
                DamageNumberFormat.format(attempt.lastHurt))
                : "";
        String preArmor = attempt == null || Float.isNaN(attempt.preArmor) ? "?" : DamageNumberFormat.format(attempt.preArmor);
        for (Credit credit : credits) {
            WoldsVaults.LOGGER.info(
                    "Hyperboss DPS hit [{}] {}: attempted {}{} -> after hurt bands {} -> after armor {} -> health removed {}{} | boss {} ({}) | 5s dps {} | fight total {} in {} hits",
                    credit.playerName(), credit.label(), attempted, iframeNote, preArmor,
                    DamageNumberFormat.format(finalAmount), DamageNumberFormat.format(dealt),
                    overkill > 0.0 ? " (overkill " + DamageNumberFormat.format(overkill) + ")" : "",
                    DamageNumberFormat.format(credit.fight().bossHealth),
                    DamageNumberFormat.percent(credit.fight().bossMaxHealth <= 0.0F ? 0.0 : credit.fight().bossHealth / credit.fight().bossMaxHealth),
                    DamageNumberFormat.format(credit.fight().windowDps(now)),
                    DamageNumberFormat.format(credit.fight().total), credit.fight().hits);
        }
        if (credits.isEmpty()) {
            WoldsVaults.LOGGER.info("Hyperboss DPS hit with no fighter to credit: {} removed {} (source {})",
                    attempted, DamageNumberFormat.format(dealt), source.getMsgId());
        }
    }

    private static void recordBlocked(Attempt attempt) {
        boolean byIframes = attempt.iframe && attempt.attempted <= attempt.lastHurt;
        String reason = byIframes
                ? "i-frames (attempted <= lastHurt " + DamageNumberFormat.format(attempt.lastHurt) + ")"
                : Float.isNaN(attempt.preArmor) ? "cancelled before the hurt event" : "cancelled or zeroed after the hurt bands";
        for (Credit credit : credits(attempt.boss, attempt.source)) {
            if (byIframes) {
                credit.fight().iframeBlocked++;
            } else {
                credit.fight().otherBlocked++;
            }
            credit.fight().dirty = true;
            if (logging()) {
                WoldsVaults.LOGGER.info("Hyperboss DPS blocked [{}] {}: attempted {} landed nothing: {}",
                        credit.playerName(), credit.label(), DamageNumberFormat.format(attempt.attempted), reason);
            }
        }
    }

    private static void logSummary(String playerName, Fight fight, long now, String reason) {
        StringBuilder sources = new StringBuilder();
        for (Map.Entry<String, SourceStats> entry : fight.sortedSources()) {
            sources.append(String.format(Locale.ROOT, " | %s %s (%s, %d hits, avg %s)", entry.getKey(),
                    DamageNumberFormat.format(entry.getValue().dealt),
                    DamageNumberFormat.percent(fight.total <= 0.0 ? 0.0 : entry.getValue().dealt / fight.total),
                    entry.getValue().hits,
                    DamageNumberFormat.format(entry.getValue().dealt / Math.max(1, entry.getValue().hits))));
        }
        WoldsVaults.LOGGER.info(
                "Hyperboss DPS summary [{}] ({}): fight dps {} | 5s dps {} | total {} over {}s in {} hits | max hit {} | i-frames {} blocked, {} reduced | {} blocked elsewhere | boss {} ({}){}",
                playerName, reason, DamageNumberFormat.format(fight.fightDps()),
                DamageNumberFormat.format(fight.windowDps(now)), DamageNumberFormat.format(fight.total),
                String.format(Locale.ROOT, "%.1f", fight.durationTicks() / 20.0), fight.hits,
                DamageNumberFormat.format(fight.maxHit), fight.iframeBlocked, fight.iframeReduced, fight.otherBlocked,
                DamageNumberFormat.format(fight.bossHealth),
                DamageNumberFormat.percent(fight.bossMaxHealth <= 0.0F ? 0.0 : fight.bossHealth / fight.bossMaxHealth),
                sources);
        fight.lastSummaryTick = now;
        fight.hitsAtLastSummary = fight.hits;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBossDeath(LivingDeathEvent event) {
        LivingEntity boss = event.getEntityLiving();
        if (boss.level.isClientSide() || !HyperVaultEvents.isHyperBoss(boss)) {
            return;
        }
        drainPending();
        long now = boss.level.getGameTime();
        for (Map.Entry<UUID, Fight> entry : FIGHTS.entrySet()) {
            Fight fight = entry.getValue();
            if (!fight.bossId.equals(boss.getUUID()) || fight.ended) {
                continue;
            }
            fight.ended = true;
            fight.bossHealth = 0.0F;
            ServerPlayer player = boss.getServer() == null ? null : boss.getServer().getPlayerList().getPlayer(entry.getKey());
            if (logging()) {
                logSummary(player == null ? entry.getKey().toString() : player.getGameProfile().getName(), fight, now, "boss killed");
            }
            if (player != null) {
                ModNetwork.sendToClient(new BossDpsSnapshotMessage(fight.snapshot(now)), player);
            }
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isClientSide()) {
            return;
        }
        drainPending();
        if (FIGHTS.isEmpty() || event.world.getServer() == null) {
            return;
        }
        long now = event.world.getGameTime();
        Iterator<Map.Entry<UUID, Fight>> iterator = FIGHTS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Fight> entry = iterator.next();
            Fight fight = entry.getValue();
            if (fight.ended || fight.dimension != event.world.dimension()) {
                continue;
            }
            ServerPlayer player = event.world.getServer().getPlayerList().getPlayer(entry.getKey());
            if (player == null) {
                iterator.remove();
                continue;
            }
            boolean live = fight.firstHitTick >= 0L && now - fight.lastHitTick <= LIVE_TICKS;
            if ((fight.dirty || live) && now - fight.lastSyncTick >= SYNC_INTERVAL_TICKS) {
                fight.prune(now);
                ModNetwork.sendToClient(new BossDpsSnapshotMessage(fight.snapshot(now)), player);
                fight.lastSyncTick = now;
                fight.dirty = false;
            }
            if (logging() && fight.hits > fight.hitsAtLastSummary && now - fight.lastSummaryTick >= SUMMARY_INTERVAL_TICKS) {
                logSummary(player.getGameProfile().getName(), fight, now, "periodic");
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            drainPending();
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        FIGHTS.remove(event.getPlayer().getUUID());
    }
}
