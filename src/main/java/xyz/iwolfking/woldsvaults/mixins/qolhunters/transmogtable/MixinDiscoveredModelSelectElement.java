package xyz.iwolfking.woldsvaults.mixins.qolhunters.transmogtable;


import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.framework.element.DiscoveredModelSelectElement;
import iskallia.vault.client.gui.framework.element.ScrollableItemStackSelectorElement;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import mezz.jei.common.Internal;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//All three of the classes in this package are from QOL Hunters, this has small changes to make it compatible with JEI 10 instead of 9.
//Original credit to JustAHuman
@Mixin(DiscoveredModelSelectElement.class)
public abstract class MixinDiscoveredModelSelectElement<E extends DiscoveredModelSelectElement<E>> extends ScrollableItemStackSelectorElement<E, DiscoveredModelSelectElement.TransmogModelEntry> {
    @Unique
    private String qOLHunters$filter;

    public MixinDiscoveredModelSelectElement(ISpatial spatial, int slotColumns, SelectorModel<DiscoveredModelSelectElement.TransmogModelEntry> selectorModel) {
        super(spatial, slotColumns, selectorModel);
    }

    @Inject(at = @At("TAIL"), method = "render", remap = false)
    public void render(IElementRenderer renderer, PoseStack poseStack, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (Internal.getRuntime().isEmpty()) return;
        String newFilter = Internal.getRuntime().get().getIngredientFilter().getFilterText().toLowerCase();
        if (!newFilter.equals(qOLHunters$filter)) {
            qOLHunters$filter = newFilter;
            refreshElements();
        }
        if (!newFilter.isEmpty()) {
            int width = Minecraft.getInstance().font.width(newFilter);
            Minecraft.getInstance().font.draw(poseStack, newFilter, this.right() - width, this.top() - Minecraft.getInstance().font.lineHeight - 1, 0x00AA00);
        }
    }
}
