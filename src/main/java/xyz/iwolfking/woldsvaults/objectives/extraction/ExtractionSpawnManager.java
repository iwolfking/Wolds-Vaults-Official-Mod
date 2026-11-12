package xyz.iwolfking.woldsvaults.objectives.extraction;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import iskallia.vault.config.VaultMobsConfig;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.entity.EntityScaler;
import iskallia.vault.entity.champion.ChampionPromoter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.iwolfking.woldsvaults.config.ExtractionObjectiveConfig;
import xyz.iwolfking.woldsvaults.entities.extraction.ExtractorEntity;
import xyz.iwolfking.woldsvaults.entities.extraction.ExtractorTargetGoal;
import xyz.iwolfking.woldsvaults.entities.extraction.LaneFollowGoal;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionEffects;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionTier;
import xyz.iwolfking.woldsvaults.objectives.ExtractionObjective;
import xyz.iwolfking.woldsvaults.objectives.data.BrutalBossesRegistry;
import xyz.iwolfking.woldsvaults.objectives.lib.ObjectiveManager;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import java.util.Arrays;
import xyz.iwolfking.woldsvaults.entities.extraction.RigTarget;

/**
 * The wave director: one wave state per drilling site room, run from the main rig's tier while it drills. Spread
 * waves go out in small packets from a shuffle bag of the room's caves, each packet to the rig its cave faces; assault
 * waves focus one rig at a time; siege waves surge from every cave at once. A battering ram walks the 2-wide lanes to
 * one rig on its own clock. Every wave mob follows the room's baked lanes ({@link LaneFollowGoal}) and stays locked on
 * its rig ({@link ExtractorTargetGoal}), switching to the nearest live rig when its own is lost. Mob stats are floored
 * to the medallion rank the main rig's power maps to.
 */
public class ExtractionSpawnManager extends ObjectiveManager<ExtractionObjective> implements LaneFollowGoal.Listener {
    public static final String TAG_TARGETED = "wv_extraction_targeted";
    public static final String TAG_WAVE = "wv_ext_wave";
    public static final String TAG_HORDE = "wv_ext_horde";
    public static final String TAG_ASSASSIN = "wv_ext_assassin";
    public static final String TAG_TANK = "wv_ext_tank";
    public static final String TAG_ELITE = "wv_ext_elite";
    public static final String TAG_BOSS = "wv_ext_boss";
    public static final String TAG_CARRIER = "wv_ext_carrier";
    public static final String TAG_RAM = "wv_ext_ram";
    public static final String TAG_GREED_CHAMPION = "greed_champion";

    private static final int SPAWN_ATTEMPTS = 24;
    private static final int DEFAULT_SEARCH_RADIUS = 3;
    private static final int RAM_SEARCH_RADIUS = 5;
    private static final int SWEEP_INTERVAL = 20;
    private static final int DEBUG_INTERVAL = 1200;
    private static final UUID BOSS_SPEED_MODIFIER = UUID.fromString("5b0f1c52-7e1d-4d6b-9d2f-3a8c6e1f0b77");
    private static final UUID RAM_SPEED_MODIFIER = UUID.fromString("0e4f3d1a-6b8c-4f2e-9a7d-2c5b8e1f4a63");

    private static final int TRICKLE = 0;
    private static final int HORDE = 1;
    private static final int COMBAT = 2;
    private static final int ASSAULT = 3;
    private static final int SIEGE = 4;
    private static final int CARRIER = 5;

    private final Map<Integer, RoomWaveState> states = new HashMap<>();
    private final List<UUID> carriers = new ArrayList<>();
    private final Set<UUID> goalled = new HashSet<>();
    private final List<PendingPacket> pending = new ArrayList<>();
    private final Set<Integer> fallbackLogged = new HashSet<>();
    private int sweepTimer;
    private int auraTimer;
    private int debugTimer;

    public ExtractionSpawnManager(Vault vault, VirtualWorld world, ExtractionObjective objective) {
        super(vault, world, objective);
    }

    @Override
    public void tick() {
        ExtractionObjectiveConfig.WavesEntry waves = ModConfigs.EXTRACTION_OBJECTIVE.WAVES;
        List<ExtractionSite> drilling = objective.getDrillingSitesWithLiveExtractor(world);
        if (drilling.isEmpty()) {
            this.states.clear();
            this.pending.clear();
            return;
        }
        Set<Integer> active = new HashSet<>();
        for (ExtractionSite site : drilling) {
            ExtractorEntity extractor = objective.getLiveExtractor(world, site);
            if (extractor == null) {
                continue;
            }
            RoomWaveState state = this.stateFor(site, extractor, waves);
            if (state == null) {
                continue;
            }
            active.add(state.siteIndex);
            this.tickRoom(state, waves);
        }
        this.states.keySet().retainAll(active);
        this.flushPending(waves);
        if (++sweepTimer >= SWEEP_INTERVAL) {
            sweepTimer = 0;
            for (ExtractionSite site : drilling) {
                this.sweepRoom(site);
            }
            this.goalled.removeIf(id -> {
                Entity entity = world.getEntity(id);
                return entity == null || !entity.isAlive();
            });
        }
        if (++auraTimer >= waves.carrier.auraIntervalTicks) {
            auraTimer = 0;
            pulseCarrierAuras(waves.carrier);
        }
        if (waves.spawnDebugLog && ++debugTimer >= DEBUG_INTERVAL) {
            debugTimer = 0;
            this.states.values().forEach(this::logDebug);
        }
    }

