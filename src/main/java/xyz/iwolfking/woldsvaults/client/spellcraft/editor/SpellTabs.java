package xyz.iwolfking.woldsvaults.client.spellcraft.editor;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftDraw;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftNames;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftPalette;
import xyz.iwolfking.woldsvaults.spellcraft.ability.AbilityLookup;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

final class SpellTabs {
    private static final int TAB_PLUS = 0xFFE0E0E0;
    private static final int DELETE_TINT = 0x80FF3030;

    private final SpellcraftScreen screen;
    private final SpellDrafts drafts;

    SpellTabs(SpellcraftScreen screen, SpellDrafts drafts) {
        this.screen = screen;
        this.drafts = drafts;
    }

    void render(PoseStack pose) {
        Font font = this.screen.font();
        int y = EditorLayout.tabY();
        int shown = EditorLayout.shownTabs(this.screen.width, this.drafts.size());
        for (int i = 0; i < EditorLayout.tabCount(this.screen.width, this.drafts.size()); i++) {
            int tx = EditorLayout.tabX(i);
            boolean on = i == this.drafts.selected();
            if (on) {
                SpellcraftDraw.insetGrey(pose, tx, y, tx + EditorLayout.TAB, y + EditorLayout.TAB);
            } else {
                SpellcraftDraw.insetDarkGrey(pose, tx, y, tx + EditorLayout.TAB, y + EditorLayout.TAB);
            }
            if (i >= shown) {
                GuiComponent.fill(pose, tx + 6, y + 9, tx + 14, y + 11, TAB_PLUS);
                GuiComponent.fill(pose, tx + 9, y + 6, tx + 11, y + 14, TAB_PLUS);
                continue;
            }
            SpellDrafts.Draft draft = this.drafts.get(i);
            String trigger = draft.noTrigger() ? null : draft.tokens.get(0);
            if (trigger != null) {
                String spec = AbilityLookup.specOf(this.screen.tree()).apply(trigger);
                SpellcraftDraw.ability(pose, spec != null ? spec : trigger, tx + 2, y + 2, 16, on ? 1.0F : 0.6F);
            } else {
                font.drawShadow(pose, "?", tx + (EditorLayout.TAB - font.width("?")) / 2.0F, y + 6, SpellcraftPalette.MUTED);
            }
            if (this.drafts.deleteArmed() == i) {
                GuiComponent.fill(pose, tx + 1, y + 1, tx + EditorLayout.TAB - 1, y + EditorLayout.TAB - 1, DELETE_TINT);
            }
            if (on) {
                SpellcraftDraw.ring(pose, tx, y, tx + EditorLayout.TAB, y + EditorLayout.TAB, SpellcraftPalette.ARCANE_GLOW);
            }
        }
    }

    @Nullable
    List<Component> tooltipAt(int mouseX, int mouseY) {
        int y = EditorLayout.tabY();
        for (int i = 0; i < EditorLayout.tabCount(this.screen.width, this.drafts.size()); i++) {
            if (SpellcraftDraw.inside(mouseX, mouseY, EditorLayout.tabX(i), y, EditorLayout.TAB, EditorLayout.TAB)) {
                List<Component> lines = new ArrayList<>();
                if (i >= EditorLayout.shownTabs(this.screen.width, this.drafts.size())) {
                    lines.add(new TextComponent(SpellcraftNames.tr("screen.new_tab")));
                } else {
                    SpellDrafts.Draft tab = this.drafts.get(i);
                    lines.add(SpellcraftNames.colored(this.drafts.storedName(tab), SpellcraftPalette.ARCANE_GLOW));
                    lines.add(new TextComponent(SpellcraftNames.tr(this.drafts.deleteArmed() == i ? "screen.delete_confirm" : "screen.tab_hint"))
                            .withStyle(ChatFormatting.DARK_GRAY));
                }
                return lines;
            }
        }
        return null;
    }

    boolean mouseClicked(double mouseX, double mouseY, int button) {
        int y = EditorLayout.tabY();
        int shown = EditorLayout.shownTabs(this.screen.width, this.drafts.size());
        for (int i = 0; i < EditorLayout.tabCount(this.screen.width, this.drafts.size()); i++) {
            if (!SpellcraftDraw.inside(mouseX, mouseY, EditorLayout.tabX(i), y, EditorLayout.TAB, EditorLayout.TAB)) {
                continue;
            }
            if (i >= shown) {
                if (button == 0) {
                    this.drafts.addDraft();
                    SpellcraftDraw.click();
                }
                return true;
            }
            if (button == 0) {
                this.drafts.select(i);
                SpellcraftDraw.click();
            } else if (button == 1 && Screen.hasShiftDown()) {
                this.drafts.deleteOrArm(i);
                SpellcraftDraw.click();
            }
            return true;
        }
        return false;
    }
}
