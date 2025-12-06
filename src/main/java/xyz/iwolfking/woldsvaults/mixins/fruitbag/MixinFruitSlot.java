package xyz.iwolfking.woldsvaults.mixins.fruitbag;

import com.shiftthedev.vaultfruitbag.containers.FruitSlot;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "vaultfruitbag")
        }
)

@Mixin(value = FruitSlot.class, remap = false)
public class MixinFruitSlot {

    /**
     * @author iwolfking
     * @reason Unlimted slot size
     */
    @Overwrite
    public int getMaxStackSize() {
        return Integer.MAX_VALUE;
    }

    /**
     * @author iwolfking
     * @reason Unlimited stack size
     */
    @Overwrite
    public int getMaxStackSize(@NotNull ItemStack stack) {
        return Integer.MAX_VALUE;
    }
}
