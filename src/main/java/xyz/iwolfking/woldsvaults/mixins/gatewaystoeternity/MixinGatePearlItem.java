package xyz.iwolfking.woldsvaults.mixins.gatewaystoeternity;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import shadows.gateways.entity.GatewayEntity;
import shadows.gateways.gate.Gateway;
import shadows.gateways.item.GatePearlItem;

@Mixin(value = GatePearlItem.class, remap = false)
public abstract class MixinGatePearlItem extends Item {

    public MixinGatePearlItem(Properties pProperties) {
        super(pProperties);
    }

    /**
     * @author iwolfking
     * @reason Remove being able to use Gate Pearls anywhere.
     */
    @Overwrite
    public InteractionResult useOn(UseOnContext ctx)  {
        return super.useOn(ctx);
    }
}
