package xyz.iwolfking.woldsvaults.mixins.vaulthunters.altar;

import iskallia.vault.altar.AltarInfusionRecipe;
import iskallia.vault.block.entity.VaultAltarTileEntity;
import iskallia.vault.world.data.PlayerVaultAltarData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import xyz.iwolfking.woldsvaults.api.helper.PlayerVaultAltarDataHelper;
import xyz.iwolfking.woldsvaults.init.ModItems;

import java.util.List;
import java.util.UUID;

@Mixin(value = VaultAltarTileEntity.class,remap = false)
public abstract class MixinVaultAltarTileEntity extends BlockEntity {
    @Shadow private UUID owner;
    @Shadow private VaultAltarTileEntity.AltarState altarState;
    @Shadow private AltarInfusionRecipe recipe;

    public MixinVaultAltarTileEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    @Shadow protected abstract void updateDisplayedIndex(AltarInfusionRecipe infusionRecipe);

    @Shadow public abstract void setAltarState(VaultAltarTileEntity.AltarState state);

    @Shadow private ItemStack input;

    @Shadow public abstract void sendUpdates();

    /**
     * @author iwolfking
     * @reason Greedy vault rock.
     */
    @Overwrite
    public InteractionResult onAddInput(ServerPlayer player, ItemStack input) {
        if (this.level != null && this.owner.equals(player.getUUID())) {
            ServerLevel serverLevel = (ServerLevel) this.level;
            List<BlockPos> altarPositions = PlayerVaultAltarData.get(serverLevel).getAltars(player.getUUID());
            for (BlockPos altarPosition: altarPositions) {
                if (serverLevel.isLoaded(altarPosition)) {
                    BlockEntity te = serverLevel.getBlockEntity(altarPosition);
                    if (te instanceof VaultAltarTileEntity altar) {
                        if (altar.getAltarState() == VaultAltarTileEntity.AltarState.INFUSING) {
                            return InteractionResult.FAIL;
                        }
                    }
                }
            }

            if(input != null && input.getItem().equals(ModItems.GREEDY_VAULT_ROCK)) {
                this.recipe = PlayerVaultAltarDataHelper.generateGreedRecipe(player, this.worldPosition, true);
            }
            else {
                PlayerVaultAltarData altarData = PlayerVaultAltarData.get(serverLevel);
                this.recipe = altarData.getRecipe(player, this.worldPosition);
            }


            this.updateDisplayedIndex(this.recipe);
            this.setAltarState(VaultAltarTileEntity.AltarState.ACCEPTING);
            this.input = input.copy();
            this.input.setCount(1);
            if (!player.isCreative()) {
                input.shrink(1);
            }

            this.sendUpdates();
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.FAIL;
        }
    }
}
