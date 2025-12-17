package xyz.iwolfking.woldsvaults.mixins.fruitbag;

import com.shiftthedev.vaultfruitbag.containers.FruitSlot;
import iskallia.vault.container.slot.ConditionalReadSlot;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.function.BiPredicate;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "vaultfruitbag")
        }
)

@Mixin(value = FruitSlot.class, remap = false)
public class MixinFruitSlot extends ConditionalReadSlot {

    public MixinFruitSlot(IItemHandler inventory, int index, int xPosition, int yPosition, BiPredicate<Integer, ItemStack> slotPredicate) {
        super(inventory, index, xPosition, yPosition, slotPredicate);
    }

    /**
     * @author iwolfking
     * @reason Unlimted slot size
     */
    @Overwrite(remap = true)
    public int getMaxStackSize() {
        return Integer.MAX_VALUE;
    }

    /**
     * @author iwolfking
     * @reason Unlimited stack size
     */
    @Overwrite(remap = true)
    public int getMaxStackSize(@NotNull ItemStack stack) {
        return Integer.MAX_VALUE;
    }
}
