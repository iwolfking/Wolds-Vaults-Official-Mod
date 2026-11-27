package xyz.iwolfking.woldsvaults.client.spellcraft.editor;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.data.ClientAbilityData;
import iskallia.vault.skill.tree.AbilityTree;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.client.spellcraft.bar.SpellBarStrip;
import xyz.iwolfking.woldsvaults.client.spellcraft.bar.SpellBarView;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftDraw;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftNames;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.spellcraft.item.StoredSpell;
import xyz.iwolfking.woldsvaults.spellcraft.spell.SpellModel;
import xyz.iwolfking.woldsvaults.spellcraft.spell.SpellcraftRules;

import javax.annotation.Nullable;
import java.util.List;

/**
 * The spell editor, opened by right-clicking a spellcraft item in the inventory. The spell bar strip gets first refusal
 * on clicks and scrolls because its slot chooser draws over the other panels. A panel's hover tooltip wins over the
 * strip's, except while the chooser is open.
 */
public class SpellcraftScreen extends Screen implements SpellBarStrip.Host {
    private static final int NAME_TEXT = 0xFFE0D0FF;

    @Nullable
    private final Screen parent;
    private final SpellDrafts drafts;
    private final SpellBarStrip strip;
    private final TitleBar title;
    private final PouchPanel pouch;
    private final SpellTabs tabs;
    private final WeavePanel weave;
    private final StatsPanel stats;
    private final EditorTooltip tooltip;
    private boolean loadingName;
    private EditBox nameBox;
    private SpellBarView effectiveView;
    private SpellBarView defaultView;

    public SpellcraftScreen(@Nullable Screen parent, int inventorySlot) {
        super(new TextComponent(SpellcraftNames.tr("screen.title")));
        this.parent = parent;
        this.strip = new SpellBarStrip(this, true);
        this.drafts = new SpellDrafts(inventorySlot, this::tree, this::loadName);
        this.title = new TitleBar(this);
        this.pouch = new PouchPanel(this, this.drafts);
        this.tabs = new SpellTabs(this, this.drafts);
        this.weave = new WeavePanel(this, this.drafts, this.strip);
        this.stats = new StatsPanel(this, this.drafts);
        this.tooltip = new EditorTooltip(this.pouch, this.tabs, this.weave, this.stats, this.title);
    }

    Font font() {
        return this.font;
    }

    ItemRenderer itemRenderer() {
        return this.itemRenderer;
    }

    @Override
    protected void init() {
        SpellcraftConfig config = ModConfigs.SPELLCRAFT;
        int nx = EditorLayout.nameX(this.width, this.drafts.size());
        this.nameBox = new EditBox(this.font, nx + 4, EditorLayout.bodyTop() + 4 + 6, Math.max(10, this.width - 21 - EditorLayout.PAD - nx - 8), 10, new TextComponent(""));
        this.nameBox.setBordered(false);
        this.nameBox.setTextColor(NAME_TEXT);
        if (config != null) {
            this.nameBox.setMaxLength(config.rules.maxNameLength);
        }
        this.nameBox.setResponder(value -> {
            SpellDrafts.Draft draft = this.drafts.draft();
            if (!this.loadingName && draft != null) {
                draft.name = value;
                this.drafts.markDirty(draft);
            }
        });
        loadName();
        this.addWidget(this.nameBox);
        this.strip.layout(21, this.height - 21 - SpellBarStrip.HEIGHT, this.width - 21);
    }

    private void loadName() {
        SpellDrafts.Draft draft = this.drafts.draft();
        this.loadingName = true;
        this.nameBox.setValue(draft == null ? "" : draft.name);
        this.loadingName = false;
    }

    private void layoutName() {
        int nx = EditorLayout.nameX(this.width, this.drafts.size());
        this.nameBox.x = nx + 4;
        this.nameBox.setWidth(Math.max(10, this.width - 21 - EditorLayout.PAD - nx - 8));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        this.drafts.countTick();
        if (!this.drafts.stackValid()) {
            onClose();
            return;
        }
        this.nameBox.tick();
        this.drafts.countDownSave();
        this.drafts.adoptNewSpells();
        this.drafts.expireDelete();
    }

