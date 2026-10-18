package xyz.iwolfking.woldsvaults.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.util.StringUtils;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Inventory;
import xyz.iwolfking.woldsvaults.blocks.containers.SuppressorContainer;
import xyz.iwolfking.woldsvaults.blocks.tiles.SuppressorTileEntity;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.network.message.SuppressorUpdateMessage;

public class SuppressorScreen extends AbstractContainerScreen<SuppressorContainer> {

    public SuppressorScreen(SuppressorContainer menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 200;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        int startY = this.topPos + 20;
        int rowHeight = 24;

        SuppressorTileEntity.SuppressorType[] types = SuppressorTileEntity.SuppressorType.values();
        for (int i = 0; i < types.length; i++) {
            SuppressorTileEntity.SuppressorType type = types[i];
            int y = startY + (i * rowHeight);

            int btnX = this.leftPos + 96;

            this.addRenderableWidget(new Button(btnX, y, 32, 18,
                new TextComponent(this.menu.isEnabled(type) ? "ON" : "OFF"),
                btn -> {
                    boolean newState = !this.menu.isEnabled(type);
                    int currentRange = this.menu.getRange(type);
                    ModNetwork.CHANNEL.sendToServer(new SuppressorUpdateMessage(this.menu.getBlockPos(), type, newState, currentRange));
                    btn.setMessage(new TextComponent(newState ? "ON" : "OFF"));
                }
            ));

            this.addRenderableWidget(new Button(btnX + 34, y, 14, 18, new TextComponent("-"), btn -> {
                int currentRange = this.menu.getRange(type);
                if (currentRange > 1) {
                    boolean enabled = this.menu.isEnabled(type);
                    ModNetwork.CHANNEL.sendToServer(new SuppressorUpdateMessage(this.menu.getBlockPos(), type, enabled, currentRange - 1));
                }
            }));

            this.addRenderableWidget(new Button(btnX + 50, y, 14, 18, new TextComponent("+"), btn -> {
                int currentRange = this.menu.getRange(type);
                if (currentRange < 64) {
                    boolean enabled = this.menu.isEnabled(type);
                    ModNetwork.CHANNEL.sendToServer(new SuppressorUpdateMessage(this.menu.getBlockPos(), type, enabled, currentRange + 1));
                }
            }));
        }
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.renderBackground(poseStack);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        this.font.draw(poseStack, this.title, (float) this.titleLabelX, (float) this.titleLabelY, 4210752);

        int startX = 12;
        int startY = 25;
        int rowHeight = 24;

        SuppressorTileEntity.SuppressorType[] types = SuppressorTileEntity.SuppressorType.values();
        for (int i = 0; i < types.length; i++) {
            SuppressorTileEntity.SuppressorType type = types[i];
            int y = startY + (i * rowHeight);

            String label = StringUtils.convertToTitleCase(type.getSerializedName());
            int maxWidth = 96 - startX - 4;

            if (this.font.width(label) > maxWidth) {
                while (this.font.width(label + "...") > maxWidth && !label.isEmpty()) {
                    label = label.substring(0, label.length() - 1);
                }
                label += "...";
            }

            this.font.draw(poseStack, label, startX, y, 0xd4d9d6);

            String rangeText = String.valueOf(this.menu.getRange(type));
            this.font.draw(poseStack, rangeText, 168, y, 0xcae8d6);
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, delta);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }
}