    private RoomWaveState stateFor(ExtractionSite site, ExtractorEntity extractor, ExtractionObjectiveConfig.WavesEntry waves) {
        int index = extractor.getSiteIndex();
        RoomWaveState state = this.states.get(index);
        if (state != null && state.extractorId.equals(extractor.getUUID())) {
            return state;
        }
        ExtractionObjectiveConfig.TierEntry tier = waves.tierFor(extractor.getPower());
        if (tier == null) {
            WoldLogger.error("Extraction WAVES has no tier for power {} (site {}); no waves will spawn there.", extractor.getPower(), index);
            return null;
        }
        double floor = floorFactor(waves.statFloor, extractor.getPower());
        state = new RoomWaveState(site, index, extractor.getUUID(), tier, floor, objective.caveAnchors(site).size(), objective.sitePedestals(site).size());
        state.nextRamTick = waves.ram.firstTicks;
        this.states.put(index, state);
        WoldLogger.info("Extraction site {} starts waves at tier '{}' (power {}, stat floor x{}, vault medallion x{}).",
                index, tier.name, extractor.getPower(), String.format("%.2f", floor),
                String.format("%.2f", GreedMedallionEffects.mobStatMultiplier(vault)));
        return state;
    }

    /**
     * The medallion factor a site's mobs are floored to: exponential between the two anchors around the power,
     * the first anchor's factor below the table and the last anchor's factor above it.
     */
    public static double floorFactor(ExtractionObjectiveConfig.StatFloorEntry cfg, int power) {
        if (!cfg.enabled || cfg.anchors.isEmpty()) {
            return 1.0D;
        }
        List<ExtractionObjectiveConfig.FloorAnchor> anchors = new ArrayList<>(cfg.anchors);
        anchors.sort((a, b) -> Integer.compare(a.power, b.power));
        ExtractionObjectiveConfig.FloorAnchor first = anchors.get(0);
        ExtractionObjectiveConfig.FloorAnchor last = anchors.get(anchors.size() - 1);
        if (power <= first.power) {
            return rankFactor(first.medallionRank);
        }
        if (power >= last.power) {
            return rankFactor(last.medallionRank);
        }
        for (int i = 0; i < anchors.size() - 1; i++) {
            ExtractionObjectiveConfig.FloorAnchor a = anchors.get(i);
            ExtractionObjectiveConfig.FloorAnchor b = anchors.get(i + 1);
            if (power >= a.power && power < b.power) {
                double fa = rankFactor(a.medallionRank);
                double fb = rankFactor(b.medallionRank);
                if (b.power == a.power || fa <= 0.0D || fb <= 0.0D) {
                    return Math.max(fa, fb);
                }
                double t = (power - a.power) / (double) (b.power - a.power);
                return fa * Math.pow(fb / fa, t);
            }
        }
        return rankFactor(last.medallionRank);
    }

    private static double rankFactor(int medallionRank) {
        return GreedMedallionTier.byRankIndex(medallionRank)
                .map(tier -> GreedMedallionEffects.multiplier(tier.getMobHealthDamageBonus()))
                .orElseGet(() -> {
                    WoldLogger.error("Extraction stat floor anchor names medallion rank {}, which does not exist; treating it as no floor.", medallionRank);
                    return 1.0D;
                });
    }

    private void tickRoom(RoomWaveState state, ExtractionObjectiveConfig.WavesEntry waves) {
        ExtractionObjectiveConfig.TierEntry tier = state.tier;
        if (state.due(TRICKLE, tier.trickle.intervalTicks)) {
            this.spawnTimedWave(state, tier.trickle, waves, TRICKLE);
        }
        if (state.due(HORDE, tier.horde.intervalTicks)) {
            this.spawnTimedWave(state, tier.horde, waves, HORDE);
        }
        if (state.due(COMBAT, tier.combat.intervalTicks)) {
            this.spawnTimedWave(state, tier.combat, waves, COMBAT);
        }
        if (state.due(ASSAULT, tier.assault.intervalTicks)) {
            this.spawnTimedWave(state, tier.assault, waves, ASSAULT);
        }
        if (state.due(SIEGE, tier.siege.intervalTicks)) {
            this.spawnTimedWave(state, tier.siege, waves, SIEGE);
        }
        if (state.due(CARRIER, waves.carrier.intervalTicks)) {
            int cave = this.nextBagCave(state);
            if (cave >= 0) {
                this.spawnCarrier(state, cave, waves.carrier);
            }
        }
        if (waves.ram.maxPerRoom > 0 && ++state.ramClock >= state.nextRamTick) {
            state.nextRamTick += Math.max(20, tier.ramIntervalTicks);
            this.spawnRam(state, waves);
        }
    }

    private int scaled(int count, ExtractionObjectiveConfig.WavesEntry waves, RoomWaveState state) {
        if (count <= 0) {
            return 0;
        }
        float progress = objective.siteProgressFraction(state.site);
        double multiplier = waves.roomCountMultiplier * (1.0D + (waves.rampAtFullProgress - 1.0D) * progress);
        double value = count * multiplier;
        int base = (int) Math.floor(value);
        return base + (world.getRandom().nextDouble() < value - base ? 1 : 0);
    }

