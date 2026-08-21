package xyz.iwolfking.woldsvaults.client.screens.gods;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.iwolfking.woldsvaults.gods.network.ClientboundSacrificeMenuMessage;
import xyz.iwolfking.woldsvaults.gods.network.GodNetwork;
import xyz.iwolfking.woldsvaults.gods.network.ServerboundRequestSacrificeMenuMessage;
import xyz.iwolfking.woldsvaults.gods.network.ServerboundSelectSacrificeGodMessage;

import javax.annotation.Nullable;
import java.awt.Rectangle;

/**
 * The Greed Cauldron's sacrificial-altar menu: pick the god to sacrifice toward, see the current
 * gate's item list with live progress, and read off whether the offering and god experience are
 * ready for the redstone-triggered sacrifice. Deposits happen at the cauldron itself (pipes,
 * hoppers, tossed items); this screen is a live readout, polling the server once a second.
 */
public class SacrificeAltarScreen extends Screen {
    private static final int PANEL_WIDTH = 276;
    private static final int PANEL_HEIGHT = 212;
    private static final int PLATE_WIDTH = 62;
    private static final int PLATE_HEIGHT = 30;

    private ClientboundSacrificeMenuMessage data;
    private int pollTicks;

    private SacrificeAltarScreen(ClientboundSacrificeMenuMessage data) {
        super(new TextComponent("Sacrificial Altar"));
        this.data = data;
    }

