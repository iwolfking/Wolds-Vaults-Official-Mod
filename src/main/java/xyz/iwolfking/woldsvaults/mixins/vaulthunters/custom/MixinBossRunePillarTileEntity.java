package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.block.entity.BossRunePillarTileEntity;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BossRunePillarTileEntity.class, remap = false)
public abstract class MixinBossRunePillarTileEntity extends BlockEntity {
    public MixinBossRunePillarTileEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }
}
