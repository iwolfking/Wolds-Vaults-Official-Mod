package xyz.iwolfking.woldsvaults.client.spellcraft.bar;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.skill.tree.AbilityTree;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftDraw;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftNames;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftPalette;
import xyz.iwolfking.woldsvaults.client.spellcraft.keys.SlotKeyBinder;
import xyz.iwolfking.woldsvaults.client.spellcraft.state.ClientSpellBar;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.network.packets.SpellBarModeMessage;
import xyz.iwolfking.woldsvaults.network.packets.SpellPlaceMessage;
import xyz.iwolfking.woldsvaults.spellcraft.ability.AbilityLookup;
import xyz.iwolfking.woldsvaults.spellcraft.bar.BarMode;
import xyz.iwolfking.woldsvaults.spellcraft.item.SpellcraftItemData;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

/**
 * The spell bar window of the skill menu and the spell editor. Its top row holds the mode button and the visible slot
 * range; scrolling over the window pages through the slots, {@link #VISIBLE} at a time. In classic mode the slot row
 * shows the resolved bar read-only with each slot's quick-fire key. In slot mode it has a picker between the default
 * and the effective bar: a left click on a slot assigns the host's selected ability to the default bar or opens the
 * chooser, and a right click clears the slot or unassigns its spell. The Set Binds button toggles bind mode, where a
 * left click arms a slot so the next key or extra mouse button pressed becomes its bind, and a right click clears the
 * slot's bind.
 */
public final class SpellBarStrip {
    public static final int HEIGHT = 35;
    public static final int BAND = 38;
    static final int VISIBLE = 16;
    static final int PICK = 18;
    static final int MODE_H = 10;
    static final int KEYS_H = 16;
    private static final int HEAD_Y = 3;
    private static final int ROW_Y = 15;
    private static final int SLOT_PITCH = 18;
    private static final int MODE_PAD = 8;
    private static final int KEYS_PAD = 8;
    private static final int KEYS_GAP = 4;
    private static final int TEXT_GAP = 6;
    public static final float KEY_SCALE = 0.75F;
    private static final int HOVER = 0x80FFFFFF;
    private static final int ARMED = 0xFFFFE040;

    public interface Host {
        AbilityTree tree();

        SpellBarView defaultView();

        /** The effective bar, or null when there is none to show. */
        @Nullable
        SpellBarView effectiveView();

        /** The ability group a slot click assigns, or null to open the ability chooser instead. */
        @Nullable
        String selectedGroup();
    }

    private final Host host;
    private final SlotChooser chooser = new SlotChooser(this);
    private int x0;
    private int y0;
    private int x1;
    private boolean showEffective;
    private SpellBarView view;
    private boolean bindMode;
    private int armedSlot = -1;
    private int page;

    public SpellBarStrip(Host host, boolean showEffective) {
        this.host = host;
        this.showEffective = showEffective;
    }

    /** (x0, y0) is the window's top left and x1 its right edge. */
    public void layout(int x0, int y0, int x1) {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
    }

    /** The effective bar in classic mode, else the bar the picker selects; the default bar while there is no other. */
    public SpellBarView refreshView() {
        SpellBarView item = classic() || this.showEffective ? this.host.effectiveView() : null;
        this.view = item != null ? item : this.host.defaultView();
        return this.view;
    }

    SpellBarView lastView() {
        return this.view;
    }

    Host host() {
        return this.host;
    }

    int x0() {
        return this.x0;
    }

    int y0() {
        return this.y0;
    }

    /** The top of the slot row. */
    int rowY() {
        return this.y0 + ROW_Y;
    }

    /** Whether the player last picked the effective bar, which is remembered while no effective bar is available. */
    boolean prefersEffective() {
        return this.showEffective;
    }

    static boolean classic() {
        return ClientSpellBar.mode() == BarMode.CLASSIC;
    }

    /** The first slot index on the visible page. */
    int first() {
        return this.page * VISIBLE;
    }

    private static int pages() {
        return (SpellcraftItemData.SLOTS + VISIBLE - 1) / VISIBLE;
    }

    /** The left edge of a slot on the visible page. */
    int slotX(int index) {
        return this.x0 + 4 + 2 * (PICK + 2) + 6 + (index - first()) * SLOT_PITCH;
    }

    /** The visible slot under the mouse, or -1. */
    int slotAt(double mouseX, double mouseY) {
        for (int i = first(); i < Math.min(SpellcraftItemData.SLOTS, first() + VISIBLE); i++) {
            if (SpellcraftDraw.inside(mouseX, mouseY, slotX(i), rowY(), 16, 16)) {
                return i;
            }
        }
        return -1;
    }

    int pickX(int k) {
        return this.x0 + 4 + k * (PICK + 2);
    }

    int pickY() {
        return rowY() - 1;
    }

    int modeX() {
        return this.x0 + 4;
    }

    int modeY() {
        return this.y0 + HEAD_Y;
    }