    private void spawnTimedWave(RoomWaveState state, ExtractionObjectiveConfig.TimedWaveEntry wave, ExtractionObjectiveConfig.WavesEntry waves, int kind) {
        ExtractionObjectiveConfig.TierEntry tier = state.tier;
        int[] counts = {
                this.scaled(wave.horde.roll(world.getRandom()), waves, state),
                this.scaled(wave.assassins.roll(world.getRandom()), waves, state),
                this.scaled(wave.tanks.roll(world.getRandom()), waves, state),
                this.scaled(wave.elites.roll(world.getRandom()), waves, state)
        };
        int bosses = wave.bosses.roll(world.getRandom());
        Group[] groups = {
                new Group(tier.hordePool, TAG_HORDE, DEFAULT_SEARCH_RADIUS, null),
                new Group(tier.assassinPool, TAG_ASSASSIN, DEFAULT_SEARCH_RADIUS, null),
                new Group(tier.tankPool, TAG_TANK, DEFAULT_SEARCH_RADIUS, null),
                new Group(tier.elitePool, TAG_ELITE, waves.boss.spawnSearchRadius,
                        living -> setAttributeBase(living, Attributes.KNOCKBACK_RESISTANCE, waves.elite.knockbackResistance))
        };
        int total = counts[0] + counts[1] + counts[2] + counts[3] + bosses;
        if (total <= 0) {
            return;
        }
        long now = world.getGameTime();
        if (kind == SIEGE && waves.surgeSiege) {
            List<Integer> order = this.usableCaves(state);
            Collections.shuffle(order, world.getRandom());
            if (order.isEmpty()) {
                return;
            }
            Map<Long, Integer> perCaveClass = new HashMap<>();
            int dealt = 0;
            for (int g = 0; g < groups.length; g++) {
                for (int i = 0; i < counts[g]; i++) {
                    int cave = order.get(dealt++ % order.size());
                    perCaveClass.merge(((long) cave << 8) | g, 1, Integer::sum);
                }
            }
            for (Map.Entry<Long, Integer> entry : perCaveClass.entrySet()) {
                int cave = (int) (entry.getKey() >> 8);
                int g = (int) (entry.getKey() & 0xFF);
                this.pending.add(new PendingPacket(state.siteIndex, cave, -1, groups[g], entry.getValue(), now));
            }
            for (LivingEntity rig : objective.liveRigs(world, state.site)) {
                world.playSound(null, rig.blockPosition(), SoundEvents.RAID_HORN, SoundSource.HOSTILE, 3.0F, 1.0F);
            }
        } else if (kind == ASSAULT) {
            int pad = this.nextFocusPad(state, waves);
            int delay = 0;
            for (int g = 0; g < groups.length; g++) {
                for (int size : packets(counts[g], waves.packetSize)) {
                    int cave = pad < 0 ? this.nextBagCave(state) : this.lruCave(state, pad);
                    if (cave >= 0) {
                        this.pending.add(new PendingPacket(state.siteIndex, cave, pad, groups[g], size, now + delay));
                        delay += waves.packetStaggerTicks;
                    }
                }
            }
            LivingEntity focused = pad < 0 ? null : objective.rigAt(world, state.site, pad);
            if (focused != null) {
                world.playSound(null, focused.blockPosition(), SoundEvents.RAID_HORN, SoundSource.HOSTILE, 1.5F, 0.6F);
            }
        } else {
            int delay = 0;
            for (int g = 0; g < groups.length; g++) {
                for (int size : packets(counts[g], waves.packetSize)) {
                    int cave = this.nextBagCave(state);
                    if (cave >= 0) {
                        this.pending.add(new PendingPacket(state.siteIndex, cave, -1, groups[g], size, now + delay));
                        delay += waves.packetStaggerTicks;
                    }
                }
            }
        }
        for (int i = 0; i < bosses; i++) {
            int cave = this.nextBagCave(state);
            if (cave >= 0) {
                this.spawnBoss(state, cave, waves);
            }
        }
    }

    private static List<Integer> packets(int count, int packetSize) {
        List<Integer> out = new ArrayList<>();
        int left = count;
        while (left > 0) {
            out.add(Math.min(packetSize, left));
            left -= packetSize;
        }
        return out;
    }

    private List<Integer> usableCaves(RoomWaveState state) {
        RoomGrid grid = objective.gridFor(state.siteIndex);
        List<Integer> out = new ArrayList<>();
        for (int cave = 0; cave < state.caveCount; cave++) {
            if (grid == null || !grid.isReady() || grid.isCaveUsable(cave)) {
                out.add(cave);
            }
        }
        return out;
    }

    private int nextBagCave(RoomWaveState state) {
        if (state.bag.isEmpty()) {
            List<Integer> refill = this.usableCaves(state);
            Collections.shuffle(refill, world.getRandom());
            state.bag.addAll(refill);
        }
        Integer cave = state.bag.poll();
        return cave == null ? -1 : cave;
    }

    private int nextFocusPad(RoomWaveState state, ExtractionObjectiveConfig.WavesEntry waves) {
        int size = waves.focusOrder.size();
        for (int tries = 0; tries < size; tries++) {
            int pad = waves.focusOrder.get(state.focusIndex++ % size);
            if (objective.rigAt(world, state.site, pad) != null) {
                return pad;
            }
        }
        return -1;
    }

    private int lruCave(RoomWaveState state, int pad) {
        List<Integer> feeders = state.feeders.computeIfAbsent(pad, p -> {
            List<Integer> list = new ArrayList<>();
            for (int cave : this.usableCaves(state)) {
                for (int home : this.homesOf(state, cave)) {
                    if (home == p) {
                        list.add(cave);
                    }
                }
            }
            return list;
        });
        if (feeders.isEmpty()) {
            return this.nextBagCave(state);
        }
        int cave = feeders.remove(0);
        feeders.add(cave);
        return cave;
    }

