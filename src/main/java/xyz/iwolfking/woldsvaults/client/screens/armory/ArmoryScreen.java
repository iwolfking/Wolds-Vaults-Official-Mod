package xyz.iwolfking.woldsvaults.client.screens.armory;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.EntityModelElement;
import iskallia.vault.client.gui.framework.element.LabelElement;
import iskallia.vault.client.gui.framework.element.NineSliceElement;
import iskallia.vault.client.gui.framework.element.SlotsElement;
import iskallia.vault.client.gui.framework.element.VerticalScrollbarElement;
import iskallia.vault.client.gui.framework.element.spi.IElement;
import iskallia.vault.client.gui.framework.ScreenRenderers;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;
import iskallia.vault.client.gui.screen.player.AbstractSkillTabElementContainerScreen;
import iskallia.vault.client.gui.screen.player.element.CuriosElement;
import iskallia.vault.client.gui.screen.player.element.SkillTabContainerElement;
import iskallia.vault.client.gui.screen.player.element.StatLabelElementBuilder;
import iskallia.vault.client.gui.screen.player.element.StatLabelListElement;
import iskallia.vault.client.gui.screen.player.element.StatListPlayerContainerElement;
import iskallia.vault.client.gui.screen.player.element.VaultLevelBarElement;
import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.armory.ArmoryTab;
import xyz.iwolfking.woldsvaults.armory.ArmoryViewInfo;
import xyz.iwolfking.woldsvaults.armory.SlotKeys;
import xyz.iwolfking.woldsvaults.armory.block.ArmoryBlockEntity;
import xyz.iwolfking.woldsvaults.armory.menu.ArmoryClientState;
import xyz.iwolfking.woldsvaults.armory.menu.ArmoryMenu;
import xyz.iwolfking.woldsvaults.armory.network.ServerboundArmorySaveMessage;
import xyz.iwolfking.woldsvaults.init.ModNetwork;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * The armory's Gear, Inventory and Excess tabs: the H screen's statistics tab rebuilt from its own elements and
 * fed the open page. Gear shows the page's armour, offhand and curio slots, a player model wearing them and the
 * stat lists recorded at Save; Inventory shows the page's inventory and hotbar plus the upgrade slots; Excess
 * shows the shared 128-slot storage.
 */
public class ArmoryScreen extends AbstractSkillTabElementContainerScreen<ArmoryMenu> {
    private static final int GUI_WIDTH = 350;
    private static final int GUI_HEIGHT = 186;
    private static final int MARK_COLOR = 0xFF55D46A;
    private static final int BUTTON_HEIGHT = 13;
    private static final int BUTTON_PAD = 10;
    private static final int BUTTON_GAP = 2;
    private static final int LIST_X = 180;

    private final ArmoryViewInfo openInfo;
    @Nullable
    private ArmoryTabs.BuildTabs buildTabs;
    @Nullable
    private RemotePlayer model;
    private ArmoryClientState shownState;
    private Button loadButton;
    private Button stowButton;
    private Button confirmButton;
    private Button cancelButton;
    private boolean confirming;
    private final List<Button> titleButtons = new ArrayList<>();
    private final List<IElement> gearElements = new ArrayList<>();

