package xyz.iwolfking.woldsvaults.objectives.enchanted_elixir;

import iskallia.vault.core.data.DataList;
import iskallia.vault.core.data.DataMap;
import iskallia.vault.core.data.adapter.basic.StringAdapter;
import iskallia.vault.core.data.adapter.basic.UuidAdapter;
import iskallia.vault.core.data.adapter.number.IntAdapter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StringList extends DataList<StringList, String> {
    public StringList() {
        super(new ArrayList<>(), new StringAdapter(Charset.defaultCharset(), false));
    }
}
