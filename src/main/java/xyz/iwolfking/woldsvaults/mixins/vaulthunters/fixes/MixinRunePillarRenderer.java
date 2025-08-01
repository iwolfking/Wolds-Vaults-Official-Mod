package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.block.render.RunePillarRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.IntFunction;

@Mixin(value = RunePillarRenderer.class, remap = false)
public class MixinRunePillarRenderer {

    @ModifyVariable(method = "renderFloatingItems", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    private static IntFunction<Double> changeAngleCalculation(IntFunction<Double> value, PoseStack ignored0, MultiBufferSource ignored1, int ignored2, int ignored3, int count) {

        return count > 100 ?
                (i) -> (double)((float)i / (float)100 * 2.0F) * Math.PI + Math.PI * ((double)System.currentTimeMillis() / (double)5000.0F) % (Math.PI * 2D) :
                value;
    }

    @ModifyVariable(method = "renderFloatingItems", at = @At("HEAD"), ordinal = 2, argsOnly = true)
    private static int stopRenderingSoMuch(int count) {
        return Math.min(count, 100);
    }
}
