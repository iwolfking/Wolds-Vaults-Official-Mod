package xyz.iwolfking.woldsvaults.api.core.layout.multicell;

import iskallia.vault.core.util.RegionPos;
import iskallia.vault.core.world.processor.tile.TileProcessor;
import iskallia.vault.core.world.template.PlacementSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

/**
 * One multi-cell room: a square of {@code span} x {@code span} grid cells whose minimum corner is the
 * anchor cell. Every cell inside reports ROOM and resolves to the single template built at the anchor.
 */
public final class MultiCellFootprint {
    public static final int ROOM_Y = 9;

    private final int anchorX;
    private final int anchorZ;
    private final int span;
    private final ResourceLocation pool;
    private final Rotation rotation;
    private final Mirror mirror;
    private final Consumer<PlacementSettings> configurator;
    private final String source;

    public MultiCellFootprint(int anchorX, int anchorZ, int span, ResourceLocation pool, Rotation rotation, Mirror mirror,
                              Consumer<PlacementSettings> configurator, String source) {
        this.anchorX = anchorX;
        this.anchorZ = anchorZ;
        this.span = span;
        this.pool = pool;
        this.rotation = rotation;
        this.mirror = mirror;
        this.configurator = configurator;
        this.source = source;
    }

    public int anchorX() {
        return this.anchorX;
    }

    public int anchorZ() {
        return this.anchorZ;
    }

    public int span() {
        return this.span;
    }

    public ResourceLocation pool() {
        return this.pool;
    }

    public Rotation rotation() {
        return this.rotation;
    }

    public Mirror mirror() {
        return this.mirror;
    }

    public Consumer<PlacementSettings> configurator() {
        return this.configurator;
    }

    public String source() {
        return this.source;
    }

    public boolean contains(int cellX, int cellZ) {
        return cellX >= this.anchorX && cellX < this.anchorX + this.span && cellZ >= this.anchorZ && cellZ < this.anchorZ + this.span;
    }

    public boolean overlaps(int otherAnchorX, int otherAnchorZ, int otherSpan) {
        return otherAnchorX < this.anchorX + this.span && this.anchorX < otherAnchorX + otherSpan
                && otherAnchorZ < this.anchorZ + this.span && this.anchorZ < otherAnchorZ + otherSpan;
    }

    public RegionPos anchorRegion(int cellX, int cellZ) {
        return RegionPos.of(this.anchorX, this.anchorZ, cellX, cellZ);
    }

    /** World position of the template's local origin (minimum corner, floor of the room volume). */
    public BlockPos origin(int cellX, int cellZ) {
        return new BlockPos(this.anchorX * cellX, ROOM_Y, this.anchorZ * cellZ);
    }

    /** Maps a block position in the template's unrotated local frame to its placed world position. */
    public BlockPos toWorld(BlockPos local, int cellX, int cellZ) {
        int pivotX = this.span * cellX / 2;
        int pivotZ = this.span * cellZ / 2;
        BlockPos mirrored = TileProcessor.mirror(this.mirror, pivotX, pivotZ, true).transform(local);
        BlockPos rotated = TileProcessor.rotate(this.rotation, pivotX, pivotZ, true).transform(mirrored);
        return rotated.offset(this.origin(cellX, cellZ));
    }

    /** Maps a local point to the horizontal centre of the block that contains it once placed, keeping its height. */
    public Vec3 toWorldCentre(Vec3 local, int cellX, int cellZ) {
        BlockPos block = new BlockPos(Math.floor(local.x), Math.floor(local.y), Math.floor(local.z));
        BlockPos placed = this.toWorld(block, cellX, cellZ);
        return new Vec3(placed.getX() + 0.5D, placed.getY() + (local.y - block.getY()), placed.getZ() + 0.5D);
    }

    @Override
    public String toString() {
        return this.span + "x" + this.span + " " + this.pool + " at (" + this.anchorX + ", " + this.anchorZ + ") from " + this.source;
    }
}