    private int[] homesOf(RoomWaveState state, int cave) {
        RoomGrid grid = objective.gridFor(state.siteIndex);
        if (grid != null && grid.isReady()) {
            return grid.caveHomes(cave);
        }
        if (this.fallbackLogged.add(state.siteIndex)) {
            WoldLogger.error("Extraction site {} has no room grid yet; caves use the authored pad groups instead of the baked fields.", state.siteIndex);
        }
        return objective.authoredCaveHomes(cave);
    }

    private int homePad(RoomWaveState state, int cave) {
        int[] homes = this.homesOf(state, cave);
        if (homes.length == 0) {
            return 0;
        }
        if (homes.length == 1) {
            return homes[0];
        }
        return homes[state.toggles[cave]++ % homes.length];
    }

    private void flushPending(ExtractionObjectiveConfig.WavesEntry waves) {
        if (this.pending.isEmpty()) {
            return;
        }
        long now = world.getGameTime();
        Iterator<PendingPacket> iterator = this.pending.iterator();
        List<PendingPacket> due = new ArrayList<>();
        while (iterator.hasNext()) {
            PendingPacket packet = iterator.next();
            if (packet.dueTick <= now) {
                due.add(packet);
                iterator.remove();
            }
        }
        for (PendingPacket packet : due) {
            RoomWaveState state = this.states.get(packet.siteIndex);
            if (state == null) {
                continue;
            }
            this.spawnPacket(state, packet, waves);
        }
    }

    private void spawnPacket(RoomWaveState state, PendingPacket packet, ExtractionObjectiveConfig.WavesEntry waves) {
        if (packet.group.pool.isEmpty()) {
            WoldLogger.error("Extraction tier '{}' asked for {} '{}' mobs but its pool is empty; skipping them.", state.tier.name, packet.count, packet.group.tag);
            return;
        }
        int pad = packet.pad >= 0 ? packet.pad : this.homePad(state, packet.cave);
        LivingEntity rig = this.resolveRig(state, pad, packet.cave);
        if (rig == null) {
            return;
        }
        BlockPos anchor = objective.caveAnchors(state.site).get(packet.cave);
        List<BlockPos> used = new ArrayList<>();
        for (int i = 0; i < packet.count; i++) {
            if (!this.underCaps(state, waves)) {
                state.dropped += packet.count - i;
                return;
            }
            LivingEntity mob = this.spawnOne(state, packet.group.pool, anchor, packet.group.tag, packet.group.radius, null, packet.group.afterScale, rig, used);
            if (mob != null) {
                state.live.add(mob.getUUID());
                state.spawnedPerCave[packet.cave]++;
            }
        }
    }

