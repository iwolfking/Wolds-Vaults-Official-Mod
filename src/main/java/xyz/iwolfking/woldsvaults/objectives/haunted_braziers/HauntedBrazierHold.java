package xyz.iwolfking.woldsvaults.objectives.haunted_braziers;

import iskallia.vault.block.MonolithBlock;
import iskallia.vault.block.MonolithBlock.State;
import iskallia.vault.block.entity.MonolithTileEntity;
import iskallia.vault.config.MonolithConfig;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultLevel;
import iskallia.vault.core.vault.modifier.modifier.DecoratorCascadeModifier;
import iskallia.vault.core.vault.objective.MonolithObjective;
import iskallia.vault.core.vault.objective.brazier.BrazierHeatTier;
import iskallia.vault.core.vault.objective.brazier.BrazierWaves;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.init.ModNetwork;
import iskallia.vault.network.message.ClientboundBrazierTickerMessage;
import iskallia.vault.network.message.MonolithIgniteMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.PacketDistributor;
import xyz.iwolfking.woldsvaults.config.HauntedBraziersConfig;
import xyz.iwolfking.woldsvaults.datagen.ModEntityTagProvider;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

public final class HauntedBrazierHold {
    private static final int SECOND_WAVE = 2;
    private static final int SYNC_INTERVAL_TICKS = 5;
    private static final int NEVER_ENTERED_COLD_TICKS = 100;
    private static final double RING_FLOOR_BELOW = 1.0D;
    private static final long MODIFIER_SEED_SALT = 90039738L;
    private static final long WAVE_SEED_SALT = 90039739L;
    private static final double LEFTOVER_WAVE_RADIUS = 32.0D;
    private static final String HAUNTED_BRAZIER_TAG = "woldsvaults:haunted_brazier_mob";

    @Nullable
    private static volatile List<EntityType<?>> cachedGhostPool;
    private static final Map<BlockPos, RingBounds> RING_BOUNDS_CACHE = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<BlockPos, HoldState> HOLD_STATES = Collections.synchronizedMap(new WeakHashMap<>());

    private HauntedBrazierHold() {
    }

    public static void start(VirtualWorld world, Vault vault, MonolithObjective objective, MonolithTileEntity tile, BlockPos pos) {
        resetHoldState(pos);
        HauntedBraziersConfig config = ModConfigs.HAUNTED_BRAZIERS;
        MonolithConfig.LevelEntry levelEntry = config.forLevel(levelOf(vault));
        tile.startHold(levelEntry.holdSecondsAt(objective.getHeat()));
        int fullWave = levelEntry.mobsPerWave(objective.getHeat(), MonolithObjective.runnerCount(vault));
        spawnGhostWave(world, vault, objective, pos, Math.max(0, fullWave - leftoverWaveMobs(world, pos)), config);
        tile.markWaveSpawned();
        world.playSound(null, pos, SoundEvents.BLAZE_SHOOT, SoundSource.BLOCKS, 1.0F, 0.7F);
        world.playSound(null, pos, SoundEvents.RAID_HORN, SoundSource.HOSTILE, 1.2F, 1.4F);
    }

    public static void tick(VirtualWorld world, Vault vault, MonolithObjective objective, MonolithTileEntity tile, BlockPos pos) {
        HauntedBraziersConfig config = ModConfigs.HAUNTED_BRAZIERS;
        boolean playerInside = isPlayerInside(world, pos, config);
        boolean mobInside = tile.getHoldingTicks() % SYNC_INTERVAL_TICKS == 0
                ? isHauntedMobInside(world, pos, config)
                : tile.isContested();

        HoldState state = getHoldState(pos);
        state.playerInsideTicks = playerInside ? state.playerInsideTicks + 1 : 0;
        state.mobInsideTicks = mobInside ? state.mobInsideTicks + 1 : 0;

        boolean drainActive = mobInside && state.mobInsideTicks >= config.getGhostDrainGraceTicks();
        double drainRate = config.getGhostDrainPerSecond();
        double progressPerSecond;
        if (playerInside) {
            progressPerSecond = drainActive ? -drainRate : 1.0D;
        } else {
            double extraGhostDrain = drainActive ? drainRate * config.getGhostOutsideDrainMultiplier() : 0.0D;
            progressPerSecond = -config.getColdDrainSpeed() - extraGhostDrain;
        }

        tile.advanceHold(progressPerSecond, playerInside, mobInside, tile.getHoldingTicks() % SYNC_INTERVAL_TICKS == 0);

        double holdSeconds = tile.getHoldSeconds();
        if (tile.getWavesSpawned() < SECOND_WAVE && tile.getProgressSeconds() >= holdSeconds * config.getSecondWaveAt()) {
            spawnWave(world, vault, objective, tile, pos, config);
        }

        if (tile.getProgressSeconds() >= holdSeconds) {
            light(world, vault, objective, tile, pos, config);
        } else {
            boolean gaveUp = tile.wasEverInside() || tile.getHoldingTicks() > NEVER_ENTERED_COLD_TICKS;
            if (!playerInside && tile.getProgressSeconds() <= 0.0D && gaveUp) {
                goCold(world, vault, tile, pos);
            }
        }
    }

