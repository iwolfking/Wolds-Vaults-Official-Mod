package xyz.iwolfking.woldsvaults.mixins.scannable;

import li.cil.scannable.common.scanning.RangeScannerModule;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "scannable")
        }
)
@Mixin(value = RangeScannerModule.class, remap = false)
public class MixinRangeScannerModule {

    /**
     * @author iwolfking
     * @reason Hard code range upgrade amount
     */
    @Overwrite
    @OnlyIn(Dist.CLIENT)
    public float adjustGlobalRange(float range) {
        return range * 2F;
    }
}
