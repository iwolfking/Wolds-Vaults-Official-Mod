package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.jei_sync;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.framework.element.CraftingSelectorElement;
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
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;

@Mixin(value = CraftingSelectorElement.class, remap = false)
public class MixinCraftingSelectorElement<E extends CraftingSelectorElement<E>> extends ScrollableItemStackSelectorElement<E, CraftingSelectorElement.CraftingEntry> {
    @Unique
    private String woldsvaults$filter;

    public MixinCraftingSelectorElement(ISpatial spatial, int slotColumns, SelectorModel<CraftingSelectorElement.CraftingEntry> selectorModel) {
        super(spatial, slotColumns, selectorModel);
    }

    @Inject(at = @At("TAIL"), method = "render", remap = false)
    public void render(IElementRenderer renderer, PoseStack poseStack, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (Internal.getRuntime().isEmpty() || WoldsVaultsConfig.CLIENT.syncJEISearchForWorkstations.get()) return;
        String newFilter = Internal.getRuntime().get().getIngredientFilter().getFilterText().toLowerCase();
        if (!newFilter.equals(woldsvaults$filter)) {
            woldsvaults$filter = newFilter;
            refreshElements();
        }
        if (!newFilter.isEmpty()) {
            int width = Minecraft.getInstance().font.width(newFilter);
            Minecraft.getInstance().font.draw(poseStack, newFilter, this.right() - width, this.top() - Minecraft.getInstance().font.lineHeight - 1, 0x00AA00);
        }
    }
}
