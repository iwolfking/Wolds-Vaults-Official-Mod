package xyz.iwolfking.woldsvaults.data.compound;

import iskallia.vault.core.data.DataList;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.data.adapter.IBitAdapter;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class BlockPosList extends DataList<BlockPosList, BlockPos> {
    protected BlockPosList(List<BlockPos> delegate, IBitAdapter<BlockPos, ?> adapter) {
        super(delegate, adapter);
    }

    public static BlockPosList create() {
        return new BlockPosList(new ArrayList<>(), Adapters.BLOCK_POS);
    }
}