    public ArmoryScreen(ArmoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, ScreenRenderers.getBuffered());
        this.openInfo = menu.info();
        this.shownState = menu.state();
        this.setGuiSize(Spatials.size(GUI_WIDTH, GUI_HEIGHT));
        this.elementStore.removeElements(SkillTabContainerElement.class);
        this.elementStore.removeElements(element -> element instanceof LabelElement<?>);
        this.addElement(new LabelElement<>(Spatials.positionXY(this.getTabContentSpatial()).translateXYZ(8, 6, 2),
                this.getTabTitle().withStyle(ChatFormatting.BLACK), LabelTextStyle.defaultStyle()));
        this.addElement(ArmoryTabs.topStrip(Spatials.positionXYZ(15, -28, 1), this.openInfo)
                .layout((screen, gui, parent, world) -> world.translateXY(this.getTabContentSpatial())));
        this.addElement(new NineSliceElement<>(Spatials.positionXY(this.getTabContentSpatial()).height(this.getTabContentSpatial()),
                ScreenTextures.DEFAULT_WINDOW_BACKGROUND).layout((screen, gui, parent, world) -> world.width(this.getTabContentSpatial())));
        this.addElement(new NineSliceElement<>(Spatials.positionY(-4).positionZ(-10), ScreenTextures.DEFAULT_WINDOW_BACKGROUND)
                .layout((screen, gui, parent, world) -> world.translateX(gui).translateY(this.getTabContentSpatial().bottom()).size(gui)));
        this.addElement(new SlotsElement<>(Spatials.zero(), this.menu.slots, ScreenTextures.INSET_ITEM_SLOT_BACKGROUND)
                .layout((screen, gui, parent, world) -> world.positionXY(gui)));
        this.addBuildTabs();
        switch (this.openInfo.tab()) {
            case INVENTORY -> this.addInventoryTab();
            case EXCESS -> this.addExcessTab();
            default -> this.addGearTab();
        }
    }

    private void addBuildTabs() {
        if (this.buildTabs != null) {
            this.removeElement(this.buildTabs);
        }
        this.buildTabs = this.addElement(ArmoryTabs.buildTabs(Spatials.positionXY(-3, 3), this.menu.info())
                .layout((screen, gui, parent, world) -> world.translateX(gui.right()).translateY(this.getTabContentSpatial().bottom())));
    }

    private void addGearTab() {
        EntityModelElement<?> model = this.gear(new EntityModelElement<>(Spatials.positionXY(25, 4), Spatials.size(54, 90), this::modelEntity)
                .layout((screen, gui, parent, world) -> world.translateX(gui).translateY(this.getTabContentSpatial().bottom())));
        this.gear(new VaultLevelBarElement<>(Spatials.positionXY(4, -8), () -> this.shownState.stats().getFloat("levelPercent"),
                () -> this.shownState.hasBuild() ? this.shownState.savedLevel() : 0)
                .layout((screen, gui, parent, world) -> world.translateX(model.right()).translateY(model.bottom())));
        StatLabelListElement<?> prominent = this.gear(new StatLabelListElement<>(Spatials.positionXY(4, 4).width(82),
                ArmoryStats.builders(this.shownState.stats(), "prominent"))
                .layout((screen, gui, parent, world) -> world.translateX(model.right()).translateY(model.top())));
        NineSliceElement<?> separator = this.gear(new NineSliceElement<>(Spatials.positionXYZ(8, -2, -5).width(3).height(-4),
                ScreenTextures.INSET_VERTICAL_SEPARATOR)
                .layout((screen, gui, parent, world) -> world.translateX(prominent.right()).translateY(this.getTabContentSpatial().bottom())
                        .height(world.height() + gui.height())));
        this.gear(new StatListPlayerContainerElement(Spatials.positionX(4).width(-7).height(-16),
                ArmoryStats.builders(this.shownState.stats(), "player"),
                ArmoryStats.favour(this.shownState.stats(), VaultGod.IDONA), ArmoryStats.favour(this.shownState.stats(), VaultGod.TENOS),
                ArmoryStats.favour(this.shownState.stats(), VaultGod.VELARA), ArmoryStats.favour(this.shownState.stats(), VaultGod.WENDARR))
                .layout((screen, gui, parent, world) -> world.translateX(separator.right()).translateY(model.top())
                        .width(world.width() + gui.right() - world.x()).height(world.height() + gui.height())));
        int hidden = SlotKeys.CURIO_IDS.size() - ArmoryMenu.CURIO_VISIBLE;
        this.gear(new CuriosElement<>(this::getTabContentSpatial, true, ArmoryMenu.CURIO_VISIBLE,
                value -> this.menu.setCurioScroll(Math.round(value * hidden))));
    }

    private <E extends IElement> E gear(E element) {
        this.gearElements.add(element);
        return this.addElement(element);
    }

    private void addInventoryTab() {
        this.addElement(new NineSliceElement<>(Spatials.positionXYZ(LIST_X - 7, -2, -5).width(3).height(-4), ScreenTextures.INSET_VERTICAL_SEPARATOR)
                .layout((screen, gui, parent, world) -> world.translateX(gui.left()).translateY(this.getTabContentSpatial().bottom())
                        .height(world.height() + gui.height())));
        List<StatLabelElementBuilder<?>> rows = new ArrayList<>();
        rows.add(row("Stored", () -> this.storedCount() + " / " + this.expectedCount(),
                "Items this page holds / items its marked slots were saved with"));
        rows.add(row("Excess", () -> this.excessCount() + " / " + ArmoryBlockEntity.EXCESS_SLOTS,
                "Shared by all 8 builds; Load puts the gear it replaces here"));
        this.addElement(new StatLabelListElement<>(Spatials.positionXY(LIST_X, 8).width(GUI_WIDTH - LIST_X - 7), rows)
                .layout((screen, gui, parent, world) -> world.translateX(gui).translateY(this.getTabContentSpatial().bottom() - 4)));
        this.addElement(new LabelElement<>(Spatials.positionXY(LIST_X, 41), new TextComponent("Upgrades").withStyle(ChatFormatting.BLACK),
                LabelTextStyle.defaultStyle())
                .layout((screen, gui, parent, world) -> world.translateX(gui).translateY(this.getTabContentSpatial().bottom() - 4)));
    }

    private void addExcessTab() {
        int rows = ArmoryBlockEntity.EXCESS_SLOTS / ArmoryMenu.EXCESS_COLUMNS;
        int hidden = rows - ArmoryMenu.EXCESS_VISIBLE_ROWS;
        this.addElement(new VerticalScrollbarElement<>(Spatials.positionXY(8 + ArmoryMenu.EXCESS_COLUMNS * 18 + 3, 8),
                ArmoryMenu.EXCESS_VISIBLE_ROWS * 18, value -> this.menu.setExcessScroll(Math.round(value * hidden)))
                .layout((screen, gui, parent, world) -> world.translateX(gui).translateY(this.getTabContentSpatial().bottom() - 4)));
    }

    private static StatLabelElementBuilder<String> row(String label, Supplier<String> value, String description) {
        return new StatLabelElementBuilder<>(() -> label, () -> List.of(description), value,
                text -> new TextComponent(text), TextColor.fromRgb(0));
    }

    private int storedCount() {
        int count = 0;
        for (int i = 0; i < SlotKeys.size(); i++) {
            if (this.menu.slots.get(ArmoryMenu.PAGE_START + i).hasItem()) {
                count++;
            }
        }
        return count;
    }

    private int expectedCount() {
        int count = 0;
        for (String key : SlotKeys.ALL) {
            ItemStack expected = this.menu.state().expected(key);
            if (this.menu.state().isMarked(key) && expected != null && !expected.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private int excessCount() {
        int count = 0;
        for (int i = 0; i < ArmoryBlockEntity.EXCESS_SLOTS; i++) {
            if (this.menu.slots.get(ArmoryMenu.EXCESS_START + i).hasItem()) {
                count++;
            }
        }
        return count;
    }

    private LivingEntity modelEntity() {
        if (this.minecraft == null || this.minecraft.level == null || this.minecraft.player == null) {
            return null;
        }
        if (this.model == null) {
            this.model = new RemotePlayer(this.minecraft.level, this.minecraft.player.getGameProfile());
        }
        for (int i = 0; i < SlotKeys.ARMOR.length; i++) {
            this.model.setItemSlot(SlotKeys.ARMOR[i], this.shown(SlotKeys.ALL.get(i)));
        }
        this.model.setItemSlot(EquipmentSlot.OFFHAND, this.shown(SlotKeys.OFFHAND));
        int selected = this.shownState.stats().getInt("selected");
        this.model.setItemSlot(EquipmentSlot.MAINHAND, this.shown("hotbar:" + Math.max(0, Math.min(8, selected))));
        return this.model;
    }

    private ItemStack shown(String key) {
        Slot slot = this.menu.slots.get(ArmoryMenu.PAGE_START + SlotKeys.indexOf(key));
        if (slot.hasItem()) {
            return slot.getItem();
        }
        ItemStack expected = this.shownState.expected(key);
        return expected == null ? ItemStack.EMPTY : expected;
    }

    @Override
    public int getTabIndex() {
        return 0;
    }

    @Override
    public MutableComponent getTabTitle() {
        if (this.openInfo == null) {
            return new TextComponent("Armory");
        }
        if (this.openInfo.tab() == ArmoryTab.EXCESS) {
            return new TextComponent("Excess storage");
        }
        return new TextComponent(this.openInfo.pageTitles().get(this.openInfo.page()));
    }

    @Override
    public ISpatial getTabContentSpatial() {
        return Spatials.copy(super.getTabContentSpatial()).height(19);
    }

    @Override
    protected void layout(ISpatial parent) {
        super.layout(parent);
        this.menu.setSlotOffsetY(this.getTabContentSpatial().bottom() - 3 - this.getGuiTop());
    }

    @Override
    protected void init() {
        super.init();
        this.titleButtons.clear();
        ISpatial title = this.getTabContentSpatial();
        int x = title.right() - 4;
        int y = title.y() + 3;
        if (this.openInfo.tab() != ArmoryTab.EXCESS) {
            this.loadButton = this.titleButton("Load", x, y, button -> this.click(ArmoryMenu.BUTTON_LOAD));
            x = this.loadButton.x - BUTTON_GAP;
            this.stowButton = this.titleButton("Stow", x, y, button -> this.click(ArmoryMenu.BUTTON_STOW));
            x = this.stowButton.x - BUTTON_GAP;
            this.titleButton("Save", x, y, button -> this.save());
            this.stowButton.active = !this.openInfo.creative();
        }
        int popupWidth = 140;
        int popupX = (this.width - popupWidth) / 2;
        int popupY = this.height / 2;
        int buttonWidth = (popupWidth - 16 - 4) / 2;
        this.confirmButton = this.addRenderableWidget(new Button(popupX + 8, popupY + 4, buttonWidth, 15,
                new TextComponent("Overwrite"), button -> this.sendSave(true)));
        this.cancelButton = this.addRenderableWidget(new Button(popupX + popupWidth - 8 - buttonWidth, popupY + 4, buttonWidth, 15,
                new TextComponent("Cancel"), button -> this.setConfirming(false)));
        this.setConfirming(false);
        this.refreshButtons();
    }

    private Button titleButton(String label, int right, int y, Button.OnPress onPress) {
        int width = this.font.width(label) + BUTTON_PAD;
        return this.titleButton(new Button(right - width, y, width, BUTTON_HEIGHT, new TextComponent(label), onPress,
                (button, poseStack, mouseX, mouseY) -> this.renderComponentTooltip(poseStack, this.buttonTooltip(label), mouseX, mouseY)));
    }

    /** VH element screens only hand clicks to their elements, so the title buttons are clicked from {@link #mouseClicked}. */
    private Button titleButton(Button button) {
        this.titleButtons.add(button);
        return this.addRenderableWidget(button);
    }

    private List<Component> buttonTooltip(String label) {
        List<Component> lines = new ArrayList<>();
        ArmoryViewInfo info = this.menu.info();
        switch (label) {
            case "Save" -> {
                lines.add(new TextComponent("Save"));
                lines.add(new TextComponent(info.creative() ? "Records your trees and copies of your marked gear"
                        : "Records your trees and what is in each marked slot").withStyle(ChatFormatting.GRAY));
            }
            case "Stow" -> {
                lines.add(new TextComponent("Stow"));
                lines.add(new TextComponent(info.creative() ? "The Creative Armory does not store items"
                        : "Moves your marked gear into this build").withStyle(ChatFormatting.GRAY));
            }
            default -> {
                lines.add(new TextComponent(info.creative() ? "Load (creative copy)" : "Load"));
                info.loadRefusals().forEach(line -> lines.add(new TextComponent(line).withStyle(ChatFormatting.RED)));
                if (!info.loadRefusals().isEmpty()) {
                    lines.add(new TextComponent("Nothing would move").withStyle(ChatFormatting.GRAY));
                }
                info.loadNotes().forEach(line -> lines.add(new TextComponent(line).withStyle(
                        line.startsWith("Raises") || line.startsWith("Learns") ? ChatFormatting.GREEN : ChatFormatting.GRAY)));
            }
        }
        return lines;
    }

    private void refreshButtons() {
        if (this.loadButton != null) {
            this.loadButton.active = this.menu.info().loadRefusals().isEmpty();
        }
    }

    private void click(int id) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
        }
    }

    private void save() {
        Boolean saved = this.menu.info().pageSaved().get(this.menu.page());
        if (Boolean.TRUE.equals(saved)) {
            this.setConfirming(true);
        } else {
            this.sendSave(false);
        }
    }

    private void sendSave(boolean overwrite) {
        this.setConfirming(false);
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }
        ModNetwork.sendToServer(new ServerboundArmorySaveMessage(this.menu.containerId, overwrite, ArmoryStats.capture(this.minecraft.player)));
    }

    private void setConfirming(boolean confirming) {
        this.confirming = confirming;
        this.confirmButton.visible = confirming;
        this.cancelButton.visible = confirming;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (this.menu.state() != this.shownState) {
            this.shownState = this.menu.state();
            this.addBuildTabs();
            this.rebuildForState();
            this.refreshButtons();
            this.requestLayout();
        }
    }

    private void rebuildForState() {
        if (this.openInfo.tab() == ArmoryTab.GEAR) {
            this.gearElements.forEach(this::removeElement);
            this.gearElements.clear();
            this.addGearTab();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.confirming) {
            return this.confirmButton.mouseClicked(mouseX, mouseY, button) || this.cancelButton.mouseClicked(mouseX, mouseY, button) || true;
        }
        for (Button title : this.titleButtons) {
            if (title.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        if (button == 1 && this.hoveredSlot instanceof ArmoryMenu.PageSlot pageSlot) {
            this.click(ArmoryMenu.BUTTON_MARK + SlotKeys.indexOf(pageSlot.key()));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.openInfo.tab() == ArmoryTab.EXCESS) {
            this.menu.setExcessScroll(this.menu.excessScroll() - (int) Math.signum(delta));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.renderMarks(poseStack);
        if (!this.confirming) {
            this.renderPageSlotTooltip(poseStack, mouseX, mouseY);
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0, 0, 400);
        fill(poseStack, 0, 0, this.width, this.height, 0xA0101010);
        int popupWidth = 140;
        int popupX = (this.width - popupWidth) / 2;
        int popupY = this.height / 2 - 22;
        this.elementRenderer.render(ScreenTextures.DEFAULT_WINDOW_BACKGROUND, poseStack,
                Spatials.positionXYZ(popupX, popupY, 0).size(popupWidth, 48));
        this.font.draw(poseStack, "Overwrite Build " + (this.menu.page() + 1) + "?", popupX + 8, popupY + 8, 0xFF404040);
        this.confirmButton.render(poseStack, mouseX, mouseY, partialTick);
        this.cancelButton.render(poseStack, mouseX, mouseY, partialTick);
        poseStack.popPose();
    }

    private void renderMarks(PoseStack poseStack) {
        poseStack.pushPose();
        poseStack.translate(0, 0, 300);
        for (int i = 0; i < SlotKeys.size(); i++) {
            Slot slot = this.menu.slots.get(ArmoryMenu.PAGE_START + i);
            if (!slot.isActive() || !this.menu.state().isMarked(SlotKeys.ALL.get(i))) {
                continue;
            }
            int x = this.leftPos + slot.x - 1;
            int y = this.topPos + slot.y - 1;
            for (int step = 0; step < 4; step++) {
                fill(poseStack, x, y + step, x + 4 - step, y + step + 1, MARK_COLOR);
            }
        }
        poseStack.popPose();
    }

    private void renderPageSlotTooltip(PoseStack poseStack, int mouseX, int mouseY) {
        if (!(this.hoveredSlot instanceof ArmoryMenu.PageSlot pageSlot) || pageSlot.hasItem() || !this.menu.getCarried().isEmpty()) {
            return;
        }
        String key = pageSlot.key();
        List<Component> lines = new ArrayList<>();
        ItemStack expected = this.menu.state().expected(key);
        boolean marked = this.menu.state().isMarked(key);
        if (expected != null && !expected.isEmpty()) {
            lines.add(new TextComponent("Missing: ").append(expected.getHoverName()));
            lines.add(new TextComponent("Load skips this slot until the item is back").withStyle(ChatFormatting.GRAY));
        } else if (expected != null && marked) {
            lines.add(new TextComponent("Saved empty"));
            lines.add(new TextComponent("Load clears this slot").withStyle(ChatFormatting.GRAY));
        }
        lines.add(new TextComponent(marked ? "Marked: swaps on Load. Right-click to unmark"
                : "Not marked: Load leaves it alone. Right-click to mark").withStyle(ChatFormatting.DARK_GRAY));
        this.renderComponentTooltip(poseStack, lines, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
    }
}
