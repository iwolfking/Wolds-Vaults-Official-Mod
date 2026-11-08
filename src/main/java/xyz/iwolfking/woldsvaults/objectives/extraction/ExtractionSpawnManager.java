package xyz.iwolfking.woldsvaults.objectives.extraction;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.entity.EntityScaler;
import iskallia.vault.entity.champion.ChampionPromoter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
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
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.ExtractionObjectiveConfig;
import xyz.iwolfking.woldsvaults.entities.extraction.ExtractorEntity;
import xyz.iwolfking.woldsvaults.entities.extraction.ExtractorTargetGoal;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionEffects;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionTier;
import xyz.iwolfking.woldsvaults.objectives.ExtractionObjective;
import xyz.iwolfking.woldsvaults.objectives.data.BrutalBossesRegistry;
import xyz.iwolfking.woldsvaults.objectives.lib.ObjectiveManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Runs the wave table of every drilling site on its own clock. Each site picks a tier from its extractor's
 * power when drilling starts and keeps it until that extractor leaves; mob stats are floored to the medallion
 * rank that power maps to, so a strong rig on a weak map still faces the fight the rig is rated for.
 */
public class ExtractionSpawnManager extends ObjectiveManager<ExtractionObjective> {
    public static final String TAG_TARGETED = "wv_extraction_targeted";
    public static final String TAG_WAVE = "wv_ext_wave";
    public static final String TAG_HORDE = "wv_ext_horde";
    public static final String TAG_ASSASSIN = "wv_ext_assassin";
    public static final String TAG_TANK = "wv_ext_tank";
    public static final String TAG_ELITE = "wv_ext_elite";
    public static final String TAG_BOSS = "wv_ext_boss";
    public static final String TAG_CARRIER = "wv_ext_carrier";
    public static final String TAG_GREED_CHAMPION = "greed_champion";

    private static final int SPAWN_ATTEMPTS = 24;
    private static final int DEFAULT_SEARCH_RADIUS = 3;
    private static final int SWEEP_INTERVAL = 20;
    private static final UUID BOSS_SPEED_MODIFIER = UUID.fromString("5b0f1c52-7e1d-4d6b-9d2f-3a8c6e1f0b77");

    private final Map<Integer, SiteWaveState> states = new HashMap<>();
    private final List<UUID> carriers = new ArrayList<>();
    private int sweepTimer;
    private int auraTimer;

    public ExtractionSpawnManager(Vault vault, VirtualWorld world, ExtractionObjective objective) {
        super(vault, world, objective);
    }

    @Override
    public void tick() {
        ExtractionObjectiveConfig.WavesEntry waves = ModConfigs.EXTRACTION_OBJECTIVE.WAVES;
        List<ExtractionSite> drilling = objective.getDrillingSitesWithLiveExtractor(world);
        if (drilling.isEmpty()) {
            this.states.clear();
            return;
        }
        Set<Integer> active = new HashSet<>();
        for (ExtractionSite site : drilling) {
            ExtractorEntity extractor = objective.getLiveExtractor(world, site);
            if (extractor == null) {
                continue;
            }
            SiteWaveState state = this.stateFor(site, extractor, waves);
            if (state == null) {
                continue;
            }
            active.add(state.siteIndex);
            this.tickSite(state, waves);
        }
        this.states.keySet().retainAll(active);
        if (++sweepTimer >= SWEEP_INTERVAL) {
            sweepTimer = 0;
            drilling.forEach(this::sweepRoomTargets);
        }
        if (++auraTimer >= waves.carrier.auraIntervalTicks) {
            auraTimer = 0;
            pulseCarrierAuras(waves.carrier);
        }
    }

