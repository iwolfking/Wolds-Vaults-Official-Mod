package xyz.iwolfking.woldsvaults.mixins.vaulthunters.block;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.block.CoinPileBlock;
import iskallia.vault.gear.data.VaultGearData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;

@Mixin(value = CoinPileBlock.class, remap = false)
public abstract class MixinCoinPileBlock extends Block implements EntityBlock {
    @Shadow
    private void generateLoot(Level level, BlockPos pos, Player player) { }

    // dummy constructor
    public MixinCoinPileBlock(Properties pProperties) {
        super(pProperties);
    }

    // We already overwrote the one in VaultChestBlock, so we might as well commit.
    /**
     * @author a1qs
     * @reason add Breaching as a consideration
     */
    @Overwrite
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        CoinPileBlock thisInstance = ((CoinPileBlock) (Object) this);
        int size = state.getValue(CoinPileBlock.SIZE);
        if (!player.isCreative()) {
            thisInstance.playerWillDestroy(level, pos, state, player);

            VaultGearData data = VaultGearData.read(player.getMainHandItem().copy());
            boolean hasBreach = data.hasAttribute(ModGearAttributes.BREACHING);

            if(hasBreach) {
                for (int currentSize = size; currentSize > 0; currentSize--) {
                    generateLoot(level, pos, player);
                }
                level.setBlock(pos, fluid.createLegacyBlock(), level.isClientSide ? 11 : 3);

            } else {
                generateLoot(level, pos, player);
                if (size != 1) {
                    level.setBlock(pos, state.setValue(CoinPileBlock.SIZE, size - 1), level.isClientSide ? 11 : 3);
                } else {
                    level.setBlock(pos, fluid.createLegacyBlock(), level.isClientSide ? 11 : 3);
                }
            }

            return true;
        } else {
            return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
        }
    }
}
