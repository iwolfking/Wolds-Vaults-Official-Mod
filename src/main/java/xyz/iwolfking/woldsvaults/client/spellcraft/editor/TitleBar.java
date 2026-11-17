package xyz.iwolfking.woldsvaults.client.spellcraft.editor;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.gear.data.VaultGearData;
import net.minecraft.client.gui.Font;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftDraw;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftNames;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftPalette;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphType;
import xyz.iwolfking.woldsvaults.spellcraft.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.spellcraft.item.StoredSpell;
import xyz.iwolfking.woldsvaults.spellcraft.spell.SpellModel;
import xyz.iwolfking.woldsvaults.spellcraft.spell.SpellcraftRules;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class TitleBar {
    private static final int ARCANA_BAR_W = 72;
    private static final int TITLE_NAME_FALLBACK = 0xFF1E6E1E;

    private final SpellcraftScreen screen;

    TitleBar(SpellcraftScreen screen) {
        this.screen = screen;
    }

    private float committedArcana(@Nullable SpellcraftRules.ItemCheck check, @Nullable SpellDrafts.Draft draft) {
        if (check == null) {
            return 0.0F;
        }
        int editing = draft == null ? Integer.MIN_VALUE : draft.id > 0 ? draft.id : StoredSpell.UNSAVED_ID;
        float total = 0.0F;
        for (Map.Entry<Integer, SpellModel> entry : check.models().entrySet()) {
            if (entry.getKey() != editing && check.charged().contains(entry.getKey())) {
                total += entry.getValue().arcana();
            }
        }
        return total;
    }

    void render(PoseStack pose, SpellcraftConfig config, ItemStack stack, @Nullable SpellcraftRules.ItemCheck check, @Nullable SpellDrafts.Draft draft) {
        Font font = this.screen.font();
        int x0 = 21;
        int x1 = this.screen.width - 21;
        SpellcraftDraw.window(pose, x0, EditorLayout.TITLE_Y, x1, EditorLayout.TITLE_Y + EditorLayout.TITLE_H);
        this.screen.itemRenderer().renderGuiItem(stack, x0 + 4, EditorLayout.TITLE_Y + 3);
        int budget = SpellcraftItemData.budget(stack);
        float committed = committedArcana(check, draft);
        float total = check == null ? 0.0F : check.arcana();
        int depthLimit = Math.min(SpellcraftItemData.depth(stack), config.rules.maxGlyphsPerSpell);
        int glyphs = 0;
        if (draft != null) {
            for (String token : draft.tokens) {
                GlyphType type = GlyphType.byId(token);
                if (type != null && !config.glyph(type).isUnique()) {
                    glyphs++;
                }
            }
        }
        String depth = SpellcraftNames.tr("screen.depth", glyphs, depthLimit);
        int ty = EditorLayout.TITLE_Y + 8;
        int dx = x1 - 6 - font.width(depth);
        font.draw(pose, depth, dx, ty, glyphs > depthLimit ? SpellcraftPalette.BAD_TEXT : SpellcraftPalette.LABEL);
        String num = Math.round(total) + "/" + budget;
        int nx = dx - 8 - font.width(num);
        boolean over = Math.round(total) > budget || (check != null && check.budgetInactive() > 0);
        font.draw(pose, num, nx, ty, over ? SpellcraftPalette.BAD_TEXT : SpellcraftPalette.ARCANA_TEXT);
        int bx = nx - 4 - ARCANA_BAR_W;
        SpellcraftDraw.arcanaBar(pose, bx, ty, ARCANA_BAR_W, 7, committed, total - committed, budget);
        String label = SpellcraftNames.tr("screen.arcana");
        int lx = bx - 4 - font.width(label);
        font.draw(pose, label, lx, ty, SpellcraftPalette.LABEL);
        String level = SpellcraftNames.tr("screen.level", VaultGearData.read(stack).getItemLevel());
        int nameX = x0 + 24;
        int room = lx - 6 - nameX - font.width(level) - 6;
        Component name = stack.getHoverName();
        int color = name.getStyle().getColor() == null ? TITLE_NAME_FALLBACK : 0xFF000000 | name.getStyle().getColor().getValue();
        FormattedText cut = font.substrByWidth(name, Math.max(0, room));
        font.draw(pose, Language.getInstance().getVisualOrder(cut), nameX, ty, color);
        font.draw(pose, level, nameX + font.width(cut) + 6, ty, SpellcraftPalette.DARK);
    }

    @Nullable
    List<Component> tooltipAt(@Nullable SpellcraftRules.ItemCheck check, int mouseX, int mouseY) {
        if (check != null && check.budgetInactive() > 0 && EditorLayout.overTitle(mouseX, mouseY, this.screen.width)) {
            List<Component> lines = new ArrayList<>();
            lines.add(SpellcraftNames.colored(SpellcraftNames.tr("screen.over_budget", check.budgetInactive()), SpellcraftPalette.BAD));
            return lines;
        }
        return null;
    }
}