    private LivingEntity resolveRig(RoomWaveState state, int pad, int cave) {
        LivingEntity rig = objective.rigAt(world, state.site, pad);
        if (rig != null) {
            return rig;
        }
        RoomGrid grid = objective.gridFor(state.siteIndex);
        LivingEntity best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (LivingEntity candidate : objective.liveRigs(world, state.site)) {
            int candidatePad = ((RigTarget) candidate).getPadIndex();
            int distance = grid == null || !grid.isReady() ? Integer.MAX_VALUE - 1 : grid.caveDistance(cave, candidatePad, false);
            if (distance >= 0 && distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        return best;
    }

    private boolean underCaps(RoomWaveState state, ExtractionObjectiveConfig.WavesEntry waves) {
        if (state.live.size() >= waves.liveMobCapPerRoom) {
            state.purge(world);
            if (state.live.size() >= waves.liveMobCapPerRoom) {
                return false;
            }
        }
        int total = 0;
        for (RoomWaveState other : this.states.values()) {
            total += other.live.size();
        }
        if (total >= waves.liveMobCapPerVault) {
            for (RoomWaveState other : this.states.values()) {
                other.purge(world);
            }
            total = 0;
            for (RoomWaveState other : this.states.values()) {
                total += other.live.size();
            }
        }
        return total < waves.liveMobCapPerVault;
    }

    private LivingEntity spawnOne(RoomWaveState state, WeightedList<ResourceLocation> pool, BlockPos anchor, String classTag, int searchRadius,
                                  Consumer<LivingEntity> beforeAdd, Consumer<LivingEntity> afterScale, LivingEntity rig, List<BlockPos> used) {
        ResourceLocation id = pool.getRandom(world.getRandom()).orElse(null);
        if (id == null || !ForgeRegistries.ENTITIES.containsKey(id)) {
            WoldLogger.error("Extraction wave pool rolled unknown entity id {}; skipping this spawn.", id);
            return null;
        }
        EntityType<?> type = ForgeRegistries.ENTITIES.getValue(id);
        BlockPos pos = findSpawnPos(anchor, type, searchRadius, used);
        if (pos == null) {
            WoldLogger.error("Extraction wave found no clear spawn spot near cave anchor {} for {}; skipping this spawn.", anchor, id);
            return null;
        }
        Entity created = type.create(world);
        if (!(created instanceof LivingEntity living)) {
            WoldLogger.error("Extraction wave entity {} is not a living entity; skipping this spawn.", id);
            return null;
        }
        used.add(pos);
        living.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, world.getRandom().nextFloat() * 360.0F, 0.0F);
        living.addTag(classTag);
        living.addTag(TAG_WAVE);
        if (beforeAdd != null) {
            beforeAdd.accept(living);
        }
        if (living instanceof Mob mob) {
            mob.setPersistenceRequired();
            mob.spawnAnim();
            ForgeEventFactory.doSpecialSpawn(mob, world, (float) mob.getX(), (float) mob.getY(), (float) mob.getZ(), null, MobSpawnType.SPAWNER);
        } else {
            WoldLogger.error("Extraction wave entity {} is not a Mob; vault modifiers will not be applied to it.", id);
        }
        world.addWithUUID(living);
        EntityScaler.scale(vault, living);
        this.applyStatFloor(living, state);
        if (afterScale != null) {
            afterScale.accept(living);
        }
        this.attachGoals(living, state.site, state.siteIndex, rig);
        return living;
    }

    /**
     * Raises the mob's final max health and attack damage to what they would be under the site's floor medallion
     * factor, by scaling the base value the way the medallion itself does; a mob already at or above the floor
     * is left untouched.
     */
    private void applyStatFloor(LivingEntity living, RoomWaveState state) {
        double vaultFactor = GreedMedallionEffects.mobStatMultiplier(vault);
        if (vaultFactor <= 0.0D || state.floorFactor <= vaultFactor) {
            return;
        }
        double ratio = state.floorFactor / vaultFactor;
        boolean healthRaised = raiseToFloor(living, Attributes.MAX_HEALTH, ratio);
        raiseToFloor(living, Attributes.ATTACK_DAMAGE, ratio);
        if (healthRaised) {
            living.setHealth(living.getMaxHealth());
        }
    }

    private static boolean raiseToFloor(LivingEntity living, Attribute attribute, double ratio) {
        AttributeInstance instance = living.getAttribute(attribute);
        if (instance == null) {
            WoldLogger.error("Extraction wave mob {} has no {} attribute; the stat floor skipped it.", living.getType().getRegistryName(), attribute.getDescriptionId());
            return false;
        }
        double before = instance.getValue();
        double base = instance.getBaseValue();
        instance.setBaseValue(base * ratio);
        if (instance.getValue() <= before) {
            instance.setBaseValue(base);
            WoldLogger.error("Extraction stat floor could not raise {} on {} (base {} x{} left the final value at {}); reverted.",
                    attribute.getDescriptionId(), living.getType().getRegistryName(), base, ratio, before);
            return false;
        }
        return true;
    }

    private static void setAttributeBase(LivingEntity living, Attribute attribute, double value) {
        AttributeInstance instance = living.getAttribute(attribute);
        if (instance == null) {
            WoldLogger.error("Extraction wave mob {} has no {} attribute; it keeps its default.", living.getType().getRegistryName(), attribute.getDescriptionId());
            return;
        }
        instance.setBaseValue(value);
    }

    private void spawnBoss(RoomWaveState state, int cave, ExtractionObjectiveConfig.WavesEntry waves) {
        ExtractionObjectiveConfig.BossEntry cfg = waves.boss;
        if (cfg.pool.isEmpty()) {
            WoldLogger.error("Extraction tier '{}' asked for a brutal boss but WAVES.boss.pool is empty; skipping it.", state.tier.name);
            return;
        }
        if (!this.underCaps(state, waves)) {
            state.dropped++;
            return;
        }
        LivingEntity rig = this.resolveRig(state, this.homePad(state, cave), cave);
        if (rig == null) {
            return;
        }
        Component name = BrutalBossesRegistry.BOSS_NAME_LIST.getRandom(world.getRandom())
                .orElseGet(() -> new TextComponent("Brute").withStyle(ChatFormatting.RED));
        String modifiers = BrutalBossesRegistry.getRandomMobModifiers(Math.max(1, cfg.infernalModifierCount), true);
        Consumer<LivingEntity> beforeAdd = living -> {
            living.setCustomName(name);
            living.setCustomNameVisible(true);
            living.setGlowingTag(true);
            InfernalMobsCore.instance().addEntityModifiersByString(living, modifiers);
        };
        Consumer<LivingEntity> afterScale = living -> {
            setAttributeBase(living, Attributes.KNOCKBACK_RESISTANCE, cfg.knockbackResistance);
            AttributeInstance speed = living.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed == null) {
                WoldLogger.error("Extraction brutal boss {} has no movement speed attribute; it keeps its default speed.", living.getType().getRegistryName());
            } else if (cfg.speedMultiplier > 0.0F && cfg.speedMultiplier != 1.0F) {
                speed.removeModifier(BOSS_SPEED_MODIFIER);
                speed.addPermanentModifier(new AttributeModifier(BOSS_SPEED_MODIFIER, "Extraction brutal boss speed",
                        cfg.speedMultiplier - 1.0D, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        };
        List<Integer> tries = new ArrayList<>(List.of(cave));
        for (int i = 0; i < 3; i++) {
            int other = this.nextBagCave(state);
            if (other >= 0) {
                tries.add(other);
            }
        }
        for (int attempt : tries) {
            BlockPos anchor = objective.caveAnchors(state.site).get(attempt);
            LivingEntity boss = this.spawnOne(state, cfg.pool, anchor, TAG_BOSS, cfg.spawnSearchRadius, beforeAdd, afterScale, rig, new ArrayList<>());
            if (boss != null) {
                state.live.add(boss.getUUID());
                state.spawnedPerCave[attempt]++;
                return;
            }
        }
        WoldLogger.error("Extraction site {} could not place a brutal boss at any of {} cave anchors; the wave goes on without it.", state.siteIndex, tries.size());
    }

    private void spawnCarrier(RoomWaveState state, int cave, ExtractionObjectiveConfig.CarrierEntry carrierCfg) {
        LivingEntity rig = this.resolveRig(state, this.homePad(state, cave), cave);
        if (rig == null) {
            return;
        }
        WeightedList<ResourceLocation> pool = new WeightedList<>();
        pool.add(new ResourceLocation(carrierCfg.baseEntity), 1);
        BlockPos anchor = objective.caveAnchors(state.site).get(cave);
        LivingEntity carrier = this.spawnOne(state, pool, anchor, TAG_CARRIER, DEFAULT_SEARCH_RADIUS, null, null, rig, new ArrayList<>());
        if (carrier == null) {
            return;
        }
        state.spawnedPerCave[cave]++;
        carrier.addTag("vault_champion");
        ChampionPromoter.applyRandomChampion(carrier);
        multiplyAttribute(carrier, Attributes.MAX_HEALTH, carrierCfg.healthMultiplier);
        carrier.setHealth(carrier.getMaxHealth());
        multiplyAttribute(carrier, Attributes.ATTACK_DAMAGE, carrierCfg.damageMultiplier);
        carrier.setCustomName(new TextComponent("Power Cell Carrier").withStyle(ChatFormatting.YELLOW));
        carrier.addEffect(new MobEffectInstance(MobEffects.GLOWING, Integer.MAX_VALUE, 0, false, false));
        carriers.add(carrier.getUUID());
    }

    private void spawnRam(RoomWaveState state, ExtractionObjectiveConfig.WavesEntry waves) {
        ExtractionObjectiveConfig.RamEntry cfg = waves.ram;
        state.rams.entrySet().removeIf(entry -> {
            Entity entity = world.getEntity(entry.getKey());
            return entity == null || !entity.isAlive();
        });
        if (state.rams.size() >= cfg.maxPerRoom) {
            return;
        }
        List<LivingEntity> rigs = objective.liveRigs(world, state.site);
        LivingEntity target = null;
        int targetPad = -1;
        int fewest = Integer.MAX_VALUE;
        Collections.shuffle(rigs, world.getRandom());
        for (LivingEntity rig : rigs) {
            int pad = ((RigTarget) rig).getPadIndex();
            int onIt = (int) state.rams.values().stream().filter(p -> p == pad).count();
            if (onIt >= cfg.maxPerTarget) {
                continue;
            }
            boolean better = onIt < fewest || (onIt == fewest && targetPad == 0 && pad != 0);
            if (better) {
                fewest = onIt;
                target = rig;
                targetPad = pad;
            }
        }
        if (target == null) {
            return;
        }
        RoomGrid grid = objective.gridFor(state.siteIndex);
        int cave = -1;
        double farthest = -1.0D;
        for (int candidate : this.usableCaves(state)) {
            boolean feeds = false;
            for (int home : this.homesOf(state, candidate)) {
                feeds |= home == targetPad;
            }
            if (!feeds) {
                continue;
            }
            double distance = grid != null && grid.isReady() ? grid.caveDistance(candidate, targetPad, true)
                    : objective.caveAnchors(state.site).get(candidate).distSqr(target.blockPosition());
            if (distance > farthest) {
                farthest = distance;
                cave = candidate;
            }
        }
        if (cave < 0) {
            WoldLogger.error("Extraction site {} found no cave feeding pad {} for a battering ram; using a random cave.", state.siteIndex, targetPad);
            cave = this.nextBagCave(state);
            if (cave < 0) {
                return;
            }
        }
        ResourceLocation id = new ResourceLocation(cfg.entity);
        if (!ForgeRegistries.ENTITIES.containsKey(id)) {
            WoldLogger.error("Extraction battering ram entity {} is not registered; no ram spawns.", id);
            return;
        }
        WeightedList<ResourceLocation> pool = new WeightedList<>();
        pool.add(id, 1);
        BlockPos anchor = objective.caveAnchors(state.site).get(cave);
        LivingEntity ram = this.spawnOne(state, pool, anchor, TAG_RAM, RAM_SEARCH_RADIUS, living -> {
            living.setCustomName(new TranslatableComponent("entity.woldsvaults.battering_ram").withStyle(ChatFormatting.DARK_RED));
            living.setCustomNameVisible(true);
            living.setGlowingTag(true);
        }, living -> this.applyRamStats(living, cfg), target, new ArrayList<>());
        if (ram == null) {
            return;
        }
        state.rams.put(ram.getUUID(), targetPad);
        state.spawnedPerCave[cave]++;
        world.playSound(null, anchor, SoundEvents.RAID_HORN, SoundSource.HOSTILE, 4.0F, 0.5F);
        WoldLogger.info("Extraction site {} sent a battering ram ({} HP) from cave {} at pad {}.", state.siteIndex,
                String.format("%.0f", ram.getMaxHealth()), cave, targetPad);
    }

    /** Ram HP = hpEliteMultiple x the reference mob's rolled HP in this vault, kept through the same multipliers as the ram. */
    private void applyRamStats(LivingEntity ram, ExtractionObjectiveConfig.RamEntry cfg) {
        double referenceRaw = this.rawHealth(cfg.hpReference);
        double ramRaw = this.rawHealth(cfg.entity);
        AttributeInstance health = ram.getAttribute(Attributes.MAX_HEALTH);
        if (health == null) {
            WoldLogger.error("Battering ram {} has no max health attribute; it keeps its scaled HP.", cfg.entity);
        } else if (referenceRaw <= 0.0D || ramRaw <= 0.0D) {
            WoldLogger.error("Battering ram HP reference {} (raw {}) or ram (raw {}) has no vault health roll; the ram uses its own scaled HP x{} instead.",
                    cfg.hpReference, referenceRaw, ramRaw, cfg.hpEliteMultiple);
            health.setBaseValue(health.getBaseValue() * cfg.hpEliteMultiple);
        } else {
            health.setBaseValue(health.getBaseValue() * cfg.hpEliteMultiple * referenceRaw / ramRaw);
        }
        ram.setHealth(ram.getMaxHealth());
        setAttributeBase(ram, Attributes.KNOCKBACK_RESISTANCE, 1.0D);
        AttributeInstance speed = ram.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            WoldLogger.error("Battering ram {} has no movement speed attribute; it keeps its default speed.", cfg.entity);
        } else {
            speed.removeModifier(RAM_SPEED_MODIFIER);
            speed.addPermanentModifier(new AttributeModifier(RAM_SPEED_MODIFIER, "Extraction battering ram speed",
                    cfg.speedMultiplier - 1.0D, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    private double rawHealth(String entityId) {
        ResourceLocation id = new ResourceLocation(entityId);
        EntityType<?> type = ForgeRegistries.ENTITIES.getValue(id);
        if (type == null || !ForgeRegistries.ENTITIES.containsKey(id)) {
            return -1.0D;
        }
        Entity created = type.create(world);
        if (!(created instanceof LivingEntity living)) {
            return -1.0D;
        }
        try {
            int level = vault.has(Vault.LEVEL) ? vault.get(Vault.LEVEL).get() : 0;
            VaultMobsConfig.scale(vault.get(Vault.OWNER), living, level);
            return living.getMaxHealth();
        } catch (Exception e) {
            WoldLogger.error("Rolling vault health for {} failed.", entityId, e);
            return -1.0D;
        } finally {
            living.discard();
        }
    }

    private static void multiplyAttribute(LivingEntity entity, Attribute attribute, float multiplier) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) {
            WoldLogger.error("Power cell carrier {} has no {} attribute; its multiplier was skipped.", entity.getName().getString(), attribute.getDescriptionId());
            return;
        }
        instance.setBaseValue(instance.getBaseValue() * multiplier);
    }

    /** Re-attaches goals to wave mobs that lost them (goals are not saved, so after a restart every mob needs them again). */
    private void sweepRoom(ExtractionSite site) {
        int siteIndex = objective.siteIndex(site);
        AABB bounds = objective.siteRoomBounds(site);
        for (Mob mob : world.getEntitiesOfClass(Mob.class, bounds)) {
            if (!mob.isAlive() || this.goalled.contains(mob.getUUID()) || mob.getTags().contains(TAG_GREED_CHAMPION)) {
                continue;
            }
            boolean wave = mob.getTags().contains(TAG_WAVE);
            if (!wave && !(mob instanceof Enemy)) {
                continue;
            }
            LivingEntity rig = this.nearestRig(site, siteIndex, mob);
            if (rig != null) {
                this.attachGoals(mob, site, siteIndex, rig);
            }
        }
    }

    private void attachGoals(LivingEntity living, ExtractionSite site, int siteIndex, LivingEntity rig) {
        if (!(living instanceof Mob mob) || mob.getTags().contains(TAG_GREED_CHAMPION) || rig == null) {
            return;
        }
        mob.targetSelector.addGoal(0, new ExtractorTargetGoal(mob, rig.getUUID(), m -> {
            LivingEntity next = this.nearestRig(site, siteIndex, m);
            return next == null ? null : next.getUUID();
        }));
        if (mob.getNavigation() instanceof GroundPathNavigation) {
            boolean wide = mob.getBbWidth() > 1.0F;
            mob.goalSelector.addGoal(0, new LaneFollowGoal(mob, siteIndex, () -> objective.gridFor(siteIndex), wide, this));
        } else if (this.fallbackLogged.add(1000 + mob.getType().hashCode())) {
            WoldLogger.info("Extraction wave mob type {} has no ground navigation; it skips lane pathing.", mob.getType().getRegistryName());
        }
        mob.addTag(TAG_TARGETED);
        this.goalled.add(mob.getUUID());
    }

    /** The live rig of the site nearest to the mob along the lanes (straight-line distance when no grid is ready). */
    private LivingEntity nearestRig(ExtractionSite site, int siteIndex, Mob mob) {
        RoomGrid grid = objective.gridFor(siteIndex);
        boolean wide = mob.getBbWidth() > 1.0F;
        int cell = grid != null && grid.isReady() ? grid.cellAt(mob.blockPosition(), wide) : -1;
        LivingEntity best = null;
        double bestScore = Double.MAX_VALUE;
        for (LivingEntity rig : objective.liveRigs(world, site)) {
            int pad = ((RigTarget) rig).getPadIndex();
            int distance = cell >= 0 ? grid.distance(pad, wide, cell) : -1;
            double score = distance >= 0 ? distance : 1000.0D + Math.sqrt(rig.distanceToSqr(mob));
            if (score < bestScore) {
                bestScore = score;
                best = rig;
            }
        }
        return best;
    }

    private void pulseCarrierAuras(ExtractionObjectiveConfig.CarrierEntry carrierCfg) {
        Iterator<UUID> iterator = carriers.iterator();
        while (iterator.hasNext()) {
            Entity entity = world.getEntity(iterator.next());
            if (!(entity instanceof LivingEntity carrier) || !carrier.isAlive()) {
                iterator.remove();
                continue;
            }
            AABB range = carrier.getBoundingBox().inflate(carrierCfg.auraRadius);
            for (Mob mob : world.getEntitiesOfClass(Mob.class, range)) {
                if (mob == carrier || !mob.getTags().contains(TAG_WAVE)) {
                    continue;
                }
                mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, carrierCfg.auraEffectDurationTicks, 0));
                if (!mob.getTags().contains(TAG_ELITE) && !mob.getTags().contains(TAG_BOSS) && !mob.getTags().contains(TAG_RAM)) {
                    mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, carrierCfg.auraEffectDurationTicks, 0));
                }
            }
        }
    }

