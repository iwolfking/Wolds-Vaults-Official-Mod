package xyz.iwolfking.woldsvaults.objectives.hyper;

import iskallia.vault.VaultMod;
import iskallia.vault.block.entity.BossRunePillarTileEntity;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.vault.Modifiers;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.modifier.MobAttributeModifier;
import iskallia.vault.core.vault.modifier.spi.EntityAttributeModifier;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.vault.objective.rune.RuneBossFight;
import iskallia.vault.core.vault.objective.rune.RuneBossFights;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.player.Runner;
import iskallia.vault.core.world.data.entity.PartialEntity;
import iskallia.vault.core.world.storage.IZonedWorld;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.core.world.storage.WorldZones;
import iskallia.vault.entity.boss.BossRuneModifiers;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import iskallia.vault.world.data.WorldZonesData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.iwolfking.woldsvaults.api.util.VaultModifierUtils;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;
import xyz.iwolfking.woldsvaults.entities.projectiles.MagicMissileEntity;
import xyz.iwolfking.woldsvaults.init.ModEffects;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.milestones.trials.GreedTrialHyper;
import xyz.iwolfking.woldsvaults.milestones.trials.TrialMastery;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.BossRunePillarAccessor;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.BossRunePillarConfigAccessor;
import xyz.iwolfking.woldsvaults.modifiers.vault.map.modifiers.MobAttributeModifierSettable;
import xyz.iwolfking.woldsvaults.network.message.MagicMissileWarningMessage;
import xyz.iwolfking.woldsvaults.modifiers.vault.map.modifiers.lib.EntityAttributeModifierSettable;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.objectives.BrutalBossesObjective;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultObjective;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultObjective.Phase;
import xyz.iwolfking.woldsvaults.objectives.lib.ObjectiveManager;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/** Drives the hyperboss cycle: arms the pillar, runs the brutal waves, hands finished fights on. */
public class HyperBossManager extends ObjectiveManager<HyperVaultObjective> {
    /** Arena adds: the curated spawnable subset of the config's tank and assassin entity groups. */
    private static final ResourceLocation[] TANK_ADDS = {
            VaultMod.id("shiver"),
            VaultMod.id("deathcap"),
            VaultMod.id("blood_tank"),
            VaultMod.id("overgrown_tank"),
            VaultMod.id("pirate_guardian_tank"),
            VaultMod.id("craftenstein"),
            VaultMod.id("yeti"),
            VaultMod.id("deep_dark_horror"),
            ResourceLocation.parse("minecraft:piglin_brute"),
            WoldsVaults.id("haturkin"),
    };
    private static final ResourceLocation[] ASSASSIN_ADDS = {
            VaultMod.id("vault_spider"),
            VaultMod.id("t3_skeleton"),
            VaultMod.id("t3_stray"),
            VaultMod.id("t3_wither_skeleton"),
            VaultMod.id("t3_creeper"),
            VaultMod.id("t3_enderman"),
            VaultMod.id("blood_slime"),
            VaultMod.id("vault_wraith_white"),
            VaultMod.id("vault_wraith_yellow"),
            VaultMod.id("grimwick"),
            VaultMod.id("winter_wolf"),
            WoldsVaults.id("black_ghost"),
            WoldsVaults.id("blue_ghost"),
    };

    private static final UUID HYPER_DAMAGE_UUID =
            UUID.nameUUIDFromBytes("woldsvaults:hyper_damage_escalation".getBytes(StandardCharsets.UTF_8));
    private static final UUID MULTIPLAYER_HEALTH_UUID =
            UUID.nameUUIDFromBytes("woldsvaults:hyper_multiplayer_health".getBytes(StandardCharsets.UTF_8));
    private static final UUID HYPER_FOLLOW_RANGE_UUID =
            UUID.nameUUIDFromBytes("woldsvaults:hyper_follow_range".getBytes(StandardCharsets.UTF_8));
    /** Added to the boss's FOLLOW_RANGE (base 18) so the whole 47-block arena is in acquisition range. */
    private static final double FOLLOW_RANGE_BONUS = 46.0D;
    /** The health_attribute trait's baseValue in {@code vault_boss.json}. */
    private static final double INNATE_HEALTH_BONUS = 0.5;
    private static final ResourceLocation MAX_HEALTH_ID = ResourceLocation.parse("generic.max_health");
    /** The four arena gates of the BOSS_1 room style, relative to the pillar (RuneBossAnimation). */
    private static final BlockPos[] GATE_OFFSETS = {
            new BlockPos(23, 4, 0), new BlockPos(-23, 4, 0),
            new BlockPos(0, 4, 23), new BlockPos(0, 4, -23)};

    /** How long the arena must stay empty of living fighters before the fight counts as wiped. */
    private static final int WIPE_GRACE_TICKS = 60;
    /** Resistance amplifier the boss holds while reinforcements live, unless a trial row lowers it. */
    private static final int MINION_RESISTANCE_AMPLIFIER = 2;

    private final HyperEscalationManager escalation;
    /** The wave bosses believed alive, pruned every tick; rebuilt from the world after a reload. */
    private final List<UUID> waveBosses = new ArrayList<>();
    private boolean waveRosterRebuilt;
    private int addTimer = HyperVaultObjective.cfg().getFightAddPeriodTicks();
    private int wipeGraceTicks = WIPE_GRACE_TICKS;
    private int missileCooldownTicks = HyperVaultObjective.cfg().getMagicMissileCooldownTicks();
    /** -1 while idle, otherwise the remaining charge-up ticks of the telegraphed volley. */
    private int missileChargeTicks = -1;
    private String lastRoster = "";

