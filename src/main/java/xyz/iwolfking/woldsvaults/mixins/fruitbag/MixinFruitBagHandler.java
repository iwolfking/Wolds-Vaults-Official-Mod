package xyz.iwolfking.woldsvaults.mixins.fruitbag;

import com.shiftthedev.vaultfruitbag.containers.FruitSlot;
import com.shiftthedev.vaultfruitbag.handlers.FruitBagHandler;
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

@Mixin(value = FruitBagHandler.class, remap = false)
public class MixinFruitBagHandler {

    /**
     * @author
     * @reason
     */
    @Overwrite
    protected int getStackLimit(int slot, @NotNull ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public int getSlotLimit(int slot) {
        return Integer.MAX_VALUE;
    }
}
