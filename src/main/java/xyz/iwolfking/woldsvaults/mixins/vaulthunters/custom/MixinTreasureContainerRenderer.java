package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.VertexConsumer;
import iskallia.vault.block.TreasureDoorBlock;
import iskallia.vault.block.render.TreasureContainerRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.blocks.LockedTreasureContainerBlock;
import xyz.iwolfking.woldsvaults.client.init.ModChestModels;

import java.util.function.Function;

@Mixin(value = TreasureContainerRenderer.class, remap = false)
public class MixinTreasureContainerRenderer {

    @WrapOperation(method = "render(Liskallia/vault/block/entity/TreasureContainerTileEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V", remap = true,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/Material;buffer(Lnet/minecraft/client/renderer/MultiBufferSource;Ljava/util/function/Function;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private VertexConsumer coloredChest(Material instance, MultiBufferSource pBuffer, Function<ResourceLocation, RenderType> pRenderTypeGetter, Operation<VertexConsumer> original,
            @Local(name = "blockState") BlockState blockState){
        TreasureDoorBlock.Type type = blockState.getOptionalValue(LockedTreasureContainerBlock.TYPE).orElse(null);
        if (type != null) {
            return original.call(ModChestModels.getMaterialForType(type), pBuffer, pRenderTypeGetter);
        }
        return original.call(instance, pBuffer, pRenderTypeGetter);
    }
}
