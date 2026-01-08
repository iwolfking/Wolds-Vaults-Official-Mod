package xyz.iwolfking.woldsvaults.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import xyz.iwolfking.woldsvaults.blocks.tiles.CrateCrackerTileEntity;

import java.util.Random;

public class CrateCrackerRenderer
        implements BlockEntityRenderer<CrateCrackerTileEntity>
{
    private final BlockRenderDispatcher blockRenderer;
    private final Random random = new Random();

    public CrateCrackerRenderer(BlockEntityRendererProvider.Context ctx)
    {
        this.blockRenderer = Minecraft.getInstance().getBlockRenderer();
    }

    @Override
    public void render(
        CrateCrackerTileEntity tile,
        float partialTicks,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        int packedOverlay
    )
    {
        ItemStack crate = tile.getCrate();
        if (crate.isEmpty())
            return;

        if (!(crate.getItem() instanceof BlockItem blockItem))
            return;

        int initial = tile.getInitialItemsInCrate();
        int remaining = tile.getTotalItemsInCrate();

        if (initial <= 0)
            return;

        float progress = 1.0F - (remaining / (float) initial);
        progress = Mth.clamp(progress, 0.0F, 1.0F);

        BlockState state = blockItem.getBlock().defaultBlockState();

        poseStack.pushPose();

        // Center inside block
        poseStack.translate(0.5D, 0.1D, 0.5D);

        // Sink as it gets consumed
        poseStack.translate(0.0D, -progress * 0.3D, 0.0D);

        // Shrink as it empties
        float scale = 0.6F * (1.0F - progress * 0.6F);
        poseStack.scale(scale, scale, scale);

        // Slow rotation
        long time = tile.getLevel().getGameTime();
        poseStack.mulPose(Vector3f.YP.rotationDegrees((time + partialTicks) * 3F));

        int light = LevelRenderer.getLightColor(
            tile.getLevel(),
            tile.getBlockPos().above()
        );

        blockRenderer.renderSingleBlock(
            state,
            poseStack,
            buffer,
            light,
            packedOverlay
        );

        poseStack.popPose();
    }
}
