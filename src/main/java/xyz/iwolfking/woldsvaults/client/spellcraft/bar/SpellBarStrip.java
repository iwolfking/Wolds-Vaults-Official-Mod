package xyz.iwolfking.woldsvaults.client.spellcraft.bar;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.framework.ScreenTextures;
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
import xyz.iwolfking.woldsvaults.network.packets.SpellPlaceMessage;
import xyz.iwolfking.woldsvaults.spellcraft.ability.AbilityLookup;
import xyz.iwolfking.woldsvaults.spellcraft.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.spellcraft.play.SpellCooldowns;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

/**
 * The spell bar window of the skill menu and the spell editor, with a picker between the default and the effective bar.
 * A left click on a slot assigns the host's selected ability to the default bar or opens the chooser, and a right click
 * clears the slot or unassigns its spell. The Set Binds button toggles bind mode, where a left click arms a slot so the
 * next key or extra mouse button pressed becomes its bind, and a right click clears the slot's bind.
 */
public final class SpellBarStrip {
    public static final int HEIGHT = 24;
    public static final int BAND = 27;
    static final int PICK = 18;
    static final int PICK_Y = 3;
    private static final int PICK_ICON = 1;
    private static final int SLOT_PITCH = 18;
    static final int KEYS_H = 16;
    private static final int KEYS_PAD = 8;
    private static final int KEYS_GAP = 4;
    public static final float KEY_SCALE = 0.75F;
    private static final int HOVER = 0x80FFFFFF;
    private static final int BROKEN = 0x80FF3030;
    private static final int COOLING = 0x90000000;
    private static final int LOCK = 0xFFFF5050;
    static final int IMPLICIT_RING = 0xFFE0B040;
    private static final int ARMED = 0xFFFFE040;
    private static final int BADGE = 8;

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

    public SpellBarView refreshView() {
        SpellBarView item = this.showEffective ? this.host.effectiveView() : null;
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

    /** Whether the player last picked the effective bar, which is remembered while no effective bar is available. */
    boolean prefersEffective() {
        return this.showEffective;
    }

    static int slotX(int x0, int index) {
        return x0 + 4 + 2 * (PICK + 2) + 6 + index * SLOT_PITCH;
    }

    int pickX(int k) {
        return this.x0 + 4 + k * (PICK + 2);
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
        return this.x1 - 4 - (slotX(this.x0, SpellcraftItemData.SLOTS - 1) + 16 + KEYS_GAP);
    }

    int keysX() {
        return this.x1 - 4 - keysW();
    }

    boolean bindMode() {
        return this.bindMode;
    }

    int armedSlot() {
        return this.armedSlot;
    }

    public void render(PoseStack pose, int mouseX, int mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        SpellBarView view = refreshView();
        SpellBarView item = this.host.effectiveView();
        RenderSystem.enableBlend();
        SpellcraftDraw.window(pose, this.x0, this.y0, this.x1, this.y0 + HEIGHT);
        drawPicker(pose, 0, !view.isEffective(), null);
        if (item != null) {
            drawPicker(pose, 1, view.isEffective(), item.stack());
        }
        AbilityTree tree = this.host.tree();
        Function<String, String> specOf = AbilityLookup.specOf(tree);
        int room = (int) ((16 - 1) / KEY_SCALE);
        for (int i = 0; i < SpellcraftItemData.SLOTS; i++) {
            int x = slotX(this.x0, i);
            int y = this.y0 + 4;
            SpellBarView.Slot slot = view.slot(i);
            drawSlot(pose, slot, specOf, x, y);
            drawBadge(slot, x, y);
            if (i == this.armedSlot) {
                SpellcraftDraw.ring(pose, x - 2, y - 2, x + 18, y + 18, ARMED);
                SpellcraftDraw.ring(pose, x - 1, y - 1, x + 17, y + 17, ARMED);
            }
            if (SpellcraftDraw.inside(mouseX, mouseY, x, y, 16, 16) && !this.chooser.popupOpen()) {
                GuiComponent.fill(pose, x, y, x + 16, y + 16, HOVER);
            }
            String key = SpellcraftNames.keyLabel(i, font, room);
            if (!key.isEmpty()) {
                pose.pushPose();
                pose.translate(0.0, 0.0, 200.0);
                SpellcraftDraw.smallText(pose, font, key, x + 1, y + 1, slot.filled() || slot.state() == SpellBarView.State.HIDDEN
                        ? SpellcraftPalette.WHITE : SpellcraftPalette.MUTED, KEY_SCALE);
                pose.popPose();
            }
        }
        SpellcraftDraw.button(pose, font, keysX(), this.y0 + 4, keysW(), KEYS_H, SpellcraftNames.tr("bar.keys"),
                SpellcraftDraw.inside(mouseX, mouseY, keysX(), this.y0 + 4, keysW(), KEYS_H), keysScale(), this.bindMode);
    }

    private void drawPicker(PoseStack pose, int k, boolean selected, @Nullable ItemStack stack) {
        int px = pickX(k);
        int py = this.y0 + PICK_Y;
        if (selected) {
            SpellcraftDraw.insetGrey(pose, px, py, px + PICK, py + PICK);
        } else {
            SpellcraftDraw.insetDarkGrey(pose, px, py, px + PICK, py + PICK);
        }
        if (stack == null) {
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, selected ? 1.0F : 0.55F);
            ScreenTextures.TAB_ICON_ABILITIES.blit(pose, px + PICK_ICON, py + PICK_ICON, 0, 16, 16);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        } else {
            Minecraft.getInstance().getItemRenderer().renderGuiItem(stack, px + PICK_ICON, py + PICK_ICON);
        }
        if (selected) {
            SpellcraftDraw.ring(pose, px, py, px + PICK, py + PICK, stack == null ? SpellcraftPalette.WHITE : SpellcraftPalette.ARCANE_GLOW);
        }
    }

