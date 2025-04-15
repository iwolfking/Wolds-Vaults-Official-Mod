package xyz.iwolfking.woldsvaults.mixins;

import com.bawnorton.mixinsquared.TargetHandler;
import com.google.common.base.Strings;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.helper.ScreenDrawHelper;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.data.GearDataCache;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.AntiqueItem;
import iskallia.vault.item.bottle.BottleItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

//Clone of Vault Hunters original Item Renderer mixin due to MixinSquared not wanting to cooperate.

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {
  @Inject(
          method = {"renderGuiItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"},
          at = {@At(
                  value = "INVOKE",
                  target = "Lnet/minecraft/client/gui/Font;drawInBatch(Ljava/lang/String;FFIZLcom/mojang/math/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;ZII)I"
          )},
          locals = LocalCapture.CAPTURE_FAILSOFT
  )
  public void preStackCount(Font font, ItemStack stack, int x, int y, String text, CallbackInfo ci, PoseStack matrixStack) {
    boolean isAntique = stack.is(ModItems.ANTIQUE);
    if (isAntique || text == null && stack.getCount() >= 1000) {
      text = ChatFormatting.stripFormatting(text);
      String countStr = text != null ? text : String.valueOf(stack.getCount());
      int strLength = countStr.length();
      if (isAntique) {
        strLength = Math.max(strLength, 5);
      }

      String countLengthStr = Strings.repeat("8", strLength);
      String comparisonLength = Strings.repeat("8", 3);
      float width = font.width(countLengthStr);
      float compareWidth = font.width(comparisonLength);
      float posX = x + 19 - 2;
      float posY = y + 6 + 3 + 9;
      matrixStack.translate(posX, posY, 0.0);
      float scale = compareWidth / width;
      matrixStack.scale(scale, scale, 1.0F);
      matrixStack.translate(-posX, -posY - 1.0F, 0.0);
    }
  }

  @ModifyVariable(
          method = {"renderGuiItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"},
          at = @At("HEAD"),
          index = 5,
          argsOnly = true
  )
  public String overrideText(String ovrText, Font font, ItemStack stack, int x, int y, String text) {
    if (stack.is(ModItems.ANTIQUE)) {
      text = AntiqueItem.buildAntiqueCountText(text, stack);
    }

    return text;
  }

  @Inject(
          method = {"renderGuiItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"},
          at = {@At(
                  value = "INVOKE",
                  target = "Lnet/minecraft/world/item/ItemStack;isBarVisible()Z"
          )}
  )
  public void renderModifierCategoryOverlay(Font font, ItemStack stack, int x, int y, String text, CallbackInfo ci) {
    GearDataCache cache = GearDataCache.of(stack);
    List<ResourceLocation> icons = new ArrayList<>();
    Stream.of(VaultGearModifier.AffixCategory.values())
            .filter(cat -> cat.getOverlayIcon() != null)
            .filter(cache::hasModifierOfCategory)
            .forEach(cat -> icons.add(cat.getOverlayIcon()));
    if (cache.getState() == VaultGearState.UNIDENTIFIED && cache.hasAttribute(ModGearAttributes.IS_LEGENDARY)) {
      icons.add(VaultGearModifier.AffixCategory.LEGENDARY.getOverlayIcon());
    }

    if(cache.getState() == VaultGearState.UNIDENTIFIED && cache.hasAttribute(xyz.iwolfking.woldsvaults.init.ModGearAttributes.IS_ETCHED)) {
      icons.add(VaultGearModifier.AffixCategory.valueOf("ETCHING").getOverlayIcon());
    }

    if (!icons.isEmpty()) {
      icons.forEach(icon -> {
        RenderSystem.setShaderTexture(0, icon);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableDepthTest();
        ScreenDrawHelper.drawTexturedQuads(buf -> {
          PoseStack pose = new PoseStack();
          ScreenDrawHelper.rect(buf, pose).at(x, y).dim(16.0F, 16.0F).draw();
        });
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
      });
    }
  }

  @Inject(
          method = {"Lnet/minecraft/client/renderer/entity/ItemRenderer;render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemTransforms$TransformType;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V"},
          at = {@At("HEAD")}
  )
  public void animateBottle(
          ItemStack stack, ItemTransforms.TransformType transformType, boolean pLeftHand, PoseStack poseStack, MultiBufferSource pBuffer, int pCombinedLight, int pCombinedOverlay, BakedModel pModel, CallbackInfo ci
  ) {
    if (transformType == ItemTransforms.TransformType.GUI && stack.getItem() == ModItems.BOTTLE) {
      BottleItem.onBeforeGuiItemRender(stack, poseStack);
    }
  }

  @Inject(
          method = {"Lnet/minecraft/client/renderer/entity/ItemRenderer;render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemTransforms$TransformType;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V"},
          at = {@At("TAIL")}
  )
  public void animateBottlePop(
          ItemStack stack, ItemTransforms.TransformType transformType, boolean pLeftHand, PoseStack poseStack, MultiBufferSource pBuffer, int pCombinedLight, int pCombinedOverlay, BakedModel pModel, CallbackInfo ci
  ) {
    if (transformType == ItemTransforms.TransformType.GUI && stack.getItem() == ModItems.BOTTLE) {
      BottleItem.onAfterGuiItemRender(stack, poseStack);
    }
  }

  @Inject(
          method = {"Lnet/minecraft/client/renderer/entity/ItemRenderer;renderGuiItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"},
          at = {@At("TAIL")}
  )
  public void renderBottleProgressNotification(Font font, ItemStack stack, int xPosition, int yPosition, String text, CallbackInfo ci) {
    if (stack.getItem() == ModItems.BOTTLE) {
      BottleItem.renderBottleProgressNotification(font, stack, xPosition, yPosition);
    }
  }

}