    private static void spawnWave(VirtualWorld world, Vault vault, MonolithObjective objective, MonolithTileEntity tile, BlockPos pos, HauntedBraziersConfig config) {
        MonolithConfig.LevelEntry levelEntry = config.forLevel(levelOf(vault));
        int count = levelEntry.mobsPerWave(objective.getHeat(), MonolithObjective.runnerCount(vault));
        spawnGhostWave(world, vault, objective, pos, count, config);
        tile.markWaveSpawned();
    }

    private static void spawnGhostWave(VirtualWorld world, Vault vault, MonolithObjective objective, BlockPos pos, int count, HauntedBraziersConfig config) {
        if (count <= 0) {
            return;
        }

        List<EntityType<?>> candidates = getGhostPool();
        if (candidates.isEmpty()) {
            BrazierWaves.spawnWave(world, vault, pos, count);
            return;
        }

        int heat = objective.getHeat();
        BrazierHeatTier tier = objective.getHeatTier();
        double minDist = config.getWaveSpawnMinDistance();
        double maxDist = config.getWaveSpawnMaxDistance();
        ChunkRandom random = ChunkRandom.any();
        random.setBlockSeed(vault.get(Vault.SEED), pos, WAVE_SEED_SALT + world.getGameTime());

        for (int i = 0; i < count; i++) {
            EntityType<?> type = pickGhostForHeat(candidates, heat, tier, config, random);
            Entity entity = type.create(world);
            if (!(entity instanceof Mob mob)) {
                continue;
            }

            double angle = random.nextDouble() * Math.PI * 2.0D;
            double dist = minDist + random.nextDouble() * Math.max(0.5D, maxDist - minDist);
            double spawnX = pos.getX() + 0.5D + Math.cos(angle) * dist;
            double spawnZ = pos.getZ() + 0.5D + Math.sin(angle) * dist;
            double spawnY = pos.getY();

            mob.moveTo(spawnX, spawnY, spawnZ, random.nextFloat() * 360.0F, 0.0F);
            mob.getTags().add(HAUNTED_BRAZIER_TAG);
            mob.finalizeSpawn(world, world.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.EVENT, null, null);
            MonolithObjective.applyHeatPrice(vault, mob);
            world.addFreshEntity(mob);
        }
    }

    private static EntityType<?> pickGhostForHeat(List<EntityType<?>> candidates, int heat, BrazierHeatTier tier, HauntedBraziersConfig config, ChunkRandom random) {
        if (candidates.size() == 1) {
            return candidates.get(0);
        }

        double tierBias = switch (tier) {
            case WARM -> 0.0D;
            case HOT -> 0.35D;
            case BLAZING -> 0.75D;
        };
        double heatBonus = Math.min(0.5D, Math.max(0, heat) * config.getHeatTierBiasPerHeat());
        double bias = Math.min(0.9D, tierBias + heatBonus);

        if (bias > 0.0D && random.nextDouble() < bias) {
            int start = candidates.size() / 2;
            int index = start + random.nextInt(Math.max(1, candidates.size() - start));
            return candidates.get(index);
        }
        return candidates.get(random.nextInt(candidates.size()));
    }

