package xyz.iwolfking.woldsvaults.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import xyz.iwolfking.woldsvaults.items.gear.VaultScepterItem;

@Mixin(ItemInHandRenderer.class)
public class MixinItemInHandRendererScepter {
    /**
     * Stretches the first-person bow draw of a charging scepter over its own charge time, so the draw completes on the
     * tick the release can fire.
     */
    @ModifyConstant(method = "renderArmWithItem", constant = @Constant(floatValue = 20.0F))
    private float woldsvaults$scepterDrawTicks(float constant) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return constant;
        }
        ItemStack using = minecraft.player.getUseItem();
        if (using.getItem() instanceof VaultScepterItem) {
            return VaultScepterItem.getChargeTicks(using);
        }
        return constant;
    }
}
