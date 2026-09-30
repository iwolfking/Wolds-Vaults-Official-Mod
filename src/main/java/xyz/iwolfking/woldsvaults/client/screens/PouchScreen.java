package xyz.iwolfking.woldsvaults.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.gear.trinket.TrinketEffect;
import iskallia.vault.gear.trinket.TrinketEffectRegistry;
import iskallia.vault.item.gear.TrinketItem;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;
import xyz.iwolfking.woldsvaults.client.invhud.PouchUseDisplay;
import xyz.iwolfking.woldsvaults.client.screens.widgets.PouchScrollBar;
import xyz.iwolfking.woldsvaults.effect.trinkets.SpeedLimitTrinketEffect;
import xyz.iwolfking.woldsvaults.items.trinket_pouch.PouchContents;
import xyz.iwolfking.woldsvaults.api.util.PouchHelper;
import xyz.iwolfking.woldsvaults.items.trinket_pouch.menu.PouchLayout;
import xyz.iwolfking.woldsvaults.items.trinket_pouch.menu.PouchGridScroll;
import xyz.iwolfking.woldsvaults.items.trinket_pouch.menu.PouchMenu;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.network.packets.ServerboundRenameTrinketPouchPresetPacket;

public final class PouchScreen extends AbstractContainerScreen<PouchMenu> {
    private static final int TEXT = 0x404040;
    private static final int MUTED = 0x666666;
    private static final String[] COLOR_NAMES = {"Red", "Blue", "Green"};
    private static final int[] COLORS = {0xAD4949, 0x397BAC, 0x548237};
    private final Map<String, ItemStack> catalog = new LinkedHashMap<>();
    private final Map<String, Integer> usablePresetCopies = new LinkedHashMap<>();
    private final Set<String> activePresetKeys = new HashSet<>();
    private List<Entry> entries = List.of();
    private String inspectedKey = "";
    private Component inspectedDescription = TextComponent.EMPTY;
    private int inspectedDescriptionY;
    private int inspectedDescriptionRows;
    private boolean keyboardNavigation;
    private final Set<GuiEventListener> pouchControls = new HashSet<>();
    private int colorFilter = -1;
    private final List<Button> colorFilters = new ArrayList<>();
    private final List<Button> cells = new ArrayList<>();
    private final List<Button> presetSelectors = new ArrayList<>();
    private final List<Button> tabs = new ArrayList<>();
    private final PouchGridScroll collectionScroll = new PouchGridScroll(PouchLayout.COLUMN_COUNT, PouchLayout.ROW_COUNT);
    private final PouchGridScroll presetScroll = new PouchGridScroll(PouchLayout.PREVIEW_COLUMNS, PouchLayout.PREVIEW_ROWS);
    private final List<PouchScrollBar> scrollBars = new ArrayList<>();
    private PouchScrollBar draggedScrollBar;
    private EditBox search;
    private EditBox presetName;
    private Button ownership;
    private Button autoReplace;
    private Button confirm;
    private Button cancel;
    private Button renamePreset;
    private Button savePreset;
    private Button applyPreset;
    private View view = View.COLLECTION;
    private Dialog dialog = Dialog.NONE;
    private String query = "";
    private String dialogError = "";
    private boolean ownedOnly = true;
    private int selectedPreset;
    private int dialogPreset;
    private int dismissedStatusRevision = -1;

    public PouchScreen(PouchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = PouchLayout.WIDTH;
        imageHeight = PouchLayout.HEIGHT;
    }

    @Override
    protected void init() {
        imageHeight = PouchLayout.HEIGHT;
        super.init();
        topPos = PouchLayout.panelTop(height, imageHeight);
        leftPos = PouchLayout.panelLeft(width);
        cells.clear();
        colorFilters.clear();
        tabs.clear();
        scrollBars.clear();
        draggedScrollBar = null;
        presetSelectors.clear();
        dialog = Dialog.NONE;
        for (View option : View.values()) {
            tabs.add(addRenderableWidget(new Button(leftPos - PouchLayout.TAB_WIDTH,
                    topPos + PouchLayout.tabY(option.ordinal()), PouchLayout.TAB_WIDTH, PouchLayout.TAB_HEIGHT,
                    label(option.label), ignored -> changeView(option)) {
                @Override
                public void renderButton(PoseStack pose, int mouseX, int mouseY, float partialTick) {
                    frame(pose, x, y, width, height);
                    if (view == option) {
                        fill(pose, x + 4, y + height - 5, x + width - 4, y + height - 3, 0xFF71865D);
                    }
                    renderTabIcon(pose, option, x + 4, y + 3);
                    if (isHoveredOrFocused()) outline(pose, x + 1, y + 1, width - 2, height - 2, 0xFFFFFFFF);
                }
            }));
        }
        search = addRenderableWidget(new EditBox(font, leftPos + 9, topPos + 10, 192, 14, label("search")));
        search.setMaxLength(64);
        search.setValue(query);
        search.setResponder(value -> {
            query = value;
            resetLaneScrolls();
            refreshEntries();
        });
        ownership = button(206, 9, 74, "owned", ignored -> {
            ownedOnly = !ownedOnly;
            resetLaneScrolls();
            refreshEntries();
        });
        for (int filter = -1; filter < 3; filter++) {
            final int selectedColor = filter;
            colorFilters.add(button(filter < 0 ? 8 : 50 + filter * 78, 30, filter < 0 ? 38 : 74,
                    filter < 0 ? "all" : COLOR_NAMES[filter].toLowerCase(Locale.ROOT), ignored -> {
                        colorFilter = selectedColor;
                        resetLaneScrolls();
                        refreshEntries();
                        updateControls();
                    }));
        }
        for (int index = 0; index < PouchLayout.VISIBLE_CELLS; index++) {
            final int cell = index;
            cells.add(addRenderableWidget(new Button(leftPos + cellX(index), topPos + cellY(index),
                    PouchLayout.CELL_SIZE, PouchLayout.CELL_SIZE, TextComponent.EMPTY,
                    ignored -> toggle(entry(cell))) {
                @Override
                public void renderButton(PoseStack pose, int mouseX, int mouseY, float partialTick) {
                    if (isHoveredOrFocused()) outline(pose, x, y, width, height, 0xFFFFFFFF);
                }
            }));
        }
        scrollBars.add(addRenderableWidget(new PouchScrollBar(leftPos + 172,
                topPos + PouchLayout.COLLECTION_Y, PouchLayout.COLLECTION_SCROLL_HEIGHT,
                label("trinkets").getString(), collectionScroll)));
        for (int preset = 0; preset < PouchContents.PRESET_COUNT; preset++) {
            final int index = preset;
            presetSelectors.add(addRenderableWidget(new Button(leftPos + 182 + preset * 32, topPos + 28, 29, 16,
                    text(String.valueOf(index + 1)), ignored -> {
                        dismissedStatusRevision = menu.statusRevision();
                        selectedPreset = index;
                        presetScroll.setFirstRow(0);
                        updateControls();
                    }) {
                @Override
                public void renderButton(PoseStack pose, int mouseX, int mouseY, float partialTick) {
                    smallButton(pose, x, y, width, height, active && isHoveredOrFocused());
                    if (menu.contents().appliedPreset() == index) {
                        fill(pose, x + 2, y + 2, x + width - 2, y + height - 2,
                                matchesPreset(index) ? 0xFF64715C : 0xFF82704E);
                    }
                    if (selectedPreset == index) outline(pose, x + 1, y + 1, width - 2, height - 2, 0xFFFFFFFF);
                    drawCenteredString(pose, font, getMessage(), x + width / 2, y + 4, 0xFFFFFF);
                }
            }));
        }
        renamePreset = button(226, 149, 49, 16, "rename", ignored -> openDialog(Dialog.RENAME, selectedPreset));
        savePreset = button(182, 149, 41, 16, "save", ignored -> {
            if (menu.contents().preset(selectedPreset).isEmpty()) action(PouchMenu.SAVE_PRESET + selectedPreset);
            else openDialog(Dialog.OVERWRITE, selectedPreset);
        });
        applyPreset = button(182, 128, 93, 16, "apply_preset", ignored -> action(PouchMenu.APPLY_PRESET + selectedPreset));
        autoReplace = button(11, PouchLayout.FOOTER_Y - 1, 12, "off", ignored -> action(PouchMenu.AUTO_REPLACE));
        scrollBars.add(addRenderableWidget(new PouchScrollBar(leftPos + 272, topPos + PouchLayout.PREVIEW_Y,
                PouchLayout.PREVIEW_SCROLL_HEIGHT,
                label("preset_trinkets").getString(), presetScroll)));
        presetName = addRenderableWidget(new EditBox(font, leftPos + 51, topPos + 92, 166, 14, label("preset_name")));
        presetName.setMaxLength(PouchContents.MAX_PRESET_NAME_LENGTH);
        presetName.setResponder(ignored -> dialogError = "");
        confirm = button(152, 116, 65, "save", ignored -> confirmDialog());
        cancel = button(81, 116, 65, "cancel", ignored -> closeDialog());
        refreshEntries();
        updateControls();
        pouchControls.clear();
        pouchControls.addAll(children());
    }

