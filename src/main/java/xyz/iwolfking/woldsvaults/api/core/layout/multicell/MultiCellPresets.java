package xyz.iwolfking.woldsvaults.api.core.layout.multicell;

import iskallia.vault.core.util.RegionPos;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.world.generator.layout.VaultGridLayout;
import iskallia.vault.item.crystal.layout.preset.PoolKeyTemplatePreset;
import iskallia.vault.item.crystal.layout.preset.StructurePreset;
import iskallia.vault.item.crystal.layout.preset.TemplatePreset;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * Turns structure-preset entries that name a multi-cell pool (room inscriptions, preset layouts) into registered
 * multi-cell rooms. The preset keeps its entry at the room cell it picked; the room grows from that cell as one of
 * its corners, trying the four corners in a fixed order, so the result is identical on every server start.
 */
public final class MultiCellPresets {

    private MultiCellPresets() {
    }

    public static void apply(Vault vault, VaultGridLayout layout, StructurePreset preset, String source) {
        if (preset == null || layout == null) {
            return;
        }
        List<Map.Entry<RegionPos, TemplatePreset>> entries = new ArrayList<>(preset.getAll().entrySet());
        entries.sort(Comparator.<Map.Entry<RegionPos, TemplatePreset>>comparingInt(e -> e.getKey().getX()).thenComparingInt(e -> e.getKey().getZ()));
        for (Map.Entry<RegionPos, TemplatePreset> entry : entries) {
            if (!(entry.getValue() instanceof PoolKeyTemplatePreset poolPreset)) {
                continue;
            }
            ResourceLocation pool = poolPreset.getPool();
            int span = ModConfigs.MULTI_CELL_ROOMS.spanOfPool(pool);
            if (span <= 1) {
                continue;
            }
            RegionPos region = entry.getKey();
            if (!place(vault, layout, preset, region, span, pool, source)) {
                preset.remove(region);
                WoldLogger.error("Multi-cell room {} from {} fits nowhere around cell ({}, {}); that cell keeps an ordinary room instead.",
                        pool, source, region.getX(), region.getZ());
            }
        }
    }

    private static boolean place(Vault vault, VaultGridLayout layout, StructurePreset preset, RegionPos region, int span, ResourceLocation pool, String source) {
        int reach = span - 1;
        int[][] anchors = {{0, 0}, {-reach, 0}, {0, -reach}, {-reach, -reach}};
        for (int[] offset : anchors) {
            int anchorX = region.getX() + offset[0];
            int anchorZ = region.getZ() + offset[1];
            if (coversOtherPreset(preset, region, anchorX, anchorZ, span) || !MultiCellRooms.fits(vault, layout, anchorX, anchorZ, span)) {
                continue;
            }
            return MultiCellRooms.force(vault, layout, anchorX, anchorZ, span, pool, null, source).isPresent();
        }
        return false;
    }

    private static boolean coversOtherPreset(StructurePreset preset, RegionPos self, int anchorX, int anchorZ, int span) {
        for (RegionPos other : preset.getAll().keySet()) {
            if (other.getX() == self.getX() && other.getZ() == self.getZ()) {
                continue;
            }
            if (other.getX() >= anchorX && other.getX() < anchorX + span && other.getZ() >= anchorZ && other.getZ() < anchorZ + span) {
                return true;
            }
        }
        return false;
    }
}