    /** The mode button's width, fitting the longer of its two labels so it keeps its size when the mode changes. */
    int modeW() {
        Font font = Minecraft.getInstance().font;
        int text = Math.max(font.width(SpellcraftNames.tr("bar.mode_classic")), font.width(SpellcraftNames.tr("bar.mode_slot")));
        return (int) Math.ceil(text * KEY_SCALE) + MODE_PAD;
    }

    /** The Set Binds label scale: full size when the button fits beside the last slot, otherwise the key label scale. */
    float keysScale() {
        Font font = Minecraft.getInstance().font;
        return font.width(SpellcraftNames.tr("bar.keys")) + KEYS_PAD <= keysRoom() ? 1.0F : KEY_SCALE;
    }

    int keysW() {
        Font font = Minecraft.getInstance().font;
        int full = font.width(SpellcraftNames.tr("bar.keys")) + KEYS_PAD;
        int room = keysRoom();
        return full <= room ? full : Math.min(room, (int) Math.ceil(font.width(SpellcraftNames.tr("bar.keys")) * KEY_SCALE) + 6);
    }

    private int keysRoom() {
        return this.x1 - 4 - (slotX(first() + VISIBLE - 1) + 16 + KEYS_GAP);
    }

    int keysX() {
        return this.x1 - 4 - keysW();
    }

    boolean bindMode() {
        return this.bindMode && !classic();
    }

    int armedSlot() {
        return this.armedSlot;
    }

    public void render(PoseStack pose, int mouseX, int mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        SpellBarView view = refreshView();
        boolean classic = classic();
        RenderSystem.enableBlend();
        SpellcraftDraw.window(pose, this.x0, this.y0, this.x1, this.y0 + HEIGHT);
        renderHead(pose, font, mouseX, mouseY);
        if (!classic) {
            SpellBarView item = this.host.effectiveView();
            StripArt.picker(pose, pickX(0), pickY(), PICK, !view.isEffective(), null);
            if (item != null) {
                StripArt.picker(pose, pickX(1), pickY(), PICK, view.isEffective(), item.stack());
            }
        }
        AbilityTree tree = this.host.tree();
        Function<String, String> specOf = AbilityLookup.specOf(tree);
        int room = (int) ((16 - 1) / KEY_SCALE);
        for (int i = first(); i < Math.min(SpellcraftItemData.SLOTS, first() + VISIBLE); i++) {
            int x = slotX(i);
            int y = rowY();
            SpellBarView.Slot slot = view.slot(i);
            StripArt.slot(pose, tree, slot, specOf, x, y);
            StripArt.badge(slot, x, y);
            if (i == this.armedSlot && bindMode()) {
                SpellcraftDraw.ring(pose, x - 2, y - 2, x + 18, y + 18, ARMED);
                SpellcraftDraw.ring(pose, x - 1, y - 1, x + 17, y + 17, ARMED);
            }
            if (SpellcraftDraw.inside(mouseX, mouseY, x, y, 16, 16) && !this.chooser.popupOpen()) {
                GuiComponent.fill(pose, x, y, x + 16, y + 16, HOVER);
            }
            String key = classic ? SpellcraftNames.quickfireLabel(slot.group(), font, room) : SpellcraftNames.keyLabel(i, font, room);
            if (!key.isEmpty()) {
                pose.pushPose();
                pose.translate(0.0, 0.0, 200.0);
                SpellcraftDraw.smallText(pose, font, key, x + 1, y + 1, slot.filled() || slot.state() == SpellBarView.State.HIDDEN
                        ? SpellcraftPalette.WHITE : SpellcraftPalette.MUTED, KEY_SCALE);
                pose.popPose();
            }
        }
        if (!classic) {
            SpellcraftDraw.button(pose, font, keysX(), rowY(), keysW(), KEYS_H, SpellcraftNames.tr("bar.keys"),
                    SpellcraftDraw.inside(mouseX, mouseY, keysX(), rowY(), keysW(), KEYS_H), keysScale(), this.bindMode);
        }
    }

    private void renderHead(PoseStack pose, Font font, int mouseX, int mouseY) {
        String label = SpellcraftNames.tr(classic() ? "bar.mode_classic" : "bar.mode_slot");
        SpellcraftDraw.button(pose, font, modeX(), modeY(), modeW(), MODE_H, label,
                SpellcraftDraw.inside(mouseX, mouseY, modeX(), modeY(), modeW(), MODE_H), KEY_SCALE, false);
        int last = Math.min(SpellcraftItemData.SLOTS, first() + VISIBLE);
        String range = SpellcraftNames.tr("bar.scroll", first() + 1, last, SpellcraftItemData.SLOTS);
        int x = modeX() + modeW() + TEXT_GAP;
        int room = (int) ((this.x1 - 4 - x) / KEY_SCALE);
        SpellcraftDraw.smallText(pose, font, font.plainSubstrByWidth(range, room), x, modeY() + 2, SpellcraftPalette.MUTED, KEY_SCALE);
    }