    private static List<EntityType<?>> getGhostPool() {
        List<EntityType<?>> current = cachedGhostPool;
        if (current != null && !current.isEmpty()) {
            return current;
        }

        List<EntityType<?>> resolved = resolveGhostPool();
        if (!resolved.isEmpty()) {
            cachedGhostPool = resolved;
        }
        return resolved;
    }

    private static List<EntityType<?>> resolveGhostPool() {
        Optional<net.minecraft.tags.TagKey<EntityType<?>>> tagKey = Optional.ofNullable(ModEntityTagProvider.HAUNTED_BRAZIERS_MOB);
        return Registry.ENTITY_TYPE.getTag(tagKey.get())
                .map(holders -> {
                    List<EntityType<?>> list = new ArrayList<>(holders.size());
                    for (Holder<EntityType<?>> holder : holders) {
                        if (holder.isBound()) {
                            list.add(holder.value());
                        }
                    }
                    return List.copyOf(list);
                })
                .orElseGet(List::of);
    }

    private static boolean isHauntedMob(Mob mob) {
        return mob.getTags().contains(HAUNTED_BRAZIER_TAG) || BrazierWaves.isBrazierMob(mob);
    }

    private static void light(VirtualWorld world, Vault vault, MonolithObjective objective, MonolithTileEntity tile, BlockPos pos, HauntedBraziersConfig config) {
        resetHoldState(pos);
        BrazierHeatTier tierBefore = objective.getHeatTier();
        String lighterName = nearestPlayerInside(world, pos, config)
                .map(player -> player.getName().getString())
                .orElse("Someone");
        world.setBlock(pos, world.getBlockState(pos).setValue(MonolithBlock.STATE, State.LIT), 3);
        tile.stopHold();
        objective.addLitBrazier();
        objective.addHeat();
        tile.getBlessing().ifPresent(blessing -> blessing.modifier().ifPresent(modifier -> {
            ChunkRandom random = ChunkRandom.any();
            random.setBlockSeed(vault.get(Vault.SEED), pos, MODIFIER_SEED_SALT);
            vault.get(Vault.MODIFIERS).addModifier(modifier, blessing.stacks(), true, random,
                    context -> context.setGroup(MonolithObjective.BRAZIER_MODIFIER_GROUP));
            if (modifier instanceof DecoratorCascadeModifier chestCascade) {
                chestCascade.cascadeUnopenedChestsAround(vault, world, blessing.stacks());
            }
        }));
        ModNetwork.send(new MonolithIgniteMessage(pos), PacketDistributor.TRACKING_CHUNK.with(() -> world.getChunkAt(pos)));
        world.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        BrazierHeatTier tierAfter = objective.getHeatTier();
        ClientboundBrazierTickerMessage ticker = tile.getBlessing()
                .map(blessing -> ClientboundBrazierTickerMessage.lit(
                        lighterName,
                        blessing,
                        tierAfter != tierBefore ? Optional.of(tierAfter) : Optional.empty(),
                        objective.get(MonolithObjective.COUNT).equals(objective.get(MonolithObjective.TARGET))
                ))
                .orElseGet(ClientboundBrazierTickerMessage::wentCold);
        sendToVault(vault, ticker, true);
    }

