package xyz.iwolfking.woldsvaults.client.spellcraft;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.data.ClientAbilityData;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SpecializedSkill;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.skill.tree.AbilityTree;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.augments.AugmentType;
import xyz.iwolfking.woldsvaults.augments.FocusAugments;
import xyz.iwolfking.woldsvaults.augments.cast.CastModifiers;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftRoller;
import xyz.iwolfking.woldsvaults.augments.spell.SpellModel;
import xyz.iwolfking.woldsvaults.augments.spell.SpellcraftRules;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.network.packets.SpellEditMessage;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * The focus spellcraft screen, opened by right-clicking a focus or wand in the inventory. Bands: the title (item,
 * arcana bar, depth); the glyph pouch (Glyphs / Abilities) beside the spell tabs, name field, weave and spell stats;
 * the item's spell bar at the bottom.
 * <p>
 * Each spell is edited as a local draft. Clicking a pouch glyph or ability inserts it after the selected weave cell
 * (the end by default; an ability goes first when the spell has no trigger yet); right-clicking a cell removes it.
 * A draft is sent to the server a moment after its last change, and only when the whole item passes the strict
 * spellcraft check with it; otherwise the stats panel lists what is wrong. Shift+right-click a spell tab twice to
 * delete that spell.
 */
public class FocusSpellcraftScreen extends Screen implements SpellBarStrip.Host {
    public static final int PAD = 4;
    public static final int TITLE_Y = 4;
    public static final int TITLE_H = 22;
    public static final int POUCH_W = 118;
    public static final int ROW = 21;
    public static final int CELL = 22;
    public static final int CELL_PITCH_Y = 26;
    public static final int TAB = 20;
    public static final int WEAVE_TOP = 11;
    public static final int WEAVE_ROWS = 2;
    public static final int WEAVE_H = WEAVE_TOP + WEAVE_ROWS * CELL_PITCH_Y + 2;
    public static final int STAT_PITCH = 10;
    public static final int ARCANA_BAR_W = 72;
    public static final int SCROLL_W = 3;
    public static final int NAME_MIN_W = 60;
    private static final int SAVE_DELAY = 8;
    private static final int SERVER_WAIT = 60;
    private static final int DELETE_WINDOW = 60;
    private static final int TITLE_NAME_FALLBACK = 0xFF1E6E1E;
    private static final int MANA_TEXT = 0xFF1F4FA0;
    private static final int COOLDOWN_TEXT = 0xFF6A3A00;
    private static final int HIDES_TEXT = 0xFF8A1A1A;
    private static final int SAVED_TEXT = 0xFF1E7A1E;
    private static final int NAME_TEXT = 0xFFE0D0FF;
    private static final int PAYLOAD = 0xFFB46CF0;
    private static final int THREAD_DIM = 0xFF3A2A55;
    private static final int PLUS = 0xFF8A8A8A;
    private static final int TAB_PLUS = 0xFFE0E0E0;
    private static final int DELETE_TINT = 0x80FF3030;
    private static final int DIVIDER = 0xFF8B8B8B;
    private static final int SCROLL_TRACK = 0xFF2A2A2A;
    private static final int SCROLL_THUMB = 0xFF8B8B8B;
    private static final int END = Integer.MAX_VALUE;

    /** A spell being edited: the stored id (0 until the server stores a new spell), its name and tokens. */
    private static final class Draft {
        int id;
        String name;
        List<String> tokens;
        boolean dirty;
        boolean pendingNew;
        int sentAt = -1;
        Set<Integer> knownIds = Set.of();

        Draft(int id, String name, List<String> tokens) {
            this.id = id;
            this.name = name;
            this.tokens = new ArrayList<>(tokens);
        }
    }

    @Nullable
    private final Screen parent;
    private final int inventorySlot;
    private final List<Draft> drafts = new ArrayList<>();
    private final SpellBarStrip strip;
    private int selected;
    private int insertAt = END;
    private boolean abilitiesTab;
    private int pouchScroll;
    private int weaveScroll;
    private int saveIn = -1;
    private int ticks;
    private int deleteArmed = -1;
    private int deleteArmedAt;
    private boolean loadingName;
    private EditBox nameBox;
    private SpellBarView itemView;
    private SpellBarView defaultView;

    public FocusSpellcraftScreen(@Nullable Screen parent, int inventorySlot) {
        super(new TextComponent(SpellcraftGui.tr("screen.title")));
        this.parent = parent;
        this.inventorySlot = inventorySlot;
        this.strip = new SpellBarStrip(this, true);
        for (SpellcraftItemData.Spell spell : SpellcraftItemData.spells(stack())) {
            this.drafts.add(new Draft(spell.id(), spell.name(), spell.tokens()));
        }
        if (this.drafts.isEmpty()) {
            this.drafts.add(new Draft(0, "", List.of()));
        }
    }