    public HyperBossManager(Vault vault, VirtualWorld world, HyperVaultObjective objective, HyperEscalationManager escalation) {
        super(vault, world, objective);
        this.escalation = escalation;
    }

    /** Escalates the pillar's stats and starts a fresh fight; the protection zone is ensured first. */
    public void armAndStartFight(BlockPos pillarPos) {
        RuneBossFights fights = objective.get(HyperVaultObjective.FIGHTS);
        if (fights.hasFightAt(pillarPos)) {
            WoldsVaults.LOGGER.warn("Podium at {} already has a scheduled/active fight; not re-arming.", pillarPos);
            return;
        }
        BlockEntity be = world.getBlockEntity(pillarPos);
        if (!(be instanceof BossRunePillarTileEntity pillar)) {
            WoldsVaults.LOGGER.error("Hyper podium at {} has no BossRunePillarTileEntity — cannot start the fight (staying armed).", pillarPos);
            return;
        }
        rerollBoss(pillar);
        objective.set(HyperVaultObjective.PILLAR_POS, pillarPos);
        objective.set(HyperVaultObjective.PILLAR_NBT, pillar.saveWithoutMetadata());
        ensureProtectionZone(pillar, pillarPos);
        snapshotOrRepairGates(pillarPos);

        int cycle = objective.getOr(HyperVaultObjective.CYCLE, 0);
        double escalation = GreedTrialHyper.bossStrength(vault, HyperVaultObjective.cfg().getBossHealthPercent())
                * Math.pow(GreedTrialHyper.cycleScaling(vault, HyperVaultObjective.cfg().getHyperStatFactor()), cycle)
                + GreedTrialHyper.statIncrement(vault, HyperVaultObjective.cfg().getBossStatIncrement()) * cycle;
        double healthFactor = vaultHealthFactor();
        double healthPercent = (1.0 + INNATE_HEALTH_BONUS + escalation) * healthFactor
                - 1.0 - INNATE_HEALTH_BONUS;
        if (healthFactor > 1.0) {
            WoldsVaults.LOGGER.info("Hyperboss health inherits the vault's mob modifiers: x{}.",
                    Math.round(healthFactor * 100.0) / 100.0);
        }
        BossRuneModifiers armed = new BossRuneModifiers(healthPercent, 0.0,
                HyperVaultObjective.cfg().getBossAbilityHaste());
        pillar.getModifiers().copyFrom(armed);
        pillar.setRuneCount(Math.min(HyperVaultObjective.cfg().getBaseRuneTier() + cycle, HyperVaultObjective.cfg().getRuneTierCap()));
        pillar.getModifiers().setReviveAbility(null);

        objective.set(HyperVaultObjective.SCORE, 0);
        fights.add(pillar.createFight());
        objective.set(HyperVaultObjective.PHASE, Phase.FIGHT);
        objective.set(HyperVaultObjective.WAVE_TICK, HyperVaultObjective.cfg().getWavePeriodTicks());
        this.addTimer = HyperVaultObjective.cfg().getFightAddPeriodTicks();
        objective.set(HyperVaultObjective.GATE_MASK, 0);
        TrialMastery.onBossArmed(vault, objective);
        HyperVaultObjective.broadcast(vault, "The Hyperboss awakens!", ChatFormatting.DARK_RED);
    }

    /** Snapshots the blocks bordering the arena gates at the first arm, and repairs them at later ones. */
    private void snapshotOrRepairGates(BlockPos pillarPos) {
        if (!objective.has(HyperVaultObjective.GATE_NBT)) {
            snapshotGateSurrounds(pillarPos);
        } else {
            repairGateSurrounds(pillarPos);
        }
    }

    /** A box per gate: ±5 along the wall, ±2 across it, pillarY−3..+3 (the frames heal higher). */
    private void forEachGatePos(BlockPos pillarPos, Consumer<BlockPos> action) {
        for (BlockPos offset : GATE_OFFSETS) {
            boolean xWall = offset.getX() != 0;
            for (int along = -5; along <= 5; along++) {
                for (int across = -2; across <= 2; across++) {
                    for (int dy = -3; dy <= 3; dy++) {
                        int dx = offset.getX() + (xWall ? across : along);
                        int dz = offset.getZ() + (xWall ? along : across);
                        action.accept(pillarPos.offset(dx, dy, dz));
                    }
                }
            }
        }
    }

    private void snapshotGateSurrounds(BlockPos pillarPos) {
        ListTag blocks = new ListTag();
        forEachGatePos(pillarPos, pos -> {
            BlockState state = world.getBlockState(pos);
            if (state.isAir()) {
                return;
            }
            CompoundTag entry = new CompoundTag();
            entry.putIntArray("p", new int[]{
                    pos.getX() - pillarPos.getX(), pos.getY() - pillarPos.getY(), pos.getZ() - pillarPos.getZ()});
            entry.put("s", NbtUtils.writeBlockState(state));
            blocks.add(entry);
        });
        CompoundTag tag = new CompoundTag();
        tag.put("blocks", blocks);
        objective.set(HyperVaultObjective.GATE_NBT, tag);
        WoldsVaults.LOGGER.info("Snapshotted {} pristine blocks around the arena gates for per-cycle repair.", blocks.size());
    }

