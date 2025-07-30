package xyz.iwolfking.woldsvaults.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import iskallia.vault.entity.entity.DollMiniMeEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.jetbrains.annotations.NotNull;
import xyz.iwolfking.woldsvaults.blocks.DollDismantlingBlock;
import xyz.iwolfking.woldsvaults.blocks.tiles.DollDismantlingTileEntity;

/**
 * This class manages block entity rendering
 *  Absolutely full credit to BONNe for the original code, this is a port out of the abandoned More Vault Tables Mod that was never released to Curseforge.
 *  Original licensing is GPLv3.
 */
public class DollDismantlingRenderer implements BlockEntityRenderer<DollDismantlingTileEntity>
{
    public DollDismantlingRenderer(BlockEntityRendererProvider.Context context)
    {
    }


    public void render(DollDismantlingTileEntity tileEntity,
                       float partialTicks,
                       @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer,
                       int combinedLight,
                       int combinedOverlay)
    {
        if (tileEntity.getDoll().isEmpty())
        {
            // Nothing to render.
            return;
        }

        DollMiniMeEntity miniMeEntity = tileEntity.getMiniMeEntity();

        if (miniMeEntity == null)
        {
            // Not an entity.
            return;
        }

        float y = 0.6f;

        long amount = tileEntity.getDoll().getOrCreateTag().getLong("amount");
        int totalItemsInDoll = tileEntity.getTotalItemsInDoll();

        float result = totalItemsInDoll * 1f / (amount == 0 ? Integer.MAX_VALUE : amount);
        y *= result;

        // Position the player model inside the block
        poseStack.pushPose();
        poseStack.translate(0.5, y, 0.5);
        poseStack.scale(0.6f, 0.6f, 0.6f);

        if (tileEntity.canOperate())
        {
            if(tileEntity.getLevel() == null) {
                return;
            }
            long gameTime = tileEntity.getLevel().getGameTime();
            float rotationAngle =
                    (gameTime % 360L) * (float) 25D;

            poseStack.mulPose(Vector3f.YP.rotationDegrees(rotationAngle));
        }
        else
        {
            poseStack.mulPose(Vector3f.YP.rotationDegrees(
                    tileEntity.getBlockState().getValue(DollDismantlingBlock.FACING).toYRot()));
        }

        // Use the player renderer to render the player
        Minecraft.getInstance().getEntityRenderDispatcher().
                getRenderer(miniMeEntity).
                render(miniMeEntity, 0.0f, partialTicks, poseStack, buffer, combinedLight);

        poseStack.popPose();
    }
}
