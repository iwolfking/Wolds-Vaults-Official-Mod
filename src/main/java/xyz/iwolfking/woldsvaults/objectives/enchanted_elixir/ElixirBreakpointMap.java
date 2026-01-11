package xyz.iwolfking.woldsvaults.objectives.enchanted_elixir;

import iskallia.vault.core.data.DataMap;
import iskallia.vault.core.data.adapter.IBitAdapter;
import iskallia.vault.core.data.adapter.basic.UuidAdapter;
import iskallia.vault.core.data.sync.context.SyncContext;
import iskallia.vault.core.net.BitBuffer;
import net.minecraft.world.entity.player.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ElixirBreakpointMap extends DataMap<ElixirBreakpointMap, UUID, List<Float>> {


    public ElixirBreakpointMap() {
        super(new ConcurrentHashMap<>(), new UuidAdapter(false), new FloatListAdapter());
    }

    public static class FloatListAdapter implements IBitAdapter<List<Float>, SyncContext> {

        @Override
        public Optional<List<Float>> readBits(BitBuffer buffer, SyncContext context) {
            int size = buffer.readIntSegmented(8);
            List<Float> list = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                list.add(buffer.readFloat());
            }
            return Optional.of(list);
        }

        @Override
        public void writeBits(List<Float> value, BitBuffer buffer, SyncContext context) {
            if (value == null) {
                buffer.writeIntSegmented(0, 8);
                return;
            }
            buffer.writeIntSegmented(value.size(), 8);
            for (Float f : value) {
                buffer.writeFloat(f);
            }
        }
    }

    public List<Float> get(Player player) {
        return this.get(player.getUUID());
    }
}
