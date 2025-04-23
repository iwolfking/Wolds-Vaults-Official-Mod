package xyz.iwolfking.woldsvaults.mixins.itemborders;

import com.anthonyhilyard.itemborders.ItemBorders;
import com.mojang.blaze3d.vertex.PoseStack;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;
@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "itemborders")
        }
)
@Mixin(value = ItemBorders.class, remap = false)
public abstract class MixinItemBorders {
    @Shadow
    private static void render(PoseStack poseStack, ItemStack item, int x, int y) {
    }

    @Inject(method = "renderBorder(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemStack;II)V", at = @At("HEAD"))
    private static void renderBorder(PoseStack poseStack, ItemStack item, int x, int y, CallbackInfo ci) {
        if ((Boolean) WoldsVaultsConfig.CLIENT.displayItemBordersInTerminals.get()) {
            render(new PoseStack(), item, x, y);
            return;
        }

    }
}
