package xyz.iwolfking.woldsvaults.mixins.scannable;

import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "scannable")
    }
)
@Mixin(targets = {"li.cil.scannable.client.scanning.ScanResultProviderBlock$BlockScanResult"}, remap = false)
public interface BlockScanResultAccessor {
    @Accessor
    Set<BlockPos> getBlocks();

    @Accessor
    Block getBlock();
}
