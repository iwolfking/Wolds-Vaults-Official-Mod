package xyz.iwolfking.woldsvaults.blocks.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import xyz.iwolfking.woldsvaults.blocks.ExtractorControllerBlock;
import xyz.iwolfking.woldsvaults.blocks.ExtractorStructureBlock;
import xyz.iwolfking.woldsvaults.blocks.tiles.ExtractorStructureTileEntity;

import java.util.ArrayList;
import java.util.List;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * Controller-plus-filler multiblocks. The controller sits at the bottom centre of a
 * width × height × depth box; every other cell is a filler carrying {@code facing} and an
 * {@code idx} that names its cell by its WORLD offset from the machine's minimum corner:
 * {@code idx = (y * depth + z) * width + x}. The art's blockstate files map each (facing, idx) to
 * the facing=north tile that lands in that world cell plus its rotation, so this class only has to
 * put the right numbers on the right blocks.
 */
public final class ExtractorMultiblock {
    private ExtractorMultiblock() {
    }

    public record Cell(BlockPos world, int idx) {
    }

    /** Vanilla blockstate rotation: "y": 90 turns a model clockwise seen from above, i.e. (dx, dz) → (−dz, dx). */
    static int[] rotate(Direction facing, int dx, int dz) {
        return switch (facing) {
            case EAST -> new int[]{-dz, dx};
            case SOUTH -> new int[]{-dx, -dz};
            case WEST -> new int[]{dz, -dx};
            default -> new int[]{dx, dz};
        };
    }

    /** The blockstate {@code "y"} the art files use for this facing; the renderer must turn its parts the same way. */
    public static int blockstateYaw(Direction facing) {
        return switch (facing) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        };
    }

    public static List<Cell> cells(BlockPos controller, Direction facing, ExtractorControllerBlock block) {
        int width = block.getWidth();
        int height = block.getHeight();
        int depth = block.getDepth();
        int centreX = width / 2;
        int centreZ = depth / 2;
        List<Cell> cells = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int z = 0; z < depth; z++) {
                for (int x = 0; x < width; x++) {
                    if (x == centreX && y == 0 && z == centreZ) {
                        continue;
                    }
                    int[] r = rotate(facing, x - centreX, z - centreZ);
                    int worldX = r[0] + centreX;
                    int worldZ = r[1] + centreZ;
                    cells.add(new Cell(controller.offset(r[0], y, r[1]), (y * depth + worldZ) * width + worldX));
                }
            }
        }
        return cells;
    }

    /** True when every cell of the footprint is replaceable or already a filler pointing at this controller. */
    public static boolean canPlace(Level level, BlockPos controller, Direction facing, ExtractorControllerBlock block) {
        for (Cell cell : cells(controller, facing, block)) {
            BlockState state = level.getBlockState(cell.world());
            if (state.getMaterial().isReplaceable()) {
                continue;
            }
            if (state.getBlock() == block.getPartBlock() && level.getBlockEntity(cell.world()) instanceof ExtractorStructureTileEntity tile
                    && controller.equals(tile.getControllerPos())) {
                continue;
            }
            return false;
        }
        return true;
    }

    public static void place(Level level, BlockPos controller, Direction facing, ExtractorControllerBlock block) {
        ExtractorStructureBlock part = block.getPartBlock();
        for (Cell cell : cells(controller, facing, block)) {
            BlockState state = part.defaultBlockState()
                    .setValue(ExtractorStructureBlock.FACING, facing)
                    .setValue(part.idxProperty(), cell.idx());
            level.setBlock(cell.world(), state, Block.UPDATE_ALL);
            if (level.getBlockEntity(cell.world()) instanceof ExtractorStructureTileEntity tile) {
                tile.setControllerPos(controller);
            } else {
                WoldLogger.error("Extractor station filler at {} has no tile entity after placement; the machine at {} is incomplete.", cell.world(), controller);
            }
        }
    }

    public static void remove(Level level, BlockPos controller, Direction facing, ExtractorControllerBlock block) {
        for (Cell cell : cells(controller, facing, block)) {
            if (level.getBlockState(cell.world()).getBlock() == block.getPartBlock()
                    && level.getBlockEntity(cell.world()) instanceof ExtractorStructureTileEntity tile
                    && controller.equals(tile.getControllerPos())) {
                level.removeBlock(cell.world(), false);
            }
        }
    }
}
