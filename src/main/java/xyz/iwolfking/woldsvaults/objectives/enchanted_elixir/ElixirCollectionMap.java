package xyz.iwolfking.woldsvaults.objectives.enchanted_elixir;

import iskallia.vault.core.data.DataMap;
import iskallia.vault.core.data.adapter.basic.UuidAdapter;
import iskallia.vault.core.data.adapter.number.IntAdapter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ElixirCollectionMap extends DataMap<ElixirCollectionMap, UUID, Integer> {
    public ElixirCollectionMap() {
        super(new ConcurrentHashMap<>(), new UuidAdapter(false), new IntAdapter(false));
    }

    public Integer get(Player player) {
        return this.get(player.getUUID());
    }

    public void put(Player player, Integer value) {
        this.put(player.getUUID(), value);
    }

    public void put(ServerPlayer player, Integer value) {
        this.put(player.getUUID(), value);
    }
}
