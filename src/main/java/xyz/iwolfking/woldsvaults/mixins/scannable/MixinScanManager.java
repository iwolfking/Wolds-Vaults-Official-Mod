package xyz.iwolfking.woldsvaults.mixins.scannable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.block.entity.PylonTileEntity;
import iskallia.vault.init.ModBlocks;
import li.cil.scannable.api.scanning.ScanResult;
import li.cil.scannable.client.ScanManager;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Predicate;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "scannable")
    }
)
@Mixin(value = ScanManager.class, remap = false)
public abstract class MixinScanManager {

    @Redirect(method = "beginScan", at = @At(value = "FIELD", target = "Lli/cil/scannable/common/config/CommonConfig;baseScanRadius:I", opcode = Opcodes.GETSTATIC))
    private static int hardCodeScanRadius() {
        return 64;
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/culling/Frustum;isVisible(Lnet/minecraft/world/phys/AABB;)Z", remap = true))
    private static boolean removeUsedBlocks(Frustum instance, AABB bounds, Operation<Boolean> original, @Local ScanResult result) {
        if (!original.call(instance, bounds)) {
            return false;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return true;
        }
        if (result instanceof BlockScanResultAccessor blockRes) {
            var block = blockRes.getBlock();
            if (block == ModBlocks.PYLON) {
                for (BlockPos pos : blockRes.getBlocks()) {
                    if (level.getBlockEntity(pos) instanceof PylonTileEntity pe && !pe.isConsumed()) {
                        return true;
                    }
                }
                return false;
            }
            if (block == ModBlocks.DUNGEON_DOOR || block == ModBlocks.VENDOR_DOOR || block == ModBlocks.TREASURE_DOOR) {
                Predicate<BlockState> isOpen = (BlockState s) -> s.hasProperty(DoorBlock.OPEN) && s.getValue(DoorBlock.OPEN);
                for (BlockPos pos : blockRes.getBlocks()) {
                    if (!isOpen.test(level.getBlockState(pos))){
                        return true; // at least one isn't open
                    }
                }
                return false;
            }
        }

        return true;
    }
}
