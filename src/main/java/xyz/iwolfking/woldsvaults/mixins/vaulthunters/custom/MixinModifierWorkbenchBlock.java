package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.block.ModifierWorkbenchBlock;
import iskallia.vault.block.base.FacedBlock;
import iskallia.vault.block.entity.ModifierWorkbenchTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import xyz.iwolfking.woldsvaults.api.util.GameruleHelper;
import xyz.iwolfking.woldsvaults.init.ModGameRules;

@Mixin(value = ModifierWorkbenchBlock.class, remap = false)
public abstract class MixinModifierWorkbenchBlock extends FacedBlock implements EntityBlock {

    public MixinModifierWorkbenchBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if(pLevel.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        else if(pPlayer instanceof ServerPlayer player) {
            if(pLevel.getBlockEntity(pPos) instanceof ModifierWorkbenchTileEntity modifierWorkbenchTileEntity) {
                if(GameruleHelper.isEnabled(ModGameRules.ENABLE_MODIFIER_WORKBENCH, pLevel)) {
                    NetworkHooks.openGui(player, modifierWorkbenchTileEntity, friendlyByteBuf -> friendlyByteBuf.writeBlockPos(pPos));
                }
            }
        }

        return InteractionResult.SUCCESS;
    }
}