    private BlockPos findSpawnPos(BlockPos anchor, EntityType<?> type, int radius, List<BlockPos> used) {
        int span = radius * 2 + 1;
        int vertical = Math.max(3, radius);
        for (int attempt = 0; attempt < SPAWN_ATTEMPTS; attempt++) {
            int x = anchor.getX() + world.getRandom().nextInt(span) - radius;
            int z = anchor.getZ() + world.getRandom().nextInt(span) - radius;
            boolean taken = false;
            for (BlockPos other : used) {
                if (other.getX() == x && other.getZ() == z) {
                    taken = true;
                    break;
                }
            }
            if (taken && attempt < SPAWN_ATTEMPTS / 2) {
                continue;
            }
            for (int yOffset = -vertical; yOffset <= vertical; yOffset++) {
                BlockPos candidate = new BlockPos(x, anchor.getY() + yOffset, z);
                BlockPos floor = candidate.below();
                if (!world.getBlockState(floor).isValidSpawn(world, floor, type)) {
                    continue;
                }
                AABB box = type.getAABB(x + 0.5D, candidate.getY(), z + 0.5D);
                if (!world.noCollision(box)) {
                    continue;
                }
                if (attempt < SPAWN_ATTEMPTS / 2 && !world.getEntitiesOfClass(LivingEntity.class, box.inflate(0.3D), LivingEntity::isAlive).isEmpty()) {
                    continue;
                }
                return candidate;
            }
        }
        return null;
    }

