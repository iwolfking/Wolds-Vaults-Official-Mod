package xyz.iwolfking.woldsvaults.client.spellcraft.bar;

import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SpecializedSkill;
import iskallia.vault.skill.tree.AbilityTree;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.client.spellcraft.state.ClientSpellBar;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.network.packets.SpellBarEditMessage;
import xyz.iwolfking.woldsvaults.network.packets.SpellBarSyncMessage;
import xyz.iwolfking.woldsvaults.spellcraft.ability.AbilityLookup;
import xyz.iwolfking.woldsvaults.spellcraft.bar.BarMode;
import xyz.iwolfking.woldsvaults.spellcraft.bar.EffectiveBar;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphType;
import xyz.iwolfking.woldsvaults.spellcraft.item.GlyphLine;
import xyz.iwolfking.woldsvaults.spellcraft.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.spellcraft.item.StoredSpell;
import xyz.iwolfking.woldsvaults.spellcraft.source.GlyphView;
import xyz.iwolfking.woldsvaults.spellcraft.spell.GlyphState;
import xyz.iwolfking.woldsvaults.spellcraft.spell.ReadMode;
import xyz.iwolfking.woldsvaults.spellcraft.spell.SpellModel;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One bar as the spell bar strip draws it: the default bar, or the effective bar as the server last resolved it. A
 * synced spell's model is rebuilt from the server's tokens, glyph lines and glyph states, never from the client's item
 * NBT.
 */
public final class SpellBarView {
    public enum State {
        EMPTY,
        ABILITY,
        SPELL,
        HIDDEN
    }

    public record Slot(int index, State state, String group, String spellName, boolean unlearned, @Nullable StoredSpell spell,
                       @Nullable SpellModel model, List<String> groups, String itemKey, int spellId, String sourceName,
                       int prio, boolean locked, String lockReason, boolean implicit, List<String> shadowed) {
        public boolean filled() {
            return this.state == State.ABILITY || (this.state == State.SPELL && !this.locked);
        }

        static Slot of(int index, State state, String group, List<String> groups, boolean unlearned) {
            return new Slot(index, state, group, "", unlearned, null, null, groups, "", 0, "", 0, false, "", false, List.of());
        }
    }

    private final Slot[] slots;
    private final boolean item;
    private final ItemStack badge;

    private SpellBarView(Slot[] slots, boolean item, ItemStack badge) {
        this.slots = slots;
        this.item = item;
        this.badge = badge;
    }

    public static SpellBarView ofDefaults(AbilityTree tree) {
        Slot[] slots = new Slot[SpellcraftItemData.SLOTS];
        for (int i = 0; i < slots.length; i++) {
            String group = ClientSpellBar.defaultSlot(i);
            slots[i] = group.isEmpty() ? Slot.of(i, State.EMPTY, "", List.of(), false)
                    : Slot.of(i, State.ABILITY, group, List.of(group), !learned(tree, group));
        }
        return new SpellBarView(slots, false, ItemStack.EMPTY);
    }

    /**
     * The effective bar as the server last synced it, or null before the first sync, or in slot mode when no item lays
     * a spell over it. In classic mode it is the bar itself, so it is shown even without spells.
     */
    @Nullable
    public static SpellBarView ofSynced(AbilityTree tree) {
        if (!ClientSpellBar.isSynced() || (ClientSpellBar.mode() == BarMode.SLOT && !ClientSpellBar.hasOverlays())) {
            return null;
        }
        Slot[] slots = new Slot[SpellcraftItemData.SLOTS];
        SpellcraftConfig config = ModConfigs.SPELLCRAFT;
        for (int i = 0; i < slots.length; i++) {
            SpellBarSyncMessage.Entry entry = ClientSpellBar.entry(i);
            String group = entry.group();
            EffectiveBar.Kind kind = ClientSpellBar.kind(i);
            if (kind == EffectiveBar.Kind.ABILITY) {
                slots[i] = Slot.of(i, State.ABILITY, group, List.of(group), !learned(tree, group));
            } else if (kind == EffectiveBar.Kind.SPELL) {
                StoredSpell spell = new StoredSpell(entry.spellId(), entry.spellName(), entry.tokens(), i, entry.prio());
                SpellModel model = config == null ? null : model(config, tree, entry);
                slots[i] = new Slot(i, State.SPELL, group, entry.spellName(), entry.groups().stream().anyMatch(g -> !learned(tree, g)), spell, model,
                        entry.groups(), entry.itemKey(), entry.spellId(), entry.sourceName(), entry.prio(), entry.locked(), entry.lockReason(),
                        entry.implicit(), entry.shadowed());
            } else {
                slots[i] = Slot.of(i, entry.hidden() ? State.HIDDEN : State.EMPTY, group, List.of(), false);
            }
        }
        return new SpellBarView(slots, true, badgeStack());
    }

