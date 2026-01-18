
package xyz.iwolfking.woldsvaults.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.atlas.TextureAtlasRegion;
import iskallia.vault.client.gui.framework.ScreenRenderers;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.LabelElement;
import iskallia.vault.client.gui.framework.element.NineSliceElement;
import iskallia.vault.client.gui.framework.element.SlotsElement;
import iskallia.vault.client.gui.framework.element.spi.AbstractSpatialElement;
import iskallia.vault.client.gui.framework.element.spi.IRenderedElement;
import iskallia.vault.client.gui.framework.render.ScreenTooltipRenderer;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.render.spi.ITooltipRenderer;
import iskallia.vault.client.gui.framework.screen.AbstractElementContainerScreen;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;
import javax.annotation.Nonnull;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import xyz.iwolfking.woldsvaults.items.scavenger_pouch.menu.ScavengerPouchContainer;

public class ScavengerPouchScreen extends AbstractElementContainerScreen<ScavengerPouchContainer> {

   public ScavengerPouchScreen(ScavengerPouchContainer container, Inventory inventory, Component title) {
      super(container, inventory, title, ScreenRenderers.getImmediate(), ScreenTooltipRenderer::create);

      int scavRows = container.getScavengerRows();

      int guiHeight =
              container.getPlayerInventoryStartY()
                      + 58
                      + 18
                      + 14;

      this.setGuiSize(Spatials.size(176, guiHeight));


      this.addElement(
         new NineSliceElement<>(this.getGuiSpatial(), ScreenTextures.DEFAULT_WINDOW_BACKGROUND)
            .layout((screen, gui, parent, world) -> world.translateXY(gui).size(Spatials.copy(gui)))
      );
      this.addElement(new SlotsElement<>(this).layout((screen, gui, parent, world) -> world.positionXY(gui)));
      MutableComponent inventoryName = inventory.getDisplayName().copy();
      int inventoryLabelY = container.getPlayerInventoryStartY() - 8;

      this.addElement(
              new LabelElement<>(
                      Spatials.positionXY(8, inventoryLabelY),
                      inventory.getDisplayName(),
                      LabelTextStyle.defaultStyle()
              ).layout((screen, gui, parent, world) -> world.translateXY(gui))
      );

      MutableComponent scavPouchTitle = new TextComponent("Scavenger Pouch").withStyle(ChatFormatting.DARK_GRAY);
      scavPouchTitle.withStyle(Style.EMPTY.withColor(-12632257));
      this.addElement(
         new LabelElement<>(Spatials.positionXY(8, 7), scavPouchTitle, LabelTextStyle.defaultStyle())
            .layout((screen, gui, parent, world) -> world.translateXY(gui))
      );
      this.addElement(
         new ScavengerPouchBackgroundElement(Spatials.positionXY(-1, -1), container, this.getTooltipRenderer())
            .layout((screen, gui, parent, world) -> world.translateXY(gui))
      );
   }

   @Override
   protected boolean renderHoveredSlotTooltips(@Nonnull PoseStack poseStack, int mouseX, int mouseY) {
      if (this.hoveredSlot != null && this.hoveredSlot instanceof ScavengerPouchContainer.ScavengerItemSlot scavSlot && !this.hoveredSlot.hasItem()) {
         this.renderTooltip(poseStack, scavSlot.getScavStack(), mouseX, mouseY);
      }

      return super.renderHoveredSlotTooltips(poseStack, mouseX, mouseY);
   }

   public static class ScavengerPouchBackgroundElement extends AbstractSpatialElement<ScavengerPouchBackgroundElement> implements IRenderedElement {
      private final ITooltipRenderer tooltipRenderer;
      private final ScavengerPouchContainer container;

      public ScavengerPouchBackgroundElement(ISpatial position, ScavengerPouchContainer container, ITooltipRenderer tooltipRenderer) {
         super(position);
         this.tooltipRenderer = tooltipRenderer;
         this.container = container;
      }

      @Override
      public void setVisible(boolean visible) {
      }

      @Override
      public boolean isVisible() {
         return true;
      }

      @Override
      public void render(IElementRenderer renderer, @NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
         TextureManager mgr = Minecraft.getInstance().getTextureManager();
         mgr.getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
         RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }
}