    public static void openOrRefresh(ClientboundSacrificeMenuMessage message) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof SacrificeAltarScreen open) {
            open.data = message;
            return;
        }
        minecraft.setScreen(new SacrificeAltarScreen(message));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        if (++this.pollTicks >= 20) {
            this.pollTicks = 0;
            GodNetwork.INSTANCE.sendToServer(new ServerboundRequestSacrificeMenuMessage());
        }
    }

    private int panelX() {
        return (this.width - PANEL_WIDTH) / 2;
    }

    private int panelY() {
        return (this.height - PANEL_HEIGHT) / 2;
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        int x = panelX();
        int y = panelY();
        fill(matrixStack, x - 2, y - 2, x + PANEL_WIDTH + 2, y + PANEL_HEIGHT + 2, 0xFF3B3347);
        fill(matrixStack, x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, 0xF0121016);
        drawCenteredString(matrixStack, this.font, this.title.getString(), x + PANEL_WIDTH / 2, y + 6, 0xFFE8DFF5);
        renderGodPlates(matrixStack, mouseX, mouseY);
        renderSelectedGod(matrixStack, mouseX, mouseY);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    private Rectangle plateBounds(int index) {
        int totalWidth = 4 * PLATE_WIDTH + 3 * 5;
        int startX = panelX() + (PANEL_WIDTH - totalWidth) / 2;
        return new Rectangle(startX + index * (PLATE_WIDTH + 5), panelY() + 18, PLATE_WIDTH, PLATE_HEIGHT);
    }

    private void renderGodPlates(PoseStack matrixStack, int mouseX, int mouseY) {
        VaultGod[] gods = VaultGod.values();
        for (int i = 0; i < gods.length; i++) {
            VaultGod god = gods[i];
            Rectangle plate = plateBounds(i);
            boolean selected = this.data.selectedGod == god;
            boolean hovered = plate.contains(mouseX, mouseY);
            int accent = GodTreeTheme.pointsColor(god);
            fill(matrixStack, plate.x, plate.y, plate.x + plate.width, plate.y + plate.height,
                    selected ? (accent & 0xFFFFFF) | 0x50000000 : (hovered ? 0x40FFFFFF : 0x28FFFFFF));
            if (selected) {
                fill(matrixStack, plate.x, plate.y + plate.height - 2, plate.x + plate.width,
                        plate.y + plate.height, (accent & 0xFFFFFF) | 0xFF000000);
            }
            GodTreeTheme.godIcon(god).blit(matrixStack, plate.x + (plate.width - 16) / 2, plate.y + 2, 0, 16, 16);
            matrixStack.pushPose();
            matrixStack.scale(0.75F, 0.75F, 1.0F);
            String name = god.getName();
            float textX = (plate.x + plate.width / 2.0F) / 0.75F - this.font.width(name) / 2.0F;
            this.font.drawShadow(matrixStack, name, textX, (plate.y + 20) / 0.75F,
                    selected ? (accent & 0xFFFFFF) | 0xFF000000 : 0xFFBBB3C8);
            matrixStack.popPose();
        }
    }

    private void renderSelectedGod(PoseStack matrixStack, int mouseX, int mouseY) {
        int x = panelX();
        int y = panelY();
        VaultGod god = this.data.selectedGod;
        if (god == null) {
            drawCenteredString(matrixStack, this.font, "Select the god to sacrifice toward.",
                    x + PANEL_WIDTH / 2, y + 90, 0xFF9A93A8);
            return;
        }
        ClientboundSacrificeMenuMessage.GodSnapshot snapshot = this.data.gods.get(god);
        if (snapshot == null) {
            return;
        }
        int accent = GodTreeTheme.pointsColor(god);
        if (snapshot.gateLabel == null) {
            drawCenteredString(matrixStack, this.font, god.getName() + " demands no further sacrifices.",
                    x + PANEL_WIDTH / 2, y + 90, (accent & 0xFFFFFF) | 0xFF000000);
            return;
        }
        drawCenteredString(matrixStack, this.font, snapshot.gateLabel + "  —  " + god.getName(),
                x + PANEL_WIDTH / 2, y + 54, (accent & 0xFFFFFF) | 0xFF000000);
        int listTop = y + 66;
        int rows = (snapshot.entries.size() + 1) / 2;
        ItemStack hoveredStack = ItemStack.EMPTY;
        for (int i = 0; i < snapshot.entries.size(); i++) {
            ClientboundSacrificeMenuMessage.EntrySnapshot entry = snapshot.entries.get(i);
            int column = i / rows;
            int row = i % rows;
            int entryX = x + 16 + column * 126;
            int entryY = listTop + row * 15;
            Item item = ForgeRegistries.ITEMS.getValue(entry.item());
            ItemStack stack = item == null ? ItemStack.EMPTY : new ItemStack(item);
            this.itemRenderer.renderGuiItem(stack, entryX, entryY - 4);
            boolean complete = entry.have() >= entry.required();
            String text = String.format("%,d / %,d", entry.have(), entry.required());
            this.font.drawShadow(matrixStack, text, entryX + 20, entryY,
                    complete ? 0xFF7BE87B : 0xFFE0DCE8);
            if (new Rectangle(entryX, entryY - 4, 120, 15).contains(mouseX, mouseY) && !stack.isEmpty()) {
                hoveredStack = stack;
            }
        }
        int footerY = y + PANEL_HEIGHT - 34;
        boolean itemsComplete = snapshot.entries.stream().allMatch(entry -> entry.have() >= entry.required());
        String offering = itemsComplete ? "Offering complete." : "Offering incomplete.";
        String xpLine = snapshot.xpReady ? "God experience full." : "God experience not yet full.";
        this.font.drawShadow(matrixStack, offering + "  " + xpLine, x + 16, footerY,
                itemsComplete && snapshot.xpReady ? 0xFF7BE87B : 0xFF9A93A8);
        matrixStack.pushPose();
        matrixStack.scale(0.75F, 0.75F, 1.0F);
        this.font.drawShadow(matrixStack, "Feed the cauldron by pipe, hopper or tossed item - offerings cannot be taken back.",
                (x + 16) / 0.75F, (footerY + 12) / 0.75F, 0xFF7A7488);
        this.font.drawShadow(matrixStack, "Apply a redstone signal to the cauldron to perform the sacrifice.",
                (x + 16) / 0.75F, (footerY + 21) / 0.75F, 0xFF7A7488);
        matrixStack.popPose();
        if (!hoveredStack.isEmpty()) {
            this.renderTooltip(matrixStack, hoveredStack, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        VaultGod[] gods = VaultGod.values();
        for (int i = 0; i < gods.length; i++) {
            if (plateBounds(i).contains(mouseX, mouseY)) {
                if (this.data.selectedGod != gods[i]) {
                    GodNetwork.INSTANCE.sendToServer(new ServerboundSelectSacrificeGodMessage(gods[i]));
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Nullable
    public VaultGod selectedGod() {
        return this.data.selectedGod;
    }
}