    @Override
    public void arrived(int siteIndex, int pad, int walkTicks) {
        RoomWaveState state = this.states.get(siteIndex);
        if (state != null && pad >= 0 && pad < state.arrivedPerPad.length) {
            state.arrivedPerPad[pad]++;
            state.walkTicksPerPad[pad] += walkTicks;
        }
    }

    @Override
    public void nudged(int siteIndex) {
        RoomWaveState state = this.states.get(siteIndex);
        if (state != null) {
            state.nudges++;
        }
    }

    private void logDebug(RoomWaveState state) {
        state.purge(world);
        StringBuilder caves = new StringBuilder();
        for (int cave = 0; cave < state.spawnedPerCave.length; cave++) {
            caves.append(cave == 0 ? "" : " ").append('S').append(cave + 1).append('=').append(state.spawnedPerCave[cave]);
        }
        StringBuilder pads = new StringBuilder();
        for (int pad = 0; pad < state.arrivedPerPad.length; pad++) {
            int n = state.arrivedPerPad[pad];
            pads.append(pad == 0 ? "" : " ").append("pad").append(pad).append('=').append(n)
                    .append(n > 0 ? String.format(" (%.1fs walk)", state.walkTicksPerPad[pad] / 20.0D / n) : "");
        }
        WoldLogger.info("Extraction site {} waves (last 60 s): spawned per cave [{}]; reached pads [{}]; live {}, rams {}, dropped at cap {}, stuck nudges {}.",
                state.siteIndex, caves, pads, state.live.size(), state.rams.size(), state.dropped, state.nudges);
        Arrays.fill(state.spawnedPerCave, 0);
        Arrays.fill(state.arrivedPerPad, 0);
        Arrays.fill(state.walkTicksPerPad, 0L);
        state.dropped = 0;
        state.nudges = 0;
    }

