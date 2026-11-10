package xyz.iwolfking.woldsvaults.client.spellcraft;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SpecializedSkill;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.skill.tree.AbilityTree;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.augments.FocusAugments;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.augments.spell.GlyphStacking;
import xyz.iwolfking.woldsvaults.augments.spell.SpellcraftRules;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * The spell bar window (skill menu and focus screen): a picker for the default bar and the focus bar, the 16 slots
 * with their key labels, and a Keys button. Slot states: inherited (faded, link marker), augmented (violet frame,
 * corner star), hidden (struck red), blank (dash), unlearned (red X).
 * <p>
 * Clicks: on the default bar a slot takes the host's selected ability (the skill menu's dialog) or, without one, opens
 * a chooser of learned abilities; on a focus bar a slot opens a chooser of the item's spells, Inherit, Blank and
 * abilities. Right-click clears a slot (default: empty, item: inherit); Shift+right-click sets an item slot to Blank.
 */
public final class SpellBarStrip {
    private static final int MANA_COLOR = 0xFF6FA8FF;
    private static final int COOLDOWN_COLOR = 0xFFFFC46A;
    private static final int DAMAGE_COLOR = 0xFFFF7A5C;
    private static final int AREA_COLOR = 0xFF7EE787;
    private static final int VELOCITY_COLOR = 0xFF5FD8E8;
    private static final int DURATION_COLOR = 0xFFF59AD0;
    private static final int EXTRA_COLOR = 0xFFD9C98A;
    public static final int HEIGHT = 24;
    public static final int BAND = 27;
    public static final int PICK = 20;
    public static final int SLOT_PITCH = 18;
    public static final int KEYS_W = 32;
    public static final int KEYS_H = 16;
    public static final float KEY_SCALE = 0.75F;
    private static final int POPUP_ROW = 12;
    private static final int POPUP_MAX_ROWS = 10;
    private static final int DASH = 0xFF6A6A6A;
    private static final int LINK_OUTER = 0xFF3A3A3A;
    private static final int LINK_INNER = 0xFFB0B0B0;
    private static final int HOVER = 0x80FFFFFF;
    private static final int BROKEN = 0x80FF3030;
    private static final int POPUP_BG = 0xF0100010;
    private static final int POPUP_HOVER = 0x405000FF;

    /** What the strip needs from its screen. */
    public interface Host {
        AbilityTree tree();

        /** The default bar view. */
        SpellBarView defaultView();

        /** The focus bar view, or null when there is none to show. */
        @Nullable
        SpellBarView itemView();

        /** The ability group a default-bar click assigns, or null to open the ability chooser instead. */
        @Nullable
        String selectedGroup();

        /** Whether the chooser on a focus bar lists every learned ability (focus screen) or only the selection. */
        boolean chooserListsAbilities();

        void openKeys();
    }

    /** One chooser entry: the value sent for the slot, its label, an ability icon (or none) and its colour. */
    private record Choice(String value, String label, @Nullable String icon, int color) {
    }

    private final Host host;
    private int x0;
    private int y0;
    private int x1;
    private boolean showItem;
    private SpellBarView view;
    private int popupSlot = -1;
    private List<Choice> popup = List.of();
    private int popupScroll;

    public SpellBarStrip(Host host, boolean showItem) {
        this.host = host;
        this.showItem = showItem;
    }

    /** Places the window; (x0, y0) is its top left and x1 its right edge, {@link #HEIGHT} tall. */
    public void layout(int x0, int y0, int x1) {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
    }

    /** The view drawn this frame: the focus bar when it is picked and available, else the default bar. */
    public SpellBarView view() {
        SpellBarView item = this.showItem ? this.host.itemView() : null;
        this.view = item != null ? item : this.host.defaultView();
        return this.view;
    }

    public boolean showingItem() {
        return this.view != null && this.view.isItem();
    }

    /** Whether the player last picked the focus bar (kept while no focus bar is available). */
    public boolean prefersItem() {
        return this.showItem;
    }

