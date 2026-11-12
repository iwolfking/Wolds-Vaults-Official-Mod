package xyz.iwolfking.woldsvaults.api.core.layout.multicell;

import iskallia.vault.core.Version;
import iskallia.vault.core.data.key.TemplatePoolKey;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.core.util.RegionPos;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultRegistry;
import iskallia.vault.core.world.generator.layout.VaultLayout;
import iskallia.vault.core.world.processor.entity.EntityProcessor;
import iskallia.vault.core.world.processor.tile.TileProcessor;
import iskallia.vault.core.world.template.EmptyTemplate;
import iskallia.vault.core.world.template.JigsawTemplate;
import iskallia.vault.core.world.template.PlacementSettings;
import iskallia.vault.core.world.template.Template;
import iskallia.vault.core.world.template.context.TemplatePoolContext;
import iskallia.vault.core.world.template.data.TemplateEntry;
import iskallia.vault.core.world.template.data.TemplatePool;
import net.minecraft.core.BlockPos;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * Builds the template of a multi-cell room. The pick, its jigsaw children and its processors come from an RNG seeded
 * with the anchor cell only, so every cell of the room, every chunk and every rebuild after a cache eviction gets the
 * same room. Room-choice listeners are deliberately not consulted: a footprint's pool is fixed when it is registered.
 */
public final class MultiCellPlacer {
    private static final long REGION_SALT = 1234567890L;

    private MultiCellPlacer() {
    }

    public static ChunkRandom anchorRandom(Vault vault, MultiCellFootprint footprint) {
        ChunkRandom random = ChunkRandom.any();
        random.setRegionSeed(vault.get(Vault.SEED), footprint.anchorX(), footprint.anchorZ(), REGION_SALT);
        return random;
    }

    public static Template build(Vault vault, MultiCellFootprint footprint, int cellX, int cellZ, PlacementSettings settings) {
        Version version = vault.get(Vault.VERSION);
        TemplatePoolKey key = VaultRegistry.TEMPLATE_POOL.getKey(footprint.pool());
        TemplatePool pool = key == null ? null : key.get(version);
        if (pool == null) {
            WoldLogger.error("Multi-cell room pool {} is missing; {} generates empty and its cells stay bedrock.", footprint.pool(), footprint);
            return EmptyTemplate.INSTANCE;
        }
        RegionPos anchor = footprint.anchorRegion(cellX, cellZ);
        ChunkRandom random = anchorRandom(vault, footprint);
        TemplatePoolContext context = TemplatePoolContext.of(vault).setRegion(anchor).setPieceType(VaultLayout.PieceType.ROOM);
        TemplateEntry entry = pool.getRandomFlat(version, random, context).orElse(null);
        if (entry == null) {
            WoldLogger.error("Multi-cell room pool {} picked no template; {} generates empty and its cells stay bedrock.", footprint.pool(), footprint);
            return EmptyTemplate.INSTANCE;
        }
        int pivotX = footprint.span() * cellX / 2;
        int pivotZ = footprint.span() * cellZ / 2;
        BlockPos origin = footprint.origin(cellX, cellZ);
        settings.addProcessors(
                TileProcessor.mirror(footprint.mirror(), pivotX, pivotZ, true),
                TileProcessor.rotate(footprint.rotation(), pivotX, pivotZ, true),
                TileProcessor.translate(origin),
                EntityProcessor.mirror(footprint.mirror(), pivotX, pivotZ, true),
                EntityProcessor.rotate(footprint.rotation(), pivotX, pivotZ, true),
                EntityProcessor.translate(origin));
        if (footprint.configurator() != null) {
            footprint.configurator().accept(settings);
        }
        return JigsawTemplate.of(version, entry, 10, random, context);
    }
}