    private void repairGateSurrounds(BlockPos pillarPos) {
        ListTag blocks = objective.get(HyperVaultObjective.GATE_NBT).getList("blocks", Tag.TAG_COMPOUND);
        int repaired = 0;
        for (int i = 0; i < blocks.size(); i++) {
            CompoundTag entry = blocks.getCompound(i);
            int[] p = entry.getIntArray("p");
            if (p.length != 3) {
                continue;
            }
            BlockPos pos = pillarPos.offset(p[0], p[1], p[2]);
            if (!world.getBlockState(pos).isAir()) {
                continue;
            }
            BlockState state = NbtUtils.readBlockState(entry.getCompound("s"));
            if (state.isAir()) {
                continue;
            }
            IZonedWorld.runWithBypass(world, true, () -> world.setBlock(pos, state, 3));
            repaired++;
        }
        if (repaired > 0) {
            WoldsVaults.LOGGER.info("Repaired {} destroyed blocks around the arena gates.", repaired);
        }
    }

    /** (Re)creates the room's no-modify zone; the escalation manager removes it when the boss dies. */
    private void ensureProtectionZone(BossRunePillarTileEntity pillar, BlockPos pillarPos) {
        BossRunePillarAccessor access = (BossRunePillarAccessor) pillar;
        WorldZones zones = WorldZonesData.get(world.getServer()).getOrCreate(world.dimension());
        int zoneId = access.getZoneId();
        if (zoneId <= 0 || zones.get(zoneId).isEmpty()) {
            access.setZoneId(0);
            pillar.onLoad();
            zoneId = access.getZoneId();
            WoldsVaults.LOGGER.info("Recreated the boss room protection zone ({}) for this fight.", zoneId);
        }
        if (zoneId <= 0) {
            WoldsVaults.LOGGER.warn("Hyper fight at {} has no protection zone: the pillar config defines no zone box.", pillarPos);
        }
        objective.set(HyperVaultObjective.ZONE_ID, zoneId);
    }

    /** Drives the FIGHT phase. No pending fight counts as a kill. */
    @Override
    public void tick() {
        if (objective.getOr(HyperVaultObjective.PHASE, Phase.ROLLING) != Phase.FIGHT) {
            return;
        }
        RuneBossFights fights = objective.get(HyperVaultObjective.FIGHTS);
        if (!fights.hasPendingFight()) {
            xyz.iwolfking.woldsvaults.events.ExecutionStrikeAudit.flush(vault, "hyperboss killed");
            escalation.onBossKilled();
            return;
        }

        if (checkFightWipe(fights)) {
            return;
        }

        tickWaveTimer();
        tickHealthGates();
        tickFightAdds();
        tickBossResistance();
        tickMagicMissile(fights);
    }

