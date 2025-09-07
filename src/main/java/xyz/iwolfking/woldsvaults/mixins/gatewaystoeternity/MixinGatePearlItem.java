package xyz.iwolfking.woldsvaults.mixins.gatewaystoeternity;

import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import shadows.gateways.item.GatePearlItem;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "gateways")
    }
)
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
