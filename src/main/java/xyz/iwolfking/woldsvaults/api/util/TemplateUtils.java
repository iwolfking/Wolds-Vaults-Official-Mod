package xyz.iwolfking.woldsvaults.api.util;

import com.mojang.datafixers.util.Pair;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.world.data.entity.PartialCompoundNbt;
import iskallia.vault.core.world.data.tile.PartialBlockState;
import iskallia.vault.core.world.data.tile.PartialTile;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.core.world.template.DynamicTemplate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TemplateUtils {

    /**
     * Method to register custom Objective blocks for VaultFaster <br>
     * for an example, see {@link xyz.iwolfking.woldsvaults.objectives.CorruptedObjective#initServer(VirtualWorld, Vault)}
     *
     * @param tiles the tiles to make up the objective block
     * @return a DynamicTemplate to be used with {@code ObjectiveTemplateEvent#registerObjectiveTemplate}
     */
    @SafeVarargs
    public static DynamicTemplate createTemplateFromPairs(Pair<BlockPos, BlockState>... tiles) {
        DynamicTemplate template = new DynamicTemplate();
        for (Pair<BlockPos, BlockState> tile : tiles) {
            BlockPos position = tile.getFirst();
            BlockState blockState = tile.getSecond();
            PartialTile partialTile = PartialTile.of(
                    PartialBlockState.of(blockState),
                    PartialCompoundNbt.empty(),
                    position
            );
            template.add(partialTile);
        }
        return template;
    }

    public static DynamicTemplate createTemplateFromTiles(PartialTile... tiles) {
        DynamicTemplate template = new DynamicTemplate();
        for (PartialTile tile : tiles) {
            template.add(tile);
        }
        return template;
    }
}