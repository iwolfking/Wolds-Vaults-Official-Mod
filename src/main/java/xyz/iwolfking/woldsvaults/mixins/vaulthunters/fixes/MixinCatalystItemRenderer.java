package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.item.render.CatalystItemRenderer;
import iskallia.vault.item.render.core.SpecialItemRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = CatalystItemRenderer.class, remap = false)
public class MixinCatalystItemRenderer extends SpecialItemRenderer {
    /**
     * @author iwolfking
     * @reason Fix Infused Vault Catalyst with no model being invisible
     */
    @Overwrite(remap = true)
    @Override
    public void renderByItem(@NotNull ItemStack stack, @NotNull ItemTransforms.@NotNull TransformType transformType, @NotNull PoseStack matrices, @NotNull MultiBufferSource buffer, int light, int overlay) {
        int model = stack.getTag() == null ? -1 : stack.getTag().getInt("model");
        model = (model == -1) ? 0 : model;
        if (model >= 0) {
            ModelResourceLocation shape = new ModelResourceLocation("the_vault:catalyst/%d#inventory".formatted(model));
            this.renderModel(shape, 16777215, stack, transformType, matrices, buffer, light, overlay, null);
        }
    }
}