    public static int slotX(int x0, int index) {
        return x0 + 4 + 2 * (PICK + 2) + 6 + index * SLOT_PITCH;
    }

    private int pickX(int k) {
        return this.x0 + 4 + k * (PICK + 2);
    }

    private int keysX() {
        return this.x1 - 4 - KEYS_W;
    }

    public void render(PoseStack pose, int mouseX, int mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        SpellBarView view = view();
        SpellBarView item = this.host.itemView();
        RenderSystem.enableBlend();
        SpellcraftGui.window(pose, this.x0, this.y0, this.x1, this.y0 + HEIGHT);
        drawPicker(pose, 0, !view.isItem(), null);
        if (item != null) {
            drawPicker(pose, 1, view.isItem(), item.stack());
        }
        AbilityTree tree = this.host.tree();
        Function<String, String> specOf = SpellcraftRules.specOf(tree);
        int room = (int) ((16 - 1) / KEY_SCALE);
        for (int i = 0; i < SpellcraftItemData.SLOTS; i++) {
            int x = slotX(this.x0, i);
            int y = this.y0 + 4;
            SpellBarView.Slot slot = view.slot(i);
            drawSlot(pose, view, slot, specOf, x, y);
            if (inside(mouseX, mouseY, x, y, 16, 16) && this.popupSlot < 0) {
                GuiComponent.fill(pose, x, y, x + 16, y + 16, HOVER);
            }
            String key = SpellcraftGui.keyLabel(i, font, room);
            if (!key.isEmpty()) {
                pose.pushPose();
                pose.translate(0.0, 0.0, 200.0);
                SpellcraftGui.smallText(pose, font, key, x + 1, y + 1, slot.filled() || slot.state() == SpellBarView.State.HIDDEN
                        ? SpellcraftGui.WHITE : SpellcraftGui.MUTED, KEY_SCALE);
                pose.popPose();
            }
        }
        button(pose, font, keysX(), this.y0 + 4, KEYS_W, KEYS_H, SpellcraftGui.tr("bar.keys"),
                inside(mouseX, mouseY, keysX(), this.y0 + 4, KEYS_W, KEYS_H));
    }

