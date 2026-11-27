package xyz.iwolfking.woldsvaults.spellcraft.play;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.spellcraft.ability.AbilityLookup;
import xyz.iwolfking.woldsvaults.spellcraft.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.spellcraft.item.StoredSpell;
import xyz.iwolfking.woldsvaults.spellcraft.source.EquippedSources;
import xyz.iwolfking.woldsvaults.spellcraft.source.GlyphView;
import xyz.iwolfking.woldsvaults.spellcraft.spell.SpellcraftRules;

import javax.annotation.Nullable;
import java.util.List;

public final class SpellEdits {
    private SpellEdits() {
    }

    /**
     * Creates (id 0), changes or deletes a spell on the focus or wand in the player's inventory slot. The whole item is
     * checked against its glyph view first, and a changed spell keeps its slot and priority.
     */
    public static void editSpell(ServerPlayer player, int inventorySlot, int spellId, String name, List<String> tokens, boolean delete) {
        SpellcraftConfig config = ModConfigs.SPELLCRAFT;
        ItemStack stack = carrier(player, inventorySlot);
        if (config == null || stack == null) {
            return;
        }
        if (delete) {
            SpellcraftItemData.removeSpell(stack, spellId);
            SpellBars.refresh(player);
            return;
        }
        String cleanName = name.strip();
        if (cleanName.isEmpty()) {
            cleanName = "Spell";
        }
        if (cleanName.length() > config.rules.maxNameLength) {
            cleanName = cleanName.substring(0, config.rules.maxNameLength);
        }
        StoredSpell stored = spellId > 0 ? SpellcraftItemData.spell(stack, spellId).orElse(null) : null;
        int slot = stored == null ? SpellcraftItemData.UNASSIGNED : stored.slot();
        int prio = stored == null ? 0 : stored.prio();
        int checkId = spellId > 0 ? spellId : StoredSpell.UNSAVED_ID;
        StoredSpell edited = new StoredSpell(checkId, cleanName, List.copyOf(tokens), slot, prio);
        if (!store(player, config, stack, SpellcraftRules.withEdit(stack, checkId, edited), "Spell not saved: ")) {
            return;
        }
        SpellcraftItemData.putSpell(stack, new StoredSpell(spellId > 0 ? spellId : 0, cleanName, List.copyOf(tokens), slot, prio));
        SpellBars.refresh(player);
    }

    /**
     * Moves a spell of the item in the player's inventory slot to a bar slot ({@link SpellcraftItemData#UNASSIGNED}
     * takes it off the bar) and sets its priority (0 keeps it). Refused when another spell of the item already uses the
     * slot.
     */
    public static void placeSpell(ServerPlayer player, int inventorySlot, int spellId, int barSlot, int prio) {
        SpellcraftConfig config = ModConfigs.SPELLCRAFT;
        if (config == null) {
            WoldLogger.error("Spellcraft: no spellcraft table; spell {} was not moved", spellId);
            return;
        }
        ItemStack stack = carrier(player, inventorySlot);
        if (stack == null) {
            return;
        }
        StoredSpell stored = SpellcraftItemData.spell(stack, spellId).orElse(null);
        if (stored == null) {
            refuse(player, "That spell is not on the item");
            return;
        }
        if (barSlot < SpellcraftItemData.UNASSIGNED || barSlot >= SpellcraftItemData.SLOTS || prio < 0 || prio > config.rules.priorities) {
            refuse(player, "Spell not moved: slot or priority out of range");
            return;
        }
        StoredSpell moved = stored.withSlot(barSlot).withPrio(prio > 0 ? prio : stored.prio());
        if (!store(player, config, stack, SpellcraftRules.withEdit(stack, spellId, moved), "Spell not moved: ")) {
            return;
        }
        SpellcraftItemData.putSpell(stack, moved);
        SpellBars.refresh(player);
    }

    private static boolean store(ServerPlayer player, SpellcraftConfig config, ItemStack stack, List<StoredSpell> spells,
                                 String refusal) {
        GlyphView view = EquippedSources.get(player).view(config, stack);
        SpellcraftRules.ItemCheck check = SpellcraftRules.checkEdit(config, stack, view, spells, AbilityLookup.specOf(AbilityLookup.tree(player)));
        if (!check.isValid()) {
            refuse(player, refusal + check.errors().get(0));
            return false;
        }
        return true;
    }

    @Nullable
    private static ItemStack carrier(ServerPlayer player, int inventorySlot) {
        if (inventorySlot < 0 || inventorySlot >= player.getInventory().getContainerSize()) {
            return null;
        }
        ItemStack stack = player.getInventory().getItem(inventorySlot);
        if (!SpellcraftItemData.canCarry(stack) || !SpellcraftItemData.has(stack)) {
            refuse(player, "That item has no spellcraft");
            return null;
        }
        return stack;
    }

    private static void refuse(ServerPlayer player, String message) {
        WoldLogger.warn("Spellcraft: refused a spell edit by {}: {}", player.getGameProfile().getName(), message);
        player.displayClientMessage(new TextComponent(message).withStyle(ChatFormatting.LIGHT_PURPLE), false);
    }
}
