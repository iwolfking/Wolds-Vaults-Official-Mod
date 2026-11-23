package xyz.iwolfking.woldsvaults.loadingzone;

import iskallia.vault.core.Version;
import iskallia.vault.core.data.key.TemplateKey;
import iskallia.vault.core.data.key.TemplatePoolKey;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.common.TemplateGenerationEvent;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.core.util.RegionPos;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultRegistry;
import iskallia.vault.core.vault.WorldManager;
import iskallia.vault.core.world.data.tile.PartialTile;
import iskallia.vault.core.world.generator.GridGenerator;
import iskallia.vault.core.world.generator.layout.VaultLayout;
import iskallia.vault.core.world.processor.ProcessorContext;
import iskallia.vault.core.world.processor.entity.EntityProcessor;
import iskallia.vault.core.world.processor.tile.TileProcessor;
import iskallia.vault.core.world.template.EmptyTemplate;
import iskallia.vault.core.world.template.JigsawTemplate;
import iskallia.vault.core.world.template.PlacementSettings;
import iskallia.vault.core.world.template.Template;
import iskallia.vault.core.world.template.configured.ChunkedTemplate;
import iskallia.vault.core.world.template.configured.ConfiguredTemplate;
import iskallia.vault.core.world.template.context.TemplatePoolContext;
import iskallia.vault.core.world.template.data.DirectTemplateEntry;
import iskallia.vault.core.world.template.data.TemplateEntry;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import xyz.iwolfking.woldsvaults.blocks.LoadingZoneArrivalBlock;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PocketTemplates {
    private static final long SALT = 0x506F636B6574L;
    private static final long IDLE_MILLIS = 30_000L;
    private static final Map<SlotKey, Built> BUILT = new ConcurrentHashMap<>();

    record SlotKey(UUID vault, int slot) {
    }

    private record Build(Template template, PlacementSettings settings, RegionPos region) {
    }

    private static final class Built {
        private final ConfiguredTemplate template;
        private final RegionPos region;
        private volatile long used = Util.getMillis();

        private Built(Build build) {
            this.template = build.template().configure(ChunkedTemplate::new, build.settings());
            this.region = build.region();
        }
    }

    private PocketTemplates() {
    }

    public static boolean exists(ResourceLocation id) {
        return VaultRegistry.TEMPLATE_POOL.getKey(id) != null || VaultRegistry.TEMPLATE.getKey(id) != null;
    }

    /**
     * Generates one chunk of pocket territory from its slot's pocket build; runs on chunk generation threads. A slot
     * with no pocket stays empty.
     */
    public static void generate(Vault vault, ServerLevelAccessor world, ChunkPos chunk) {
        UUID id = vault.get(Vault.ID);
        int slot = PocketTerritory.slotOf(chunk.getMinBlockX());
        PocketData.Pocket pocket = PocketData.lookup(id, slot);
        if (pocket == null) {
            return;
        }
        Built built = BUILT.computeIfAbsent(new SlotKey(id, slot), key -> new Built(build(vault, slot, pocket.target())));
        built.used = Util.getMillis();
        ChunkRandom random = random(vault, slot);
        ConfiguredTemplate template = CommonEvents.TEMPLATE_GENERATION.invoke(world, built.template, built.region, chunk, random, TemplateGenerationEvent.Phase.PRE).getTemplate();
        template.place(world, chunk);
        CommonEvents.TEMPLATE_GENERATION.invoke(world, template, built.region, chunk, random, TemplateGenerationEvent.Phase.POST);
    }

    /** Scans every block of a fresh copy of the slot's build. */
    static PocketData.Scan scan(Vault vault, int slot, ResourceLocation target) {
        Build build = build(vault, slot, target);
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        List<PocketData.Spot> arrivals = new ArrayList<>();
        Iterator<PartialTile> tiles = build.template().getTiles(build.settings());
        while (tiles.hasNext()) {
            PartialTile tile = tiles.next();
            if (tile == null) {
                continue;
            }
            BlockPos pos = tile.getPos();
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
            if (tile.getState().getBlock().asWhole().filter(block -> block == ModBlocks.LOADING_ZONE_ARRIVAL).isPresent()) {
                String link = tile.getEntity().asWhole().map(tag -> tag.getString("Link")).orElse("");
                Integer rotation = tile.getState().get(LoadingZoneArrivalBlock.ROTATION);
                arrivals.add(new PocketData.Spot(pos.immutable(), link, rotation == null ? 0.0F : rotation * 22.5F));
            }
        }
        if (minX > maxX) {
            return PocketData.Scan.EMPTY;
        }
        return new PocketData.Scan(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ), arrivals);
    }

    public static int builtCount() {
        return BUILT.size();
    }

    static void evictIdle() {
        long now = Util.getMillis();
        BUILT.values().removeIf(built -> now - built.used > IDLE_MILLIS);
    }

    static void forget(UUID vault) {
        BUILT.keySet().removeIf(key -> key.vault().equals(vault));
    }

    static void clear() {
        BUILT.clear();
    }

    private static Build build(Vault vault, int slot, ResourceLocation target) {
        Version version = vault.get(Vault.VERSION);
        BlockPos origin = PocketTerritory.origin(slot);
        RegionPos region = region(vault, origin);
        ChunkRandom random = random(vault, slot);
        PlacementSettings settings = new PlacementSettings(new ProcessorContext(vault, random)).setFlags(3);
        CommonEvents.TEMPLATE_CONFIGURATION.invoke(settings, vault, random, region);
        TemplatePoolContext context = TemplatePoolContext.of(vault).setRegion(region).setPieceType(VaultLayout.PieceType.ROOM);
        TemplateEntry entry = entry(target, version, random, context);
        if (entry == null) {
            return new Build(EmptyTemplate.INSTANCE, settings, region);
        }
        settings.addProcessor(TileProcessor.translate(origin));
        settings.addProcessor(EntityProcessor.translate(origin));
        return new Build(JigsawTemplate.of(version, entry, 10, random, context), settings, region);
    }

    @Nullable
    private static TemplateEntry entry(ResourceLocation target, Version version, ChunkRandom random, TemplatePoolContext context) {
        TemplatePoolKey pool = VaultRegistry.TEMPLATE_POOL.getKey(target);
        if (pool != null && pool.supports(version)) {
            return pool.get(version).getRandomFlat(version, random, context).orElse(null);
        }
        TemplateKey template = VaultRegistry.TEMPLATE.getKey(target);
        return template != null && template.supports(version) ? new DirectTemplateEntry(target) : null;
    }

    private static RegionPos region(Vault vault, BlockPos origin) {
        if (vault.get(Vault.WORLD).get(WorldManager.GENERATOR) instanceof GridGenerator grid) {
            return RegionPos.ofBlockPos(origin, grid.get(GridGenerator.CELL_X), grid.get(GridGenerator.CELL_Z));
        }
        return RegionPos.ofBlockPos(origin, 47, 47);
    }

    private static ChunkRandom random(Vault vault, int slot) {
        ChunkRandom random = ChunkRandom.any();
        random.setRegionSeed(vault.get(Vault.SEED), slot, 0, SALT);
        return random;
    }
}
