package xyz.iwolfking.woldsvaults.mixins.vaulthunters.multicell;

import iskallia.vault.VaultMod;
import iskallia.vault.core.Version;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.common.TemplateGenerationEvent;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.core.util.RegionPos;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.player.Runner;
import iskallia.vault.core.world.generator.GridGenerator;
import iskallia.vault.core.world.generator.cache.ConcurrentGridCache;
import iskallia.vault.core.world.generator.layout.GridLayout;
import iskallia.vault.core.world.template.configured.ConfiguredTemplate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import xyz.iwolfking.woldsvaults.api.core.layout.multicell.MultiCellFootprint;
import xyz.iwolfking.woldsvaults.api.core.layout.multicell.MultiCellRooms;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(value = GridGenerator.class, remap = false)
public abstract class MixinGridGeneratorMultiCell {
    @Shadow private ConcurrentGridCache cache;

    /**
     * @author PoorMansPhysicist
     * @reason Multi-cell rooms: every cell of a room resolves to its anchor region and anchor seed, and the room is
     *         placed (with its template-generation events) once per chunk even when the chunk overlaps several of
     *         its cells. Cells outside multi-cell rooms generate exactly as before.
     */
    @Overwrite
    public void generate(Vault vault, ServerLevelAccessor world, ChunkPos chunkPos) {
        if (this.cache == null) {
            List<Listener> activeRunners = vault.get(Vault.LISTENERS).getAll().stream().filter(l -> l instanceof Runner && l.isOnline()).toList();
            if (!activeRunners.isEmpty()) {
                VaultMod.LOGGER.error("GridGenerator cache is null, but there are active runners. This should not happen. Report this in the discord.");
            }
            return;
        }
        GridGenerator self = (GridGenerator) (Object) this;
        int cellX = self.get(GridGenerator.CELL_X);
        int cellZ = self.get(GridGenerator.CELL_Z);
        BlockPos min = new BlockPos(chunkPos.x * 16, Integer.MIN_VALUE, chunkPos.z * 16);
        BlockPos max = new BlockPos(chunkPos.x * 16 + 15, Integer.MAX_VALUE, chunkPos.z * 16 + 15);
        Long seed = vault.get(Vault.SEED);
        GridLayout layout = self.get(GridGenerator.LAYOUT);
        Version version = vault.get(Vault.VERSION);
        Set<RegionPos> placedAnchors = null;
        for (int x = min.getX() - Math.floorMod(min.getX(), cellX); x <= max.getX(); x += cellX) {
            for (int z = min.getZ() - Math.floorMod(min.getZ(), cellZ); z <= max.getZ(); z += cellZ) {
                RegionPos region = RegionPos.ofBlockPos(new BlockPos(x, 0, z), cellX, cellZ);
                MultiCellFootprint footprint = MultiCellRooms.at(vault, region.getX(), region.getZ());
                if (footprint != null) {
                    region = footprint.anchorRegion(cellX, cellZ);
                    if (placedAnchors == null) {
                        placedAnchors = new HashSet<>();
                    }
                    if (!placedAnchors.add(region)) {
                        continue;
                    }
                }
                ChunkRandom random = ChunkRandom.any();
                if (version.isOlderThan(Version.v1_7)) {
                    random.setCarverSeed(seed, region.getX(), region.getZ());
                } else {
                    random.setRegionSeed(seed, region.getX(), region.getZ(), 1234567890L);
                }
                if (this.cache == null) {
                    return;
                }
                ConfiguredTemplate template = this.cache.getOrCreate(region, layout, vault, random);
                template = CommonEvents.TEMPLATE_GENERATION.invoke(world, template, region, chunkPos, random, TemplateGenerationEvent.Phase.PRE).getTemplate();
                template.place(world, chunkPos);
                CommonEvents.TEMPLATE_GENERATION.invoke(world, template, region, chunkPos, random, TemplateGenerationEvent.Phase.POST);
            }
        }
    }
}
