package xyz.iwolfking.woldsvaults.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.AbilityScrollItem;
import iskallia.vault.item.render.core.SpecialItemRenderer;
import iskallia.vault.research.type.Research;
import iskallia.vault.skill.base.Skill;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.iwolfking.woldsvaults.items.ResearchTokenItem;

public class ResearchTokenRenderer extends SpecialItemRenderer {
    public static final ResearchTokenRenderer INSTANCE = new ResearchTokenRenderer();

    public void renderByItem(
            @NotNull ItemStack stack, @NotNull ItemTransforms.TransformType transformType, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int light, int overlay
    ) {
        super.renderByItem(stack, transformType, poseStack, buffer, light, overlay);
        ModelResourceLocation base = new ModelResourceLocation("woldsvaults:research_token_base#inventory");
        this.renderModel(base, 16777215, stack, transformType, poseStack, buffer, light, overlay, null);
        if (ResearchTokenItem.getResearchTag(stack) != null) {
            Research research = ResearchTokenItem.getResearchTag(stack);
            ResourceLocation icon = ModConfigs.RESEARCHES_GUI.getStyles().get(research.getName()).icon;
            if (icon == null) {
                return;
            }

            String[] split = icon.toString().split("/");
            String last = split[split.length - 1];
            ModelResourceLocation model = new ModelResourceLocation("woldsvaults:researches/" + last + "#inventory");
            poseStack.pushPose();
            poseStack.scale(0.75F, 0.75F, 1.0F);
            poseStack.translate(0.25, 0.1F, 0.01F);
            if (transformType == ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND) {
                poseStack.translate(-0.1F, 0.0, 0.0);
            } else if (transformType.equals(ItemTransforms.TransformType.GROUND)) {
                poseStack.translate(0.0, 0.0, 0.0);
            }

            this.renderModel(model, 16777215, stack, transformType, poseStack, buffer, light, overlay, null);
            poseStack.popPose();
        }
    }
}
