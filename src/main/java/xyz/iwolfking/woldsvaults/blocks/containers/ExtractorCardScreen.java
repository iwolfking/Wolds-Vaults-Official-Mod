package xyz.iwolfking.woldsvaults.blocks.containers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.render.TextureRegion;
import iskallia.vault.client.render.TextureRegionRenderer;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.extraction.jobs.GreedCardUpgrades;

import java.util.ArrayList;

/** The Greed Infuser's insert screen, framed like the Extractor Assembler: card slots, a status line and Start. */
public class ExtractorCardScreen extends AbstractContainerScreen<ExtractorCardMenu> {
    private static final TextureRegion BORDER_CORNER_TOP_LEFT = new TextureRegion(0, 0, 15, 24);
    private static final TextureRegion BORDER_CORNER_TOP_RIGHT = new TextureRegion(18, 0, 15, 24);
    private static final TextureRegion BORDER_CORNER_BOTTOM_LEFT = new TextureRegion(0, 27, 15, 16);
    private static final TextureRegion BORDER_CORNER_BOTTOM_RIGHT = new TextureRegion(18, 27, 15, 16);
    private static final TextureRegion BORDER_TOP = new TextureRegion(16, 0, 1, 24);
    private static final TextureRegion BORDER_BOTTOM = new TextureRegion(16, 27, 1, 16);
    private static final TextureRegion BORDER_LEFT = new TextureRegion(0, 25, 15, 1);
    private static final TextureRegion BORDER_RIGHT = new TextureRegion(18, 25, 15, 1);

    private static final int BACKGROUND = 0xFF0B0D12;
    private static final int GRID = 0xFF11151C;
    private static final int FRAME = 0xFF3A4654;
    private static final int SLOT_FILL = 0xFF0E1218;
    private static final int SLOT_RING = 0xFF3A4654;
    private static final int SLOT_RING_HOVER = 0xFF8A8A8A;
    private static final int TITLE = 0xFFFFAA00;
    private static final int TEXT = 0xFFD8CBA0;
    private static final int TEXT_DIM = 0xFF8A8A8A;
    private static final int BAD = 0xFFFF5A5A;
    private static final int GREED = 0xFFDAA520;
    private static final int BUTTON_FILL = 0xFF3D2F0C;
    private static final int BUTTON_FILL_HOVER = 0xFF5A4512;
    private static final int BUTTON_DEAD = 0xFF1C1F26;
    private static final int BUTTON_HEIGHT = 18;

    public ExtractorCardScreen(ExtractorCardMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = menu.getPlayerTop() + 82;
    }

    private int cardBottom() {
        return ExtractorCardMenu.CARD_TOP + this.menu.getCardRows() * 18;
    }

    private int buttonX() {
        return this.leftPos + ExtractorCardMenu.LEFT;
    }

    private int buttonY() {
        return this.topPos + this.cardBottom() + 18;
    }

    private int buttonWidth() {
        return this.imageWidth - ExtractorCardMenu.LEFT * 2;
    }

    private boolean overButton(double mouseX, double mouseY) {
        return mouseX >= this.buttonX() && mouseX < this.buttonX() + this.buttonWidth() && mouseY >= this.buttonY() && mouseY < this.buttonY() + BUTTON_HEIGHT;
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        this.dropForeignWidgets();
        this.renderBackground(pose);
        super.render(pose, mouseX, mouseY, partialTicks);
        this.renderTooltip(pose, mouseX, mouseY);
    }

    /** Other mods bolt sidebar buttons onto every container screen; this one registers none of its own, so all of them go. */
    private void dropForeignWidgets() {
        for (Widget widget : new ArrayList<>(this.renderables)) {
            if (widget instanceof GuiEventListener listener) {
                this.removeWidget(listener);
            }
        }
        for (GuiEventListener listener : new ArrayList<>(this.children())) {
            this.removeWidget(listener);
        }
    }

