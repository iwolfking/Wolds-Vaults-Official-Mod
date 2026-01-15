package xyz.iwolfking.woldsvaults.integration.cctweaked;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import iskallia.vault.block.VaultPortalBlock;
import iskallia.vault.block.entity.VaultPortalTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class VaultPortalPeripheralProvider implements IPeripheralProvider {

    @Override
    public @NotNull LazyOptional<IPeripheral> getPeripheral(@NotNull Level level, @NotNull BlockPos blockPos, @NotNull Direction direction) {
        BlockEntity te = level.getBlockEntity(blockPos);
        if (te instanceof VaultPortalTileEntity portalTE) {
            return LazyOptional.of(() -> new VaultPortalPeripheral(portalTE));
        }

        return LazyOptional.empty();
    }
}