    @Override
    public void onClose() {
        if (this.drafts.savePending() && this.drafts.stackValid()) {
            this.drafts.saveDirty();
        }
        Minecraft.getInstance().setScreen(this.parent);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.strip.keyPressed(keyCode, scanCode)) {
            return true;
        }
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
    public SpellBarView effectiveView() {
        return this.effectiveView;
    }

    @Nullable
    @Override
    public String selectedGroup() {
        return null;
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(pose);
        SpellcraftConfig config = ModConfigs.SPELLCRAFT;
        ItemStack stack = this.drafts.stack();
        if (config == null || !this.drafts.stackValid()) {
            return;
        }
        AbilityTree tree = tree();
        this.defaultView = SpellBarView.ofDefaults(tree);
        this.effectiveView = SpellBarView.ofSynced(tree);
        SpellDrafts.Draft draft = this.drafts.draft();
        SpellcraftRules.ItemCheck check = draft == null ? null : this.drafts.check(draft);
        SpellModel model = draft == null || check == null ? null : check.models().get(draft.id > 0 ? draft.id : StoredSpell.UNSAVED_ID);
        RenderSystem.enableBlend();
        this.title.render(pose, config, stack, check, draft);
        this.pouch.render(pose, config);
        int rx0 = EditorLayout.rightX0();
        int rx1 = this.width - 21;
        SpellcraftDraw.window(pose, rx0, EditorLayout.bodyTop(), rx1, EditorLayout.bodyBottom(this.height));
        this.tabs.render(pose);
        layoutName();
        SpellcraftDraw.insetBlack(pose, EditorLayout.nameX(this.width, this.drafts.size()), EditorLayout.bodyTop() + 4 + 2, rx1 - EditorLayout.PAD,
                EditorLayout.bodyTop() + 4 + EditorLayout.TAB - 2);
        this.nameBox.render(pose, mouseX, mouseY, partialTick);
        int rows = this.weave.shownRows();
        this.weave.render(pose, draft, model, EditorLayout.innerX0(), EditorLayout.weaveTop(), EditorLayout.innerX1(this.width), EditorLayout.weaveBottom(rows),
                mouseX, mouseY);
        this.stats.render(pose, config, tree, draft, check, model, EditorLayout.innerX0(), EditorLayout.statsTop(rows), EditorLayout.innerX1(this.width),
                EditorLayout.statsBottom(this.height));
        this.strip.render(pose, mouseX, mouseY);
        if (this.strip.popupOpen()) {
            this.strip.renderOverlay(pose, this, mouseX, mouseY);
            return;
        }
        List<Component> tooltip = this.tooltip.hoverTooltip(config, draft, check, mouseX, mouseY);
        if (!tooltip.isEmpty()) {
            this.renderComponentTooltip(pose, tooltip, mouseX, mouseY);
        } else {
            this.strip.renderOverlay(pose, this, mouseX, mouseY);
        }
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
        SpellcraftConfig config = ModConfigs.SPELLCRAFT;
        if (config == null) {
            return false;
        }
        if (this.pouch.mouseClicked(config, mouseX, mouseY, button)) {
            return true;
        }
        if (this.stats.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (this.tabs.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (this.weave.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.strip.mouseScrolled(mouseX, mouseY, delta)) {
            return true;
        }
        int step = delta < 0 ? 1 : -1;
        if (this.stats.mouseScrolled(mouseX, mouseY, step)) {
            return true;
        }
        if (this.pouch.mouseScrolled(mouseX, mouseY, step)) {
            return true;
        }
        if (this.weave.mouseScrolled(mouseX, mouseY, step)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    public static void open(@Nullable Screen parent, int inventorySlot) {
        if (ModConfigs.SPELLCRAFT == null) {
            WoldLogger.error("Spellcraft screen: the spellcraft table is not loaded on this client; the screen stays closed");
            return;
        }
        Minecraft.getInstance().setScreen(new SpellcraftScreen(parent, inventorySlot));
    }
}
