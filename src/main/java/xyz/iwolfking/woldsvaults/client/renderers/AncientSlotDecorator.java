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

/**
 * Diablo style slot dressing for ancient uniques: an ember background painted under the item sprite and
 * a mostly transparent ember frame painted over it, in every container screen slot that holds one.
 * <p>
 * Detection goes through GearDataCache, the same client side cache the base game reads gear attributes
 * from while rendering. The first query on a stack decodes its gear data once and memoises the answer
 * into the stack's own clientCache tag; every later frame reads a single NBT byte. Stacks with no gear
 * data at all never get that far, because GearDataCache.of hands back an empty backed cache for them.
 */
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

    /**
     * Depth testing is switched off so the quad lands purely by draw order, which is what makes the
     * background sit under the item and the frame over it despite the item model occupying a whole depth
     * range of its own. This is the same approach the base game's own slot overlays use in
     * GearRarityRenderer and MixinItemRenderer.
     */
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
