package xyz.iwolfking.woldsvaults.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.refinedmods.refinedstorage.container.FilterContainerMenu;
import me.desht.pneumaticcraft.common.item.ClassifyFilterItem;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import vazkii.quark.base.client.handler.InventoryButtonHandler;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gui.menus.FilterNecklaceMenu;

public class FilterNecklaceContainerScreen extends AbstractContainerScreen<FilterNecklaceMenu> {
    public static final ResourceLocation TEXTURE = WoldsVaults.id("textures/gui/filter_necklace.png");

    public FilterNecklaceContainerScreen(FilterNecklaceMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = 176;
        this.imageHeight = 133;
    }

    public void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(poseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(stack);
        super.render(stack, mouseX, mouseY, partialTicks);
        renderTooltip(stack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(PoseStack stack, int x, int y) {
        font.draw(stack, title, 8, 6, 0x404040);
       // font.draw(stack, playerInventoryTitle, 8, 6, 0x404040);
    }
}