    private SiteWaveState stateFor(ExtractionSite site, ExtractorEntity extractor, ExtractionObjectiveConfig.WavesEntry waves) {
        int index = extractor.getSiteIndex();
        SiteWaveState state = this.states.get(index);
        if (state != null && state.extractorId.equals(extractor.getUUID())) {
            return state;
        }
        ExtractionObjectiveConfig.TierEntry tier = waves.tierFor(extractor.getPower());
        if (tier == null) {
            WoldsVaults.LOGGER.error("Extraction WAVES has no tier for power {} (site {}); no waves will spawn there.", extractor.getPower(), index);
            return null;
        }
        double floor = floorFactor(waves.statFloor, extractor.getPower());
        state = new SiteWaveState(site, index, extractor.getUUID(), tier, floor);
        this.states.put(index, state);
        WoldsVaults.LOGGER.info("Extraction site {} starts waves at tier '{}' (power {}, stat floor x{}, vault medallion x{}).",
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
                    WoldsVaults.LOGGER.error("Extraction stat floor anchor names medallion rank {}, which does not exist; treating it as no floor.", medallionRank);
                    return 1.0D;
                });
    }

    private void tickSite(SiteWaveState state, ExtractionObjectiveConfig.WavesEntry waves) {
        ExtractionObjectiveConfig.TierEntry tier = state.tier;
        if (state.due(SiteWaveState.TRICKLE, tier.trickle.intervalTicks)) {
            this.spawnTimedWave(state, tier.trickle, waves, false);
        }
        if (state.due(SiteWaveState.HORDE, tier.horde.intervalTicks)) {
            this.spawnTimedWave(state, tier.horde, waves, false);
        }
        if (state.due(SiteWaveState.COMBAT, tier.combat.intervalTicks)) {
            this.spawnTimedWave(state, tier.combat, waves, false);
        }
        if (state.due(SiteWaveState.ASSAULT, tier.assault.intervalTicks)) {
            this.spawnTimedWave(state, tier.assault, waves, false);
        }
        if (state.due(SiteWaveState.SIEGE, tier.siege.intervalTicks)) {
            this.spawnTimedWave(state, tier.siege, waves, true);
        }
        if (state.due(SiteWaveState.CARRIER, waves.carrier.intervalTicks)) {
            this.spawnCarrier(state, waves.carrier);
        }
    }

    private void spawnTimedWave(SiteWaveState state, ExtractionObjectiveConfig.TimedWaveEntry wave, ExtractionObjectiveConfig.WavesEntry waves, boolean siege) {
        ExtractionObjectiveConfig.TierEntry tier = state.tier;
        int hordes = wave.horde.roll(world.getRandom());
        int assassins = wave.assassins.roll(world.getRandom());
        int tanks = wave.tanks.roll(world.getRandom());
        int elites = wave.elites.roll(world.getRandom());
        int bosses = wave.bosses.roll(world.getRandom());
        if (siege && hordes + assassins + tanks + elites + bosses > 0) {
            ExtractorEntity extractor = objective.getLiveExtractor(world, state.site);
            if (extractor != null) {
                world.playSound(null, extractor.blockPosition(), SoundEvents.RAID_HORN, SoundSource.HOSTILE, 3.0F, 1.0F);
            }
        }
        this.spawnGroup(state, tier.hordePool, hordes, TAG_HORDE, waves, DEFAULT_SEARCH_RADIUS, null);
        this.spawnGroup(state, tier.assassinPool, assassins, TAG_ASSASSIN, waves, DEFAULT_SEARCH_RADIUS, null);
        this.spawnGroup(state, tier.tankPool, tanks, TAG_TANK, waves, DEFAULT_SEARCH_RADIUS, null);
        this.spawnGroup(state, tier.elitePool, elites, TAG_ELITE, waves, waves.boss.spawnSearchRadius,
                living -> setAttributeBase(living, Attributes.KNOCKBACK_RESISTANCE, waves.elite.knockbackResistance));
        for (int i = 0; i < bosses; i++) {
            this.spawnBoss(state, waves);
        }
    }

    private void spawnGroup(SiteWaveState state, WeightedList<ResourceLocation> pool, int count, String classTag,
                            ExtractionObjectiveConfig.WavesEntry waves, int searchRadius, Consumer<LivingEntity> afterScale) {
        if (count <= 0) {
            return;
        }
        if (pool.isEmpty()) {
            WoldsVaults.LOGGER.error("Extraction tier '{}' asked for {} '{}' mobs but its pool is empty; skipping them.", state.tier.name, count, classTag);
            return;
        }
        BlockPos anchor = objective.randomCaveAnchor(state.site, world.getRandom());
        for (int i = 0; i < count; i++) {
            if (!state.underCap(world, waves.liveMobCapPerSite)) {
                WoldsVaults.LOGGER.debug("Extraction site {} is at its live mob cap ({}); dropping the rest of this '{}' group.", state.siteIndex, waves.liveMobCapPerSite, classTag);
                return;
            }
            this.spawnOne(state, pool, anchor, classTag, searchRadius, null, afterScale);
        }
    }

    private LivingEntity spawnOne(SiteWaveState state, WeightedList<ResourceLocation> pool, BlockPos anchor, String classTag,
                                  int searchRadius, Consumer<LivingEntity> beforeAdd, Consumer<LivingEntity> afterScale) {
        ResourceLocation id = pool.getRandom(world.getRandom()).orElse(null);
        if (id == null || !ForgeRegistries.ENTITIES.containsKey(id)) {
            WoldsVaults.LOGGER.error("Extraction wave pool rolled unknown entity id {}; skipping this spawn.", id);
            return null;
        }
        EntityType<?> type = ForgeRegistries.ENTITIES.getValue(id);
        BlockPos pos = findSpawnPos(anchor, type, searchRadius);
        if (pos == null) {
            WoldsVaults.LOGGER.error("Extraction wave found no clear spawn spot near cave anchor {} for {}; skipping this spawn.", anchor, id);
            return null;
        }
        Entity created = type.create(world);
        if (!(created instanceof LivingEntity living)) {
            WoldsVaults.LOGGER.error("Extraction wave entity {} is not a living entity; skipping this spawn.", id);
            return null;
        }
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
            WoldsVaults.LOGGER.error("Extraction wave entity {} is not a Mob; vault modifiers will not be applied to it.", id);
        }
        world.addWithUUID(living);
        EntityScaler.scale(vault, living);
        this.applyStatFloor(living, state);
        if (afterScale != null) {
            afterScale.accept(living);
        }
        attachTargetGoal(living, state.site);
        state.live.add(living.getUUID());
        return living;
    }

    /**
     * Raises the mob's final max health and attack damage to what they would be under the site's floor medallion
     * factor, by scaling the base value the way the medallion itself does; a mob already at or above the floor
     * is left untouched.
     */
    private void applyStatFloor(LivingEntity living, SiteWaveState state) {
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
            WoldsVaults.LOGGER.error("Extraction wave mob {} has no {} attribute; the stat floor skipped it.", living.getType().getRegistryName(), attribute.getDescriptionId());
            return false;
        }
        double before = instance.getValue();
        double base = instance.getBaseValue();
        instance.setBaseValue(base * ratio);
        if (instance.getValue() <= before) {
            instance.setBaseValue(base);
            WoldsVaults.LOGGER.error("Extraction stat floor could not raise {} on {} (base {} x{} left the final value at {}); reverted.",
                    attribute.getDescriptionId(), living.getType().getRegistryName(), base, ratio, before);
            return false;
        }
        return true;
    }

    private static void setAttributeBase(LivingEntity living, Attribute attribute, double value) {
        AttributeInstance instance = living.getAttribute(attribute);
        if (instance == null) {
            WoldsVaults.LOGGER.error("Extraction wave mob {} has no {} attribute; it keeps its default.", living.getType().getRegistryName(), attribute.getDescriptionId());
            return;
        }
        instance.setBaseValue(value);
    }

    private void spawnBoss(SiteWaveState state, ExtractionObjectiveConfig.WavesEntry waves) {
        ExtractionObjectiveConfig.BossEntry cfg = waves.boss;
        if (cfg.pool.isEmpty()) {
            WoldsVaults.LOGGER.error("Extraction tier '{}' asked for a brutal boss but WAVES.boss.pool is empty; skipping it.", state.tier.name);
            return;
        }
        if (!state.underCap(world, waves.liveMobCapPerSite)) {
            WoldsVaults.LOGGER.debug("Extraction site {} is at its live mob cap ({}); dropping a brutal boss.", state.siteIndex, waves.liveMobCapPerSite);
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
                WoldsVaults.LOGGER.error("Extraction brutal boss {} has no movement speed attribute; it keeps its default speed.", living.getType().getRegistryName());
            } else if (cfg.speedMultiplier > 0.0F && cfg.speedMultiplier != 1.0F) {
                speed.removeModifier(BOSS_SPEED_MODIFIER);
                speed.addPermanentModifier(new AttributeModifier(BOSS_SPEED_MODIFIER, "Extraction brutal boss speed",
                        cfg.speedMultiplier - 1.0D, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        };
        LivingEntity boss = null;
        for (int attempt = 0; attempt < 4 && boss == null; attempt++) {
            BlockPos anchor = objective.randomCaveAnchor(state.site, world.getRandom());
            boss = this.spawnOne(state, cfg.pool, anchor, TAG_BOSS, cfg.spawnSearchRadius, beforeAdd, afterScale);
        }
        if (boss == null) {
            WoldsVaults.LOGGER.error("Extraction site {} could not place a brutal boss at any cave anchor after 4 attempts; the siege goes on without it.", state.siteIndex);
        }
    }

    private void spawnCarrier(SiteWaveState state, ExtractionObjectiveConfig.CarrierEntry carrierCfg) {
        WeightedList<ResourceLocation> pool = new WeightedList<>();
        pool.add(new ResourceLocation(carrierCfg.baseEntity), 1);
        BlockPos anchor = objective.randomCaveAnchor(state.site, world.getRandom());
        LivingEntity carrier = this.spawnOne(state, pool, anchor, TAG_CARRIER, DEFAULT_SEARCH_RADIUS, null, null);
        if (carrier == null) {
            return;
        }
        carrier.addTag("vault_champion");
        ChampionPromoter.applyRandomChampion(carrier);
        multiplyAttribute(carrier, Attributes.MAX_HEALTH, carrierCfg.healthMultiplier);
        carrier.setHealth(carrier.getMaxHealth());
        multiplyAttribute(carrier, Attributes.ATTACK_DAMAGE, carrierCfg.damageMultiplier);
        carrier.setCustomName(new TextComponent("Power Cell Carrier").withStyle(ChatFormatting.YELLOW));
        carrier.addEffect(new MobEffectInstance(MobEffects.GLOWING, Integer.MAX_VALUE, 0, false, false));
        carriers.add(carrier.getUUID());
    }

    private static void multiplyAttribute(LivingEntity entity, Attribute attribute, float multiplier) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) {
            WoldsVaults.LOGGER.error("Power cell carrier {} has no {} attribute; its multiplier was skipped.", entity.getName().getString(), attribute.getDescriptionId());
            return;
        }
        instance.setBaseValue(instance.getBaseValue() * multiplier);
    }

    private void sweepRoomTargets(ExtractionSite site) {
        ExtractorEntity extractor = objective.getLiveExtractor(world, site);
        if (extractor == null) {
            return;
        }
        AABB bounds = objective.siteRoomBounds(site);
        for (Mob mob : world.getEntitiesOfClass(Mob.class, bounds)) {
            if (!(mob instanceof Enemy) || mob.getTags().contains(TAG_TARGETED) || mob.getTags().contains(TAG_GREED_CHAMPION)) {
                continue;
            }
            attachTargetGoal(mob, site);
        }
    }

    private void attachTargetGoal(LivingEntity living, ExtractionSite site) {
        ExtractorEntity extractor = objective.getLiveExtractor(world, site);
        if (extractor == null || !(living instanceof Mob mob) || mob.getTags().contains(TAG_GREED_CHAMPION)) {
            return;
        }
        mob.targetSelector.addGoal(0, new ExtractorTargetGoal(mob, extractor.getUUID()));
        mob.addTag(TAG_TARGETED);
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
                if (mob == carrier || !(mob instanceof Enemy)) {
                    continue;
                }
                mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, carrierCfg.auraEffectDurationTicks, 0));
                if (!mob.getTags().contains(TAG_ELITE) && !mob.getTags().contains(TAG_BOSS)) {
                    mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, carrierCfg.auraEffectDurationTicks, 0));
                }
            }
        }
    }

    private BlockPos findSpawnPos(BlockPos anchor, EntityType<?> type, int radius) {
        int span = radius * 2 + 1;
        int vertical = Math.max(3, radius);
        for (int attempt = 0; attempt < SPAWN_ATTEMPTS; attempt++) {
            int x = anchor.getX() + world.getRandom().nextInt(span) - radius;
            int z = anchor.getZ() + world.getRandom().nextInt(span) - radius;
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
                return candidate;
            }
        }
        return null;
    }

    /** One drilling site's wave clocks, tier and floor; replaced whenever a different extractor stands on the site. */
    private static final class SiteWaveState {
        static final int TRICKLE = 0;
        static final int HORDE = 1;
        static final int COMBAT = 2;
        static final int ASSAULT = 3;
        static final int SIEGE = 4;
        static final int CARRIER = 5;

        final ExtractionSite site;
        final int siteIndex;
        final UUID extractorId;
        final ExtractionObjectiveConfig.TierEntry tier;
        final double floorFactor;
        final List<UUID> live = new ArrayList<>();
        private final int[] timers = new int[6];

        SiteWaveState(ExtractionSite site, int siteIndex, UUID extractorId, ExtractionObjectiveConfig.TierEntry tier, double floorFactor) {
            this.site = site;
            this.siteIndex = siteIndex;
            this.extractorId = extractorId;
            this.tier = tier;
            this.floorFactor = floorFactor;
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

        boolean underCap(VirtualWorld world, int cap) {
            if (cap <= 0 || this.live.size() < cap) {
                return true;
            }
            this.live.removeIf(id -> {
                Entity entity = world.getEntity(id);
                return entity == null || !entity.isAlive();
            });
            return this.live.size() < cap;
        }
    }
}