    /**
     * Forces the server's state onto each independent glyph it found neither active nor dormant, since the client
     * cannot see why.
     */
    private static SpellModel model(SpellcraftConfig config, AbilityTree tree, SpellBarSyncMessage.Entry entry) {
        Map<GlyphType, GlyphLine> lines = new LinkedHashMap<>();
        Map<Integer, GlyphState> forced = new HashMap<>();
        for (SpellBarSyncMessage.Glyph glyph : entry.glyphs()) {
            if (glyph.token() < 0 || glyph.token() >= entry.tokens().size()) {
                continue;
            }
            GlyphType type = GlyphType.byId(entry.tokens().get(glyph.token()));
            if (type == null) {
                continue;
            }
            lines.putIfAbsent(type, new GlyphLine(type, glyph.potency(), glyph.quality(), glyph.cost(), entry.tokens().size(), false,
                    entry.tokens().size()));
            GlyphState state = glyph.stateEnum();
            if (!glyph.dependent() && state != GlyphState.ACTIVE && state != GlyphState.DORMANT) {
                forced.put(glyph.token(), state);
            }
        }
        GlyphView view = GlyphView.of(config, "", "", new ArrayList<>(lines.values()), List.of());
        return SpellModel.build(config, view, config.rules.maxGlyphsPerSpell, entry.tokens(), AbilityLookup.specOf(tree), ReadMode.LENIENT,
                forced, Map.of());
    }

    private static ItemStack badgeStack() {
        for (SpellBarSyncMessage.Source source : ClientSpellBar.sources()) {
            ItemStack stack = ClientSpellBar.stackOf(source.key());
            if (!stack.isEmpty()) {
                return stack;
            }
        }
        return Minecraft.getInstance().player == null ? ItemStack.EMPTY : Minecraft.getInstance().player.getOffhandItem();
    }

    public static boolean learned(AbilityTree tree, String group) {
        Skill skill = tree.getForId(group).orElse(null);
        return skill instanceof SpecializedSkill specialized && specialized.isUnlocked();
    }

    public Slot slot(int index) {
        return this.slots[index];
    }

    public boolean isEffective() {
        return this.item;
    }

    /** The item shown on the strip's picker for this view, empty for the default bar. */
    public ItemStack stack() {
        return this.badge;
    }

    /** The slot that casts this ability group, or -1. */
    public int slotOf(String group) {
        for (Slot slot : this.slots) {
            if (slot.filled() && slot.groups().contains(group)) {
                return slot.index();
            }
        }
        return -1;
    }

    /** The filled slot that hides the hidden slot at {@code index} by casting the same ability, or -1. */
    int hiddenBy(int index) {
        Slot hidden = this.slots[index];
        if (hidden.state() != State.HIDDEN) {
            return -1;
        }
        for (Slot slot : this.slots) {
            if (slot.index() != index && slot.filled() && slot.groups().contains(hidden.group())) {
                return slot.index();
            }
        }
        return -1;
    }

    /** Sets a default-bar slot to an ability group, or clears it with an empty value, whichever view this is. */
    public void send(int index, String value) {
        ModNetwork.sendToServer(new SpellBarEditMessage(index, value));
    }
}
