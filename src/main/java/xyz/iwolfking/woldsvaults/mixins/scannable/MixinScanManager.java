package xyz.iwolfking.woldsvaults.mixins.scannable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.block.entity.PylonTileEntity;
import iskallia.vault.init.ModBlocks;
import li.cil.scannable.api.scanning.ScanResult;
import li.cil.scannable.api.scanning.ScanResultProvider;
import li.cil.scannable.api.scanning.ScannerModule;
import li.cil.scannable.client.ScanManager;
import li.cil.scannable.common.capabilities.Capabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

@Mixin(value = ScanManager.class, remap = false)
public abstract class MixinScanManager {
    @Shadow
    public static void cancelScan() {
    }

    @Shadow
    @Final
    private static Set<ScanResultProvider> collectingProviders;

    /**
     * @author iwolfking
     * @reason Hard code scan radius
     */
    @Overwrite
    public static void beginScan(Player player, List<ItemStack> stacks) {
        cancelScan();
        float scanRadius = 64;
        List<ScannerModule> modules = new ArrayList<>();

        for (ItemStack stack : stacks) {
            LazyOptional<ScannerModule> module = stack.getCapability(Capabilities.SCANNER_MODULE_CAPABILITY);
            Objects.requireNonNull(modules);
            module.ifPresent(modules::add);
        }

        for (ScannerModule module : modules) {
            ScanResultProvider provider = module.getResultProvider();
            if (provider != null) {
                collectingProviders.add(provider);
            }

            scanRadius = module.adjustGlobalRange(scanRadius);
        }

        if (!collectingProviders.isEmpty()) {
            Vec3 center = player.position();

            for (ScanResultProvider provider : collectingProviders) {
                provider.initialize(player, stacks, center, scanRadius, 40);
            }

        }
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/culling/Frustum;isVisible(Lnet/minecraft/world/phys/AABB;)Z", remap = true))
    private static boolean removeInactivePylons(Frustum instance, AABB bounds, Operation<Boolean> original, @Local ScanResult result) {
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