    private void drawSlot(PoseStack pose, SpellBarView.Slot slot, Function<String, String> specOf, int x, int y) {
        SpellcraftDraw.slot(pose, x, y, false);
        String spec = slot.group().isEmpty() ? null : specOf.apply(slot.group());
        String icon = spec != null ? spec : slot.group();
        switch (slot.state()) {
            case ABILITY -> {
                SpellcraftDraw.ability(pose, icon, x, y, 16, slot.unlearned() ? 0.4F : 1.0F);
                if (slot.unlearned()) {
                    SpellcraftDraw.redX(pose, x, y);
                }
            }
            case SPELL -> {
                SpellcraftDraw.ability(pose, icon, x, y, 16, slot.locked() ? 0.45F : 1.0F);
                if (slot.locked()) {
                    GuiComponent.fill(pose, x, y, x + 16, y + 16, BROKEN);
                    GuiComponent.fill(pose, x + 5, y + 7, x + 11, y + 13, LOCK);
                    GuiComponent.fill(pose, x + 6, y + 4, x + 7, y + 7, LOCK);
                    GuiComponent.fill(pose, x + 9, y + 4, x + 10, y + 7, LOCK);
                    GuiComponent.fill(pose, x + 6, y + 4, x + 10, y + 5, LOCK);
                }
                SpellcraftDraw.ring(pose, x - 1, y - 1, x + 17, y + 17, slot.implicit() ? IMPLICIT_RING : SpellcraftPalette.ARCANE_GLOW);
                if (!slot.implicit()) {
                    SpellcraftDraw.spellStar(pose, x, y);
                }
                if (slot.unlearned()) {
                    SpellcraftDraw.redX(pose, x, y);
                }
            }
            case HIDDEN -> {
                SpellcraftDraw.ability(pose, icon, x, y, 16, 0.45F);
                SpellcraftDraw.strike(pose, x, y);
            }
            case EMPTY -> {
            }
        }
        if (slot.filled() && SpellCooldowns.blocking(this.host.tree(), slot.groups()) != null) {
            GuiComponent.fill(pose, x, y, x + 16, y + 16, COOLING);
        }
    }

    private static void drawBadge(SpellBarView.Slot slot, int x, int y) {
        if (slot.state() != SpellBarView.State.SPELL || slot.itemKey().isEmpty()) {
            return;
        }
        ItemStack stack = ClientSpellBar.stackOf(slot.itemKey());
        if (stack.isEmpty()) {
            return;
        }
        PoseStack modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        modelView.translate(x + 16 - BADGE, y + 16 - BADGE, 250.0);
        modelView.scale(0.5F, 0.5F, 1.0F);
        RenderSystem.applyModelViewMatrix();
        Minecraft.getInstance().getItemRenderer().renderGuiItem(stack, 0, 0);
        modelView.popPose();
        RenderSystem.applyModelViewMatrix();
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
        if (this.armedSlot >= 0 && button > 1) {
            if (SlotKeyBinder.bindMouse(this.armedSlot, button)) {
                this.armedSlot = -1;
            }
            return true;
        }
        if (!SpellcraftDraw.inside(mouseX, mouseY, this.x0, this.y0, this.x1 - this.x0, HEIGHT)) {
            this.armedSlot = -1;
            return false;
        }
        if (SpellcraftDraw.inside(mouseX, mouseY, pickX(0), this.y0 + PICK_Y, PICK, PICK)) {
            this.showEffective = false;
            SpellcraftDraw.click();
            return true;
        }
        if (this.host.effectiveView() != null && SpellcraftDraw.inside(mouseX, mouseY, pickX(1), this.y0 + PICK_Y, PICK, PICK)) {
            this.showEffective = true;
            SpellcraftDraw.click();
            return true;
        }
        if (SpellcraftDraw.inside(mouseX, mouseY, keysX(), this.y0 + 4, keysW(), KEYS_H)) {
            SpellcraftDraw.click();
            this.bindMode = !this.bindMode;
            this.armedSlot = -1;
            return true;
        }
        for (int i = 0; i < SpellcraftItemData.SLOTS; i++) {
            if (SpellcraftDraw.inside(mouseX, mouseY, slotX(this.x0, i), this.y0 + 4, 16, 16)) {
                if (this.bindMode) {
                    clickBindSlot(i, button);
                } else {
                    clickSlot(i, button);
                }
                return true;
            }
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
        if (this.armedSlot < 0) {
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

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return this.chooser.mouseScrolled(delta);
    }

    public boolean popupOpen() {
        return this.chooser.popupOpen();
    }
}
