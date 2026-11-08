package xyz.iwolfking.woldsvaults.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.client.spellcraft.ClientSpellBar;
import xyz.iwolfking.woldsvaults.spellcraft.bar.EffectiveBar;
import xyz.iwolfking.woldsvaults.spellcraft.bar.SpellBarData;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Server to client: the player's default bar, selected slot, and the effective bar they cast (each slot's kind,
 * ability, spell name and whether it is inherited, hidden or broken), plus the bar item's name and problems.
 */
public record SpellBarSyncMessage(String[] defaults, int selected, String sourceName, List<Entry> entries, List<String> itemErrors) {

    /** One effective slot as the client shows it. */
    public record Entry(int kind, String group, String spellName, boolean inherited, boolean hidden, boolean broken) {
    }

    public static SpellBarSyncMessage of(SpellBarData.PlayerBar saved, EffectiveBar bar) {
        List<Entry> entries = new ArrayList<>();
        for (EffectiveBar.Slot slot : bar.slots()) {
            entries.add(new Entry(slot.kind().ordinal(), slot.group(), slot.spell() == null ? "" : slot.spell().name(),
                    slot.inherited(), slot.hidden(), slot.model() != null && !slot.model().isValid()));
        }
        String name = bar.hasItemBar() ? bar.source().getHoverName().getString() : "";
        return new SpellBarSyncMessage(saved.defaults.clone(), saved.selected, name, entries, bar.itemErrors());
    }

    public static void encode(SpellBarSyncMessage message, FriendlyByteBuf buffer) {
        for (String slot : message.defaults) {
            buffer.writeUtf(slot);
        }
        buffer.writeVarInt(message.selected);
        buffer.writeUtf(message.sourceName);
        buffer.writeVarInt(message.entries.size());
        for (Entry entry : message.entries) {
            buffer.writeVarInt(entry.kind());
            buffer.writeUtf(entry.group());
            buffer.writeUtf(entry.spellName());
            buffer.writeBoolean(entry.inherited());
            buffer.writeBoolean(entry.hidden());
            buffer.writeBoolean(entry.broken());
        }
        buffer.writeVarInt(message.itemErrors.size());
        message.itemErrors.forEach(buffer::writeUtf);
    }

    public static SpellBarSyncMessage decode(FriendlyByteBuf buffer) {
        String[] defaults = new String[SpellcraftItemData.SLOTS];
        for (int i = 0; i < defaults.length; i++) {
            defaults[i] = buffer.readUtf();
        }
        int selected = buffer.readVarInt();
        String name = buffer.readUtf();
        int count = buffer.readVarInt();
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            entries.add(new Entry(buffer.readVarInt(), buffer.readUtf(), buffer.readUtf(), buffer.readBoolean(), buffer.readBoolean(),
                    buffer.readBoolean()));
        }
        int errors = buffer.readVarInt();
        List<String> itemErrors = new ArrayList<>();
        for (int i = 0; i < errors; i++) {
            itemErrors.add(buffer.readUtf());
        }
        return new SpellBarSyncMessage(defaults, selected, name, entries, itemErrors);
    }

    public static void handle(SpellBarSyncMessage message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientSpellBar.update(message)));
        context.get().setPacketHandled(true);
    }
}
