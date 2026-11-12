package xyz.iwolfking.woldsvaults.spellcraft.bar;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftItemData;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * Each player's default spell bar (ability group per slot, empty string for an empty slot), the selected slot the
 * cast key fires, and whether the bar was filled from their learned abilities yet. Saved with the overworld.
 */
public class SpellBarData extends SavedData {
    private static final String NAME = "woldsvaults_spell_bars";
    private final Map<UUID, PlayerBar> bars = new HashMap<>();

    /** One player's saved bar. */
    public static final class PlayerBar {
        public final String[] defaults = new String[SpellcraftItemData.SLOTS];
        public int selected;
        public boolean migrated;

        PlayerBar() {
            Arrays.fill(this.defaults, "");
        }
    }

    public static SpellBarData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(SpellBarData::load, SpellBarData::new, NAME);
    }

    public PlayerBar bar(UUID player) {
        return this.bars.computeIfAbsent(player, id -> new PlayerBar());
    }

    private static SpellBarData load(CompoundTag tag) {
        SpellBarData data = new SpellBarData();
        CompoundTag players = tag.getCompound("players");
        for (String key : players.getAllKeys()) {
            CompoundTag entry = players.getCompound(key);
            PlayerBar bar = new PlayerBar();
            ListTag slots = entry.getList("defaults", Tag.TAG_STRING);
            for (int i = 0; i < Math.min(SpellcraftItemData.SLOTS, slots.size()); i++) {
                bar.defaults[i] = slots.getString(i);
            }
            bar.selected = Math.max(0, Math.min(SpellcraftItemData.SLOTS - 1, entry.getInt("selected")));
            bar.migrated = entry.getBoolean("migrated");
            try {
                data.bars.put(UUID.fromString(key), bar);
            } catch (IllegalArgumentException e) {
                WoldLogger.error("Spell bars: skipped a saved bar with an invalid player id {}", key);
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag players = new CompoundTag();
        this.bars.forEach((id, bar) -> {
            CompoundTag entry = new CompoundTag();
            ListTag slots = new ListTag();
            for (String slot : bar.defaults) {
                slots.add(StringTag.valueOf(slot));
            }
            entry.put("defaults", slots);
            entry.putInt("selected", bar.selected);
            entry.putBoolean("migrated", bar.migrated);
            players.put(id.toString(), entry);
        });
        tag.put("players", players);
        return tag;
    }
}