    private Button button(int x, int y, int width, String label, Button.OnPress action) {
        return button(x, y, width, 14, label, action);
    }

    private Button button(int x, int y, int width, int height, String label, Button.OnPress action) {
        return addRenderableWidget(new Button(leftPos + x, topPos + y, width, height, label(label), action) {
            @Override
            public void renderButton(PoseStack pose, int mouseX, int mouseY, float partialTick) {
                smallButton(pose, this.x, this.y, this.width, height, active && isHoveredOrFocused());
                if (this == autoReplace) {
                    if (menu.contents().autoReplace()) {
                        drawCheckboxTick(pose, this.x + (width - 8) / 2, this.y + (height - 7) / 2,
                                active ? 0xFFFFFFFF : 0xFFAAAAAA);
                    }
                    return;
                }
                drawCenteredString(pose, font, fit(getMessage().getString(), this.width - 6), this.x + this.width / 2, this.y + (height - 8) / 2, active ? 0xFFFFFF : 0xAAAAAA);
            }
        });
    }

    private static Component text(String value) { return new TextComponent(value); }
    private static Component label(String key, Object... arguments) {
        return new TranslatableComponent("gui.woldsvaults.pouch." + key, arguments);
    }
    private static int cellX(int cell) { return PouchLayout.COLLECTION_X + cell % PouchLayout.COLUMN_COUNT * PouchLayout.CELL_PITCH; }
    private static int cellY(int cell) { return PouchLayout.COLLECTION_Y + cell / PouchLayout.COLUMN_COUNT * PouchLayout.ROW_PITCH; }

    private void changeView(View next) {
        if (!menu.getCarried().isEmpty()) {
            return;
        }
        dismissedStatusRevision = menu.statusRevision();
        view = next;
        menu.setStoredView(view == View.STORAGE);
        search.setFocus(false);
        setFocused(null);
        clearWidgets();
        init();
        updateControls();
    }

    private void resetLaneScrolls() { collectionScroll.setFirstRow(0); }

