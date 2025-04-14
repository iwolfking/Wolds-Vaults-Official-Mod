package xyz.iwolfking.woldsvaults.blocks.containers;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.framework.element.CraftingSelectorElement;
import iskallia.vault.client.gui.screen.block.base.ForgeRecipeContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import xyz.iwolfking.woldsvaults.blocks.tiles.AugmentCraftingTableTileEntity;
import xyz.iwolfking.woldsvaults.blocks.tiles.ModBoxWorkstationTileEntity;

import javax.annotation.Nonnull;

public class ModBoxWorkstationScreen extends ForgeRecipeContainerScreen<ModBoxWorkstationTileEntity, ModBoxWorkstationContainer> {
    public ModBoxWorkstationScreen(ModBoxWorkstationContainer container, Inventory inventory, Component title) {
        super(container, inventory, title, 173);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float v, int i, int i1) {

    }

    @Nonnull
    protected CraftingSelectorElement<?> createCraftingSelector() {
        return this.makeCraftingSelector();
    }
}