    @Override
    public void renderBackground(PoseStack pose) {
        super.renderBackground(pose);
        int x0 = this.leftPos;
        int y0 = this.topPos;
        fill(pose, x0, y0, x0 + this.imageWidth, y0 + this.imageHeight, BACKGROUND);
        for (int x = x0; x < x0 + this.imageWidth; x += 16) {
            fill(pose, x, y0, x + 1, y0 + this.imageHeight, GRID);
        }
        for (int y = y0; y < y0 + this.imageHeight; y += 16) {
            fill(pose, x0, y, x0 + this.imageWidth, y + 1, GRID);
        }
        RenderSystem.enableBlend();
        TextureRegionRenderer.getInstance().begin(ScreenTextures.UI_RESOURCE).with(pose)
                .draw(x0 - 9, y0 - 18, BORDER_CORNER_TOP_LEFT)
                .draw(x0 + this.imageWidth - 7, y0 - 18, BORDER_CORNER_TOP_RIGHT)
                .draw(x0 - 9, y0 + this.imageHeight - 7, BORDER_CORNER_BOTTOM_LEFT)
                .draw(x0 + this.imageWidth - 7, y0 + this.imageHeight - 7, BORDER_CORNER_BOTTOM_RIGHT)
                .push().translateXY(x0 + 6, y0 - 18).scaleX(this.imageWidth - 13).draw(BORDER_TOP)
                .translateY(this.imageHeight + 11).draw(BORDER_BOTTOM).pop()
                .push().translateXY(x0 - 9, y0 + 6).scaleY(this.imageHeight - 13).draw(BORDER_LEFT)
                .translateX(this.imageWidth + 2).draw(BORDER_RIGHT).pop()
                .end();
    }

    @Override
    protected void renderBg(PoseStack pose, float partialTicks, int mouseX, int mouseY) {
        for (Slot slot : this.menu.slots) {
            int x = this.leftPos + slot.x - 1;
            int y = this.topPos + slot.y - 1;
            boolean hovered = this.hoveredSlot == slot;
            fill(pose, x, y, x + 18, y + 18, hovered ? SLOT_RING_HOVER : SLOT_RING);
            fill(pose, x + 1, y + 1, x + 17, y + 17, SLOT_FILL);
        }
        int dividerY = this.topPos + this.menu.getPlayerTop() - 14;
        fill(pose, this.leftPos + 6, dividerY, this.leftPos + this.imageWidth - 6, dividerY + 1, FRAME);

        boolean ready = this.menu.filledSlots() > 0;
        boolean hover = ready && this.overButton(mouseX, mouseY);
        int bx = this.buttonX();
        int by = this.buttonY();
        fill(pose, bx, by, bx + this.buttonWidth(), by + BUTTON_HEIGHT, ready ? GREED : FRAME);
        fill(pose, bx + 1, by + 1, bx + this.buttonWidth() - 1, by + BUTTON_HEIGHT - 1, !ready ? BUTTON_DEAD : hover ? BUTTON_FILL_HOVER : BUTTON_FILL);
        String label = "Start Extractor";
        this.font.draw(pose, label, bx + (this.buttonWidth() - this.font.width(label)) / 2.0F, by + 5, ready ? TITLE : TEXT_DIM);
    }

    @Override
    protected void renderLabels(PoseStack pose, int mouseX, int mouseY) {
        String title = "Greed Infuser";
        this.font.draw(pose, title, (this.imageWidth - this.font.width(title)) / 2.0F, 6, TITLE);
        String target = GreedCardUpgrades.displayName(this.menu.getTargetClass());
        String subtitle = "Upgrades cards to " + target + " Greed, Tier 1";
        this.drawFit(pose, subtitle, 18, GREED);
        ItemStack carried = this.menu.getCarried();
        String refusal = carried.isEmpty() ? null : GreedCardUpgrades.refusal(carried, this.menu.getTargetClass());
        if (refusal != null) {
            this.drawFit(pose, refusal, this.cardBottom() + 4, BAD);
        } else {
            String status = String.format("%d / %d card slots filled", this.menu.filledSlots(), this.menu.getCardSlots());
            this.drawFit(pose, status, this.cardBottom() + 4, TEXT);
        }
        this.font.draw(pose, "Inventory", ExtractorCardMenu.LEFT, this.menu.getPlayerTop() - 11, TEXT_DIM);
    }

    private void drawFit(PoseStack pose, String text, int y, int color) {
        int room = this.imageWidth - ExtractorCardMenu.LEFT * 2;
        int width = this.font.width(text);
        float scale = width > room ? room / (float) width : 1.0F;
        pose.pushPose();
        pose.translate((this.imageWidth - width * scale) / 2.0F, y, 0.0D);
        pose.scale(scale, scale, 1.0F);
        this.font.draw(pose, text, 0.0F, 0.0F, color);
        pose.popPose();
    }

    @Override
    protected void renderTooltip(PoseStack pose, int mouseX, int mouseY) {
        super.renderTooltip(pose, mouseX, mouseY);
        if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && !this.hoveredSlot.hasItem()
                && this.hoveredSlot.index < this.menu.getCardSlots()) {
            this.renderTooltip(pose, new TextComponent("A greed card weaker than " + GreedCardUpgrades.displayName(this.menu.getTargetClass())), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.menu.filledSlots() > 0 && this.overButton(mouseX, mouseY) && this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, ExtractorCardMenu.BUTTON_START);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
