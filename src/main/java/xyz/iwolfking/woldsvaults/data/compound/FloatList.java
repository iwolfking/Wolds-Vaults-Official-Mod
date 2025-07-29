package xyz.iwolfking.woldsvaults.data.compound;

import iskallia.vault.core.data.DataList;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.data.adapter.IBitAdapter;

import java.util.ArrayList;
import java.util.List;

public class FloatList extends DataList<FloatList, Float> {
    protected FloatList(List<Float> delegate, IBitAdapter<Float, ?> adapter) {
        super(delegate, adapter);
    }

    public static FloatList create() {
        return new FloatList(new ArrayList<>(), Adapters.FLOAT);
    }
}