    private void action(int action) {
        if (menu.canChangeLoadout() && minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, action);
        }
    }

    private void toggle(Entry entry) {
        if (entry != null && chosenIndex(entry) >= 0) {
            search.setFocus(false);
            action(PouchMenu.TOGGLE_TRINKET + chosenIndex(entry));
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        search.tick();
        presetName.tick();
        if (!menu.canChangeLoadout() && dialog != Dialog.NONE) {
            closeDialog();
        }
        refreshEntries();
        updateControls();
    }

    private void refreshEntries() {
        if (catalog.isEmpty()) {
            for (TrinketEffect<?> effect : TrinketEffectRegistry.getOrderedEntries()) {
                if (effect.getConfig().hasCuriosSlot() && PouchHelper.COLORS.contains(effect.getConfig().getCuriosSlot())) {
                    ItemStack icon = TrinketItem.createBaseTrinket(effect);
                    catalog.put(PouchHelper.effectKey(icon), icon);
                }
            }
        }
        Map<String, Entry> combined = new LinkedHashMap<>();
        catalog.forEach((key, icon) -> combined.put(key, new Entry(icon, new ArrayList<>())));
        for (int slot = 0; slot < PouchContents.SIZE; slot++) {
            ItemStack stack = menu.contents().getStackInSlot(slot);
            if (PouchHelper.isTrinket(stack)) {
                combined.computeIfAbsent(PouchHelper.effectKey(stack), ignored -> new Entry(stack, new ArrayList<>())).indices().add(slot);
            }
        }
        entries = combined.values().stream()
                .filter(entry -> colorFilter < 0 || PouchHelper.COLORS.get(colorFilter).equals(PouchHelper.color(entry.icon())))
                .filter(entry -> !ownedOnly || !entry.indices().isEmpty())
                .filter(entry -> entry.icon().getHoverName().getString().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)))
                .sorted(Comparator.comparing(entry -> entry.icon().getHoverName().getString())).toList();
        collectionScroll.setEntryCount(entries.size());
        if (entries.stream().noneMatch(entry -> PouchHelper.effectKey(entry.icon()).equals(inspectedKey))) inspectedKey = "";
    }

    private Entry entry(int cell) {
        int index = collectionScroll.entryIndex(cell);
        return index >= 0 ? entries.get(index) : null;
    }

    private int chosenIndex(Entry entry) {
        for (int index : entry.indices()) {
            if (menu.contents().isActive(index)) return index;
        }
        return entry.indices().stream().min(Comparator.comparingInt(index -> {
            int uses = PouchHelper.remainingUses(menu.contents().getStackInSlot(index));
            return uses == 0 ? Integer.MAX_VALUE : uses;
        })).orElse(-1);
    }

    public Slot trinketConfigurationSlot() {
        if (minecraft == null || dialog != Dialog.NONE || !menu.getCarried().isEmpty()
                || search.visible && search.isFocused()) {
            return null;
        }
        if (view == View.STORAGE) {
            return keyboardNavigation ? null : getSlotUnderMouse();
        }
        int mouseX = (int) (minecraft.mouseHandler.xpos() * width / minecraft.getWindow().getScreenWidth());
        int mouseY = (int) (minecraft.mouseHandler.ypos() * height / minecraft.getWindow().getScreenHeight());
        int cell = collectionInspectedCell(mouseX, mouseY);
        Entry inspected = cell >= 0 ? entry(cell) : null;
        int storedIndex = inspected == null ? -1 : chosenIndex(inspected);
        return storedIndex >= 0 ? menu.getSlot(storedIndex) : null;
    }

    private void updateControls() {
        boolean modal = dialog != Dialog.NONE;
        boolean collection = view == View.COLLECTION;
        search.visible = ownership.visible = collection;
        search.setEditable(!modal);
        search.setSuggestion(query.isEmpty() ? label("search").getString() : "");
        ownership.active = !modal;
        ownership.setMessage(label(ownedOnly ? "owned" : "catalog"));
        tabs.forEach(tab -> tab.active = !modal && menu.getCarried().isEmpty());
        scrollBars.get(0).visible = collection && collectionScroll.canScroll();
        scrollBars.get(0).active = !modal;
        for (int filter = -1; filter < 3; filter++) {
            Button control = colorFilters.get(filter + 1);
            control.visible = collection;
            control.active = !modal;
            Component name = label(filter < 0 ? "all" : COLOR_NAMES[filter].toLowerCase(Locale.ROOT));
            String count = "";
            if (filter >= 0) {
                String color = PouchHelper.COLORS.get(filter);
                long active = menu.contents().activeStacks().stream().filter(stack -> color.equals(PouchHelper.color(stack))).count();
                count = " " + active + "/" + PouchHelper.capacities(menu.pouch()).getOrDefault(color, 0);
            }
            control.setMessage(text((colorFilter == filter ? "> " : "") + name.getString() + count));
        }
        for (int cell = 0; cell < PouchLayout.VISIBLE_CELLS; cell++) {
            Button button = cells.get(cell);
            Entry entry = entry(cell);
            button.visible = collection && entry != null;
            button.active = !modal;
            if (entry != null) button.setMessage(entry.icon().getHoverName());
        }
        for (int preset = 0; preset < presetSelectors.size(); preset++) {
            Button control = presetSelectors.get(preset);
            control.visible = view == View.STORAGE;
            control.active = !modal;
            control.setMessage(text(String.valueOf(preset + 1)));
        }
        for (Button control : List.of(renamePreset, savePreset, applyPreset)) {
            control.visible = view == View.STORAGE;
            control.active = !modal && menu.canChangeLoadout();
        }
        savePreset.setMessage(label(feedbackVisible() && menu.feedback() == PouchMenu.Feedback.SAVED ? "preset_saved" : "save"));
        renamePreset.setMessage(label(feedbackVisible() && menu.feedback() == PouchMenu.Feedback.RENAMED ? "preset_renamed" : "rename"));
        applyPreset.setMessage(label(feedbackVisible() && menu.feedback() == PouchMenu.Feedback.APPLIED ? "preset_applied" : "apply_preset"));
        presetScroll.setEntryCount(menu.contents().preset(selectedPreset).size());
        scrollBars.get(1).visible = view == View.STORAGE && presetScroll.canScroll();
        scrollBars.get(1).active = !modal;
        autoReplace.visible = true;
        autoReplace.active = !modal && !menu.isLocked();
        autoReplace.setMessage(text(menu.contents().autoReplace() ? "x" : ""));
        presetName.visible = dialog == Dialog.RENAME;
        confirm.visible = cancel.visible = modal;
        confirm.active = menu.canChangeLoadout();
        confirm.setMessage(label(dialog == Dialog.OVERWRITE ? "replace" : "save"));
    }

    private void openDialog(Dialog next, int preset) {
        dialog = next;
        dialogPreset = preset;
        dialogError = "";
        search.setFocus(false);
        presetName.setValue(menu.contents().presetName(preset));
        updateControls();
        if (next == Dialog.RENAME) {
            setFocused(presetName);
            presetName.setFocus(true);
        }
    }

    private void closeDialog() {
        dialog = Dialog.NONE;
        presetName.setFocus(false);
        setFocused(null);
        updateControls();
    }

    private void confirmDialog() {
        if (!menu.canChangeLoadout()) return;
        if (dialog == Dialog.RENAME) {
            try {
                String name = PouchContents.validatedPresetName(presetName.getValue());
                ModNetwork.sendToServer(new ServerboundRenameTrinketPouchPresetPacket(menu.containerId, dialogPreset, name));
            } catch (IllegalArgumentException exception) {
                dialogError = label("invalid_name").getString();
                return;
            }
        } else if (dialog == Dialog.OVERWRITE) {
            selectedPreset = dialogPreset;
            action(PouchMenu.SAVE_PRESET + dialogPreset);
        }
        closeDialog();
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        removeOverlappingExternalControls();
        if (view == View.STORAGE) refreshPresetCopies();
        int inspectedCell = collectionInspectedCell(mouseX, mouseY);
        if (view == View.COLLECTION && inspectedCell >= 0) {
            inspectedKey = PouchHelper.effectKey(entry(inspectedCell).icon());
        }
        renderBackground(pose);
        super.render(pose, mouseX, mouseY, partialTick);
        if (dialog != Dialog.NONE) {
            pose.pushPose();
            pose.translate(0, 0, 400);
            fill(pose, 0, 0, width, height, 0xA0000000);
            frame(pose, leftPos + 42, topPos + 67, 184, 72);
            font.draw(pose, dialog == Dialog.RENAME ? label("rename_title").getString() : label("overwrite_title").getString(), leftPos + 51, topPos + 77, TEXT);
            if (dialog == Dialog.OVERWRITE) {
                font.draw(pose, fit(menu.contents().presetName(dialogPreset), 164), leftPos + 51, topPos + 96, MUTED);
            } else {
                presetName.render(pose, mouseX, mouseY, partialTick);
            }
            confirm.render(pose, mouseX, mouseY, partialTick);
            cancel.render(pose, mouseX, mouseY, partialTick);
            if (!dialogError.isEmpty()) font.draw(pose, dialogError, leftPos + 45, topPos + 145, 0xFF9999);
            pose.popPose();
            return;
        }
        if (!renderHints(pose, mouseX, mouseY) && !keyboardNavigation) {
            renderTooltip(pose, mouseX, mouseY);
        }
    }

    private void removeOverlappingExternalControls() {
        for (GuiEventListener listener : List.copyOf(children())) {
            if (!pouchControls.contains(listener) && listener instanceof AbstractWidget widget
                    && widget.x < leftPos + imageWidth && widget.x + widget.getWidth() > leftPos - PouchLayout.TAB_WIDTH
                    && widget.y < topPos + imageHeight && widget.y + widget.getHeight() > topPos) {
                removeWidget(listener);
            }
        }
    }

    private int collectionInspectedCell(int mouseX, int mouseY) {
        for (int index = 0; index < cells.size(); index++) {
            Button cell = cells.get(index);
            if (cell.visible && keyboardNavigation && cell.isFocused() && entry(index) != null) return index;
        }
        if (!keyboardNavigation) {
            for (int index = 0; index < cells.size(); index++) {
                if (cells.get(index).isMouseOver(mouseX, mouseY) && entry(index) != null) return index;
            }
        }
        return -1;
    }

    @Override
    protected void renderBg(PoseStack pose, float partialTick, int mouseX, int mouseY) {
        frame(pose, leftPos, topPos, imageWidth, imageHeight);
        recessed(pose, leftPos + 8, topPos + 175, 272, 19);
        if (view == View.COLLECTION) {
            recessed(pose, leftPos + 8, topPos + 49, 172, 123);
            recessed(pose, leftPos + 184, topPos + 49, 96, 123);
            for (int cell = 0; cell < PouchLayout.VISIBLE_CELLS; cell++) {
                Entry entry = entry(cell);
                if (entry == null) continue;
                int x = leftPos + cellX(cell);
                int y = topPos + cellY(cell);
                int chosen = chosenIndex(entry);
                boolean selected = chosen >= 0 && menu.contents().isActive(chosen);
                collectionSlot(pose, x, y, selected);
                int color = PouchHelper.COLORS.indexOf(PouchHelper.color(entry.icon()));
                if (color >= 0) fill(pose, x + 1, y + 1, x + 19, y + 2, 0xFF000000 | COLORS[color]);
                itemRenderer.renderAndDecorateItem(entry.icon(), x + 2, y + 2);
                RenderSystem.disableDepthTest();
                if (chosen < 0) fill(pose, x + 2, y + 2, x + 18, y + 18, 0xAA8B8B8B);
                else if (PouchHelper.remainingUses(menu.contents().getStackInSlot(chosen)) == 0) {
                    fill(pose, x + 2, y + 2, x + 18, y + 18, 0x88883333);
                }
                if (selected) check(pose, x + 1, y + 12);
                RenderSystem.enableDepthTest();
            }
        } else if (view == View.STORAGE) {
            for (Slot slot : menu.slots) {
                if (slot.isActive()) inset(pose, leftPos + slot.x - 1, topPos + slot.y - 1);
            }
            recessed(pose, leftPos + PouchLayout.PRESET_PANEL_X, topPos + PouchLayout.PRESET_PANEL_Y,
                    PouchLayout.PRESET_PANEL_WIDTH, PouchLayout.PREVIEW_PANEL_HEIGHT);
            for (int cell = 0; cell < PouchLayout.PREVIEW_COLUMNS * PouchLayout.PREVIEW_ROWS; cell++) {
                PresetEntry entry = presetEntry(cell);
                if (entry == null) continue;
                ItemStack stack = entry.icon();
                int x = leftPos + previewX(cell);
                int y = topPos + previewY(cell);
                collectionSlot(pose, x, y, false);
                int lane = PouchHelper.COLORS.indexOf(PouchHelper.color(stack));
                if (lane >= 0) fill(pose, x + 1, y + 1, x + 19, y + 2, 0xFF000000 | COLORS[lane]);
                itemRenderer.renderAndDecorateItem(stack, x + 2, y + 2);
                if (entry.missing()) {
                    RenderSystem.disableDepthTest();
                    fill(pose, x + 2, y + 2, x + 18, y + 18, 0xAA8B8B8B);
                    RenderSystem.enableDepthTest();
                }
            }
        }
    }

    private static int previewX(int cell) { return PouchLayout.PREVIEW_X + cell % PouchLayout.PREVIEW_COLUMNS * PouchLayout.PREVIEW_PITCH; }
    private static int previewY(int cell) { return PouchLayout.PREVIEW_Y + cell / PouchLayout.PREVIEW_COLUMNS * PouchLayout.PREVIEW_PITCH; }

    private void refreshPresetCopies() {
        usablePresetCopies.clear();
        activePresetKeys.clear();
        for (int index = 0; index < PouchContents.SIZE; index++) {
            ItemStack stack = menu.contents().getStackInSlot(index);
            if (!PouchHelper.isTrinket(stack)) continue;
            String key = PouchHelper.effectKey(stack);
            if (menu.contents().isActive(index)) activePresetKeys.add(key);
            if (PouchHelper.remainingUses(stack) == 0) continue;
            Integer previous = usablePresetCopies.get(key);
            if (previous == null || !menu.contents().isActive(previous)
                    && (menu.contents().isActive(index) || PouchHelper.remainingUses(stack)
                    < PouchHelper.remainingUses(menu.contents().getStackInSlot(previous)))) {
                usablePresetCopies.put(key, index);
            }
        }
    }

    private record PresetEntry(ItemStack icon, boolean missing) {}

    private PresetEntry presetEntry(int cell) {
        List<String> keys = menu.contents().preset(selectedPreset);
        presetScroll.setEntryCount(keys.size());
        int index = presetScroll.entryIndex(cell);
        if (index < 0) return null;
        String key = keys.get(index);
        int stored = usablePresetCopies.getOrDefault(key, -1);
        if (stored >= 0) return new PresetEntry(menu.contents().getStackInSlot(stored), false);
        ItemStack icon = catalog.get(key);
        if (icon == null) icon = new ItemStack(Items.BARRIER).setHoverName(text(key));
        return new PresetEntry(icon, true);
    }

    private boolean matchesPreset(int preset) {
        List<String> desired = menu.contents().preset(preset);
        return !desired.isEmpty() && new HashSet<>(desired).equals(activePresetKeys);
    }

    private Component presetState(int preset) {
        List<String> desired = menu.contents().preset(preset);
        if (desired.isEmpty()) return label("preset_empty");
        if (menu.contents().appliedPreset() == preset) {
            if (matchesPreset(preset)) return label("active");
            long selected = desired.stream().filter(activePresetKeys::contains).count();
            return label("preset_partial", selected, desired.size());
        }
        if (matchesPreset(preset)) return label("preset_same_loadout");
        return label(menu.contents().appliedPreset() < 0 && !activePresetKeys.isEmpty() ? "preset_custom" : "not_matching");
    }

    private int presetStateColor() {
        if (menu.contents().appliedPreset() == selectedPreset) return matchesPreset(selectedPreset) ? 0x435B33 : 0x8C5C11;
        return MUTED;
    }

    private boolean feedbackVisible() {
        return menu.statusPreset() == selectedPreset && menu.statusRevision() != dismissedStatusRevision && menu.hasRecentFeedback();
    }

    private int missingPresetEntries() {
        return (int) menu.contents().preset(selectedPreset).stream().filter(key -> !usablePresetCopies.containsKey(key)).count();
    }

    private String selectionError(Entry entry) {
        int chosen = chosenIndex(entry);
        if (chosen < 0) return label("unowned").getString();
        if (menu.contents().isActive(chosen)) return "";
        List<Integer> proposed = new ArrayList<>(menu.contents().activeIndices());
        proposed.add(chosen);
        return PouchHelper.validate(menu.pouch(), menu.contents(), proposed, minecraft.player, !menu.inStartRoom());
    }

    @Override
    protected void renderLabels(PoseStack pose, int mouseX, int mouseY) {
        boolean showStatus = menu.isLocked() || !menu.isEquipped();
        font.draw(pose, fit(label("auto_replace").getString(), showStatus ? 175 : 249),
                27, PouchLayout.FOOTER_Y + 3, MUTED);
        if (showStatus) {
            String status = fit(label(footerStatus()).getString(), 72);
            font.draw(pose, status, 276 - font.width(status), PouchLayout.FOOTER_Y + 3,
                    menu.inStartRoom() ? 0x3F6B2A : menu.isLocked() ? 0x992222 : MUTED);
        }
        if (view == View.COLLECTION) {
            if (entries.isEmpty()) font.draw(pose, label("no_matches"), 16, 59, MUTED);
            renderInspector(pose);
        } else if (view == View.STORAGE) {
            font.draw(pose, label("storage_caption"), PouchLayout.GRID_X, PouchLayout.GRID_Y - 10, TEXT);
            String stored = (PouchContents.SIZE - menu.contents().emptySlots()) + "/27";
            font.draw(pose, stored, PouchLayout.GRID_X + 162 - font.width(stored), PouchLayout.GRID_Y - 10, MUTED);
            font.draw(pose, playerInventoryTitle, PouchLayout.GRID_X, PouchLayout.INVENTORY_Y - 10, TEXT);

            font.draw(pose, label("presets"), 181, 14, TEXT);
            font.draw(pose, fit(menu.contents().presetName(selectedPreset), 92), 183, 51, TEXT);
            if (menu.contents().preset(selectedPreset).isEmpty()) {
                drawWrapped(pose, label("empty_preset"), 183, 76, 92, 2, MUTED);
            }
            boolean error = feedbackVisible() && menu.feedback() == PouchMenu.Feedback.ERROR;
            String feedback = error ? menu.status() : presetState(selectedPreset).getString();
            font.draw(pose, fit(feedback, 92), 183, 113, error ? 0x992222 : presetStateColor());
        }
    }

    private void renderInspector(PoseStack pose) {
        Entry inspected = entries.stream().filter(entry -> PouchHelper.effectKey(entry.icon()).equals(inspectedKey)).findFirst().orElse(null);
        inspectedDescription = TextComponent.EMPTY;
        if (inspected == null) {
            drawWrapped(pose, label("inspect_hint"), 190, 58, 84, 8, MUTED);
            return;
        }

        Component name = inspected.icon().getHoverName();
        int chosen = chosenIndex(inspected);
        ItemStack inspectedStack = chosen >= 0 ? menu.contents().getStackInSlot(chosen) : inspected.icon();
        Component description = description(inspectedStack, chosen >= 0);
        int descriptionY = 58 + Math.min(2, font.split(name, 84).size()) * 10 + 4;
        int descriptionRows = Math.min(5, font.split(description, 84).size());
        drawWrapped(pose, name, 190, 58, 84, 2, TEXT);
        drawWrapped(pose, description, 190, descriptionY, 84, 5, MUTED);
        inspectedDescription = description;
        inspectedDescriptionY = descriptionY;
        inspectedDescriptionRows = descriptionRows;
        int usesY = descriptionY + descriptionRows * 10 + 6;
        if (chosen >= 0) {
            font.draw(pose, fit(label("uses", PouchUseDisplay.remainingUses(menu.contents(), menu.contents().getStackInSlot(chosen))).getString(), 84),
                    190, usesY, TEXT);
        }
        boolean active = chosen >= 0 && menu.contents().isActive(chosen);
        Component reason = activationBlock(inspected);
        Component state = active ? label("active") : reason.getString().isEmpty() ? label("inactive") : reason;
        drawWrapped(pose, state, 190, usesY + (chosen >= 0 ? 14 : 0), 84, 2,
                active ? 0x435B33 : reason.getString().isEmpty() ? MUTED : 0x883333);
    }

    private String footerStatus() {
        return menu.inStartRoom() ? "start_room" : menu.isLocked() ? "locked" : "unequipped";
    }

    private Component activationBlock(Entry entry) {
        int chosen = chosenIndex(entry);
        if (chosen < 0) return label("not_in_pouch");
        if (!menu.canChangeLoadout()) return label("locked");
        if (menu.inStartRoom() && PouchHelper.isTimeExtension(menu.contents().getStackInSlot(chosen))) return label("time_fixed");
        if (menu.contents().isActive(chosen)) return TextComponent.EMPTY;
        if (PouchHelper.remainingUses(menu.contents().getStackInSlot(chosen)) == 0) return label("exhausted");
        String error = selectionError(entry);
        if (error.startsWith("No free ")) {
            int color = PouchHelper.COLORS.indexOf(PouchHelper.color(entry.icon()));
            return label("slots_full", label(COLOR_NAMES[color].toLowerCase(Locale.ROOT)));
        }
        return switch (error) {
            case "This effect is already active" -> label("effect_active");
            case "This trinket cannot be equipped right now" -> label("cannot_equip");
            default -> text(error);
        };
    }

    private void drawWrapped(PoseStack pose, Component value, int x, int y, int width, int rows, int color) {
        List<FormattedCharSequence> lines = font.split(value, width);
        int visibleRows = lines.size() > rows ? rows - 1 : lines.size();
        for (int index = 0; index < visibleRows; index++) font.draw(pose, lines.get(index), x, y + index * 10, color);
        if (lines.size() > rows) font.draw(pose, "...", x, y + (rows - 1) * 10, color);
    }

    private boolean renderHints(PoseStack pose, int mouseX, int mouseY) {
        if (view == View.COLLECTION) {
            int index = collectionInspectedCell(mouseX, mouseY);
            if (index >= 0) {
                Button cell = cells.get(index);
                if (keyboardNavigation) {
                    mouseX = cell.x + cell.getWidth();
                    mouseY = cell.y;
                }
                Entry entry = entry(index);
                int chosen = chosenIndex(entry);
                Component blocked = activationBlock(entry);
                Component action = blocked.getString().isEmpty()
                        ? label(menu.contents().isActive(chosen) ? "deactivate" : "activate").copy().withStyle(ChatFormatting.YELLOW)
                        : blocked.copy().withStyle(ChatFormatting.RED);
                tooltip(pose, mouseX, mouseY, index % PouchLayout.COLUMN_COUNT >= PouchLayout.COLUMN_COUNT / 2,
                        entry.icon().getHoverName(), action);
                return true;
            }
            if (!inspectedDescription.getString().isBlank()
                    && isMouseHintInside(mouseX, mouseY, 190, inspectedDescriptionY, 84, inspectedDescriptionRows * 10)) {
                tooltip(pose, mouseX, mouseY, true, inspectedDescription.copy().withStyle(ChatFormatting.GRAY));
                return true;
            }
            if (isHintTarget(ownership, mouseX, mouseY)) {
                tooltip(pose, mouseX, mouseY, ownedOnly ? label("show_catalog").getString() : label("show_owned").getString());
                return true;
            }
        }
        if (view == View.STORAGE) {
            for (int index = 0; index < presetSelectors.size(); index++) {
                if (isHintTarget(presetSelectors.get(index), mouseX, mouseY)) {
                    tooltip(pose, mouseX, mouseY, text(menu.contents().presetName(index)), presetState(index), label("preview_hint"));
                    return true;
                }
            }
            for (int cell = 0; cell < PouchLayout.PREVIEW_COLUMNS * PouchLayout.PREVIEW_ROWS; cell++) {
                PresetEntry entry = presetEntry(cell);
                if (entry != null && isMouseHintInside(mouseX, mouseY, previewX(cell), previewY(cell), 20, 20)) {
                    if (entry.missing()) tooltip(pose, mouseX, mouseY, entry.icon().getHoverName(),
                            label("preset_missing").copy().withStyle(ChatFormatting.RED));
                    else renderCompactTrinketTooltip(pose, entry.icon(), mouseX, mouseY);
                    return true;
                }
            }
            if (isHintTarget(renamePreset, mouseX, mouseY)) {
                tooltip(pose, mouseX, mouseY, label("rename_hint").getString());
                return true;
            }
            if (isHintTarget(savePreset, mouseX, mouseY)) {
                tooltip(pose, mouseX, mouseY, label("save_hint").getString());
                return true;
            }
            if (isHintTarget(applyPreset, mouseX, mouseY)) {
                tooltip(pose, mouseX, mouseY, label("apply_hint").getString());
                return true;
            }

            if (isMouseHintInside(mouseX, mouseY, 183, 51, 92, 10)) {
                tooltip(pose, mouseX, mouseY, menu.contents().presetName(selectedPreset));
                return true;
            }
            if (isMouseHintInside(mouseX, mouseY, 183, 111, 92, 13)) {
                List<Component> lines = new ArrayList<>();
                lines.add(presetState(selectedPreset));
                List<String> desired = menu.contents().preset(selectedPreset);
                lines.add(label("preset_details", desired.size(), desired.stream().filter(activePresetKeys::contains).count()));
                int missing = missingPresetEntries();
                if (missing > 0) lines.add(label("preset_missing_count", missing).copy().withStyle(ChatFormatting.RED));
                int applied = menu.contents().appliedPreset();
                lines.add(applied < 0 ? label("preset_custom") : label("preset_current", menu.contents().presetName(applied)));
                if (feedbackVisible() && menu.feedback() == PouchMenu.Feedback.ERROR) lines.add(text(menu.status()).copy().withStyle(ChatFormatting.RED));
                tooltip(pose, mouseX, mouseY, lines.toArray(Component[]::new));
                return true;
            }
        }
        for (int index = 0; index < scrollBars.size(); index++) {
            PouchScrollBar bar = scrollBars.get(index);
            if (bar.visible && isHintTarget(bar, mouseX, mouseY)) {
                PouchGridScroll scroll = index == 0 ? collectionScroll : presetScroll;
                tooltip(pose, mouseX, mouseY, label("scroll_hint").getString(), label("scroll_row", scroll.firstRow() + 1, scroll.totalRows()).getString());
                return true;
            }
        }
        for (Button tab : tabs) {
            if (isHintTarget(tab, mouseX, mouseY)) {
                tooltip(pose, mouseX, mouseY, tab.getMessage());
                return true;
            }
        }
        if (isHintTarget(autoReplace, mouseX, mouseY) || isMouseHintInside(mouseX, mouseY, 26, PouchLayout.FOOTER_Y, 175, 14)) {
            tooltip(pose, mouseX, mouseY, label(menu.isLocked() ? footerStatus() + "_hint" : "auto_replace_hint"));
            return true;
        } else if ((menu.isLocked() || !menu.isEquipped()) && isMouseHintInside(mouseX, mouseY, 204, PouchLayout.FOOTER_Y, 76, 14)) {
            tooltip(pose, mouseX, mouseY, label(footerStatus() + "_hint"));
            return true;
        }
        return false;
    }

    private boolean isHintTarget(AbstractWidget widget, int mouseX, int mouseY) {
        return widget.visible && (keyboardNavigation ? widget.isFocused() : widget.isMouseOver(mouseX, mouseY));
    }

    private boolean isMouseHintInside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return !keyboardNavigation && inside(mouseX, mouseY, x, y, width, height);
    }

    private void tooltip(PoseStack pose, int mouseX, int mouseY, String... lines) {
        Component[] components = new Component[lines.length];
        for (int index = 0; index < lines.length; index++) {
            components[index] = new TextComponent(lines[index]).withStyle(index == 0 ? ChatFormatting.YELLOW : ChatFormatting.GRAY);
        }
        tooltip(pose, mouseX, mouseY, components);
    }

    private void tooltip(PoseStack pose, int mouseX, int mouseY, Component... lines) {
        tooltip(pose, mouseX, mouseY, false, lines);
    }

    private void tooltip(PoseStack pose, int mouseX, int mouseY, boolean leftOfCursor, Component... lines) {
        int wrapWidth = leftOfCursor ? Math.max(1, Math.min(220, mouseX - 36))
                : Math.max(80, Math.min(220, width - 30));
        List<FormattedCharSequence> wrapped = new ArrayList<>();
        for (Component line : lines) {
            if (line.getString().isBlank()) wrapped.add(FormattedCharSequence.EMPTY);
            else wrapped.addAll(font.split(line, wrapWidth));
        }
        int tooltipWidth = Math.max(48, wrapped.stream().mapToInt(font::width).max().orElse(0));
        int anchorX = leftOfCursor ? Math.max(0, mouseX - tooltipWidth - 32)
                : Math.max(0, Math.min(mouseX, width - tooltipWidth - 32));
        int anchorY = Math.max(16, Math.min(mouseY, height - 4));
        renderTooltip(pose, wrapped, anchorX, anchorY);
    }

    private static Component description(ItemStack stack, boolean includeCopySettings) {
        return new TextComponent(PouchHelper.effects(stack).stream()
                .map(entry -> entry.trinket() instanceof SpeedLimitTrinketEffect && includeCopySettings
                        ? speedLimit(stack).getString() + "\n" + effectText(entry.trinket()) : effectText(entry.trinket()))
                .filter(value -> !value.isBlank())
                .distinct().reduce((first, second) -> first + "\n" + second).orElse(""));
    }

    private static String effectText(TrinketEffect<?> effect) {
        String configured = effect.getTrinketConfig() == null ? null : effect.getTrinketConfig().getEffectText();
        return configured == null ? "" : configured.strip();
    }

    private static Component speedLimit(ItemStack stack) {
        int capPercent = SpeedLimitTrinketEffect.getCapPercent(stack);
        return new TranslatableComponent("gui.woldsvaults.pouch.speed_limit", capPercent == 0
                ? new TranslatableComponent("gui.woldsvaults.pouch.speed_uncapped") : new TextComponent(capPercent + "%"));
    }

    private List<Component> trinketTooltip(ItemStack stack) {
        List<Component> lines = new ArrayList<>();
        lines.add(stack.getHoverName().copy());
        Component description = description(stack, true);
        if (!description.getString().isBlank()) lines.add(description.copy().withStyle(ChatFormatting.GRAY));
        lines.add(TextComponent.EMPTY);
        return lines;
    }

    private void renderCompactTrinketTooltip(PoseStack pose, ItemStack stack, int mouseX, int mouseY) {
        List<Component> lines = trinketTooltip(stack);
        if (!TrinketItem.isIdentified(stack)) {
            lines.add(label("identify_first").copy().withStyle(ChatFormatting.GRAY));
        } else {
            lines.add(label("uses", PouchHelper.remainingUses(stack)).copy().withStyle(ChatFormatting.GRAY));
            boolean active = menu.contents().activeStacks().stream().anyMatch(stored -> stored == stack);
            lines.add(label(active ? "active" : PouchHelper.remainingUses(stack) == 0 ? "exhausted" : "inactive")
                    .copy().withStyle(active ? ChatFormatting.GREEN : ChatFormatting.GRAY));
        }
        tooltip(pose, mouseX, mouseY, lines.toArray(Component[]::new));
    }

    @Override
    protected void renderTooltip(PoseStack pose, int mouseX, int mouseY) {
        if (menu.getCarried().isEmpty() && hoveredSlot != null && hoveredSlot.hasItem()
                && hoveredSlot.getItem().getItem() instanceof TrinketItem) {
            renderCompactTrinketTooltip(pose, hoveredSlot.getItem(), mouseX, mouseY);
            return;
        }
        super.renderTooltip(pose, mouseX, mouseY);
    }

    @Override
    public void mouseMoved(double x, double y) {
        keyboardNavigation = false;
        super.mouseMoved(x, y);
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        keyboardNavigation = false;
        if (dialog != Dialog.NONE) {
            if (presetName.visible && presetName.mouseClicked(x, y, button)) {
                setFocused(presetName);
                return true;
            }
            confirm.mouseClicked(x, y, button);
            if (dialog != Dialog.NONE) cancel.mouseClicked(x, y, button);
            return true;
        }
        for (PouchScrollBar bar : scrollBars) {
            if (bar.mouseClicked(x, y, button)) {
                draggedScrollBar = bar;
                search.setFocus(false);
                setFocused(bar);
                updateControls();
                return true;
            }
        }
        return super.mouseClicked(x, y, button);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top, int button) {
        for (int index = 0; index < tabs.size(); index++) {
            if (inside(mouseX, mouseY, -PouchLayout.TAB_WIDTH, PouchLayout.tabY(index),
                    PouchLayout.TAB_WIDTH, PouchLayout.TAB_HEIGHT)) return false;
        }
        return super.hasClickedOutside(mouseX, mouseY, left, top, button);
    }

    @Override
    public boolean mouseDragged(double x, double y, int button, double deltaX, double deltaY) {
        if (dialog != Dialog.NONE) return true;
        if (button == 0 && draggedScrollBar != null) {
            draggedScrollBar.dragTo(y);
            updateControls();
            return true;
        }
        return super.mouseDragged(x, y, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        if (draggedScrollBar != null && button == 0) {
            draggedScrollBar = null;
            return true;
        }
        return dialog != Dialog.NONE || super.mouseReleased(x, y, button);
    }

    private boolean inside(double x, double y, int left, int top, int width, int height) {
        return x >= leftPos + left && x < leftPos + left + width && y >= topPos + top && y < topPos + top + height;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double amount) {
        if (dialog != Dialog.NONE) return true;
        if (view == View.COLLECTION && inside(x, y, 8, 49, 172, 123)) {
            collectionScroll.scroll(amount < 0 ? 1 : amount > 0 ? -1 : 0);
            updateControls();
            return true;
        } else if (view == View.STORAGE && inside(x, y, PouchLayout.PRESET_PANEL_X, PouchLayout.PREVIEW_Y,
                PouchLayout.PRESET_PANEL_WIDTH, PouchLayout.PREVIEW_SCROLL_HEIGHT)) {
            presetScroll.scroll(amount < 0 ? 1 : amount > 0 ? -1 : 0);
            updateControls();
            return true;
        }
        return super.mouseScrolled(x, y, amount);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == GLFW.GLFW_KEY_TAB) keyboardNavigation = true;
        if (dialog != Dialog.NONE) {
            if (key == GLFW.GLFW_KEY_ESCAPE) closeDialog();
            else if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) confirmDialog();
            else if (dialog == Dialog.RENAME) presetName.keyPressed(key, scanCode, modifiers);
            return true;
        }
        if (search.visible && search.isFocused() && key != GLFW.GLFW_KEY_ESCAPE && key != GLFW.GLFW_KEY_TAB) {
            return search.keyPressed(key, scanCode, modifiers) || search.canConsumeInput();
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char character, int modifiers) {
        if (dialog != Dialog.NONE) return dialog == Dialog.RENAME && presetName.charTyped(character, modifiers);
        return super.charTyped(character, modifiers);
    }

    private String fit(String value, int width) {
        return font.width(value) <= width ? value : font.plainSubstrByWidth(value, width - font.width("...")) + "...";
    }

    private void renderTabIcon(PoseStack pose, View tab, int x, int y) {
        if (tab == View.STORAGE) {
            itemRenderer.renderAndDecorateItem(new ItemStack(Items.CHEST), x - 1, y - 1);
        } else if (tab == View.COLLECTION) {
            for (int row = 0; row < 3; row++) {
                for (int column = 0; column < 3; column++) {
                    fill(pose, x + column * 5, y + row * 5, x + column * 5 + 3, y + row * 5 + 3, 0xFF40483A);
                }
            }

        }
    }

    private static void drawCheckboxTick(PoseStack pose, int x, int y, int color) {
        for (int index = 0; index < 3; index++) fill(pose, x + 1 + index, y + 3 + index, x + 2 + index, y + 4 + index, color);
        for (int index = 0; index < 4; index++) fill(pose, x + 3 + index, y + 5 - index, x + 4 + index, y + 6 - index, color);
    }

    private static void check(PoseStack pose, int x, int y) {
        fill(pose, x, y, x + 8, y + 7, 0xFF263526);
        for (int index = 0; index < 3; index++) fill(pose, x + 1 + index, y + 3 + index, x + 2 + index, y + 4 + index, 0xFFB9FC80);
        for (int index = 0; index < 4; index++) fill(pose, x + 3 + index, y + 5 - index, x + 4 + index, y + 6 - index, 0xFFB9FC80);
    }

    private static void frame(PoseStack pose, int x, int y, int width, int height) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        ScreenTextures.DEFAULT_WINDOW_BACKGROUND.blit(pose, x, y, 0, width, height);
    }

    private static void recessed(PoseStack pose, int x, int y, int width, int height) {
        fill(pose, x, y, x + width, y + height, 0xFFEEEEEE);
        fill(pose, x, y, x + width - 1, y + height - 1, 0xFF777777);
        fill(pose, x + 1, y + 1, x + width - 1, y + height - 1, 0xFFA4A4A4);
    }

    private static void collectionSlot(PoseStack pose, int x, int y, boolean active) {
        fill(pose, x, y, x + 20, y + 20, 0xFFE6E6E6);
        fill(pose, x, y, x + 19, y + 19, 0xFF555555);
        fill(pose, x + 1, y + 1, x + 19, y + 19, active ? 0xFF738268 : 0xFFB5B5B5);
    }

    private static void smallButton(PoseStack pose, int x, int y, int width, int height, boolean hovered) {
        fill(pose, x, y, x + width, y + height, 0xFF444444);
        fill(pose, x + 1, y + 1, x + width - 1, y + height - 1, 0xFFE6E6E6);
        fill(pose, x + 2, y + 2, x + width - 2, y + height - 2, hovered ? 0xFF8A957B : 0xFF777777);
    }

    private static void outline(PoseStack pose, int x, int y, int width, int height, int color) {
        fill(pose, x, y, x + width, y + 1, color);
        fill(pose, x, y + height - 1, x + width, y + height, color);
        fill(pose, x, y, x + 1, y + height, color);
        fill(pose, x + width - 1, y, x + width, y + height, color);
    }

    private static void inset(PoseStack pose, int x, int y) {
        fill(pose, x, y, x + 18, y + 18, 0xFFFFFFFF);
        fill(pose, x, y, x + 17, y + 17, 0xFF373737);
        fill(pose, x + 1, y + 1, x + 17, y + 17, 0xFF8B8B8B);
    }

    private enum View {
        COLLECTION("trinkets"), STORAGE("storage");
        private final String label;
        View(String label) { this.label = label; }
    }
    private enum Dialog { NONE, RENAME, OVERWRITE }
    private record Entry(ItemStack icon, List<Integer> indices) {}
}
