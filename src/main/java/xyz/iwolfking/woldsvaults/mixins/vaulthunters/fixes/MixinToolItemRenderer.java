package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.util.ClientScheduler;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.item.render.ToolItemRenderer;
import iskallia.vault.item.render.core.SpecialItemRenderer;
import iskallia.vault.item.tool.ToolMaterial;
import iskallia.vault.item.tool.ToolType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import xyz.iwolfking.woldsvaults.lib.ExtendedToolType;

@Mixin(value = ToolItemRenderer.class, remap = false)
public abstract class MixinToolItemRenderer extends SpecialItemRenderer {
    /**
     * @author iwolfking
     * @reason Display Reaping correctly.
     */
    @Overwrite(remap = true) @Override
    public void renderByItem(@NotNull ItemStack stack, @NotNull ItemTransforms.TransformType transformType, @NotNull PoseStack matrices, @NotNull MultiBufferSource buffer, int light, int overlay) {
        ToolType type = ToolType.of(stack);
        VaultGearData data = VaultGearData.read(stack);
        ToolMaterial material = data.get(ModGearAttributes.TOOL_MATERIAL, VaultGearAttributeTypeMerger.of(() -> null, (a, b) -> b));
        if (material == null) {
            int total = type == null ? 16 * ToolType.values().length : 16;
            material = ToolMaterial.values()[(int) (ClientScheduler.INSTANCE.getTick() / total) % ToolMaterial.values().length];
        }

        if ((type == null && ExtendedToolType.of(stack) == null) || data.hasAttribute(xyz.iwolfking.woldsvaults.init.ModGearAttributes.ROTATING_TOOL)) {
            type = ToolType.values()[(int) (ClientScheduler.INSTANCE.getTick() >> 4) % ToolType.values().length];
        }
        else if(ExtendedToolType.of(stack) != null) {
            ExtendedToolType extendedToolType = ExtendedToolType.of(stack);
            ModelResourceLocation head = new ModelResourceLocation("the_vault:tool/%s/head/%s#inventory".formatted(extendedToolType.getId(), material.getId()));
            ModelResourceLocation handle = new ModelResourceLocation("the_vault:tool/pick/handle#inventory");
            this.renderModel(handle, 16777215, stack, transformType, matrices, buffer, light, overlay, null);
            this.renderModel(head, 16777215, stack, transformType, matrices, buffer, light, overlay, null);
            return;
        }

        ModelResourceLocation head = new ModelResourceLocation("the_vault:tool/%s/head/%s#inventory".formatted(type.getId(), material.getId()));
        ModelResourceLocation handle = new ModelResourceLocation("the_vault:tool/%s/handle#inventory".formatted(type.getId()));
        this.renderModel(handle, 16777215, stack, transformType, matrices, buffer, light, overlay, null);
        this.renderModel(head, 16777215, stack, transformType, matrices, buffer, light, overlay, null);
    }
}
