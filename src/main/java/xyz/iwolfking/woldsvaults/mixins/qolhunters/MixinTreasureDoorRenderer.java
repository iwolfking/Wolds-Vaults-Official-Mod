package xyz.iwolfking.woldsvaults.mixins.qolhunters;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import io.iridium.qolhunters.config.QOLHuntersClientConfigs;
import io.iridium.qolhunters.features.treasuredoors.TreasureDoorTileEntityRenderer;
import iskallia.vault.block.TreasureDoorBlock;
import iskallia.vault.block.entity.TreasureDoorTileEntity;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "qolhunters")
        }
)
@Mixin(value = TreasureDoorTileEntityRenderer.class, remap = false)
public class MixinTreasureDoorRenderer {
    @Shadow private String playerName;

    /**
     * @author iwolfking
     * @reason lazy overwrite fix, hush it
     */
    @Overwrite
    public void render(TreasureDoorTileEntity tileEntity, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        if (QOLHuntersClientConfigs.TREASURE_DOOR_NAMES.get()) {
            if (tileEntity.getBlockState().getValue(TreasureDoorBlock.HALF) != DoubleBlockHalf.LOWER) {
                poseStack.pushPose();
                Direction facing = tileEntity.getBlockState().getValue(TreasureDoorBlock.FACING);
                switch (facing) {
                    case NORTH:
                        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
                        poseStack.translate(-1.0, 0.0, -1.0);
                    case SOUTH:
                    default:
                        break;
                    case WEST:
                        poseStack.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
                        poseStack.translate(0.0, 0.0, -1.0);
                        break;
                    case EAST:
                        poseStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
                        poseStack.translate(-1.0, 0.0, 0.0);
                }

                poseStack.translate(0.5, 0.75, 0.3);
                float scale = 0.014F;
                poseStack.scale(scale, -scale, scale);
                String text = tileEntity.getBlockState().getValue(TreasureDoorBlock.TYPE).toString();
                text = text.equals("PETZANITE") ? "PETEZANITE" : text;
                if (this.playerName == null) {
                    this.playerName = Minecraft.getInstance().player.getName().getString();
                }

                text = text.equals("ISKALLIUM") ? "WOLDIUM" : text;
                int xOffset = Minecraft.getInstance().font.width(text);
                Minecraft.getInstance().font.drawInBatch(text, -xOffset / 2.0F, 0.0F, 16777215, true, poseStack.last().pose(), bufferSource, false, 0, combinedLight);
                poseStack.popPose();
            }
        }
    }
}
