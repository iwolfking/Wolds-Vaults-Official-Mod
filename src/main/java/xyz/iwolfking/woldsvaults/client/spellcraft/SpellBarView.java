package xyz.iwolfking.woldsvaults.client.spellcraft;

import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SpecializedSkill;
import iskallia.vault.skill.tree.AbilityTree;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.augments.AugmentType;
import xyz.iwolfking.woldsvaults.augments.FocusAugments;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.augments.spell.SpellModel;
import xyz.iwolfking.woldsvaults.augments.spell.SpellcraftRules;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.network.packets.SpellBarEditMessage;
import xyz.iwolfking.woldsvaults.network.packets.SpellBarSyncMessage;
import xyz.iwolfking.woldsvaults.spellcraft.pool.GlyphView;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One bar as the spell bar strip draws it: the default bar (editable), or the effective bar exactly as the server last
 * resolved it (read-only; items no longer hold bars). Each slot knows what it shows (ability, spell, hidden, empty),
 * whether it is unlearned, LOCKED or implicit, where its spell comes from, and which ability groups it holds. A synced
 * spell's model is rebuilt from the server's tokens, glyph lines and glyph states, never from the client's item NBT.
 */
public final class SpellBarView {
    public enum State {
        EMPTY,
        ABILITY,
        SPELL,
        HIDDEN
    }

    /**
     * One displayed slot; {@code spell} and {@code model} are set for spells. {@code itemKey}/{@code spellId} identify the
     * spell's item and spell; {@code sourceName} names the item.
     */
    public record Slot(int index, State state, String group, String spellName, boolean unlearned, @Nullable SpellcraftItemData.Spell spell,
                       @Nullable SpellModel model, List<String> groups, String itemKey, int spellId, String sourceName,
                       @Nullable InteractionHand hand, int prio, boolean locked, String lockReason, boolean implicit, List<String> shadowed) {
        public boolean filled() {
            return this.state == State.ABILITY || (this.state == State.SPELL && !this.locked);
        }

        static Slot of(int index, State state, String group, List<String> groups, boolean unlearned) {
            return new Slot(index, state, group, "", unlearned, null, null, groups, "", 0, "", null, 0, false, "", false, List.of());
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

    /** The default bar as last synced. */
    public static SpellBarView ofDefaults(AbilityTree tree) {
        Slot[] slots = new Slot[SpellcraftItemData.SLOTS];
        for (int i = 0; i < slots.length; i++) {
            String group = ClientSpellBar.defaultSlot(i);
            slots[i] = group.isEmpty() ? Slot.of(i, State.EMPTY, "", List.of(), false)
                    : Slot.of(i, State.ABILITY, group, List.of(group), !learned(tree, group));
        }
        return new SpellBarView(slots, false, ItemStack.EMPTY);
    }

    /** The effective bar as the server last synced it, or null when no item lays a spell over it. */
    @Nullable
    public static SpellBarView ofSynced(AbilityTree tree) {
        if (!ClientSpellBar.isSynced() || !ClientSpellBar.hasOverlays()) {
            return null;
        }
        Slot[] slots = new Slot[SpellcraftItemData.SLOTS];
        FocusAugmentsConfig config = FocusAugments.config();
        for (int i = 0; i < slots.length; i++) {
            SpellBarSyncMessage.Entry entry = ClientSpellBar.entry(i);
            String group = entry.group();
            if (entry.kind() == ClientSpellBar.ABILITY) {
                slots[i] = Slot.of(i, State.ABILITY, group, List.of(group), !learned(tree, group));
            } else if (entry.kind() == ClientSpellBar.SPELL) {
                SpellcraftItemData.Spell spell = new SpellcraftItemData.Spell(entry.spellId(), entry.spellName(), entry.tokens(), i, entry.prio());
                SpellModel model = config == null ? null : model(config, tree, entry);
                InteractionHand hand = entry.hand() >= 0 && entry.hand() < InteractionHand.values().length ? InteractionHand.values()[entry.hand()] : null;
                slots[i] = new Slot(i, State.SPELL, group, entry.spellName(), entry.groups().stream().anyMatch(g -> !learned(tree, g)), spell, model,
                        entry.groups(), entry.itemKey(), entry.spellId(), entry.sourceName(), hand, entry.prio(), entry.locked(), entry.lockReason(),
                        entry.implicit(), entry.shadowed());
            } else {
                slots[i] = Slot.of(i, entry.hidden() ? State.HIDDEN : State.EMPTY, group, List.of(), false);
            }
        }
        return new SpellBarView(slots, true, badgeStack());
    }

    /**
     * Rebuilds a synced spell's model: a view made of the server's glyph lines, and the server's states forced on every
     * glyph it did not find active for a reason the client can't see (sources, uses, budget).
     */
    private static SpellModel model(FocusAugmentsConfig config, AbilityTree tree, SpellBarSyncMessage.Entry entry) {
        Map<AugmentType, SpellcraftItemData.Line> lines = new LinkedHashMap<>();
        Map<Integer, SpellModel.State> forced = new HashMap<>();
        for (SpellBarSyncMessage.Glyph glyph : entry.glyphs()) {
            if (glyph.token() < 0 || glyph.token() >= entry.tokens().size()) {
                continue;
            }
            AugmentType type = AugmentType.byId(entry.tokens().get(glyph.token()));
            if (type == null) {
                continue;
            }
            lines.putIfAbsent(type, new SpellcraftItemData.Line(type, glyph.potency(), glyph.quality(), glyph.cost(), entry.tokens().size(), false,
                    entry.tokens().size()));
            SpellModel.State state = glyph.stateEnum();
            if (!glyph.dependent() && state != SpellModel.State.ACTIVE && state != SpellModel.State.DORMANT) {
                forced.put(glyph.token(), state);
            }
        }
        GlyphView view = GlyphView.of(config, "", "", new ArrayList<>(lines.values()), List.of());
        return SpellModel.build(config, view, config.rules.maxGlyphsPerSpell, entry.tokens(), SpellcraftRules.specOf(tree), SpellModel.Mode.LENIENT,
                forced, Map.of());
    }

    /** The first synced arcana item found in the player's hands, for the strip's picker icon. */
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

    public boolean isItem() {
        return this.item;
    }

    /** The item shown on the strip's picker for this view (empty for the default bar). */
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

    /** The filled slot that hides a hidden slot (it casts the same ability), or -1. */
    public int hiddenBy(int index) {
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

    /** Sets a default-bar slot to an ability group, or clears it with an empty value. Synced views are read-only. */
    public void send(int index, String value) {
        if (!this.item) {
            ModNetwork.sendToServer(new SpellBarEditMessage(index, value));
        }
    }
}
