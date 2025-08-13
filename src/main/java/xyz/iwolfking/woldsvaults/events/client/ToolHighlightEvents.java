package xyz.iwolfking.woldsvaults.events.client;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.block.entity.DehammerizerTileEntity;
import iskallia.vault.event.ActiveFlags;
import iskallia.vault.event.InputEvents;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.tool.ToolItem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.DrawSelectionEvent;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = {Dist.CLIENT})
public class ToolHighlightEvents {

    private static boolean shouldRefreshToolHighlightCache = false;

    private static BlockPos previousPos;
    private static Direction previousFace;
    private static BlockState previousState;
    private static long previousOffset;
    private static final List<BlockPos> cachedPositions = new ArrayList<>();

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!shouldRefreshToolHighlightCache) return;
        Player player = Minecraft.getInstance().player;
        if (player == null || previousPos == null || previousFace == null) return;

        if (!InputEvents.isShiftDown()
                || ActiveFlags.IS_AOE_MINING.isSet()
                || player.getMainHandItem().getItem() != ModItems.TOOL
                || DehammerizerTileEntity.hasDemhammerizerAround(player)) {
            return;
        } // TODO ensure its a hammer




        Level level = player.getLevel();
        BlockPos pos = previousPos;
        Direction face = previousFace;
        ItemStack stack = player.getMainHandItem();


        cachedPositions.clear();
        ToolItem.getHammerPositions(level, stack, pos, face, player)
                .forEachRemaining(cachedPositions::add);

        shouldRefreshToolHighlightCache = false;
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onBlockHighlight(DrawSelectionEvent.HighlightBlock event) {
        if (!(event.getCamera().getEntity() instanceof Player player)
                || !InputEvents.isShiftDown()
                || ActiveFlags.IS_AOE_MINING.isSet()
                || player.getMainHandItem().getItem() != ModItems.TOOL
                || DehammerizerTileEntity.hasDemhammerizerAround(player)) {
            return;
        }


        BlockHitResult target = event.getTarget();
        BlockPos pos = target.getBlockPos();
        Direction face = target.getDirection();
        Level level = player.getLevel();
        BlockState blockState = level.getBlockState(pos);
        long offset = player.getMainHandItem().getOrCreateTag().getLong("offset");

        if (!blockState.equals(previousState)
                || !pos.equals(previousPos)
                || !face.equals(previousFace)
                || offset != previousOffset) {

            shouldRefreshToolHighlightCache = true;
            previousPos = pos;
            previousFace = face;
            previousState = blockState;
            previousOffset = offset;
        }

        if (cachedPositions.isEmpty()) return;


        MultiBufferSource multiBufferSource = event.getMultiBufferSource();
        PoseStack poseStack = event.getPoseStack();
        Camera camera = event.getCamera();
        var cameraPos = camera.getPosition();
        var vertexConsumer = Lazy.of(() -> multiBufferSource.getBuffer(RenderType.lines())).get();

        for (BlockPos p : cachedPositions) {
            if (p.equals(pos)) continue;
            if (!level.getWorldBorder().isWithinBounds(p)) continue;

            BlockState state = level.getBlockState(p);
            if (state.isAir()) continue;

            VoxelShape shape = state.isCollisionShapeFullBlock(level, p) ? Shapes.block() : state.getShape(level, p, CollisionContext.of(player));
            LevelRenderer.renderShape(
                    poseStack,
                    vertexConsumer,
                    shape,
                    p.getX() - cameraPos.x(),
                    p.getY() - cameraPos.y(),
                    p.getZ() - cameraPos.z(),
                    0.0F, 1F, 0.0F, 0.7F
            );
        }
    }

}
