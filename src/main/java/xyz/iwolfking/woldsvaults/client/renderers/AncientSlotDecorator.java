package xyz.iwolfking.woldsvaults.client.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.gear.data.GearDataCache;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;

/** Paints an ember background under and an ember frame over any ancient unique in a container slot. */
@OnlyIn(Dist.CLIENT)
public final class AncientSlotDecorator {
    private static final ResourceLocation BACKGROUND = WoldsVaults.id("textures/gui/ancient_slot_background.png");
    private static final ResourceLocation FRAME = WoldsVaults.id("textures/gui/ancient_slot_frame.png");
    private static final int SLOT_SIZE = 16;

    private AncientSlotDecorator() {
    }

    public static void renderBackground(PoseStack poseStack, ItemStack stack, int x, int y) {
        if (isAncient(stack)) {
            draw(poseStack, BACKGROUND, x, y);
        }
    }

    public static void renderFrame(PoseStack poseStack, ItemStack stack, int x, int y) {
        if (isAncient(stack)) {
            draw(poseStack, FRAME, x, y);
        }
    }

    private static boolean isAncient(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return false;
        }
        return GearDataCache.of(stack).hasAttribute(ModGearAttributes.ANCIENT_UNIQUE);
    }

    /** Depth testing is off, so the quad lands by draw order: background under the item, frame over it. */
    private static void draw(PoseStack poseStack, ResourceLocation texture, int x, int y) {
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        GuiComponent.blit(poseStack, x, y, 0, 0.0F, 0.0F, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}
