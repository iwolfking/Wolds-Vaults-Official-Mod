package xyz.iwolfking.woldsvaults.mixins.jei;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.ingredients.RecipeSlot;
import mezz.jei.common.gui.recipes.RecipesGui;
import mezz.jei.common.gui.recipes.layout.RecipeLayout;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RecipesGui.class)
public class MixinRecipesGui {
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lmezz/jei/common/gui/recipes/layout/RecipeLayout;isMouseOver(DD)Z", remap = false))
    private boolean captureOutOfBoundsHoveredSlot(RecipeLayout<?> instance, double mouseX, double mouseY, Operation<Boolean> original, @Share("recLayout") LocalRef<RecipeLayout<?>> recLayout) {
        Boolean hoveringOverRecipe = original.call(instance, mouseX, mouseY);
        if (!hoveringOverRecipe) { // not in recipe bg bounds
            RecipeSlot hoveredSlot = instance.getRecipeSlots().getHoveredSlot(mouseX - instance.getPosX(), mouseY - instance.getPosY()).orElse(null);
            if (hoveredSlot != null) {
                recLayout.set(instance);
            }
        }
        return hoveringOverRecipe;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lmezz/jei/common/gui/recipes/layout/RecipeLayout;drawOverlays(Lcom/mojang/blaze3d/vertex/PoseStack;II)V", remap = false))
    private void removeCaptureIfHoveredInBounds(PoseStack poseStack, int mouseX, int mouseY, float partialTicks, CallbackInfo ci, @Share("recLayout") LocalRef<RecipeLayout<?>> recLayout) {
        recLayout.set(null);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lmezz/jei/common/gui/HoverChecker;checkHover(DD)Z", remap = false))
    private void renderOutOfBoundsHover(PoseStack poseStack, int mouseX, int mouseY, float partialTicks, CallbackInfo ci, @Share("recLayout") LocalRef<RecipeLayout<?>> recLayout){
        var recipeLayout = recLayout.get();
        if (recipeLayout == null) {
            return;
        }
        var hoveredSlot = recipeLayout.getRecipeSlots().getHoveredSlot(mouseX - recipeLayout.getPosX(), mouseY - recipeLayout.getPosY()).orElse(null);
        if (hoveredSlot == null) {
            return;
        }
        final int posX = recipeLayout.getPosX();
        final int posY = recipeLayout.getPosY();

        final int recipeMouseX = mouseX - posX;
        final int recipeMouseY = mouseY - posY;
        hoveredSlot.drawOverlays(poseStack, posX, posY, recipeMouseX, recipeMouseY, Internal.getHelpers().getModIdHelper());
    }
}
