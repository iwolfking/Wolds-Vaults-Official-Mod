package xyz.iwolfking.woldsvaults.mixins.dungeons_library;

import com.infamous.dungeons_libraries.client.artifactBar.ArtifactsBarRender;
import com.mojang.blaze3d.vertex.PoseStack;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "dungeons_library")
        }
)
@Mixin(value = ArtifactsBarRender.class, remap = false)
public abstract class MixinArtifactsBarRenderer {


    /**
     * @author iwolfking
     * @reason Disable artifact bar
     */
    @SubscribeEvent
    @Overwrite
    public static void displayArtifactBar(RenderGameOverlayEvent.Pre event) {


    }

    /**
     * @author iwolfking
     * @reason Disable artifact bar
     */
    @Overwrite
    private static void renderBar(PoseStack poseStack, Minecraft mc, Player renderPlayer, int x, int y, ICuriosItemHandler iCuriosItemHandler) {

    }
}
