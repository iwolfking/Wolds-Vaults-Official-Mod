package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import iskallia.vault.block.TreasureDoorBlock;
import iskallia.vault.block.entity.TreasureContainerTileEntity;
import iskallia.vault.block.render.TreasureContainerRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.blocks.LockedTreasureContainerBlock;
import xyz.iwolfking.woldsvaults.client.init.ModModelLayers;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.models.treasure.LockModel;

@Mixin(value = TreasureContainerRenderer.class, remap = false)
public class MixinTreasureContainerRenderer {
    @Unique
    private LockModel woldsVaults$lockModel = null;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initLockModel(BlockEntityRendererProvider.Context context, CallbackInfo ci){
        this.woldsVaults$lockModel = new LockModel(context.bakeLayer(ModModelLayers.LOCK));
    }

    @Inject(method = "render(Liskallia/vault/block/entity/TreasureContainerTileEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V", remap = true,
        at = @At(value = "INVOKE", target = "Liskallia/vault/block/model/VaultChestModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V", shift = At.Shift.AFTER))
    private void renderLock(TreasureContainerTileEntity tileEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, CallbackInfo ci){
        var blockState = tileEntity.getBlockState();
        if (blockState.getBlock() == ModBlocks.LOCKED_TREASURE_CONTAINER_BLOCK
                && !blockState.getOptionalValue(LockedTreasureContainerBlock.UNLOCKED).orElse(false)) {
            TreasureDoorBlock.Type type = blockState.getOptionalValue(LockedTreasureContainerBlock.TYPE).orElse(null);
            var lockTexture =  WoldsVaults.id("textures/entity/chest/lock.png");
            if (type != null) {
                lockTexture = WoldsVaults.id("textures/entity/chest/locked_treasure/lock_"+type.getSerializedName()+".png");
            }
            VertexConsumer lockBuffer = bufferSource.getBuffer(RenderType.entityCutout(lockTexture));
            poseStack.pushPose();
            poseStack.translate(0.5 - 0.5/16, 1.5F/16.0F, 17.2F/16.0F);
            woldsVaults$lockModel.renderToBuffer(poseStack, lockBuffer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
            poseStack.popPose();
        }
    }
}
