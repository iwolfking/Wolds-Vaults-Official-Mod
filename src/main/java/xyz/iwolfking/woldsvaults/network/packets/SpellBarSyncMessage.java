package xyz.iwolfking.woldsvaults.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.augments.spell.SpellModel;
import xyz.iwolfking.woldsvaults.augments.spell.SpellcraftRules;
import xyz.iwolfking.woldsvaults.client.spellcraft.ClientSpellBar;
import xyz.iwolfking.woldsvaults.spellcraft.bar.EffectiveBar;
import xyz.iwolfking.woldsvaults.spellcraft.bar.SpellBarData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Server to client: the player's default bar, selected slot, and the effective bar the server resolved. Each slot
 * carries its occupant (item key and spell id, or the base group), the spell's name and tokens, its source item and
 * hand, priority, LOCKED state and reason, the implicit flag, the groups it holds, the overlays it shadows, and every
 * glyph's state and line. Each equipped arcana item reports its arcana, budget, budget-greyed glyphs and errors. The
 * client never resolves the bar from its own item NBT.
 */
public record SpellBarSyncMessage(String[] defaults, int selected, List<Entry> entries, List<Source> sources) {
    private static final int MAX_LIST = 256;

    /** One placed glyph as the server read it: its token index, state, dependence and line. */
    public record Glyph(int token, int state, boolean dependent, float potency, float quality, int cost) {
        public SpellModel.State stateEnum() {
            SpellModel.State[] states = SpellModel.State.values();
            return this.state >= 0 && this.state < states.length ? states[this.state] : SpellModel.State.INACTIVE_SOURCE;
        }
    }

    /** One effective slot as the client shows it. {@code hand}: -1 none, else the {@link InteractionHand} ordinal. */
    public record Entry(int kind, String group, String key, String itemKey, int spellId, String spellName, List<String> tokens,
                        String sourceName, int hand, int prio, boolean locked, String lockReason, boolean implicit, boolean hidden,
                        List<String> groups, List<String> shadowed, List<Glyph> glyphs) {
    }

    /** One equipped arcana item: its key, name, hand, budget, active arcana, budget-greyed glyphs and errors. */
    public record Source(String key, String name, int hand, int budget, int arcana, int budgetInactive, List<String> errors) {
    }

    public static SpellBarSyncMessage of(SpellBarData.PlayerBar saved, EffectiveBar bar) {
        List<Entry> entries = new ArrayList<>();
        for (EffectiveBar.Slot slot : bar.slots()) {
            List<Glyph> glyphs = new ArrayList<>();
            if (slot.model() != null) {
                for (SpellModel.Glyph glyph : slot.model().glyphs()) {
                    glyphs.add(new Glyph(glyph.token(), glyph.state().ordinal(), glyph.dependent(), glyph.potency(), glyph.quality(), glyph.cost()));
                }
            }
            entries.add(new Entry(slot.kind().ordinal(), slot.group(), slot.key(), slot.sourceKey() == null ? "" : slot.sourceKey(),
                    slot.spellId(), slot.spell() == null ? "" : slot.spell().name(), slot.spell() == null ? List.of() : slot.spell().tokens(),
                    slot.sourceName(), hand(slot.hand()), slot.prio(), slot.locked(), slot.lockReason(), slot.implicit(), slot.hidden(),
                    List.copyOf(slot.groups()), slot.shadowed(), glyphs));
        }
        List<Source> sources = new ArrayList<>();
        for (EffectiveBar.ArcanaItem item : bar.items()) {
            SpellcraftRules.ItemCheck check = item.check();
            sources.add(new Source(item.key(), item.name(), hand(item.hand()), check.budget(), Math.round(check.arcana()), check.budgetInactive(),
                    check.errors()));
        }
        return new SpellBarSyncMessage(saved.defaults.clone(), saved.selected, entries, sources);
    }

    private static int hand(@Nullable InteractionHand hand) {
        return hand == null ? -1 : hand.ordinal();
    }

    /** Everything the client shows, as one string; the server only resends when it changes. */
    public String signature() {
        return this.selected + "|" + String.join(",", this.defaults) + "|" + this.entries + "|" + this.sources;
    }