    private void drawPicker(PoseStack pose, int k, boolean selected, @Nullable ItemStack stack) {
        int px = pickX(k);
        int py = this.y0 + 2;
        if (selected) {
            SpellcraftGui.insetGrey(pose, px, py, px + PICK, py + PICK);
        } else {
            SpellcraftGui.insetDarkGrey(pose, px, py, px + PICK, py + PICK);
        }
        if (stack == null) {
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, selected ? 1.0F : 0.55F);
            ScreenTextures.TAB_ICON_ABILITIES.blit(pose, px + 2, py + 2, 0, 16, 16);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        } else {
            Minecraft.getInstance().getItemRenderer().renderGuiItem(stack, px + 2, py + 2);
        }
        if (selected) {
            SpellcraftGui.ring(pose, px, py, px + PICK, py + PICK, stack == null ? SpellcraftGui.WHITE : SpellcraftGui.ARCANE_GLOW);
        }
    }

    private void drawSlot(PoseStack pose, SpellBarView view, SpellBarView.Slot slot, Function<String, String> specOf, int x, int y) {
        SpellcraftGui.slot(pose, x, y, false);
        String spec = slot.group().isEmpty() ? null : specOf.apply(slot.group());
        String icon = spec != null ? spec : slot.group();
        switch (slot.state()) {
            case ABILITY -> {
                boolean faded = view.isItem() && slot.inherited();
                SpellcraftGui.ability(pose, icon, x, y, 16, faded ? 0.45F : slot.unlearned() ? 0.4F : 1.0F);
                if (faded) {
                    GuiComponent.fill(pose, x + 12, y + 12, x + 16, y + 16, LINK_OUTER);
                    GuiComponent.fill(pose, x + 13, y + 13, x + 15, y + 15, LINK_INNER);
                }
                if (slot.unlearned()) {
                    SpellcraftGui.redX(pose, x, y);
                }
            }
            case SPELL -> {
                SpellcraftGui.ability(pose, icon, x, y, 16, 1.0F);
                if (slot.broken()) {
                    GuiComponent.fill(pose, x, y, x + 16, y + 16, BROKEN);
                }
                SpellcraftGui.ring(pose, x - 1, y - 1, x + 17, y + 17, SpellcraftGui.ARCANE_GLOW);
                SpellcraftGui.augmentStar(pose, x, y);
                if (slot.unlearned()) {
                    SpellcraftGui.redX(pose, x, y);
                }
            }
            case HIDDEN -> {
                SpellcraftGui.ability(pose, icon, x, y, 16, 0.45F);
                if (slot.inherited()) {
                    GuiComponent.fill(pose, x + 12, y + 12, x + 16, y + 16, LINK_OUTER);
                    GuiComponent.fill(pose, x + 13, y + 13, x + 15, y + 15, LINK_INNER);
                }
                SpellcraftGui.strike(pose, x, y);
            }
            case BLANK -> GuiComponent.fill(pose, x + 3, y + 7, x + 13, y + 9, DASH);
            case EMPTY -> {
            }
        }
    }

    /** A vanilla button at any height: the texture's top rows plus its bottom edge row, label centred. */
    public static void button(PoseStack pose, Font font, int x, int y, int w, int h, String label, boolean hovered) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, AbstractWidget.WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        int v = 46 + (hovered ? 2 : 1) * 20;
        int half = w / 2;
        GuiComponent.blit(pose, x, y, 0, v, half, h - 1, 256, 256);
        GuiComponent.blit(pose, x + half, y, 200 - (w - half), v, w - half, h - 1, 256, 256);
        GuiComponent.blit(pose, x, y + h - 1, 0, v + 19, half, 1, 256, 256);
        GuiComponent.blit(pose, x + half, y + h - 1, 200 - (w - half), v + 19, w - half, 1, 256, 256);
        String text = font.plainSubstrByWidth(label, w - 4);
        font.drawShadow(pose, text, x + (w - font.width(text)) / 2.0F, y + (h - 8) / 2.0F, hovered ? 0xFFFFFFA0 : 0xFFE0E0E0);
    }

    /** The chooser and the hover tooltip, drawn above everything. */
    public void renderOverlay(PoseStack pose, Screen screen, int mouseX, int mouseY) {
        if (this.view == null) {
            return;
        }
        pose.pushPose();
        pose.translate(0.0, 0.0, 400.0);
        if (this.popupSlot >= 0) {
            renderPopup(pose, screen, mouseX, mouseY);
            pose.popPose();
            return;
        }
        pose.popPose();
        List<Component> tooltip = tooltip(mouseX, mouseY);
        if (!tooltip.isEmpty()) {
            screen.renderComponentTooltip(pose, tooltip, mouseX, mouseY);
        }
    }

    private List<Component> tooltip(int mouseX, int mouseY) {
        List<Component> lines = new ArrayList<>();
        if (inside(mouseX, mouseY, pickX(0), this.y0 + 2, PICK, PICK)) {
            lines.add(new TextComponent(SpellcraftGui.tr("bar.default")));
            return lines;
        }
        SpellBarView item = this.host.itemView();
        if (item != null && inside(mouseX, mouseY, pickX(1), this.y0 + 2, PICK, PICK)) {
            lines.add(new TextComponent(SpellcraftGui.tr("bar.item", item.stack().getHoverName().getString()))
                    .withStyle(style -> style.withColor(TextColor.fromRgb(SpellcraftGui.ARCANE_GLOW & 0xFFFFFF))));
            return lines;
        }
        if (inside(mouseX, mouseY, keysX(), this.y0 + 4, KEYS_W, KEYS_H)) {
            lines.add(new TextComponent(SpellcraftGui.tr("bar.keys_hint")));
            return lines;
        }
        for (int i = 0; i < SpellcraftItemData.SLOTS; i++) {
            if (inside(mouseX, mouseY, slotX(this.x0, i), this.y0 + 4, 16, 16)) {
                return slotTooltip(this.view, i);
            }
        }
        return lines;
    }

    private List<Component> slotTooltip(SpellBarView view, int index) {
        List<Component> lines = new ArrayList<>();
        AbilityTree tree = this.host.tree();
        FocusAugmentsConfig config = FocusAugments.config();
        SpellBarView.Slot slot = view.slot(index);
        switch (slot.state()) {
            case ABILITY -> {
                lines.add(colored(SpellcraftGui.abilityName(tree, slot.group()) + tierSuffix(tree, slot.group()), SpellcraftGui.NAME_GOLD));
                ClientAbilityStats.Step stats = ClientAbilityStats.plain(tree, slot.group());
                if (stats != null) {
                    lines.addAll(statLines(stats, ""));
                }
                ClientSpellStats.Numbers numbers = ClientSpellStats.plain(tree, slot.group());
                if (numbers != null) {
                    lines.addAll(numberLines(numbers));
                }
                if (view.isItem() && slot.inherited()) {
                    lines.add(colored(SpellcraftGui.tr("bar.inherited"), SpellcraftGui.GREY));
                }
            }
            case SPELL -> {
                lines.add(colored(slot.spellName(), SpellcraftGui.ARCANE_GLOW));
                List<String> parts = new ArrayList<>();
                for (String group : slot.groups()) {
                    parts.add(SpellcraftGui.groupName(tree, group) + tierSuffix(tree, group));
                }
                lines.add(colored(SpellcraftGui.tr("bar.augmented", String.join(" + ", parts)), SpellcraftGui.GREY));
                if (config != null && slot.model() != null) {
                    ClientAbilityStats.SpellStats stats = ClientAbilityStats.spell(config, tree, slot.model());
                    if (stats != null) {
                        lines.addAll(spellLines(tree, stats));
                    }
                    ClientSpellStats.Numbers numbers = ClientSpellStats.spell(config, tree, slot.model());
                    if (numbers != null) {
                        lines.addAll(numberLines(numbers));
                    }
                }
                if (slot.broken()) {
                    lines.add(colored(SpellcraftGui.tr("bar.broken"), SpellcraftGui.BAD));
                    if (slot.model() != null && !slot.model().errors().isEmpty()) {
                        lines.add(colored(slot.model().errors().get(0), SpellcraftGui.BAD));
                    }
                }
                if (!view.itemErrors().isEmpty()) {
                    lines.add(colored(SpellcraftGui.tr("bar.item_errors"), SpellcraftGui.BAD));
                    lines.add(colored(view.itemErrors().get(0), SpellcraftGui.BAD));
                }
            }
            case HIDDEN -> {
                int by = view.hiddenBy(index);
                String name = by < 0 ? "?" : view.slot(by).state() == SpellBarView.State.SPELL ? view.slot(by).spellName()
                        : SpellcraftGui.abilityName(tree, view.slot(by).group());
                lines.add(colored(SpellcraftGui.tr("bar.hidden", name, by + 1, SpellcraftGui.groupName(tree, slot.group())), SpellcraftGui.BAD));
            }
            case BLANK -> lines.add(colored(SpellcraftGui.tr("bar.blank"), SpellcraftGui.GREY));
            case EMPTY -> lines.add(colored(SpellcraftGui.tr(view.isItem() ? "bar.inherited_empty" : "bar.empty"), SpellcraftGui.GREY));
        }
        if (slot.unlearned()) {
            for (String group : slot.groups()) {
                if (!SpellBarView.learned(tree, group)) {
                    lines.add(colored(SpellcraftGui.tr("bar.unlearned", SpellcraftGui.groupName(tree, group)), SpellcraftGui.BAD));
                }
            }
        }
        String key = SpellcraftGui.keyName(index);
        String keyLine = key.isEmpty() ? SpellcraftGui.tr("bar.no_key", index + 1) : SpellcraftGui.tr("bar.key", index + 1, key);
        lines.add(colored(keyLine, SpellcraftGui.GREY));
        String hint;
        if (view.isItem()) {
            hint = SpellcraftGui.tr("bar.hint_item");
        } else if (this.host.selectedGroup() != null) {
            hint = SpellcraftGui.tr("bar.hint_default", SpellcraftGui.abilityName(tree, this.host.selectedGroup()));
        } else {
            hint = SpellcraftGui.tr("bar.hint_choose");
        }
        lines.add(new TextComponent(hint).withStyle(ChatFormatting.DARK_GRAY));
        return lines;
    }

    /** Mana (blue), cooldown (gold) and spell haste (violet) lines, the label grey and the numbers coloured. */
    private static List<Component> numberLines(ClientSpellStats.Numbers numbers) {
        List<Component> lines = new ArrayList<>();
        if (numbers.hasMana()) {
            String before = ClientAbilityStats.compact(Math.round(numbers.manaBefore()));
            String after = ClientAbilityStats.compact(Math.round(numbers.manaAfter()));
            String mana = before.equals(after) ? after : SpellcraftGui.tr("fmt.arrow", before, after);
            lines.add(labelled(SpellcraftGui.tr("bar.mana_label"), mana, MANA_COLOR));
        }
        String cooldown = numbers.cooldownBefore() == numbers.cooldownAfter()
                ? SpellcraftGui.tr("fmt.seconds", SpellcraftGui.seconds(numbers.cooldownAfter()))
                : SpellcraftGui.tr("fmt.arrow", SpellcraftGui.tr("fmt.seconds", SpellcraftGui.seconds(numbers.cooldownBefore())),
                SpellcraftGui.tr("fmt.seconds", SpellcraftGui.seconds(numbers.cooldownAfter())));
        lines.add(labelled(SpellcraftGui.tr("bar.cooldown_label"), cooldown, COOLDOWN_COLOR));
        Integer haste = ClientSpellStats.hastePercent();
        if (haste != null && haste != 0) {
            lines.add(labelled(SpellcraftGui.tr("bar.haste_label"), (haste > 0 ? "+" : "") + haste + "%", SpellcraftGui.ARCANE_GLOW));
        }
        return lines;
    }

    /**
     * A spell's stat lines: with one row, that step's lines; with more, each ability step and Bundled Spell orb under its
     * own header, indented by payload depth, then the whole press's damage when several steps deal damage.
     */
    private static List<Component> spellLines(AbilityTree tree, ClientAbilityStats.SpellStats stats) {
        List<Component> lines = new ArrayList<>();
        if (stats.rows().size() == 1 && stats.rows().get(0) instanceof ClientAbilityStats.Step step) {
            lines.addAll(statLines(step, ""));
            return lines;
        }
        for (ClientAbilityStats.Row row : stats.rows()) {
            String indent = "  ".repeat(row.depth());
            if (row instanceof ClientAbilityStats.Step step) {
                lines.add(colored(indent + SpellcraftGui.groupName(tree, step.group()), SpellcraftGui.NAME_GOLD));
                lines.addAll(statLines(step, indent + "  "));
            } else if (row instanceof ClientAbilityStats.Orb orb) {
                lines.add(colored(indent + SpellcraftGui.tr("bar.stat.orb_step"), SpellcraftGui.ARCANE_GLOW));
                String inner = indent + "  ";
                if (orb.copies() > 1) {
                    lines.add(labelled(inner + SpellcraftGui.tr("bar.stat.orbs"), String.valueOf(orb.copies()), EXTRA_COLOR));
                }
                lines.add(labelled(inner + SpellcraftGui.tr("bar.stat.velocity"),
                        SpellcraftGui.tr("fmt.blocks_per_second", ClientAbilityStats.compact(orb.velocity())), VELOCITY_COLOR));
                lines.add(labelled(inner + SpellcraftGui.tr("bar.stat.flight"),
                        SpellcraftGui.tr("fmt.seconds", SpellcraftGui.trim(orb.flightSeconds())), DURATION_COLOR));
                glyphLine(orb.glyphs(), inner, lines);
            }
        }
        if (stats.total() != null) {
            lines.add(labelled(SpellcraftGui.tr("bar.stat.spell_total"), ClientAbilityStats.hit(stats.total()), DAMAGE_COLOR));
        }
        return lines;
    }

    /** One step's stats, one per line: damage (and total, splinter, splash), radius, velocity, duration, then VH's other numbers. */
    private static List<Component> statLines(ClientAbilityStats.Step step, String indent) {
        List<Component> lines = new ArrayList<>();
        if (step.hit() != null) {
            String hit = ClientAbilityStats.hit(step.hit());
            if (step.charged() != null) {
                hit = SpellcraftGui.tr("fmt.charged", hit, ClientAbilityStats.hit(step.charged()));
            }
            lines.add(labelled(indent + SpellcraftGui.tr("bar.stat.damage"), hit, DAMAGE_COLOR));
        }
        if (step.total() != null) {
            String total = ClientAbilityStats.hit(step.total());
            int echo = Math.round(step.echoPower() * 100.0F);
            if (step.echoPower() > 0.0F) {
                total = step.casts() > 1 ? SpellcraftGui.tr("fmt.casts_echo", total, step.casts(), echo) : SpellcraftGui.tr("fmt.echo", total, echo);
            } else {
                total = SpellcraftGui.tr("fmt.casts", total, step.casts());
            }
            if (step.chargedTotal() != null) {
                total = SpellcraftGui.tr("fmt.charged", total, ClientAbilityStats.hit(step.chargedTotal()));
            }
            lines.add(labelled(indent + SpellcraftGui.tr("bar.stat.total"), total, DAMAGE_COLOR));
        }
        if (step.splinter() != null) {
            lines.add(labelled(indent + SpellcraftGui.tr("bar.stat.splinter"), ClientAbilityStats.hit(step.splinter()), DAMAGE_COLOR));
        }
        if (step.splash() != null) {
            lines.add(labelled(indent + SpellcraftGui.tr("bar.stat.splash"), ClientAbilityStats.hit(step.splash()), DAMAGE_COLOR));
        }
        if (step.radius() >= 0.0F) {
            lines.add(labelled(indent + SpellcraftGui.tr("bar.stat.radius"),
                    SpellcraftGui.tr("fmt.blocks", ClientAbilityStats.compact(step.radius())), AREA_COLOR));
        }
        if (step.splashRadius() >= 0.0F) {
            lines.add(labelled(indent + SpellcraftGui.tr("bar.stat.splash_radius"),
                    SpellcraftGui.tr("fmt.blocks", ClientAbilityStats.compact(step.splashRadius())), AREA_COLOR));
        }
        if (step.velocity() >= 0.0F) {
            lines.add(labelled(indent + SpellcraftGui.tr("bar.stat.velocity"),
                    SpellcraftGui.tr("fmt.blocks_per_second", ClientAbilityStats.compact(step.velocity())), VELOCITY_COLOR));
        }
        if (step.durationTicks() >= 0) {
            lines.add(labelled(indent + SpellcraftGui.tr("bar.stat.duration"),
                    SpellcraftGui.tr("fmt.seconds", SpellcraftGui.seconds(step.durationTicks())), DURATION_COLOR));
        }
        for (ClientAbilityStats.Extra extra : step.extras()) {
            lines.add(labelled(indent + SpellcraftGui.tr("bar.stat." + extra.key()), extra.value(), EXTRA_COLOR));
        }
        if (step.glyphs() != null) {
            glyphLine(step.glyphs(), indent, lines);
        }
        return lines;
    }

    /** The step's stacked glyph stats ("+48% dmg, +22% area; 0.86x area"), when any glyph moves a stat. */
    private static void glyphLine(GlyphStacking.Pool pool, String indent, List<Component> lines) {
        String summary = SpellcraftGui.poolSummary(pool);
        if (!summary.isEmpty()) {
            lines.add(labelled(indent + SpellcraftGui.tr("bar.stat.glyphs"), summary, SpellcraftGui.ARCANE_GLOW));
        }
    }

    private static Component labelled(String label, String value, int color) {
        return new TextComponent(label).withStyle(style -> style.withColor(TextColor.fromRgb(SpellcraftGui.GREY & 0xFFFFFF)))
                .append(new TextComponent(value).withStyle(style -> style.withColor(TextColor.fromRgb(color & 0xFFFFFF))));
    }

    private static String tierSuffix(AbilityTree tree, String group) {
        Skill skill = tree.getForId(group).orElse(null);
        if (skill instanceof SpecializedSkill specialized && specialized.getSpecialization() instanceof TieredSkill tiered && tiered.isUnlocked()) {
            return " " + tiered.getActualTier();
        }
        return "";
    }

    private static Component colored(String text, int color) {
        return new TextComponent(text).withStyle(style -> style.withColor(TextColor.fromRgb(color & 0xFFFFFF)));
    }

    private void renderPopup(PoseStack pose, Screen screen, int mouseX, int mouseY) {
        Font font = Minecraft.getInstance().font;
        int[] box = popupBox(screen);
        int px = box[0];
        int py = box[1];
        int w = box[2];
        int rows = box[3];
        GuiComponent.fill(pose, px - 2, py - 2, px + w + 2, py + rows * POPUP_ROW + 2, POPUP_BG);
        SpellcraftGui.ring(pose, px - 2, py - 2, px + w + 2, py + rows * POPUP_ROW + 2, SpellcraftGui.ARCANE_LINE);
        for (int r = 0; r < rows; r++) {
            Choice choice = this.popup.get(r + this.popupScroll);
            int ry = py + r * POPUP_ROW;
            if (inside(mouseX, mouseY, px, ry, w, POPUP_ROW)) {
                GuiComponent.fill(pose, px, ry, px + w, ry + POPUP_ROW, POPUP_HOVER);
            }
            if (choice.icon() != null) {
                SpellcraftGui.ability(pose, choice.icon(), px + 2, ry + 2, 8, 1.0F);
            }
            font.drawShadow(pose, font.plainSubstrByWidth(choice.label(), w - 14), px + 12, ry + 2, choice.color());
        }
        if (this.popup.size() > rows) {
            String more = SpellcraftGui.tr("bar.scroll", this.popupScroll + 1, this.popupScroll + rows, this.popup.size());
            SpellcraftGui.smallText(pose, font, more, px, py - 10, SpellcraftGui.MUTED, KEY_SCALE);
        }
    }

    /** The chooser's left, top, width and visible rows: above the slot, inside the screen. */
    private int[] popupBox(Screen screen) {
        Font font = Minecraft.getInstance().font;
        int w = 60;
        for (Choice choice : this.popup) {
            w = Math.max(w, font.width(choice.label()) + 16);
        }
        w = Math.min(w, screen.width - 8);
        int top = 14;
        int rows = Math.min(this.popup.size(), Math.min(POPUP_MAX_ROWS, (this.y0 - 4 - top) / POPUP_ROW));
        int x = Math.max(4, Math.min(screen.width - w - 4, slotX(this.x0, this.popupSlot) - 2));
        int y = this.y0 - 4 - rows * POPUP_ROW;
        return new int[]{x, y, w, Math.max(1, rows)};
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.view == null) {
            return false;
        }
        Screen screen = Minecraft.getInstance().screen;
        if (this.popupSlot >= 0 && screen != null) {
            int[] box = popupBox(screen);
            if (inside(mouseX, mouseY, box[0], box[1], box[2], box[3] * POPUP_ROW) && button == 0) {
                int row = (int) ((mouseY - box[1]) / POPUP_ROW) + this.popupScroll;
                if (row >= 0 && row < this.popup.size()) {
                    this.view.send(this.popupSlot, this.popup.get(row).value());
                    click();
                }
            }
            this.popupSlot = -1;
            return true;
        }
        if (!inside(mouseX, mouseY, this.x0, this.y0, this.x1 - this.x0, HEIGHT)) {
            return false;
        }
        if (inside(mouseX, mouseY, pickX(0), this.y0 + 2, PICK, PICK)) {
            this.showItem = false;
            click();
            return true;
        }
        if (this.host.itemView() != null && inside(mouseX, mouseY, pickX(1), this.y0 + 2, PICK, PICK)) {
            this.showItem = true;
            click();
            return true;
        }
        if (inside(mouseX, mouseY, keysX(), this.y0 + 4, KEYS_W, KEYS_H)) {
            click();
            this.host.openKeys();
            return true;
        }
        for (int i = 0; i < SpellcraftItemData.SLOTS; i++) {
            if (inside(mouseX, mouseY, slotX(this.x0, i), this.y0 + 4, 16, 16)) {
                clickSlot(i, button);
                return true;
            }
        }
        return true;
    }

    private void clickSlot(int index, int button) {
        AbilityTree tree = this.host.tree();
        if (button == 1) {
            this.view.send(index, this.view.isItem() && Screen.hasShiftDown() ? SpellcraftItemData.BLANK : "");
            click();
            return;
        }
        if (button != 0) {
            return;
        }
        String selected = this.host.selectedGroup();
        if (!this.view.isItem() && selected != null) {
            if (SpellBarView.learned(tree, selected)) {
                this.view.send(index, selected);
                click();
            }
            return;
        }
        List<Choice> choices = new ArrayList<>();
        if (this.view.isItem()) {
            for (SpellcraftItemData.Spell spell : SpellcraftItemData.spells(this.view.stack())) {
                choices.add(new Choice(SpellcraftItemData.SPELL_PREFIX + spell.id(), spell.name(), spell.trigger(), SpellcraftGui.ARCANE_GLOW));
            }
            choices.add(new Choice(SpellcraftItemData.INHERIT, SpellcraftGui.tr("picker.inherit"), null, SpellcraftGui.GREY));
            choices.add(new Choice(SpellcraftItemData.BLANK, SpellcraftGui.tr("picker.blank"), null, SpellcraftGui.GREY));
            if (selected != null && SpellBarView.learned(tree, selected)) {
                choices.add(0, new Choice(SpellcraftItemData.ABILITY_PREFIX + selected, SpellcraftGui.abilityName(tree, selected), selected,
                        SpellcraftGui.NAME_GOLD));
            }
        } else {
            choices.add(new Choice("", SpellcraftGui.tr("picker.empty"), null, SpellcraftGui.GREY));
        }
        if (!this.view.isItem() || this.host.chooserListsAbilities()) {
            String prefix = this.view.isItem() ? SpellcraftItemData.ABILITY_PREFIX : "";
            for (Skill skill : tree.skills) {
                if (skill instanceof SpecializedSkill specialized && specialized.isUnlocked()) {
                    choices.add(new Choice(prefix + specialized.getId(), SpellcraftGui.abilityName(tree, specialized.getId()), specialized.getId(),
                            SpellcraftGui.NAME_GOLD));
                }
            }
        }
        this.popup = choices;
        this.popupSlot = index;
        this.popupScroll = 0;
        click();
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        Screen screen = Minecraft.getInstance().screen;
        if (this.popupSlot < 0 || screen == null) {
            return false;
        }
        int rows = popupBox(screen)[3];
        int max = Math.max(0, this.popup.size() - rows);
        this.popupScroll = Math.max(0, Math.min(max, this.popupScroll + (delta < 0 ? 1 : -1)));
        return true;
    }

    public boolean popupOpen() {
        return this.popupSlot >= 0;
    }

    public void closePopup() {
        this.popupSlot = -1;
    }

    private static void click() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    public static boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }
}