    private static void goCold(VirtualWorld world, Vault vault, MonolithTileEntity tile, BlockPos pos) {
        resetHoldState(pos);
        tile.stopHold();
        world.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 0.8F);
        sendToVault(vault, ClientboundBrazierTickerMessage.wentCold(), false);
    }

    private static void sendToVault(Vault vault, ClientboundBrazierTickerMessage message, boolean bell) {
        for (Listener listener : vault.get(Vault.LISTENERS).getAll()) {
            listener.getPlayer().ifPresent(player -> {
                ModNetwork.sendTo(message, player);
                if (bell) {
                    player.getLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.NOTE_BLOCK_BELL, SoundSource.PLAYERS, 0.9F, 1.2F);
                }
            });
        }
    }

    private static boolean isPlayerInside(VirtualWorld world, BlockPos pos, MonolithConfig config) {
        double radius = config.getRingRadius();
        double radiusSq = radius * radius;
        double minY = pos.getY() - RING_FLOOR_BELOW;
        double maxY = pos.getY() + config.getRingHeight();
        double centerX = pos.getX() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;

        for (ServerPlayer player : world.players()) {
            if (!player.isAlive() || player.isSpectator()) {
                continue;
            }
            double y = player.getY();
            if (y < minY || y > maxY) {
                continue;
            }
            double dx = player.getX() - centerX;
            double dz = player.getZ() - centerZ;
            if (dx * dx + dz * dz <= radiusSq) {
                return true;
            }
        }
        return false;
    }

    private static Optional<ServerPlayer> nearestPlayerInside(VirtualWorld world, BlockPos pos, MonolithConfig config) {
        double radius = config.getRingRadius();
        double radiusSq = radius * radius;
        double minY = pos.getY() - RING_FLOOR_BELOW;
        double maxY = pos.getY() + config.getRingHeight();
        double centerX = pos.getX() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;

        ServerPlayer nearest = null;
        double bestDistSq = Double.MAX_VALUE;

        for (ServerPlayer player : world.players()) {
            if (!player.isAlive() || player.isSpectator()) {
                continue;
            }
            double y = player.getY();
            if (y < minY || y > maxY) {
                continue;
            }
            double dx = player.getX() - centerX;
            double dz = player.getZ() - centerZ;
            double distSq = dx * dx + dz * dz;
            if (distSq <= radiusSq && distSq < bestDistSq) {
                bestDistSq = distSq;
                nearest = player;
            }
        }
        return Optional.ofNullable(nearest);
    }

    private static int leftoverWaveMobs(VirtualWorld world, BlockPos pos) {
        return world.getEntitiesOfClass(Mob.class, new AABB(pos).inflate(LEFTOVER_WAVE_RADIUS),
                mob -> mob.isAlive() && isHauntedMob(mob)).size();
    }

    private static boolean isHauntedMobInside(VirtualWorld world, BlockPos pos, MonolithConfig config) {
        double radius = config.getRingRadius();
        double height = config.getRingHeight();
        RingBounds bounds = getRingBounds(pos, radius, height);
        double radiusSq = radius * radius;
        double minY = pos.getY() - RING_FLOOR_BELOW;
        double maxY = pos.getY() + height;
        double centerX = pos.getX() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;

        for (Mob mob : world.getEntitiesOfClass(Mob.class, bounds.box)) {
            if (!mob.isAlive() || !isHauntedMob(mob)) {
                continue;
            }
            double y = mob.getY();
            if (y < minY || y > maxY) {
                continue;
            }
            double dx = mob.getX() - centerX;
            double dz = mob.getZ() - centerZ;
            if (dx * dx + dz * dz <= radiusSq) {
                return true;
            }
        }
        return false;
    }

    private static RingBounds getRingBounds(BlockPos pos, double radius, double height) {
        BlockPos immutable = pos.immutable();
        RingBounds existing = RING_BOUNDS_CACHE.get(immutable);
        if (existing != null && existing.radius == radius && existing.height == height) {
            return existing;
        }
        AABB box = new AABB(immutable)
                .inflate(radius, 0.0D, radius)
                .expandTowards(0.0D, height, 0.0D)
                .expandTowards(0.0D, -RING_FLOOR_BELOW, 0.0D);
        RingBounds created = new RingBounds(radius, height, box);
        RING_BOUNDS_CACHE.put(immutable, created);
        return created;
    }

    private static HoldState getHoldState(BlockPos pos) {
        return HOLD_STATES.computeIfAbsent(pos.immutable(), k -> new HoldState());
    }

    private static void resetHoldState(BlockPos pos) {
        HoldState state = HOLD_STATES.get(pos.immutable());
        if (state != null) {
            state.reset();
        }
    }

    private static int levelOf(Vault vault) {
        return vault.get(Vault.LEVEL).getOr(VaultLevel.VALUE, 0);
    }

    private record RingBounds(double radius, double height, AABB box) {
    }

    private static final class HoldState {
        int mobInsideTicks;
        int playerInsideTicks;

        void reset() {
            this.mobInsideTicks = 0;
            this.playerInsideTicks = 0;
        }
    }
}