    private record Group(WeightedList<ResourceLocation> pool, String tag, int radius, Consumer<LivingEntity> afterScale) {
    }

    private record PendingPacket(int siteIndex, int cave, int pad, Group group, int count, long dueTick) {
    }

    /** One drilling room's wave clocks, cave rotation, live mobs and debug counters; replaced when a different main rig drills there. */
    private static final class RoomWaveState {
        final ExtractionSite site;
        final int siteIndex;
        final UUID extractorId;
        final ExtractionObjectiveConfig.TierEntry tier;
        final double floorFactor;
        final int caveCount;
        final Set<UUID> live = new HashSet<>();
        final Map<UUID, Integer> rams = new HashMap<>();
        final ArrayDeque<Integer> bag = new ArrayDeque<>();
        final Map<Integer, List<Integer>> feeders = new HashMap<>();
        final int[] toggles;
        final int[] spawnedPerCave;
        final int[] arrivedPerPad;
        final long[] walkTicksPerPad;
        int focusIndex;
        int ramClock;
        int nextRamTick;
        int dropped;
        int nudges;
        private final int[] timers = new int[6];

        RoomWaveState(ExtractionSite site, int siteIndex, UUID extractorId, ExtractionObjectiveConfig.TierEntry tier, double floorFactor, int caveCount, int padCount) {
            this.site = site;
            this.siteIndex = siteIndex;
            this.extractorId = extractorId;
            this.tier = tier;
            this.floorFactor = floorFactor;
            this.caveCount = caveCount;
            this.toggles = new int[caveCount];
            this.spawnedPerCave = new int[caveCount];
            this.arrivedPerPad = new int[Math.max(1, padCount)];
            this.walkTicksPerPad = new long[Math.max(1, padCount)];
        }

        boolean due(int wave, int intervalTicks) {
            if (intervalTicks <= 0) {
                return false;
            }
            if (++this.timers[wave] >= intervalTicks) {
                this.timers[wave] = 0;
                return true;
            }
            return false;
        }

        void purge(VirtualWorld world) {
            this.live.removeIf(id -> {
                Entity entity = world.getEntity(id);
                return entity == null || !entity.isAlive();
            });
        }
    }
}
