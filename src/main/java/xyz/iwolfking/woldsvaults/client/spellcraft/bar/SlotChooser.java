package xyz.iwolfking.woldsvaults.client.spellcraft.bar;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SpecializedSkill;
import iskallia.vault.skill.tree.AbilityTree;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftDraw;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftNames;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftPalette;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.network.packets.SpellPlaceMessage;
import xyz.iwolfking.woldsvaults.spellcraft.ability.AbilityLookup;
import xyz.iwolfking.woldsvaults.spellcraft.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.spellcraft.item.StoredSpell;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

final class SlotChooser {
    private static final int POPUP_ROW = 12;
    private static final int POPUP_MAX_ROWS = 10;
    private static final int POPUP_BG = 0xF0100010;
    private static final int POPUP_HOVER = 0x405000FF;

    private static final String SPELL_CHOICE = "spell:";

    private record Choice(String value, String label, @Nullable String icon, int color) {
    }

    private final SpellBarStrip strip;
    private int popupSlot = -1;
    private List<Choice> popup = List.of();
    private int popupScroll;

    SlotChooser(SpellBarStrip strip) {
        this.strip = strip;
    }

    void open(int index, AbilityTree tree) {
        List<Choice> choices = new ArrayList<>();
        choices.add(new Choice("", SpellcraftNames.tr("picker.empty"), null, SpellcraftPalette.GREY));
        choices.addAll(spellChoices(tree));
        for (Skill skill : tree.skills) {
            if (skill instanceof SpecializedSkill specialized && specialized.isUnlocked()) {
                choices.add(new Choice(specialized.getId(), SpellcraftNames.abilityName(tree, specialized.getId()), specialized.getId(),
                        SpellcraftPalette.NAME_GOLD));
            }
        }
        this.popup = choices;
        this.popupSlot = index;
        this.popupScroll = 0;
    }

    void render(PoseStack pose, Screen screen, int mouseX, int mouseY) {
        Font font = Minecraft.getInstance().font;
        int[] box = popupBox(screen);
        int px = box[0];
        int py = box[1];
        int w = box[2];
        int rows = box[3];
        GuiComponent.fill(pose, px - 2, py - 2, px + w + 2, py + rows * POPUP_ROW + 2, POPUP_BG);
        SpellcraftDraw.ring(pose, px - 2, py - 2, px + w + 2, py + rows * POPUP_ROW + 2, SpellcraftPalette.ARCANE_LINE);
        for (int r = 0; r < rows; r++) {
            Choice choice = this.popup.get(r + this.popupScroll);
            int ry = py + r * POPUP_ROW;
            if (SpellcraftDraw.inside(mouseX, mouseY, px, ry, w, POPUP_ROW)) {
                GuiComponent.fill(pose, px, ry, px + w, ry + POPUP_ROW, POPUP_HOVER);
            }
            if (choice.icon() != null) {
                SpellcraftDraw.ability(pose, choice.icon(), px + 2, ry + 2, 8, 1.0F);
            }
            font.drawShadow(pose, font.plainSubstrByWidth(choice.label(), w - 14), px + 12, ry + 2, choice.color());
        }
        if (this.popup.size() > rows) {
            String more = SpellcraftNames.tr("bar.scroll", this.popupScroll + 1, this.popupScroll + rows, this.popup.size());
            SpellcraftDraw.smallText(pose, font, more, px, py - 10, SpellcraftPalette.MUTED, SpellBarStrip.KEY_SCALE);
        }
    }

    /** The chooser's left, top, width and visible row count, placed above the slot and inside the screen. */
    private int[] popupBox(Screen screen) {
        Font font = Minecraft.getInstance().font;
        int w = 60;
        for (Choice choice : this.popup) {
            w = Math.max(w, font.width(choice.label()) + 16);
        }
        w = Math.min(w, screen.width - 8);
        int top = 14;
        int rows = Math.min(this.popup.size(), Math.min(POPUP_MAX_ROWS, (this.strip.y0() - 4 - top) / POPUP_ROW));
        int x = Math.max(4, Math.min(screen.width - w - 4, this.strip.slotX(this.popupSlot) - 2));
        int y = this.strip.y0() - 4 - rows * POPUP_ROW;
        return new int[]{x, y, w, Math.max(1, rows)};
    }

    boolean mouseClicked(double mouseX, double mouseY, int button) {
        Screen screen = Minecraft.getInstance().screen;
        if (this.popupSlot >= 0 && screen != null) {
            int[] box = popupBox(screen);
            if (SpellcraftDraw.inside(mouseX, mouseY, box[0], box[1], box[2], box[3] * POPUP_ROW) && button == 0) {
                int row = (int) ((mouseY - box[1]) / POPUP_ROW) + this.popupScroll;
                if (row >= 0 && row < this.popup.size()) {
                    pick(this.popupSlot, this.popup.get(row).value());
                    SpellcraftDraw.click();
                }
            }
            this.popupSlot = -1;
            return true;
        }
        return false;
    }

    private void pick(int index, String value) {
        if (value.startsWith(SPELL_CHOICE)) {
            String[] parts = value.substring(SPELL_CHOICE.length()).split(":");
            ModNetwork.sendToServer(new SpellPlaceMessage(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), index, 0));
            return;
        }
        this.strip.lastView().send(index, value);
    }

    private static List<Choice> spellChoices(AbilityTree tree) {
        List<Choice> choices = new ArrayList<>();
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return choices;
        }
        ItemStack stack = player.getInventory().getItem(Inventory.SLOT_OFFHAND);
        if (!SpellcraftItemData.canCarry(stack) || !SpellcraftItemData.has(stack)) {
            return choices;
        }
        Function<String, String> specOf = AbilityLookup.specOf(tree);
        for (StoredSpell spell : SpellcraftItemData.spells(stack)) {
            String spec = specOf.apply(spell.trigger());
            String label = spell.assigned() ? SpellcraftNames.tr("picker.spell_on", spell.name(), spell.slot() + 1) : SpellcraftNames.tr("picker.spell", spell.name());
            choices.add(new Choice(SPELL_CHOICE + Inventory.SLOT_OFFHAND + ":" + spell.id(), label, spec != null ? spec : spell.trigger(),
                    SpellcraftPalette.ARCANE_GLOW));
        }
        return choices;
    }

    boolean mouseScrolled(double delta) {
        Screen screen = Minecraft.getInstance().screen;
        if (this.popupSlot < 0 || screen == null) {
            return false;
        }
        int rows = popupBox(screen)[3];
        int max = Math.max(0, this.popup.size() - rows);
        this.popupScroll = Math.max(0, Math.min(max, this.popupScroll + (delta < 0 ? 1 : -1)));
        return true;
    }

    boolean popupOpen() {
        return this.popupSlot >= 0;
    }
}
