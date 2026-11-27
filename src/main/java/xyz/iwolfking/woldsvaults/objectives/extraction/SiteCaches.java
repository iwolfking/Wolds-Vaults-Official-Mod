package xyz.iwolfking.woldsvaults.objectives.extraction;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.player.Runner;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.core.world.storage.IZonedWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.blocks.ExtractionCacheBlock;
import xyz.iwolfking.woldsvaults.config.ExtractionObjectiveConfig;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * The cache chests of drilling site rooms: spawns batches on the room grid's chest spots while a site drills, keeps the
 * standing positions in {@link ExtractionSite#CACHE_CHESTS}, and removes the unmined ones when the fight ends.
 */
public final class SiteCaches {
    private final Map<Integer, Integer> timers = new HashMap<>();
    private final Runnable markDirty;

    public SiteCaches(Runnable markDirty) {
        this.markDirty = markDirty;
    }

    /** Per drilling tick: every interval, places a batch of caches on the room grid's chest spots, away from players. */
    public void tick(VirtualWorld world, Vault vault, ExtractionSite site, int index, @Nullable RoomGrid grid, ExtractionObjectiveConfig.CacheEntry cfg) {
        if (grid == null || !grid.isReady()) {
            return;
        }
        int timer = this.timers.merge(index, 1, Integer::sum);
        if (timer < cfg.intervalTicks) {
            return;
        }
        this.timers.put(index, 0);
        if (grid.chestSpotCount() == 0) {
            WoldLogger.errorOnce("extraction_cache_spots_" + index, "Extraction site {} room has no cache chest spots; no caches will spawn there.", index);
            return;
        }
        List<BlockPos> standing = new ArrayList<>();
        for (long packed : site.has(ExtractionSite.CACHE_CHESTS) ? site.get(ExtractionSite.CACHE_CHESTS) : new long[0]) {
            BlockPos pos = BlockPos.of(packed);
            if (world.getBlockState(pos).is(ModBlocks.EXTRACTION_CACHE)) {
                standing.add(pos);
            }
        }
        int room = cfg.cap - standing.size();
        if (room <= 0) {
            this.storeCaches(site, standing);
            return;
        }
        Random random = world.getRandom();
        int wanted = Math.min(room, cfg.batchMin + random.nextInt(Math.max(1, cfg.batchMax - cfg.batchMin + 1)));
        List<ServerPlayer> players = new ArrayList<>();
        for (Runner runner : vault.get(Vault.LISTENERS).getAll(Runner.class)) {
            runner.getPlayer().filter(p -> p.level == world).ifPresent(players::add);
        }
        int placed = 0;
        if (cfg.cluster) {
            BlockPos centre = grid.randomChestSpot(random,
                    candidate -> farFrom(candidate, players, cfg.playerClearance) && farFromAll(candidate, standing, cfg.clusterSpacing));
            if (centre == null) {
                WoldLogger.debug("Extraction site {} has no cache bed clear of players and standing piles; this batch waits.", index);
            } else {
                List<BlockPos> spots = grid.chestSpotsNear(centre, cfg.clusterRadius);
                Collections.shuffle(spots, random);
                for (BlockPos spot : spots) {
                    if (placed >= wanted) {
                        break;
                    }
                    if (farFromAll(spot, standing, cfg.minSpacing) && this.placeCache(world, spot, random)) {
                        standing.add(spot);
                        placed++;
                    }
                }
            }
        } else {
            for (int i = 0; i < wanted; i++) {
                for (int attempt = 0; attempt < 8; attempt++) {
                    BlockPos spot = grid.randomChestSpot(random);
                    if (spot != null && farFrom(spot, players, cfg.playerClearance) && farFromAll(spot, standing, cfg.minSpacing) && this.placeCache(world, spot, random)) {
                        standing.add(spot);
                        placed++;
                        break;
                    }
                }
            }
        }
        if (placed < wanted) {
            WoldLogger.debug("Extraction site {} placed {} of {} cache chests this batch.", index, placed, wanted);
        }
        this.storeCaches(site, standing);
    }

    /** Forgets the site's cache timer while it is not drilling. */
    public void stop(int index) {
        this.timers.remove(index);
    }

    private void storeCaches(ExtractionSite site, List<BlockPos> standing) {
        long[] packed = new long[standing.size()];
        for (int i = 0; i < packed.length; i++) {
            packed[i] = standing.get(i).asLong();
        }
        site.set(ExtractionSite.CACHE_CHESTS, packed);
        this.markDirty.run();
    }

    private static boolean farFrom(BlockPos pos, List<ServerPlayer> players, int clearance) {
        for (ServerPlayer player : players) {
            if (player.blockPosition().distSqr(pos) < (double) clearance * clearance) {
                return false;
            }
        }
        return true;
    }

    private static boolean farFromAll(BlockPos pos, List<BlockPos> others, int spacing) {
        for (BlockPos other : others) {
            if (Math.max(Math.abs(other.getX() - pos.getX()), Math.abs(other.getZ() - pos.getZ())) < spacing && Math.abs(other.getY() - pos.getY()) < spacing + 2) {
                return false;
            }
        }
        return true;
    }

    /** Places one cache chest inside the room's no-modify zone, with the world's zone checks bypassed for the call. */
    private boolean placeCache(VirtualWorld world, BlockPos pos, Random random) {
        if (!world.getBlockState(pos).isAir()) {
            return false;
        }
        BlockState chest = ModBlocks.EXTRACTION_CACHE.defaultBlockState()
                .setValue(ExtractionCacheBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random));
        IZonedWorld.runWithBypass(world, true, () -> world.setBlock(pos, chest, 3));
        boolean ok = world.getBlockState(pos).is(ModBlocks.EXTRACTION_CACHE);
        if (!ok) {
            WoldLogger.error("Cache chest placement at {} was refused by the world (zone or mixin); skipping that spot.", pos);
        }
        return ok;
    }

    /** Removes every standing cache chest of the site: unmined chests vanish when its fight ends. */
    public void clear(@Nullable VirtualWorld world, ExtractionSite site, int index) {
        if (world == null || !site.has(ExtractionSite.CACHE_CHESTS)) {
            return;
        }
        int removed = 0;
        for (long packed : site.get(ExtractionSite.CACHE_CHESTS)) {
            BlockPos pos = BlockPos.of(packed);
            if (world.isLoaded(pos) && world.getBlockState(pos).is(ModBlocks.EXTRACTION_CACHE)) {
                world.removeBlock(pos, false);
                removed++;
            }
        }
        site.set(ExtractionSite.CACHE_CHESTS, new long[0]);
        this.markDirty.run();
        if (removed > 0) {
            WoldLogger.debug("Extraction site {} fight ended; removed {} unmined cache chests.", index, removed);
        }
    }
}