    private ItemStack stack() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || this.inventorySlot < 0 || this.inventorySlot >= minecraft.player.getInventory().getContainerSize()) {
            return ItemStack.EMPTY;
        }
        return minecraft.player.getInventory().getItem(this.inventorySlot);
    }

    private boolean stackValid() {
        ItemStack stack = stack();
        return SpellcraftRoller.canCarry(stack) && SpellcraftItemData.has(stack);
    }

    @Nullable
    private Draft draft() {
        return this.selected >= 0 && this.selected < this.drafts.size() ? this.drafts.get(this.selected) : null;
    }

    private int bodyTop() {
        return TITLE_Y + TITLE_H + 3;
    }

    private int bodyBottom() {
        return this.height - 21 - SpellBarStrip.HEIGHT - 3;
    }

    private int rightX0() {
        return 21 + POUCH_W + 3;
    }

    private int weaveTop() {
        return bodyTop() + 4 + TAB + 3;
    }

    private int maxTabs() {
        FocusAugmentsConfig config = FocusAugments.config();
        int byTable = config == null ? this.drafts.size() : config.rules.maxSpellsPerItem;
        int byRoom = (this.width - 21 - PAD - NAME_MIN_W - rightX0() - PAD) / (TAB + 2);
        return Math.max(1, Math.min(byTable, byRoom - 1));
    }

    private int tabCount() {
        return Math.min(this.drafts.size(), maxTabs()) + (this.drafts.size() < maxTabs() ? 1 : 0);
    }

    @Override
    protected void init() {
        FocusAugmentsConfig config = FocusAugments.config();
        int nx = nameX();
        this.nameBox = new EditBox(this.font, nx + 4, bodyTop() + 4 + 6, this.width - 21 - PAD - nx - 8, 10, new TextComponent(""));
        this.nameBox.setBordered(false);
        this.nameBox.setTextColor(NAME_TEXT);
        if (config != null) {
            this.nameBox.setMaxLength(config.rules.maxNameLength);
        }
        this.nameBox.setResponder(value -> {
            Draft draft = draft();
            if (!this.loadingName && draft != null) {
                draft.name = value;
                markDirty(draft);
            }
        });
        loadName();
        this.addWidget(this.nameBox);
        this.strip.layout(21, this.height - 21 - SpellBarStrip.HEIGHT, this.width - 21);
    }

    private int nameX() {
        return rightX0() + PAD + tabCount() * (TAB + 2) + 2;
    }

    private void loadName() {
        Draft draft = draft();
        this.loadingName = true;
        this.nameBox.setValue(draft == null ? "" : draft.name);
        this.loadingName = false;
    }

    private void layoutName() {
        int nx = nameX();
        this.nameBox.x = nx + 4;
        this.nameBox.setWidth(Math.max(10, this.width - 21 - PAD - nx - 8));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        this.ticks++;
        if (!stackValid()) {
            onClose();
            return;
        }
        this.nameBox.tick();
        if (this.saveIn > 0 && --this.saveIn == 0) {
            saveDirty();
        }
        adoptNewSpells();
        if (this.deleteArmed >= 0 && this.ticks - this.deleteArmedAt > DELETE_WINDOW) {
            this.deleteArmed = -1;
        }
    }

    private void markDirty(Draft draft) {
        draft.dirty = true;
        draft.sentAt = -1;
        this.saveIn = SAVE_DELAY;
    }

    /** The name the server will store for a draft: trimmed, cut to the table's limit, the trigger's name when blank. */
    private String storedName(Draft draft) {
        String name = draft.name.strip();
        if (name.isEmpty()) {
            String trigger = draft.tokens.isEmpty() || AugmentType.byId(draft.tokens.get(0)) != null ? "" : draft.tokens.get(0);
            name = trigger.isEmpty() ? SpellcraftGui.tr("screen.new_spell") : SpellcraftGui.groupName(tree(), trigger);
        }
        FocusAugmentsConfig config = FocusAugments.config();
        return config != null && name.length() > config.rules.maxNameLength ? name.substring(0, config.rules.maxNameLength) : name;
    }

    private List<SpellcraftItemData.Spell> withDraft(Draft draft) {
        int checkId = draft.id > 0 ? draft.id : Integer.MAX_VALUE;
        return SpellcraftRules.withEdit(stack(), checkId, new SpellcraftItemData.Spell(checkId, storedName(draft), List.copyOf(draft.tokens)));
    }

    @Nullable
    private SpellcraftRules.ItemCheck check(Draft draft) {
        FocusAugmentsConfig config = FocusAugments.config();
        if (config == null) {
            return null;
        }
        return SpellcraftRules.check(config, stack(), withDraft(draft), SpellcraftRules.specOf(tree()), true);
    }

    private boolean matchesStored(Draft draft) {
        if (draft.id <= 0) {
            return false;
        }
        SpellcraftItemData.Spell stored = SpellcraftItemData.spell(stack(), draft.id).orElse(null);
        return stored != null && stored.name().equals(storedName(draft)) && stored.tokens().equals(draft.tokens);
    }

    private void saveDirty() {
        for (Draft draft : this.drafts) {
            if (!draft.dirty || draft.pendingNew || matchesStored(draft)) {
                draft.dirty = draft.dirty && draft.pendingNew;
                continue;
            }
            SpellcraftRules.ItemCheck check = check(draft);
            if (check == null || !check.isValid()) {
                continue;
            }
            ModNetwork.sendToServer(new SpellEditMessage(this.inventorySlot, Math.max(0, draft.id), storedName(draft), List.copyOf(draft.tokens), false));
            draft.dirty = false;
            draft.sentAt = this.ticks;
            if (draft.id <= 0) {
                draft.pendingNew = true;
                Set<Integer> ids = new HashSet<>();
                SpellcraftItemData.spells(stack()).forEach(spell -> ids.add(spell.id()));
                draft.knownIds = ids;
            }
        }
    }

    private void adoptNewSpells() {
        Set<Integer> claimed = new HashSet<>();
        this.drafts.forEach(draft -> claimed.add(draft.id));
        for (Draft draft : this.drafts) {
            if (!draft.pendingNew) {
                continue;
            }
            for (SpellcraftItemData.Spell spell : SpellcraftItemData.spells(stack())) {
                if (!draft.knownIds.contains(spell.id()) && !claimed.contains(spell.id())) {
                    draft.id = spell.id();
                    draft.pendingNew = false;
                    claimed.add(spell.id());
                    if (draft.dirty) {
                        this.saveIn = SAVE_DELAY;
                    }
                    break;
                }
            }
            if (draft.pendingNew && this.ticks - draft.sentAt > SERVER_WAIT) {
                draft.pendingNew = false;
                WoldsVaults.LOGGER.error("Spellcraft screen: the server did not store new spell '{}' (inventory slot {}); left as an unsaved draft",
                        draft.name, this.inventorySlot);
            }
        }
    }

    @Override
    public void onClose() {
        if (this.saveIn > 0 && stackValid()) {
            saveDirty();
        }
        Minecraft.getInstance().setScreen(this.parent);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.nameBox.isFocused()) {
            if (keyCode == InputConstants.KEY_ESCAPE || keyCode == InputConstants.KEY_RETURN) {
                this.nameBox.setFocus(false);
                return true;
            }
            this.nameBox.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }
        if (Minecraft.getInstance().options.keyInventory.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode))) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return this.nameBox.isFocused() && this.nameBox.charTyped(codePoint, modifiers);
    }

    @Override
    public AbilityTree tree() {
        return ClientAbilityData.getTree();
    }

    @Override
    public SpellBarView defaultView() {
        return this.defaultView == null ? SpellBarView.ofDefaults(tree()) : this.defaultView;
    }

    @Nullable
    @Override
    public SpellBarView itemView() {
        return this.itemView;
    }

    @Nullable
    @Override
    public String selectedGroup() {
        return null;
    }

    @Override
    public boolean chooserListsAbilities() {
        return true;
    }

    @Override
    public void openKeys() {
        Minecraft.getInstance().setScreen(new SpellKeysScreen(this, false));
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(pose);
        FocusAugmentsConfig config = FocusAugments.config();
        ItemStack stack = stack();
        if (config == null || !stackValid()) {
            return;
        }
        AbilityTree tree = tree();
        this.defaultView = SpellBarView.ofDefaults(tree);
        this.itemView = SpellBarView.ofItem(tree, stack, this.inventorySlot);
        Draft draft = draft();
        SpellcraftRules.ItemCheck check = draft == null ? null : check(draft);
        SpellModel model = draft == null || check == null ? null : check.models().get(draft.id > 0 ? draft.id : Integer.MAX_VALUE);
        RenderSystem.enableBlend();
        renderTitle(pose, config, stack, check, draft);
        renderPouch(pose, config, stack, check, mouseX, mouseY);
        int rx0 = rightX0();
        int rx1 = this.width - 21;
        SpellcraftGui.window(pose, rx0, bodyTop(), rx1, bodyBottom());
        renderTabs(pose, mouseX, mouseY);
        layoutName();
        SpellcraftGui.insetBlack(pose, nameX(), bodyTop() + 4 + 2, rx1 - PAD, bodyTop() + 4 + TAB - 2);
        this.nameBox.render(pose, mouseX, mouseY, partialTick);
        int wy0 = weaveTop();
        renderWeave(pose, draft, rx0 + PAD, wy0, rx1 - PAD, wy0 + WEAVE_H, mouseX, mouseY);
        renderStats(pose, config, tree, stack, draft, check, model, rx0 + PAD, wy0 + WEAVE_H + 3, rx1 - PAD, bodyBottom() - PAD);
        this.strip.render(pose, mouseX, mouseY);
        if (this.strip.popupOpen()) {
            this.strip.renderOverlay(pose, this, mouseX, mouseY);
            return;
        }
        List<Component> tooltip = hoverTooltip(config, stack, draft, check, mouseX, mouseY);
        if (!tooltip.isEmpty()) {
            this.renderComponentTooltip(pose, tooltip, mouseX, mouseY);
        } else {
            this.strip.renderOverlay(pose, this, mouseX, mouseY);
        }
    }

    private float committedArcana(@Nullable SpellcraftRules.ItemCheck check, @Nullable Draft draft) {
        if (check == null) {
            return 0.0F;
        }
        int editing = draft == null ? Integer.MIN_VALUE : draft.id > 0 ? draft.id : Integer.MAX_VALUE;
        float total = 0.0F;
        for (Map.Entry<Integer, SpellModel> entry : check.models().entrySet()) {
            if (entry.getKey() != editing) {
                total += entry.getValue().arcana();
            }
        }
        return total;
    }

    private void renderTitle(PoseStack pose, FocusAugmentsConfig config, ItemStack stack, @Nullable SpellcraftRules.ItemCheck check, @Nullable Draft draft) {
        int x0 = 21;
        int x1 = this.width - 21;
        SpellcraftGui.window(pose, x0, TITLE_Y, x1, TITLE_Y + TITLE_H);
        this.itemRenderer.renderGuiItem(stack, x0 + 4, TITLE_Y + 3);
        int budget = SpellcraftItemData.budget(stack);
        float committed = committedArcana(check, draft);
        float total = check == null ? 0.0F : check.arcana();
        int depthLimit = Math.min(SpellcraftItemData.depth(stack), config.rules.maxGlyphsPerSpell);
        int glyphs = 0;
        if (draft != null) {
            for (String token : draft.tokens) {
                if (AugmentType.byId(token) != null) {
                    glyphs++;
                }
            }
        }
        String depth = SpellcraftGui.tr("screen.depth", glyphs, depthLimit);
        int ty = TITLE_Y + 8;
        int dx = x1 - 6 - this.font.width(depth);
        this.font.draw(pose, depth, dx, ty, glyphs > depthLimit ? SpellcraftGui.BAD_TEXT : SpellcraftGui.LABEL);
        String num = Math.round(total) + "/" + budget;
        int nx = dx - 8 - this.font.width(num);
        boolean over = Math.round(total) > budget;
        this.font.draw(pose, num, nx, ty, over ? SpellcraftGui.BAD_TEXT : SpellcraftGui.ARCANA_TEXT);
        int bx = nx - 4 - ARCANA_BAR_W;
        SpellcraftGui.arcanaBar(pose, bx, ty, ARCANA_BAR_W, 7, committed, total - committed, budget);
        String label = SpellcraftGui.tr("screen.arcana");
        int lx = bx - 4 - this.font.width(label);
        this.font.draw(pose, label, lx, ty, SpellcraftGui.LABEL);
        String level = SpellcraftGui.tr("screen.level", VaultGearData.read(stack).getItemLevel());
        int nameX = x0 + 24;
        int room = lx - 6 - nameX - this.font.width(level) - 6;
        Component name = stack.getHoverName();
        int color = name.getStyle().getColor() == null ? TITLE_NAME_FALLBACK : 0xFF000000 | name.getStyle().getColor().getValue();
        FormattedText cut = this.font.substrByWidth(name, Math.max(0, room));
        this.font.draw(pose, Language.getInstance().getVisualOrder(cut), nameX, ty, color);
        this.font.draw(pose, level, nameX + this.font.width(cut) + 6, ty, SpellcraftGui.DARK);
    }

    private int pouchListTop() {
        return bodyTop() + 19;
    }

    private int pouchListBottom() {
        return bodyBottom() - PAD;
    }

    private int pouchFit() {
        return Math.max(1, (pouchListBottom() - pouchListTop() - 2) / ROW);
    }

    private List<String> pouchAbilities() {
        List<String> groups = new ArrayList<>();
        for (Skill skill : tree().skills) {
            if (skill instanceof SpecializedSkill specialized && specialized.isUnlocked()) {
                groups.add(specialized.getId());
            }
        }
        return groups;
    }

    private int pouchSize(ItemStack stack) {
        return this.abilitiesTab ? pouchAbilities().size() : SpellcraftItemData.lines(stack).size();
    }

    private void renderPouch(PoseStack pose, FocusAugmentsConfig config, ItemStack stack, @Nullable SpellcraftRules.ItemCheck check, int mouseX, int mouseY) {
        int x0 = 21;
        int y0 = bodyTop();
        int x1 = x0 + POUCH_W;
        SpellcraftGui.window(pose, x0, y0, x1, bodyBottom());
        int tw = (POUCH_W - 2 * PAD - 2) / 2;
        String[] labels = {SpellcraftGui.tr("screen.tab.glyphs"), SpellcraftGui.tr("screen.tab.abilities")};
        for (int k = 0; k < 2; k++) {
            int tx = x0 + PAD + k * (tw + 2);
            boolean on = (k == 1) == this.abilitiesTab;
            if (on) {
                SpellcraftGui.insetGrey(pose, tx, y0 + 4, tx + tw, y0 + 16);
            } else {
                SpellcraftGui.insetDarkGrey(pose, tx, y0 + 4, tx + tw, y0 + 16);
            }
            String label = this.font.plainSubstrByWidth(labels[k], tw - 4);
            this.font.drawShadow(pose, label, tx + (tw - this.font.width(label)) / 2.0F, y0 + 6, on ? SpellcraftGui.WHITE : SpellcraftGui.MUTED);
        }
        int lx0 = x0 + PAD;
        int lx1 = x1 - PAD;
        int ly0 = pouchListTop();
        int ly1 = pouchListBottom();
        SpellcraftGui.insetBlack(pose, lx0, ly0, lx1, ly1);
        int fit = pouchFit();
        int size = pouchSize(stack);
        this.pouchScroll = Math.max(0, Math.min(this.pouchScroll, size - fit));
        int right = lx1 - 3 - SCROLL_W;
        if (this.abilitiesTab) {
            List<String> groups = pouchAbilities();
            Draft draft = draft();
            for (int r = 0; r < Math.min(fit, groups.size() - this.pouchScroll); r++) {
                renderAbilityRow(pose, config, groups.get(r + this.pouchScroll), draft, lx0, ly0 + 2 + r * ROW, right);
            }
        } else {
            List<SpellcraftItemData.Line> lines = SpellcraftItemData.lines(stack);
            for (int r = 0; r < Math.min(fit, lines.size() - this.pouchScroll); r++) {
                renderGlyphRow(pose, config, lines.get(r + this.pouchScroll), check, lx0, ly0 + 2 + r * ROW, right);
            }
        }
        if (size > fit) {
            int track = ly1 - ly0 - 4;
            int thumb = Math.max(6, track * fit / size);
            int top = ly0 + 2 + (track - thumb) * this.pouchScroll / Math.max(1, size - fit);
            GuiComponent.fill(pose, lx1 - SCROLL_W, ly0 + 2, lx1 - 1, ly1 - 2, SCROLL_TRACK);
            GuiComponent.fill(pose, lx1 - SCROLL_W, top, lx1 - 1, top + thumb, SCROLL_THUMB);
        }
    }

    private int usesLeft(SpellcraftItemData.Line line, @Nullable SpellcraftRules.ItemCheck check) {
        int used = check == null ? 0 : check.uses().getOrDefault(line.type(), 0);
        return line.uses() - used;
    }

    private void renderGlyphRow(PoseStack pose, FocusAugmentsConfig config, SpellcraftItemData.Line line, @Nullable SpellcraftRules.ItemCheck check,
                                int lx0, int y, int right) {
        int left = usesLeft(line, check);
        boolean spent = left <= 0;
        SpellcraftGui.slot(pose, lx0 + 3, y + 1, false);
        SpellcraftGui.rune(pose, line.type(), lx0 + 3, y + 1, 16, spent);
        int tx = lx0 + 23;
        int room = right - tx;
        String uses = Math.max(0, left) + "/" + line.uses();
        String name = this.font.plainSubstrByWidth(line.type().displayName(), room - this.font.width(uses) - 3);
        this.font.drawShadow(pose, name, tx, y + 1, spent ? SpellcraftGui.DARK : SpellcraftGui.categoryColor(config, line.type()));
        this.font.drawShadow(pose, uses, right - this.font.width(uses), y + 1, spent ? SpellcraftGui.DARK : SpellcraftGui.WHITE);
        String cost = String.valueOf(line.cost());
        int cw = this.font.width(cost);
        String potency = this.font.plainSubstrByWidth(SpellcraftGui.potency(config, line.type(), line.potency()), room - cw - 9);
        this.font.drawShadow(pose, potency, tx, y + 10, SpellcraftGui.GREY);
        GuiComponent.fill(pose, right - cw - 7, y + 12, right - cw - 3, y + 16, SpellcraftGui.ARCANA);
        this.font.drawShadow(pose, cost, right - cw, y + 10, SpellcraftGui.ARCANA);
    }

    private boolean spellable(FocusAugmentsConfig config, String spec) {
        List<String> traits = config.abilityTraits.get(spec);
        return traits != null && !config.isDisabled(spec) && traits.stream().noneMatch(t -> t.equalsIgnoreCase("hold"));
    }

    private void renderAbilityRow(PoseStack pose, FocusAugmentsConfig config, String group, @Nullable Draft draft, int lx0, int y, int right) {
        AbilityTree tree = tree();
        String spec = SpellcraftRules.specOf(tree).apply(group);
        boolean usable = spec != null && spellable(config, spec) && (draft == null || !draft.tokens.contains(group));
        SpellcraftGui.slot(pose, lx0 + 3, y + 1, false);
        SpellcraftGui.ability(pose, spec != null ? spec : group, lx0 + 3, y + 1, 16, usable ? 1.0F : 0.4F);
        int tx = lx0 + 23;
        int room = right - tx;
        String tier = tier(tree, group);
        String name = this.font.plainSubstrByWidth(SpellcraftGui.groupName(tree, group), room - this.font.width(tier) - 3);
        this.font.drawShadow(pose, name, tx, y + 1, usable ? SpellcraftGui.NAME_GOLD : SpellcraftGui.DARK);
        this.font.drawShadow(pose, tier, right - this.font.width(tier), y + 1, usable ? SpellcraftGui.WHITE : SpellcraftGui.DARK);
        String traits = spec == null ? "" : traitText(config, spec);
        this.font.drawShadow(pose, this.font.plainSubstrByWidth(traits, room), tx, y + 10, SpellcraftGui.GREY);
    }

    private String traitText(FocusAugmentsConfig config, String spec) {
        List<String> traits = config.abilityTraits.get(spec);
        if (traits == null || config.isDisabled(spec)) {
            return SpellcraftGui.tr("screen.no_spellcraft");
        }
        List<String> names = new ArrayList<>();
        traits.forEach(trait -> names.add(trait.toLowerCase(Locale.ROOT)));
        return String.join(" ", names);
    }

    private static String tier(AbilityTree tree, String group) {
        Skill skill = tree.getForId(group).orElse(null);
        if (skill instanceof SpecializedSkill specialized && specialized.getSpecialization() instanceof TieredSkill tiered) {
            return String.valueOf(tiered.getActualTier());
        }
        return "";
    }

    private int tabX(int index) {
        return rightX0() + PAD + index * (TAB + 2);
    }

    private void renderTabs(PoseStack pose, int mouseX, int mouseY) {
        int y = bodyTop() + 4;
        int shown = Math.min(this.drafts.size(), maxTabs());
        for (int i = 0; i < tabCount(); i++) {
            int tx = tabX(i);
            boolean on = i == this.selected;
            if (on) {
                SpellcraftGui.insetGrey(pose, tx, y, tx + TAB, y + TAB);
            } else {
                SpellcraftGui.insetDarkGrey(pose, tx, y, tx + TAB, y + TAB);
            }
            if (i >= shown) {
                GuiComponent.fill(pose, tx + 6, y + 9, tx + 14, y + 11, TAB_PLUS);
                GuiComponent.fill(pose, tx + 9, y + 6, tx + 11, y + 14, TAB_PLUS);
                continue;
            }
            Draft draft = this.drafts.get(i);
            String trigger = draft.tokens.isEmpty() || AugmentType.byId(draft.tokens.get(0)) != null ? null : draft.tokens.get(0);
            if (trigger != null) {
                String spec = SpellcraftRules.specOf(tree()).apply(trigger);
                SpellcraftGui.ability(pose, spec != null ? spec : trigger, tx + 2, y + 2, 16, on ? 1.0F : 0.6F);
            } else {
                this.font.drawShadow(pose, "?", tx + (TAB - this.font.width("?")) / 2.0F, y + 6, SpellcraftGui.MUTED);
            }
            if (this.deleteArmed == i) {
                GuiComponent.fill(pose, tx + 1, y + 1, tx + TAB - 1, y + TAB - 1, DELETE_TINT);
            }
            if (on) {
                SpellcraftGui.ring(pose, tx, y, tx + TAB, y + TAB, SpellcraftGui.ARCANE_GLOW);
            }
        }
    }

    private record Cell(int index, int x, int y) {
    }

    private int perRow(int x0, int x1) {
        return Math.max(1, (x1 - x0 - 12) / CELL);
    }

    private List<Cell> cells(@Nullable Draft draft, int x0, int y0, int x1) {
        List<Cell> cells = new ArrayList<>();
        int count = (draft == null ? 0 : draft.tokens.size()) + 1;
        int perRow = perRow(x0, x1);
        for (int i = 0; i < count; i++) {
            int row = i / perRow - this.weaveScroll;
            if (row < 0 || row >= WEAVE_ROWS) {
                continue;
            }
            cells.add(new Cell(i, x0 + 8 + (i % perRow) * CELL, y0 + WEAVE_TOP + row * CELL_PITCH_Y));
        }
        return cells;
    }

    private int payloadStart(Draft draft) {
        int bundle = draft.tokens.indexOf(AugmentType.BUNDLED_SPELL.id());
        if (bundle < 0) {
            return -1;
        }
        for (int i = bundle + 1; i < draft.tokens.size(); i++) {
            if (AugmentType.byId(draft.tokens.get(i)) == null) {
                return i;
            }
        }
        return -1;
    }

    private void renderWeave(PoseStack pose, @Nullable Draft draft, int x0, int y0, int x1, int y1, int mouseX, int mouseY) {
        SpellcraftGui.starfield(pose, x0, y0, x1, y1);
        List<Cell> cells = cells(draft, x0, y0, x1);
        int size = draft == null ? 0 : draft.tokens.size();
        for (int k = 1; k < cells.size(); k++) {
            Cell a = cells.get(k - 1);
            Cell b = cells.get(k);
            if (a.y() != b.y()) {
                continue;
            }
            boolean toAdd = b.index() == size;
            GuiComponent.fill(pose, a.x() + 17, a.y() + 7, b.x() - 1, a.y() + 9, toAdd ? THREAD_DIM : SpellcraftGui.ARCANE_LINE);
            GuiComponent.fill(pose, a.x() + 17, a.y() + 7, b.x() - 1, a.y() + 8, toAdd ? THREAD_DIM : SpellcraftGui.ARCANE_GLOW);
        }
        int payload = draft == null ? -1 : payloadStart(draft);
        if (payload >= 0) {
            Integer firstRowY = null;
            int minX = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int rowY = Integer.MIN_VALUE;
            for (Cell cell : cells) {
                if (cell.index() < payload) {
                    continue;
                }
                if (cell.y() != rowY) {
                    if (rowY != Integer.MIN_VALUE) {
                        SpellcraftGui.ring(pose, minX - 3, rowY - 3, maxX + 20, rowY + 20, PAYLOAD);
                    }
                    rowY = cell.y();
                    minX = cell.x();
                    if (firstRowY == null) {
                        firstRowY = rowY;
                    }
                }
                maxX = cell.x();
            }
            if (rowY != Integer.MIN_VALUE) {
                SpellcraftGui.ring(pose, minX - 3, rowY - 3, maxX + 20, rowY + 20, PAYLOAD);
            }
            Cell first = cells.stream().filter(cell -> cell.index() == payload).findFirst().orElse(null);
            if (first != null && first.y() == y0 + WEAVE_TOP) {
                SpellcraftGui.smallText(pose, this.font, SpellcraftGui.tr("screen.payload"), first.x() - 1, first.y() - 10, PAYLOAD, SpellBarStrip.KEY_SCALE);
            }
        }
        for (Cell cell : cells) {
            int x = cell.x();
            int y = cell.y();
            if (cell.index() == size) {
                SpellcraftGui.slot(pose, x, y, true);
                GuiComponent.fill(pose, x + 5, y + 7, x + 11, y + 9, PLUS);
                GuiComponent.fill(pose, x + 7, y + 5, x + 9, y + 11, PLUS);
            } else {
                String token = draft.tokens.get(cell.index());
                AugmentType type = AugmentType.byId(token);
                SpellcraftGui.slot(pose, x, y, false);
                if (type != null) {
                    SpellcraftGui.rune(pose, type, x, y, 16, false);
                } else {
                    String spec = SpellcraftRules.specOf(tree()).apply(token);
                    SpellcraftGui.ability(pose, spec != null ? spec : token, x, y, 16, 1.0F);
                }
            }
            boolean marked = this.insertAt == END ? cell.index() == size : cell.index() == this.insertAt - 1;
            if (marked) {
                RenderSystem.enableBlend();
                ScreenTextures.INSET_SLOT_SELECT_FRAME.blit(pose, x - 3, y - 3);
            }
            if (SpellBarStrip.inside(mouseX, mouseY, x, y, 16, 16) && !this.strip.popupOpen()) {
                GuiComponent.fill(pose, x, y, x + 16, y + 16, 0x80FFFFFF);
            }
        }
        int perRow = perRow(x0, x1);
        int rows = (size + 1 + perRow - 1) / perRow;
        if (rows > WEAVE_ROWS) {
            String more = SpellcraftGui.tr("screen.weave_rows", this.weaveScroll + 1, Math.min(rows, this.weaveScroll + WEAVE_ROWS), rows);
            float w = this.font.width(more) * SpellBarStrip.KEY_SCALE;
            SpellcraftGui.smallText(pose, this.font, more, x1 - 3 - w, y1 - 8, SpellcraftGui.MUTED, SpellBarStrip.KEY_SCALE);
        }
    }

    private void renderStats(PoseStack pose, FocusAugmentsConfig config, AbilityTree tree, ItemStack stack, @Nullable Draft draft,
                             @Nullable SpellcraftRules.ItemCheck check, @Nullable SpellModel model, int x0, int y0, int x1, int y1) {
        SpellcraftGui.insetGrey(pose, x0, y0, x1, y1);
        int mid = (x0 + x1) / 2;
        int lw = mid - x0 - 8;
        int rw = x1 - mid - 8;
        GuiComponent.fill(pose, mid, y0 + 3, mid + 1, y1 - 3, DIVIDER);
        int rows = (y1 - y0 - 4) / STAT_PITCH;
        List<String[]> left = new ArrayList<>();
        List<Integer> leftColors = new ArrayList<>();
        ClientSpellStats.Numbers numbers = model == null || model.groups().isEmpty() ? null : ClientSpellStats.spell(config, tree, model);
        String mana = "-";
        String cooldown = "-";
        if (numbers != null) {
            mana = numbers.hasMana() ? SpellcraftGui.tr("fmt.arrow", Math.round(numbers.manaBefore()), Math.round(numbers.manaAfter())) : "-";
            cooldown = SpellcraftGui.tr("fmt.arrow", SpellcraftGui.seconds(numbers.cooldownBefore()),
                    SpellcraftGui.tr("fmt.seconds", SpellcraftGui.seconds(numbers.cooldownAfter())));
        }
        addStat(left, leftColors, "screen.stat.mana", mana, MANA_TEXT);
        addStat(left, leftColors, "screen.stat.cooldown", cooldown, COOLDOWN_TEXT);
        addStat(left, leftColors, "screen.stat.casts", model == null ? "-" : String.valueOf(model.casts()), SpellcraftGui.LABEL);
        addStat(left, leftColors, "screen.stat.arcana", model == null ? "-" : String.valueOf(Math.round(model.arcana())), SpellcraftGui.ARCANA_TEXT);
        addStat(left, leftColors, "screen.stat.escalation", model == null ? "-" : "x" + String.format(Locale.ROOT, "%.2f", escalation(config, model)),
                SpellcraftGui.LABEL);
        addStat(left, leftColors, "screen.stat.hides", model == null ? "-" : hides(tree, stack, draft, model), HIDES_TEXT);
        for (int i = 0; i < Math.min(rows, left.size()); i++) {
            statRow(pose, x0 + 5, y0 + 4 + i * STAT_PITCH, lw, left.get(i)[0], left.get(i)[1], leftColors.get(i));
        }
        int status = rows - 1;
        int rx = mid + 4;
        List<String> errors = errors(draft, check);
        if (!errors.isEmpty()) {
            List<String> wrapped = new ArrayList<>();
            for (String error : errors) {
                this.font.getSplitter().splitLines(error, rw, net.minecraft.network.chat.Style.EMPTY)
                        .forEach(line -> wrapped.add(line.getString()));
            }
            int room = status;
            boolean overflow = wrapped.size() > room;
            int shown = overflow ? room - 1 : wrapped.size();
            for (int i = 0; i < shown; i++) {
                this.font.draw(pose, wrapped.get(i), rx, y0 + 4 + i * STAT_PITCH, SpellcraftGui.BAD_TEXT);
            }
            if (overflow && room > 0) {
                this.font.draw(pose, SpellcraftGui.tr("screen.more_errors", wrapped.size() - shown), rx, y0 + 4 + shown * STAT_PITCH, SpellcraftGui.DARK);
            }
        } else if (model != null) {
            List<String[]> steps = new ArrayList<>();
            stepRows(config, tree, model.steps(), steps);
            for (int i = 0; i < Math.min(status, steps.size()); i++) {
                statRow(pose, rx + 1, y0 + 4 + i * STAT_PITCH, rw, steps.get(i)[0], steps.get(i)[1], SpellcraftGui.LABEL);
            }
        }
        if (status >= 0 && draft != null) {
            Status state = status(draft, check);
            this.font.draw(pose, this.font.plainSubstrByWidth(state.text(), rw), rx, y0 + 4 + status * STAT_PITCH, state.color());
        }
    }

    private record Status(String text, int color) {
    }

    private void addStat(List<String[]> rows, List<Integer> colors, String key, String value, int color) {
        rows.add(new String[]{SpellcraftGui.tr(key), value});
        colors.add(color);
    }

    private void statRow(PoseStack pose, int x, int y, int width, String label, String value, int valueColor) {
        int vw = this.font.width(value);
        String cutLabel = this.font.plainSubstrByWidth(label, Math.max(0, width - Math.min(vw, width / 2) - 4));
        this.font.draw(pose, cutLabel, x, y, SpellcraftGui.LABEL);
        String cutValue = this.font.plainSubstrByWidth(value, width - this.font.width(cutLabel) - 4);
        this.font.draw(pose, cutValue, x + width - this.font.width(cutValue), y, valueColor);
    }

    private static float escalation(FocusAugmentsConfig config, SpellModel model) {
        int n = Math.max(0, model.groups().size() - 1);
        for (Map.Entry<AugmentType, Integer> use : model.uses().entrySet()) {
            if (!config.glyph(use.getKey()).noEscalation) {
                n += use.getValue();
            }
        }
        return 1.0F + config.rules.escalationPerGlyph * Math.max(0, n - 1);
    }

    /** Default-bar slots this spell would hide: abilities it casts that sit on another default slot. */
    private String hides(AbilityTree tree, ItemStack stack, @Nullable Draft draft, SpellModel model) {
        int own = -1;
        if (draft != null && draft.id > 0) {
            String[] bar = SpellcraftItemData.bar(stack);
            for (int i = 0; i < bar.length; i++) {
                if (bar[i].equals(SpellcraftItemData.SPELL_PREFIX + draft.id)) {
                    own = i;
                }
            }
        }
        List<String> hidden = new ArrayList<>();
        for (int i = 0; i < SpellcraftItemData.SLOTS; i++) {
            String group = ClientSpellBar.defaultSlot(i);
            if (i != own && !group.isEmpty() && model.groups().contains(group)) {
                hidden.add(SpellcraftGui.tr("screen.slot_ref", SpellcraftGui.groupName(tree, group), i + 1));
            }
        }
        if (hidden.isEmpty()) {
            return "-";
        }
        return hidden.size() == 1 ? hidden.get(0) : SpellcraftGui.tr("screen.and_more", hidden.get(0), hidden.size() - 1);
    }

    private void stepRows(FocusAugmentsConfig config, AbilityTree tree, List<SpellModel.Step> steps, List<String[]> rows) {
        for (SpellModel.Step step : steps) {
            if (step instanceof SpellModel.Cast cast) {
                rows.add(new String[]{SpellcraftGui.groupName(tree, cast.group()), stepValue(CastModifiers.of(config, cast.glyphs()))});
            } else if (step instanceof SpellModel.Bundle bundle) {
                rows.add(new String[]{SpellcraftGui.tr("screen.step.bundle"), stepValue(CastModifiers.of(config, bundle.glyphs()))});
                stepRows(config, tree, bundle.payload(), rows);
            }
        }
    }

    private static String stepValue(CastModifiers modifiers) {
        if (modifiers.echoPower() > 0.0F) {
            return SpellcraftGui.tr("screen.step.echo", Math.round(modifiers.echoPower() * 100.0F));
        }
        String value = SpellcraftGui.tr("screen.step.damage", String.format(Locale.ROOT, "%.2f", modifiers.damage()));
        if (Math.abs(modifiers.area() - 1.0F) > 1.0E-4F) {
            value += " " + SpellcraftGui.tr("screen.step.area", String.format(Locale.ROOT, "%.2f", modifiers.area()));
        }
        return value;
    }

    /** Every problem that blocks saving this draft; the draft's own name prefix is dropped. */
    private List<String> errors(@Nullable Draft draft, @Nullable SpellcraftRules.ItemCheck check) {
        if (draft == null || check == null || draft.tokens.isEmpty()) {
            return List.of();
        }
        String prefix = storedName(draft) + ": ";
        List<String> errors = new ArrayList<>();
        for (String error : check.errors()) {
            errors.add(error.startsWith(prefix) ? error.substring(prefix.length()) : error);
        }
        return errors;
    }

    /** The draft's save state as shown on the stats panel's last row. */
    private Status status(Draft draft, @Nullable SpellcraftRules.ItemCheck check) {
        if (draft.tokens.isEmpty()) {
            return new Status(SpellcraftGui.tr("screen.status.empty"), SpellcraftGui.DARK);
        }
        if (draft.pendingNew) {
            return new Status(SpellcraftGui.tr("screen.status.saving"), COOLDOWN_TEXT);
        }
        if (matchesStored(draft)) {
            return new Status(SpellcraftGui.tr("screen.status.saved"), SAVED_TEXT);
        }
        if (check == null || !check.isValid()) {
            return new Status(SpellcraftGui.tr("screen.status.unsaved"), SpellcraftGui.BAD_TEXT);
        }
        if (!draft.dirty && draft.sentAt >= 0 && this.ticks - draft.sentAt > SERVER_WAIT) {
            return new Status(SpellcraftGui.tr("screen.status.refused"), SpellcraftGui.BAD_TEXT);
        }
        return new Status(SpellcraftGui.tr("screen.status.saving"), COOLDOWN_TEXT);
    }

    private List<Component> hoverTooltip(FocusAugmentsConfig config, ItemStack stack, @Nullable Draft draft, @Nullable SpellcraftRules.ItemCheck check,
                                         int mouseX, int mouseY) {
        List<Component> lines = new ArrayList<>();
        int lx0 = 21 + PAD;
        int lx1 = 21 + POUCH_W - PAD;
        int ly0 = pouchListTop();
        if (SpellBarStrip.inside(mouseX, mouseY, lx0, ly0 + 2, lx1 - lx0, pouchFit() * ROW)) {
            int row = (mouseY - ly0 - 2) / ROW + this.pouchScroll;
            if (this.abilitiesTab) {
                List<String> groups = pouchAbilities();
                if (row < groups.size()) {
                    String group = groups.get(row);
                    String spec = SpellcraftRules.specOf(tree()).apply(group);
                    lines.add(colored(SpellcraftGui.abilityName(tree(), group), SpellcraftGui.NAME_GOLD));
                    if (spec != null) {
                        lines.add(colored(traitText(config, spec), SpellcraftGui.GREY));
                    }
                    lines.add(new TextComponent(SpellcraftGui.tr("screen.pouch_hint")).withStyle(ChatFormatting.DARK_GRAY));
                }
            } else {
                List<SpellcraftItemData.Line> all = SpellcraftItemData.lines(stack);
                if (row < all.size()) {
                    SpellcraftItemData.Line line = all.get(row);
                    glyphLines(config, line.type(), line.potency(), lines);
                    lines.add(colored(SpellcraftGui.tr("screen.uses_left", Math.max(0, usesLeft(line, check)), line.uses()), SpellcraftGui.WHITE));
                    lines.add(colored(SpellcraftGui.tr("screen.cost", line.cost()), SpellcraftGui.ARCANA));
                    lines.add(new TextComponent(SpellcraftGui.tr("screen.pouch_hint")).withStyle(ChatFormatting.DARK_GRAY));
                }
            }
            return lines;
        }
        int y = bodyTop() + 4;
        for (int i = 0; i < tabCount(); i++) {
            if (SpellBarStrip.inside(mouseX, mouseY, tabX(i), y, TAB, TAB)) {
                if (i >= Math.min(this.drafts.size(), maxTabs())) {
                    lines.add(new TextComponent(SpellcraftGui.tr("screen.new_tab")));
                } else {
                    Draft tab = this.drafts.get(i);
                    lines.add(colored(storedName(tab), SpellcraftGui.ARCANE_GLOW));
                    lines.add(new TextComponent(SpellcraftGui.tr(this.deleteArmed == i ? "screen.delete_confirm" : "screen.tab_hint"))
                            .withStyle(ChatFormatting.DARK_GRAY));
                }
                return lines;
            }
        }
        int wy0 = weaveTop();
        int rx0 = rightX0() + PAD;
        int rx1 = this.width - 21 - PAD;
        for (Cell cell : cells(draft, rx0, wy0, rx1)) {
            if (!SpellBarStrip.inside(mouseX, mouseY, cell.x(), cell.y(), 16, 16)) {
                continue;
            }
            if (draft == null || cell.index() >= draft.tokens.size()) {
                lines.add(new TextComponent(SpellcraftGui.tr("screen.add_hint")).withStyle(ChatFormatting.GRAY));
            } else {
                String token = draft.tokens.get(cell.index());
                AugmentType type = AugmentType.byId(token);
                if (type != null) {
                    float potency = SpellcraftItemData.line(stack, type).map(SpellcraftItemData.Line::potency).orElse(0.0F);
                    glyphLines(config, type, potency, lines);
                } else {
                    lines.add(colored(SpellcraftGui.abilityName(tree(), token), SpellcraftGui.NAME_GOLD));
                }
                int payload = payloadStart(draft);
                if (payload >= 0 && cell.index() >= payload) {
                    lines.add(colored(SpellcraftGui.tr("screen.in_payload"), PAYLOAD));
                }
                lines.add(new TextComponent(SpellcraftGui.tr("screen.cell_hint")).withStyle(ChatFormatting.DARK_GRAY));
            }
            return lines;
        }
        int sy0 = wy0 + WEAVE_H + 3;
        int mid = (rx0 + rx1) / 2;
        List<String> errors = errors(draft, check);
        if (!errors.isEmpty() && SpellBarStrip.inside(mouseX, mouseY, mid, sy0, rx1 - mid, bodyBottom() - PAD - sy0)) {
            errors.forEach(error -> lines.add(colored(error, SpellcraftGui.BAD)));
        }
        return lines;
    }

    private void glyphLines(FocusAugmentsConfig config, AugmentType type, float potency, List<Component> lines) {
        lines.add(colored(type.displayName(), SpellcraftGui.categoryColor(config, type)));
        lines.add(colored(SpellcraftGui.potency(config, type, potency), SpellcraftGui.WHITE));
        String side = SpellcraftGui.sideEffects(config, type, potency);
        if (!side.isEmpty()) {
            lines.add(colored(side, SpellcraftGui.GREY));
        }
        List<String> fits = new ArrayList<>();
        for (List<String> set : config.glyph(type).requires) {
            fits.add(String.join("+", set));
        }
        if (!fits.isEmpty()) {
            lines.add(colored(SpellcraftGui.tr("screen.fits", String.join(", ", fits)), SpellcraftGui.GREY));
        }
    }

    private static Component colored(String text, int color) {
        return new TextComponent(text).withStyle(style -> style.withColor(TextColor.fromRgb(color & 0xFFFFFF)));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.strip.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (this.nameBox.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        this.nameBox.setFocus(false);
        FocusAugmentsConfig config = FocusAugments.config();
        if (config == null) {
            return false;
        }
        int y0 = bodyTop();
        int tw = (POUCH_W - 2 * PAD - 2) / 2;
        for (int k = 0; k < 2; k++) {
            if (SpellBarStrip.inside(mouseX, mouseY, 21 + PAD + k * (tw + 2), y0 + 4, tw, 12)) {
                this.abilitiesTab = k == 1;
                this.pouchScroll = 0;
                click();
                return true;
            }
        }
        int lx0 = 21 + PAD;
        int lx1 = 21 + POUCH_W - PAD;
        int ly0 = pouchListTop();
        if (button == 0 && SpellBarStrip.inside(mouseX, mouseY, lx0, ly0 + 2, lx1 - lx0, pouchFit() * ROW)) {
            int row = (int) ((mouseY - ly0 - 2) / ROW) + this.pouchScroll;
            if (this.abilitiesTab) {
                List<String> groups = pouchAbilities();
                if (row < groups.size()) {
                    insert(groups.get(row));
                }
            } else {
                List<SpellcraftItemData.Line> lines = SpellcraftItemData.lines(stack());
                if (row < lines.size()) {
                    insert(lines.get(row).type().id());
                }
            }
            return true;
        }
        if (clickTabs(mouseX, mouseY, button)) {
            return true;
        }
        if (clickWeave(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean clickTabs(double mouseX, double mouseY, int button) {
        int y = bodyTop() + 4;
        int shown = Math.min(this.drafts.size(), maxTabs());
        for (int i = 0; i < tabCount(); i++) {
            if (!SpellBarStrip.inside(mouseX, mouseY, tabX(i), y, TAB, TAB)) {
                continue;
            }
            if (i >= shown) {
                if (button == 0) {
                    this.drafts.add(new Draft(0, "", List.of()));
                    select(this.drafts.size() - 1);
                    click();
                }
                return true;
            }
            if (button == 0) {
                select(i);
                click();
            } else if (button == 1 && hasShiftDown()) {
                if (this.deleteArmed == i) {
                    delete(i);
                } else {
                    this.deleteArmed = i;
                    this.deleteArmedAt = this.ticks;
                }
                click();
            }
            return true;
        }
        return false;
    }

    private void select(int index) {
        if (this.saveIn > 0) {
            saveDirty();
            this.saveIn = -1;
        }
        this.selected = index;
        this.insertAt = END;
        this.weaveScroll = 0;
        this.deleteArmed = -1;
        loadName();
    }

    private void delete(int index) {
        Draft draft = this.drafts.get(index);
        if (draft.id > 0) {
            ModNetwork.sendToServer(new SpellEditMessage(this.inventorySlot, draft.id, "", List.of(), true));
        }
        this.drafts.remove(index);
        if (this.drafts.isEmpty()) {
            this.drafts.add(new Draft(0, "", List.of()));
        }
        this.deleteArmed = -1;
        this.selected = Math.min(this.selected, this.drafts.size() - 1);
        this.insertAt = END;
        loadName();
    }

    private boolean clickWeave(double mouseX, double mouseY, int button) {
        Draft draft = draft();
        int wy0 = weaveTop();
        int rx0 = rightX0() + PAD;
        int rx1 = this.width - 21 - PAD;
        for (Cell cell : cells(draft, rx0, wy0, rx1)) {
            if (!SpellBarStrip.inside(mouseX, mouseY, cell.x(), cell.y(), 16, 16)) {
                continue;
            }
            int size = draft == null ? 0 : draft.tokens.size();
            if (button == 0) {
                this.insertAt = cell.index() >= size ? END : cell.index() + 1;
                click();
            } else if (button == 1 && draft != null && cell.index() < size) {
                draft.tokens.remove(cell.index());
                if (this.insertAt != END && this.insertAt > cell.index()) {
                    this.insertAt--;
                }
                markDirty(draft);
                click();
            }
            return true;
        }
        return false;
    }

    /** Inserts a token after the selected weave cell; an ability goes first when the spell has no trigger yet. */
    private void insert(String token) {
        Draft draft = draft();
        if (draft == null) {
            return;
        }
        boolean ability = AugmentType.byId(token) == null;
        boolean noTrigger = draft.tokens.isEmpty() || AugmentType.byId(draft.tokens.get(0)) != null;
        int at = ability && noTrigger ? 0 : Math.min(this.insertAt, draft.tokens.size());
        draft.tokens.add(at, token);
        if (this.insertAt != END) {
            this.insertAt = at + 1;
        }
        if (ability && at == 0 && draft.name.isBlank()) {
            draft.name = SpellcraftGui.groupName(tree(), token);
            loadName();
        }
        markDirty(draft);
        click();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.strip.mouseScrolled(mouseX, mouseY, delta)) {
            return true;
        }
        int step = delta < 0 ? 1 : -1;
        if (SpellBarStrip.inside(mouseX, mouseY, 21, bodyTop(), POUCH_W, bodyBottom() - bodyTop())) {
            this.pouchScroll = Math.max(0, Math.min(Math.max(0, pouchSize(stack()) - pouchFit()), this.pouchScroll + step));
            return true;
        }
        int wy0 = weaveTop();
        int rx0 = rightX0() + PAD;
        int rx1 = this.width - 21 - PAD;
        if (SpellBarStrip.inside(mouseX, mouseY, rx0, wy0, rx1 - rx0, WEAVE_H)) {
            Draft draft = draft();
            int perRow = perRow(rx0, rx1);
            int rows = ((draft == null ? 0 : draft.tokens.size()) + 1 + perRow - 1) / perRow;
            this.weaveScroll = Math.max(0, Math.min(Math.max(0, rows - WEAVE_ROWS), this.weaveScroll + step));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private static void click() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    /** Opens the screen for the focus or wand in a player inventory slot. */
    public static void open(@Nullable Screen parent, int inventorySlot) {
        if (FocusAugments.config() == null) {
            WoldsVaults.LOGGER.error("Spellcraft screen: the focus augments table is not loaded on this client; the screen stays closed");
            return;
        }
        Minecraft.getInstance().setScreen(new FocusSpellcraftScreen(parent, inventorySlot));
    }
}