    public static void encode(SpellBarSyncMessage message, FriendlyByteBuf buffer) {
        for (String slot : message.defaults) {
            buffer.writeUtf(slot);
        }
        buffer.writeVarInt(message.selected);
        buffer.writeVarInt(message.entries.size());
        for (Entry entry : message.entries) {
            buffer.writeVarInt(entry.kind());
            buffer.writeUtf(entry.group());
            buffer.writeUtf(entry.key());
            buffer.writeUtf(entry.itemKey());
            buffer.writeVarInt(entry.spellId());
            buffer.writeUtf(entry.spellName());
            writeStrings(buffer, entry.tokens());
            buffer.writeUtf(entry.sourceName());
            buffer.writeVarInt(entry.hand() + 1);
            buffer.writeVarInt(entry.prio());
            buffer.writeBoolean(entry.locked());
            buffer.writeUtf(entry.lockReason());
            buffer.writeBoolean(entry.implicit());
            buffer.writeBoolean(entry.hidden());
            writeStrings(buffer, entry.groups());
            writeStrings(buffer, entry.shadowed());
            buffer.writeVarInt(entry.glyphs().size());
            for (Glyph glyph : entry.glyphs()) {
                buffer.writeVarInt(glyph.token());
                buffer.writeVarInt(glyph.state());
                buffer.writeBoolean(glyph.dependent());
                buffer.writeFloat(glyph.potency());
                buffer.writeFloat(glyph.quality());
                buffer.writeVarInt(glyph.cost());
            }
        }
        buffer.writeVarInt(message.sources.size());
        for (Source source : message.sources) {
            buffer.writeUtf(source.key());
            buffer.writeUtf(source.name());
            buffer.writeVarInt(source.hand() + 1);
            buffer.writeVarInt(source.budget());
            buffer.writeVarInt(source.arcana());
            buffer.writeVarInt(source.budgetInactive());
            writeStrings(buffer, source.errors());
        }
    }

    public static SpellBarSyncMessage decode(FriendlyByteBuf buffer) {
        String[] defaults = new String[SpellcraftItemData.SLOTS];
        for (int i = 0; i < defaults.length; i++) {
            defaults[i] = buffer.readUtf();
        }
        int selected = buffer.readVarInt();
        int count = Math.min(MAX_LIST, buffer.readVarInt());
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int kind = buffer.readVarInt();
            String group = buffer.readUtf();
            String key = buffer.readUtf();
            String itemKey = buffer.readUtf();
            int spellId = buffer.readVarInt();
            String spellName = buffer.readUtf();
            List<String> tokens = readStrings(buffer);
            String sourceName = buffer.readUtf();
            int hand = buffer.readVarInt() - 1;
            int prio = buffer.readVarInt();
            boolean locked = buffer.readBoolean();
            String lockReason = buffer.readUtf();
            boolean implicit = buffer.readBoolean();
            boolean hidden = buffer.readBoolean();
            List<String> groups = readStrings(buffer);
            List<String> shadowed = readStrings(buffer);
            int glyphCount = Math.min(MAX_LIST, buffer.readVarInt());
            List<Glyph> glyphs = new ArrayList<>();
            for (int g = 0; g < glyphCount; g++) {
                glyphs.add(new Glyph(buffer.readVarInt(), buffer.readVarInt(), buffer.readBoolean(), buffer.readFloat(), buffer.readFloat(),
                        buffer.readVarInt()));
            }
            entries.add(new Entry(kind, group, key, itemKey, spellId, spellName, tokens, sourceName, hand, prio, locked, lockReason, implicit,
                    hidden, groups, shadowed, glyphs));
        }
        int sourceCount = Math.min(MAX_LIST, buffer.readVarInt());
        List<Source> sources = new ArrayList<>();
        for (int i = 0; i < sourceCount; i++) {
            sources.add(new Source(buffer.readUtf(), buffer.readUtf(), buffer.readVarInt() - 1, buffer.readVarInt(), buffer.readVarInt(),
                    buffer.readVarInt(), readStrings(buffer)));
        }
        return new SpellBarSyncMessage(defaults, selected, entries, sources);
    }

    private static void writeStrings(FriendlyByteBuf buffer, List<String> strings) {
        buffer.writeVarInt(strings.size());
        strings.forEach(buffer::writeUtf);
    }

    private static List<String> readStrings(FriendlyByteBuf buffer) {
        int count = Math.min(MAX_LIST, buffer.readVarInt());
        List<String> strings = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            strings.add(buffer.readUtf());
        }
        return strings;
    }

    public static void handle(SpellBarSyncMessage message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientSpellBar.update(message)));
        context.get().setPacketHandled(true);
    }
}