    /**
     * While any brutal wave boss lives, the boss holds Resistance III, refreshed once a second. Only
     * wave brutals count: the tank and assassin arena adds, and the boss's own trait summons, leave
     * it undefended, so a fight with no waves configured never grants it at all.
     *
     * <p>The same pass re-asserts the boss's red glow. The glowing flag lives in one entity's synched
     * data, and the arena has been seen to build two boss instances sharing a uuid, of which the
     * entity manager keeps only the first; marking the instance the spawn event handed us can
     * therefore mark a copy no client ever sees. The boss resolved here by uuid is always the live one.
     */
    private void tickBossResistance() {
        if (world.getTickCount() % 20 != 0) {
            return;
        }
        UUID bossId = objective.getOr(HyperVaultObjective.BOSS_ID, null);
        if (bossId == null || !(world.getEntity(bossId) instanceof LivingEntity boss) || !boss.isAlive()) {
            return;
        }
        if (!HyperBossGlow.isMarked(boss)) {
            HyperBossGlow.mark(boss);
        }
        for (Entity entity : world.getAllEntities()) {
            if (entity instanceof LivingEntity living && living.isAlive()
                    && entity.getTags().contains(HyperVaultObjective.BRUTAL_WAVE_TAG)) {
                boss.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 25,
                        MINION_RESISTANCE_AMPLIFIER, true, false));
                return;
            }
        }
    }

    /** The Magic Missile loop: a cooldown, a telegraphed charge, then a volley at random arena players. */
    private void tickMagicMissile(RuneBossFights fights) {
        UUID bossId = objective.getOr(HyperVaultObjective.BOSS_ID, null);
        if (bossId == null || !(world.getEntity(bossId) instanceof LivingEntity boss) || !boss.isAlive()) {
            if (this.missileChargeTicks >= 0) {
                sendMissileWarning(fights, -1);
            }
            this.missileChargeTicks = -1;
            this.missileCooldownTicks = HyperVaultObjective.cfg().getMagicMissileCooldownTicks();
            return;
        }
        if (this.missileChargeTicks < 0) {
            if (--this.missileCooldownTicks > 0) {
                return;
            }
            this.missileCooldownTicks = HyperVaultObjective.cfg().getMagicMissileCooldownTicks();
            this.missileChargeTicks = HyperVaultObjective.cfg().getMagicMissileChargeTicks();
            world.playSound(null, boss.getX(), boss.getY(), boss.getZ(),
                    SoundEvents.EVOKER_PREPARE_ATTACK, SoundSource.HOSTILE, 1.5F, 0.8F);
            for (ServerPlayer player : livingFighters(fights)) {
                player.displayClientMessage(new TextComponent("The Hyperboss charges Magic Missile!")
                        .withStyle(ChatFormatting.AQUA), true);
            }
            sendMissileWarning(fights, this.missileChargeTicks);
            return;
        }
        if (this.missileChargeTicks > 0) {
            this.missileChargeTicks--;
            sendMissileWarning(fights, this.missileChargeTicks);
            spawnMissileChargeParticles(boss);
            return;
        }
        this.missileChargeTicks = -1;
        sendMissileWarning(fights, -1);
        launchMissileVolley(boss, fights);
    }

    /** Streams the charge countdown to every living arena fighter; a negative value clears the display. */
    private void sendMissileWarning(RuneBossFights fights, int remainingTicks) {
        int window = remainingTicks < 0 ? 0 : Math.max(1, HyperVaultObjective.cfg().getMagicMissileChargeTicks());
        MagicMissileWarningMessage message = new MagicMissileWarningMessage(Math.max(0, remainingTicks), window);
        for (ServerPlayer player : livingFighters(fights)) {
            ModNetwork.sendToClient(message, player);
        }
    }

    private void spawnMissileChargeParticles(LivingEntity boss) {
        int total = Math.max(1, HyperVaultObjective.cfg().getMagicMissileChargeTicks());
        double radius = 0.8D + 1.8D * this.missileChargeTicks / (double) total;
        double centerY = boss.getY() + boss.getBbHeight() * 0.6D;
        for (int i = 0; i < 10; i++) {
            double angle = world.getTickCount() * 0.35D + i * (Math.PI * 2.0D / 10.0D);
            double x = boss.getX() + radius * Math.cos(angle);
            double z = boss.getZ() + radius * Math.sin(angle);
            world.sendParticles(new DustParticleOptions(MagicMissileEntity.PARTICLE_COLOR, 1.6F), x, centerY, z, 1, 0.0D, 0.03D, 0.0D, 0.0D);
        }
        if (world.getTickCount() % 10 == 0) {
            world.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, boss.getX(), centerY, boss.getZ(), 4, 0.3D, 0.3D, 0.3D, 0.02D);
        }
    }

    /** Missiles form an overhead fan, hover for {@code magicMissileHoverTicks}, then fly at their targets. */
    private void launchMissileVolley(LivingEntity boss, RuneBossFights fights) {
        List<ServerPlayer> targets = livingFighters(fights);
        if (targets.isEmpty()) {
            WoldsVaults.LOGGER.info("Magic Missile volley fizzled — no living arena targets.");
            return;
        }
        RandomSource random = JavaRandom.ofNanoTime();
        int count = HyperVaultObjective.cfg().getMagicMissileCount();
        AttributeInstance attack = boss.getAttribute(Attributes.ATTACK_DAMAGE);
        double attackDamage = attack == null ? 6.0D : attack.getValue();
        float damage = (float) Math.max(1.0D, attackDamage * HyperVaultObjective.cfg().getMagicMissileDamageMultiplier());
        Vec3 center = new Vec3(boss.getX(), boss.getY() + boss.getBbHeight() + 2.0D, boss.getZ());
        ServerPlayer facingTarget = targets.get(random.nextInt(targets.size()));
        Vec3 facing = new Vec3(facingTarget.getX() - boss.getX(), 0.0D, facingTarget.getZ() - boss.getZ());
        facing = facing.lengthSqr() < 1.0E-4D ? new Vec3(1.0D, 0.0D, 0.0D) : facing.normalize();
        Vec3 right = new Vec3(-facing.z, 0.0D, facing.x);
        for (int i = 0; i < count; i++) {
            ServerPlayer target = targets.get(random.nextInt(targets.size()));
            double slot = i - (count - 1) / 2.0D;
            Vec3 spawnPos = center.add(right.scale(slot * 2.0D));
            Vec3 aim = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D).subtract(spawnPos);
            Vec3 direction = aim.lengthSqr() < 1.0E-6D ? facing : aim.normalize();
            direction = direction.yRot((float) Math.toRadians(-slot * 15.0D));
            MagicMissileEntity missile = new MagicMissileEntity(world, boss, target, spawnPos, direction,
                    damage, (float) HyperVaultObjective.cfg().getMagicMissileAoeRadius(),
                    (float) HyperVaultObjective.cfg().getMagicMissileSpeed(),
                    HyperVaultObjective.cfg().getMagicMissileTurnDegrees(),
                    HyperVaultObjective.cfg().getMagicMissileLifetimeTicks(),
                    HyperVaultObjective.cfg().getMagicMissileHoverTicks());
            world.addFreshEntity(missile);
        }
        world.playSound(null, boss.getX(), boss.getY(), boss.getZ(), SoundEvents.SHULKER_SHOOT, SoundSource.HOSTILE, 1.4F, 0.9F);
        WoldsVaults.LOGGER.info("Hyperboss fired {} Magic Missiles ({} damage each).", count, Math.round(damage));
    }

    private List<ServerPlayer> livingFighters(RuneBossFights fights) {
        List<ServerPlayer> fighters = new ArrayList<>();
        RuneBossFight fight = activeFight(fights);
        if (fight == null) {
            return fighters;
        }
        for (UUID uuid : fight.getPlayers()) {
            ServerPlayer player = world.getServer().getPlayerList().getPlayer(uuid);
            if (player != null && player.level == world && player.isAlive() && !player.isSpectator()) {
                fighters.add(player);
            }
        }
        return fighters;
    }

    /** Winds a fight with no living fighter left back to the armed pillar: same cycle, no rewards. */
    private boolean checkFightWipe(RuneBossFights fights) {
        RuneBossFight fight = activeFight(fights);
        UUID bossId = objective.getOr(HyperVaultObjective.BOSS_ID, null);
        Entity boss = bossId == null ? null : world.getEntity(bossId);
        if (fight == null || !(boss instanceof LivingEntity living) || !living.isAlive()) {
            this.wipeGraceTicks = WIPE_GRACE_TICKS;
            return false;
        }
        logRosterChanges(fight);
        if (hasLivingFighter(fight)) {
            this.wipeGraceTicks = WIPE_GRACE_TICKS;
            return false;
        }
        if (--this.wipeGraceTicks > 0) {
            return false;
        }
        this.wipeGraceTicks = WIPE_GRACE_TICKS;
        WoldsVaults.LOGGER.info(
                "Hyperboss fight wiped: no living fighter left in the arena for {} ticks. Discarding the boss and re-arming the pillar (cycle unchanged).",
                WIPE_GRACE_TICKS);
        boss.discard();
        escalation.onFightWiped();
        return true;
    }

    private RuneBossFight activeFight(RuneBossFights fights) {
        BlockPos pillarPos = objective.getOr(HyperVaultObjective.PILLAR_POS, null);
        if (pillarPos == null) {
            return null;
        }
        for (RuneBossFight fight : fights.getFights()) {
            if (!fight.isCompleted() && pillarPos.equals(fight.getOrigin())) {
                return fight;
            }
        }
        return null;
    }

    /** True while any fight participant is alive in the arena; offline participants count as living. */
    private boolean hasLivingFighter(RuneBossFight fight) {
        for (UUID uuid : fight.getPlayers()) {
            ServerPlayer player = world.getServer().getPlayerList().getPlayer(uuid);
            if (player == null) {
                return true;
            }
            if (player.level == world && player.isAlive() && !player.isSpectator()) {
                return true;
            }
        }
        return false;
    }

    private void logRosterChanges(RuneBossFight fight) {
        List<String> names = new ArrayList<>();
        for (UUID uuid : fight.getPlayers()) {
            ServerPlayer player = world.getServer().getPlayerList().getPlayer(uuid);
            names.add(player == null ? uuid + " (offline)" : player.getGameProfile().getName());
        }
        Collections.sort(names);
        String roster = String.join(", ", names);
        if (!roster.equals(this.lastRoster)) {
            WoldsVaults.LOGGER.info("Hyperboss arena roster: [{}]", roster);
            this.lastRoster = roster;
        }
    }

    private void tickFightAdds() {
        if (--this.addTimer > 0) {
            return;
        }
        this.addTimer = HyperVaultObjective.cfg().getFightAddPeriodTicks();
        BlockPos center = objective.getOr(HyperVaultObjective.PILLAR_POS, null);
        if (center == null) {
            return;
        }
        RandomSource random = JavaRandom.ofNanoTime();
        ResourceLocation[] pool = random.nextBoolean() ? TANK_ADDS : ASSASSIN_ADDS;
        ResourceLocation id = pool[random.nextInt(pool.length)];
        EntityType<?> type = ForgeRegistries.ENTITIES.getValue(id);
        Entity created = type == null ? null : type.create(world);
        if (!(created instanceof Mob mob)) {
            WoldsVaults.LOGGER.error("Fight-add entity {} is missing or not a mob — skipping this add.", id);
            return;
        }
        double min = 6.0;
        double max = 12.0;
        for (int attempt = 0; attempt < 50; attempt++) {
            double angle = Math.PI * 2 * random.nextDouble();
            double distance = Math.sqrt(random.nextDouble() * (max * max - min * min) + min * min);
            int x = center.getX() + (int) Math.ceil(distance * Math.cos(angle));
            int z = center.getZ() + (int) Math.ceil(distance * Math.sin(angle));
            int y = center.getY() + random.nextInt(5) - 2;
            BlockPos ground = new BlockPos(x, y - 1, z);
            if (!world.getBlockState(ground).isValidSpawn(world, ground, mob.getType())) {
                continue;
            }
            AABB box = mob.getType().getAABB(x + 0.5, y, z + 0.5);
            if (!world.noCollision(box)) {
                continue;
            }
            mob.moveTo(x + 0.5, y + 0.2, z + 0.5, (float) (random.nextDouble() * 2.0 * Math.PI), 0.0F);
            mob.finalizeSpawn(world, new DifficultyInstance(Difficulty.PEACEFUL, 13000L, 0L, 0.0F), MobSpawnType.STRUCTURE, null, null);
            mob.setPersistenceRequired();
            mob.addTag(HyperVaultObjective.FIGHT_SPAWN_TAG);
            world.addWithUUID(mob);
            return;
        }
        mob.discard();
    }

    /**
     * Wave bosses still alive. The roster is in-memory, so the first call of a freshly built manager
     * rebuilds it from the arena - a vault reloaded mid-fight would otherwise spawn straight past the
     * cap.
     */
    private int livingWaveBosses() {
        if (!this.waveRosterRebuilt) {
            this.waveRosterRebuilt = true;
            this.waveBosses.clear();
            for (Entity entity : world.getAllEntities()) {
                if (entity instanceof LivingEntity living && living.isAlive()
                        && entity.getTags().contains(HyperVaultObjective.BRUTAL_WAVE_TAG)) {
                    this.waveBosses.add(entity.getUUID());
                }
            }
            if (!this.waveBosses.isEmpty()) {
                WoldsVaults.LOGGER.info("Recovered {} live brutal wave bosses after a reload; the wave cap counts them.",
                        this.waveBosses.size());
            }
        }
        this.waveBosses.removeIf(id -> !(world.getEntity(id) instanceof LivingEntity living) || !living.isAlive());
        return this.waveBosses.size();
    }

    /** The arena's ceiling on live wave brutal bosses, after any hyper trial override. */
    private int waveAliveCap() {
        return GreedTrialHyper.waveAliveCap(vault, HyperVaultObjective.cfg().getWaveAliveCap());
    }

    private boolean waveCapReached() {
        return livingWaveBosses() >= waveAliveCap();
    }

    /**
     * Holds at the wave cap without spending a tick, so the next wave is a full period away. A trial
     * row may switch the timed wave off entirely, leaving only the health gates.
     */
    private void tickWaveTimer() {
        if (!GreedTrialHyper.hasTimedWaves(vault) || waveCapReached()) {
            return;
        }
        int remaining = objective.getOr(HyperVaultObjective.WAVE_TICK, HyperVaultObjective.cfg().getWavePeriodTicks()) - 1;
        if (remaining <= 0) {
            spawnBrutalWave("timed");
            remaining = HyperVaultObjective.cfg().getWavePeriodTicks();
        }
        objective.set(HyperVaultObjective.WAVE_TICK, remaining);
    }

    /**
     * Fires a brutal wave at each configured health fraction. The boss's first live sighting also
     * escalates and scores its stats, then applies the multiplayer health scale.
     */
    private void tickHealthGates() {
        UUID bossId = objective.getOr(HyperVaultObjective.BOSS_ID, null);
        if (bossId == null) {
            return;
        }
        Entity entity = world.getEntity(bossId);
        if (!(entity instanceof LivingEntity boss) || !boss.isAlive() || boss.getMaxHealth() <= 0.0F) {
            return;
        }
        if (objective.getOr(HyperVaultObjective.SCORE, 0) == 0) {
            applyBossStats(boss);
            AttributeInstance health = boss.getAttribute(Attributes.MAX_HEALTH);
            AttributeInstance damage = boss.getAttribute(Attributes.ATTACK_DAMAGE);
            double healthMultiplier = health == null || health.getBaseValue() <= 0.0
                    ? 1.0 : boss.getMaxHealth() / health.getBaseValue();
            double damageMultiplier = damage == null || damage.getBaseValue() <= 0.0
                    ? 0.0 : damage.getValue() / damage.getBaseValue();
            long score = Math.round((healthMultiplier * HyperVaultObjective.cfg().getReferenceBossHealth()
                    + damageMultiplier * 100.0 * HyperVaultObjective.cfg().getReferenceBossDamage()) / 1000.0);
            objective.set(HyperVaultObjective.SCORE,
                    (int) Math.max(1L, Math.min(Integer.MAX_VALUE, score)));
            applyMultiplayerHealthScale(boss);
        }
        float fraction = boss.getHealth() / boss.getMaxHealth();
        float[] gates = HyperVaultObjective.cfg().getHealthGates();
        int mask = objective.getOr(HyperVaultObjective.GATE_MASK, 0);
        for (int i = 0; i < gates.length && i < 31; i++) {
            if (fraction <= gates[i] && (mask & (1 << i)) == 0) {
                if (waveCapReached()) {
                    break;
                }
                mask |= 1 << i;
                spawnBrutalWave((int) (gates[i] * 100) + "% gate");
            }
        }
        objective.set(HyperVaultObjective.GATE_MASK, mask);
    }

    /**
     * Spawns brutal bosses around the pillar; they belong to no wave, so their deaths add no modifiers.
     * The wave is trimmed so the arena never holds more than the configured cap at once, and a trial
     * row may refuse brutal reinforcements outright.
     */
    private void spawnBrutalWave(String reason) {
        if (!GreedTrialHyper.hasBrutalWaves(vault)) {
            return;
        }
        BlockPos center = objective.getOr(HyperVaultObjective.PILLAR_POS, null);
        if (center == null) {
            WoldsVaults.LOGGER.warn("Hyper brutal wave ({}) skipped: no pillar position recorded.", reason);
            return;
        }
        RandomSource random = JavaRandom.ofNanoTime();
        int rolled = HyperVaultObjective.cfg().getWaveMobMin()
                + random.nextInt(HyperVaultObjective.cfg().getWaveMobMax() - HyperVaultObjective.cfg().getWaveMobMin() + 1);
        int cap = waveAliveCap();
        int room = cap - livingWaveBosses();
        int count = Math.min(rolled, room);
        if (count <= 0) {
            return;
        }
        if (count < rolled) {
            WoldsVaults.LOGGER.info("Hyper brutal wave ({}) trimmed from {} to {}: the arena is near the {}-boss cap.",
                    reason, rolled, count, cap);
        }
        int spawned = 0;
        for (int i = 0; i < count; i++) {
            if (spawnAround(center, random)) {
                spawned++;
            }
        }
        if (spawned < count) {
            WoldsVaults.LOGGER.warn("Hyper brutal wave ({}): only {}/{} mobs found a valid spawn spot around {}.", reason, spawned, count, center);
        }
        if (spawned > 0) {
            HyperVaultObjective.broadcast(vault, "Brutal reinforcements have arrived!", ChatFormatting.RED);
        }
    }

    /** A fresh boss from the pillar's roster; assigned without {@code copy()}, which NPEs here. */
    private void rerollBoss(BossRunePillarTileEntity pillar) {
        WeightedList<PartialEntity> pool =
                ((BossRunePillarConfigAccessor) (Object) ((BossRunePillarAccessor) pillar).getConfig()).getBossPool();
        if (pool == null || pool.isEmpty()) {
            WoldsVaults.LOGGER.warn("The boss pillar has no roster to reroll from; keeping {}.",
                    pillar.getBoss() == null ? "nothing" : pillar.getBoss().getId());
            return;
        }
        pool.getRandom(JavaRandom.ofNanoTime()).ifPresent(rolled -> {
            ((BossRunePillarAccessor) pillar).setBoss(rolled);
            WoldsVaults.LOGGER.info("Hyperboss for this cycle: {}.", rolled.getId());
        });
    }

    /**
     * Applies the damage escalation and every non-max-health vault mob modifier to the live boss,
     * which is IModifierImmunity; max health was folded into its trait at arm time.
     */
    private void applyBossStats(LivingEntity boss) {
        int cycle = objective.getOr(HyperVaultObjective.CYCLE, 0);
        double damageEscalation = GreedTrialHyper.bossStrength(vault, HyperVaultObjective.cfg().getBossDamagePercent())
                * Math.pow(GreedTrialHyper.cycleScaling(vault, HyperVaultObjective.cfg().getHyperStatFactor()), cycle)
                + GreedTrialHyper.statIncrement(vault, HyperVaultObjective.cfg().getBossStatIncrement()) * cycle;
        AttributeInstance damage = boss.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null && damage.getModifier(HYPER_DAMAGE_UUID) == null) {
            damage.addPermanentModifier(new AttributeModifier(HYPER_DAMAGE_UUID,
                    "hyper_damage_escalation", damageEscalation, AttributeModifier.Operation.MULTIPLY_BASE));
        }
        AttributeInstance followRange = boss.getAttribute(Attributes.FOLLOW_RANGE);
        if (followRange != null && followRange.getModifier(HYPER_FOLLOW_RANGE_UUID) == null) {
            followRange.addPermanentModifier(new AttributeModifier(HYPER_FOLLOW_RANGE_UUID,
                    "hyper_follow_range", FOLLOW_RANGE_BONUS, AttributeModifier.Operation.ADDITION));
            WoldsVaults.LOGGER.info("Hyperboss follow range raised to {} — the arena corners are inside acquisition range now.",
                    Math.round(followRange.getValue()));
        }
        Modifiers vaultModifiers = vault.get(Vault.MODIFIERS);
        int applied = 0;
        for (Modifiers.Entry entry : vaultModifiers.getEntries()) {
            VaultModifier<?> modifier = entry.getModifier().orElse(null);
            ModifierContext context = vaultModifiers.getContext(entry);
            if (modifier instanceof MobAttributeModifier mob
                    && !targetsMaxHealth(mob.properties().getType())) {
                mob.applyToEntity(boss, context.getUUID(), context);
                applied++;
            } else if (modifier instanceof MobAttributeModifierSettable settable
                    && !targetsMaxHealth(settable.properties().getType())) {
                settable.applyToEntity(boss, context.getUUID(), context);
                applied++;
            }
        }
        double capFactor = HyperVaultObjective.speedCapFactor(vault);
        if (HyperVaultObjective.clampMovementSpeed(boss, capFactor)) {
            WoldsVaults.LOGGER.info("Hyperboss movement speed capped at +{}%.",
                    Math.round((capFactor - 1.0) * 100.0));
        }
        boss.addEffect(new MobEffectInstance(ModEffects.REAVING, Integer.MAX_VALUE, 0, true, false));
        boss.setHealth(boss.getMaxHealth());
        WoldsVaults.LOGGER.info(
                "Hyperboss stats: {} HP (vault health factor folded at arm), {} damage — {} non-health vault mob modifiers applied.",
                Math.round(boss.getMaxHealth()),
                damage == null ? "?" : Math.round(damage.getValue()), applied);
        logDamageAmplifierAudit();
    }

    /** {@code playerScaleBossHealth} per extra runner as MULTIPLY_TOTAL, once when the boss first ticks. */
    private void applyMultiplayerHealthScale(LivingEntity boss) {
        int runners = vault.get(Vault.LISTENERS).getAll(Runner.class).size();
        int extra = Math.max(0, runners - 1);
        if (extra == 0) {
            return;
        }
        AttributeInstance health = boss.getAttribute(Attributes.MAX_HEALTH);
        if (health == null || health.getModifier(MULTIPLAYER_HEALTH_UUID) != null) {
            return;
        }
        double bonus = HyperVaultObjective.cfg().getPlayerScaleBossHealth() * extra;
        health.addPermanentModifier(new AttributeModifier(MULTIPLAYER_HEALTH_UUID,
                "hyper_multiplayer_health", bonus, AttributeModifier.Operation.MULTIPLY_TOTAL));
        boss.setHealth(boss.getMaxHealth());
        WoldsVaults.LOGGER.info("Hyperboss max health x{} for {} runners (+{}% per extra player): {} HP.",
                1.0 + bonus, runners,
                Math.round(HyperVaultObjective.cfg().getPlayerScaleBossHealth() * 100.0),
                Math.round(boss.getMaxHealth()));
    }

    /** One audit line per fight: player-damage multipliers and each runner's scaling gear. */
    private void logDamageAmplifierAudit() {
        if (!WoldsVaultsConfig.COMMON.logHyperbossDamage.get()
                && !WoldsVaultsConfig.COMMON.enableDebugMode.get()) {
            return;
        }
        long frenzy = VaultModifierUtils.getCountOfModifiers(vault, ResourceLocation.parse("the_vault:frenzy"));
        long brew = VaultModifierUtils.getCountOfModifiers(vault, ResourceLocation.parse("the_vault:catastrophic_brew"));
        WoldsVaults.LOGGER.info(
                "Damage-amplifier audit: {} Frenzy (+200% each) + {} Catastrophic Brew (+100% each) stacks -> all player damage x{} (additive per modifier in hyper).",
                frenzy, brew, String.format("%.0f", (1.0 + 2.0 * frenzy) * (1.0 + 1.0 * brew)));
        for (Listener listener : vault.get(Vault.LISTENERS).getAll()) {
            listener.getPlayer().ifPresent(player -> {
                var snapshot = AttributeSnapshotHelper.getInstance().getSnapshot(player);
                var merger = VaultGearAttributeTypeMerger.floatSum();
                WoldsVaults.LOGGER.info(
                        "  {} gear: reaving={} execution={} apScaling={} thornsScaling={}",
                        player.getGameProfile().getName(),
                        snapshot.getAttributeValue(ModGearAttributes.REAVING_DAMAGE, merger),
                        snapshot.getAttributeValue(ModGearAttributes.EXECUTION_DAMAGE, merger),
                        snapshot.getAttributeValue(ModGearAttributes.AP_SCALING_DAMAGE, merger),
                        snapshot.getAttributeValue(ModGearAttributes.THORNS_SCALING_DAMAGE, merger));
            });
        }
    }

    /** The addon's settable modifiers carry their own ModifierType enum, hence the overload pair. */
    private static boolean targetsMaxHealth(EntityAttributeModifier.ModifierType type) {
        return type != null && type.getAttributeResourceLocations().contains(MAX_HEALTH_ID);
    }

    private static boolean targetsMaxHealth(EntityAttributeModifierSettable.ModifierType type) {
        return type != null && type.getAttributeResourceLocations().contains(MAX_HEALTH_ID);
    }

    /** Max-health growth from the vault's modifiers: (1 + additive sum) x (product of multiplicatives). */
    private double vaultHealthFactor() {
        double additive = 0.0;
        double multiplicative = 1.0;
        for (Modifiers.Entry entry : vault.get(Vault.MODIFIERS).getEntries()) {
            VaultModifier<?> modifier = entry.getModifier().orElse(null);
            AttributeModifier.Operation operation;
            double amount;
            if (modifier instanceof MobAttributeModifier mob
                    && targetsMaxHealth(mob.properties().getType())) {
                operation = mob.properties().getType().getAttributeModifierOperation();
                amount = mob.properties().getAmount();
            } else if (modifier instanceof MobAttributeModifierSettable settable
                    && targetsMaxHealth(settable.properties().getType())) {
                operation = settable.properties().getType().getAttributeModifierOperation();
                amount = settable.properties().getValue();
            } else {
                continue;
            }
            if (operation == AttributeModifier.Operation.MULTIPLY_TOTAL) {
                multiplicative *= 1.0 + amount;
            } else if (operation == AttributeModifier.Operation.MULTIPLY_BASE) {
                additive += amount;
            }
        }
        return (1.0 + additive) * multiplicative;
    }

    private boolean spawnAround(BlockPos center, RandomSource random) {
        double min = 6.0;
        double max = 12.0;
        for (int attempt = 0; attempt < 50; attempt++) {
            double angle = Math.PI * 2 * random.nextDouble();
            double distance = Math.sqrt(random.nextDouble() * (max * max - min * min) + min * min);
            int x = (int) Math.ceil(distance * Math.cos(angle));
            int z = (int) Math.ceil(distance * Math.sin(angle));
            int y = random.nextInt(5) - 2;
            LivingEntity spawned = BrutalBossesObjective.spawnMob(world, vault, center.getX() + x, center.getY() + y, center.getZ() + z, random);
            if (spawned != null) {
                spawned.addTag(HyperVaultObjective.FIGHT_SPAWN_TAG);
                spawned.addTag(HyperVaultObjective.BRUTAL_WAVE_TAG);
                this.waveBosses.add(spawned.getUUID());
                return true;
            }
        }
        return false;
    }
}
