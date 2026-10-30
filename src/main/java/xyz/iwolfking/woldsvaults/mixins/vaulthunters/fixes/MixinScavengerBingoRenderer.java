package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.ScavengerBingoObjective;
import iskallia.vault.core.vault.objective.ScavengerBingoRenderer;
import iskallia.vault.init.ModOptions;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.objectives.ScalingScavengerBingoCrystalObjective;

@Mixin(value = ScavengerBingoRenderer.class, remap = false)
public class MixinScavengerBingoRenderer {
    @Unique
    private static final ThreadLocal<Integer> woldsVaults$gridSize = ThreadLocal.withInitial(() -> 0);
    @Inject(method = "renderExpanded", at = @At(value = "HEAD"))
    private static void captureGridSize(ScavengerBingoObjective objective, Vault vault, PoseStack poseStack, Window window, float partialTicks, Player player, CallbackInfo ci) {
        woldsVaults$gridSize.set(Math.max(objective.getWidth(), objective.getHeight()));
    }

    @ModifyArg(method = "renderExpandedTile", at = @At(value = "INVOKE", target = "Liskallia/vault/client/gui/framework/element/ScalableItemElement;renderItemStack(Lnet/minecraft/world/item/ItemStack;FFFZ)V"), index = 3)
    private static float modifyScale(float scale) {
        var gs = woldsVaults$gridSize.get();
        if (gs == 0) return scale;
        return ModOptions.OBJECTIVES_HUD_SETTINGS.getValue().getSettings("bingo").getScale() * (4f/gs);
    }

    @ModifyArg(method = "renderExpandedTile", at = @At(value = "INVOKE", target = "Lcom/mojang/math/Vector4f;<init>(FFFF)V"), index = 0)
    private static float modifyPosX(float scale) {
        var gs = woldsVaults$gridSize.get();
        if (gs == 0) return scale;
        return ScalingScavengerBingoCrystalObjective.posX(gs);
    }

    @ModifyArg(method = "renderExpandedTile", at = @At(value = "INVOKE", target = "Lcom/mojang/math/Vector4f;<init>(FFFF)V"), index = 1)
    private static float modifyPosY(float scale) {
        var gs = woldsVaults$gridSize.get();
        if (gs == 0) return scale;
        return ScalingScavengerBingoCrystalObjective.posY(gs);
    }

    @ModifyArg(method = "renderCompactTile", at = @At(value = "INVOKE", target = "Liskallia/vault/client/gui/framework/element/ScalableItemElement;renderItemStack(Lnet/minecraft/world/item/ItemStack;FFFZ)V"), index = 3)
    private static float modifyScaleC(float scale) {
        return ModOptions.OBJECTIVES_HUD_SETTINGS.getValue().getSettings("bingo").getScale();
    }

    @ModifyArg(method = "renderCompactTile", at = @At(value = "INVOKE", target = "Lcom/mojang/math/Vector4f;<init>(FFFF)V"), index = 0)
    private static float modifyPosXC(float scale) {
        return -8f/ModOptions.OBJECTIVES_HUD_SETTINGS.getValue().getSettings("bingo").getScale();
    }

    @ModifyArg(method = "renderCompactTile", at = @At(value = "INVOKE", target = "Lcom/mojang/math/Vector4f;<init>(FFFF)V"), index = 1)
    private static float modifyPosYC(float scale) {
        return -8f/ModOptions.OBJECTIVES_HUD_SETTINGS.getValue().getSettings("bingo").getScale();
    }
}