    public void renderOverlay(PoseStack pose, Screen screen, int mouseX, int mouseY) {
        if (this.view == null) {
            return;
        }
        pose.pushPose();
        pose.translate(0.0, 0.0, 400.0);
        if (this.chooser.popupOpen()) {
            this.chooser.render(pose, screen, mouseX, mouseY);
            pose.popPose();
            return;
        }
        pose.popPose();
        List<Component> tooltip = SlotTooltip.tooltip(this, mouseX, mouseY);
        if (!tooltip.isEmpty()) {
            screen.renderComponentTooltip(pose, tooltip, mouseX, mouseY);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.view == null) {
            return false;
        }
        if (this.chooser.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (this.armedSlot >= 0 && button > 1 && bindMode()) {
            if (SlotKeyBinder.bindMouse(this.armedSlot, button)) {
                this.armedSlot = -1;
            }
            return true;
        }
        if (!SpellcraftDraw.inside(mouseX, mouseY, this.x0, this.y0, this.x1 - this.x0, HEIGHT)) {
            this.armedSlot = -1;
            return false;
        }
        if (SpellcraftDraw.inside(mouseX, mouseY, modeX(), modeY(), modeW(), MODE_H)) {
            ModNetwork.sendToServer(new SpellBarModeMessage((classic() ? BarMode.SLOT : BarMode.CLASSIC).ordinal()));
            this.bindMode = false;
            this.armedSlot = -1;
            SpellcraftDraw.click();
            return true;
        }
        if (classic()) {
            return true;
        }
        if (SpellcraftDraw.inside(mouseX, mouseY, pickX(0), pickY(), PICK, PICK)) {
            this.showEffective = false;
            SpellcraftDraw.click();
            return true;
        }
        if (this.host.effectiveView() != null && SpellcraftDraw.inside(mouseX, mouseY, pickX(1), pickY(), PICK, PICK)) {
            this.showEffective = true;
            SpellcraftDraw.click();
            return true;
        }
        if (SpellcraftDraw.inside(mouseX, mouseY, keysX(), rowY(), keysW(), KEYS_H)) {
            SpellcraftDraw.click();
            this.bindMode = !this.bindMode;
            this.armedSlot = -1;
            return true;
        }
        int index = slotAt(mouseX, mouseY);
        if (index >= 0) {
            if (this.bindMode) {
                clickBindSlot(index, button);
            } else {
                clickSlot(index, button);
            }
            return true;
        }
        this.armedSlot = -1;
        return true;
    }

    private void clickBindSlot(int index, int button) {
        if (button == 0) {
            this.armedSlot = index;
            SpellcraftDraw.click();
        } else if (button == 1) {
            SlotKeyBinder.clear(index);
            this.armedSlot = -1;
            SpellcraftDraw.click();
        }
    }

    /** While a slot is armed in bind mode, the pressed key becomes its bind. Returns true when the key was used. */
    public boolean keyPressed(int keyCode, int scanCode) {
        if (this.armedSlot < 0 || !bindMode()) {
            return false;
        }
        if (SlotKeyBinder.bindKey(this.armedSlot, keyCode, scanCode)) {
            this.armedSlot = -1;
        }
        return true;
    }

    private void clickSlot(int index, int button) {
        AbilityTree tree = this.host.tree();
        if (button == 1) {
            SpellBarView.Slot slot = this.view.slot(index);
            if (slot.spell() != null) {
                int inventorySlot = inventorySlotOf(slot.itemKey());
                if (inventorySlot >= 0) {
                    ModNetwork.sendToServer(new SpellPlaceMessage(inventorySlot, slot.spellId(), SpellcraftItemData.UNASSIGNED, 0));
                    SpellcraftDraw.click();
                }
                return;
            }
            this.view.send(index, "");
            SpellcraftDraw.click();
            return;
        }
        if (button != 0) {
            return;
        }
        String selected = this.host.selectedGroup();
        if (selected != null) {
            if (SpellBarView.learned(tree, selected)) {
                this.view.send(index, selected);
                SpellcraftDraw.click();
            }
            return;
        }
        this.chooser.open(index, tree);
        SpellcraftDraw.click();
    }

    private static int inventorySlotOf(String itemKey) {
        Player player = Minecraft.getInstance().player;
        if (player == null || itemKey == null || itemKey.isEmpty()) {
            return -1;
        }
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (SpellcraftItemData.has(stack) && itemKey.equals(SpellcraftItemData.key(stack))) {
                return i;
            }
        }
        return -1;
    }

    /** Scrolls the open chooser, or pages through the slots while the mouse is over the window. */
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.chooser.mouseScrolled(delta)) {
            return true;
        }
        if (this.view == null || !SpellcraftDraw.inside(mouseX, mouseY, this.x0, this.y0, this.x1 - this.x0, HEIGHT)) {
            return false;
        }
        this.page = Math.max(0, Math.min(pages() - 1, this.page + (delta < 0 ? 1 : -1)));
        return true;
    }

    public boolean popupOpen() {
        return this.chooser.popupOpen();
    }
}
